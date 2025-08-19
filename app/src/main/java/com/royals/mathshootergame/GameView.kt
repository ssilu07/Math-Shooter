package com.royals.mathshootergame

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
import kotlin.math.sqrt
import kotlin.random.Random

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
    private val targetedBullets = mutableListOf<TargetedBullet>()

    // Game state variables
    private var playerX = 0f
    private var playerY = 0f

    // Solution box system - FIXED
    private var currentSolutionBoxes = listOf<Int>()
    private var selectedSolutionIndex = -1
    private var selectedAnswer = 0
    private var hasValidSelection = false
    private lateinit var solutionBoxesLayout: LinearLayout

    // Track the target enemy for solution boxes - FIXED
    private var targetEnemyId = -1
    private var nextEnemyId = 0

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

    // Add sound manager
    private var soundManager: SoundManager? = null

    fun setSoundManager(soundManager: SoundManager) {
        this.soundManager = soundManager
    }

    fun initializeGame() {
        gameState = GameState.PLAYING
        score = 0
        lives = 3
        wave = 1
        combo = 0
        comboMultiplier = 1.0f

        // CLEAR ALL SOLUTION BOX STATE
        selectedAnswer = 0
        selectedSolutionIndex = -1
        hasValidSelection = false
        targetEnemyId = -1
        currentSolutionBoxes = listOf()

        nextEnemyId = 0
        totalEnemiesKilled = 0
        currentDifficultyLevel = 1
        difficultyUpgradeMessage = ""
        difficultyUpgradeDetail = ""
        difficultyUpgradeTimer = 0

        enemies.clear()
        bullets.clear()
        powerUps.clear()
        targetedBullets.clear()
        bossEnemy = null
        isBossWave = false
        lastEnemySpawn = System.currentTimeMillis()
        enemySpawnDelay = 1000L

        gameEngine.initializeGame(gameMode)
        spawnFirstEnemy()

        println("🎮 Game initialized with fixed player position")
    }

    private fun spawnFirstEnemy() {
        val equation = when (gameMode) {
            GameMode.PRACTICE -> gameEngine.generatePracticeEquation()
            else -> gameEngine.generateEquationForDifficulty(1)
        }

        val enemy = Enemy(
            x = Random.nextFloat() * (width - 100f) + 50f,
            y = 50f,
            equation = equation.first,
            answer = equation.second,
            speed = if (gameMode == GameMode.PRACTICE) 1.0f else 0.8f,
            id = nextEnemyId++
        )
        enemies.add(enemy)

        println("🎯 Spawned first enemy: ${enemy.equation} = ${enemy.answer} (ID: ${enemy.id})")

        // IMMEDIATELY generate solution boxes for first enemy
        generateSolutionBoxesForEnemy(enemy)
    }

    fun selectSolutionBox(index: Int, answer: Int) {
        selectedSolutionIndex = index
        selectedAnswer = answer
        hasValidSelection = true

        println("🎯 Selected solution box $index with answer: $answer")

        // Update UI selection first
        Handler(Looper.getMainLooper()).post {
            (context as MainActivity).updateSolutionBoxSelection(index)
        }

        // AUTOMATICALLY FIRE after selection
        Handler(Looper.getMainLooper()).postDelayed({
            autoFire()
        }, 200) // Small delay for visual feedback
    }

    // NEW: Automatic firing when answer is selected
    private fun autoFire() {
        if (enemies.isEmpty()) {
            println("❌ Cannot auto-fire: No enemies present")
            return
        }

        // Find the target enemy
        val targetEnemy = enemies.firstOrNull { it.id == targetEnemyId }
        if (targetEnemy == null) {
            println("❌ Cannot auto-fire: No target enemy found")
            return
        }

        // Auto-solve takes priority
        if (gameEngine.isPowerUpActive(PowerUpType.AUTO_SOLVE)) {
            val targetedBullet = TargetedBullet(
                x = playerX,
                y = playerY - 30f,
                value = targetEnemy.answer,
                targetX = targetEnemy.x,
                targetY = targetEnemy.y,
                targetId = targetEnemy.id
            )
            targetedBullets.add(targetedBullet)
            soundManager?.playShootSound()
            println("🤖 AUTO-FIRE: Firing with correct answer ${targetEnemy.answer}")
            return
        }

        // Normal auto-firing with selected answer
        println("🔥 AUTO-FIRE at enemy ${targetEnemy.id} with selected answer: $selectedAnswer")
        println("🎯 Target position: (${targetEnemy.x}, ${targetEnemy.y})")

        val targetedBullet = TargetedBullet(
            x = playerX,
            y = playerY - 30f,
            value = selectedAnswer,
            targetX = targetEnemy.x,
            targetY = targetEnemy.y,
            targetId = targetEnemy.id
        )

        targetedBullets.add(targetedBullet)
        soundManager?.playShootSound()
    }

    // COMPLETELY REWRITTEN SOLUTION BOX GENERATION
    private fun generateSolutionBoxesForEnemy(enemy: Enemy) {
        // Don't generate if already generated for this enemy
        if (enemy.id == targetEnemyId) {
            println("🎯 Solution boxes already exist for enemy ${enemy.id}")
            return
        }

        println("🎯 Generating NEW solution boxes for enemy ${enemy.id}: ${enemy.equation} = ${enemy.answer}")

        val correctAnswer = enemy.answer
        val numOptions = if (currentDifficultyLevel <= 3) 3 else 4
        val options = mutableSetOf<Int>()

        // ALWAYS add correct answer first
        options.add(correctAnswer)
        println("📦 Added correct answer: $correctAnswer")

        // Generate unique incorrect answers
        var attempts = 0
        while (options.size < numOptions && attempts < 50) {
            val incorrectAnswer = generateIncorrectAnswer(correctAnswer)
            if (options.add(incorrectAnswer)) {
                println("📦 Added incorrect answer: $incorrectAnswer")
            }
            attempts++
        }

        // Convert to list and shuffle
        val finalOptions = options.toList().shuffled()

        // Update state
        currentSolutionBoxes = finalOptions
        targetEnemyId = enemy.id

        // Clear any previous selection
        selectedSolutionIndex = -1
        selectedAnswer = 0
        hasValidSelection = false

        println("🎯 FINAL boxes for enemy ${enemy.id}: $finalOptions (correct: $correctAnswer)")

        // Update UI immediately
        Handler(Looper.getMainLooper()).post {
            println("📱 Updating UI with new solution boxes...")
            (context as MainActivity).updateSolutionBoxes(currentSolutionBoxes)
            (context as MainActivity).updateSolutionBoxSelection(-1)
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
            if (currentDifficultyLevel <= 2 && incorrectAnswer < 0) {
                incorrectAnswer = abs(incorrectAnswer)
            }
        } while (incorrectAnswer == correctAnswer)

        return incorrectAnswer
    }

    private fun fire() {
        when (gameState) {
            GameState.PLAYING -> {
                if (enemies.isEmpty()) {
                    println("❌ Cannot fire: No enemies present")
                    return
                }

                // Must select answer first
                if (!hasValidSelection || selectedSolutionIndex < 0) {
                    println("❌ Cannot fire: Must select an answer first!")
                    showSelectAnswerPrompt()
                    return
                }

                // Find the target enemy
                val targetEnemy = enemies.firstOrNull { it.id == targetEnemyId }
                if (targetEnemy == null) {
                    println("❌ Cannot fire: No target enemy found")
                    return
                }

                // Auto-solve takes priority
                if (gameEngine.isPowerUpActive(PowerUpType.AUTO_SOLVE)) {
                    val targetedBullet = TargetedBullet(
                        x = playerX,
                        y = playerY - 30f,
                        value = targetEnemy.answer,
                        targetX = targetEnemy.x,
                        targetY = targetEnemy.y,
                        targetId = targetEnemy.id
                    )
                    targetedBullets.add(targetedBullet)
                    soundManager?.playShootSound()
                    println("🤖 AUTO-SOLVE: Firing with correct answer ${targetEnemy.answer}")
                    return
                }

                // Normal firing - Use current enemy position for better accuracy
                println("🔥 FIRING at enemy ${targetEnemy.id} with answer: $selectedAnswer")
                println("🎯 Target position: (${targetEnemy.x}, ${targetEnemy.y})")

                val targetedBullet = TargetedBullet(
                    x = playerX,
                    y = playerY - 30f,
                    value = selectedAnswer,
                    targetX = targetEnemy.x,  // Use current position
                    targetY = targetEnemy.y,  // Use current position
                    targetId = targetEnemy.id
                )

                targetedBullets.add(targetedBullet)
                soundManager?.playShootSound()
            }
            GameState.GAME_OVER -> {
                initializeGame()
            }
            else -> {}
        }
    }

    private fun showSelectAnswerPrompt() {
        Handler(Looper.getMainLooper()).post {
            (context as MainActivity).highlightSolutionBoxes()
        }
    }

    private fun updateTargetedBullets() {
        targetedBullets.forEach { bullet ->
            // Calculate direction toward target
            val deltaX = bullet.targetX - bullet.x
            val deltaY = bullet.targetY - bullet.y
            val distance = sqrt(deltaX * deltaX + deltaY * deltaY)

            // IMPROVED: Check for collision with target enemy first
            val targetEnemy = enemies.firstOrNull { it.id == bullet.targetId }
            if (targetEnemy != null && isTargetedBulletColliding(bullet, targetEnemy)) {
                bullet.isActive = false
                handleBulletReachedTarget(bullet)
                return@forEach
            }

            // If not colliding, continue moving toward target
            if (distance > 5f) { // Reduced threshold for better accuracy
                val speed = 12f // Slightly slower for better accuracy
                bullet.x += (deltaX / distance) * speed
                bullet.y += (deltaY / distance) * speed
            } else {
                // Reached target position
                bullet.isActive = false
                handleBulletReachedTarget(bullet)
            }

            // Remove if off screen
            if (bullet.y < 0 || bullet.y > height || bullet.x < 0 || bullet.x > width) {
                bullet.isActive = false
            }
        }
        targetedBullets.removeAll { !it.isActive }
    }

    // NEW: Collision detection specifically for targeted bullets
    private fun isTargetedBulletColliding(bullet: TargetedBullet, enemy: Enemy): Boolean {
        val enemyWidth = if (enemy.isBoss) 140f else 120f
        val enemyHeight = if (enemy.isBoss) 80f else 60f

        val enemyLeft = enemy.x - enemyWidth / 2
        val enemyRight = enemy.x + enemyWidth / 2
        val enemyTop = enemy.y - enemyHeight / 2
        val enemyBottom = enemy.y + enemyHeight / 2

        val bulletRadius = 10f // Slightly larger for targeted bullets
        val bulletLeft = bullet.x - bulletRadius
        val bulletRight = bullet.x + bulletRadius
        val bulletTop = bullet.y - bulletRadius
        val bulletBottom = bullet.y + bulletRadius

        val isColliding = bulletRight >= enemyLeft &&
                bulletLeft <= enemyRight &&
                bulletBottom >= enemyTop &&
                bulletTop <= enemyBottom

        if (isColliding) {
            println("🎯 Targeted bullet collision detected with enemy ${enemy.id}")
        }

        return isColliding
    }

    private fun handleBulletReachedTarget(bullet: TargetedBullet) {
        val targetEnemy = enemies.firstOrNull { it.id == bullet.targetId }
        if (targetEnemy != null) {
            val isCorrect = bullet.value == targetEnemy.answer

            if (isCorrect) {
                println("✅ HIT! Enemy ${targetEnemy.id} destroyed with answer ${bullet.value}")

                // Handle correct answer BEFORE removing enemy
                handleCorrectAnswer(targetEnemy)

                // Remove enemy after handling
                if (!isBossWave || !targetEnemy.isBoss) {
                    enemies.remove(targetEnemy)
                    gameEngine.addExplosion(targetEnemy.x, targetEnemy.y, ExplosionType.CORRECT)
                    totalEnemiesKilled++
                    checkDifficultyUpgrade()

                    // IMMEDIATELY find new target after enemy removal
                    println("🎯 Enemy removed, immediately finding new target...")
                    findNewTargetEnemy()
                }
                gameEngine.recordShot(true)
            } else {
                println("❌ MISS! Expected ${targetEnemy.answer}, got ${bullet.value}")
                handleWrongAnswer()
                gameEngine.recordShot(false)
                gameEngine.addExplosion(bullet.x, bullet.y, ExplosionType.WRONG)
            }
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
        playerX = width / 2f // FIXED POSITION IN CENTER
        playerY = height - 100f
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        pause()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if game is over and restart on touch
                if (gameState == GameState.GAME_OVER) {
                    initializeGame()
                    return true
                }

                val gameAreaHeight = height * 0.6f
                if (event.y < gameAreaHeight) {
                    // Touch in game area - can add future functionality if needed
                    return true
                }
                return false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun update() {
        if (gameState != GameState.PLAYING) return

        val currentTime = System.currentTimeMillis()
        lastUpdate = currentTime

        gameEngine.updatePowerUps()

        // PLAYER STAYS IN FIXED POSITION
        playerX = width / 2f

        // Update targeted bullets
        updateTargetedBullets()

        // AUTO-SOLVE
        if (gameEngine.isPowerUpActive(PowerUpType.AUTO_SOLVE) &&
            enemies.isNotEmpty() &&
            targetedBullets.isEmpty()) {
            handleAutoSolve()
        }

        // Spawn enemies
        val dynamicSpawnDelay = when {
            wave == 1 && enemies.isEmpty() -> 500L
            wave <= 3 -> max(1500L, enemySpawnDelay)
            else -> enemySpawnDelay
        }

        if (currentTime - lastEnemySpawn > dynamicSpawnDelay && enemies.size < 5) {
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
                    if (gameEngine.consumeExtraLife()) {
                        println("💚 Extra life consumed!")
                        gameEngine.addExplosion(playerX, playerY, ExplosionType.CORRECT)
                    } else {
                        lives--
                        combo = 0
                        comboMultiplier = 1.0f
                    }
                } else {
                    gameEngine.consumeShield()
                }
                enemy.isAlive = false
                enemiesToRemove.add(enemy)
                println("💀 Enemy ${enemy.id} went off screen")
            }
        }

        // Remove enemies that went off screen
        enemies.removeAll(enemiesToRemove)

        // CRITICAL: Check if target enemy was removed
        if (enemiesToRemove.any { it.id == targetEnemyId }) {
            println("🎯 Target enemy removed, finding new target...")
            findNewTargetEnemy()
        }

        // Update regular bullets
        bullets.forEach { bullet ->
            bullet.y -= bullet.speed
            if (bullet.y < 0) {
                bullet.isActive = false
            }
        }
        bullets.removeAll { !it.isActive }

        // Update power-ups - REMOVED since player can't collect them
        /*
        powerUps.forEach { powerUp ->
            powerUp.y += powerUp.speed
            if (powerUp.y > height) {
                powerUp.isActive = false
            }
        }
        powerUps.removeAll { !it.isActive }
        */

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

    // NEW: Find new target enemy when current target is destroyed/removed
    private fun findNewTargetEnemy() {
        println("🔍 Finding new target enemy...")

        // Clear current target state
        targetEnemyId = -1
        selectedSolutionIndex = -1
        selectedAnswer = 0
        hasValidSelection = false

        // Find next available enemy
        val nextEnemy = enemies.firstOrNull { !it.isBoss } ?: enemies.firstOrNull()

        if (nextEnemy != null) {
            println("🎯 Found new target enemy: ${nextEnemy.id} (${nextEnemy.equation} = ${nextEnemy.answer})")
            generateSolutionBoxesForEnemy(nextEnemy)
        } else {
            println("🎯 No enemies available, clearing solution boxes")
            currentSolutionBoxes = listOf()
            Handler(Looper.getMainLooper()).post {
                (context as MainActivity).updateSolutionBoxes(listOf())
                (context as MainActivity).updateSolutionBoxSelection(-1)
            }
        }
    }

    private fun handleAutoSolve() {
        if (!gameEngine.isPowerUpActive(PowerUpType.AUTO_SOLVE)) return

        val targetEnemy = enemies.firstOrNull { !it.isBoss } ?: enemies.firstOrNull()

        if (targetEnemy != null) {
            val correctAnswer = targetEnemy.answer

            val targetedBullet = TargetedBullet(
                x = playerX,
                y = playerY - 30f,
                value = correctAnswer,
                targetX = targetEnemy.x,
                targetY = targetEnemy.y,
                targetId = targetEnemy.id
            )
            targetedBullets.add(targetedBullet)

            soundManager?.playShootSound()
            println("🤖 AUTO-SOLVE: Firing with correct answer $correctAnswer")
            gameEngine.addExplosion(playerX, playerY - 30f, ExplosionType.NORMAL)
        }
    }

    private fun spawnEnemy() {
        if (gameMode != GameMode.PRACTICE && wave % 5 == 0 && !isBossWave && enemies.isEmpty()) {
            bossEnemy = gameEngine.initializeBoss(wave)
            bossEnemy?.let {
                it.isBoss = true
                it.id = nextEnemyId++
                val speedMultiplier = sharedPreferences.getFloat("enemy_speed_multiplier", 1.0f)
                it.speed = (0.5f + (getDifficultyFromKills() * 0.15f)) * speedMultiplier
                enemies.add(it)
            }
            isBossWave = true

            // Generate boxes for boss
            bossEnemy?.let { generateSolutionBoxesForEnemy(it) }
            return
        }

        if (isBossWave) return

        val equation = when (gameMode) {
            GameMode.PRACTICE -> gameEngine.generatePracticeEquation()
            else -> {
                val currentDifficulty = getDifficultyFromKills()
                gameEngine.generateEquationForDifficulty(currentDifficulty)
            }
        }

        val speedMultiplier = sharedPreferences.getFloat("enemy_speed_multiplier", 1.0f)
        val baseSpeed = if (gameMode == GameMode.PRACTICE) {
            1.0f
        } else {
            0.8f + (getDifficultyFromKills() * 0.15f)
        }
        val finalSpeed = baseSpeed * speedMultiplier

        val enemy = Enemy(
            x = Random.nextFloat() * (width - 100f) + 50f,
            y = 50f,
            equation = equation.first,
            answer = equation.second,
            speed = finalSpeed,
            id = nextEnemyId++
        )
        enemies.add(enemy)

        println("👾 Spawned enemy ${enemy.id}: ${enemy.equation} = ${enemy.answer}")

        // Generate solution boxes if this is the first enemy or no target exists
        if (targetEnemyId == -1) {
            generateSolutionBoxesForEnemy(enemy)
        }

        // REMOVED: Power-up spawning since player can't collect them
        /*
        val powerUpChance = if (gameMode == GameMode.PRACTICE) 0.05f else 0.15f
        if (Random.nextFloat() < powerUpChance) {
            spawnPowerUp()
        }
        */
    }

    private fun checkCollisions() {
        val bulletsToRemove = mutableListOf<Bullet>()
        val enemiesToRemove = mutableListOf<Enemy>()
        // REMOVED: powerUpsToRemove since no power-ups now

        // Regular bullet collisions
        bullets.forEach { bullet ->
            enemies.forEach { enemy ->
                if (isColliding(bullet, enemy)) {
                    val isCorrect = bullet.value == enemy.answer // REMOVED: auto-solve check

                    if (isCorrect) {
                        println("✅ Regular bullet hit! Enemy ${enemy.id} destroyed")
                        handleCorrectAnswer(enemy)

                        if (!isBossWave || !enemy.isBoss) {
                            enemiesToRemove.add(enemy)
                            gameEngine.addExplosion(enemy.x, enemy.y, ExplosionType.CORRECT)
                            totalEnemiesKilled++
                            checkDifficultyUpgrade()
                        }
                        bulletsToRemove.add(bullet)
                        gameEngine.recordShot(true)
                    } else {
                        println("❌ Regular bullet miss!")
                        handleWrongAnswer()
                        bulletsToRemove.add(bullet)
                        gameEngine.recordShot(false)
                        gameEngine.addExplosion(bullet.x, bullet.y, ExplosionType.WRONG)
                    }
                }
            }
        }

        // REMOVED: Power-up collision detection - no longer needed
        /*
        powerUps.forEach { powerUp ->
            if (isCollidingWithPlayer(powerUp)) {
                // Power-up collision handling removed
            }
        }
        */

        bullets.removeAll(bulletsToRemove)
        enemies.removeAll(enemiesToRemove)
        // REMOVED: powerUps.removeAll(powerUpsToRemove)

        // CRITICAL: Check if we removed the target enemy
        if (enemiesToRemove.any { it.id == targetEnemyId }) {
            println("🎯 Target enemy destroyed in collision, finding new target...")
            findNewTargetEnemy()
        }
    }

    private fun isColliding(bullet: Bullet, enemy: Enemy): Boolean {
        val enemyWidth = if (enemy.isBoss) 140f else 120f
        val enemyHeight = if (enemy.isBoss) 80f else 60f

        val enemyLeft = enemy.x - enemyWidth / 2
        val enemyRight = enemy.x + enemyWidth / 2
        val enemyTop = enemy.y - enemyHeight / 2
        val enemyBottom = enemy.y + enemyHeight / 2

        val bulletRadius = 8f
        val bulletLeft = bullet.x - bulletRadius
        val bulletRight = bullet.x + bulletRadius
        val bulletTop = bullet.y - bulletRadius
        val bulletBottom = bullet.y + bulletRadius

        return bulletRight >= enemyLeft &&
                bulletLeft <= enemyRight &&
                bulletBottom >= enemyTop &&
                bulletTop <= enemyBottom
    }

    private fun isCollidingWithPlayer(powerUp: PowerUp): Boolean {
        val playerSize = 30f
        val playerLeft = playerX - playerSize
        val playerRight = playerX + playerSize
        val playerTop = playerY - playerSize
        val playerBottom = playerY + playerSize

        val powerUpRadius = 20f
        val powerUpLeft = powerUp.x - powerUpRadius
        val powerUpRight = powerUp.x + powerUpRadius
        val powerUpTop = powerUp.y - powerUpRadius
        val powerUpBottom = powerUp.y + powerUpRadius

        return powerUpRight >= playerLeft &&
                powerUpLeft <= playerRight &&
                powerUpBottom >= playerTop &&
                powerUpTop <= playerBottom
    }

    private fun handleCorrectAnswer(enemy: Enemy) {
        soundManager?.playHitSound()

        val basePoints = 10 * wave
        val difficultyBonus = currentDifficultyLevel * 5
        val multiplier = comboMultiplier * (if (gameEngine.isPowerUpActive(PowerUpType.DOUBLE_POINTS)) 2 else 1)
        val points = ((basePoints + difficultyBonus) * multiplier).toInt()
        score += points
        combo++
        comboMultiplier = 1.0f + (combo * 0.1f)

        println("✅ Correct answer processed for enemy ${enemy.id}")

        // CRITICAL: Clear selection state immediately
        selectedSolutionIndex = -1
        selectedAnswer = 0
        hasValidSelection = false

        // Handle boss battle
        if (isBossWave && enemy.isBoss) {
            val bossDefeated = gameEngine.handleBossHit()
            if (bossDefeated) {
                isBossWave = false
                bossEnemy = null
                score += wave * 100
                println("🏆 Boss defeated! Finding new target...")
                targetEnemyId = -1
                findNewTargetEnemy()
            } else {
                val newEquation = gameEngine.getCurrentBossEquation()
                if (newEquation != null) {
                    enemy.equation = newEquation.first
                    enemy.answer = newEquation.second
                    enemy.id = nextEnemyId++
                    println("🔄 Boss equation updated, regenerating boxes...")
                    targetEnemyId = -1  // Clear current target
                    generateSolutionBoxesForEnemy(enemy)  // Generate for updated boss
                }
            }
        } else {
            // Normal enemy defeated - clear target and find new one
            println("🔄 Normal enemy defeated, clearing target and finding new enemy...")
            targetEnemyId = -1
            findNewTargetEnemy()
        }

        if (gameEngine.isPowerUpActive(PowerUpType.AUTO_SOLVE)) {
            gameEngine.consumeAutoSolve()
        }

        // FORCE UI update to clear selection
        Handler(Looper.getMainLooper()).post {
            (context as MainActivity).updateSolutionBoxSelection(-1)
        }
    }

    private fun handleWrongAnswer() {
        soundManager?.playMissSound()
        combo = 0
        comboMultiplier = 1.0f

        Handler(Looper.getMainLooper()).post {
            if (selectedSolutionIndex >= 0) {
                (context as MainActivity).updateSolutionBoxSelection(selectedSolutionIndex)
            }
        }
    }

    private fun getDifficultyFromKills(): Int {
        return when (totalEnemiesKilled) {
            in 0..49 -> 1
            in 50..99 -> 2
            in 100..149 -> 3
            in 150..199 -> 4
            in 200..249 -> 5
            in 250..299 -> 6
            in 300..349 -> 7
            in 350..399 -> 8
            in 400..449 -> 9
            else -> 10
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

        difficultyUpgradeMessage = levelInfo.first
        difficultyUpgradeDetail = levelInfo.second
        difficultyUpgradeTimer = 180

        println("🎉 DIFFICULTY UPGRADE! ${levelInfo.first} (${totalEnemiesKilled} enemies defeated)")
    }

    private fun spawnPowerUp() {
        val availablePowerUps = listOf(
            PowerUpType.TIME_FREEZE,
            PowerUpType.AUTO_SOLVE,
            PowerUpType.SHIELD,
            PowerUpType.DOUBLE_POINTS,
            PowerUpType.EXTRA_LIFE
        )

        val powerUp = PowerUp(
            x = Random.nextFloat() * (width - 50f) + 25f,
            y = 50f,
            type = availablePowerUps.random()
        )
        powerUps.add(powerUp)

        println("🎁 Spawned power-up: ${powerUp.type}")
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
                waveText.text = if (gameMode == GameMode.PRACTICE) "Practice" else "Wave: $wave"
                comboText.text = "Combo: ${combo}x"
                difficultyText.text = if (gameMode == GameMode.PRACTICE) {
                    val practiceType = sharedPreferences.getInt("practice_type", 0)
                    val practiceLevel = sharedPreferences.getInt("practice_difficulty", 1)
                    val operationName = when (practiceType) {
                        0 -> "Add"
                        1 -> "Sub"
                        2 -> "Mul"
                        3 -> "Div"
                        4 -> "Mix"
                        else -> "Math"
                    }
                    val levelName = when (practiceLevel) {
                        1 -> "Easy"
                        2 -> "Med"
                        3 -> "Hard"
                        4 -> "Expert"
                        else -> "L$practiceLevel"
                    }
                    "$operationName-$levelName"
                } else {
                    "Level: $currentDifficultyLevel"
                }
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
                canvas.drawText("Touch anywhere to restart", width / 2f, height / 2f + 50, restartPaint)
            }
            else -> drawCenteredText(canvas, "Math Shooter", height / 2f)
        }
    }

    private fun drawGame(canvas: Canvas) {
        drawBackground(canvas)

        // Draw player at fixed center position
        val playerSize = 30f
        canvas.drawRect(
            playerX - playerSize, playerY - playerSize,
            playerX + playerSize, playerY + playerSize,
            playerPaint
        )

        // Draw cannon barrel pointing up
        canvas.drawRect(
            playerX - 5f, playerY - playerSize - 20f,
            playerX + 5f, playerY - playerSize,
            playerPaint
        )

        // Draw enemies
        drawEnemies(canvas)

        // Draw regular bullets
        bullets.forEach { bullet ->
            val bulletSize = 8f
            val bulletColor = if (gameEngine.isPowerUpActive(PowerUpType.AUTO_SOLVE)) {
                Paint().apply {
                    color = Color.GREEN
                    style = Paint.Style.FILL
                }
            } else bulletPaint

            canvas.drawCircle(bullet.x, bullet.y, bulletSize, bulletColor)

            canvas.drawText(
                bullet.value.toString(),
                bullet.x,
                bullet.y - 15f,
                equationPaint
            )
        }

        // Draw targeted bullets
        drawTargetedBullets(canvas)

        // Draw power-ups
        powerUps.forEach { powerUp ->
            try {
                val powerUpPaint = Paint().apply {
                    color = when (powerUp.type) {
                        PowerUpType.TIME_FREEZE -> Color.BLUE
                        PowerUpType.AUTO_SOLVE -> Color.GREEN
                        PowerUpType.SHIELD -> Color.MAGENTA
                        PowerUpType.DOUBLE_POINTS -> Color.YELLOW
                        PowerUpType.EXTRA_LIFE -> Color.RED
                    }
                    style = Paint.Style.FILL
                }

                canvas.drawCircle(powerUp.x, powerUp.y, 20f, powerUpPaint)

                val symbol = when (powerUp.type) {
                    PowerUpType.TIME_FREEZE -> "⏰"
                    PowerUpType.AUTO_SOLVE -> "🤖"
                    PowerUpType.SHIELD -> "🛡️"
                    PowerUpType.DOUBLE_POINTS -> "2X"
                    PowerUpType.EXTRA_LIFE -> "💚"
                }

                val symbolPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 16f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = Typeface.DEFAULT_BOLD
                }

                canvas.drawText(symbol, powerUp.x, powerUp.y + 5f, symbolPaint)
            } catch (e: Exception) {
                println("❌ Error drawing power-up: ${e.message}")
            }
        }

        gameEngine.drawEffects(canvas)

        // Enhanced instruction display
        if (hasValidSelection) {
            val displayPaint = Paint().apply {
                color = Color.GREEN
                textSize = 24f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(
                "Firing: $selectedAnswer ✓",
                width / 2f,
                height - 50f,
                displayPaint
            )
        } else {
            val displayPaint = Paint().apply {
                color = Color.YELLOW
                textSize = 20f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(
                "Select an answer to auto-fire!",
                width / 2f,
                height - 50f,
                displayPaint
            )
        }

        // Practice mode UI
        if (gameMode == GameMode.PRACTICE) {
            val practiceInfoPaint = Paint().apply {
                color = Color.parseColor("#4CAF50")
                textSize = 20f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
                setShadowLayer(2f, 0f, 0f, Color.BLACK)
            }

            val practiceType = sharedPreferences.getInt("practice_type", 0)
            val practiceLevel = sharedPreferences.getInt("practice_difficulty", 1)

            val operationName = when (practiceType) {
                0 -> "Addition Practice"
                1 -> "Subtraction Practice"
                2 -> "Multiplication Practice"
                3 -> "Division Practice"
                4 -> "Mixed Operations Practice"
                else -> "Math Practice"
            }

            val levelName = when (practiceLevel) {
                1 -> "Easy Level"
                2 -> "Medium Level"
                3 -> "Hard Level"
                4 -> "Expert Level"
                else -> "Level $practiceLevel"
            }

            canvas.drawText("$operationName - $levelName", width / 2f, 50f, practiceInfoPaint)
        }

        // Debug info for target enemy
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

        // Power-up status display
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
            val autoSolvePaint = Paint().apply {
                color = Color.GREEN
                textSize = 32f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("🤖 AUTO-SOLVE: $autoSolveCount", width / 2f, statusY, autoSolvePaint)
            statusY += 50f

            val instructionPaint = Paint().apply {
                color = Color.YELLOW
                textSize = 20f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Automatic targeting active!", width / 2f, statusY, instructionPaint)
        }

        // Difficulty upgrade notification
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

        // Difficulty and kill count info
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

    private fun drawTargetedBullets(canvas: Canvas) {
        targetedBullets.forEach { bullet ->
            val bulletSize = 10f
            val bulletColor = if (gameEngine.isPowerUpActive(PowerUpType.AUTO_SOLVE)) {
                Paint().apply {
                    color = Color.GREEN
                    style = Paint.Style.FILL
                }
            } else Paint().apply {
                color = Color.CYAN
                style = Paint.Style.FILL
            }

            canvas.drawCircle(bullet.x, bullet.y, bulletSize, bulletColor)

            canvas.drawText(
                bullet.value.toString(),
                bullet.x,
                bullet.y - 15f,
                equationPaint
            )

            // REMOVED: Targeting line - no longer drawing the line
            // if (bullet.isActive) {
            //     val linePaint = Paint().apply {
            //         color = Color.CYAN
            //         alpha = 100
            //         strokeWidth = 2f
            //     }
            //     canvas.drawLine(bullet.x, bullet.y, bullet.targetX, bullet.targetY, linePaint)
            // }
        }
    }

    private fun drawEnemies(canvas: Canvas) {
        enemies.forEach { enemy ->
            drawNeonEnemy(canvas, enemy)
        }
    }

    private fun drawNeonEnemy(canvas: Canvas, enemy: Enemy) {
        val enemyWidth = if (enemy.isBoss) 140f else 120f
        val enemyHeight = if (enemy.isBoss) 80f else 60f
        val cornerRadius = 12f

        val left = enemy.x - enemyWidth / 2
        val top = enemy.y - enemyHeight / 2
        val right = enemy.x + enemyWidth / 2
        val bottom = enemy.y + enemyHeight / 2
        val rect = RectF(left, top, right, bottom)

        val (fillColor, glowColor) = when (currentDifficultyLevel) {
            1 -> Pair("#800000", "#FF0000")
            2 -> Pair("#804000", "#FF8000")
            3 -> Pair("#660000", "#FF3300")
            4 -> Pair("#660033", "#CC0066")
            5 -> Pair("#4D0066", "#9900CC")
            6 -> Pair("#330066", "#6600FF")
            7 -> Pair("#003366", "#0066FF")
            8 -> Pair("#006666", "#00CCFF")
            9 -> Pair("#004D00", "#00FF99")
            else -> Pair("#666600", "#FFFF00")
        }

        val (finalFillColor, finalGlowColor) = if (enemy.isBoss) {
            Pair("#660066", "#FF00FF")
        } else {
            Pair(fillColor, glowColor)
        }

        drawNeonGlowLayers(canvas, rect, cornerRadius, finalGlowColor)
        drawEnemyBody(canvas, rect, cornerRadius, finalFillColor, finalGlowColor)
        drawEnemyEquation(canvas, enemy, rect)

        if (enemy.isBoss) {
            drawBossHealthBar(canvas, enemy, rect)
        }
    }

    private fun drawNeonGlowLayers(canvas: Canvas, rect: RectF, cornerRadius: Float, glowColor: String) {
        val glowPaint = Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        glowPaint.apply {
            color = Color.parseColor(glowColor)
            alpha = 30
            strokeWidth = 8f
            maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(
            RectF(rect.left - 4, rect.top - 4, rect.right + 4, rect.bottom + 4),
            cornerRadius + 4, cornerRadius + 4, glowPaint
        )

        glowPaint.apply {
            alpha = 60
            strokeWidth = 4f
            maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(
            RectF(rect.left - 2, rect.top - 2, rect.right + 2, rect.bottom + 2),
            cornerRadius + 2, cornerRadius + 2, glowPaint
        )

        glowPaint.apply {
            alpha = 120
            strokeWidth = 2f
            maskFilter = BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)
    }

    private fun drawEnemyBody(canvas: Canvas, rect: RectF, cornerRadius: Float, fillColor: String, borderColor: String) {
        val bodyPaint = Paint().apply {
            color = Color.parseColor(fillColor)
            style = Paint.Style.FILL
            isAntiAlias = true
            alpha = 180
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bodyPaint)

        val borderPaint = Paint().apply {
            color = Color.parseColor(borderColor)
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
            alpha = 255
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
    }

    private fun drawEnemyEquation(canvas: Canvas, enemy: Enemy, rect: RectF) {
        val equationPaint = Paint().apply {
            color = Color.WHITE
            textSize = if (enemy.isBoss) 28f else 24f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(4f, 0f, 0f, Color.parseColor("#00FFFF"))
        }

        val textY = rect.centerY() + (equationPaint.textSize / 3)
        canvas.drawText(enemy.equation, rect.centerX(), textY, equationPaint)
    }

    private fun drawBossHealthBar(canvas: Canvas, enemy: Enemy, rect: RectF) {
        val healthPercent = gameEngine.getBossHealthPercentage()
        val barWidth = rect.width() - 20f
        val barHeight = 8f
        val barLeft = rect.left + 10f
        val barTop = rect.top - 25f
        val barRight = barLeft + barWidth
        val barBottom = barTop + barHeight

        val bgPaint = Paint().apply {
            color = Color.parseColor("#333333")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            RectF(barLeft, barTop, barRight, barBottom),
            4f, 4f, bgPaint
        )

        val healthPaint = Paint().apply {
            color = Color.parseColor("#FF0000")
            style = Paint.Style.FILL
            isAntiAlias = true
            setShadowLayer(4f, 0f, 0f, Color.parseColor("#FF6666"))
        }
        canvas.drawRoundRect(
            RectF(barLeft, barTop, barLeft + (barWidth * healthPercent), barBottom),
            4f, 4f, healthPaint
        )

        val borderPaint = Paint().apply {
            color = Color.parseColor("#FF4444")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            RectF(barLeft, barTop, barRight, barBottom),
            4f, 4f, borderPaint
        )
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

// Data class for targeted bullets
data class TargetedBullet(
    var x: Float,
    var y: Float,
    val value: Int,
    val targetX: Float,
    val targetY: Float,
    val targetId: Int,
    var isActive: Boolean = true
)