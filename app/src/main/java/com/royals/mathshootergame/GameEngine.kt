package com.royals.mathshootergame

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.os.Vibrator
import kotlin.math.*
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.*

class GameEngine(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MathShooterPrefs", Context.MODE_PRIVATE)

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    // Game mode
    var gameMode = GameMode.NORMAL

    // Enhanced game statistics
    private var totalShots = 0
    private var totalHits = 0
    private var totalMisses = 0
    private var startTime = 0L
    private var sessionTime = 0L

    // Boss battle system
    private var bossActive = false
    private var bossHealth = 0
    private var bossMaxHealth = 0
    private var bossEquations = mutableListOf<String>()
    private var bossAnswers = mutableListOf<Int>()
    private var currentBossEquationIndex = 0

    // Special effects
    private val explosions = mutableListOf<Explosion>()
    private val particles = mutableListOf<Particle>()

    // Enhanced power-up system
    private val activePowerUps = mutableMapOf<PowerUpType, PowerUpEffect>()

    // Daily challenge system
    private var dailyChallengeEquations = mutableListOf<Pair<String, Int>>()
    private var dailyChallengeIndex = 0

    fun initializeGame(mode: GameMode = GameMode.NORMAL) {
        gameMode = mode
        startTime = System.currentTimeMillis()
        totalShots = 0
        totalHits = 0
        totalMisses = 0

        when (mode) {
            GameMode.DAILY_CHALLENGE -> initializeDailyChallenge()
            GameMode.PRACTICE -> initializePracticeMode()
            else -> { /* Normal mode - no special initialization */ }
        }
    }

    private fun initializeDailyChallenge() {
        // Generate 50 problems for daily challenge
        dailyChallengeEquations.clear()
        val random = Random(getDailySeed()) // Use daily seed for consistency

        repeat(50) { index ->
            val difficulty = when {
                index < 10 -> 1 // Easy
                index < 25 -> 2 // Medium
                index < 40 -> 3 // Hard
                else -> 4 // Expert
            }
            dailyChallengeEquations.add(generateEquationForDifficulty(difficulty, random))
        }
        dailyChallengeIndex = 0
    }

    private fun initializePracticeMode() {
        // Practice mode allows focused practice on specific operations
        val practiceType = sharedPreferences.getInt("practice_type", 0)
        // 0: Addition, 1: Subtraction, 2: Multiplication, 3: Division, 4: Mixed
    }

    // Updated main equation generation method
    fun generateEquation(wave: Int): Pair<String, Int> {
        return when (gameMode) {
            GameMode.DAILY_CHALLENGE -> {
                if (dailyChallengeIndex < dailyChallengeEquations.size) {
                    dailyChallengeEquations[dailyChallengeIndex++]
                } else {
                    generateStandardEquation(wave)
                }
            }
            GameMode.PRACTICE -> generatePracticeEquation() // Use practice-specific generation
            else -> generateStandardEquation(wave)
        }
    }

    private fun generateStandardEquation(wave: Int): Pair<String, Int> {
        val adaptiveDifficulty = sharedPreferences.getBoolean("adaptive_difficulty", true)
        val accuracy = if (totalShots > 0) totalHits.toFloat() / totalShots else 1f

        val adjustedWave = if (adaptiveDifficulty) {
            when {
                accuracy > 0.9f && wave > 1 -> wave + 1 // Increase difficulty
                accuracy < 0.5f && wave > 1 -> max(1, wave - 1) // Decrease difficulty
                else -> wave
            }
        } else {
            wave
        }

        return generateEquationForDifficulty(min(adjustedWave, 10))
    }

    fun generatePracticeEquation(): Pair<String, Int> {
        val practiceType = sharedPreferences.getInt("practice_type", 0)
        val difficulty = sharedPreferences.getInt("practice_difficulty", 1)

        return when (practiceType) {
            0 -> generateAdditionEquation(difficulty)      // Addition
            1 -> generateSubtractionEquation(difficulty)   // Subtraction
            2 -> generateMultiplicationEquation(difficulty) // Multiplication
            3 -> generateDivisionEquation(difficulty)      // Division
            4 -> generateMixedEquation(difficulty)         // Mixed Operations
            else -> generateAdditionEquation(difficulty)   // Default to addition
        }
    }

    fun generateEquationForDifficulty(difficulty: Int, random: Random = Random): Pair<String, Int> {
        return when (difficulty) {
            1 -> { // Basic addition/subtraction
                val a = random.nextInt(1, 20)
                val b = random.nextInt(1, 20)
                if (random.nextBoolean()) {
                    Pair("$a + $b", a + b)
                } else {
                    val larger = max(a, b)
                    val smaller = min(a, b)
                    Pair("$larger - $smaller", larger - smaller)
                }
            }
            2 -> { // Add simple multiplication
                when (random.nextInt(3)) {
                    0 -> generateAdditionEquation(2)
                    1 -> generateSubtractionEquation(2)
                    else -> generateMultiplicationEquation(1)
                }
            }
            3 -> { // Add division
                when (random.nextInt(4)) {
                    0 -> generateAdditionEquation(2)
                    1 -> generateSubtractionEquation(2)
                    2 -> generateMultiplicationEquation(2)
                    else -> generateDivisionEquation(1)
                }
            }
            4 -> { // Two-step equations
                val a = random.nextInt(2, 15)
                val b = random.nextInt(2, 10)
                val c = random.nextInt(1, 8)
                when (random.nextInt(4)) {
                    0 -> Pair("$a + $b × $c", a + (b * c))
                    1 -> Pair("$a × $b - $c", (a * b) - c)
                    2 -> Pair("($a + $b) × $c", (a + b) * c)
                    else -> {
                        val product = b * c
                        Pair("$a + $product ÷ $b", a + c)
                    }
                }
            }
            5 -> { // Fractions and decimals
                when (random.nextInt(3)) {
                    0 -> {
                        val whole = random.nextInt(1, 10)
                        val numerator = random.nextInt(1, 4)
                        val denominator = random.nextInt(2, 5)
                        val result = (whole * denominator + numerator) * 10 / denominator
                        Pair("$whole $numerator/$denominator × 10", result)
                    }
                    1 -> {
                        val a = random.nextInt(10, 100)
                        val b = random.nextInt(10, 100)
                        Pair("$a + $b", a + b)
                    }
                    else -> generateEquationForDifficulty(4, random)
                }
            }
            6 -> { // Exponents
                when (random.nextInt(3)) {
                    0 -> {
                        val base = random.nextInt(2, 8)
                        val exp = random.nextInt(2, 4)
                        val result = when (exp) {
                            2 -> base * base
                            3 -> base * base * base
                            else -> base.toDouble().pow(exp.toDouble()).toInt()
                        }
                        when (exp) {
                            2 -> Pair("$base²", result)
                            3 -> Pair("$base³", result)
                            else -> Pair("$base^$exp", result)
                        }
                    }
                    1 -> {
                        val a = random.nextInt(1, 15)
                        val square = a * a  // More reliable than pow for squares
                        Pair("√$square", a)
                    }
                    else -> generateEquationForDifficulty(5, random)
                }
            }
            7 -> { // Negative numbers
                when (random.nextInt(4)) {
                    0 -> {
                        val a = random.nextInt(5, 20)
                        val b = random.nextInt(1, a)
                        Pair("$b - $a", b - a)
                    }
                    1 -> {
                        val a = random.nextInt(-10, -1)
                        val b = random.nextInt(1, 15)
                        Pair("$a + $b", a + b)
                    }
                    2 -> {
                        val a = random.nextInt(-8, -2)
                        val b = random.nextInt(2, 6)
                        Pair("$a × $b", a * b)
                    }
                    else -> generateEquationForDifficulty(6, random)
                }
            }
            8 -> { // Complex multi-step
                when (random.nextInt(4)) {
                    0 -> {
                        val a = random.nextInt(2, 12)
                        val b = random.nextInt(2, 8)
                        val c = random.nextInt(1, 6)
                        val d = random.nextInt(1, 5)
                        Pair("$a × $b + $c × $d", (a * b) + (c * d))
                    }
                    1 -> {
                        val a = random.nextInt(20, 100)
                        val b = random.nextInt(2, 15)
                        val c = random.nextInt(1, 10)
                        Pair("$a ÷ $b + $c", (a / b) + c)
                    }
                    2 -> {
                        val a = random.nextInt(2, 8)
                        val b = random.nextInt(1, 6)
                        val c = random.nextInt(1, 5)
                        val square = a * a
                        Pair("$square ÷ $a + $b × $c", a + (b * c))
                    }
                    else -> generateEquationForDifficulty(7, random)
                }
            }
            9 -> { // Advanced operations
                when (random.nextInt(5)) {
                    0 -> {
                        val a = random.nextInt(100, 1000)
                        val b = random.nextInt(10, 100)
                        Pair("$a + $b", a + b)
                    }
                    1 -> {
                        val a = random.nextInt(3, 12)
                        val result = a * a * a  // More reliable calculation
                        Pair("$a³", result)
                    }
                    2 -> {
                        val a = random.nextInt(5, 25)
                        val b = random.nextInt(2, 8)
                        val c = random.nextInt(10, 50)
                        Pair("$a × $b - $c", (a * b) - c)
                    }
                    3 -> {
                        val percent = random.nextInt(10, 90)
                        val value = random.nextInt(100, 500)
                        val result = (value * percent) / 100
                        Pair("$percent% of $value", result)
                    }
                    else -> generateEquationForDifficulty(8, random)
                }
            }
            else -> { // Expert level (10+)
                when (random.nextInt(6)) {
                    0 -> {
                        val a = random.nextInt(12, 25)
                        val b = random.nextInt(8, 20)
                        val c = random.nextInt(3, 12)
                        val d = random.nextInt(2, 8)
                        Pair("($a + $b) × $c ÷ $d", ((a + b) * c) / d)
                    }
                    1 -> {
                        val a = random.nextInt(10, 30)
                        val b = random.nextInt(5, 15)
                        val square = a * a
                        Pair("$square ÷ $a + $b²", a + (b * b))
                    }
                    2 -> {
                        val a = random.nextInt(2, 8)
                        val b = random.nextInt(2, 4)
                        val c = random.nextInt(10, 50)
                        val powerResult = when (b) {
                            2 -> a * a
                            3 -> a * a * a
                            else -> a.toDouble().pow(b.toDouble()).toInt()
                        }
                        val result = powerResult + c
                        when (b) {
                            2 -> Pair("$a² + $c", result)
                            3 -> Pair("$a³ + $c", result)
                            else -> Pair("$a^$b + $c", result)
                        }
                    }
                    3 -> {
                        // Square root problems
                        val base = random.nextInt(2, 15)
                        val square = base * base
                        Pair("√$square", base)
                    }
                    4 -> {
                        // Fibonacci sequence
                        val n = random.nextInt(6, 12)
                        val fib = generateFibonacci(n)
                        Pair("F($n)", fib)
                    }
                    else -> {
                        val a = random.nextInt(100, 999)
                        val b = random.nextInt(100, 999)
                        val c = random.nextInt(10, 99)
                        Pair("$a + $b - $c", a + b - c)
                    }
                }
            }
        }
    }

    // Enhanced Addition Equations with proper difficulty levels
    private fun generateAdditionEquation(difficulty: Int): Pair<String, Int> {
        return when (difficulty) {
            1 -> { // Easy - 2 digit numbers
                val a = Random.nextInt(10, 99)  // 10-99
                val b = Random.nextInt(10, 99)  // 10-99
                Pair("$a + $b", a + b)
            }
            2 -> { // Medium - 3 digit numbers
                val a = Random.nextInt(100, 999)  // 100-999
                val b = Random.nextInt(100, 999)  // 100-999
                Pair("$a + $b", a + b)
            }
            3 -> { // Hard - 4 digit numbers
                val a = Random.nextInt(1000, 9999)  // 1000-9999
                val b = Random.nextInt(1000, 9999)  // 1000-9999
                Pair("$a + $b", a + b)
            }
            4 -> { // Expert - 5 digit numbers
                val a = Random.nextInt(10000, 99999)  // 10000-99999
                val b = Random.nextInt(10000, 99999)  // 10000-99999
                Pair("$a + $b", a + b)
            }
            else -> { // Default to easy
                val a = Random.nextInt(10, 99)
                val b = Random.nextInt(10, 99)
                Pair("$a + $b", a + b)
            }
        }
    }

    // Enhanced Subtraction Equations with proper difficulty levels
    private fun generateSubtractionEquation(difficulty: Int): Pair<String, Int> {
        return when (difficulty) {
            1 -> { // Easy - 2 digit numbers
                val a = Random.nextInt(20, 99)  // Larger number
                val b = Random.nextInt(10, a)   // Smaller number to ensure positive result
                Pair("$a - $b", a - b)
            }
            2 -> { // Medium - 3 digit numbers
                val a = Random.nextInt(200, 999)
                val b = Random.nextInt(100, a)
                Pair("$a - $b", a - b)
            }
            3 -> { // Hard - 4 digit numbers
                val a = Random.nextInt(2000, 9999)
                val b = Random.nextInt(1000, a)
                Pair("$a - $b", a - b)
            }
            4 -> { // Expert - 5 digit numbers
                val a = Random.nextInt(20000, 99999)
                val b = Random.nextInt(10000, a)
                Pair("$a - $b", a - b)
            }
            else -> {
                val a = Random.nextInt(20, 99)
                val b = Random.nextInt(10, a)
                Pair("$a - $b", a - b)
            }
        }
    }

    // Enhanced Multiplication Equations with proper difficulty levels
    private fun generateMultiplicationEquation(difficulty: Int): Pair<String, Int> {
        return when (difficulty) {
            1 -> { // Easy - Single digit × 2 digit
                val a = Random.nextInt(2, 9)     // 2-9
                val b = Random.nextInt(10, 99)   // 10-99
                Pair("$a × $b", a * b)
            }
            2 -> { // Medium - 2 digit × 2 digit
                val a = Random.nextInt(10, 99)   // 10-99
                val b = Random.nextInt(10, 99)   // 10-99
                Pair("$a × $b", a * b)
            }
            3 -> { // Hard - 2 digit × 3 digit
                val a = Random.nextInt(10, 99)   // 10-99
                val b = Random.nextInt(100, 999) // 100-999
                Pair("$a × $b", a * b)
            }
            4 -> { // Expert - 3 digit × 3 digit
                val a = Random.nextInt(100, 999) // 100-999
                val b = Random.nextInt(100, 999) // 100-999
                Pair("$a × $b", a * b)
            }
            else -> {
                val a = Random.nextInt(2, 9)
                val b = Random.nextInt(10, 99)
                Pair("$a × $b", a * b)
            }
        }
    }

    // Enhanced Division Equations with proper difficulty levels
    private fun generateDivisionEquation(difficulty: Int): Pair<String, Int> {
        return when (difficulty) {
            1 -> { // Easy - 2 digit ÷ 1 digit
                val divisor = Random.nextInt(2, 9)          // 2-9
                val quotient = Random.nextInt(10, 99)       // 10-99
                val dividend = divisor * quotient
                Pair("$dividend ÷ $divisor", quotient)
            }
            2 -> { // Medium - 3 digit ÷ 1 digit
                val divisor = Random.nextInt(2, 9)          // 2-9
                val quotient = Random.nextInt(100, 999)     // 100-999
                val dividend = divisor * quotient
                Pair("$dividend ÷ $divisor", quotient)
            }
            3 -> { // Hard - 3 digit ÷ 2 digit
                val divisor = Random.nextInt(10, 99)        // 10-99
                val quotient = Random.nextInt(10, 99)       // 10-99
                val dividend = divisor * quotient
                Pair("$dividend ÷ $divisor", quotient)
            }
            4 -> { // Expert - 4 digit ÷ 2 digit
                val divisor = Random.nextInt(10, 99)        // 10-99
                val quotient = Random.nextInt(100, 999)     // 100-999
                val dividend = divisor * quotient
                Pair("$dividend ÷ $divisor", quotient)
            }
            else -> {
                val divisor = Random.nextInt(2, 9)
                val quotient = Random.nextInt(10, 99)
                val dividend = divisor * quotient
                Pair("$dividend ÷ $divisor", quotient)
            }
        }
    }

    // Mixed Operations based on difficulty
    private fun generateMixedEquation(difficulty: Int): Pair<String, Int> {
        val operationType = Random.nextInt(0, 4) // 0=+, 1=-, 2=×, 3=÷

        return when (operationType) {
            0 -> generateAdditionEquation(difficulty)
            1 -> generateSubtractionEquation(difficulty)
            2 -> generateMultiplicationEquation(difficulty)
            3 -> generateDivisionEquation(difficulty)
            else -> generateAdditionEquation(difficulty)
        }
    }

    fun initializeBoss(wave: Int): Enemy {
        bossActive = true
        bossMaxHealth = wave * 3
        bossHealth = bossMaxHealth

        // Generate multiple equations for boss
        bossEquations.clear()
        bossAnswers.clear()
        currentBossEquationIndex = 0

        repeat(bossMaxHealth) {
            val equation = generateEquationForDifficulty(min(wave + 2, 8))
            bossEquations.add(equation.first)
            bossAnswers.add(equation.second)
        }

        return Enemy(
            x = 400f, // Center of screen
            y = 100f,
            equation = bossEquations[0],
            answer = bossAnswers[0],
            speed = 0.5f,
            isAlive = true
        ).apply {
            // Mark as boss
            // You could add a boss flag to Enemy class
        }
    }

    fun handleBossHit(): Boolean {
        bossHealth--
        currentBossEquationIndex++

        if (bossHealth <= 0) {
            bossActive = false
            return true // Boss defeated
        }

        return false // Boss still alive
    }

    fun getBossHealthPercentage(): Float {
        return if (bossMaxHealth > 0) bossHealth.toFloat() / bossMaxHealth else 0f
    }

    fun getCurrentBossEquation(): Pair<String, Int>? {
        return if (bossActive && currentBossEquationIndex < bossEquations.size) {
            Pair(bossEquations[currentBossEquationIndex], bossAnswers[currentBossEquationIndex])
        } else null
    }

    fun recordShot(isHit: Boolean) {
        totalShots++
        if (isHit) {
            totalHits++
        } else {
            totalMisses++
        }

        // Vibration feedback
        if (sharedPreferences.getBoolean("vibration_enabled", true)) {
            if (isHit) {
                vibrator.vibrate(50) // Short vibration for hit
            } else {
                vibrator.vibrate(longArrayOf(0, 100, 50, 100), -1) // Pattern for miss
            }
        }
    }

    fun getAccuracy(): Float {
        return if (totalShots > 0) totalHits.toFloat() / totalShots else 0f
    }

    fun saveHighScore(score: Int, wave: Int) {
        val currentHighScore = sharedPreferences.getInt("high_score", 0)

        if (score > currentHighScore) {
            sharedPreferences.edit().putInt("high_score", score).apply()
        }

        // Save detailed score entry
        saveDetailedScore(score, wave, getAccuracy())
    }

    private fun saveDetailedScore(score: Int, wave: Int, accuracy: Float) {
        val scores = mutableListOf<ScoreEntry>()

        // Load existing scores
        for (i in 1..10) {
            val existingScore = sharedPreferences.getInt("score_$i", 0)
            val existingWave = sharedPreferences.getInt("wave_$i", 0)
            val existingDate = sharedPreferences.getString("date_$i", "") ?: ""
            val existingAccuracy = sharedPreferences.getFloat("accuracy_$i", 0f)

            if (existingScore > 0) {
                scores.add(ScoreEntry(existingScore, existingWave, existingDate, existingAccuracy))
            }
        }

        // Add new score
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        scores.add(ScoreEntry(score, wave, currentDate, accuracy))

        // Sort by score (descending) and keep top 10
        scores.sortByDescending { it.score }
        val topScores = scores.take(10)

        // Save top scores
        val editor = sharedPreferences.edit()
        topScores.forEachIndexed { index, scoreEntry ->
            val position = index + 1
            editor.putInt("score_$position", scoreEntry.score)
            editor.putInt("wave_$position", scoreEntry.wave)
            editor.putString("date_$position", scoreEntry.date)
            editor.putFloat("accuracy_$position", scoreEntry.accuracy)
        }

        // Clear any remaining slots
        for (i in (topScores.size + 1)..10) {
            editor.remove("score_$i")
            editor.remove("wave_$i")
            editor.remove("date_$i")
            editor.remove("accuracy_$i")
        }

        editor.apply()
    }

    fun addExplosion(x: Float, y: Float, type: ExplosionType = ExplosionType.NORMAL) {
        if (sharedPreferences.getBoolean("show_particles", true)) {
            explosions.add(Explosion(x, y, type))

            // Add particle effects
            repeat(if (type == ExplosionType.BOSS) 20 else 10) {
                particles.add(
                    Particle(
                        x = x + Random.nextFloat() * 20 - 10,
                        y = y + Random.nextFloat() * 20 - 10,
                        velocityX = Random.nextFloat() * 6 - 3,
                        velocityY = Random.nextFloat() * 6 - 3,
                        color = when (type) {
                            ExplosionType.CORRECT -> Color.GREEN
                            ExplosionType.WRONG -> Color.RED
                            ExplosionType.BOSS -> Color.MAGENTA
                            else -> Color.YELLOW
                        },
                        life = 60
                    )
                )
            }
        }
    }

    fun updateEffects() {
        // Update explosions
        explosions.removeAll { explosion ->
            explosion.age++
            explosion.age > explosion.maxAge
        }

        // Update particles
        particles.removeAll { particle ->
            particle.x += particle.velocityX
            particle.y += particle.velocityY
            particle.velocityY += 0.1f // Gravity
            particle.life--
            particle.alpha = (particle.life / 60f).coerceIn(0f, 1f)
            particle.life <= 0
        }
    }

    fun drawEffects(canvas: Canvas) {
        if (!sharedPreferences.getBoolean("show_particles", true)) return

        // Draw explosions
        explosions.forEach { explosion ->
            val progress = explosion.age.toFloat() / explosion.maxAge
            val radius = explosion.maxRadius * sin(progress * PI).toFloat()
            val alpha = (255 * (1 - progress)).toInt().coerceIn(0, 255)

            val paint = Paint().apply {
                color = explosion.color
                this.alpha = alpha
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }

            canvas.drawCircle(explosion.x, explosion.y, radius, paint)
        }

        // Draw particles
        particles.forEach { particle ->
            val paint = Paint().apply {
                color = particle.color
                alpha = (255 * particle.alpha).toInt().coerceIn(0, 255)
                style = Paint.Style.FILL
            }

            canvas.drawCircle(particle.x, particle.y, 3f, paint)
        }
    }

    fun activatePowerUp(type: PowerUpType) {
        val currentTime = System.currentTimeMillis()

        when (type) {
            PowerUpType.TIME_FREEZE -> {
                activePowerUps[type] = PowerUpEffect(currentTime + 5000, null)
            }
            PowerUpType.AUTO_SOLVE -> {
                val current = activePowerUps[type]?.data as? Int ?: 0
                activePowerUps[type] = PowerUpEffect(Long.MAX_VALUE, current + 3)
            }
            PowerUpType.SHIELD -> {
                activePowerUps[type] = PowerUpEffect(currentTime + 15000, 1)
            }
            PowerUpType.DOUBLE_POINTS -> {
                activePowerUps[type] = PowerUpEffect(currentTime + 10000, null)
            }
            PowerUpType.EXTRA_LIFE -> {
                // FIXED: Handle EXTRA_LIFE power-up properly
                // This should add an extra life to the player
                activePowerUps[type] = PowerUpEffect(currentTime + 1000, 1) // Short duration, immediate effect
            }
        }
    }

    fun isPowerUpActive(type: PowerUpType): Boolean {
        val effect = activePowerUps[type] ?: return false
        val currentTime = System.currentTimeMillis()

        return when (type) {
            PowerUpType.AUTO_SOLVE -> (effect.data as? Int ?: 0) > 0
            PowerUpType.EXTRA_LIFE -> false // EXTRA_LIFE is consumed immediately, never "active"
            else -> currentTime < effect.endTime
        }
    }

    fun consumeAutoSolve(): Boolean {
        val effect = activePowerUps[PowerUpType.AUTO_SOLVE]
        if (effect != null) {
            val remaining = (effect.data as? Int ?: 0) - 1
            if (remaining > 0) {
                activePowerUps[PowerUpType.AUTO_SOLVE] = PowerUpEffect(Long.MAX_VALUE, remaining)
            } else {
                activePowerUps.remove(PowerUpType.AUTO_SOLVE)
            }
            return true
        }
        return false
    }

    fun consumeShield(): Boolean {
        val effect = activePowerUps[PowerUpType.SHIELD]
        if (effect != null) {
            activePowerUps.remove(PowerUpType.SHIELD)
            return true
        }
        return false
    }

    // FIXED: Add method to consume extra life and return if successful
    fun consumeExtraLife(): Boolean {
        val effect = activePowerUps[PowerUpType.EXTRA_LIFE]
        if (effect != null) {
            activePowerUps.remove(PowerUpType.EXTRA_LIFE)
            return true
        }
        return false
    }

    fun updatePowerUps() {
        val currentTime = System.currentTimeMillis()
        val expiredPowerUps = mutableListOf<PowerUpType>()

        activePowerUps.forEach { (type, effect) ->
            if (type != PowerUpType.AUTO_SOLVE && currentTime >= effect.endTime) {
                expiredPowerUps.add(type)
            }
        }

        expiredPowerUps.forEach { activePowerUps.remove(it) }
    }

    fun getActivePowerUpCount(type: PowerUpType): Int {
        return when (type) {
            PowerUpType.AUTO_SOLVE -> activePowerUps[type]?.data as? Int ?: 0
            PowerUpType.SHIELD -> if (isPowerUpActive(type)) 1 else 0
            PowerUpType.EXTRA_LIFE -> if (activePowerUps.containsKey(type)) 1 else 0
            else -> if (isPowerUpActive(type)) 1 else 0
        }
    }

    fun getRemainingTime(type: PowerUpType): Long {
        val effect = activePowerUps[type] ?: return 0
        return maxOf(0, effect.endTime - System.currentTimeMillis())
    }

    private fun getDailySeed(): Long {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) * 10000L +
                calendar.get(Calendar.DAY_OF_YEAR) * 100L
    }

    private fun generateFibonacci(n: Int): Int {
        return if (n <= 1) n
        else {
            var a = 0
            var b = 1
            for (i in 2..n) {
                val temp = a + b
                a = b
                b = temp
            }
            b
        }
    }

    fun getGameStatistics(): GameStatistics {
        sessionTime = System.currentTimeMillis() - startTime

        return GameStatistics(
            totalShots = totalShots,
            totalHits = totalHits,
            totalMisses = totalMisses,
            accuracy = getAccuracy(),
            sessionTime = sessionTime,
            shotsPerMinute = if (sessionTime > 0) (totalShots * 60000f / sessionTime) else 0f
        )
    }
}

// Data classes for game engine
enum class GameMode {
    NORMAL, PRACTICE, DAILY_CHALLENGE, BOSS_RUSH
}

enum class ExplosionType {
    NORMAL, CORRECT, WRONG, BOSS
}

data class Explosion(
    val x: Float,
    val y: Float,
    val type: ExplosionType,
    var age: Int = 0,
    val maxAge: Int = 30,
    val maxRadius: Float = when (type) {
        ExplosionType.BOSS -> 80f
        ExplosionType.CORRECT -> 40f
        ExplosionType.WRONG -> 25f
        else -> 30f
    },
    val color: Int = when (type) {
        ExplosionType.CORRECT -> Color.GREEN
        ExplosionType.WRONG -> Color.RED
        ExplosionType.BOSS -> Color.MAGENTA
        else -> Color.YELLOW
    }
)

data class Particle(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    val color: Int,
    var life: Int,
    var alpha: Float = 1f
)

data class PowerUpEffect(
    val endTime: Long,
    val data: Any? = null
)

data class ScoreEntry(
    val score: Int,
    val wave: Int,
    val date: String,
    val accuracy: Float
)

data class GameStatistics(
    val totalShots: Int,
    val totalHits: Int,
    val totalMisses: Int,
    val accuracy: Float,
    val sessionTime: Long,
    val shotsPerMinute: Float
)