package com.example.livelinesalpha.ui.graph

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.livelinesalpha.databinding.FragmentGraphBinding
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.beust.klaxon.*
import com.example.livelinesalpha.R
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import org.jetbrains.anko.*
import java.net.URL
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

const val lowPos: Float = 1.003f

fun getJson(url: String): JsonObject? {
    return Parser().parse(StringBuilder(URL(url).readText())) as JsonObject
}

@RequiresApi(Build.VERSION_CODES.M)
fun applyCandleSetParams(context: Context, set: CandleDataSet): CandleDataSet {
    with(set) {
        axisDependency = AxisDependency.LEFT
        color = context.getColor(R.color.colorChartCandle)
        shadowColor = context.getColor(R.color.colorChartCandle)
        shadowWidth = 0.7f
        decreasingColor = context.getColor(R.color.colorChartDecreasing)
        decreasingPaintStyle = android.graphics.Paint.Style.FILL
        increasingColor = context.getColor(R.color.colorChartIncreasing)
        increasingPaintStyle = android.graphics.Paint.Style.FILL
        barSpace = 0.05f
        valueTextColor = context.getColor(R.color.colorChartIncreasing)
        neutralColor = context.getColor(R.color.colorChartNeutral)
        valueTextSize = 8f
        highLightColor = Color.TRANSPARENT
    }
    return set
}

@RequiresApi(Build.VERSION_CODES.M)
fun applyBarSetParams(context: Context, set: BarDataSet): BarDataSet {
    with(set) {
        color = context.getColor(R.color.colorChartBar)
        valueTextColor = context.getColor(R.color.colorChartBarText)
        valueTextSize = 8f
        axisDependency = AxisDependency.RIGHT
    }
    return set
}

//this is unused
fun applyLineSetParams(set: LineDataSet): LineDataSet {
    with(set) {
        setDrawHighlightIndicators(false)
        color = Color.TRANSPARENT
        valueTextColor = Color.TRANSPARENT
        valueTextSize = 8f
        setDrawCircles(false)
        setDrawCircleHole(false)
        axisDependency = AxisDependency.LEFT
        valueFormatter = object :
            DefaultValueFormatter((set.valueFormatter as DefaultValueFormatter).decimalDigits) {
            override fun getFormattedValue(
                value: Float,
                entry: Entry,
                dataSetIndex: Int,
                viewPortHandler: ViewPortHandler
            ): String {
                return super.getFormattedValue(value * lowPos, entry, dataSetIndex, viewPortHandler)
            }
        }
    }
    return set
}

class GraphFragment : Fragment(), ChoiceDialogListener {

    private var _binding: FragmentGraphBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var coinSpinner: Spinner
    private lateinit var currencySpinner: Spinner
    private lateinit var intervalSpinner: Spinner
    private lateinit var exchangeSpinner: Spinner
    private lateinit var chart: CombinedChart
    private lateinit var latest: TextView

    var coin: String? = null
    var currency: String? = null
    var exchange: String? = null
    var interval: String? = null

    private val histoVals = 300

    private lateinit var coins: Array<String>
    private lateinit var currencies: Array<String>
    val intervals = arrayOf("Minute", "Hour", "Day")

    var exchanges = arrayOf("None")


    private var timestamps: List<Int>? = listOf()

    @RequiresApi(Build.VERSION_CODES.M)
    fun updateExchanges() {
        doAsync {

            val jsonObj =
                getJson("https://min-api.cryptocompare.com/data/top/exchanges?fsym=$coin&tsym=$currency&limit=50")

            if (jsonObj?.string("Response") == "Success") {
                exchanges = (jsonObj.array<JsonObject>("Data")
                    ?.string("exchange")?.value as List<String>).toTypedArray()
                if (exchanges.isNotEmpty())
                    uiThread {
                        exchangeSpinner.adapter = ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            exchanges
                        )
                        exchange = exchanges[0]
                        loadChart()
                    }
                else {
                    uiThread {
                        chart.clear()
                    }
                }
            } else {
                uiThread {
                    chart.clear()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun loadLatest() {
        doAsync {
            val jsonObj =
                getJson("https://min-api.cryptocompare.com/data/price?fsym=$coin&tsyms=$currency&e=$exchange")
            if (jsonObj?.string("Response") == "Error")
                return@doAsync
            val last = jsonObj?.get(currency!!)

            uiThread {
                // latest.text = getString(R.string.text_latest,last,currency) //bad formatting, not enough formatting options
                latest.text = getString(R.string.text_latest) + " $last $currency"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun loadChart() {
        if (coin != null && currency != null && exchange != null && interval != null)
            doAsync {

                val jsonObj = getJson(
                    "https://min-api.cryptocompare.com/data/histo${
                        interval?.toLowerCase(Locale.ROOT)
                    }?fsym=$coin&tsym=$currency&e=$exchange&limit=$histoVals"
                )

                if (jsonObj?.string("Response") == "Success") {
                    val arr = (jsonObj.array<JsonObject>("Data") as JsonArray<JsonObject>)
//                    Log.d("DBG",arr.toString())

                    val barValues = List(arr.size) { i ->
                        BarEntry(
                            i.toFloat(),
                            (arr[i]["volumeto"] as Number).toFloat()
                        )
                    }


                    val candleValues = List(arr.size) { i ->
                        CandleEntry(
                            i.toFloat(),
                            (arr[i]["high"] as Number).toFloat(),
                            (arr[i]["low"] as Number).toFloat(),
                            (arr[i]["open"] as Number).toFloat(),
                            (arr[i]["close"] as Number).toFloat()
                        )
                    }

                    val lineValues = List(arr.size) { i ->
                        Entry(
                            i.toFloat(),
                            (arr[i]["low"] as Number).toFloat() / lowPos
                        )
                    }

                    timestamps = List(arr.size) { i -> (arr[i]["time"] as Int) }

                    val data = CombinedData()
                    data.setData(
                        BarData(
                            applyBarSetParams(
                                requireContext().applicationContext,
                                BarDataSet(barValues, getString(R.string.legendVolume))
                            )
                        )
                    )
                    data.setData(
                        CandleData(
                            applyCandleSetParams(
                                requireContext().applicationContext,
                                CandleDataSet(candleValues, getString(R.string.legendOHLC))
                            )
                        )
                    )
                    data.setData(LineData(applyLineSetParams(LineDataSet(lineValues, ""))))

                    loadLatest()

                    uiThread {
                        chart.resetTracking()
                        chart.xAxis.valueFormatter =
                            TimestampAxisValueFormatter(timestamps, interval)
                        chart.xAxis.granularity = 1f
                        chart.axisRight.axisMaximum = data.barData.yMax * 3
                        chart.data = data
                        chart.invalidate()
                    }
                } else {
                    uiThread {
                        chart.clear()
                    }
                }
            }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)

        val root: View = binding.root





        coinSpinner = binding.spinnerCoin
        currencySpinner = binding.spinnerCurrency
        intervalSpinner = binding.spinnerInterval
        exchangeSpinner = binding.spinnerExchange

        latest = binding.textLatest

        //coins = getCoinChoiceArray(this.requireContext())
        coins = arrayOf("BTC","ETH","BCH","XRP","LINK","LTC","EOS","BNB","FIL","TRX","BSV","YFI","ZEC","USDT","XMR","AAVE","ABBC","ADA","ALGO","ANKR","APM","ATOM","BAL","BAND","BAT","BKK","BTM","BTMX","BTT","CELO","CETH","COMP","CRO","CRV","DASH","DIA","DOGE","DOT","ETC","FTM","GTO","GXS","HIVE","HT","ICX","IOST","ITC","JST","KAVA","KCASH","KCS","KNC","KSM","LEND","LET","LRC","MIOTA","MKR","NEAR","NEO","NEST","NULS","OKB","OMG","ONT","PAX","PAY","QTUM","REN","RFR","RSR","SEELE","SNX","SRM","STORJ","SUSHI","SXP","THETA","THR","TOMO","TT","TUSD","UNI","USDC","VET","WAVES","WBTC","WICC","XEM","XLM","XTZ", "XVS","YFII","ZIL","ZRX")
        currencies = resources.getStringArray(R.array.currencies)

        val prefs = this.requireActivity().getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE)
        coin = prefs.getString(getString(R.string.pref_coin), coins[0])
        currency = prefs.getString(getString(R.string.pref_currency), currencies[0])
        interval = prefs.getString(getString(R.string.pref_interval), intervals[0])
        Log.d("DBG:", "$coin $currency $interval")

        chart = binding.chartCandle

        chart.setBackgroundColor(this.requireActivity().getColor(R.color.colorBackground))
        chart.setMaxVisibleValueCount(30)
        chart.description.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.axisLeft.gridColor = this.requireActivity().getColor(R.color.colorChartCandleText)
        chart.axisLeft.spaceBottom = 50f
        chart.axisLeft.textColor = this.requireActivity().getColor(R.color.colorChartCandleText)
        chart.axisRight.gridColor = this.requireActivity().getColor(R.color.colorChartBarText)
        chart.axisRight.textColor = this.requireActivity().getColor(R.color.colorChartBarText)
        chart.axisRight.spaceBottom = 0f
        chart.axisRight.spaceTop = 20f
        chart.legend.textColor = this.requireActivity().getColor(R.color.colorOnBackground)
        chart.drawOrder = arrayOf(
            DrawOrder.BAR,
            DrawOrder.BUBBLE,
            DrawOrder.CANDLE,
            DrawOrder.LINE,
            DrawOrder.SCATTER
        )

        coinSpinner.adapter = ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_list_item_1, coins)
        currencySpinner.adapter =
            ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_list_item_1, currencies)
        intervalSpinner.adapter =
            ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_list_item_1, intervals)
        exchangeSpinner.adapter =
            ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_list_item_1, exchanges)

        coinSpinner.setSelection(coins.indexOf(coin))
        currencySpinner.setSelection(currencies.indexOf(currency))
        intervalSpinner.setSelection(intervals.indexOf(interval))

        coinSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            @SuppressLint("CommitPrefEdits")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (coins[p2] == getString(R.string.coin_edit)) {
                    CoinChoiceDialog().show(parentFragmentManager, "coin")
                    return
                }
                if (coin != coins[p2]) {
                    coin = coins[p2]
                    with(this@GraphFragment.requireActivity().
                        getSharedPreferences(
                            getString(R.string.pref_file_key),
                            Context.MODE_PRIVATE
                        )?.edit()!!
                    ) {
                        putString(getString(R.string.pref_coin), coin)
                        apply()
                    }
                    if (currency != null)
                        updateExchanges()
                }
            }
        }


        currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            @SuppressLint("CommitPrefEdits")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (currency != currencies[p2]) {
                    currency = currencies[p2]
                    with(this@GraphFragment.requireActivity().
                        getSharedPreferences(
                            getString(R.string.pref_file_key),
                            Context.MODE_PRIVATE
                        )?.edit()!!
                    ) {
                        putString(getString(R.string.pref_currency), currency)
                        apply()
                    }
                    if (coin != null)
                        updateExchanges()
                }
            }
        }

        intervalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            @SuppressLint("CommitPrefEdits")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (interval != intervals[p2]) {
                    interval = intervals[p2]
                    with(this@GraphFragment.requireActivity().
                        getSharedPreferences(
                            getString(R.string.pref_file_key),
                            Context.MODE_PRIVATE
                        )?.edit()!!
                    ) {
                        putString(getString(R.string.pref_interval), interval)
                        apply()
                    }
                    loadChart()
                }
            }
        }

        exchangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                exchange = exchanges[p2]
                loadChart()
            }
        }

        updateExchanges()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        if (dialog::class == CoinChoiceDialog::class) {
            coins = getCoinChoiceArray(this.requireContext())
            coinSpinner.adapter =
                ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_list_item_1, coins)
        }

    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        TODO("Not yet implemented")
    }
}

private fun getCoinChoiceArray(context: Context): Array<String> {

    val coinSet: Set<String> =
        context.getSharedPreferences("Pref_Crptah", Context.MODE_PRIVATE)
            .getStringSet("Pref_Coins", setOf()) as Set<String>
    val coinArray: Array<String> = context.resources.getStringArray(R.array.coins_100)
    val coinList: MutableList<String> = coinArray.toMutableList()
    coinList.retainAll { it in coinSet }
    coinList.add("Edit...")
    return coinList.toTypedArray()
}


interface ChoiceDialogListener {
    fun onDialogPositiveClick(dialog: DialogFragment)
    fun onDialogNegativeClick(dialog: DialogFragment)
}
