package com.royals.mathshootergame

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MathTableActivity : AppCompatActivity() {

    private lateinit var mathType: String
    private lateinit var mathTitle: String
    private lateinit var tableContentLayout: LinearLayout
    private var tableNumber: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        mathType = intent.getStringExtra("math_type") ?: "multiplication"
        mathTitle = intent.getStringExtra("math_title") ?: "Math Table"
        tableNumber = intent.getIntExtra("table_number", 1)

        createEnhancedTableLayout()
    }

    private fun createEnhancedTableLayout() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        // Main table card container (no header, no selector buttons)
        val tableCardContainer = createTableCardContainer()
        mainLayout.addView(tableCardContainer)

        // Action buttons at bottom
        val actionButtonsLayout = createActionButtons()
        mainLayout.addView(actionButtonsLayout)

        setContentView(mainLayout)
    }

    private fun createTableHeader(): LinearLayout {
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
            text = "← BACK"
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener { finish() }
        }

        val titleText = TextView(this).apply {
            text = "$mathTitle Table"
            textSize = 22f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val menuButton = Button(this).apply {
            text = "⋮"
            textSize = 24f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { showOptionsMenu() }
        }

        headerLayout.addView(backButton)
        headerLayout.addView(titleText)
        headerLayout.addView(menuButton)

        return headerLayout
    }

    private fun createTableSelector(): LinearLayout {
        val selectorLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(12, 12, 12, 0)
        }

        val selectExamButton = Button(this).apply {
            text = "SELECT EXAM"
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(getColorForMathType())
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0,
                120,
                1f
            ).apply {
                setMargins(4, 0, 4, 0)
            }
            setOnClickListener { showTableSelector() }
        }

        val customButton = Button(this).apply {
            text = "CUSTOM"
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(getColorForMathType())
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0,
                120,
                1f
            ).apply {
                setMargins(4, 0, 4, 0)
            }
            setOnClickListener { showCustomTableDialog() }
        }

        selectorLayout.addView(selectExamButton)
        selectorLayout.addView(customButton)

        return selectorLayout
    }

    private fun createTableCardContainer(): LinearLayout {
        val containerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setPadding(12, 24, 12, 16) // Added top padding since no header
        }

        // Main table card with rounded corners and shadow
        val tableCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            background = createRoundedCardBackground()
            elevation = 8f
        }

        // Table header with number circle (this is now the main header)
        val tableHeaderCard = createStyledTableHeader()
        tableCard.addView(tableHeaderCard)

        // ScrollView for equations
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setPadding(0, 0, 0, 16)
        }

        tableContentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(24, 0, 24, 0)
        }

        // Generate table equations
        generateStyledTableContent()

        // Play button (like in your image)
        val playButtonContainer = createPlayButton()
        tableCard.addView(playButtonContainer)

        scrollView.addView(tableContentLayout)
        tableCard.addView(scrollView)

        containerLayout.addView(tableCard)
        return containerLayout
    }

    private fun createRoundedCardBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            setColor(Color.WHITE)
            setStroke(3, getColorForMathType())
        }
    }

    private fun createStyledTableHeader(): LinearLayout {
        val headerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(getColorForMathType())
            setPadding(32, 32, 32, 32) // Increased padding since it's the main header now
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadii = floatArrayOf(24f, 24f, 24f, 24f, 0f, 0f, 0f, 0f) // Only top corners rounded
                setColor(getColorForMathType())
            }
        }

        // Number circle (like "15" in your image - just the number, not "15x")
        val numberCircle = TextView(this).apply {
            text = tableNumber.toString()
            textSize = 32f
            setTextColor(getColorForMathType())
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                120,
                120
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                setMargins(0, 0, 0, 20)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
                setStroke(4, getColorForMathType())
            }
        }

        // Title text
        val titleText = TextView(this).apply {
            text = "$mathTitle Table"
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
        }

        headerCard.addView(numberCircle)
        headerCard.addView(titleText)

        return headerCard
    }

    private fun generateStyledTableContent() {
        tableContentLayout.removeAllViews()

        val equations = generateEquations(tableNumber)

        equations.forEach { equation ->
            val equationView = createStyledEquationView(equation)
            tableContentLayout.addView(equationView)
        }
    }

    private fun createStyledEquationView(equation: String): TextView {
        return TextView(this).apply {
            text = equation
            textSize = 28f
            setTextColor(Color.parseColor("#333333"))
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 16)
            }
            setPadding(16, 20, 16, 20)

            // Add subtle background for each equation
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f
                setColor(Color.parseColor("#FAFAFA"))
            }
        }
    }

    private fun createPlayButton(): LinearLayout {
        val playContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = android.view.Gravity.END
            setPadding(0, 0, 24, 24)
        }

        val playButton = Button(this).apply {
            text = "▶"
            textSize = 24f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(80, 80)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(getColorForMathType())
            }
            elevation = 6f
            setOnClickListener {
                // Play audio or start animation
                Toast.makeText(this@MathTableActivity, "Playing table audio", Toast.LENGTH_SHORT).show()
            }
        }

        playContainer.addView(playButton)
        return playContainer
    }

    private fun createActionButtons(): LinearLayout {
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(12, 0, 12, 24)
        }

        val practiceButton = Button(this).apply {
            text = "PRACTICE"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(getColorForMathType())
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0,
                120,
                1f
            ).apply {
                setMargins(4, 0, 4, 0)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12f
                setColor(getColorForMathType())
            }
            elevation = 4f
            setOnClickListener { startPracticeMode() }
        }

        val examButton = Button(this).apply {
            text = "SELECT EXAM"
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(getColorForMathType())
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0,
                120,
                1f
            ).apply {
                setMargins(4, 0, 4, 0)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 12f
                setColor(getColorForMathType())
            }
            elevation = 4f
            setOnClickListener { showTableSelector() }
        }

        buttonLayout.addView(practiceButton)
        buttonLayout.addView(examButton)

        return buttonLayout
    }

    private fun generateEquations(tableNum: Int): List<String> {
        val tableRange = intent.getIntExtra("table_range", 12)

        return when (mathType) {
            "multiplication" -> (1..tableRange).map { "$tableNum × $it = ${tableNum * it}" }
            "division" -> (1..tableRange).map {
                val dividend = tableNum * it
                "$dividend ÷ $tableNum = $it"
            }
            "addition" -> (1..tableRange).map { "$tableNum + $it = ${tableNum + it}" }
            "subtraction" -> (1..tableRange).map {
                val result = maxOf(0, tableNum - it)
                "$tableNum - $it = $result"
            }
            else -> (1..tableRange).map { "$tableNum × $it = ${tableNum * it}" }
        }
    }

    private fun getColorForMathType(): Int {
        return when (mathType) {
            "addition" -> Color.parseColor("#00BCD4")
            "subtraction" -> Color.parseColor("#4CAF50")
            "multiplication" -> Color.parseColor("#FF5722")
            "division" -> Color.parseColor("#E91E63")
            "squares" -> Color.parseColor("#FF9800")
            "square_roots" -> Color.parseColor("#9C27B0")
            "cubes" -> Color.parseColor("#FF6347")
            "cube_roots" -> Color.parseColor("#8E24AA")
            else -> Color.parseColor("#FF5722")
        }
    }

    private fun showTableSelector() {
        val intent = Intent(this, TableGridActivity::class.java)
        intent.putExtra("math_type", mathType)
        intent.putExtra("math_title", mathTitle)
        startActivity(intent)
    }

    private fun showCustomTableDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }

        val tableInput = EditText(this).apply {
            hint = "Enter Number of Table"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            textSize = 16f
            setPadding(16, 16, 16, 16)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f
                setColor(Color.parseColor("#F5F5F5"))
                setStroke(2, Color.parseColor("#DDDDDD"))
            }
        }

        val rangeInput = EditText(this).apply {
            hint = "Enter Number till you want that"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            textSize = 16f
            setPadding(16, 16, 16, 16)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f
                setColor(Color.parseColor("#F5F5F5"))
                setStroke(2, Color.parseColor("#DDDDDD"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0)
            }
        }

        dialogLayout.addView(TextView(this).apply {
            text = "$mathTitle Table"
            textSize = 20f
            setTextColor(Color.BLACK)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 24)
        })

        dialogLayout.addView(tableInput)
        dialogLayout.addView(rangeInput)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setPositiveButton("OK") { _, _ ->
                val tableNum = tableInput.text.toString().toIntOrNull() ?: tableNumber
                val range = rangeInput.text.toString().toIntOrNull() ?: 12

                if (tableNum in 1..1000 && range in 1..100) {
                    tableNumber = tableNum
                    val intent = Intent(this, MathTableActivity::class.java)
                    intent.putExtra("math_type", mathType)
                    intent.putExtra("math_title", mathTitle)
                    intent.putExtra("table_number", tableNum)
                    intent.putExtra("table_range", range)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun startPracticeMode() {
        val options = arrayOf("Study Mode", "Quiz Mode", "Game Mode")

        AlertDialog.Builder(this)
            .setTitle("Choose Practice Type")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, "Continue studying the table", Toast.LENGTH_SHORT).show()
                    1 -> startQuizMode()
                    2 -> startGameMode()
                }
            }
            .show()
    }

    private fun startQuizMode() {
        val intent = Intent(this, MathQuizActivity::class.java)
        intent.putExtra("math_type", mathType)
        intent.putExtra("math_title", mathTitle)
        intent.putExtra("table_number", tableNumber)
        intent.putExtra("total_questions", 10)
        startActivity(intent)
    }

    private fun startGameMode() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("game_mode", "practice")

        val practiceType = when (mathType) {
            "addition" -> 0
            "subtraction" -> 1
            "multiplication" -> 2
            "division" -> 3
            else -> 4
        }

        val sharedPreferences = getSharedPreferences("MathShooterPrefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt("practice_type", practiceType)
            .putInt("practice_difficulty", 1)
            .apply()

        startActivity(intent)
        finish()
    }

    private fun showOptionsMenu() {
        val options = arrayOf("Share Table", "Print Table", "Audio Mode", "Settings")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Table Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, "Share feature coming soon!", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(this, "Print feature coming soon!", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(this, "Audio mode coming soon!", Toast.LENGTH_SHORT).show()
                    3 -> Toast.makeText(this, "Table settings coming soon!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}