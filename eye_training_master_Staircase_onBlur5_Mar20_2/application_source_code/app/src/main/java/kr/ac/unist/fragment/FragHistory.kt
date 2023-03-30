package kr.ac.unist.fragment

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.ac.unist.R
import kr.ac.unist.custom.EyeScreenView
import kr.ac.unist.database.DbManager
import kr.ac.unist.database.entity.TestTrial
import kr.ac.unist.database.entity.TrainTrial
import kr.ac.unist.database.entity.User
import kr.ac.unist.databinding.FragHistoryBinding
import kr.ac.unist.manager.SharedPrefManager
import kr.ac.unist.util.Util
import timber.log.Timber
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap


/**
* 훈련 기록 Screen
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragHistory : Fragment() {

    companion object {

        val TOTAL_TEST_SCOPE_COUNT = 5
        val DISPLAY_BAR_COUNT = 10

        val MAX_DAY_VALUE : Float = 1f        // 시력 훈련 Day 최대 값
        val MAX_TRAIN_VALUE : Float = 1f        // 시력 훈련 Block 최대 값
        val MAX_TEST_VALUE : Float = 2f         // 시력 진단 최대 값


        /**
         * 시력 진단 계산
         * @author 임성진
         * @version 1.0.0
         * @since 2021-08-09 오후 2:01
         **/
        fun calculatorTest(latestInfo : List<TestTrial>) : ArrayList<Object> {

            var data: TestTrial
            var changeCount: Int = 0
            var isChange : Boolean
            var cal: Calendar
            var blockScore: HashMap<String, Float> = HashMap<String, Float>()
            var totalCount: HashMap<String, Float> = HashMap<String, Float>()
            var dateList: HashMap<String, String> = HashMap<String, String>()
            var block: String
            var scope: Float = 0.0f

            var beforeState : Int = -1   // -1 : first, 0 : up , 1 down
            var currentState : Int = -1   // -1 : first, 0 : up , 1 down
            var size = latestInfo.size - 1

            for (i in size downTo 0) {
                data = latestInfo.get(i)

                cal = Calendar.getInstance()
                cal.timeInMillis = data.instDtm

                block = data.block

                scope = 0.0f
                isChange = false

                // 변화 감지
                if (size  > i) {
                    if (latestInfo.get(i+1).degree.toFloat() > data.degree.toFloat()) {
                        currentState = 1 // down
                        isChange = true
                    } else if (latestInfo.get(i+1).degree.toFloat() < data.degree.toFloat()) {
                        currentState = 0 // up
                        isChange = true
                    }
                }

                if (isChange && changeCount < TOTAL_TEST_SCOPE_COUNT) {

                    if (beforeState != -1 && beforeState != currentState) {
                        scope += (data.degree.toFloat())
                        changeCount += 1
                    }
                }

                beforeState = currentState

                if (blockScore.containsKey(block) && scope > 0) {
                    blockScore.put(
                        block,
                        blockScore.get(block)!! + scope
                    )
                    totalCount.put(block, totalCount.get(block)!! + 1)
                } else {
                    if (!blockScore.containsKey(block)) {
                        blockScore.put(block, scope)

                        if (scope > 0) {
                            totalCount.put(block, 1.0f)
                        } else {
                            totalCount.put(block, 0.0f)
                        }
                        changeCount = 0

                        dateList.put(block, String.format("%02d", cal.get(Calendar.MONTH) + 1) + "."+ String.format("%02d", cal.get(Calendar.DATE)))
                    }
                }

            }

            var returnObject : ArrayList<Object> = ArrayList<Object>()
            returnObject.add(blockScore as Object)
            returnObject.add(totalCount as Object)
            returnObject.add(dateList as Object)

            return returnObject
        }

    }

    // 그래프 개수
    val GRAPH_COUNT = 3

    // 바인딩
    lateinit var binding: FragHistoryBinding

    private lateinit var progressDialog: AppCompatDialog
    
    // 그려진 횟수
    private var finishDrawCount : Int = 0

    // layout data 사용
    val maxDayValue : String = Math.round(MAX_DAY_VALUE).toString()        // 시력 훈련 Day 최대 값
    val maxTrainValue : String = Math.round(MAX_TRAIN_VALUE).toString()      // 시력 훈련 Block 최대 값
    val maxTestValue : String = Math.round(MAX_TEST_VALUE).toString()       // 시력 진단 최대 값

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Util.settingFullScreen(requireActivity(), false)
        
        // 바인딩
        this.binding = DataBindingUtil.inflate<FragHistoryBinding>(
                inflater, R.layout.frag_history, container, false)

        this.binding.frag = this

        // 프로그래스바 보이기
        showProgressbar()

        // 초기화
        init()

        // 차트에 데이터 넣기
        inputDayChartData()
        inputBlockChartData()
        inputTestBlockChartData()

        return this.binding.root
    }

    /**
    * 프로그래스바 보이기
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-17 오후 1:11
    **/
    fun showProgressbar() {
        this.progressDialog = AppCompatDialog(requireContext())
        this.progressDialog.setCancelable(false)
        this.progressDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.progressDialog.setContentView(R.layout.dialog_progressbar)
        this.progressDialog.show()
        var progressbar : ProgressBar? = this.progressDialog.findViewById<ProgressBar>(R.id.pb_progress)
        val filter: ColorFilter = PorterDuffColorFilter(ContextCompat.getColor(requireContext(), R.color.rgb_6200ee), PorterDuff.Mode.SRC_IN)
        progressbar!!.indeterminateDrawable.colorFilter = filter
    }

    /**
    * 프로그래스바 숨기기
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-17 오후 1:13
    **/
    fun hideProgressbar(){
        GlobalScope.launch(Dispatchers.Main) {
            if (finishDrawCount >= GRAPH_COUNT) {
                if (::progressDialog.isInitialized && progressDialog != null && progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
            }
        }
    }

    /**
     * 초기화
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오후 3:19
     **/
    fun init(){

        GlobalScope.launch {
            var user : List<User> = DbManager.getInstance().userDao().findUser(SharedPrefManager.getUserSeqId())
            binding!!.tvName.text = String.format(getString(R.string.user_name, user.get(0).userId))
        }


        this.finishDrawCount = 0
        
        // day chart init
        initChart(this.binding.bcChartDay, 0f, MAX_DAY_VALUE)

        // block chart init
        initChart(this.binding.bcChartBlock,0f, MAX_TRAIN_VALUE)

        // test block chart init
        initChart(this.binding.bcTestChartBlock,0f, MAX_TEST_VALUE)
    }

    /**
     * 초기화
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오후 3:19
     **/
    fun initChart(barChart : BarChart, minValue : Float, maxValue : Float){

        // day chart init
        barChart.setNoDataText(getString(R.string.msg_no_chart_data))
        barChart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.rgb_6200ee))

        barChart.description.isEnabled = true;
        barChart.setMaxVisibleValueCount(100)
        barChart.setDrawGridBackground(false)
        barChart.isDoubleTapToZoomEnabled = false
        barChart.description = null
        barChart.setDrawValueAboveBar(false)
        barChart.setTouchEnabled(false)
        barChart.setPinchZoom(false)
        barChart.legend.isEnabled = false

        var xAxis: XAxis = barChart.getXAxis()
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)
        xAxis.setDrawAxisLine(false)
        xAxis.axisMinimum = 0f;
        xAxis.setCenterAxisLabels(true)
        xAxis.textSize = 12f
        xAxis.typeface = Typeface.DEFAULT_BOLD

        var leftAxis: YAxis = barChart.getAxisLeft()
        leftAxis.setLabelCount(10, false)
        leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawAxisLine(false)
        leftAxis.axisMinimum = minValue
        leftAxis.axisMaximum = maxValue

        var rightAxis: YAxis = barChart.getAxisRight()
        rightAxis.isEnabled = false

    }



    /**
     * Day 차트에 데이터 넣기
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오후 3:19
     **/
    fun inputDayChartData(){

        GlobalScope.launch {

            //var latestInfo : List<TrainTrial> = DbManager.getLatestTrainTrial(SharedPrefManager.getUserSeqId())
            var latestInfo : List<TrainTrial> = DbManager.getInstance().processDao().selectTrainTrialInfo(SharedPrefManager.getUserSeqId())

            if (latestInfo.size > 0) {

                var data: TrainTrial
                var cal: Calendar
                var totalScore: LinkedHashMap<String, Int> = LinkedHashMap<String, Int>()
                var totalCount: HashMap<String, Int> = HashMap<String, Int>()
                var date: String
                var scope: Int = 0

                for (i in 0 .. latestInfo.size - 1) {
                    data = latestInfo.get(i)

                    cal = Calendar.getInstance()
                    cal.timeInMillis = data.instDtm

                    date = String.format("%02d", cal.get(Calendar.MONTH) + 1) + "."+ String.format("%02d", cal.get(Calendar.DATE))

                    scope = 0
                    if (data.correct.equals("T")) {
                        scope = Util.calculatorResult(
                            (data.distance.toFloat() / 10f),
                            (data.size.toFloat())
                        )
                    }

                    if (totalScore.containsKey(date)) {
                        totalScore.put(
                            date,
                            totalScore.get(date)!! + scope
                        )

                        if (scope > 0) {
                            totalCount.put(date, totalCount.get(date)!! + 1)
                        }
                    } else {
                        totalScore.put(date, scope)
                        if (scope > 0) {
                            totalCount.put(date, 1)
                        } else {
                            totalCount.put(date, 0)
                        }
                    }

                }

                // day chart 데이터 넣기
                var dayChart = binding.bcChartDay

                var values: ArrayList<BarEntry> = ArrayList()
                var labelList : ArrayList<String> = ArrayList()
                var labels : List<String>
                var count: Int = 0
                var temp : Float = 0f
                var displayBarCount : Int = 0
                var set = totalScore.keys.sortedDescending()
                var maxValueMap = totalScore.maxByOrNull { it.value }
                var maxValue : Float = maxValueMap.let { it!!.value.toFloat() }
                var maxValueCount : Int? = totalCount.get(maxValueMap!!.key)
                var maxAvg = maxValue / maxValueCount!!
                var calValue : Float

                if (DISPLAY_BAR_COUNT > set.size) {
                    displayBarCount = set.size
                } else {
                    displayBarCount = DISPLAY_BAR_COUNT
                }

                // 첫 번째 블록만 있거나 첫 번째 날의 결과만 있다면 이 값은 꽉 찬 1로 보여줄것
                if (set.size == 1) {
                    values.add(BarEntry(((displayBarCount - count)-1).toFloat(), MAX_DAY_VALUE))
                    labelList.add(set.get(0))
                } else {
                    for (i in 0..set.size - 1) {
                        temp = totalScore.get(set.get(i))!!.toFloat() / totalCount.get(set.get(i))!!
                            .toFloat()
                        if (temp.isNaN()) temp = 0f

                        // (M - 평균인덱스) / M
                        calValue  = (maxAvg - temp) / maxAvg

                        // 0점이라고 하더라도 막대를 default로 작게 그려기
                        if (calValue < 0.1f) calValue = 0.1f
                        
                        values.add(
                            BarEntry(
                                ((displayBarCount - count) - 1).toFloat(),
                                calValue
                            )
                        )

                        labelList.add(set.get(i))

                        count += 1

                        if (displayBarCount <= count) break;

                    }
                }

                labels = labelList.sorted()

                GlobalScope.launch(Dispatchers.Main) {
                    // 데이터 설정
                    setInputChartData(values, labels, dayChart)
                }
            } else {
                finishDrawCount+=1
                hideProgressbar()
            }

        }


    }

    /**
     * Block 차트에 데이터 넣기
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오후 3:19
     **/
    fun inputBlockChartData(){

        GlobalScope.launch {

            var latestInfo : List<TrainTrial> = DbManager.getTrainTrial(SharedPrefManager.getUserSeqId())

            if (latestInfo.size > 0) {

                var data: TrainTrial
                var blockScore: LinkedHashMap<String, Int> = LinkedHashMap<String, Int>()
                var totalCount: HashMap<String, Int> = HashMap<String, Int>()
                var block: String
                var scope: Int = 0

                for (i in 0..latestInfo.size - 1) {
                    data = latestInfo.get(i)

                    block = data.block

                    scope = 0
                    if (data.correct.equals("T")) {
                        // wave length : 1/(spatial frequency) x (자극의 픽셀 사이즈 / 6visual angle)
                        scope = Util.calculatorResult(
                            (data.distance.toFloat() / 10f),
                            1/data.cycle.toFloat() * (data.size.toFloat() / EyeScreenView.TRAIN_VISUAL_ANGLE_DEGREE))
                    }

                    if (blockScore.containsKey(block)) {
                        blockScore.put(
                            block,
                            blockScore.get(block)!! + scope
                        )

                        if (scope > 0) {
                            totalCount.put(block, totalCount.get(block)!! + 1)
                        }
                    } else {

                        blockScore.put(block, scope)

                        if (scope > 0) {
                            totalCount.put(block, 1)
                        } else {
                            totalCount.put(block, 0)
                        }
                    }
                }

                // day chart 데이터 넣기
                var blockChart = binding.bcChartBlock

                var values: ArrayList<BarEntry> = ArrayList()
                var labelList : ArrayList<String> = ArrayList()
                var labels : List<String>
                var count: Int = 0
                var temp : Float = 0f
                var maxValueMap = blockScore.maxByOrNull { it.value }

                maxValueMap.let {

                    var maxValue : Float = maxValueMap.let { it!!.value.toFloat() }
                    var maxValueCount : Int? = totalCount.get(maxValueMap!!.key)
                    var maxAvg = maxValue / maxValueCount!!
                    var set = blockScore.keys.sortedByDescending { it.toInt() }
                    var displayBarCount : Int = 0
                    var calValue : Float

                    if (DISPLAY_BAR_COUNT > set.size) {
                        displayBarCount = set.size
                    } else {
                        displayBarCount = DISPLAY_BAR_COUNT
                    }

                    // 첫 번째 블록만 있거나 첫 번째 날의 결과만 있다면 이 값은 꽉 찬 1로 보여줄것
                    if (set.size == 1) {
                        values.add(BarEntry(((displayBarCount - count)-1).toFloat(), MAX_DAY_VALUE))
                        labelList.add(set.get(0))
                    } else {
                        for (i in 0..set.size - 1) {
                            temp = blockScore.get(set.get(i))!!.toFloat() / totalCount.get(set.get(i))!!
                                .toFloat()

                            if (temp.isNaN()) temp = 0f

                            // (M - 평균인덱스) / M
                            calValue  = (maxAvg - temp) / maxAvg

                            // 0점이라고 하더라도 막대를 default로 작게 그려기
                            if (calValue < 0.1f) calValue = 0.1f

                            // (M - 각 블록 평균 인덱스)/ M
                            values.add(
                                BarEntry(
                                    ((displayBarCount - count) - 1).toFloat(),
                                    calValue
                                )
                            )
                            labelList.add(set.get(i))
                            count += 1

                            if (displayBarCount <= count) break;
                        }
                    }
                    labels = labelList.sortedBy { it.toInt() }
                }

                // 데이터 설정
                GlobalScope.launch(Dispatchers.Main) {

                    if (latestInfo.size > 0) {

                        var startCal: Calendar
                        var endCal: Calendar

                        startCal = Calendar.getInstance()
                        startCal.timeInMillis = latestInfo.get(0).instDtm

                        endCal = Calendar.getInstance()
                        endCal.timeInMillis = latestInfo.get(latestInfo.size-1).instDtm

                        var start = String.format("%02d", startCal.get(Calendar.MONTH) + 1) + "."+ String.format("%02d", startCal.get(Calendar.DATE))
                        var end = String.format("%02d", endCal.get(Calendar.MONTH) + 1) + "."+ String.format("%02d", endCal.get(Calendar.DATE))

                        binding.tvTrainFirstDate.text = start
                        binding.tvTrainLastDate.text = end

                    }

                    setInputChartData(values, labels, blockChart)
                }

            } else {
                finishDrawCount+=1
                hideProgressbar()
            }

        }

    }

    /**
     * Test Block 차트에 데이터 넣기
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오후 3:19
     **/
    fun inputTestBlockChartData(){

        GlobalScope.launch {

            var latestInfo : List<TestTrial> = DbManager.getTestTrial(SharedPrefManager.getUserSeqId())

            if (latestInfo.size > 0) {

                var returnData : ArrayList<Object> = calculatorTest(latestInfo)

                var blockScore: HashMap<String, Float> = returnData.get(0) as HashMap<String, Float>
                var totalCount: HashMap<String, Float> = returnData.get(1) as HashMap<String, Float>

                // test chart 데이터 넣기
                var testChart = binding.bcTestChartBlock

                var values: ArrayList<BarEntry> = ArrayList()
                var labelList : ArrayList<String> = ArrayList()
                var labels : List<String>
                var count: Int = 0
                var set = blockScore.keys.sortedByDescending { it.toInt() }
                var displayBarCount : Int = 0

                if (DISPLAY_BAR_COUNT > set.size) {
                    displayBarCount = set.size
                } else {
                    displayBarCount = DISPLAY_BAR_COUNT
                }

                for (i in 0 .. set.size -1) {
                    if (totalCount.get(set.get(i))!!.toFloat() > 0) {
                        values.add(
                            BarEntry(
                                ((displayBarCount - count)-1).toFloat(),
                                MAX_TEST_VALUE-(blockScore.get(set.get(i))!!.toFloat() / totalCount.get(set.get(i))!!.toFloat())
                            )
                        )
                    } else {
                        values.add(
                            BarEntry(
                                ((displayBarCount - count)-1).toFloat(),
                                MAX_TEST_VALUE
                            )
                        )
                    }
                    labelList.add(set.get(i))
                    count += 1

                    if (displayBarCount <= count) break;
                }

                labels = labelList.sortedBy { it.toInt() }

                // 데이터 설정
                GlobalScope.launch(Dispatchers.Main) {

                    if (latestInfo.size > 0) {

                        var startCal: Calendar
                        var endCal: Calendar

                        startCal = Calendar.getInstance()
                        startCal.timeInMillis = latestInfo.get(0).instDtm

                        endCal = Calendar.getInstance()
                        endCal.timeInMillis = latestInfo.get(latestInfo.size-1).instDtm

                        var start = String.format("%02d", startCal.get(Calendar.MONTH) + 1) + "."+ String.format("%02d", startCal.get(Calendar.DATE))
                        var end = String.format("%02d", endCal.get(Calendar.MONTH) + 1) + "."+ String.format("%02d", endCal.get(Calendar.DATE))

                        binding.tvTestFirstDate.text = start
                        binding.tvTestLastDate.text = end

                    }

                    setInputChartData(values, labels, testChart)
                }

            } else {
                finishDrawCount+=1
                hideProgressbar()
            }

        }

    }

    /**
    * chart 데이터 설정
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-20 오후 4:50
    **/
    fun setInputChartData(values : ArrayList<BarEntry>, labels : List<String>, barChart : BarChart) {


        val dataSets: ArrayList<IBarDataSet> = ArrayList()

        if (values != null && values.size > 0) {


            var colors : ArrayList<Int> = ArrayList<Int>()

            for (i in 0 .. values.size - 1){
                values.get(i).x = values.get(i).x + 0.5f

                if (values.get(i).y.isNaN()) values.get(i).y = 0.0f

                if (i == 0) {
                    colors.add(ContextCompat.getColor(requireContext(), R.color.rgb_f29cee))
                } else {
                    colors.add(ContextCompat.getColor(requireContext(), R.color.rgb_6200ee))
                }

            }

            var set = BarDataSet(values, "")
            set.setDrawIcons(false)
            set.setDrawValues(false)
            set.colors = colors
            dataSets.add(set)

            barChart.xAxis.valueFormatter = object : IndexAxisValueFormatter() {

                override fun getFormattedValue(value: Float): String {
                    var temp : String = ""
                    try {
                        if (labels.size > value.toInt() &&  (value.toInt() >= 0)) {
                            temp = labels.get(value.toInt())
                        }
                    } catch (ex : Exception) {
                    } finally {
                        return temp
                    }
                }

            }

           barChart.xAxis.labelCount = labels.size

        }

        val data = BarData(dataSets)

        if (dataSets.size > 1) {
            data.barWidth = (data.barWidth / dataSets.size) - 0.08f
            barChart.groupBars(0f, 0.08f, 0.02f)
        }

        barChart.run {
            this.data = data
            setFitBars(false)
            invalidate()
            notifyDataSetChanged()
        }

        this.finishDrawCount+=1

        hideProgressbar()

    }

    /**
     * 개인정보 조회/수정 이동
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 3:06
     **/
    fun clickProfile(view: View) {
        this.findNavController().navigate(R.id.fragProfile)
    }

    /**
     * close click event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-18 오후 5:15
     **/
    fun clickClose(view: View){
        this.findNavController().navigateUp()
    }

}