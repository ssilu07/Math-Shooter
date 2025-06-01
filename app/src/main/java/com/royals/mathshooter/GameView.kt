package com.royals.mathshooter

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

// Updated GameView.kt - Solution Box System

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

    // Solution box system - Enhanced with proper zero handling
    private var currentSolutionBoxes = listOf<Int>()
    private var selectedSolutionIndex = -1
    private var selectedAnswer = 0
    private var hasValidSelection = false  // NEW: Track if we have a valid selection
    private lateinit var solutionBoxesLayout: LinearLayout

    // Track the target enemy for solution boxes
    private var targetEnemyId = -1
    private var nextEnemyId = 0

    // Flag to prevent solution box updates during active selection
    private var hasPendingSelection = false

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

    fun setSolutionBoxesLayout(layout: LinearLayout) {
        solutionBoxesLayout = layout
    }

    private fun initializeGame() {
        gameState = GameState.PLAYING
        score = 0
        lives = 3
        wave = 1
        combo = 0
        comboMultiplier = 1.0f
        selectedAnswer = 0
        selectedSolutionIndex = -1
        hasValidSelection = false
        hasPendingSelection = false
        targetEnemyId = -1
        nextEnemyId = 0
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

        println("🎮 Game initialized")
    }

    fun setMovement(direction: Int) {
        playerMovement = direction
    }

    fun selectSolutionBox(index: Int, answer: Int) {
        selectedSolutionIndex = index
        selectedAnswer = answer
        hasValidSelection = true  // Mark that we have a valid selection
        hasPendingSelection = true

        println("🎯 Selected solution box $index with answer: $answer")

        // Update UI in main thread
        Handler(Looper.getMainLooper()).post {
            (context as MainActivity).updateSolutionBoxSelection(index)
        }
    }

    private fun generateNewSolutionBoxes() {
        // Don't update solution boxes if player has made a selection
        if (hasPendingSelection) {
            return
        }

        // Find the first non-boss enemy, or boss if that's all we have
        val targetEnemy = enemies.firstOrNull { !it.isBoss } ?: enemies.firstOrNull()

        if (targetEnemy == null) {
            // No enemies - clear solution boxes
            currentSolutionBoxes = listOf()
            targetEnemyId = -1
            selectedSolutionIndex = -1
            selectedAnswer = 0
            hasValidSelection = false
            hasPendingSelection = false

            Handler(Looper.getMainLooper()).post {
                (context as MainActivity).updateSolutionBoxes(listOf(0, 0, 0)) // Placeholder
                (context as MainActivity).updateSolutionBoxSelection(-1)
            }
            return
        }

        // Check if we already have solution boxes for this enemy
        if (targetEnemyId == targetEnemy.id) {
            return
        }

        // Generate solution boxes for the target enemy
        val numOptions = if (currentDifficultyLevel <= 3) 3 else 4
        val correctAnswer = targetEnemy.answer
        val options = mutableListOf<Int>()

        // Add the correct answer
        options.add(correctAnswer)

        // Generate incorrect answers
        while (options.size < numOptions) {
            val incorrectAnswer = generateIncorrectAnswer(correctAnswer)
            if (!options.contains(incorrectAnswer)) {
                options.add(incorrectAnswer)
            }
        }

        // Shuffle the options
        options.shuffle()
        currentSolutionBoxes = options
        targetEnemyId = targetEnemy.id

        // Only reset selection if no pending selection
        if (!hasPendingSelection) {
            selectedSolutionIndex = -1
            selectedAnswer = 0
            hasValidSelection = false
        }

        println("🎯 Generated solution boxes for enemy ${targetEnemy.id}: ${targetEnemy.equation} = ${targetEnemy.answer}")
        println("📦 Solution boxes: $options")

        // Update UI in main thread
        Handler(Looper.getMainLooper()).post {
            (context as MainActivity).updateSolutionBoxes(currentSolutionBoxes)
            if (!hasPendingSelection) {
                (context as MainActivity).updateSolutionBoxSelection(-1)
            }
        }
    }

    private fun generateIncorrectAnswer(correctAnswer: Int): Int {
        val variance = when (currentDifficultyLevel) {
            1, 2 -> listOf(-3, -2, -1, 1, 2, 3)
            3, 4 -> listOf(-5, -4, -3, -2, -1, 1, 2, 3, 4, 5)
            5, 6 -> listOf(-10, -8, -5, -3, -1, 1, 3, 5, 8, 10)
            else -> listOf(-20, -15, -10, -5, -2, -1, 1, 2, 5, 10, 15, 20)
        }

        var incorrectAnswer: Int
        do {
            incorrectAnswer = correctAnswer + variance.random()
            // Ensure positive answers for early levels
            if (currentDifficultyLevel <= 2 && incorrectAnswer < 0) {
                incorrectAnswer = abs(incorrectAnswer)
            }
        } while (incorrectAnswer == correctAnswer)

        return incorrectAnswer
    }

    fun fire() {
        when (gameState) {
            GameState.PLAYING -> {
                // FIXED: Check hasValidSelection instead of selectedAnswer != 0
                if (hasValidSelection && selectedSolutionIndex >= 0) {
                    println("🔥 FIRING with answer: $selectedAnswer")
                    bullets.add(Bullet(playerX, playerY - 30f, selectedAnswer))
                } else {
                    println("❌ Cannot fire: No valid selection (hasValidSelection=$hasValidSelection, selectedIndex=$selectedSolutionIndex)")
                }
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
        val enemiesToRemove = mutableListOf<Enemy>()

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
                enemiesToRemove.add(enemy)
                println("💀 Enemy ${enemy.id} went off screen: ${enemy.equation}")
            }
        }

        // Remove enemies that went off screen
        enemies.removeAll(enemiesToRemove)

        // If the target enemy went off screen, generate new solution boxes
        if (enemiesToRemove.any { it.id == targetEnemyId }) {
            println("🎯 Target enemy went off screen, generating new solution boxes...")
            selectedSolutionIndex = -1
            selectedAnswer = 0
            hasValidSelection = false
            hasPendingSelection = false
            targetEnemyId = -1
            generateNewSolutionBoxes()

            Handler(Looper.getMainLooper()).post {
                (context as MainActivity).updateSolutionBoxSelection(-1)
            }
        }

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
                it.id = nextEnemyId++
                enemies.add(it)
            }
            isBossWave = true

            // Only generate new solution boxes if no pending selection
            if (!hasPendingSelection) {
                targetEnemyId = -1  // Reset for boss
                generateNewSolutionBoxes()
            }
            return
        }

        if (isBossWave) return

        val currentDifficulty = getDifficultyFromKills()
        val equation = gameEngine.generateEquationForDifficulty(currentDifficulty)

        val enemy = Enemy(
            x = Random.nextFloat() * (width - 100f) + 50f,
            y = 50f,
            equation = equation.first,
            answer = equation.second,
            speed = 0.8f + (currentDifficulty * 0.15f),
            id = nextEnemyId++
        )
        enemies.add(enemy)

        println("👾 Spawned enemy ${enemy.id}: ${enemy.equation} = ${enemy.answer}")

        // Only generate new solution boxes for the FIRST enemy when no selection is pending
        if (enemies.size == 1 && !hasPendingSelection) {
            generateNewSolutionBoxes()
        }

        // Occasionally spawn power-ups
        if (Random.nextFloat() < 0.15f) {
            spawnPowerUp()
        }
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
                        println("✅ Correct hit! Enemy ${enemy.id} destroyed with answer ${bullet.value}")
                        handleCorrectAnswer(enemy, bullet)

                        if (!isBossWave || !enemy.isBoss) {
                            enemiesToRemove.add(enemy)
                            gameEngine.addExplosion(enemy.x, enemy.y, ExplosionType.CORRECT)
                            totalEnemiesKilled++
                            checkDifficultyUpgrade()
                        }
                        bulletsToRemove.add(bullet)
                        gameEngine.recordShot(true)

                    } else {
                        println("❌ Wrong answer! Expected ${enemy.answer}, got ${bullet.value}")
                        handleWrongAnswer()
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

        // IMPORTANT: After removing enemies, check if we need new solution boxes
        if (enemiesToRemove.isNotEmpty()) {
            val wasTargetDestroyed = enemiesToRemove.any { it.id == targetEnemyId }
            if (wasTargetDestroyed) {
                println("🎯 Target enemy destroyed, generating new solution boxes...")
                generateNewSolutionBoxes()
            }
        }
    }

    private fun handleCorrectAnswer(enemy: Enemy, bullet: Bullet) {
        // Calculate score
        val basePoints = 10 * wave
        val difficultyBonus = currentDifficultyLevel * 5
        val multiplier = comboMultiplier * (if (gameEngine.isPowerUpActive(PowerUpType.DOUBLE_POINTS)) 2 else 1)
        val points = ((basePoints + difficultyBonus) * multiplier).toInt()
        score += points
        combo++
        comboMultiplier = 1.0f + (combo * 0.1f)

        // CLEAR SELECTION - allow new solution boxes to be generated
        selectedSolutionIndex = -1
        selectedAnswer = 0
        hasValidSelection = false
        hasPendingSelection = false
        targetEnemyId = -1  // Reset enemy tracking

        // Handle boss battle
        if (isBossWave && enemy.isBoss) {
            val bossDefeated = gameEngine.handleBossHit()
            if (bossDefeated) {
                isBossWave = false
                bossEnemy = null
                score += wave * 100
                // Generate solution boxes for next wave
                generateNewSolutionBoxes()
            } else {
                val newEquation = gameEngine.getCurrentBossEquation()
                if (newEquation != null) {
                    enemy.equation = newEquation.first
                    enemy.answer = newEquation.second
                    enemy.id = nextEnemyId++ // New ID for updated boss
                    // Generate new solution boxes for the updated boss equation
                    generateNewSolutionBoxes()
                }
            }
        } else {
            // Normal enemy defeated - generate solution boxes for remaining/next enemies
            generateNewSolutionBoxes()
        }

        if (gameEngine.isPowerUpActive(PowerUpType.AUTO_SOLVE)) {
            gameEngine.consumeAutoSolve()
        }

        // Update UI to clear selection
        Handler(Looper.getMainLooper()).post {
            (context as MainActivity).updateSolutionBoxSelection(-1)
        }
    }

    private fun handleWrongAnswer() {
        // Wrong answer - DON'T clear selection, DON'T change solution boxes
        combo = 0
        comboMultiplier = 1.0f

        // Keep the current selection and solution boxes unchanged
        // Player can try again with the same boxes

        // Optional: Add visual feedback for wrong answer
        Handler(Looper.getMainLooper()).post {
            // Flash the selected box red briefly, then return to normal
            if (selectedSolutionIndex >= 0) {
                // This could be enhanced with a brief red flash animation
                (context as MainActivity).updateSolutionBoxSelection(selectedSolutionIndex)
            }
        }
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

    private fun spawnPowerUp() {
        val powerUp = PowerUp(
            x = Random.nextFloat() * (width - 50f) + 25f,
            y = 50f,
            type = PowerUpType.values().random()
        )
        powerUps.add(powerUp)
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

        // Enhanced selected answer display with debugging info
        if (hasValidSelection) {
            val displayPaint = Paint().apply {
                color = Color.GREEN
                textSize = 24f
                textAlign = Paint.Align.LEFT
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(
                "Selected: $selectedAnswer ✓",
                100f,
                height - 50f,
                displayPaint
            )
        } else {
            val displayPaint = Paint().apply {
                color = Color.GRAY
                textSize = 20f
                textAlign = Paint.Align.LEFT
                isAntiAlias = true
            }
            canvas.drawText(
                "Select an answer below",
                100f,
                height - 50f,
                displayPaint
            )
        }

        // Debug info for target enemy and selection state
        val targetEnemy = enemies.firstOrNull { it.id == targetEnemyId }
        if (targetEnemy != null) {
            val debugPaint = Paint().apply {
                color = Color.YELLOW
                textSize = 16f
                textAlign = Paint.Align.LEFT
                isAntiAlias = true
            }
            canvas.drawText(
                "Target: ${targetEnemy.equation} = ${targetEnemy.answer}",
                20f,
                height - 100f,
                debugPaint
            )
        }

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