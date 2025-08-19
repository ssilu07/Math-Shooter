package com.royals.mathshootergame

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class HighScoreActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("MathShooterPrefs", MODE_PRIVATE)

        createHighScoreLayout()
    }

    private fun createHighScoreLayout() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(ContextCompat.getColor(this@HighScoreActivity, android.R.color.black))
            setPadding(32, 32, 32, 32)
        }

        // Title
        val titleText = TextView(this).apply {
            text = "HIGH SCORES"
            textSize = 28f
            setTextColor(ContextCompat.getColor(this@HighScoreActivity, android.R.color.white))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32
            }
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }

        // Scroll view for scores
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
        }

        // Load and display high scores
        displayHighScores(scoresLayout)

        scrollView.addView(scoresLayout)

        // Buttons
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
        }

        val clearButton = Button(this).apply {
            text = "CLEAR SCORES"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, 100, 1f).apply {
                rightMargin = 8
            }
            setBackgroundColor(ContextCompat.getColor(this@HighScoreActivity, android.R.color.holo_red_dark))
            setTextColor(ContextCompat.getColor(this@HighScoreActivity, android.R.color.white))
            setOnClickListener {
                clearHighScores()
                recreate()
            }
        }

        val backButton = Button(this).apply {
            text = "BACK"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, 100, 1f).apply {
                leftMargin = 8
            }
            setBackgroundColor(ContextCompat.getColor(this@HighScoreActivity, android.R.color.holo_blue_dark))
            setTextColor(ContextCompat.getColor(this@HighScoreActivity, android.R.color.white))
            setOnClickListener {
                finish()
            }
        }

        buttonLayout.addView(clearButton)
        buttonLayout.addView(backButton)

        // Add views to main layout
        mainLayout.addView(titleText)
        mainLayout.addView(scrollView)
        mainLayout.addView(buttonLayout)

        setContentView(mainLayout)
    }

    private fun displayHighScores(parent: LinearLayout) {
        // Get stored high scores
        val highScores = mutableListOf<ScoreEntry>()

        // Load top 10 scores
        for (i in 1..10) {
            val score = sharedPreferences.getInt("score_$i", 0)
            val wave = sharedPreferences.getInt("wave_$i", 0)
            val date = sharedPreferences.getString("date_$i", "")
            val accuracy = sharedPreferences.getFloat("accuracy_$i", 0f)

            if (score > 0) {
                highScores.add(ScoreEntry(score, wave, date ?: "", accuracy))
            }
        }

        if (highScores.isEmpty()) {
            val noScoresText = TextView(this).apply {
                text = "No high scores yet!\nStart playing to set your first record."
                textSize = 18f
                setTextColor(ContextCompat.getColor(this@HighScoreActivity, android.R.color.darker_gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 64
                }
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            parent.addView(noScoresText)
            return
        }

        // Sort scores in descending order
        highScores.sortByDescending { it.score }

        // Create header
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            setBackgroundColor(ContextCompat.getColor(this@HighScoreActivity, android.R.color.holo_blue_dark))
            setPadding(16, 8, 16, 8)
        }

        val headers = arrayOf("Rank", "Score", "Wave", "Accuracy", "Date")
        val weights = arrayOf(1f, 2f, 1f, 1.5f, 2f)

        headers.forEachIndexed { index, header ->
            val headerText = TextView(this).apply {
                text = header
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@HighScoreActivity, android.R.color.white))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weights[index])
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            headerLayout.addView(headerText)
        }

        parent.addView(headerLayout)

        // Display scores
        highScores.forEachIndexed { index, scoreEntry ->
            val scoreLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 8
                }
                setBackgroundColor(
                    if (index % 2 == 0)
                        ContextCompat.getColor(this@HighScoreActivity, android.R.color.darker_gray)
                    else
                        ContextCompat.getColor(this@HighScoreActivity, android.R.color.black)
                )
                setPadding(16, 12, 16, 12)
            }

            // Rank
            val rankText = TextView(this).apply {
                text = "${index + 1}"
                textSize = 16f
                setTextColor(
                    when (index) {
                        0 -> ContextCompat.getColor(this@HighScoreActivity, android.R.color.holo_orange_light) // Gold
                        1 -> ContextCompat.getColor(this@HighScoreActivity, android.R.color.darker_gray) // Silver
                        2 -> ContextCompat.getColor(this@HighScoreActivity, android.R.color.holo_orange_dark) // Bronze
                        else -> ContextCompat.getColor(this@HighScoreActivity, android.R.color.white)
                    }
                )
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }

            // Score
            val scoreText = TextView(this).apply {
                text = "${scoreEntry.score}"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@HighScoreActivity, android.R.color.holo_green_light))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }

            // Wave
            val waveText = TextView(this).apply {
                text = "${scoreEntry.wave}"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@HighScoreActivity, android.R.color.holo_blue_light))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }

            // Accuracy
            val accuracyText = TextView(this).apply {
                text = "${(scoreEntry.accuracy * 100).toInt()}%"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@HighScoreActivity, android.R.color.holo_purple))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }

            // Date
            val dateText = TextView(this).apply {
                text = scoreEntry.date.take(10) // Show only date part
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@HighScoreActivity, android.R.color.white))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }

            scoreLayout.addView(rankText)
            scoreLayout.addView(scoreText)
            scoreLayout.addView(waveText)
            scoreLayout.addView(accuracyText)
            scoreLayout.addView(dateText)

            parent.addView(scoreLayout)
        }
    }

    private fun clearHighScores() {
        val editor = sharedPreferences.edit()
        for (i in 1..10) {
            editor.remove("score_$i")
            editor.remove("wave_$i")
            editor.remove("date_$i")
            editor.remove("accuracy_$i")
        }
        editor.remove("high_score")
        editor.apply()
    }

    data class ScoreEntry(
        val score: Int,
        val wave: Int,
        val date: String,
        val accuracy: Float
    )
}