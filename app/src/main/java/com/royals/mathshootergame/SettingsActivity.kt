package com.royals.mathshootergame

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide action bar
        supportActionBar?.hide()

        sharedPreferences = getSharedPreferences("MathShooterPrefs", MODE_PRIVATE)

        createSettingsLayout()
    }

    private fun createSettingsLayout() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#1a1a2e"))
            setPadding(16, 16, 16, 16)
        }

        // Header
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 16, 0, 32)
        }

        // Back Button
        val backButton = Button(this).apply {
            text = "← Back"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#4CAF50"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { finish() }
        }

        // Title
        val titleText = TextView(this).apply {
            text = "⚙️ SETTINGS"
            textSize = 28f
            setTextColor(Color.CYAN)
            gravity = android.view.Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        headerLayout.addView(backButton)
        headerLayout.addView(titleText)

        // Settings ScrollView
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val settingsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 0, 16, 16)
        }

        // Create settings sections
        createAudioSettings(settingsLayout)
        createGameplaySettings(settingsLayout)
        createVisualSettings(settingsLayout)
        createDebugSettings(settingsLayout)
        createResetSection(settingsLayout)

        scrollView.addView(settingsLayout)

        mainLayout.addView(headerLayout)
        mainLayout.addView(scrollView)

        setContentView(mainLayout)
    }

    private fun createSectionHeader(layout: LinearLayout, title: String) {
        val headerText = TextView(this).apply {
            text = title
            textSize = 22f
            setTextColor(Color.YELLOW)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 24, 0, 12)
            }
        }
        layout.addView(headerText)
    }

    private fun createSettingRow(layout: LinearLayout, title: String, description: String,
                                 prefKey: String, defaultValue: Boolean) {
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            setBackgroundColor(Color.parseColor("#2a2a3e"))
            setPadding(16, 16, 16, 16)
        }

        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val titleText = TextView(this).apply {
            text = title
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        val descText = TextView(this).apply {
            text = description
            textSize = 14f
            setTextColor(Color.LTGRAY)
        }

        textLayout.addView(titleText)
        textLayout.addView(descText)

        val switch = Switch(this).apply {
            isChecked = sharedPreferences.getBoolean(prefKey, defaultValue)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnCheckedChangeListener { _, isChecked ->
                sharedPreferences.edit().putBoolean(prefKey, isChecked).apply()

                // Show confirmation for sound setting
                if (prefKey == "sound_enabled") {
                    showSoundToggleConfirmation(isChecked)
                }
            }
        }

        rowLayout.addView(textLayout)
        rowLayout.addView(switch)
        layout.addView(rowLayout)
    }

    // FIXED: Sound toggle confirmation method
    private fun showSoundToggleConfirmation(enabled: Boolean) {
        val message = if (enabled) {
            "🔊 Sound effects have been ENABLED!\n\nYou will hear sounds when:\n• Shooting bullets\n• Hitting enemies\n• Missing shots\n• Collecting power-ups"
        } else {
            "🔇 Sound effects have been DISABLED!\n\nThe game will now run silently."
        }

        Toast.makeText(this, if (enabled) "🔊 Sound Enabled" else "🔇 Sound Disabled", Toast.LENGTH_SHORT).show()
    }

    private fun createAudioSettings(layout: LinearLayout) {
        createSectionHeader(layout, "🔊 Audio Settings")

        createSettingRow(
            layout,
            "Sound Effects",
            "Enable or disable game sound effects",
            "sound_enabled",
            true
        )

        createSettingRow(
            layout,
            "Vibration Feedback",
            "Enable haptic feedback for actions",
            "vibration_enabled",
            true
        )
    }

    private fun createGameplaySettings(layout: LinearLayout) {
        createSectionHeader(layout, "🎮 Gameplay Settings")

        createSettingRow(
            layout,
            "Adaptive Difficulty",
            "Automatically adjust difficulty based on performance",
            "adaptive_difficulty",
            true
        )

        createSettingRow(
            layout,
            "Auto-Save Progress",
            "Automatically save game progress",
            "auto_save",
            true
        )

        // ADD ENEMY SPEED SETTING
        createEnemySpeedSetting(layout)
    }

    // NEW METHOD: Create enemy speed slider setting
    private fun createEnemySpeedSetting(layout: LinearLayout) {
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 16)
            }
            setBackgroundColor(Color.parseColor("#2a2a3e"))
            setPadding(16, 16, 16, 16)
        }

        // Title and description
        val titleText = TextView(this).apply {
            text = "🏃 Enemy Speed"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        val currentSpeed = sharedPreferences.getFloat("enemy_speed_multiplier", 1.0f)
        val descText = TextView(this).apply {
            text = "Control how fast enemies move (Current: ${String.format("%.1f", currentSpeed)}x)"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            id = android.R.id.text1 // We'll use this to update the text
        }

        // Speed control layout
        val speedControlLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0)
            }
        }

        // Speed labels
        val slowLabel = TextView(this).apply {
            text = "Slow\n0.5x"
            textSize = 12f
            setTextColor(Color.GREEN)
            gravity = android.view.Gravity.CENTER
        }

        val fastLabel = TextView(this).apply {
            text = "Fast\n2.0x"
            textSize = 12f
            setTextColor(Color.RED)
            gravity = android.view.Gravity.CENTER
        }

        // Speed slider
        val speedSeekBar = SeekBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(16, 0, 16, 0)
            }

            // Set range: 0.5x to 2.0x speed (50 to 200, then divide by 100)
            max = 150 // 0.5x to 2.0x = 150 steps
            progress = ((currentSpeed - 0.5f) * 100).toInt() // Convert to seekbar value

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val newSpeed = (progress / 100.0f) + 0.5f // Convert back to speed multiplier

                        // Save the setting
                        sharedPreferences.edit()
                            .putFloat("enemy_speed_multiplier", newSpeed)
                            .apply()

                        // Update description text
                        descText.text = "Control how fast enemies move (Current: ${String.format("%.1f", newSpeed)}x)"
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val finalSpeed = ((seekBar?.progress ?: 50) / 100.0f) + 0.5f
                    Toast.makeText(this@SettingsActivity,
                        "Enemy speed set to ${String.format("%.1f", finalSpeed)}x",
                        Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Preset buttons
        val presetLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 12, 0, 0)
            }
        }

        val easyButton = createSpeedPresetButton("Easy (0.7x)", 0.7f, speedSeekBar, descText)
        val normalButton = createSpeedPresetButton("Normal (1.0x)", 1.0f, speedSeekBar, descText)
        val hardButton = createSpeedPresetButton("Hard (1.5x)", 1.5f, speedSeekBar, descText)

        presetLayout.addView(easyButton)
        presetLayout.addView(normalButton)
        presetLayout.addView(hardButton)

        speedControlLayout.addView(slowLabel)
        speedControlLayout.addView(speedSeekBar)
        speedControlLayout.addView(fastLabel)

        rowLayout.addView(titleText)
        rowLayout.addView(descText)
        rowLayout.addView(speedControlLayout)
        rowLayout.addView(presetLayout)

        layout.addView(rowLayout)
    }

    // Helper method to create preset speed buttons
    private fun createSpeedPresetButton(text: String, speed: Float,
                                        seekBar: SeekBar, descText: TextView): Button {
        return Button(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#4CAF50"))
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(4, 0, 4, 0)
            }

            setOnClickListener {
                // Update seekbar
                seekBar.progress = ((speed - 0.5f) * 100).toInt()

                // Save setting
                sharedPreferences.edit()
                    .putFloat("enemy_speed_multiplier", speed)
                    .apply()

                // Update description
                descText.text = "Control how fast enemies move (Current: ${String.format("%.1f", speed)}x)"

                // Show feedback
                Toast.makeText(this@SettingsActivity,
                    "Enemy speed set to ${String.format("%.1f", speed)}x",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createVisualSettings(layout: LinearLayout) {
        createSectionHeader(layout, "✨ Visual Settings")

        createSettingRow(
            layout,
            "Particle Effects",
            "Show explosion and particle effects",
            "show_particles",
            true
        )

        createSettingRow(
            layout,
            "Screen Shake",
            "Enable screen shake effects",
            "screen_shake",
            true
        )

        createSettingRow(
            layout,
            "Background Animation",
            "Animate background starfield",
            "background_animation",
            true
        )
    }

    private fun createDebugSettings(layout: LinearLayout) {
        createSectionHeader(layout, "🔧 Debug Settings")

        createSettingRow(
            layout,
            "Show FPS Counter",
            "Display frames per second counter",
            "show_fps",
            false
        )

        createSettingRow(
            layout,
            "Debug Mode",
            "Enable debug information overlay",
            "debug_mode",
            false
        )
    }

    private fun createResetSection(layout: LinearLayout) {
        createSectionHeader(layout, "🔄 Reset Options")

        // Reset Settings Button
        val resetSettingsButton = Button(this).apply {
            text = "Reset All Settings"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#FF9800"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            setPadding(16, 16, 16, 16)
            setOnClickListener { resetAllSettings() }
        }

        // Reset Game Data Button
        val resetDataButton = Button(this).apply {
            text = "Reset All Game Data"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#F44336"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            setPadding(16, 16, 16, 16)
            setOnClickListener { resetAllData() }
        }

        layout.addView(resetSettingsButton)
        layout.addView(resetDataButton)
    }

    private fun resetAllSettings() {
        AlertDialog.Builder(this)
            .setTitle("Reset Settings")
            .setMessage("Are you sure you want to reset all settings to default values?")
            .setPositiveButton("Yes, Reset") { _, _ ->
                val editor = sharedPreferences.edit()
                editor.putBoolean("sound_enabled", true)
                editor.putBoolean("vibration_enabled", true)
                editor.putBoolean("show_particles", true)
                editor.putBoolean("adaptive_difficulty", true)
                editor.putBoolean("screen_shake", true)
                editor.putBoolean("background_animation", true)
                editor.putBoolean("auto_save", true)
                editor.putBoolean("show_fps", false)
                editor.putBoolean("debug_mode", false)
                // ADD ENEMY SPEED RESET
                editor.putFloat("enemy_speed_multiplier", 1.0f)
                editor.apply()

                Toast.makeText(this, "Settings reset to default!", Toast.LENGTH_SHORT).show()
                recreate() // Refresh the activity
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetAllData() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Reset All Data")
            .setMessage("This will permanently delete:\n\n• All high scores\n• Game progress\n• Settings\n• Statistics\n\nThis action cannot be undone!")
            .setPositiveButton("Yes, Delete Everything") { _, _ ->
                sharedPreferences.edit().clear().apply()
                Toast.makeText(this, "All data has been reset!", Toast.LENGTH_LONG).show()
                recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}