package com.jvk.studio.audio

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Kotlin bridge to the native Oboe C++ audio engine.
 * All audio processing runs on the native audio thread at ultra-low latency.
 * This class is the single entry point for all audio operations.
 */
class AudioBridge private constructor() {

    private val _isRunning    = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _sampleRate   = MutableStateFlow(48000)
    val sampleRate: StateFlow<Int> = _sampleRate

    private val _bufferSize   = MutableStateFlow(0)
    val bufferSize: StateFlow<Int> = _bufferSize

    private val _cpuLoad      = MutableStateFlow(0f)
    val cpuLoad: StateFlow<Float> = _cpuLoad

    private val _activeVoices = MutableStateFlow(0)
    val activeVoices: StateFlow<Int> = _activeVoices

    // ── Lifecycle ──────────────────────────────────────────────────────────

    fun start(): Boolean {
        val ok = nativeCreate()
        _isRunning.value = ok
        if (ok) {
            _sampleRate.value = nativeGetSampleRate()
            _bufferSize.value = nativeGetBufferSize()
            Log.i(TAG, "Oboe engine started — SR=${_sampleRate.value} Buffer=${_bufferSize.value}")
        } else {
            Log.e(TAG, "Failed to start Oboe engine")
        }
        return ok
    }

    fun stop() {
        nativeDestroy()
        _isRunning.value = false
        Log.i(TAG, "Oboe engine stopped")
    }

    fun refreshStats() {
        _cpuLoad.value      = nativeGetCpuLoad()
        _activeVoices.value = nativeGetActiveVoices()
        _bufferSize.value   = nativeGetBufferSize()
    }

    // ── MIDI ───────────────────────────────────────────────────────────────

    fun noteOn (channel: Int, note: Int, velocity: Int) = nativeNoteOn(channel, note, velocity)
    fun noteOff(channel: Int, note: Int)                = nativeNoteOff(channel, note)
    fun allNotesOff()                                   = nativeAllNotesOff()
    fun sendCC (channel: Int, cc: Int, value: Int)      = nativeSendCC(channel, cc, value)
    fun setPitchBend(channel: Int, semitones: Float)    = nativeSetPitchBend(channel, semitones)

    // ── Master controls ────────────────────────────────────────────────────

    fun setMasterVolume (v: Float)  = nativeSetMasterVolume(v)
    fun setReverbMix    (m: Float)  = nativeSetReverbMix(m)
    fun setDelayMix     (m: Float)  = nativeSetDelayMix(m)
    fun setDelayTime    (s: Float)  = nativeSetDelayTime(s)
    fun setDelayFeedback(f: Float)  = nativeSetDelayFeedback(f)

    // ── JNI declarations ───────────────────────────────────────────────────

    private external fun nativeCreate(): Boolean
    private external fun nativeDestroy()

    private external fun nativeNoteOn(channel: Int, note: Int, velocity: Int)
    private external fun nativeNoteOff(channel: Int, note: Int)
    private external fun nativeAllNotesOff()
    private external fun nativeSendCC(channel: Int, cc: Int, value: Int)
    private external fun nativeSetPitchBend(channel: Int, semitones: Float)

    private external fun nativeSetMasterVolume(volume: Float)
    private external fun nativeSetReverbMix(mix: Float)
    private external fun nativeSetDelayMix(mix: Float)
    private external fun nativeSetDelayTime(seconds: Float)
    private external fun nativeSetDelayFeedback(feedback: Float)

    private external fun nativeGetSampleRate(): Int
    private external fun nativeGetBufferSize(): Int
    private external fun nativeGetCpuLoad(): Float
    private external fun nativeGetActiveVoices(): Int

    companion object {
        private const val TAG = "AudioBridge"

        // Singleton
        @Volatile private var instance: AudioBridge? = null
        fun getInstance(): AudioBridge = instance ?: synchronized(this) {
            instance ?: AudioBridge().also { instance = it }
        }

        init {
            System.loadLibrary("jvk_audio_engine")
        }
    }
}
