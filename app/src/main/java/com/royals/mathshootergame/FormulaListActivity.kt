package com.royals.mathshootergame

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FormulaListActivity : AppCompatActivity() {

    private lateinit var topicType: String
    private lateinit var topicTitle: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var formulaAdapter: FormulaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        topicType = intent.getStringExtra("topic_type") ?: "percentage"
        topicTitle = intent.getStringExtra("topic_title") ?: "Mathematics"

        createFormulaLayout()
        loadFormulas()
    }

    private fun createFormulaLayout() {
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

        // Filter buttons
        val filterLayout = createFilterButtons()
        mainLayout.addView(filterLayout)

        // RecyclerView for formulas
        recyclerView = RecyclerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setPadding(16, 8, 16, 16)
            layoutManager = LinearLayoutManager(this@FormulaListActivity)
        }

        mainLayout.addView(recyclerView)
        setContentView(mainLayout)
    }

    private fun createHeader(): LinearLayout {
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(getColorForTopic())
            setPadding(16, 24, 16, 24)
        }

        val backButton = Button(this).apply {
            text = "← Back"
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener { finish() }
        }

        val titleText = TextView(this).apply {
            text = "$topicTitle Formulas"
            textSize = 20f
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
            textSize = 20f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { showFormulaMenu() }
        }

        headerLayout.addView(backButton)
        headerLayout.addView(titleText)
        headerLayout.addView(menuButton)

        return headerLayout
    }

    private fun createFilterButtons(): LinearLayout {
        val filterLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 12, 16, 8)
        }

        val allButton = createFilterButton("All", true)
        val basicButton = createFilterButton("Basic", false)
        val advancedButton = createFilterButton("Advanced", false)
        val exampleButton = createFilterButton("Examples", false)

        filterLayout.addView(allButton)
        filterLayout.addView(basicButton)
        filterLayout.addView(advancedButton)
        filterLayout.addView(exampleButton)

        return filterLayout
    }

    private fun createFilterButton(text: String, isSelected: Boolean): Button {
        return Button(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(if (isSelected) Color.WHITE else getColorForTopic())
            setBackgroundColor(if (isSelected) getColorForTopic() else Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(4, 0, 4, 0)
            }
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener {
                filterFormulas(text)
                updateFilterButtons(this)
            }
        }
    }

    private fun updateFilterButtons(selectedButton: Button) {
        val parent = selectedButton.parent as LinearLayout
        for (i in 0 until parent.childCount) {
            val button = parent.getChildAt(i) as Button
            if (button == selectedButton) {
                button.setTextColor(Color.WHITE)
                button.setBackgroundColor(getColorForTopic())
            } else {
                button.setTextColor(getColorForTopic())
                button.setBackgroundColor(Color.WHITE)
            }
        }
    }

    private fun loadFormulas() {
        val formulas = getFormulasForTopic(topicType)
        formulaAdapter = FormulaAdapter(formulas, getColorForTopic())
        recyclerView.adapter = formulaAdapter
    }

    private fun getFormulasForTopic(type: String): List<Formula> {
        return when (type) {
            "percentage" -> getPercentageFormulas()
            "algebra" -> getAlgebraFormulas()
            "geometry" -> getGeometryFormulas()
            "trigonometry" -> getTrigonometryFormulas()
            "average" -> getAverageFormulas()
            "time_distance" -> getTimeDistanceFormulas()
            "profit_loss" -> getProfitLossFormulas()
            "simple_interest" -> getSimpleInterestFormulas()
            "number_system" -> getNumberSystemFormulas()
            "statistics" -> getStatisticsFormulas()
            "probability" -> getProbabilityFormulas()
            "mensuration" -> getMensurationFormulas()
            else -> getPercentageFormulas()
        }
    }

    private fun getPercentageFormulas(): List<Formula> {
        return listOf(
            Formula(
                "Basic Percentage",
                "Percentage = (Part/Whole) × 100",
                "To find what percent one number is of another",
                "If 25 out of 100 students passed:\n25/100 × 100 = 25%",
                "basic"
            ),
            Formula(
                "Percentage to Decimal",
                "Decimal = Percentage ÷ 100",
                "Convert percentage to decimal form",
                "75% = 75 ÷ 100 = 0.75",
                "basic"
            ),
            Formula(
                "Percentage Increase",
                "% Increase = [(New - Old)/Old] × 100",
                "Calculate percentage increase between two values",
                "Price increased from ₹100 to ₹120:\n[(120-100)/100] × 100 = 20%",
                "basic"
            ),
            Formula(
                "Percentage Decrease",
                "% Decrease = [(Old - New)/Old] × 100",
                "Calculate percentage decrease between two values",
                "Price decreased from ₹200 to ₹150:\n[(200-150)/200] × 100 = 25%",
                "basic"
            ),
            Formula(
                "Finding the Part",
                "Part = (Percentage × Whole) ÷ 100",
                "Find the part when percentage and whole are known",
                "30% of 250 = (30 × 250) ÷ 100 = 75",
                "basic"
            ),
            Formula(
                "Finding the Whole",
                "Whole = (Part × 100) ÷ Percentage",
                "Find the whole when part and percentage are known",
                "If 45 is 15% of a number:\nWhole = (45 × 100) ÷ 15 = 300",
                "basic"
            ),
            Formula(
                "Discount Formula",
                "Discount = (Discount% × Marked Price) ÷ 100",
                "Calculate discount amount",
                "20% discount on ₹500:\nDiscount = (20 × 500) ÷ 100 = ₹100",
                "advanced"
            ),
            Formula(
                "Successive Percentage",
                "Final = Initial × (1 ± %₁/100) × (1 ± %₂/100)",
                "Calculate effect of multiple percentage changes",
                "20% increase then 10% decrease on 100:\n100 × 1.2 × 0.9 = 108",
                "advanced"
            )
        )
    }

    private fun getAlgebraFormulas(): List<Formula> {
        return listOf(
            Formula(
                "Linear Equation",
                "ax + b = 0, Solution: x = -b/a",
                "Solve first degree equations",
                "2x + 6 = 0\nx = -6/2 = -3",
                "basic"
            ),
            Formula(
                "Quadratic Formula",
                "x = [-b ± √(b² - 4ac)] / 2a",
                "Solve ax² + bx + c = 0",
                "x² - 5x + 6 = 0\na=1, b=-5, c=6\nx = [5 ± √(25-24)]/2 = 3, 2",
                "advanced"
            ),
            Formula(
                "Factorization",
                "a² - b² = (a + b)(a - b)",
                "Difference of squares",
                "x² - 9 = (x + 3)(x - 3)",
                "basic"
            ),
            Formula(
                "Perfect Square",
                "(a ± b)² = a² ± 2ab + b²",
                "Square of binomial",
                "(x + 3)² = x² + 6x + 9",
                "basic"
            ),
            Formula(
                "Arithmetic Progression",
                "nth term: aₙ = a + (n-1)d\nSum: Sₙ = n/2[2a + (n-1)d]",
                "Sequence with common difference",
                "2, 5, 8, 11... (a=2, d=3)\n5th term = 2 + 4×3 = 14",
                "advanced"
            ),
            Formula(
                "Geometric Progression",
                "nth term: aₙ = a × r^(n-1)\nSum: Sₙ = a(r^n - 1)/(r - 1)",
                "Sequence with common ratio",
                "2, 6, 18, 54... (a=2, r=3)\n4th term = 2 × 3³ = 54",
                "advanced"
            )
        )
    }

    private fun getGeometryFormulas(): List<Formula> {
        return listOf(
            Formula(
                "Area of Rectangle",
                "Area = Length × Width",
                "Calculate rectangular area",
                "Length = 8m, Width = 5m\nArea = 8 × 5 = 40 m²",
                "basic"
            ),
            Formula(
                "Area of Triangle",
                "Area = (1/2) × Base × Height",
                "Calculate triangular area",
                "Base = 10cm, Height = 6cm\nArea = (1/2) × 10 × 6 = 30 cm²",
                "basic"
            ),
            Formula(
                "Area of Circle",
                "Area = π × r²",
                "Calculate circular area",
                "Radius = 7cm\nArea = π × 7² = 154 cm² (π ≈ 22/7)",
                "basic"
            ),
            Formula(
                "Circumference of Circle",
                "Circumference = 2πr",
                "Calculate circle perimeter",
                "Radius = 14cm\nCircumference = 2 × π × 14 = 88cm",
                "basic"
            ),
            Formula(
                "Volume of Cube",
                "Volume = a³",
                "Calculate cube volume",
                "Side = 5cm\nVolume = 5³ = 125 cm³",
                "basic"
            ),
            Formula(
                "Volume of Cylinder",
                "Volume = πr²h",
                "Calculate cylindrical volume",
                "Radius = 3cm, Height = 10cm\nVolume = π × 3² × 10 = 90π cm³",
                "advanced"
            ),
            Formula(
                "Pythagorean Theorem",
                "a² + b² = c²",
                "Right triangle relationship",
                "Sides 3, 4: c² = 3² + 4² = 25\nc = 5",
                "basic"
            ),
            Formula(
                "Surface Area of Sphere",
                "Surface Area = 4πr²",
                "Calculate sphere surface area",
                "Radius = 7cm\nSA = 4 × π × 7² = 616 cm²",
                "advanced"
            )
        )
    }

    private fun getTrigonometryFormulas(): List<Formula> {
        return listOf(
            Formula(
                "Basic Ratios",
                "sin θ = Opposite/Hypotenuse\ncos θ = Adjacent/Hypotenuse\ntan θ = Opposite/Adjacent",
                "Fundamental trigonometric ratios",
                "In 3-4-5 triangle:\nsin θ = 3/5 = 0.6\ncos θ = 4/5 = 0.8\ntan θ = 3/4 = 0.75",
                "basic"
            ),
            Formula(
                "Pythagorean Identity",
                "sin²θ + cos²θ = 1",
                "Fundamental trigonometric identity",
                "If sin θ = 3/5, then:\ncos²θ = 1 - (3/5)² = 16/25\ncos θ = 4/5",
                "basic"
            ),
            Formula(
                "Complementary Angles",
                "sin(90° - θ) = cos θ\ncos(90° - θ) = sin θ",
                "Cofunction identities",
                "sin 30° = cos 60° = 1/2\ncos 30° = sin 60° = √3/2",
                "basic"
            ),
            Formula(
                "Standard Values",
                "sin 0° = 0, sin 30° = 1/2, sin 45° = √2/2, sin 60° = √3/2, sin 90° = 1",
                "Common angle values",
                "sin 45° = √2/2 ≈ 0.707\ncos 45° = √2/2 ≈ 0.707\ntan 45° = 1",
                "basic"
            ),
            Formula(
                "Sum Formulas",
                "sin(A + B) = sin A cos B + cos A sin B\ncos(A + B) = cos A cos B - sin A sin B",
                "Addition formulas",
                "sin(30° + 45°) = sin 30° cos 45° + cos 30° sin 45°",
                "advanced"
            ),
            Formula(
                "Double Angle",
                "sin 2θ = 2 sin θ cos θ\ncos 2θ = cos²θ - sin²θ",
                "Double angle formulas",
                "If θ = 30°, then:\nsin 60° = 2 sin 30° cos 30° = 2 × (1/2) × (√3/2) = √3/2",
                "advanced"
            )
        )
    }

    private fun getAverageFormulas(): List<Formula> {
        return listOf(
            Formula(
                "Simple Average",
                "Average = Sum of all values / Number of values",
                "Basic mean calculation",
                "Numbers: 10, 20, 30, 40\nAverage = (10+20+30+40)/4 = 25",
                "basic"
            ),
            Formula(
                "Weighted Average",
                "Weighted Average = Σ(weight × value) / Σ(weights)",
                "Average with different weights",
                "Marks: 80(3 credits), 90(2 credits)\nWA = (80×3 + 90×2)/(3+2) = 84",
                "advanced"
            ),
            Formula(
                "Average Speed",
                "Average Speed = Total Distance / Total Time",
                "Speed over entire journey",
                "100km in 2hrs, 150km in 3hrs\nAvg Speed = 250km/5hrs = 50 km/hr",
                "basic"
            ),
            Formula(
                "Combined Average",
                "Combined Average = (n₁×A₁ + n₂×A₂) / (n₁ + n₂)",
                "Average of two groups",
                "Group1: 20 students, avg 80\nGroup2: 30 students, avg 70\nCombined = (20×80 + 30×70)/50 = 74",
                "advanced"
            )
        )
    }

    private fun getTimeDistanceFormulas(): List<Formula> {
        return listOf(
            Formula(
                "Basic Formula",
                "Distance = Speed × Time\nSpeed = Distance / Time\nTime = Distance / Speed",
                "Fundamental relationship",
                "Speed = 60 km/hr, Time = 3 hrs\nDistance = 60 × 3 = 180 km",
                "basic"
            ),
            Formula(
                "Relative Speed",
                "Same Direction: Relative Speed = |S₁ - S₂|\nOpposite Direction: Relative Speed = S₁ + S₂",
                "Speed between two moving objects",
                "Car A: 80 km/hr, Car B: 60 km/hr\nSame direction: 20 km/hr\nOpposite: 140 km/hr",
                "advanced"
            ),
            Formula(
                "Average Speed Formula",
                "Average Speed = 2S₁S₂ / (S₁ + S₂)",
                "When equal distances at different speeds",
                "Half distance at 40 km/hr, half at 60 km/hr\nAvg = (2×40×60)/(40+60) = 48 km/hr",
                "advanced"
            ),
            Formula(
                "Time to Meet",
                "Time = Distance / Relative Speed",
                "Time for two objects to meet",
                "200km apart, speeds 30 & 70 km/hr (opposite)\nTime = 200/(30+70) = 2 hours",
                "basic"
            )
        )
    }

    private fun getProfitLossFormulas(): List<Formula> {
        return listOf(
            Formula(
                "Profit Formula",
                "Profit = Selling Price - Cost Price\nProfit% = (Profit/Cost Price) × 100",
                "Calculate profit and profit percentage",
                "CP = ₹100, SP = ₹120\nProfit = 120-100 = ₹20\nProfit% = (20/100)×100 = 20%",
                "basic"
            ),
            Formula(
                "Loss Formula",
                "Loss = Cost Price - Selling Price\nLoss% = (Loss/Cost Price) × 100",
                "Calculate loss and loss percentage",
                "CP = ₹150, SP = ₹120\nLoss = 150-120 = ₹30\nLoss% = (30/150)×100 = 20%",
                "basic"
            ),
            Formula(
                "Selling Price from Profit%",
                "SP = CP × (100 + Profit%) / 100",
                "Find selling price when profit% is known",
                "CP = ₹200, Profit% = 25%\nSP = 200 × (100+25)/100 = ₹250",
                "basic"
            ),
            Formula(
                "Cost Price from Profit%",
                "CP = SP × 100 / (100 + Profit%)",
                "Find cost price when profit% is known",
                "SP = ₹300, Profit% = 20%\nCP = 300 × 100/(100+20) = ₹250",
                "advanced"
            ),
            Formula(
                "Discount Formula",
                "Discount = Marked Price - Selling Price\nDiscount% = (Discount/MP) × 100",
                "Calculate discount on marked price",
                "MP = ₹500, SP = ₹400\nDiscount = 500-400 = ₹100\nDiscount% = (100/500)×100 = 20%",
                "basic"
            ),
            Formula(
                "Successive Discounts",
                "Final Price = MP × (100-d₁)/100 × (100-d₂)/100",
                "Multiple discounts applied",
                "MP = ₹1000, 20% then 10% discount\nFinal = 1000 × 0.8 × 0.9 = ₹720",
                "advanced"
            )
        )
    }

    private fun getSimpleInterestFormulas(): List<Formula> {
        return listOf(
            Formula(
                "Simple Interest",
                "SI = (P × R × T) / 100",
                "Basic simple interest calculation",
                "P = ₹1000, R = 10%, T = 2 years\nSI = (1000×10×2)/100 = ₹200",
                "basic"
            ),
            Formula(
                "Amount Formula",
                "Amount = Principal + Simple Interest\nA = P + (P × R × T)/100",
                "Total amount after interest",
                "P = ₹5000, R = 8%, T = 3 years\nSI = ₹1200, Amount = 5000+1200 = ₹6200",
                "basic"
            ),
            Formula(
                "Principal Formula",
                "P = (SI × 100) / (R × T)",
                "Find principal from known SI",
                "SI = ₹600, R = 12%, T = 2 years\nP = (600×100)/(12×2) = ₹2500",
                "advanced"
            ),
            Formula(
                "Rate Formula",
                "R = (SI × 100) / (P × T)",
                "Find rate from known values",
                "SI = ₹400, P = ₹2000, T = 2 years\nR = (400×100)/(2000×2) = 10%",
                "advanced"
            ),
            Formula(
                "Time Formula",
                "T = (SI × 100) / (P × R)",
                "Find time period from known values",
                "SI = ₹300, P = ₹1500, R = 10%\nT = (300×100)/(1500×10) = 2 years",
                "advanced"
            )
        )
    }

    private fun getNumberSystemFormulas(): List<Formula> {
        return listOf(
            Formula(
                "LCM Formula",
                "LCM = (a × b) / GCD(a,b)",
                "Least Common Multiple of two numbers",
                "Find LCM of 12 and 18:\nGCD(12,18) = 6\nLCM = (12×18)/6 = 36",
                "basic"
            ),
            Formula(
                "Prime Factorization",
                "Every number = Product of prime factors",
                "Express number as product of primes",
                "60 = 2² × 3¹ × 5¹\n= 4 × 3 × 5",
                "basic"
            ),
            Formula(
                "Divisibility Rules",
                "Divisible by 2: Last digit even\nDivisible by 3: Sum of digits divisible by 3",
                "Quick divisibility tests",
                "234: Last digit 4 (even) → divisible by 2\n2+3+4=9 → divisible by 3",
                "basic"
            ),
            Formula(
                "Perfect Square Test",
                "n² has odd number of factors",
                "Identify perfect squares",
                "36 = 2² × 3²\nFactors: 1,2,3,4,6,9,12,18,36 (9 factors)",
                "advanced"
            )
        )
    }

    private fun getStatisticsFormulas(): List<Formula> {
        return listOf(
            Formula(
                "Mean (Average)",
                "Mean = Σx / n",
                "Arithmetic mean of data",
                "Data: 2,4,6,8,10\nMean = (2+4+6+8+10)/5 = 6",
                "basic"
            ),
            Formula(
                "Median",
                "Median = Middle value when arranged in order",
                "Middle value of sorted data",
                "Data: 3,7,2,9,5 → Sorted: 2,3,5,7,9\nMedian = 5",
                "basic"
            ),
            Formula(
                "Mode",
                "Mode = Most frequently occurring value",
                "Most common value in data",
                "Data: 2,3,3,4,3,5\nMode = 3 (appears 3 times)",
                "basic"
            ),
            Formula(
                "Range",
                "Range = Maximum value - Minimum value",
                "Spread of data",
                "Data: 10,15,8,20,12\nRange = 20-8 = 12",
                "basic"
            ),
            Formula(
                "Standard Deviation",
                "σ = √[Σ(x-μ)² / n]",
                "Measure of data spread",
                "Shows how spread out data is from mean",
                "advanced"
            )
        )
    }

    private fun getProbabilityFormulas(): List<Formula> {
        return listOf(
            Formula(
                "Basic Probability",
                "P(Event) = Favorable outcomes / Total outcomes",
                "Fundamental probability formula",
                "Rolling a die, P(even) = 3/6 = 1/2\n(favorable: 2,4,6)",
                "basic"
            ),
            Formula(
                "Complement Rule",
                "P(not A) = 1 - P(A)",
                "Probability of opposite event",
                "P(not getting 6) = 1 - P(getting 6)\n= 1 - 1/6 = 5/6",
                "basic"
            ),
            Formula(
                "Addition Rule",
                "P(A or B) = P(A) + P(B) - P(A and B)",
                "Probability of either event",
                "Card: P(King or Heart) = 4/52 + 13/52 - 1/52 = 16/52",
                "advanced"
            ),
            Formula(
                "Multiplication Rule",
                "P(A and B) = P(A) × P(B|A)",
                "Probability of both events",
                "Two cards without replacement:\nP(both Aces) = 4/52 × 3/51 = 1/221",
                "advanced"
            ),
            Formula(
                "Conditional Probability",
                "P(B|A) = P(A and B) / P(A)",
                "Probability given condition",
                "P(Heart|Red card) = P(Heart and Red)/P(Red)\n= (13/52)/(26/52) = 1/2",
                "advanced"
            )
        )
    }

    private fun getMensurationFormulas(): List<Formula> {
        return listOf(
            Formula(
                "Rectangle Formulas",
                "Area = l × w\nPerimeter = 2(l + w)",
                "Rectangle measurements",
                "Length = 8m, Width = 5m\nArea = 40 m², Perimeter = 26m",
                "basic"
            ),
            Formula(
                "Square Formulas",
                "Area = a²\nPerimeter = 4a\nDiagonal = a√2",
                "Square measurements",
                "Side = 6m\nArea = 36 m², Perimeter = 24m, Diagonal = 6√2 m",
                "basic"
            ),
            Formula(
                "Circle Formulas",
                "Area = πr²\nCircumference = 2πr\nDiameter = 2r",
                "Circle measurements",
                "Radius = 7m\nArea = 154 m², Circumference = 44m",
                "basic"
            ),
            Formula(
                "Triangle Formulas",
                "Area = (1/2) × base × height\nHeron's formula: A = √[s(s-a)(s-b)(s-c)]",
                "Triangle area calculations",
                "Base = 10m, Height = 6m\nArea = 30 m²",
                "basic"
            ),
            Formula(
                "Cube Formulas",
                "Volume = a³\nSurface Area = 6a²\nDiagonal = a√3",
                "Cube measurements",
                "Side = 4m\nVolume = 64 m³, SA = 96 m²",
                "basic"
            ),
            Formula(
                "Cylinder Formulas",
                "Volume = πr²h\nCurved SA = 2πrh\nTotal SA = 2πr(r+h)",
                "Cylinder measurements",
                "r = 3m, h = 7m\nVolume = 198 m³, CSA = 132 m²",
                "advanced"
            ),
            Formula(
                "Cone Formulas",
                "Volume = (1/3)πr²h\nCurved SA = πrl\nTotal SA = πr(r+l)",
                "Cone measurements",
                "r = 4m, h = 3m, l = 5m\nVolume = 50.3 m³",
                "advanced"
            ),
            Formula(
                "Sphere Formulas",
                "Volume = (4/3)πr³\nSurface Area = 4πr²",
                "Sphere measurements",
                "Radius = 6m\nVolume = 904.8 m³, SA = 452.4 m²",
                "advanced"
            )
        )
    }

    private fun filterFormulas(filterType: String) {
        val allFormulas = getFormulasForTopic(topicType)
        val filteredFormulas = when (filterType) {
            "Basic" -> allFormulas.filter { it.category == "basic" }
            "Advanced" -> allFormulas.filter { it.category == "advanced" }
            "Examples" -> allFormulas // Show all with focus on examples
            else -> allFormulas
        }
        formulaAdapter.updateFormulas(filteredFormulas)
    }

    private fun showFormulaMenu() {
        val options = arrayOf("Download PDF", "Share Formulas", "Practice Quiz", "Bookmark")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Formula Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, "PDF download coming soon!", Toast.LENGTH_SHORT).show()
                    1 -> shareFormulas()
                    2 -> startPracticeQuiz()
                    3 -> Toast.makeText(this, "Bookmark feature coming soon!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun shareFormulas() {
        val formulas = getFormulasForTopic(topicType)
        val shareText = buildString {
            appendLine("$topicTitle Formulas - Math Shooter App")
            appendLine("=".repeat(40))
            formulas.forEach { formula ->
                appendLine()
                appendLine("${formula.name}:")
                appendLine("Formula: ${formula.formula}")
                appendLine("Example: ${formula.example}")
                appendLine("-".repeat(30))
            }
        }

        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "$topicTitle Formulas")
        }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Formulas"))
    }

    private fun startPracticeQuiz() {
        Toast.makeText(this, "Practice quiz feature coming soon!", Toast.LENGTH_SHORT).show()
        // Future: Navigate to a quiz activity based on the current topic
    }

    private fun getColorForTopic(): Int {
        return when (topicType) {
            "percentage" -> Color.parseColor("#FF5722")
            "algebra" -> Color.parseColor("#2196F3")
            "geometry" -> Color.parseColor("#4CAF50")
            "trigonometry" -> Color.parseColor("#9C27B0")
            "average" -> Color.parseColor("#FF9800")
            "time_distance" -> Color.parseColor("#00BCD4")
            "profit_loss" -> Color.parseColor("#E91E63")
            "simple_interest" -> Color.parseColor("#795548")
            "number_system" -> Color.parseColor("#607D8B")
            "statistics" -> Color.parseColor("#3F51B5")
            "probability" -> Color.parseColor("#009688")
            "mensuration" -> Color.parseColor("#CDDC39")
            else -> Color.parseColor("#FF5722")
        }
    }
}

// Data class for Formula
data class Formula(
    val name: String,
    val formula: String,
    val description: String,
    val example: String,
    val category: String // "basic", "advanced"
)