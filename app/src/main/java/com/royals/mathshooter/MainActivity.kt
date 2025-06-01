package com.royals.mathshooter


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

// Data classes for game entities
data class Enemy(
    var x: Float,
    var y: Float,
    var equation: String,
    var answer: Int,
    var speed: Float = 2f,
    var isAlive: Boolean = true,
    var isBoss: Boolean = false,
    var id: Int = 0  // Add unique ID for each enemy
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

// Updated MainActivity.kt - Solution Box System

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide action bar for immersive gaming
        supportActionBar?.hide()

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

        // Create new solution box controls
        createSolutionBoxControls(mainLayout)

        setContentView(mainLayout)

        // Connect UI elements to game view
        gameView.setUIElements(scoreText, livesText, waveText, comboText, difficultyText)
        gameView.setSolutionBoxesLayout(solutionBoxesLayout)
    }

    private fun createHUD(parent: LinearLayout) {
        val hudLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 8)
            background = ContextCompat.getDrawable(this@MainActivity, android.R.drawable.screen_background_dark_transparent)
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
            setOnClickListener { gameView.pauseGame() }
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
            background = ContextCompat.getDrawable(this@MainActivity, android.R.drawable.screen_background_dark)
            setPadding(12, 12, 12, 12)
        }

        // Movement controls
        val movementLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val leftButton = createStyledButton("⬅️", Color.parseColor("#2196F3")).apply {
            layoutParams = LinearLayout.LayoutParams(0, 100, 1f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        gameView.setMovement(-1)
                        hapticFeedback()
                    }
                    MotionEvent.ACTION_UP -> gameView.setMovement(0)
                }
                true
            }
        }

        val fireButton = createStyledButton("🔥 FIRE", Color.parseColor("#FF5722")).apply {
            layoutParams = LinearLayout.LayoutParams(0, 100, 2f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnClickListener {
                gameView.fire()
                hapticFeedback()
            }
        }

        val rightButton = createStyledButton("➡️", Color.parseColor("#2196F3")).apply {
            layoutParams = LinearLayout.LayoutParams(0, 100, 1f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        gameView.setMovement(1)
                        hapticFeedback()
                    }
                    MotionEvent.ACTION_UP -> gameView.setMovement(0)
                }
                true
            }
        }

        movementLayout.addView(leftButton)
        movementLayout.addView(fireButton)
        movementLayout.addView(rightButton)
        controlsLayout.addView(movementLayout)

        // Solution boxes layout
        createSolutionBoxes()

        parent.addView(controlsLayout)
    }

    private fun createSolutionBoxes() {
        // Title for solution section
        val solutionTitle = TextView(this).apply {
            text = "Select Answer:"
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

        // Initially create placeholder boxes
        updateSolutionBoxes(listOf(0, 0, 0))

        controlsLayout.addView(solutionBoxesLayout)
    }

    fun updateSolutionBoxes(answers: List<Int>) {
        // Clear existing boxes
        solutionBoxesLayout.removeAllViews()

        // Create new solution boxes
        answers.forEachIndexed { index, answer ->
            val solutionBox = Button(this).apply {
                text = answer.toString()
                textSize = 24f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD

                // Create modern button background
                background = createSolutionBoxBackground(false)

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    120,
                    1f
                ).apply {
                    setMargins(8, 8, 8, 8)
                }

                // Remove default button padding
                setPadding(0, 0, 0, 0)
                stateListAnimator = null

                // Center the text
                gravity = android.view.Gravity.CENTER

                setOnClickListener {
                    hapticFeedback()
                    gameView.selectSolutionBox(index, answer)
                    updateSolutionBoxSelection(index)
                }
            }
            solutionBoxesLayout.addView(solutionBox)
        }
    }

    fun updateSolutionBoxSelection(selectedIndex: Int) {
        // Update visual state of all solution boxes
        for (i in 0 until solutionBoxesLayout.childCount) {
            val box = solutionBoxesLayout.getChildAt(i) as Button
            box.background = createSolutionBoxBackground(i == selectedIndex)
        }
    }

    private fun createSolutionBoxBackground(isSelected: Boolean): android.graphics.drawable.Drawable {
        val strokeWidth = 4
        val cornerRadius = 16f

        val gradientDrawable = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setCornerRadius(cornerRadius)

            if (isSelected) {
                // Selected state - bright colors
                setColor(Color.parseColor("#2A2A2A"))
                setStroke(strokeWidth, Color.parseColor("#00E676"))
            } else {
                // Normal state - dark with cyan border
                setColor(Color.parseColor("#1A1A1A"))
                setStroke(strokeWidth, Color.parseColor("#00BCD4"))
            }
        }

        // Create pressed state drawable
        val pressedDrawable = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setCornerRadius(cornerRadius)
            setColor(Color.parseColor("#3A3A3A"))
            setStroke(strokeWidth, Color.parseColor("#26C6DA"))
        }

        // Create state list drawable for press effects
        val stateListDrawable = android.graphics.drawable.StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
            addState(intArrayOf(), gradientDrawable)
        }

        return stateListDrawable
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
            // Haptic feedback not available
        }
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (gameView.gameState == GameState.PLAYING) {
            gameView.pauseGame()
        } else {
            showExitDialog()
        }
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit Game")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                super.onBackPressed()
            }
            .setNegativeButton("No", null)
            .show()
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
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
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
                    e.printStackTrace()
                }
            }
        }
    }
}


