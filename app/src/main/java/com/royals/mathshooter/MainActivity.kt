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
    var isBoss: Boolean = false
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

        // Create controls
        createControls(mainLayout)

        setContentView(mainLayout)

        // Connect UI elements to game view
        gameView.setUIElements(scoreText, livesText, waveText, comboText, difficultyText)
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

    private fun createControls(parent: LinearLayout) {
        controlsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            background = ContextCompat.getDrawable(this@MainActivity, android.R.drawable.screen_background_dark)
            setPadding(12, 12, 12, 12)
        }

        // Movement controls with better styling
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

        // Enhanced number pad with better visual design
        createNumberPad()

        parent.addView(controlsLayout)
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

    private fun createNumberPad() {
        val numberPadLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(12, 12, 12, 12)
        }

        val rows = arrayOf(
            arrayOf("7", "8", "9"),
            arrayOf("4", "5", "6"),
            arrayOf("1", "2", "3"),
            arrayOf("±", "0", "⌫")
        )

        rows.forEach { row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            row.forEach { number ->
                val button = Button(this).apply {
                    text = number
                    textSize = 28f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD

                    // Create modern button background with border
                    background = createModernButtonBackground(number)

                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        140, // Increased height for better appearance
                        1f
                    ).apply {
                        setMargins(6, 6, 6, 6) // Slightly larger margins
                    }

                    // Remove default button padding and elevation
                    setPadding(0, 0, 0, 0)
                    stateListAnimator = null

                    // Center the text
                    gravity = android.view.Gravity.CENTER

                    setOnClickListener {
                        hapticFeedback()
                        when (number) {
                            "±" -> gameView.selectAnswer(-1)
                            "⌫" -> gameView.clearAnswer()
                            else -> gameView.selectAnswer(number.toInt())
                        }
                    }
                }
                rowLayout.addView(button)
            }
            numberPadLayout.addView(rowLayout)
        }

        controlsLayout.addView(numberPadLayout)
    }

    private fun createModernButtonBackground(buttonType: String): android.graphics.drawable.Drawable {
        val strokeWidth = 4 // Border thickness
        val cornerRadius = 16f // Rounded corners

        // Create gradient drawable for the button
        val gradientDrawable = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setCornerRadius(cornerRadius)

            // Set background color based on button type
            when (buttonType) {
                "⌫" -> {
                    // Red background for clear button
                    setColor(Color.parseColor("#1A1A1A"))
                    setStroke(strokeWidth, Color.parseColor("#F44336"))
                }
                "±" -> {
                    // Blue background for negative button
                    setColor(Color.parseColor("#1A1A1A"))
                    setStroke(strokeWidth, Color.parseColor("#2196F3"))
                }
                else -> {
                    // Dark background with cyan border for numbers
                    setColor(Color.parseColor("#1A1A1A"))
                    setStroke(strokeWidth, Color.parseColor("#00BCD4"))
                }
            }
        }

        // Create pressed state drawable
        val pressedDrawable = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setCornerRadius(cornerRadius)

            when (buttonType) {
                "⌫" -> {
                    setColor(Color.parseColor("#2A2A2A"))
                    setStroke(strokeWidth, Color.parseColor("#FF5252"))
                }
                "±" -> {
                    setColor(Color.parseColor("#2A2A2A"))
                    setStroke(strokeWidth, Color.parseColor("#42A5F5"))
                }
                else -> {
                    setColor(Color.parseColor("#2A2A2A"))
                    setStroke(strokeWidth, Color.parseColor("#26C6DA"))
                }
            }
        }

        // Create state list drawable for press effects
        val stateListDrawable = android.graphics.drawable.StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)
            addState(intArrayOf(), gradientDrawable)
        }

        return stateListDrawable
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



class GameView(context: Context, attrs: AttributeSet? = null) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var gameThread: GameThread? = null
    var gameState = GameState.PLAYING
        private set

    // Game engine
    private val gameEngine = GameEngine(context)

    // Game objects
    private val enemies = mutableListOf<Enemy>()
    private val bullets = mutableListOf<Bullet>()
    private val powerUps = mutableListOf<PowerUp>()

    // Game state variables
    private var playerX = 0f
    private var playerY = 0f
    private var playerMovement = 0
    private var selectedAnswer = 0
    private var inputAnswer = ""
    private var score = 0
    private var lives = 3
    private var wave = 1
    private var combo = 0
    private var comboMultiplier = 1.0f

    // Enemy kill tracking
    private var totalEnemiesKilled = 0
    private var currentDifficultyLevel = 1

    // Difficulty upgrade notification system
    private var difficultyUpgradeMessage = ""
    private var difficultyUpgradeDetail = ""
    private var difficultyUpgradeTimer = 0

    // Touch controls
    private var isTouching = false
    private var touchX = 0f

    // UI elements
    private lateinit var scoreText: TextView
    private lateinit var livesText: TextView
    private lateinit var waveText: TextView
    private lateinit var comboText: TextView
    private lateinit var difficultyText: TextView

    // Timing
    private var lastEnemySpawn = 0L
    private var enemySpawnDelay = 2000L
    private var lastUpdate = 0L

    // Boss battle
    private var bossEnemy: Enemy? = null
    private var isBossWave = false

    // Game mode
    private var gameMode = GameMode.NORMAL

    // SharedPreferences for settings
    private val sharedPreferences = context.getSharedPreferences("MathShooterPrefs", Context.MODE_PRIVATE)

    // Paint objects
    private val playerPaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.FILL
    }

    private val enemyPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val bulletPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 32f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val equationPaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    init {
        holder.addCallback(this)
        initializeGame()
    }

    fun setGameMode(mode: GameMode) {
        gameMode = mode
        gameEngine.initializeGame(mode)
    }

    fun setUIElements(score: TextView, lives: TextView, wave: TextView, combo: TextView, difficulty: TextView) {
        scoreText = score
        livesText = lives
        waveText = wave
        comboText = combo
        difficultyText = difficulty
    }

    private fun initializeGame() {
        gameState = GameState.PLAYING
        score = 0
        lives = 3
        wave = 1
        combo = 0
        comboMultiplier = 1.0f
        inputAnswer = ""
        selectedAnswer = 0
        totalEnemiesKilled = 0
        currentDifficultyLevel = 1
        difficultyUpgradeMessage = ""
        difficultyUpgradeDetail = ""
        difficultyUpgradeTimer = 0
        enemies.clear()
        bullets.clear()
        powerUps.clear()
        bossEnemy = null
        isBossWave = false
        lastEnemySpawn = System.currentTimeMillis()
        enemySpawnDelay = max(2000L, 5000L - (wave * 200L))

        gameEngine.initializeGame(gameMode)
    }

    fun setMovement(direction: Int) {
        playerMovement = direction
    }

    fun selectAnswer(digit: Int) {
        if (digit == -1) {
            // Negative sign - toggle negative
            if (inputAnswer.startsWith("-")) {
                inputAnswer = inputAnswer.drop(1)
            } else {
                inputAnswer = "-$inputAnswer"
            }
        } else {
            // Add digit to input
            if (inputAnswer.length < 4) {
                inputAnswer += digit.toString()
            }
        }

        // Update selected answer
        selectedAnswer = inputAnswer.toIntOrNull() ?: 0
    }

    fun clearAnswer() {
        inputAnswer = ""
        selectedAnswer = 0
    }

    private fun getDifficultyFromKills(): Int {
        return when (totalEnemiesKilled) {
            in 0..49 -> 1      // Level 1: Basic Addition/Subtraction (0-49 kills)
            in 50..99 -> 2     // Level 2: Add Multiplication (50-99 kills)
            in 100..149 -> 3   // Level 3: Add Division (100-149 kills)
            in 150..199 -> 4   // Level 4: Two-step equations (150-199 kills)
            in 200..249 -> 5   // Level 5: Fractions/Decimals (200-249 kills)
            in 250..299 -> 6   // Level 6: Exponents (250-299 kills)
            in 300..349 -> 7   // Level 7: Negative numbers (300-349 kills)
            in 350..399 -> 8   // Level 8: Complex multi-step (350-399 kills)
            in 400..449 -> 9   // Level 9: Advanced operations (400-449 kills)
            else -> 10         // Level 10: Expert level (450+ kills)
        }
    }

    private fun checkDifficultyUpgrade() {
        val newDifficulty = getDifficultyFromKills()
        if (newDifficulty > currentDifficultyLevel) {
            currentDifficultyLevel = newDifficulty
            showDifficultyUpgrade(newDifficulty)
        }
    }

    private fun showDifficultyUpgrade(level: Int) {
        val levelInfo = when (level) {
            2 -> Pair("Multiplication Unlocked!", "Now includes × operations")
            3 -> Pair("Division Unlocked!", "Now includes ÷ operations")
            4 -> Pair("Two-step Equations!", "Complex math problems ahead")
            5 -> Pair("Fractions & Decimals!", "Advanced number types")
            6 -> Pair("Exponents Unlocked!", "Powers and square roots")
            7 -> Pair("Negative Numbers!", "Working with negative values")
            8 -> Pair("Complex Equations!", "Multi-step calculations")
            9 -> Pair("Advanced Math!", "Professional level problems")
            10 -> Pair("Expert Level!", "Master mathematician mode")
            else -> Pair("Level Up!", "Difficulty increased")
        }

        // Store the upgrade message to show on screen
        difficultyUpgradeMessage = levelInfo.first
        difficultyUpgradeDetail = levelInfo.second
        difficultyUpgradeTimer = 180 // Show for 3 seconds at 60 FPS

        println("🎉 DIFFICULTY UPGRADE! ${levelInfo.first} (${totalEnemiesKilled} enemies defeated)")
    }

    fun fire() {
        when (gameState) {
            GameState.PLAYING -> {
                bullets.add(Bullet(playerX, playerY - 30f, selectedAnswer))
            }
            GameState.GAME_OVER -> {
                // Restart the game
                initializeGame()
            }
            else -> {}
        }
    }

    fun pauseGame() {
        gameState = if (gameState == GameState.PLAYING) GameState.PAUSED else GameState.PLAYING
    }

    fun pause() {
        gameThread?.setRunning(false)
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun resume() {
        gameThread = GameThread(holder, this)
        gameThread?.setRunning(true)
        gameThread?.start()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        resume()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        playerX = width / 2f
        playerY = height - 100f
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pause()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val gameAreaHeight = height * 0.6f
                if (event.y < gameAreaHeight) {
                    isTouching = true
                    touchX = event.x
                    handleGameAreaTouch(event.x, event.y, event.action)
                    return true
                }
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                if (isTouching) {
                    val gameAreaHeight = height * 0.6f
                    if (event.y < gameAreaHeight) {
                        touchX = event.x
                        handleGameAreaTouch(event.x, event.y, event.action)
                    } else {
                        isTouching = false
                        playerMovement = 0
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouching = false
                playerMovement = 0
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleGameAreaTouch(x: Float, y: Float, action: Int) {
        val centerX = width / 2f
        val deadZone = 50f

        when {
            x < centerX - deadZone -> {
                playerMovement = -1
            }
            x > centerX + deadZone -> {
                playerMovement = 1
            }
            else -> {
                playerMovement = 0
                if (action == MotionEvent.ACTION_DOWN) {
                    fire()
                }
            }
        }
    }

    fun update() {
        if (gameState != GameState.PLAYING) return

        val currentTime = System.currentTimeMillis()
        lastUpdate = currentTime

        gameEngine.updatePowerUps()

        // Move player
        val finalMovement = if (isTouching) playerMovement else playerMovement
        if (finalMovement != 0) {
            val moveSpeed = 8f
            playerX += finalMovement * moveSpeed
            playerX = playerX.coerceIn(50f, width - 50f)
        }

        // Spawn enemies
        if (currentTime - lastEnemySpawn > enemySpawnDelay && enemies.size < 5) {
            spawnEnemy()
            lastEnemySpawn = currentTime
        }

        // Update enemies
        val speedMultiplier = if (gameEngine.isPowerUpActive(PowerUpType.TIME_FREEZE)) 0f else 1f
        enemies.forEach { enemy ->
            enemy.y += enemy.speed * speedMultiplier
            if (enemy.y > height) {
                if (!gameEngine.isPowerUpActive(PowerUpType.SHIELD)) {
                    lives--
                    combo = 0
                    comboMultiplier = 1.0f
                } else {
                    gameEngine.consumeShield()
                }
                enemy.isAlive = false
            }
        }
        enemies.removeAll { !it.isAlive }

        // Update bullets
        bullets.forEach { bullet ->
            bullet.y -= bullet.speed
            if (bullet.y < 0) {
                bullet.isActive = false
            }
        }
        bullets.removeAll { !it.isActive }

        // Update power-ups
        powerUps.forEach { powerUp ->
            powerUp.y += powerUp.speed
            if (powerUp.y > height) {
                powerUp.isActive = false
            }
        }
        powerUps.removeAll { !it.isActive }

        gameEngine.updateEffects()
        checkCollisions()

        // Check wave completion
        if (enemies.isEmpty() && currentTime - lastEnemySpawn > enemySpawnDelay * 2 && !isBossWave) {
            completeWave()
        }

        // Check game over
        if (lives <= 0) {
            gameState = GameState.GAME_OVER
            gameEngine.saveHighScore(score, wave)
        }

        updateUI()
    }

    private fun spawnEnemy() {
        // Check if it's time for a boss wave
        if (wave % 5 == 0 && !isBossWave && enemies.isEmpty()) {
            bossEnemy = gameEngine.initializeBoss(wave)
            bossEnemy?.let {
                it.isBoss = true
                enemies.add(it)
            }
            isBossWave = true
            return
        }

        if (isBossWave) return

        // Use kill-based difficulty instead of wave-based
        val currentDifficulty = getDifficultyFromKills()
        val equation = gameEngine.generateEquationForDifficulty(currentDifficulty)

        val enemy = Enemy(
            x = Random.nextFloat() * (width - 100f) + 50f,
            y = 50f,
            equation = equation.first,
            answer = equation.second,
            speed = 0.8f + (currentDifficulty * 0.15f)
        )
        enemies.add(enemy)

        println("Enemies Killed: $totalEnemiesKilled | Difficulty: $currentDifficulty | Equation: '${equation.first}' = ${equation.second}")

        // Occasionally spawn power-ups
        if (Random.nextFloat() < 0.15f) {
            spawnPowerUp()
        }
    }

    private fun spawnPowerUp() {
        val powerUp = PowerUp(
            x = Random.nextFloat() * (width - 50f) + 25f,
            y = 50f,
            type = PowerUpType.values().random()
        )
        powerUps.add(powerUp)
    }

    private fun checkCollisions() {
        val bulletsToRemove = mutableListOf<Bullet>()
        val enemiesToRemove = mutableListOf<Enemy>()
        val powerUpsToRemove = mutableListOf<PowerUp>()

        // Bullet-Enemy collisions
        bullets.forEach { bullet ->
            enemies.forEach { enemy ->
                if (abs(bullet.x - enemy.x) < 40 && abs(bullet.y - enemy.y) < 40) {
                    val isCorrect = bullet.value == enemy.answer || gameEngine.isPowerUpActive(PowerUpType.AUTO_SOLVE)

                    if (isCorrect) {
                        // Correct answer
                        val basePoints = 10 * wave
                        val difficultyBonus = currentDifficultyLevel * 5
                        val multiplier = comboMultiplier * (if (gameEngine.isPowerUpActive(PowerUpType.DOUBLE_POINTS)) 2 else 1)
                        val points = ((basePoints + difficultyBonus) * multiplier).toInt()
                        score += points
                        combo++
                        comboMultiplier = 1.0f + (combo * 0.1f)

                        clearAnswer()

                        // Handle boss battle
                        if (isBossWave && enemy.isBoss) {
                            val bossDefeated = gameEngine.handleBossHit()
                            if (bossDefeated) {
                                isBossWave = false
                                bossEnemy = null
                                score += wave * 100
                                totalEnemiesKilled++
                                gameEngine.addExplosion(enemy.x, enemy.y, ExplosionType.BOSS)
                            } else {
                                val newEquation = gameEngine.getCurrentBossEquation()
                                if (newEquation != null) {
                                    enemy.equation = newEquation.first
                                    enemy.answer = newEquation.second
                                }
                            }
                        }

                        if (gameEngine.isPowerUpActive(PowerUpType.AUTO_SOLVE)) {
                            gameEngine.consumeAutoSolve()
                        }

                        if (!isBossWave || !enemy.isBoss) {
                            enemiesToRemove.add(enemy)
                            gameEngine.addExplosion(enemy.x, enemy.y, ExplosionType.CORRECT)
                            totalEnemiesKilled++
                            checkDifficultyUpgrade()
                        }
                        bulletsToRemove.add(bullet)

                        gameEngine.recordShot(true)

                    } else {
                        // Wrong answer
                        combo = 0
                        comboMultiplier = 1.0f
                        bulletsToRemove.add(bullet)
                        gameEngine.recordShot(false)
                        gameEngine.addExplosion(bullet.x, bullet.y, ExplosionType.WRONG)
                    }
                }
            }
        }

        // Player-PowerUp collisions
        powerUps.forEach { powerUp ->
            if (abs(playerX - powerUp.x) < 50 && abs(playerY - powerUp.y) < 50) {
                gameEngine.activatePowerUp(powerUp.type)
                powerUpsToRemove.add(powerUp)
            }
        }

        bullets.removeAll(bulletsToRemove)
        enemies.removeAll(enemiesToRemove)
        powerUps.removeAll(powerUpsToRemove)
    }

    private fun completeWave() {
        wave++
        enemySpawnDelay = max(2000L, 5000L - (wave * 200L))

        val accuracyBonus = (combo * 10)
        score += accuracyBonus

        gameState = GameState.WAVE_COMPLETE

        Handler(Looper.getMainLooper()).postDelayed({
            gameState = GameState.PLAYING
        }, 2000)
    }

    private fun updateUI() {
        Handler(Looper.getMainLooper()).post {
            if (::scoreText.isInitialized) {
                scoreText.text = "Score: $score"
                livesText.text = "Lives: $lives"
                waveText.text = "Wave: $wave"
                comboText.text = "Combo: ${combo}x"
                difficultyText.text = "Level: $currentDifficultyLevel"
            }
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        when (gameState) {
            GameState.PLAYING -> drawGame(canvas)
            GameState.WAVE_COMPLETE -> {
                drawGame(canvas)
                drawCenteredText(canvas, "Wave $wave Complete!", height / 2f)
            }
            GameState.GAME_OVER -> {
                drawCenteredText(canvas, "GAME OVER", height / 2f - 100)
                drawCenteredText(canvas, "Final Score: $score", height / 2f - 50)

                val restartPaint = Paint().apply {
                    color = Color.YELLOW
                    textSize = 24f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.drawText("Tap FIRE to restart", width / 2f, height / 2f + 50, restartPaint)
            }
            else -> drawCenteredText(canvas, "Math Shooter", height / 2f)
        }
    }

    private fun drawGame(canvas: Canvas) {
        drawBackground(canvas)

        // Draw player
        val playerSize = 30f
        canvas.drawRect(
            playerX - playerSize, playerY - playerSize,
            playerX + playerSize, playerY + playerSize,
            playerPaint
        )

        canvas.drawRect(
            playerX - 5f, playerY - playerSize - 20f,
            playerX + 5f, playerY - playerSize,
            playerPaint
        )

        // Draw enemies
        drawEnemies(canvas)

        // Draw bullets
        bullets.forEach { bullet ->
            val bulletSize = 8f
            canvas.drawCircle(bullet.x, bullet.y, bulletSize, bulletPaint)

            canvas.drawText(
                bullet.value.toString(),
                bullet.x,
                bullet.y - 15f,
                equationPaint
            )
        }

        // Draw power-ups
        powerUps.forEach { powerUp ->
            val powerUpPaint = Paint().apply {
                color = when (powerUp.type) {
                    PowerUpType.TIME_FREEZE -> Color.BLUE
                    PowerUpType.AUTO_SOLVE -> Color.GREEN
                    PowerUpType.SHIELD -> Color.MAGENTA
                    PowerUpType.DOUBLE_POINTS -> Color.YELLOW
                    else -> Color.CYAN
                }
                style = Paint.Style.FILL
            }

            canvas.drawCircle(powerUp.x, powerUp.y, 20f, powerUpPaint)

            val symbol = when (powerUp.type) {
                PowerUpType.TIME_FREEZE -> "⏰"
                PowerUpType.AUTO_SOLVE -> "🤖"
                PowerUpType.SHIELD -> "🛡️"
                PowerUpType.DOUBLE_POINTS -> "2X"
                else -> "?"
            }

            canvas.drawText(symbol, powerUp.x, powerUp.y + 5f, equationPaint)
        }

        gameEngine.drawEffects(canvas)

        // Draw selected answer with input display
        val displayText = if (inputAnswer.isEmpty()) "Input: _" else "Input: $inputAnswer"
        canvas.drawText(
            displayText,
            100f,
            height - 50f,
            textPaint
        )

        canvas.drawText(
            "Answer: $selectedAnswer",
            100f,
            height - 20f,
            Paint().apply {
                color = Color.YELLOW
                textSize = 20f
                textAlign = Paint.Align.LEFT
                isAntiAlias = true
            }
        )

        // Draw power-up status
        var statusY = 100f
        if (gameEngine.isPowerUpActive(PowerUpType.TIME_FREEZE)) {
            canvas.drawText("TIME FREEZE ACTIVE", width / 2f, statusY, textPaint)
            statusY += 40f
        }
        if (gameEngine.isPowerUpActive(PowerUpType.DOUBLE_POINTS)) {
            canvas.drawText("DOUBLE POINTS ACTIVE", width / 2f, statusY, textPaint)
            statusY += 40f
        }
        if (gameEngine.isPowerUpActive(PowerUpType.SHIELD)) {
            canvas.drawText("SHIELD ACTIVE", width / 2f, statusY, textPaint)
            statusY += 40f
        }
        val autoSolveCount = gameEngine.getActivePowerUpCount(PowerUpType.AUTO_SOLVE)
        if (autoSolveCount > 0) {
            canvas.drawText("AUTO-SOLVE: $autoSolveCount", width / 2f, statusY, textPaint)
        }

        // Draw difficulty upgrade notification
        if (difficultyUpgradeTimer > 0) {
            difficultyUpgradeTimer--

            val alpha = if (difficultyUpgradeTimer > 150) {
                255
            } else {
                (difficultyUpgradeTimer * 255 / 30).coerceIn(0, 255)
            }

            val upgradePaint = Paint().apply {
                color = Color.YELLOW
                textSize = 36f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                this.alpha = alpha
            }

            val detailPaint = Paint().apply {
                color = Color.WHITE
                textSize = 24f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                this.alpha = alpha
            }

            canvas.drawText(difficultyUpgradeMessage, width / 2f, height / 3f, upgradePaint)
            canvas.drawText(difficultyUpgradeDetail, width / 2f, height / 3f + 50f, detailPaint)
        }

        // Draw current difficulty and kill count
        val difficultyInfoPaint = Paint().apply {
            color = Color.CYAN
            textSize = 18f
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }

        canvas.drawText(
            "Difficulty: $currentDifficultyLevel | Kills: $totalEnemiesKilled",
            20f,
            height - 150f,
            difficultyInfoPaint
        )

        // Show next difficulty milestone
        val nextMilestone = when (currentDifficultyLevel) {
            1 -> 50
            2 -> 100
            3 -> 150
            4 -> 200
            5 -> 250
            6 -> 300
            7 -> 350
            8 -> 400
            9 -> 450
            else -> null
        }

        nextMilestone?.let { milestone ->
            val remaining = milestone - totalEnemiesKilled
            canvas.drawText(
                "Next upgrade: $remaining kills",
                20f,
                height - 130f,
                difficultyInfoPaint
            )
        }

        if (isBossWave) {
            canvas.drawText("BOSS BATTLE!", width / 2f, 150f,
                Paint().apply {
                    color = Color.RED
                    textSize = 36f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                })
        }
    }

    private fun drawEnemies(canvas: Canvas) {
        enemies.forEach { enemy ->
            val enemySize = if (enemy.isBoss) 60f else 40f

            val enemyColor = when (currentDifficultyLevel) {
                1 -> Color.RED
                2 -> Color.parseColor("#FF6600")
                3 -> Color.parseColor("#FF3300")
                4 -> Color.parseColor("#CC0066")
                5 -> Color.parseColor("#9900CC")
                6 -> Color.parseColor("#6600FF")
                7 -> Color.parseColor("#0066FF")
                8 -> Color.parseColor("#00CCFF")
                9 -> Color.parseColor("#00FF99")
                else -> Color.parseColor("#FFFF00")
            }

            val paint = if (enemy.isBoss) {
                Paint().apply {
                    color = Color.MAGENTA
                    style = Paint.Style.FILL
                }
            } else {
                Paint().apply {
                    color = enemyColor
                    style = Paint.Style.FILL
                }
            }

            canvas.drawRect(
                enemy.x - enemySize, enemy.y - enemySize,
                enemy.x + enemySize, enemy.y + enemySize,
                paint
            )

            canvas.drawText(
                enemy.equation,
                enemy.x,
                enemy.y + 5f,
                equationPaint
            )

            if (enemy.isBoss) {
                val healthPercent = gameEngine.getBossHealthPercentage()
                val barWidth = 120f
                val barHeight = 8f
                val barX = enemy.x - barWidth / 2
                val barY = enemy.y - enemySize - 20f

                canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight,
                    Paint().apply { color = Color.DKGRAY })

                canvas.drawRect(barX, barY, barX + (barWidth * healthPercent), barY + barHeight,
                    Paint().apply { color = Color.RED })
            }
        }
    }

    private fun drawBackground(canvas: Canvas) {
        val random = Random(42)
        repeat(100) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val brightness = random.nextFloat()
            canvas.drawCircle(x, y, 1f, Paint().apply {
                color = Color.argb((255 * brightness).toInt(), 255, 255, 255)
            })
        }
    }

    private fun drawCenteredText(canvas: Canvas, text: String, y: Float) {
        canvas.drawText(text, width / 2f, y, textPaint)
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


