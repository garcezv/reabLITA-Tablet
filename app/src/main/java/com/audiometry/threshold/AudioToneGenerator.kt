package com.audiometry.threshold

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

class AudioToneGenerator {
    private var audioTrack: AudioTrack? = null
    private var masterVolume: Float = 0.7f
    
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val REF_DB = 60 // 60 dB ~ amplitude 1.0 (referência didática)
    }
    
    fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
    }
    
    private fun ampFromDb(db: Int): Float {
        val a = 10.0.pow((db - REF_DB) / 20.0).toFloat()
        return a.coerceIn(0f, 1.2f)
    }
    
    fun playTone(frequencyHz: Int, db: Int, durationMs: Int) {
        try {
            val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt()
            val samples = ShortArray(numSamples)
            
            val amplitude = ampFromDb(db) * masterVolume
            val fadeInSamples = (SAMPLE_RATE * 0.01).toInt() // 10ms fade in
            val fadeOutSamples = (SAMPLE_RATE * 0.03).toInt() // 30ms fade out
            
            for (i in samples.indices) {
                val angle = 2.0 * PI * i * frequencyHz / SAMPLE_RATE
                var value = sin(angle) * amplitude
                
                // Fade in
                if (i < fadeInSamples) {
                    value *= (i.toFloat() / fadeInSamples)
                }
                
                // Fade out
                if (i >= numSamples - fadeOutSamples) {
                    val fadePos = numSamples - i
                    value *= (fadePos.toFloat() / fadeOutSamples)
                }
                
                samples[i] = (value * Short.MAX_VALUE).toInt().toShort()
            }
            
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize.coerceAtLeast(numSamples * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            
            audioTrack?.write(samples, 0, samples.size)
            audioTrack?.play()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun release() {
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}
