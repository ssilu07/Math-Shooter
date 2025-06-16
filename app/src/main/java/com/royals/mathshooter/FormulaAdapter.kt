package com.royals.mathshooter

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FormulaAdapter(
    private var formulas: List<Formula>,
    private val accentColor: Int
) : RecyclerView.Adapter<FormulaAdapter.FormulaViewHolder>() {

    class FormulaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardLayout: LinearLayout = itemView as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormulaViewHolder {
        val cardLayout = createFormulaCard(parent)
        return FormulaViewHolder(cardLayout)
    }

    override fun onBindViewHolder(holder: FormulaViewHolder, position: Int) {
        val formula = formulas[position]
        bindFormulaToCard(holder.cardLayout, formula)
    }

    override fun getItemCount(): Int = formulas.size

    fun updateFormulas(newFormulas: List<Formula>) {
        formulas = newFormulas
        notifyDataSetChanged()
    }

    private fun createFormulaCard(parent: ViewGroup): LinearLayout {
        val context = parent.context

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            setBackgroundColor(Color.WHITE)
            setPadding(20, 16, 20, 16)
            elevation = 6f

            // Add rounded corners programmatically
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 12f
                setColor(Color.WHITE)
                setStroke(2, Color.parseColor("#E0E0E0"))
            }
        }
    }

    private fun bindFormulaToCard(cardLayout: LinearLayout, formula: Formula) {
        val context = cardLayout.context
        cardLayout.removeAllViews()

        // Header with formula name and category badge
        val headerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
        }

        val nameText = TextView(context).apply {
            text = formula.name
            textSize = 20f
            setTextColor(Color.parseColor("#333333"))
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val categoryBadge = TextView(context).apply {
            text = formula.category.uppercase()
            textSize = 12f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(12, 6, 12, 6)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(if (formula.category == "basic") Color.parseColor("#4CAF50") else accentColor)
            }
        }

        headerLayout.addView(nameText)
        headerLayout.addView(categoryBadge)

        // Formula text with special styling
        val formulaText = TextView(context).apply {
            text = formula.formula
            textSize = 18f
            setTextColor(accentColor)
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true) // Allow copying
            setPadding(16, 12, 16, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 8f
                setColor(Color.parseColor("#F8F9FA"))
                setStroke(1, Color.parseColor("#E9ECEF"))
            }
        }

        // Description
        val descriptionText = TextView(context).apply {
            text = formula.description
            textSize = 16f
            setTextColor(Color.parseColor("#666666"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
        }

        // Example section with expandable design
        val exampleHeader = TextView(context).apply {
            text = "📝 Example:"
            textSize = 16f
            setTextColor(Color.parseColor("#333333"))
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 4)
            }
        }

        val exampleText = TextView(context).apply {
            text = formula.example
            textSize = 15f
            setTextColor(Color.parseColor("#444444"))
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true)
            setPadding(12, 10, 12, 10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 6f
                setColor(Color.parseColor("#FFF9C4"))
                setStroke(1, Color.parseColor("#FBC02D"))
            }
        }

        // Action buttons
        val actionLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0)
            }
            gravity = android.view.Gravity.END
        }

        val copyButton = createActionButton(context, "📋", "Copy") {
            copyFormulaToClipboard(context, formula)
        }

        val shareButton = createActionButton(context, "📤", "Share") {
            shareFormula(context, formula)
        }

        val bookmarkButton = createActionButton(context, "🔖", "Save") {
            bookmarkFormula(context, formula)
        }

        actionLayout.addView(copyButton)
        actionLayout.addView(shareButton)
        actionLayout.addView(bookmarkButton)

        // Add all views to card
        cardLayout.addView(headerLayout)
        cardLayout.addView(formulaText)
        cardLayout.addView(descriptionText)
        cardLayout.addView(exampleHeader)
        cardLayout.addView(exampleText)
        cardLayout.addView(actionLayout)
    }

    private fun createActionButton(
        context: android.content.Context,
        icon: String,
        text: String,
        onClick: () -> Unit
    ): TextView {
        return TextView(context).apply {
            this.text = "$icon $text"
            textSize = 12f
            setTextColor(accentColor)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(12, 8, 12, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 0, 0)
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(Color.parseColor("#F5F5F5"))
                setStroke(1, accentColor)
            }
            setOnClickListener { onClick() }

            // Add click effect
            isClickable = true
            isFocusable = true
            background = android.graphics.drawable.StateListDrawable().apply {
                // Pressed state
                val pressedDrawable = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 16f
                    setColor(Color.parseColor("#E0E0E0"))
                    setStroke(1, accentColor)
                }
                addState(intArrayOf(android.R.attr.state_pressed), pressedDrawable)

                // Normal state
                val normalDrawable = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 16f
                    setColor(Color.parseColor("#F5F5F5"))
                    setStroke(1, accentColor)
                }
                addState(intArrayOf(), normalDrawable)
            }
        }
    }

    private fun copyFormulaToClipboard(context: android.content.Context, formula: Formula) {
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText(
            formula.name,
            "${formula.name}\n${formula.formula}\n\nExample:\n${formula.example}"
        )
        clipboard.setPrimaryClip(clip)
        android.widget.Toast.makeText(context, "Formula copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun shareFormula(context: android.content.Context, formula: Formula) {
        val shareText = buildString {
            appendLine("📐 ${formula.name}")
            appendLine("=".repeat(30))
            appendLine("Formula: ${formula.formula}")
            appendLine()
            appendLine("Description: ${formula.description}")
            appendLine()
            appendLine("Example: ${formula.example}")
            appendLine()
            appendLine("Shared from Math Shooter App")
        }

        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            putExtra(android.content.Intent.EXTRA_SUBJECT, formula.name)
        }
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Formula"))
    }

    private fun bookmarkFormula(context: android.content.Context, formula: Formula) {
        // Future implementation: Save to local database or shared preferences
        android.widget.Toast.makeText(context, "Formula bookmarked! (Feature coming soon)", android.widget.Toast.LENGTH_SHORT).show()
    }
}