package com.example.appetite.util

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission

class AudioRecordRecorder(
    private val sampleRate: Int = 16000,
    private val bufferSize: Int = AudioRecord.getMinBufferSize(
        16000,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordThread: Thread? = null


    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    fun start(onData: (ByteArray) -> Unit) {
        stop() // ensure cleanup before new start

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        Log.d("AudioRecordRecorder", "AudioRecord state=${audioRecord?.state}")

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioRecordRecorder", "Failed to init AudioRecord")
            audioRecord = null
            return
        }

        audioRecord?.startRecording()
        isRecording = true

        recordThread = Thread {
            val pcmBuffer = ByteArray(bufferSize)
            try {
                while (isRecording) {
                    val read = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                    if (read > 0) {
                        val chunk = pcmBuffer.copyOf(read)
                        val rms = rmsOfPcm16(chunk, read)
                        Log.d("AudioRecordRecorder", "read=$read, rms=$rms")
                        onData(chunk)
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioRecordRecorder", "Recording error", e)
            } finally {
                try { audioRecord?.stop() } catch (_: Exception) {}
                audioRecord?.release()
                audioRecord = null
            }
        }
        recordThread?.start()
    }

    fun stop() {
        isRecording = false
        try {
            recordThread?.join(500) // wait max 500ms for thread to finish
        } catch (_: InterruptedException) {}
        recordThread = null
    }

    private fun rmsOfPcm16(pcm: ByteArray, len: Int): Double {
        var sum = 0.0
        var i = 0
        while (i + 1 < len) {
            val lo = pcm[i].toInt() and 0xFF
            val hi = pcm[i + 1].toInt() // signed ok
            val s = (hi shl 8) or lo
            val f = s.toShort().toInt() / 32768.0
            sum += f * f
            i += 2
        }
        val n = (len / 2).coerceAtLeast(1)
        return kotlin.math.sqrt(sum / n)
    }

}
