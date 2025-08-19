// Create new file: SoundManager.kt
package com.royals.mathshootergame

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.content.SharedPreferences

class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MathShooterPrefs", Context.MODE_PRIVATE)

    // Sound IDs
    private var shootSoundId = 0
    private var hitSoundId = 0
    private var missSoundId = 0
    private var powerUpSoundId = 0

    init {
        initializeSoundPool()
        loadSounds()
    }

    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Maximum simultaneous sounds
            .setAudioAttributes(audioAttributes)
            .build()
    }

    private fun loadSounds() {
        try {
            soundPool?.let { pool ->
                // Load shooting sound
                shootSoundId = pool.load(context, R.raw.bullet_shoot, 1)
                soundMap["shoot"] = shootSoundId

                // Load hit sound
                hitSoundId = pool.load(context, R.raw.bullet_hit, 1)
                soundMap["hit"] = hitSoundId

                // Load miss sound
                missSoundId = pool.load(context, R.raw.bullet_miss, 1)
                soundMap["miss"] = missSoundId

                // Load power-up sound
                powerUpSoundId = pool.load(context, R.raw.powerup_collect, 1)
                soundMap["powerup"] = powerUpSoundId

                println("🔊 All sounds loaded successfully")
            }
        } catch (e: Exception) {
            println("❌ Error loading sounds: ${e.message}")
        }
    }

    fun playShootSound() {
        playSound("shoot", 0.8f) // 80% volume
    }

    fun playHitSound() {
        playSound("hit", 1.0f) // 100% volume
    }

    fun playMissSound() {
        playSound("miss", 0.6f) // 60% volume
    }

    fun playPowerUpSound() {
        playSound("powerup", 1.0f) // 100% volume
    }

    private fun playSound(soundName: String, volume: Float = 1.0f) {
        try {
            // Check if sound is enabled in settings
            val soundEnabled = sharedPreferences.getBoolean("sound_enabled", true)
            if (!soundEnabled) return

            val soundId = soundMap[soundName] ?: return
            soundPool?.play(
                soundId,
                volume,      // Left volume
                volume,      // Right volume
                1,           // Priority
                0,           // Loop (0 = no loop)
                1.0f         // Rate (1.0 = normal speed)
            )
        } catch (e: Exception) {
            println("❌ Error playing sound $soundName: ${e.message}")
        }
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            soundMap.clear()
        } catch (e: Exception) {
            println("❌ Error releasing sound pool: ${e.message}")
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun isSoundEnabled(): Boolean {
        return sharedPreferences.getBoolean("sound_enabled", true)
    }
}