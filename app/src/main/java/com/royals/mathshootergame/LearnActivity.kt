package com.royals.mathshooter

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LearnActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        createLearnLayout()
    }

    private fun createLearnLayout() {
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

        // Scroll view for topics
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

        // Create topic cards
        createTopicCards(contentLayout)

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
            setBackgroundColor(Color.parseColor("#4CAF50"))
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
            text = "📚 Learn Mathematics"
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

        // Search button
        val searchButton = Button(this).apply {
            text = "🔍"
            textSize = 20f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                showSearchDialog()
            }
        }

        headerLayout.addView(backButton)
        headerLayout.addView(titleText)
        headerLayout.addView(searchButton)

        return headerLayout
    }

    private fun createTopicCards(parent: LinearLayout) {
        // Create a grid layout with 2 columns
        val topicRows = listOf(
            // Row 1: Percentage and Algebra
            listOf(
                MathTopic("Percentage", "📊", "Learn about percentages,\nratio, proportion", Color.parseColor("#FF5722"), "percentage"),
                MathTopic("Algebra", "🔢", "Variables, equations,\nlinear & quadratic", Color.parseColor("#2196F3"), "algebra")
            ),
            // Row 2: Geometry and Trigonometry
            listOf(
                MathTopic("Geometry", "📐", "Shapes, area, volume,\nperimeter calculations", Color.parseColor("#4CAF50"), "geometry"),
                MathTopic("Trigonometry", "📐", "Sin, cos, tan,\nangles & triangles", Color.parseColor("#9C27B0"), "trigonometry")
            ),
            // Row 3: Average and Time & Distance
            listOf(
                MathTopic("Average", "📊", "Mean, median, mode,\nweighted average", Color.parseColor("#FF9800"), "average"),
                MathTopic("Time & Distance", "🚗", "Speed, time, distance\nproblems", Color.parseColor("#00BCD4"), "time_distance")
            ),
            // Row 4: Profit & Loss and Simple Interest
            listOf(
                MathTopic("Profit & Loss", "💰", "Cost price, selling price,\nprofit percentage", Color.parseColor("#E91E63"), "profit_loss"),
                MathTopic("Simple Interest", "🏦", "Principal, rate, time,\ninterest calculations", Color.parseColor("#795548"), "simple_interest")
            ),
            // Row 5: Number System and Statistics
            listOf(
                MathTopic("Number System", "🔢", "Prime, composite,\nHCF, LCM", Color.parseColor("#607D8B"), "number_system"),
                MathTopic("Statistics", "📈", "Data analysis,\ncharts, graphs", Color.parseColor("#3F51B5"), "statistics")
            ),
            // Row 6: Probability and Mensuration
            listOf(
                MathTopic("Probability", "🎲", "Chance, events,\nlikelihood calculation", Color.parseColor("#009688"), "probability"),
                MathTopic("Mensuration", "📏", "2D & 3D shapes,\narea & volume", Color.parseColor("#CDDC39"), "mensuration")
            )
        )

        topicRows.forEach { row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
            }

            row.forEach { topic ->
                val card = createTopicCard(topic)
                rowLayout.addView(card)
            }

            parent.addView(rowLayout)
        }
    }

    private fun createTopicCard(topic: MathTopic): LinearLayout {
        val cardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                320,
                1f
            ).apply {
                setMargins(8, 8, 8, 8)
            }
            setBackgroundColor(Color.WHITE)
            setPadding(16, 16, 16, 16)
            elevation = 8f
            setOnClickListener {
                openTopicFormulas(topic.type, topic.title)
            }
        }

        // Header with icon
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(topic.color)
            setPadding(16, 12, 16, 12)
        }

        val iconText = TextView(this).apply {
            text = topic.icon
            textSize = 32f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
        }

        val titleText = TextView(this).apply {
            text = topic.title
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = android.view.Gravity.CENTER
        }

        headerLayout.addView(iconText)
        headerLayout.addView(titleText)

        // Content with description
        val contentText = TextView(this).apply {
            text = topic.description
            textSize = 16f
            setTextColor(Color.parseColor("#333333"))
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setPadding(0, 16, 0, 16)
        }

        // Progress indicator (future feature)
        val progressText = TextView(this).apply {
            text = "Tap to learn formulas"
            textSize = 14f
            setTextColor(Color.parseColor("#666666"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 8, 0, 0)
            typeface = Typeface.DEFAULT_BOLD
        }

        cardLayout.addView(headerLayout)
        cardLayout.addView(contentText)
        cardLayout.addView(progressText)

        return cardLayout
    }

    private fun openTopicFormulas(type: String, title: String) {
        val intent = Intent(this, FormulaListActivity::class.java)
        intent.putExtra("topic_type", type)
        intent.putExtra("topic_title", title)
        startActivity(intent)
    }

    private fun showSearchDialog() {
        val searchInput = EditText(this).apply {
            hint = "Search for topics or formulas..."
            textSize = 16f
            setPadding(16, 16, 16, 16)
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Search Mathematics")
            .setView(searchInput)
            .setPositiveButton("Search") { _, _ ->
                val query = searchInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                } else {
                    Toast.makeText(this, "Please enter search term", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performSearch(query: String) {
        // Simple search implementation
        val searchResults = when {
            query.contains("percent", true) -> "percentage"
            query.contains("algebra", true) -> "algebra"
            query.contains("geometry", true) -> "geometry"
            query.contains("trigonometry", true) || query.contains("trig", true) -> "trigonometry"
            query.contains("average", true) || query.contains("mean", true) -> "average"
            query.contains("distance", true) || query.contains("speed", true) -> "time_distance"
            query.contains("profit", true) || query.contains("loss", true) -> "profit_loss"
            query.contains("interest", true) -> "simple_interest"
            query.contains("number", true) || query.contains("prime", true) -> "number_system"
            query.contains("statistics", true) || query.contains("data", true) -> "statistics"
            query.contains("probability", true) || query.contains("chance", true) -> "probability"
            query.contains("mensuration", true) || query.contains("area", true) -> "mensuration"
            else -> null
        }

        if (searchResults != null) {
            val intent = Intent(this, FormulaListActivity::class.java)
            intent.putExtra("topic_type", searchResults)
            intent.putExtra("topic_title", query.capitalize())
            startActivity(intent)
        } else {
            Toast.makeText(this, "No results found for '$query'", Toast.LENGTH_SHORT).show()
        }
    }

    data class MathTopic(
        val title: String,
        val icon: String,
        val description: String,
        val color: Int,
        val type: String
    )
}