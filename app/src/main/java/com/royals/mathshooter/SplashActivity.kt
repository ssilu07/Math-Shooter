package com.royals.mathshooter

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide action bar
        supportActionBar?.hide()

        createSplashLayout()

        // Navigate to MenuActivity after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }, 3000)
    }

    private fun createSplashLayout() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(ContextCompat.getColor(this@SplashActivity, R.color.game_black))
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        // App Icon
        val iconText = TextView(this).apply {
            text = "🚀"
            textSize = 80f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 30)
            }
        }

        // App Title
        val titleText = TextView(this).apply {
            text = getString(R.string.game_title)
            textSize = 42f
            setTextColor(ContextCompat.getColor(this@SplashActivity, R.color.game_title))
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 20)
            }
        }

        // Subtitle
        val subtitleText = TextView(this).apply {
            text = getString(R.string.game_subtitle)
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@SplashActivity, R.color.game_subtitle))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 40)
            }
        }

        // Loading text
        val loadingText = TextView(this).apply {
            text = "Loading..."
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@SplashActivity, R.color.game_white))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 60, 0, 0)
            }
        }

        // Developer info
        val developerText = TextView(this).apply {
            text = "Developed by Royal Games"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@SplashActivity, R.color.game_gray))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 20, 0, 0)
            }
        }

        mainLayout.addView(iconText)
        mainLayout.addView(titleText)
        mainLayout.addView(subtitleText)
        mainLayout.addView(loadingText)
        mainLayout.addView(developerText)

        setContentView(mainLayout)
    }
}