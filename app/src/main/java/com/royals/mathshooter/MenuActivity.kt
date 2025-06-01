package com.royals.mathshooter

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MenuActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("MathShooterPrefs", MODE_PRIVATE)

        createMenuLayout()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createMenuLayout() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(ContextCompat.getColor(this@MenuActivity, R.color.game_black))
            setPadding(32, 32, 32, 32)
        }

        // Title
        val titleText = TextView(this).apply {
            text = getString(R.string.game_title)
            setTextAppearance(R.style.GameTitle)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 50, 0, 30)
            }
        }

        // Subtitle
        val subtitleText = TextView(this).apply {
            text = getString(R.string.game_subtitle)
            setTextAppearance(R.style.GameSubtitle)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 50)
            }
        }

        // High Score Display
        val highScore = sharedPreferences.getInt("high_score", 0)
        val highScoreText = TextView(this).apply {
            text = getString(R.string.high_score_display, highScore)
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@MenuActivity, R.color.game_score))
            gravity = android.view.Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 40)
            }
        }

        // Menu Buttons
        val playButton = createStyledButton(getString(R.string.start_mission), R.color.game_primary) {
            startGame(GameMode.NORMAL)
        }

        val practiceButton = createStyledButton(getString(R.string.practice_mode), R.color.game_secondary) {
            showPracticeMenu()
        }

        val dailyChallengeButton = createStyledButton(getString(R.string.daily_challenge), R.color.game_accent) {
            startGame(GameMode.DAILY_CHALLENGE)
        }

        val leaderboardButton = createStyledButton(getString(R.string.high_scores), R.color.game_info) {
            showLeaderboard()
        }

        val settingsButton = createStyledButton(getString(R.string.settings), R.color.game_warning) {
            showSettings()
        }

        // Add all views to layout
        mainLayout.addView(titleText)
        mainLayout.addView(subtitleText)
        mainLayout.addView(highScoreText)
        mainLayout.addView(playButton)
        mainLayout.addView(practiceButton)
        mainLayout.addView(dailyChallengeButton)
        mainLayout.addView(leaderboardButton)
        mainLayout.addView(settingsButton)

        setContentView(mainLayout)
    }

    private fun createStyledButton(text: String, colorRes: Int, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@MenuActivity, R.color.game_white))
            setBackgroundColor(ContextCompat.getColor(this@MenuActivity, colorRes))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
            ).apply {
                setMargins(20, 8, 20, 8)
            }
            elevation = 8f
            setOnClickListener { onClick() }
        }
    }

    private fun startGame(mode: GameMode) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("game_mode", when(mode) {
            GameMode.NORMAL -> "normal"
            GameMode.PRACTICE -> "practice"
            GameMode.DAILY_CHALLENGE -> "daily_challenge"
            GameMode.BOSS_RUSH -> "boss_rush"
        })
        startActivity(intent)
    }

    private fun showPracticeMenu() {
        val options = resources.getStringArray(R.array.operation_options)

        AlertDialog.Builder(this)
            .setTitle("Choose Practice Type")
            .setItems(options) { _, which ->
                sharedPreferences.edit().putInt("practice_type", which).apply()
                showDifficultyMenu()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showDifficultyMenu() {
        val difficulties = resources.getStringArray(R.array.difficulty_options)

        AlertDialog.Builder(this)
            .setTitle("Choose Difficulty")
            .setItems(difficulties) { _, which ->
                sharedPreferences.edit().putInt("practice_difficulty", which + 1).apply()
                startGame(GameMode.PRACTICE)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showLeaderboard() {
        startActivity(Intent(this, LeaderboardActivity::class.java))
    }

    private fun showSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        // Update high score display when returning from game
        val highScore = sharedPreferences.getInt("high_score", 0)
        // You would need to update the TextView here if you keep a reference to it
    }
}