package com.royals.mathshootergame

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MathQuizActivity : AppCompatActivity() {

    private lateinit var mathType: String
    private lateinit var mathTitle: String
    private var tableNumber: Int = 1
    private var currentQuestionIndex = 0
    private var score = 0
    private var totalQuestions = 10
    private var timePerQuestion = 30000L

    // UI Elements
    private lateinit var timerCircle: ProgressBar
    private lateinit var timerText: TextView
    private lateinit var questionText: TextView
    private lateinit var answerButtons: List<Button>
    private lateinit var timer: CountDownTimer
    private lateinit var scoreDisplay: TextView

    // Quiz Data
    private lateinit var questions: List<QuizQuestion>
    private var currentQuestion: QuizQuestion? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        mathType = intent.getStringExtra("math_type") ?: "multiplication"
        mathTitle = intent.getStringExtra("math_title") ?: "Multiplication"
        tableNumber = intent.getIntExtra("table_number", 1)
        totalQuestions = intent.getIntExtra("total_questions", 30)

        generateQuestions()
        createQuizLayout()
        startQuestion()
    }

    private fun createQuizLayout() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F0F8FF"))
        }

        // Header
        val headerLayout = createQuizHeader()
        mainLayout.addView(headerLayout)

        // Main content area
        val contentLayout = createMainContent()
        mainLayout.addView(contentLayout)

        // Answer buttons
        val answersLayout = createAnswerButtons()
        mainLayout.addView(answersLayout)

        setContentView(mainLayout)
    }

    private fun createQuizHeader(): LinearLayout {
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(getColorForMathType())
            setPadding(16, 24, 16, 24)
        }

        val backButton = Button(this).apply {
            text = "←"
            textSize = 24f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { showExitQuizDialog() }
        }

        val titleText = TextView(this).apply {
            text = "$mathTitle Quiz"
            textSize = 20f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val menuButton = Button(this).apply {
            text = "⋮"
            textSize = 20f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { showQuizMenu() }
        }

        headerLayout.addView(backButton)
        headerLayout.addView(titleText)
        headerLayout.addView(menuButton)

        return headerLayout
    }

    private fun createMainContent(): LinearLayout {
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setPadding(24, 40, 24, 40)
        }

        // TIMER AND SCORE ROW - EXACTLY LIKE YOUR IMAGE
        val timerScoreRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        // TIMER - LEFT SIDE
        val timerContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // TIMER - LEFT SIDE - MATCH SCORE HEIGHT
        timerCircle = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = FrameLayout.LayoutParams(70, 70) // Match score height
            max = 100
            progress = 100
            progressDrawable = createCircularProgressDrawable()
        }

        timerText = TextView(this).apply {
            text = "30"
            textSize = 16f
            setTextColor(getColorForMathType())
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(70, 70) // Match timer circle
        }

        timerContainer.addView(timerCircle)
        timerContainer.addView(timerText)

        // SPACER
        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        }

        // SCORE - RIGHT SIDE - FIXED WIDTH AND TEXT SIZE
        scoreDisplay = TextView(this).apply {
            text = "Score:\n+0"
            textSize = 12f // Smaller text size to fit properly
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(100, 70) // Adjusted size
            setPadding(4, 4, 4, 4) // Reduced padding
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 12f
                setColor(getColorForMathType())
            }
        }

        timerScoreRow.addView(timerContainer)
        timerScoreRow.addView(spacer)
        timerScoreRow.addView(scoreDisplay)

        // QUESTION BOX - BELOW TIMER AND SCORE
        val questionBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                350
            ).apply {
                setMargins(0, 40, 0, 0)
            }
            setPadding(40, 50, 40, 50)
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(Color.WHITE)
                setStroke(3, getColorForMathType())
            }
        }

        questionText = TextView(this).apply {
            text = "7 × 12"
            textSize = 48f
            setTextColor(Color.parseColor("#333333"))
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
        }

        questionBox.addView(questionText)
        contentLayout.addView(timerScoreRow)
        contentLayout.addView(questionBox)

        return contentLayout
    }

    private fun createCircularProgressDrawable(): android.graphics.drawable.Drawable {
        val backgroundCircle = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(Color.parseColor("#E8E8E8"))
            setStroke(6, Color.parseColor("#DDDDDD"))
        }

        val progressCircle = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(Color.TRANSPARENT)
            setStroke(6, getColorForMathType())
        }

        return android.graphics.drawable.LayerDrawable(arrayOf(backgroundCircle, progressCircle))
    }

    private fun createAnswerButtons(): LinearLayout {
        val answersLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(24, 20, 24, 40)
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0)
            }
        }

        answerButtons = listOf(
            createAnswerButton("75", 0),
            createAnswerButton("96", 1),
            createAnswerButton("84", 2),
            createAnswerButton("81", 3)
        )

        topRow.addView(answerButtons[0])
        topRow.addView(answerButtons[1])
        bottomRow.addView(answerButtons[2])
        bottomRow.addView(answerButtons[3])

        answersLayout.addView(topRow)
        answersLayout.addView(bottomRow)

        return answersLayout
    }

    private fun createAnswerButton(text: String, index: Int): Button {
        return Button(this).apply {
            this.text = text
            textSize = 24f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, 120, 1f).apply {
                setMargins(8, 8, 8, 8)
            }
            setPadding(16, 16, 16, 16)
            includeFontPadding = false
            elevation = 4f
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(getColorForMathType())
            }
            setOnClickListener {
                handleAnswer(index, text.toIntOrNull() ?: 0)
            }
        }
    }

    private fun generateQuestions() {
        questions = (1..totalQuestions).map {
            when (mathType) {
                "multiplication" -> {
                    val randomMultiplier = Random.nextInt(1, 101)
                    val answer = tableNumber * randomMultiplier
                    QuizQuestion(
                        question = "$tableNumber × $randomMultiplier",
                        correctAnswer = answer,
                        options = generateOptions(answer)
                    )
                }
                else -> {
                    val randomMultiplier = Random.nextInt(1, 101)
                    val answer = tableNumber * randomMultiplier
                    QuizQuestion(
                        question = "$tableNumber × $randomMultiplier",
                        correctAnswer = answer,
                        options = generateOptions(answer)
                    )
                }
            }
        }
    }

    private fun generateOptions(correctAnswer: Int): List<Int> {
        val options = mutableListOf<Int>()
        options.add(correctAnswer)

        val range = maxOf(10, correctAnswer / 4)
        while (options.size < 4) {
            val wrongAnswer = correctAnswer + Random.nextInt(-range, range + 1)
            if (wrongAnswer > 0 && wrongAnswer != correctAnswer && wrongAnswer !in options) {
                options.add(wrongAnswer)
            }
        }
        return options.shuffled()
    }

    private fun startQuestion() {
        if (currentQuestionIndex < questions.size) {
            currentQuestion = questions[currentQuestionIndex]
            displayQuestion()
            startTimer()
        } else {
            showQuizResults()
        }
    }

    private fun displayQuestion() {
        currentQuestion?.let { question ->
            questionText.text = question.question
            question.options.forEachIndexed { index, option ->
                if (index < answerButtons.size) {
                    answerButtons[index].text = option.toString()
                    answerButtons[index].background = android.graphics.drawable.GradientDrawable().apply {
                        shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                        cornerRadius = 16f
                        setColor(getColorForMathType())
                    }
                    answerButtons[index].isEnabled = true
                    answerButtons[index].setTextColor(Color.WHITE)
                    answerButtons[index].gravity = android.view.Gravity.CENTER
                }
            }
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timePerQuestion, 100) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                timerText.text = secondsLeft.toString()
                val progress = ((millisUntilFinished.toFloat() / timePerQuestion) * 100).toInt()
                timerCircle.progress = progress
            }
            override fun onFinish() {
                handleTimeUp()
            }
        }.start()
    }

    private fun handleAnswer(selectedIndex: Int, selectedAnswer: Int) {
        timer.cancel()
        val correctAnswer = currentQuestion?.correctAnswer ?: 0
        val isCorrect = selectedAnswer == correctAnswer

        println("🎯 Answer Handler - Selected: $selectedAnswer, Correct: $correctAnswer, Is Correct: $isCorrect")

        // Update buttons visual feedback
        answerButtons.forEachIndexed { index, button ->
            button.isEnabled = false
            val buttonAnswer = button.text.toString().toIntOrNull() ?: 0
            button.background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(when {
                    buttonAnswer == correctAnswer -> Color.parseColor("#4CAF50")
                    index == selectedIndex && !isCorrect -> Color.parseColor("#F44336")
                    else -> Color.parseColor("#9E9E9E")
                })
            }
            button.setTextColor(Color.WHITE)
            button.gravity = android.view.Gravity.CENTER
        }

        // Update score and display
        if (isCorrect) {
            score += 50
            println("🎯 Correct! New score: $score")
            updateScoreDisplay("+50")

            // CORRECT ANSWER - NO DELAY, IMMEDIATE NEXT QUESTION
            currentQuestionIndex++
            startQuestion()
            // Handler(mainLooper).postDelayed({ }, 500) // Very short delay just for visual feedback
        } else {
            score -= 50
            println("🎯 Wrong! New score: $score")
            updateScoreDisplay("-50")

            // WRONG ANSWER - 2 SECOND DELAY TO SHOW CORRECT ANSWER
            currentQuestionIndex++
            Handler(mainLooper).postDelayed({ startQuestion() }, 2000) // 2 second delay
        }
    }

    private fun handleTimeUp() {
        val correctAnswer = currentQuestion?.correctAnswer ?: 0
        answerButtons.forEach { button ->
            button.isEnabled = false
            val buttonAnswer = button.text.toString().toIntOrNull() ?: 0
            button.background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(if (buttonAnswer == correctAnswer) Color.parseColor("#4CAF50") else Color.parseColor("#9E9E9E"))
            }
            button.setTextColor(Color.WHITE)
            button.gravity = android.view.Gravity.CENTER
        }
        score -= 50
        updateScoreDisplay("-50")
        currentQuestionIndex++
        Handler(mainLooper).postDelayed({ startQuestion() }, 2000)
    }

    private fun updateScoreDisplay(scoreChange: String) {
        // Update display with score change
        scoreDisplay.text = "Score:\n$scoreChange"

        // Change background color for feedback
        val isPositive = scoreChange.startsWith("+")
        scoreDisplay.background = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(if (isPositive) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
        }

        // After 1.5 seconds, show total score
        Handler(mainLooper).postDelayed({
            scoreDisplay.text = "Score:\n+$score"
            scoreDisplay.background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 12f
                setColor(getColorForMathType())
            }
        }, 1500)
    }

    private fun showFeedback(message: String, color: Int) {
        // No toast messages
    }

    private fun showQuizResults() {
        val percentage = (score.toFloat() / (totalQuestions * 50) * 100).toInt()
        val grade = when {
            percentage >= 90 -> "A+"
            percentage >= 80 -> "A"
            percentage >= 70 -> "B"
            percentage >= 60 -> "C"
            else -> "F"
        }

        AlertDialog.Builder(this)
            .setTitle("Quiz Complete!")
            .setMessage("Your Score: $score\nPercentage: $percentage%\nGrade: $grade")
            .setPositiveButton("Play Again") { _, _ -> restartQuiz() }
            .setNegativeButton("Back to Menu") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun restartQuiz() {
        currentQuestionIndex = 0
        score = 0
        // Reset score display with proper format
        scoreDisplay.text = "Score:\n+0"
        scoreDisplay.background = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(getColorForMathType())
        }
        generateQuestions()
        startQuestion()
    }

    private fun getColorForMathType(): Int {
        return when (mathType) {
            "addition" -> Color.parseColor("#00BCD4")
            "subtraction" -> Color.parseColor("#4CAF50")
            "multiplication" -> Color.parseColor("#FF5722")
            "division" -> Color.parseColor("#E91E63")
            else -> Color.parseColor("#FF5722")
        }
    }

    private fun showExitQuizDialog() {
        AlertDialog.Builder(this)
            .setTitle("Exit Quiz?")
            .setMessage("Your progress will be lost. Are you sure?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showQuizMenu() {
        val options = arrayOf("Restart Quiz", "Change Timer", "Settings")
        AlertDialog.Builder(this)
            .setTitle("Quiz Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> restartQuiz()
                    1 -> changeTimer()
                    2 -> openQuizSettings()
                }
            }
            .show()
    }

    private fun changeTimer() {
        val timerOptions = arrayOf("15 seconds", "30 seconds", "60 seconds", "No timer")
        val currentSelection = when (timePerQuestion) {
            15000L -> 0
            30000L -> 1
            60000L -> 2
            else -> 3
        }

        AlertDialog.Builder(this)
            .setTitle("Select Timer Duration")
            .setSingleChoiceItems(timerOptions, currentSelection) { dialog, which ->
                timePerQuestion = when (which) {
                    0 -> 15000L
                    1 -> 30000L
                    2 -> 60000L
                    else -> Long.MAX_VALUE
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun openQuizSettings() {
        // Settings implementation
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::timer.isInitialized) {
            timer.cancel()
        }
    }

    data class QuizQuestion(
        val question: String,
        val correctAnswer: Int,
        val options: List<Int>
    )
}