package com.example.payday

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.text.DateFormatSymbols
import java.time.YearMonth
import java.util.*
import kotlin.collections.ArrayList
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.model.GradientColor
import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView


class GraphDetailsActivity : AppCompatActivity() {

    private val TAG = "GraphDetailsActivity"

    private lateinit var parentIntent: Intent
    private lateinit var operations: ArrayList<Operation>
    private lateinit var displayedOperations: ArrayList<Operation>
    private lateinit var reserves: ArrayList<Reserve>
    private lateinit var displayedReserves: ArrayList<Reserve>
    private lateinit var operationAdapter: OperationAdapter
    private lateinit var reserveAdapter: ReserveAdapter

    private lateinit var monthSpinner: Spinner
    private lateinit var typeSpinner: Spinner
    private lateinit var barChart: BarChart
    private lateinit var bottomOperationsSeparator: TextView
    private lateinit var bottomReservesSeparator: TextView
    private lateinit var operationsRV: ExpandableHeightListView
    private lateinit var reservesRV: ExpandableHeightListView

    private var numDaysInMonth: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph_details)

        supportActionBar?.hide()

        parentIntent = intent
        val symbol = parentIntent.getStringExtra("symbol")

        operations = ArrayList()
        reserves = ArrayList()
        displayedOperations = ArrayList()
        displayedReserves = ArrayList()

        operations = parentIntent.getSerializableExtra("operations") as ArrayList<Operation>
        reserves = parentIntent.getSerializableExtra("reserves") as ArrayList<Reserve>

        Log.d(TAG, "num reserves -> " + reserves.size)

        monthSpinner = findViewById(R.id.sp_month)
        typeSpinner = findViewById(R.id.sp_type)
        barChart = findViewById(R.id.bar_chart)
        bottomOperationsSeparator = findViewById(R.id.tv_bottom_spacer_operations)
        bottomReservesSeparator = findViewById(R.id.tv_bottom_spacer_reserves)
        reservesRV = findViewById(R.id.ehl_reserves)
        operationsRV = findViewById(R.id.ehl_operations)

        reserveAdapter = ReserveAdapter(this, displayedReserves, symbol, false)
        operationAdapter = OperationAdapter(this, displayedOperations, symbol)
        reservesRV.adapter = reserveAdapter
        operationsRV.adapter = operationAdapter
        reservesRV.isExpanded = true
        operationsRV.isExpanded = true
        operationsRV.divider = null
        operationsRV.dividerHeight = 0
        reservesRV.divider = null
        reservesRV.dividerHeight = 0

        numDaysInMonth = numOfDaysInMonth(0)

        val reserveDailyAmount = computeDailyReservesAmount(reserves, numDaysInMonth)
        setData(numDaysInMonth, reserveDailyAmount, "January", barChart)

        val monthArray = ArrayList<String>()
        val typeArray = ArrayList<String>()

        initArrays(monthArray, typeArray)

        val monthAdapter = ArrayAdapter<String>(this, R.layout.spinner_item, monthArray)
        val typeAdapter = ArrayAdapter<String>(this, R.layout.spinner_item, typeArray)

        monthSpinner.adapter = monthAdapter
        typeSpinner.adapter = typeAdapter

        changeData(0, "January")
        reserveAdapter.notifyDataSetChanged()

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                changeData(typeSpinner.selectedItemPosition, monthArray[p2])
                numDaysInMonth = numOfDaysInMonth(p2)
                if (typeArray[typeSpinner.selectedItemPosition].compareTo("Expense", true) == 0) {
                    val amount = computeDailyReservesAmount(displayedReserves, numDaysInMonth)
                    setData(numDaysInMonth, amount, monthArray[monthSpinner.selectedItemPosition], barChart)
                    barChart.invalidate()
                    reserveAdapter.notifyDataSetChanged()
                    operationsRV.visibility = View.GONE
                    bottomOperationsSeparator.visibility = View.GONE
                    reservesRV.visibility = View.VISIBLE
                    bottomReservesSeparator.visibility = View.VISIBLE
                }
                else {
                    val amount = computeDailyOperationsAmount(displayedOperations, numDaysInMonth)
                    setData(numDaysInMonth, amount, monthArray[monthSpinner.selectedItemPosition], barChart)
                    barChart.invalidate()
                    operationAdapter.notifyDataSetChanged()
                    operationsRV.visibility = View.VISIBLE
                    bottomOperationsSeparator.visibility = View.VISIBLE
                    reservesRV.visibility = View.GONE
                    bottomReservesSeparator.visibility = View.GONE
                }
            }

        }

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                changeData(p2, monthArray[monthSpinner.selectedItemPosition])
                if (p2 == 0) {
                    val amount = computeDailyReservesAmount(displayedReserves, numDaysInMonth)
                    setData(numDaysInMonth, amount, monthArray[monthSpinner.selectedItemPosition], barChart)
                    barChart.invalidate()
                    reserveAdapter.notifyDataSetChanged()
                    operationsRV.visibility = View.GONE
                    bottomOperationsSeparator.visibility = View.GONE
                    reservesRV.visibility = View.VISIBLE
                    bottomReservesSeparator.visibility = View.VISIBLE
                }
                else {
                    val amount = computeDailyOperationsAmount(displayedOperations, numDaysInMonth)
                    setData(numDaysInMonth, amount, monthArray[monthSpinner.selectedItemPosition], barChart)
                    barChart.invalidate()
                    operationAdapter.notifyDataSetChanged()
                    operationsRV.visibility = View.VISIBLE
                    bottomOperationsSeparator.visibility = View.VISIBLE
                    reservesRV.visibility = View.GONE
                    bottomReservesSeparator.visibility = View.GONE
                }
            }
        }

    }

    private fun initArrays(monthArray: ArrayList<String>, typeArray: ArrayList<String>) {
        typeArray.add("Expense")
        typeArray.add("Income")

        monthArray.add("January")
        monthArray.add("February")
        monthArray.add("March")
        monthArray.add("April")
        monthArray.add("May")
        monthArray.add("June")
        monthArray.add("July")
        monthArray.add("August")
        monthArray.add("September")
        monthArray.add("October")
        monthArray.add("November")
        monthArray.add("December")
    }

    private fun changeData(dataType: Int, month: String) {
        if (dataType == 0) {
            displayReserves(month)
        }
        else {
            displayOperations(month)
        }
    }

    private fun displayOperations(month: String) {
        displayedOperations.clear()
        Log.d(TAG, "selected month -> $month")
        for (index in 0 until operations.size) {
            val operation = operations[index]


            val reserveMonth = operation.date.split("/")[0]
            val monthName = getMonth(reserveMonth.toInt())
            Log.d(TAG, "operation month as int -> " + reserveMonth.toInt())
            Log.d(TAG, "operation month -> $monthName")
            if (month.compareTo(monthName, true) == 0) {
                displayedOperations.add(operation)
                Log.d(TAG, "operation date -> " + operation.date)
            }
        }
        Log.d(TAG, "displayed operations -> " + displayedOperations.size)
    }

    private fun displayReserves(month: String) {
        displayedReserves.clear()
        Log.d(TAG, "selected month -> $month")
        for (index in 0 until reserves.size) {
            val reserve = reserves[index]


            val reserveMonth = reserve.fullScheduledDate.split("/")[0]
            val monthName = getMonth(reserveMonth.toInt())
            Log.d(TAG, "reserve month as int -> " + reserveMonth.toInt())
            Log.d(TAG, "reserve month -> $monthName")
            if (month.compareTo(monthName, true) == 0) {
                displayedReserves.add(reserve)
                Log.d(TAG, "reserve scheduled date -> " + reserve.fullScheduledDate)
            }
        }
        Log.d(TAG, "displayed reserves -> " + displayedReserves.size)
    }

    private fun getMonth(month: Int): String {
        return DateFormatSymbols().months[month - 1]
    }

    private fun numOfDaysInMonth(index: Int): Int {
        val calendar = GregorianCalendar(2019, index, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun setData(numDays: Int, dailyAmount: ArrayList<Int>, month: String, chart: BarChart) {

        var values = ArrayList<BarEntry>()

        for (index in 0 until numDays) {
            val entry = BarEntry(index.toFloat() + 1, dailyAmount[index].toFloat())
            values.add(entry)
        }

        val set1: BarDataSet

        if (chart.data != null && chart.data.dataSetCount > 0) {
            set1 = chart.data.getDataSetByIndex(0) as BarDataSet
            set1.values = values
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()

        } else {
            set1 = BarDataSet(values, "Month $month")

            set1.setDrawIcons(false)

            val startColor1 = ContextCompat.getColor(this, android.R.color.holo_orange_light)
            val startColor2 = ContextCompat.getColor(this, android.R.color.holo_blue_light)
            val startColor3 = ContextCompat.getColor(this, android.R.color.holo_orange_light)
            val startColor4 = ContextCompat.getColor(this, android.R.color.holo_green_light)
            val startColor5 = ContextCompat.getColor(this, android.R.color.holo_red_light)
            val endColor1 = ContextCompat.getColor(this, android.R.color.holo_blue_dark)
            val endColor2 = ContextCompat.getColor(this, android.R.color.holo_purple)
            val endColor3 = ContextCompat.getColor(this, android.R.color.holo_green_dark)
            val endColor4 = ContextCompat.getColor(this, android.R.color.holo_red_dark)
            val endColor5 = ContextCompat.getColor(this, android.R.color.holo_orange_dark)

            val gradientColors = ArrayList<GradientColor>()
            gradientColors.add(GradientColor(startColor1, endColor1))
            gradientColors.add(GradientColor(startColor2, endColor2))
            gradientColors.add(GradientColor(startColor3, endColor3))
            gradientColors.add(GradientColor(startColor4, endColor4))
            gradientColors.add(GradientColor(startColor5, endColor5))

            set1.gradientColors = gradientColors

            val dataSets = ArrayList<BarDataSet>()
            dataSets.add(set1)

            val data = BarData(dataSets as List<IBarDataSet>?)
            data.setValueTextSize(10f)
            data.barWidth = 0.9f

            data.setDrawValues(true)
            chart.data = data
        }
    }

    private fun computeDailyOperationsAmount(operations: ArrayList<Operation>, numDaysInMonth: Int): ArrayList<Int> {
        val dailyAmount = ArrayList<Int>()

        for (index in 0 until numDaysInMonth) {
            dailyAmount.add(0)
        }

        Log.d(TAG, "values in dailyAmount -> " + dailyAmount.size)

        for (operation in operations) {
            val day = operation.date.split("/")[1].toInt()
            val amountForDay = dailyAmount[day - 1] + operation.amount
            dailyAmount[day - 1] = amountForDay

            Log.d(TAG, "Amount for day " + day + " is -> " + dailyAmount[day - 1])
        }

        return dailyAmount
    }

    private fun computeDailyReservesAmount(reserves: ArrayList<Reserve>, numDaysInMonth: Int): ArrayList<Int> {
        val dailyAmount = ArrayList<Int>()

        for (index in 0 until numDaysInMonth) {
            dailyAmount.add(0)
        }

        Log.d(TAG, "values in dailyAmount -> " + dailyAmount.size)

        for (reserve in reserves) {
            val day = reserve.fullScheduledDate.split("/")[1].toInt()
            val amountForDay = dailyAmount[day - 1] + reserve.actualAmount
            dailyAmount[day - 1] = amountForDay
        }

        return dailyAmount
    }
}
