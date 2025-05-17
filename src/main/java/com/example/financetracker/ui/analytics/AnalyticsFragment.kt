package com.example.financetracker.ui.analytics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financetracker.R
import com.example.financetracker.databinding.FragmentAnalyticsBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.NumberFormat
import java.util.Locale

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AnalyticsViewModel
    private lateinit var budgetAdapter: BudgetCategoryAdapter
    
    private lateinit var incomeVsExpenseChart: PieChart
    private lateinit var categorySpendingChart: PieChart
    private lateinit var monthlyOverviewChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[AnalyticsViewModel::class.java]
        
        // Initialize budget adapter
        budgetAdapter = BudgetCategoryAdapter()
        binding.rvBudgetsByCategory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetAdapter
            isNestedScrollingEnabled = false
        }
        
        // Initialize charts
        setupCharts()
        
        // Setup listeners
        setupTimeRangeSelection()
        
        // Observe ViewModel data
        observeViewModel()
    }
    
    private fun setupTimeRangeSelection() {
        // Setup time range chip group listeners
        binding.chipGroupTimeRange.setOnCheckedChangeListener { _, checkedId ->
            val timeRange = when(checkedId) {
                R.id.chipThisMonth -> AnalyticsViewModel.TimeRange.THIS_MONTH
                R.id.chipLastMonth -> AnalyticsViewModel.TimeRange.LAST_MONTH
                R.id.chipLast3Months -> AnalyticsViewModel.TimeRange.LAST_3_MONTHS
                R.id.chipLast6Months -> AnalyticsViewModel.TimeRange.LAST_6_MONTHS
                R.id.chipThisYear -> AnalyticsViewModel.TimeRange.THIS_YEAR
                else -> AnalyticsViewModel.TimeRange.THIS_MONTH
            }
            viewModel.setTimeRange(timeRange)
        }
    }
    
    private fun setupCharts() {
        // Setup Income vs Expense Chart
        incomeVsExpenseChart = PieChart(requireContext())
        binding.chartIncomeVsExpense.addView(incomeVsExpenseChart)
        setupPieChart(incomeVsExpenseChart, "Income vs Expense")
        
        // Setup Category Spending Chart
        categorySpendingChart = PieChart(requireContext())
        binding.chartCategorySpending.addView(categorySpendingChart)
        setupPieChart(categorySpendingChart, "Spending by Category")
        
        // Setup Monthly Overview Chart
        monthlyOverviewChart = BarChart(requireContext())
        binding.chartMonthlyOverview.addView(monthlyOverviewChart)
        setupBarChart(monthlyOverviewChart)
    }
    
    private fun setupPieChart(chart: PieChart, title: String) {
        chart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            legend.isEnabled = true
            legend.textSize = 12f
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
            setCenterText(title)
            setCenterTextSize(16f)
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            setExtraOffsets(20f, 0f, 20f, 0f)
        }
    }
    
    private fun setupBarChart(chart: BarChart) {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            
            val xAxis = getXAxis()
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setCenterAxisLabels(true)
            xAxis.setDrawGridLines(false)
            
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            
            extraBottomOffset = 10f
            setFitBars(true)
        }
    }
    
    private fun observeViewModel() {
        // Observe budget data
        viewModel.budgetVsActual.observe(viewLifecycleOwner) { budgetList ->
            updateBudgetReports(budgetList)
        }
        
        // Observe category spending
        viewModel.categorySpending.observe(viewLifecycleOwner) { categorySpending ->
            updateCategorySpendingChart(categorySpending)
        }
        
        // Observe income and expenses
        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            viewModel.totalExpenses.observe(viewLifecycleOwner) { expenses ->
                updateIncomeVsExpenseChart(income, expenses)
            }
        }
        
        // Observe monthly data
        viewModel.monthlyData.observe(viewLifecycleOwner) { monthlyData ->
            updateMonthlyOverviewChart(monthlyData)
        }
    }
    
    private fun updateBudgetReports(budgetList: List<AnalyticsViewModel.BudgetVsActual>) {
        if (budgetList.isEmpty()) {
            binding.cardBudgetReports.visibility = View.GONE
            return
        }
        
        binding.cardBudgetReports.visibility = View.VISIBLE
        
        // Find the overall budget (or use income as virtual budget)
        val overallBudget = budgetList.find { it.budget.category == null }
        
        if (overallBudget != null) {
            // Update overall budget summary
            binding.tvTotalBudget.text = formatCurrency(overallBudget.budget.amount)
            binding.tvTotalSpent.text = formatCurrency(overallBudget.actualSpent)
            binding.tvTotalRemaining.text = formatCurrency(overallBudget.remaining)
            
            // Update progress bar
            binding.progressOverallBudget.progress = overallBudget.percentageUsed
            
            // Update color based on budget status
            val progressColor = when {
                overallBudget.percentageUsed >= 100 -> R.color.expense_red
                overallBudget.percentageUsed >= 80 -> R.color.warning_yellow
                else -> R.color.income_green
            }
            binding.progressOverallBudget.progressTintList = resources.getColorStateList(progressColor, null)
        }
        
        // Update category budgets
        val categoryBudgets = budgetList.filter { it.budget.category != null }
        budgetAdapter.submitList(categoryBudgets)
    }
    
    private fun updateCategorySpendingChart(categorySpending: Map<com.example.financetracker.model.Category, Double>) {
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()
        
        categorySpending.forEach { (category, amount) ->
            if (amount > 0) {
                entries.add(PieEntry(amount.toFloat(), category.displayName))
                colors.add(getRandomColor())
            }
        }
        
        val dataSet = PieDataSet(entries, "Categories")
        dataSet.colors = colors
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK
        dataSet.sliceSpace = 3f
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.valueLinePart1Length = 0.3f
        dataSet.valueLinePart2Length = 0.4f
        dataSet.valueLineColor = Color.GRAY
        dataSet.valueLineWidth = 1f
        
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val format = NumberFormat.getPercentInstance()
                return format.format(value / 100)
            }
        })
        
        categorySpendingChart.data = pieData
        categorySpendingChart.invalidate()
    }
    
    private fun updateIncomeVsExpenseChart(income: Double, expenses: Double) {
        val entries = ArrayList<PieEntry>()
        
        if (income > 0) {
            entries.add(PieEntry(income.toFloat(), "Income"))
        }
        
        if (expenses > 0) {
            entries.add(PieEntry(expenses.toFloat(), "Expenses"))
        }
        
        val dataSet = PieDataSet(entries, "Income vs Expenses")
        dataSet.colors = listOf(
            resources.getColor(R.color.income_green, null),
            resources.getColor(R.color.expense_red, null)
        )
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK
        dataSet.sliceSpace = 3f
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.valueLinePart1Length = 0.3f
        dataSet.valueLinePart2Length = 0.4f
        dataSet.valueLineColor = Color.GRAY
        dataSet.valueLineWidth = 1f
        
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val total = income + expenses
                val percentage = if (total > 0) (value / total.toFloat()) * 100f else 0f
                val format = NumberFormat.getPercentInstance()
                return format.format(percentage / 100)
            }
        })
        
        incomeVsExpenseChart.data = pieData
        incomeVsExpenseChart.invalidate()
    }
    
    private fun updateMonthlyOverviewChart(monthlyData: List<AnalyticsViewModel.MonthlyData>) {
        val incomeEntries = ArrayList<BarEntry>()
        val expenseEntries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        
        monthlyData.forEachIndexed { index, data ->
            incomeEntries.add(BarEntry(index.toFloat(), data.income.toFloat()))
            expenseEntries.add(BarEntry(index.toFloat(), data.expenses.toFloat()))
            labels.add(data.monthName)
        }
        
        val incomeDataSet = BarDataSet(incomeEntries, "Income")
        incomeDataSet.color = resources.getColor(R.color.income_green, null)
        
        val expenseDataSet = BarDataSet(expenseEntries, "Expenses")
        expenseDataSet.color = resources.getColor(R.color.expense_red, null)
        
        val barData = BarData(incomeDataSet, expenseDataSet)
        barData.barWidth = 0.3f
        
        monthlyOverviewChart.data = barData
        monthlyOverviewChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        monthlyOverviewChart.groupBars(-0.5f, 0.25f, 0.05f)
        monthlyOverviewChart.invalidate()
    }
    
    private fun formatCurrency(amount: Double): String {
        return "Rs ${String.format("%.2f", amount)}"
    }
    
    private fun getRandomColor(): Int {
        val colors = ColorTemplate.MATERIAL_COLORS.toList() + 
                     ColorTemplate.COLORFUL_COLORS.toList() +
                     ColorTemplate.JOYFUL_COLORS.toList()
        return colors.random()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 