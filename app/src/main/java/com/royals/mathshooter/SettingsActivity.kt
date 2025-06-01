package com.royals.mathshooter

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
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
            }
        }

        rowLayout.addView(textLayout)
        rowLayout.addView(switch)
        layout.addView(rowLayout)
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
        editor.apply()

        Toast.makeText(this, "Settings reset to default!", Toast.LENGTH_SHORT).show()
        recreate() // Refresh the activity
    }

    private fun resetAllData() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reset All Data")
            .setMessage("This will delete all your high scores, progress, and settings. Are you sure?")
            .setPositiveButton("Yes, Reset Everything") { _, _ ->
                sharedPreferences.edit().clear().apply()
                Toast.makeText(this, "All data has been reset!", Toast.LENGTH_SHORT).show()
                recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}