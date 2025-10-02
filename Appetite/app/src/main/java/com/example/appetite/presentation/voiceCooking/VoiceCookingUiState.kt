// VoiceCookingUiState.kt
package com.example.appetite.presentation.voiceCooking

data class VoiceCookingUiState(
    val isListening: Boolean = true,
    val isSpeaking: Boolean = false,
    val isMuted: Boolean = false,

    val recipeName: String = "",
    val recipeSteps: List<String> = emptyList(), // 👈 full list of steps
    val currentStepIndex: Int = 0,               // 👈 which step we’re on

    val agentResponse: String = ""
) {
    val currentStep: String
        get() = recipeSteps.getOrNull(currentStepIndex) ?: "No steps available"
}

data class VoiceMessage(
    val fromUser: Boolean,
    val text: String
)