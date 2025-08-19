package com.royals.mathshooter

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MathLearnerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        createMathLearnerLayout()
    }

    private fun createMathLearnerLayout() {
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }

        // Header
        val headerLayout = createHeader()
        mainLayout.addView(headerLayout)

        // Scroll view for math operations
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setPadding(16, 16, 16, 16)
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Create math operation cards
        createMathOperationCards(contentLayout)

        scrollView.addView(contentLayout)
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
            setBackgroundColor(Color.parseColor("#2196F3"))
            setPadding(16, 24, 16, 24)
        }

        // Back button
        val backButton = Button(this).apply {
            text = "←"
            textSize = 24f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { finish() }
        }

        // Title
        val titleText = TextView(this).apply {
            text = "📚 Math Learner"
            textSize = 24f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        // Settings button
        val settingsButton = Button(this).apply {
            text = "⚙️"
            textSize = 20f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                // Could open math learner settings
            }
        }

        headerLayout.addView(backButton)
        headerLayout.addView(titleText)
        headerLayout.addView(settingsButton)

        return headerLayout
    }

    private fun createMathOperationCards(parent: LinearLayout) {
        // Create a grid layout with 2 columns
        val gridRows = listOf(
            // Row 1: Addition and Multiplication
            listOf(
                MathOperation("Addition", "+", "1+1=2\n1+2=3\n1+3=4", Color.parseColor("#00BCD4"), "addition"),
                MathOperation("Multiplication", "×", "1×1=1\n1×2=2\n1×3=3", Color.parseColor("#FF5722"), "multiplication")
            ),
            // Row 2: Subtraction and Division
            listOf(
                MathOperation("Subtraction", "−", "1−1=0\n2−1=1\n3−1=2", Color.parseColor("#4CAF50"), "subtraction"),
                MathOperation("Division", "÷", "1÷1=1\n2÷1=2\n4÷2=2", Color.parseColor("#E91E63"), "division")
            ),
            // Row 3: Squares and Square Roots
            listOf(
                MathOperation("Squares", "x²", "1²=1\n2²=4\n3²=9", Color.parseColor("#FF9800"), "squares"),
                MathOperation("Square Roots", "√x", "√1=1\n√4=2\n√9=3", Color.parseColor("#9C27B0"), "square_roots")
            ),
            // Row 4: Cubes and Cube Roots
            listOf(
                MathOperation("Cubes", "x³", "1³=1\n2³=8\n3³=27", Color.parseColor("#FF6347"), "cubes"),
                MathOperation("Cube Roots", "∛x", "∛1=1\n∛8=2\n∛27=3", Color.parseColor("#8E24AA"), "cube_roots")
            )
        )

        gridRows.forEach { row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
            }

            row.forEach { operation ->
                val card = createMathCard(operation)
                rowLayout.addView(card)
            }

            parent.addView(rowLayout)
        }
    }

    private fun createMathCard(operation: MathOperation): LinearLayout {
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                300,
                1f
            ).apply {
                setMargins(8, 8, 8, 8)
            }
            setBackgroundColor(Color.WHITE)
            setPadding(16, 16, 16, 16)
            elevation = 8f
            setOnClickListener {
                openMathTable(operation.type, operation.title)
            }
        }

        // Header with symbol
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(operation.color)
            setPadding(16, 12, 16, 12)
        }

        val symbolText = TextView(this).apply {
            text = operation.symbol
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
        }

        val titleText = TextView(this).apply {
            text = operation.title
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
        }

        headerLayout.addView(symbolText)
        headerLayout.addView(titleText)

        // Content with examples
        val contentText = TextView(this).apply {
            text = operation.examples
            textSize = 18f
            setTextColor(Color.parseColor("#333333"))
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setPadding(0, 16, 0, 16)
        }

        cardLayout.addView(headerLayout)
        cardLayout.addView(contentText)

        return cardLayout
    }

    // Update the openMathTable method in MathLearnerActivity.kt
    private fun openMathTable(type: String, title: String) {
        if (type == "multiplication" || type == "division") {
            // Go to grid selection for multiplication/division
            val intent = Intent(this, TableGridActivity::class.java)
            intent.putExtra("math_type", type)
            intent.putExtra("math_title", title)
            startActivity(intent)
        } else {
            // Go directly to table for other operations
            val intent = Intent(this, MathTableActivity::class.java)
            intent.putExtra("math_type", type)
            intent.putExtra("math_title", title)
            startActivity(intent)
        }
    }

    data class MathOperation(
        val title: String,
        val symbol: String,
        val examples: String,
        val color: Int,
        val type: String
    )
}