package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Synthesizes crisp, non-intrusive audio feedback offline via AudioTrack PCM buffers
 * so correct/incorrect/completion tones work everywhere without internet or media files.
 */
class QuizSoundManager {
  private val sampleRate = 44100
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  private val correctPcm: ShortArray by lazy { buildCorrectChime() }
  private val incorrectPcm: ShortArray by lazy { buildIncorrectTone() }
  private val completePcm: ShortArray by lazy { buildCompletionFanfare() }

  private val activeTrackLock = Any()
  private var activePlaybackJob: Job? = null
  @Volatile private var activeTrack: AudioTrack? = null

  @Volatile var isEnabled: Boolean = true

  fun playCorrect() {
    if (!isEnabled) return
    playBufferAsync(correctPcm)
  }

  fun playIncorrect() {
    if (!isEnabled) return
    playBufferAsync(incorrectPcm)
  }

  fun playComplete() {
    if (!isEnabled) return
    playBufferAsync(completePcm)
  }

  private fun playBufferAsync(samples: ShortArray) {
    activePlaybackJob?.cancel()
    synchronized(activeTrackLock) {
      try {
        activeTrack?.pause()
        activeTrack?.flush()
        activeTrack?.release()
      } catch (_: Throwable) {}
      activeTrack = null
    }

    activePlaybackJob = scope.launch {
      var track: AudioTrack? = null
      try {
        val minBuf = AudioTrack.getMinBufferSize(
          sampleRate,
          AudioFormat.CHANNEL_OUT_MONO,
          AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(samples.size * 2)
        val targetSamples = maxOf(samples.size, (minBuf + 1) / 2)
        val alignedSamples =
          if (targetSamples > samples.size) samples.copyOf(targetSamples) else samples
        val bufferSizeBytes = alignedSamples.size * 2

        track =
          AudioTrack.Builder()
            .setAudioAttributes(
              AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            )
            .setAudioFormat(
              AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            )
            .setBufferSizeInBytes(bufferSizeBytes)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        synchronized(activeTrackLock) {
          activeTrack = track
        }
        track.write(alignedSamples, 0, alignedSamples.size)
        track.play()
        val durationMs = (samples.size * 1000L) / sampleRate + 60L
        delay(durationMs)
      } catch (_: Throwable) {
        // Safely ignore on headless/test devices without audio output
      } finally {
        synchronized(activeTrackLock) {
          if (activeTrack === track) activeTrack = null
        }
        try {
          track?.stop()
        } catch (_: Throwable) {}
        try {
          track?.release()
        } catch (_: Throwable) {}
      }
    }
  }

  private fun buildCorrectChime(): ShortArray {
    // Crisp ascending two-note harmonic chime: E5 (659.25 Hz) -> B5 (987.77 Hz)
    val note1 = synthNote(freq = 659.25, durationSec = 0.11, decay = 14.0, gain = 0.35)
    val note2 = synthNote(freq = 987.77, durationSec = 0.22, decay = 9.0, gain = 0.38)
    val gap = ShortArray((sampleRate * 0.015).toInt())
    return note1 + gap + note2
  }

  private fun buildIncorrectTone(): ShortArray {
    // Gentle descending low two-tone buzz: 240 Hz -> 175 Hz
    val note1 = synthNote(freq = 240.0, durationSec = 0.12, decay = 10.0, gain = 0.32, warmHarmonic = true)
    val note2 = synthNote(freq = 175.0, durationSec = 0.20, decay = 8.0, gain = 0.34, warmHarmonic = true)
    val gap = ShortArray((sampleRate * 0.02).toInt())
    return note1 + gap + note2
  }

  private fun buildCompletionFanfare(): ShortArray {
    // Bright C5 - E5 - G5 - C6 celebration chord arpeggio
    val notes = listOf(523.25, 659.25, 783.99, 1046.50)
    val out = ArrayList<Short>()
    notes.forEachIndexed { idx, freq ->
      val dur = if (idx == notes.lastIndex) 0.30 else 0.09
      val dec = if (idx == notes.lastIndex) 7.5 else 15.0
      val pcm = synthNote(freq = freq, durationSec = dur, decay = dec, gain = 0.32)
      for (s in pcm) out.add(s)
    }
    return out.toShortArray()
  }

  private fun synthNote(
    freq: Double,
    durationSec: Double,
    decay: Double,
    gain: Double,
    warmHarmonic: Boolean = false,
  ): ShortArray {
    val count = (sampleRate * durationSec).toInt()
    val arr = ShortArray(count)
    val attackSamples = (sampleRate * 0.008).toInt().coerceAtLeast(1)
    for (i in 0 until count) {
      val t = i.toDouble() / sampleRate
      val attack = if (i < attackSamples) i.toDouble() / attackSamples else 1.0
      val env = attack * exp(-decay * t)
      val fundamental = sin(2.0 * PI * freq * t)
      val overtone =
        if (warmHarmonic) 0.25 * sin(2.0 * PI * (freq * 1.5) * t)
        else 0.18 * sin(2.0 * PI * (freq * 2.0) * t)
      val sample = (fundamental + overtone) * env * gain
      arr[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
    }
    return arr
  }

  fun release() {
    activePlaybackJob?.cancel()
    synchronized(activeTrackLock) {
      try {
        activeTrack?.stop()
        activeTrack?.release()
      } catch (_: Throwable) {}
      activeTrack = null
    }
    scope.cancel()
  }
}
