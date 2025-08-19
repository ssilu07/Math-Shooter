package com.royals.mathshooter

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide action bar
        supportActionBar?.hide()

        sharedPreferences = getSharedPreferences("MathShooterPrefs", MODE_PRIVATE)

        createLeaderboardLayout()
    }

    private fun createLeaderboardLayout() {
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
            setPadding(0, 16, 0, 16)
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
            text = "🏆 HIGH SCORES"
            textSize = 28f
            setTextColor(Color.YELLOW)
            gravity = android.view.Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        // Clear Button
        val clearButton = Button(this).apply {
            text = "Clear"
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#F44336"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { showClearConfirmation() }
        }

        headerLayout.addView(backButton)
        headerLayout.addView(titleText)
        headerLayout.addView(clearButton)

        // Scores ScrollView
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val scoresLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 16)
        }

        // Load and display scores
        loadScores(scoresLayout)

        scrollView.addView(scoresLayout)

        mainLayout.addView(headerLayout)
        mainLayout.addView(scrollView)

        setContentView(mainLayout)
    }

    private fun loadScores(layout: LinearLayout) {
        var hasScores = false

        for (i in 1..10) {
            val score = sharedPreferences.getInt("score_$i", 0)
            val wave = sharedPreferences.getInt("wave_$i", 0)
            val date = sharedPreferences.getString("date_$i", "") ?: ""
            val accuracy = sharedPreferences.getFloat("accuracy_$i", 0f)

            if (score > 0) {
                hasScores = true

                // Score Card
                val scoreCard = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 8, 0, 8)
                    }
                    setBackgroundColor(Color.parseColor("#2a2a3e"))
                    setPadding(16, 12, 16, 12)
                }

                // Rank and Score
                val rankScoreText = TextView(this).apply {
                    text = "#$i - Score: ${formatScore(score)}"
                    textSize = 20f
                    setTextColor(when (i) {
                        1 -> Color.parseColor("#FFD700") // Gold
                        2 -> Color.parseColor("#C0C0C0") // Silver
                        3 -> Color.parseColor("#CD7F32") // Bronze
                        else -> Color.WHITE
                    })
                    typeface = Typeface.DEFAULT_BOLD
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // Details
                val detailsText = TextView(this).apply {
                    text = "Wave: $wave | Accuracy: ${(accuracy * 100).toInt()}%"
                    textSize = 16f
                    setTextColor(Color.LTGRAY)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // Date
                if (date.isNotEmpty()) {
                    val dateText = TextView(this).apply {
                        text = "📅 $date"
                        textSize = 14f
                        setTextColor(Color.GRAY)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 4, 0, 0)
                        }
                    }
                    scoreCard.addView(dateText)
                }

                scoreCard.addView(rankScoreText)
                scoreCard.addView(detailsText)

                layout.addView(scoreCard)
            }
        }

        if (!hasScores) {
            val noScoresText = TextView(this).apply {
                text = "🎮 No high scores yet!\n\nPlay some games to see your scores here."
                textSize = 18f
                setTextColor(Color.WHITE)
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 60, 0, 0)
                }
            }
            layout.addView(noScoresText)
        }
    }

    private fun formatScore(score: Int): String {
        return when {
            score >= 1000000 -> "${score / 1000000}M"
            score >= 1000 -> "${score / 1000}K"
            else -> score.toString()
        }
    }

    private fun showClearConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Clear Leaderboard")
            .setMessage("Are you sure you want to clear all high scores? This action cannot be undone.")
            .setPositiveButton("Yes, Clear") { _, _ ->
                clearLeaderboard()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearLeaderboard() {
        val editor = sharedPreferences.edit()
        for (i in 1..10) {
            editor.remove("score_$i")
            editor.remove("wave_$i")
            editor.remove("date_$i")
            editor.remove("accuracy_$i")
        }
        editor.apply()

        Toast.makeText(this, "Leaderboard cleared!", Toast.LENGTH_SHORT).show()

        // Refresh the display
        recreate()
    }
}