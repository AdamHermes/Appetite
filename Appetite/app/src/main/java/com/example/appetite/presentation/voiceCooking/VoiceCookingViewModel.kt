package com.example.appetite.presentation.voiceCooking

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.annotation.RequiresPermission
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appetite.repository.RecipeRepository
import com.example.appetite.util.AudioRecordRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import okhttp3.*
import okio.ByteString
import android.speech.tts.TextToSpeech
import android.util.Log
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.ContextCompat
import com.example.appetite.network.ApiClient


@HiltViewModel
class VoiceCookingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recipeId: String = checkNotNull(savedStateHandle["recipeId"])
    private val _uiState = MutableStateFlow(VoiceCookingUiState())
    val uiState: StateFlow<VoiceCookingUiState> = _uiState

    private var tts: TextToSpeech? = null
    private var audioRecorder: AudioRecordRecorder? = null
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private lateinit var appContext: Context
    private var pendingSpeech: String? = null
    private var pendingSpeechQueue: MutableList<String> = mutableListOf()




    init {
        loadRecipe()
    }

    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    private fun startRecorderOnly() {
        audioRecorder?.stop()
        audioRecorder = AudioRecordRecorder().apply {
            start { pcmBytes ->
                webSocket?.send(ByteString.of(*pcmBytes))
            }
        }
    }

    fun initTTS(context: Context, onReady: (() -> Unit)? = null) {
        appContext = context.applicationContext
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US

                // Attach listener
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d("VoiceCookingVM", "TTS started")
                    }

                    override fun onDone(utteranceId: String?) {
                        Log.d("VoiceCookingVM", "TTS finished")
                        viewModelScope.launch(Dispatchers.Main) {
                            if (_uiState.value.isListening) {
                                val ctx = appContext  // you’ll need a reference to Context (see below)
                                if (ContextCompat.checkSelfPermission(
                                        ctx,
                                        android.Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    startRecorderOnly()
                                } else {
                                    Log.w("VoiceCookingVM", "Mic restart skipped: RECORD_AUDIO permission not granted")
                                }
                            }
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        Log.e("VoiceCookingVM", "TTS error")
                    }
                })

                if (pendingSpeechQueue.isNotEmpty()) {
                    viewModelScope.launch {
                        for (text in pendingSpeechQueue) {
                            speakResponseAgent(text)
                        }
                        pendingSpeechQueue.clear()
                    }
                }

                onReady?.invoke()
            }
        }
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            val recipe = RecipeRepository.getRecipeById(recipeId)
            val steps = recipe.steps ?: emptyList()

            _uiState.update {
                it.copy(
                    recipeName = recipe.name ?: "Cooking Agent",
                    recipeSteps = steps,
                    currentStepIndex = 0
                )
            }
            Log.d("VoiceCookingVM", "Loaded recipe: $recipe")

            if (steps.isNotEmpty()) {
                pendingSpeech = steps[0] // queue it
                Log.d("VoiceCookingVM", "Queued first step to speak: ${steps[0]}")
            }

            //val firstStep: String = pendingSpeech ?: "Let's start cooking!"
            //speakResponseAgent(firstStep)

            val firstStep: String = steps.firstOrNull() ?: "Let's start cooking!"
            // Queue it until TTS is ready
            pendingSpeechQueue.add(firstStep)
        }
    }

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    fun toggleListening(context: Context) {
        if (_uiState.value.isListening) {
            stopListening()
        } else {
            startListening(context)
        }
    }

    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    fun startListening(context: Context) {
        _uiState.update { it.copy(isListening = true) }


        // 1. Connect to WebSocket server
//        val request = Request.Builder()
//            .url("ws://10.0.2.2:8000/ws/v1/voice-agent/$recipeId") // 10.0.2.2 for emulator
//            .build()
        val request = Request.Builder()
            .url(ApiClient.getVoiceAgentWsUrl(recipeId))
            .build()
        Log.d("VoiceCookingVM", "URL = ${ApiClient.getVoiceAgentWsUrl(recipeId)}")




        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                viewModelScope.launch {
                    val response = parseServerResponse(text)
                    _uiState.update {
                        it.copy(
                            agentResponse = response.text_response,
                            currentStepIndex = response.current_step
                        )
                    }
                    Log.d("VoiceCookingVM", "Server set currentStepIndex = ${response.current_step}")
                    if (response.intent == "question") {
                        speakResponseAgent(response.text_response)
                    }
                    else if (response.intent != "noise") {
                        val steps = _uiState.value.recipeSteps
                        val idx = response.current_step
                        if (idx in steps.indices) {
                            speakResponseAgent(steps[idx])
                        } else {
                            Log.w("VoiceCookingVM", "Invalid step index: $idx")
                        }
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
                _uiState.update { it.copy(isListening = false) }
            }
        })

        // 2. Start recorder and send PCM chunks
        audioRecorder = AudioRecordRecorder().apply {
            start { pcmBytes ->
                webSocket?.send(ByteString.of(*pcmBytes))
            }
        }

        // Start mic initially
        startRecorderOnly()
    }

    fun stopListening() {
        _uiState.update { it.copy(isListening = false) }
        audioRecorder?.stop()
        audioRecorder = null
        webSocket?.close(1000, "User stopped")
        webSocket = null
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        stopListening()
    }

    private fun parseServerResponse(json: String): ServerResponse {
        return try {
            val obj = JSONObject(json)
            ServerResponse(
                intent = obj.optString("intent"),
                transcript = obj.optString("transcript"),
                text_response = obj.optString("text_response"),
                current_step = obj.optInt("current_step", 0)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ServerResponse("", "", json, 0)
        }
    }

    fun updateCurrentStep(index: Int) {
        _uiState.update { it.copy(currentStepIndex = index) }
    }

    private suspend fun speakResponseAgent(text_speak: String) {
        withContext(Dispatchers.Main) {
            if (!_uiState.value.isMuted && text_speak.isNotBlank()) {
                // Stop mic before speaking
                audioRecorder?.stop()
                audioRecorder = null

                tts?.speak(
                    text_speak,
                    TextToSpeech.QUEUE_FLUSH,
                    Bundle(),
                    "voiceCookingUtterance"
                )
                _uiState.update { it.copy(isSpeaking = true) }
            }
        }
    }

}

data class ServerResponse(
    val intent: String,
    val transcript: String,
    val text_response: String,
    val current_step: Int
)