package com.royals.mathshootergame

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class TableGridActivity : AppCompatActivity() {

    private lateinit var mathType: String
    private lateinit var mathTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        mathType = intent.getStringExtra("math_type") ?: "multiplication"
        mathTitle = intent.getStringExtra("math_title") ?: "Multiplication"

        createGridLayout()
    }

    private fun createGridLayout() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F0F8FF"))
        }

        // Header
        val headerLayout = createHeader()
        mainLayout.addView(headerLayout)

        // Buttons row
        val buttonsLayout = createButtonsRow()
        mainLayout.addView(buttonsLayout)

        // Grid
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setPadding(16, 16, 16, 16)
        }

        val gridLayout = createNumberGrid()
        scrollView.addView(gridLayout)
        mainLayout.addView(scrollView)

        setContentView(mainLayout)
    }

    private fun createHeader(): LinearLayout {
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
            text = "← $mathTitle Table"
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { finish() }
        }

        val menuButton = Button(this).apply {
            text = "⋮"
            textSize = 20f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener {
                showGridMenu()
            }
        }

        headerLayout.addView(backButton)
        headerLayout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
        })
        headerLayout.addView(menuButton)

        return headerLayout
    }

    private fun createButtonsRow(): LinearLayout {
        val buttonsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 8)
        }

        val selectExamButton = Button(this).apply {
            text = "SELECT EXAM"
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(getColorForMathType())
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(4, 0, 4, 0)
            }
            setOnClickListener {
                // Show exam mode options
                showExamOptions()
            }
        }

        val customButton = Button(this).apply {
            text = "CUSTOM"
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(getColorForMathType())
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(4, 0, 4, 0)
            }
            setOnClickListener {
                showCustomDialog()
            }
        }

        buttonsLayout.addView(selectExamButton)
        buttonsLayout.addView(customButton)

        return buttonsLayout
    }

    private fun createNumberGrid(): LinearLayout {
        val gridContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Create 25 rows with 4 numbers each (1-100)
        for (row in 0 until 25) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            for (col in 0 until 4) {
                val number = row * 4 + col + 1
                if (number <= 100) {
                    val numberButton = createNumberButton(number)
                    rowLayout.addView(numberButton)
                }
            }

            gridContainer.addView(rowLayout)
        }

        return gridContainer
    }

    private fun createNumberButton(number: Int): Button {
        return Button(this).apply {
            text = number.toString()
            textSize = 18f
            setTextColor(Color.WHITE)
            setBackgroundColor(getColorForMathType())
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0,
                120,
                1f
            ).apply {
                setMargins(4, 4, 4, 4)
            }
            elevation = 4f

            setOnClickListener {
                showTableOptions(number)
            }
        }
    }

    private fun showTableOptions(tableNumber: Int) {
        val options = arrayOf("View Table", "Practice Quiz", "Quick Test")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("$mathTitle Table of $tableNumber")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewTable(tableNumber)
                    1 -> startQuiz(tableNumber)
                    2 -> startQuickTest(tableNumber)
                }
            }
            .show()
    }

    // Update the viewTable method in TableGridActivity.kt
    private fun viewTable(tableNumber: Int) {
        val intent = Intent(this, MathTableActivity::class.java)
        intent.putExtra("math_type", mathType)
        intent.putExtra("math_title", mathTitle)
        intent.putExtra("table_number", tableNumber) // This line should pass the selected number
        startActivity(intent)
    }

    private fun startQuiz(tableNumber: Int) {
        val intent = Intent(this, MathQuizActivity::class.java)
        intent.putExtra("math_type", mathType)
        intent.putExtra("math_title", mathTitle)
        intent.putExtra("table_number", tableNumber)
        intent.putExtra("total_questions", 10)
        startActivity(intent)
    }

    private fun startQuickTest(tableNumber: Int) {
        val intent = Intent(this, MathQuizActivity::class.java)
        intent.putExtra("math_type", mathType)
        intent.putExtra("math_title", mathTitle)
        intent.putExtra("table_number", tableNumber)
        intent.putExtra("total_questions", 5) // Quick test = 5 questions
        startActivity(intent)
    }

    private fun showExamOptions() {
        val examTypes = arrayOf("Tables 1-12", "Tables 13-20", "Tables 21-50", "Tables 51-100", "Mixed Tables")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Exam Type")
            .setItems(examTypes) { _, which ->
                when (which) {
                    0 -> startExam(1, 12)
                    1 -> startExam(13, 20)
                    2 -> startExam(21, 50)
                    3 -> startExam(51, 100)
                    4 -> startMixedExam()
                }
            }
            .show()
    }

    private fun startExam(startTable: Int, endTable: Int) {
        val intent = Intent(this, MathQuizActivity::class.java)
        intent.putExtra("math_type", mathType)
        intent.putExtra("math_title", mathTitle)
        intent.putExtra("start_table", startTable)
        intent.putExtra("end_table", endTable)
        intent.putExtra("total_questions", 20)
        intent.putExtra("exam_mode", true)
        startActivity(intent)
    }

    private fun startMixedExam() {
        val intent = Intent(this, MathQuizActivity::class.java)
        intent.putExtra("math_type", "mixed")
        intent.putExtra("math_title", "Mixed Operations")
        intent.putExtra("total_questions", 25)
        intent.putExtra("exam_mode", true)
        startActivity(intent)
    }

    private fun showCustomDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }

        val tableInput = EditText(this).apply {
            hint = "Enter Number of Table"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val rangeInput = EditText(this).apply {
            hint = "Enter Number till you want that"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        dialogLayout.addView(TextView(this).apply {
            text = "Multiplication Table"
            textSize = 20f
            setTextColor(Color.BLACK)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 16)
        })

        dialogLayout.addView(tableInput)
        dialogLayout.addView(rangeInput)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setPositiveButton("OK") { _, _ ->
                val tableNumber = tableInput.text.toString().toIntOrNull() ?: 1
                val range = rangeInput.text.toString().toIntOrNull() ?: 10

                if (tableNumber in 1..1000 && range in 1..100) {
                    val intent = Intent(this, MathTableActivity::class.java)
                    intent.putExtra("math_type", mathType)
                    intent.putExtra("math_title", mathTitle)
                    intent.putExtra("table_number", tableNumber)
                    intent.putExtra("table_range", range)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun showGridMenu() {
        val options = arrayOf("Search Table", "Random Table", "Favorites", "Settings")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Grid Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSearchDialog()
                    1 -> openRandomTable()
                    2 -> showFavorites()
                    3 -> openGridSettings()
                }
            }
            .show()
    }

    private fun showSearchDialog() {
        val searchInput = EditText(this).apply {
            hint = "Enter table number to search"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Search Table")
            .setView(searchInput)
            .setPositiveButton("Search") { _, _ ->
                val number = searchInput.text.toString().toIntOrNull()
                if (number != null && number in 1..100) {
                    showTableOptions(number)
                } else {
                    Toast.makeText(this, "Please enter a number between 1-100", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openRandomTable() {
        val randomNumber = (1..100).random()
        showTableOptions(randomNumber)
    }

    private fun showFavorites() {
        Toast.makeText(this, "Favorites feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun openGridSettings() {
        val settings = arrayOf("Change Grid Size", "Color Theme", "Number Range")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Grid Settings")
            .setItems(settings) { _, which ->
                when (which) {
                    0 -> changeGridSize()
                    1 -> changeColorTheme()
                    2 -> changeNumberRange()
                }
            }
            .show()
    }

    private fun changeGridSize() {
        val sizes = arrayOf("3x3 Grid", "4x4 Grid", "5x5 Grid", "6x6 Grid")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Grid Size")
            .setItems(sizes) { _, which ->
                Toast.makeText(this, "Grid size changed to ${sizes[which]}", Toast.LENGTH_SHORT).show()
                // You can implement grid size change here
            }
            .show()
    }

    private fun changeColorTheme() {
        val themes = arrayOf("Red Theme", "Blue Theme", "Green Theme", "Purple Theme")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Color Theme")
            .setItems(themes) { _, which ->
                Toast.makeText(this, "Theme changed to ${themes[which]}", Toast.LENGTH_SHORT).show()
                // You can implement theme change here
            }
            .show()
    }

    private fun changeNumberRange() {
        val ranges = arrayOf("1-50", "1-100", "1-200", "Custom Range")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Number Range")
            .setItems(ranges) { _, which ->
                when (which) {
                    3 -> showCustomRangeDialog()
                    else -> {
                        Toast.makeText(this, "Range changed to ${ranges[which]}", Toast.LENGTH_SHORT).show()
                        // You can implement range change here
                    }
                }
            }
            .show()
    }

    private fun showCustomRangeDialog() {
        val rangeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }

        val startInput = EditText(this).apply {
            hint = "Start"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val endInput = EditText(this).apply {
            hint = "End"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val toText = TextView(this).apply {
            text = " to "
            setPadding(16, 0, 16, 0)
        }

        rangeLayout.addView(startInput)
        rangeLayout.addView(toText)
        rangeLayout.addView(endInput)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Custom Range")
            .setView(rangeLayout)
            .setPositiveButton("Apply") { _, _ ->
                val start = startInput.text.toString().toIntOrNull() ?: 1
                val end = endInput.text.toString().toIntOrNull() ?: 100

                if (start < end && start > 0) {
                    Toast.makeText(this, "Range set to $start-$end", Toast.LENGTH_SHORT).show()
                    // Recreate grid with new range
                } else {
                    Toast.makeText(this, "Invalid range", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
}