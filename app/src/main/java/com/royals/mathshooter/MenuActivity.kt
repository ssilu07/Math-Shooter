package com.royals.mathshooter

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
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

        // NEW: Learn Mathematics button
        val learnButton = createStyledButton("📚 Learn Mathematics", R.color.game_info) {
            startLearnSection()
        }

        val mathLearnerButton = createStyledButton(getString(R.string.math_learner), R.color.game_info) {
            startMathLearner()
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
        mainLayout.addView(learnButton)  // ADD THE NEW LEARN BUTTON
        mainLayout.addView(mathLearnerButton)
        mainLayout.addView(dailyChallengeButton)
        mainLayout.addView(leaderboardButton)
        mainLayout.addView(settingsButton)

        setContentView(mainLayout)
    }

    // NEW: Method to start the Learn section
    private fun startLearnSection() {
        val intent = Intent(this, LearnActivity::class.java)
        startActivity(intent)
    }

    // Keep existing method for backward compatibility
    private fun startMathLearner() {
        val intent = Intent(this, MathLearnerActivity::class.java)
        startActivity(intent)
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
        val operations = arrayOf(
            "Addition (+)",
            "Subtraction (−)",
            "Multiplication (×)",
            "Division (÷)",
            "Mixed Operations"
        )

        AlertDialog.Builder(this)
            .setTitle("Choose Practice Type")
            .setItems(operations) { _, which ->
                sharedPreferences.edit().putInt("practice_type", which).apply()
                showDifficultyMenu(which)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDifficultyMenu(operationType: Int) {
        val operationName = when (operationType) {
            0 -> "Addition"
            1 -> "Subtraction"
            2 -> "Multiplication"
            3 -> "Division"
            4 -> "Mixed Operations"
            else -> "Math"
        }

        val difficulties = when (operationType) {
            0 -> arrayOf( // Addition
                "Easy - 2-digit addition (10-99)",
                "Medium - 3-digit addition (100-999)",
                "Hard - 4-digit addition (1000-9999)",
                "Expert - 5-digit addition (10000-99999)"
            )
            1 -> arrayOf( // Subtraction
                "Easy - 2-digit subtraction",
                "Medium - 3-digit subtraction",
                "Hard - 4-digit subtraction",
                "Expert - 5-digit subtraction"
            )
            2 -> arrayOf( // Multiplication
                "Easy - 1-digit × 2-digit",
                "Medium - 2-digit × 2-digit",
                "Hard - 2-digit × 3-digit",
                "Expert - 3-digit × 3-digit"
            )
            3 -> arrayOf( // Division
                "Easy - 2-digit ÷ 1-digit",
                "Medium - 3-digit ÷ 1-digit",
                "Hard - 3-digit ÷ 2-digit",
                "Expert - 4-digit ÷ 2-digit"
            )
            4 -> arrayOf( // Mixed
                "Easy - Basic mixed operations",
                "Medium - Intermediate mixed operations",
                "Hard - Advanced mixed operations",
                "Expert - Complex mixed operations"
            )
            else -> arrayOf("Easy", "Medium", "Hard", "Expert")
        }

        AlertDialog.Builder(this)
            .setTitle("$operationName Practice - Choose Difficulty")
            .setItems(difficulties) { _, which ->
                sharedPreferences.edit().putInt("practice_difficulty", which + 1).apply()

                // Show confirmation message
                val selectedDifficulty = difficulties[which]
                showPracticeConfirmation(operationName, selectedDifficulty)
            }
            .setNegativeButton("Back") { _, _ ->
                showPracticeMenu() // Go back to operation selection
            }
            .show()
    }

    private fun showPracticeConfirmation(operationName: String, difficulty: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Start Practice Session")
            .setMessage("Ready to practice $operationName?\n\n$difficulty")
            .setPositiveButton("START PRACTICE") { _, _ ->
                startGame(GameMode.PRACTICE)
            }
            .setNegativeButton("BACK") { _, _ ->
                showPracticeMenu()
            }
            .setCancelable(false)
            .create()

        // Style the dialog to make title and message visible
        dialog.setOnShowListener {
            // Style title
            val titleId = resources.getIdentifier("alertTitle", "id", "android")
            if (titleId > 0) {
                dialog.findViewById<TextView>(titleId)?.apply {
                    setTextColor(Color.BLACK)
                    textSize = 20f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = android.view.Gravity.CENTER
                }
            }

            // Style message
            val messageId = android.R.id.message
            dialog.findViewById<TextView>(messageId)?.apply {
                setTextColor(Color.DKGRAY)
                textSize = 16f
                gravity = android.view.Gravity.CENTER
                setPadding(20, 20, 20, 40)
            }

            // Style buttons
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                setTextColor(Color.parseColor("#9C27B0")) // Purple for START PRACTICE
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                setTextColor(Color.parseColor("#FF5722")) // Red for BACK
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
            }
        }

        dialog.show()
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