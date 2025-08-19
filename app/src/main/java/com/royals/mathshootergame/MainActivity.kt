package com.royals.mathshootergame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.SurfaceHolder
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

// Data classes for game entities
data class Enemy(
    var x: Float,
    var y: Float,
    var equation: String,
    var answer: Int,
    var speed: Float = 2f,
    var isAlive: Boolean = true,
    var isBoss: Boolean = false,
    var id: Int = 0
)

data class Bullet(
    var x: Float,
    var y: Float,
    val value: Int,
    var speed: Float = 15f,
    var isActive: Boolean = true
)

data class PowerUp(
    var x: Float,
    var y: Float,
    val type: PowerUpType,
    var speed: Float = 3f,
    var isActive: Boolean = true
)

enum class PowerUpType {
    TIME_FREEZE, AUTO_SOLVE, SHIELD, DOUBLE_POINTS, EXTRA_LIFE
}

enum class GameState {
    MENU, PLAYING, PAUSED, GAME_OVER, WAVE_COMPLETE, SETTINGS
}

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var controlsLayout: LinearLayout
    private lateinit var scoreText: TextView
    private lateinit var livesText: TextView
    private lateinit var waveText: TextView
    private lateinit var comboText: TextView
    private lateinit var difficultyText: TextView
    private lateinit var pauseButton: Button
    private lateinit var solutionBoxesLayout: LinearLayout

    // Add sound manager
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide action bar for immersive gaming
        supportActionBar?.hide()

        try {
            // Get game mode from intent
            val gameModeString = intent.getStringExtra("game_mode") ?: "normal"
            val gameMode = when (gameModeString) {
                "practice" -> GameMode.PRACTICE
                "daily_challenge" -> GameMode.DAILY_CHALLENGE
                "boss_rush" -> GameMode.BOSS_RUSH
                else -> GameMode.NORMAL
            }

            // Create main layout
            val mainLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(Color.BLACK)
            }

            // Create HUD
            createHUD(mainLayout)

            // Create game view
            gameView = GameView(this)
            gameView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            gameView.setGameMode(gameMode)
            mainLayout.addView(gameView)

            // Create new solution box controls (removed movement controls)
            createSolutionBoxControls(mainLayout)

            setContentView(mainLayout)

            // Connect UI elements to game view
            gameView.setUIElements(scoreText, livesText, waveText, comboText, difficultyText)
            gameView.setSolutionBoxesLayout(solutionBoxesLayout)

        } catch (e: Exception) {
            println("❌ Error initializing MainActivity: ${e.message}")
            e.printStackTrace()

            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Failed to initialize game. Please restart the app.")
                .setPositiveButton("OK") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }

        // Initialize sound manager
        soundManager = SoundManager(this)
        gameView.setSoundManager(soundManager)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }

    private fun createHUD(parent: LinearLayout) {
        val hudLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 8)
            setBackgroundColor(Color.parseColor("#80000000"))
        }

        // First row - Score and Lives
        val firstRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        scoreText = TextView(this).apply {
            text = "Score: 0"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
        }

        livesText = TextView(this).apply {
            text = "❤️ 3"
            textSize = 18f
            setTextColor(Color.RED)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        pauseButton = Button(this).apply {
            text = "⏸️"
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                try {
                    gameView.pauseGame()
                } catch (e: Exception) {
                    println("❌ Error pausing game: ${e.message}")
                }
            }
        }

        firstRow.addView(scoreText)
        firstRow.addView(livesText)
        firstRow.addView(pauseButton)

        // Second row - Wave, Combo, and Difficulty
        val secondRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        waveText = TextView(this).apply {
            text = "Wave: 1"
            textSize = 16f
            setTextColor(Color.CYAN)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        comboText = TextView(this).apply {
            text = "Combo: 0x"
            textSize = 16f
            setTextColor(Color.YELLOW)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        difficultyText = TextView(this).apply {
            text = "Level: 1"
            textSize = 16f
            setTextColor(Color.GREEN)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        secondRow.addView(waveText)
        secondRow.addView(comboText)
        secondRow.addView(difficultyText)

        hudLayout.addView(firstRow)
        hudLayout.addView(secondRow)
        parent.addView(hudLayout)
    }

    private fun createSolutionBoxControls(parent: LinearLayout) {
        controlsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#1A1A1A"))
            setPadding(12, 12, 12, 12)
        }

        // COMMENTED OUT: FIRE BUTTON - No longer needed
        /*
        val fireControlLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = android.view.Gravity.CENTER
        }

        val fireButton = createStyledButton("🎯 FIRE TARGET", Color.parseColor("#FF5722")).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
            ).apply {
                setMargins(16, 8, 16, 8)
            }
            setOnClickListener {
                try {
                    gameView.fire()
                    hapticFeedback()
                } catch (e: Exception) {
                    println("❌ Error firing: ${e.message}")
                }
            }
        }

        fireControlLayout.addView(fireButton)
        controlsLayout.addView(fireControlLayout)
        */

        // Solution boxes layout
        createSolutionBoxes()

        parent.addView(controlsLayout)
    }

    private fun createSolutionBoxes() {
        // Enhanced title for solution section
        val solutionTitle = TextView(this).apply {
            text = "🎯 SELECT ANSWER TO AUTO-FIRE:"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 8)
            }
        }
        controlsLayout.addView(solutionTitle)

        // Container for solution boxes
        solutionBoxesLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = android.view.Gravity.CENTER
            setPadding(8, 8, 8, 8)
        }

        // Initially show waiting message
        updateSolutionBoxes(listOf())

        controlsLayout.addView(solutionBoxesLayout)
    }

    fun updateSolutionBoxes(answers: List<Int>) {
        try {
            solutionBoxesLayout.removeAllViews()

            if (answers.isEmpty()) {
                // Enhanced waiting state
                val waitingLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        120
                    )
                    gravity = android.view.Gravity.CENTER
                }

                val waitingIcon = TextView(this).apply {
                    text = "🎯"
                    textSize = 32f
                    gravity = android.view.Gravity.CENTER
                }

                val waitingMessage = TextView(this).apply {
                    text = "Scanning for targets..."
                    textSize = 16f
                    setTextColor(Color.GRAY)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = android.view.Gravity.CENTER
                }

                waitingLayout.addView(waitingIcon)
                waitingLayout.addView(waitingMessage)
                solutionBoxesLayout.addView(waitingLayout)
                return
            }

            // Create enhanced solution boxes
            answers.forEachIndexed { index, answer ->
                val solutionBox = Button(this).apply {
                    text = answer.toString()
                    textSize = 28f  // Larger text
                    setTextColor(Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD

                    background = createEnhancedSolutionBoxBackground(false)

                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        140,  // Taller buttons
                        1f
                    ).apply {
                        setMargins(8, 8, 8, 8)
                    }

                    setPadding(0, 0, 0, 0)
                    stateListAnimator = null
                    gravity = android.view.Gravity.CENTER

                    setOnClickListener {
                        try {
                            hapticFeedback()
                            gameView.selectSolutionBox(index, answer)
                            updateSolutionBoxSelection(index)

                            // Show selection feedback
                            showSelectionFeedback(answer)
                        } catch (e: Exception) {
                            println("❌ Error selecting solution box: ${e.message}")
                        }
                    }
                }
                solutionBoxesLayout.addView(solutionBox)
            }
        } catch (e: Exception) {
            println("❌ Error updating solution boxes: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun createEnhancedSolutionBoxBackground(isSelected: Boolean): android.graphics.drawable.Drawable {
        val strokeWidth = 6  // Thicker border
        val cornerRadius = 20f  // More rounded

        val gradientDrawable = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setCornerRadius(cornerRadius)

            if (isSelected) {
                // Selected state - bright glowing effect
                setColor(Color.parseColor("#1B5E20"))  // Dark green background
                setStroke(strokeWidth, Color.parseColor("#4CAF50"))  // Bright green border
            } else {
                // Normal state - blue theme
                setColor(Color.parseColor("#0D47A1"))  // Dark blue background
                setStroke(strokeWidth, Color.parseColor("#2196F3"))  // Blue border
            }
        }

        val pressedDrawable = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setCornerRadius(cornerRadius)
            setColor(Color.parseColor("#263238"))  // Dark gray when pressed
            setStroke(strokeWidth, Color.parseColor("#FF9800"))  // Orange border when pressed
        }

        val stateListDrawable = android.graphics.drawable.StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
            addState(intArrayOf(), gradientDrawable)
        }

        return stateListDrawable
    }

    private fun showSelectionFeedback(answer: Int) {
        // Brief visual feedback when answer is selected
        val feedbackText = TextView(this).apply {
            text = "Selected: $answer ✓"
            textSize = 20f
            setTextColor(Color.GREEN)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            alpha = 0f
        }

        // Add to layout temporarily
        controlsLayout.addView(feedbackText)

        // Animate in and out
        feedbackText.animate()
            .alpha(1f)
            .setDuration(200)
            .withEndAction {
                Handler(Looper.getMainLooper()).postDelayed({
                    feedbackText.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            controlsLayout.removeView(feedbackText)
                        }
                }, 800)
            }
    }

    fun updateSolutionBoxSelection(selectedIndex: Int) {
        try {
            if (solutionBoxesLayout.childCount > 0 && solutionBoxesLayout.getChildAt(0) is Button) {
                for (i in 0 until solutionBoxesLayout.childCount) {
                    val child = solutionBoxesLayout.getChildAt(i)
                    if (child is Button) {
                        child.background = createEnhancedSolutionBoxBackground(i == selectedIndex)

                        // Add glow effect to selected button
                        if (i == selectedIndex) {
                            child.elevation = 12f
                            child.scaleX = 1.05f
                            child.scaleY = 1.05f
                        } else {
                            child.elevation = 4f
                            child.scaleX = 1f
                            child.scaleY = 1f
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error updating solution box selection: ${e.message}")
        }
    }

    // New method to highlight solution boxes when player tries to fire without selection
    fun highlightSolutionBoxes() {
        try {
            for (i in 0 until solutionBoxesLayout.childCount) {
                val child = solutionBoxesLayout.getChildAt(i)
                if (child is Button) {
                    // Flash red briefly
                    val originalBackground = child.background
                    child.setBackgroundColor(Color.RED)

                    Handler(Looper.getMainLooper()).postDelayed({
                        child.background = originalBackground
                    }, 200)
                }
            }

            // Show toast message
            Toast.makeText(this, "⚠️ Select an answer first!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            println("❌ Error highlighting solution boxes: ${e.message}")
        }
    }

    private fun createStyledButton(text: String, backgroundColor: Int): Button {
        return Button(this).apply {
            this.text = text
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(backgroundColor)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(8, 8, 8, 8)
            elevation = 4f
        }
    }

    private fun hapticFeedback() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (e: Exception) {
            println("❌ Haptic feedback not available: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            if (::gameView.isInitialized) {
                gameView.pause()
            }
        } catch (e: Exception) {
            println("❌ Error pausing game: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            if (::gameView.isInitialized) {
                gameView.resume()
            }
        } catch (e: Exception) {
            println("❌ Error resuming game: ${e.message}")
        }
    }

    override fun onBackPressed() {
        try {
            if (::gameView.isInitialized) {
                when (gameView.gameState) {
                    GameState.PLAYING -> {
                        gameView.pauseGame()
                        showPauseDialog()
                    }
                    GameState.PAUSED -> {
                        showPauseDialog()
                    }
                    GameState.GAME_OVER -> {
                        showExitDialog()
                    }
                    else -> {
                        showExitDialog()
                    }
                }
            } else {
                showExitDialog()
            }
        } catch (e: Exception) {
            println("❌ Error handling back press: ${e.message}")
            super.onBackPressed()
        }
    }

    private fun showExitDialog() {
        try {
            val dialog = AlertDialog.Builder(this)
                .setTitle("Exit Game?")
                .setMessage("Do you want to return to the main menu?\n\nYour progress will be lost.")
                .setPositiveButton("YES") { _, _ ->
                    finish()
                }
                .setNegativeButton("NO") { dialog, _ ->
                    dialog.dismiss()
                    try {
                        if (::gameView.isInitialized && gameView.gameState == GameState.PAUSED) {
                            gameView.pauseGame()
                        }
                    } catch (e: Exception) {
                        println("❌ Error resuming game: ${e.message}")
                    }
                }
                .setCancelable(false)
                .create()

            dialog.setOnShowListener {
                val titleId = resources.getIdentifier("alertTitle", "id", "android")
                if (titleId > 0) {
                    dialog.findViewById<TextView>(titleId)?.apply {
                        setTextColor(Color.BLACK)
                        textSize = 20f
                        typeface = Typeface.DEFAULT_BOLD
                        gravity = android.view.Gravity.CENTER
                    }
                }

                val messageId = android.R.id.message
                dialog.findViewById<TextView>(messageId)?.apply {
                    setTextColor(Color.DKGRAY)
                    textSize = 16f
                    gravity = android.view.Gravity.CENTER
                    setPadding(20, 20, 20, 40)
                }

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                    setTextColor(Color.parseColor("#FF5722"))
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                }
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                    setTextColor(Color.parseColor("#4CAF50"))
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                }
            }

            dialog.show()
        } catch (e: Exception) {
            println("❌ Error showing exit dialog: ${e.message}")
            finish()
        }
    }

    private fun showPauseDialog() {
        try {
            val dialog = AlertDialog.Builder(this)
                .setTitle("Game Paused")
                .setMessage("What would you like to do?")
                .setPositiveButton("RESUME") { dialog, _ ->
                    dialog.dismiss()
                    try {
                        if (::gameView.isInitialized) {
                            gameView.pauseGame()
                        }
                    } catch (e: Exception) {
                        println("❌ Error resuming game: ${e.message}")
                    }
                }
                .setNegativeButton("MAIN MENU") { _, _ ->
                    finish()
                }
                .setNeutralButton("RESTART") { _, _ ->
                    try {
                        if (::gameView.isInitialized) {
                            gameView.initializeGame()
                        }
                    } catch (e: Exception) {
                        println("❌ Error restarting game: ${e.message}")
                        finish()
                    }
                }
                .setCancelable(false)
                .create()

            dialog.setOnShowListener {
                val titleId = resources.getIdentifier("alertTitle", "id", "android")
                if (titleId > 0) {
                    dialog.findViewById<TextView>(titleId)?.apply {
                        setTextColor(Color.BLACK)
                        textSize = 20f
                        typeface = Typeface.DEFAULT_BOLD
                        gravity = android.view.Gravity.CENTER
                    }
                }

                val messageId = android.R.id.message
                dialog.findViewById<TextView>(messageId)?.apply {
                    setTextColor(Color.GRAY)
                    textSize = 16f
                    gravity = android.view.Gravity.CENTER
                    setPadding(20, 20, 20, 40)
                }

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                    setTextColor(Color.parseColor("#4CAF50"))
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                }
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                    setTextColor(Color.parseColor("#FF5722"))
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                }
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.apply {
                    setTextColor(Color.parseColor("#2196F3"))
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                }
            }

            dialog.show()
        } catch (e: Exception) {
            println("❌ Error showing pause dialog: ${e.message}")
        }
    }
}

class GameThread(private val surfaceHolder: SurfaceHolder, private val gameView: GameView) : Thread() {
    private var running = false
    private val targetFPS = 60
    private val targetTime = 1000 / targetFPS

    fun setRunning(running: Boolean) {
        this.running = running
    }

    override fun run() {
        var startTime: Long
        var timeMillis: Long
        var waitTime: Long

        while (running) {
            startTime = System.nanoTime()

            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                synchronized(surfaceHolder) {
                    gameView.update()
                    gameView.draw(canvas)
                }
            } catch (e: Exception) {
                println("❌ Error in game thread: ${e.message}")
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        println("❌ Error unlocking canvas: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }

            timeMillis = (System.nanoTime() - startTime) / 1000000
            waitTime = targetTime - timeMillis

            if (waitTime > 0) {
                try {
                    sleep(waitTime)
                } catch (e: Exception) {
                    println("❌ Error in game thread sleep: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
}