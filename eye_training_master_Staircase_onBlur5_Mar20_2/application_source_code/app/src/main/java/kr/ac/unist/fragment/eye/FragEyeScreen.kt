package kr.ac.unist.fragment.eye

import android.content.Context
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.toast
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kr.ac.unist.Constants
import kr.ac.unist.R
import kr.ac.unist.activity.MainActivity
import kr.ac.unist.custom.EyeScreenView
import kr.ac.unist.custom.`interface`.OnSwipeTouchListener
import kr.ac.unist.database.DbManager
import kr.ac.unist.database.entity.TestTrial
import kr.ac.unist.database.entity.TrainTrial
import kr.ac.unist.database.entity.User
import kr.ac.unist.databinding.FragEyeScreenBinding
import kr.ac.unist.dialog.CommonDialog
import kr.ac.unist.fragment.FragHistory
import kr.ac.unist.fragment.network.Network
import kr.ac.unist.manager.BleManager
import kr.ac.unist.manager.SharedPrefManager
import kr.ac.unist.util.Util
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer
import kotlin.math.abs
import kotlin.math.round



/**
 * 시력 훈련/진단 Screen
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-13 오후 2:39
 **/
inline fun Context.toast(message:()->String){
    Toast.makeText(this, message() , Toast.LENGTH_LONG).show()
}

inline fun Fragment.toast(message: CharSequence){
    Toast.makeText(this.context, message , Toast.LENGTH_LONG).show()
}

//class FragEyeScreen : Fragment(), BleManager.OnResult {
class FragEyeScreen : Fragment(), BleManager.OnResult, EyeScreenView.EyeScreenModeChangeListener {

    companion object {

        val RESULT_START = 0        // 시작점
        val RESULT_CORRECT = 1      // 정답
        val RESULT_INCORRECT = 2    // 틀림
        val RESULT_NO_RESPONSE = 3  // 응답없음
        //val ADD_TRAIN_CYCLE = 0.5f  // 시력훈련 추가할 사이클(0.1f)
        //liza
        val ADD_TRAIN_CYCLE = 10.0f  // 시력훈련 추가할 사이클(0.1f)
        val ADD_TRAIN_CYCLE_SHARP = 0.0f
        val ADD_TEST_CYCLE = 0.5f  // 시력진단 추가할 사이클(0.1f)

        //val DEFAULT_CYCLE = 2.0f    // 기본 cycle
        //liza
        val DEFAULT_CYCLE = 10.0f    // 기본 cycle

        // 시각 훈련 모드별 사이즈 ([40, 40] = 1 block
        //val SHARP_SIZE = Constants.ONE_BLOCK_TRAIN_COUNT / 2
        //val BLUR_SIZE = Constants.ONE_BLOCK_TRAIN_COUNT / 2


        val SHARP_SIZE = 24 // sharp image will appear 24 times (30%), total trial 80
        val BLUR_SIZE = 56 // blur image will appear 56 times (70%), total trial 80

        // 시각 진단 모드별 사이즈 ([20, 20, 20, 20] = 1 block
        val TOP_SIZE = Constants.ONE_BLOCK_TRAIN_COUNT / 4
        val LEFT_SIZE = Constants.ONE_BLOCK_TRAIN_COUNT / 4
        val RIGHT_SIZE = Constants.ONE_BLOCK_TRAIN_COUNT / 4
        val BOTTOM_SIZE = Constants.ONE_BLOCK_TRAIN_COUNT / 4

        // 그림 보여지는 시간
        val INTERBAL_SHOW = 1000L       // default 1000L

        // 정답 고르는 시간
        val INTERBAL_SELECT_RESULT = 5000L

        // 정답 보여줄 시간
        val INTERBAL_SHOW_RESULT = 1000L

        // 재전송 시간
        val INTERBAL_RE_SEND = 1000L

        // 대기 시간
        val INTERBAL_WAIT_TIME =  1000L

        val INTERBAL_WAIT_TIME_TEST_RESULT = 5000L

        // CLASS BLOCK
        val CLASS_BLOCK_NM = "block"

        // CLASS SESSION
        val CLASS_SESSION_NM = "session"

        // showing distance
        var sumDistance = 0
        var numTrials = 0
        var averageDistance = 0

        var sumSF = 0
        //var numTrials = 0
        var averagesf = 0

    }

    // view model
    lateinit var viewModel : EyeScreenViewModel

    // 스크린 모드
    var screenMode : Int = EyeScreenView.MODE_SCREEN_TRAIN

    //liza
    var mode = EyeScreenView.MODE_TRAIN_BLUR
    val MODE_TRAIN_SHARP = 0                    // Square wave
    val MODE_TRAIN_BLUR = 1

    // 훈련 리스트
    var trainList: ArrayList<Int> = ArrayList<Int>()

    // 현재 시도 스탭
    var currentTrialStep: Int = -1
    var beforBlockStep: Int = -1

    // 오늘 블럭 스탭
    var todayBlockCount: Int = -1

    // 타이머 설정
    var timerJob: Job? = null

    // 정답 고를 시간
    var isResultTime: Boolean = false

    // 바인딩
    var binding: FragEyeScreenBinding? = null

    // swipe listener
    var swipeTouchListener: OnSwipeTouchListener? = null

    // 자극 시작 시간
    var startTime: Long = 0

    // 자극 종료 시간
    var endTime: Long = 0

    // 센서 에러 메세지
    var sensorErrorMsg : Snackbar? = null

    // 재전송 타이머 설정
    var reSendTimerJob: Job? = null
    var reSendSnackbar : Snackbar? = null

    // 현재 휴식 시간
    var currentBreakTime : Int = 0
    
    // 완전 중지 여부
    var isNaverStop : Boolean = false

    
    // 유저 정보
    lateinit var user  : List<User>

    /**
     * 초기화
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-25 오후 5:16
     **/
    fun init() {
        this.currentTrialStep = -1
        if (this.timerJob != null) {
            this.timerJob!!.cancel()
            this.timerJob = null
        }
        this.isResultTime = false
        this.startTime = 0
        this.endTime = 0

    }

    /**
     * 내 시도 정보를 설정한다.
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-26 오후 6:00
     **/
    fun settingTrainInfo() {
        // 오늘 진행한 Trial, Block 단위를 구한다.
        GlobalScope.launch() {

            // 오늘 시도한 개수
            if (binding!!.esvScreen.screenMode == EyeScreenView.MODE_SCREEN_TRAIN) {
                currentTrialStep = DbManager.getTodayTrainTrialCount(SharedPrefManager.getUserSeqId())
            } else {
                currentTrialStep = DbManager.getTodayTestTrialCount(SharedPrefManager.getUserSeqId())
            }

            // 오늘 하나라도 수행했으면, 기존 유지 (시력 훈련)
            if (binding!!.esvScreen.screenMode == EyeScreenView.MODE_SCREEN_TRAIN) {
                binding!!.esvScreen.frequency = SharedPrefManager.getCurrentFrequncy()
            } else {
                // 오늘 처음 수행한다면, 기본 CYCLE 셋팅
                binding!!.esvScreen.frequency = FragEyeScreen.DEFAULT_CYCLE
            }

            // 오늘 BLOCK 개수
            todayBlockCount = currentTrialStep / Constants.ONE_BLOCK_TRAIN_COUNT
            beforBlockStep = todayBlockCount

            if (currentTrialStep < 0) currentTrialStep = 0

            // 오늘 횟수를 모두 끝내면, 끝났다고 알린다.
            if (todayBlockCount >= Constants.ONE_SESSION_BLOCK_COUNT - 1) {
                GlobalScope.launch(Dispatchers.Main) {
                    showFinishSession(RESULT_START, false)
                }
            }

        }
    }


    /**
     * 현재 Block Step을 가져온다.
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-26 오후 7:01
     **/
    fun getCurrentBlockStep(): Int {
        return (currentTrialStep / Constants.ONE_BLOCK_TRAIN_COUNT) + 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Util.settingFullScreen(requireActivity(), true)

        // 바인딩
        if (this.binding == null) {
            this.binding = DataBindingUtil.inflate<FragEyeScreenBinding>(
                inflater, R.layout.frag_eye_screen, container, false
            )
        }

        BleManager.addResult(this)

        // 화면 진입시, 센서 에러라면 센서 에러 메세지를 띄운다.
        if (!BleManager.isSensorStateNormal()) {
            sensorError()
        }

        this.binding!!.frag = this

        // view model 연결
        this.viewModel = ViewModelProvider(this).get(EyeScreenViewModel::class.java)
        this.binding!!.model = this.viewModel
        this.binding!!.lifecycleOwner = viewLifecycleOwner

        // 스크린 모드 셋팅
        var args = FragEyeScreenArgs.fromBundle(requireArguments())
        this.binding!!.esvScreen.init(args.screenMode)

        // 스크린 모드에 따라서 타이틀 & 시작 ui 변경
        if (args.screenMode == EyeScreenView.MODE_SCREEN_TRAIN) {
            this.viewModel.changeStartContainerText(getString(R.string.title_eye_train),
                getString(R.string.start_train_title),
                getString(R.string.start_train_message),
                getString(R.string.start_train_start),
                View.VISIBLE,
                View.GONE)
        } else {
            this.viewModel.changeStartContainerText(getString(R.string.title_eye_test),
                getString(R.string.start_test_title),
                getString(R.string.start_test_message),
                getString(R.string.start_test_start),
                View.GONE,
                View.VISIBLE)
        }
        
        // 훈련 그림 관찰자 추가
        this.viewModel.drawEye.observe(viewLifecycleOwner, androidx.lifecycle.Observer { eyeDrawVO ->
            
            // 훈련 그림 변경
            var success : Boolean = binding!!.esvScreen.draw(eyeDrawVO.mode, MainActivity.distance.toInt(), eyeDrawVO.addCycle)

            if (!success) {
                sensorError()
            }
        })
        
        // 초기화
        init()

        // 내 시도 정보를 셋팅
        settingTrainInfo()

        // 스와이프 리스너
        this.swipeTouchListener = object : OnSwipeTouchListener(requireContext()) {

            // TOP SWIPE
            override fun onSwipeTop() {

                if (isResultTime && binding!!.esvScreen.screenMode == EyeScreenView.MODE_SCREEN_TEST) {

                    isResultTime = false

                    var isCorrent = false
                    var trainMode = trainList.get(getTrainListIndex())

                    when (trainMode) {
                        EyeScreenView.MODE_TEST_TOP -> {
                            isCorrent = true
                        }
                        else -> {
                            isCorrent = false
                        }
                    }

                    if (isCorrent) {
                        showResult(
                            getString(R.string.msg_train_correct),
                            trainMode,
                            RESULT_CORRECT,
                        false)
                    } else {
                        showResult(
                            getString(R.string.msg_train_incorrect),
                            trainMode,
                            RESULT_INCORRECT,
                            false
                        )
                    }
                }

            }

            // BOTTOM SWIPE
            override fun onSwipeBottom() {

                if (isResultTime && binding!!.esvScreen.screenMode == EyeScreenView.MODE_SCREEN_TEST) {

                    isResultTime = false
                    var isCorrent = false
                    var trainMode = trainList.get(getTrainListIndex())

                    when (trainMode) {
                        EyeScreenView.MODE_TEST_BOTTOM -> {
                            isCorrent = true
                        }
                        else -> {
                            isCorrent = false
                        }
                    }

                    if (isCorrent) {
                        showResult(
                            getString(R.string.msg_train_correct),
                            trainMode,
                            RESULT_CORRECT,
                            false
                        )
                    } else {
                        showResult(
                            getString(R.string.msg_train_incorrect),
                            trainMode,
                            RESULT_INCORRECT,
                            false
                        )
                    }

                }

            }

            // RIGHT SWIPE
            override fun onSwipeRight() {
                if (isResultTime) {

                    isResultTime = false

                    var isCorrent = false
                    var trainMode = trainList.get(getTrainListIndex())

                    when(trainMode) {
                        EyeScreenView.MODE_TEST_RIGHT,
                        EyeScreenView.MODE_TRAIN_BLUR -> {
                            isCorrent = true
                        }
                        else -> {
                            isCorrent = false
                        }
                    }

                    if (isCorrent) {
                        showResult(
                            getString(R.string.msg_train_correct),
                            trainMode,
                            RESULT_CORRECT,
                            false
                        )
                    } else {
                        showResult(
                            getString(R.string.msg_train_incorrect),
                            trainMode,
                            RESULT_INCORRECT,
                            false
                        )
                    }
                }
            }

            // LEFT SWIPE
            override fun onSwipeLeft() {
                if (isResultTime) {
                    isResultTime = false

                    var isCorrent = false
                    var trainMode = trainList.get(getTrainListIndex())

                    when(trainMode) {
                        EyeScreenView.MODE_TEST_LEFT,
                        EyeScreenView.MODE_TRAIN_SHARP -> {
                            isCorrent = true
                        }
                        else -> {
                            isCorrent = false
                        }
                    }

                    if (isCorrent) {
                        showResult(
                            getString(R.string.msg_train_correct),
                            trainMode,
                            RESULT_CORRECT,
                            false
                        )
                    } else {
                        showResult(
                            getString(R.string.msg_train_incorrect),
                            trainMode,
                            RESULT_INCORRECT,
                            false
                        )
                    }

                }
            }
        }

        this.viewModel.hideEyeScreenView()
        
        // 유저 정보
        GlobalScope.launch {
            user = DbManager.getInstance().userDao().findUser(SharedPrefManager.getUserSeqId())
        }

        
        return this.binding!!.root
    }



    override fun onModeChanged(mode: Int) {
        // update the mode variable
        this.mode = mode

        // handle the mode change
        when (mode) {
            EyeScreenView.MODE_TRAIN_BLUR -> {
                // handle MODE_TRAIN_BLUR
            }
            EyeScreenView.MODE_TRAIN_SHARP -> {
                // handle MODE_TRAIN_SHARP
            }
            // add other mode cases if needed
        }
    }



    /**
     * 다음 스탭
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-25 오후 12:46
     **/
    fun nextStep(result: Int) {

        if (this.beforBlockStep == 0) {
            this.beforBlockStep = getCurrentBlockStep()
        }

        if (this.currentTrialStep >= (Constants.ONE_BLOCK_TRAIN_COUNT * this.beforBlockStep)) {

            this.beforBlockStep = getCurrentBlockStep()

            // showing average distance
//            print(averageDistance)
            // 스탭 종료
            stepFinish(result)



        } else {

            this.beforBlockStep = getCurrentBlockStep()

            this.currentTrialStep += 1

            var addCycle : Float = 0.0f

            if (result == RESULT_START || result == RESULT_NO_RESPONSE) {
                this.viewModel.drawEyeScreen(EyeDrawVO(this.trainList.get(getTrainListIndex()), this.binding!!.esvScreen.distance, 0f))
            } else if (result == RESULT_CORRECT) {

                if (this.screenMode == EyeScreenView.MODE_SCREEN_TRAIN) {
                    if (this.mode == EyeScreenView.MODE_TRAIN_SHARP) {
                       addCycle = ADD_TRAIN_CYCLE_SHARP
                        val tag = "MyApp"
                        Log.d(tag, "++++++++++++++++++++++++++++++++++++++++    @MODE:SHARP ,addCycle for next trial is: $addCycle")
                    }
                    else if (this.mode == EyeScreenView.MODE_TRAIN_BLUR) { //liza
                        // do nothing, leave the addCycle unchanged
                        addCycle = ADD_TRAIN_CYCLE
                        val tag = "MyApp"
                        Log.d(tag, "++++++++++++++++++++++++++++++++++++++++    @MODE:BLUR,addCycle for next trial is: $addCycle")
                    }
                } else {
                        addCycle = ADD_TEST_CYCLE
                    val tag = "MyApp"
                    Log.d(tag, "++++++++++++++++++++++++++++++++++++++++    mode:TEST ,addCycle for next trial is: $addCycle")
                    }

                this.viewModel.drawEyeScreen(
                    EyeDrawVO(
                        this.trainList.get(getTrainListIndex()),
                        this.binding!!.esvScreen.distance,
                        addCycle
                    )
                )

            } else if (result == RESULT_INCORRECT) {

                if (this.screenMode == EyeScreenView.MODE_SCREEN_TRAIN) {
                    if (this.mode == EyeScreenView.MODE_TRAIN_SHARP) {
                        addCycle = -ADD_TRAIN_CYCLE_SHARP
                        val tag = "MyApp"
                        Log.d(tag, "++++++++++++++++++++++++++++++++++++++++    @MODE:SHARP,addCycle for next trial is: $addCycle, *MODE:$mode")
                    }
                    else if (this.mode == EyeScreenView.MODE_TRAIN_SHARP) { //liza
                        // do nothing, leave the addCycle unchanged
                        addCycle = -ADD_TRAIN_CYCLE
                        val tag = "MyApp"
                        Log.d(tag, "++++++++++++++++++++++++++++++++++++++++    @MODE:BLUR ,addCycle for next trial is: $addCycle, *MODE:$mode")
                    }
                } else {
                    addCycle = -ADD_TEST_CYCLE
                    val tag = "MyApp"
                    Log.d(tag, "++++++++++++++++++++++++++++++++++++++++    mode:TEST ,addCycle for next trial is: $addCycle")
                }
                this.viewModel.drawEyeScreen(
                    EyeDrawVO(
                        this.trainList.get(getTrainListIndex()),
                        this.binding!!.esvScreen.distance,
                        addCycle
                    )
                )

            }

            // 문제 제출
            showQuest()

        }

    }

    /**
     * 현재 훈련 리스트 index 반환
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-26 오후 7:24
     **/
    fun getTrainListIndex(): Int {
        return abs((Constants.ONE_BLOCK_TRAIN_COUNT * (getCurrentBlockStep() - 1)) - this.currentTrialStep)
    }

    /**
     * 문제를 보여준다.
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-25 오후 2:16
     **/
    fun showQuest() {
        if (BleManager.isSensorStateNormal()) {
            if (this.timerJob != null) {
                this.timerJob!!.cancel()
            }

            // 시작 시간 기록
            this.startTime = Calendar.getInstance().timeInMillis

            this.viewModel.hideResult()
            this.viewModel.showEyeScreenView()

            // 보여주는 시간이 지나면 문제를 숨기고, 정답 고를 시간을 준다.
            this.timerJob = GlobalScope.launch(Dispatchers.Main) {
                delay(INTERBAL_SHOW)
                viewModel.hideEyeScreenView()

                // 정답 고를 시간을 준다.
                waitSelectResult()
            }
        }
    }

    /**
     * 정답 고를 시간을 준다.
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-25 오후 2:16
     **/
    fun waitSelectResult() {
        isResultTime = true

        if (this.timerJob != null) {
            this.timerJob!!.cancel()
        }

        // 스와이프 리스너를 등록해서 정답을 고를 수 있게 한다.
        this.binding!!.mainLayout.setOnTouchListener(this.swipeTouchListener)

        // 정답 고를 시간을 준다.
        timerJob = GlobalScope.launch(Dispatchers.Main) {
            delay(INTERBAL_SELECT_RESULT)
            isResultTime = false

            // 스와이프 리스너를 제거해서 정답을 고를 수 없게 한다.
            binding!!.mainLayout.setOnTouchListener(null)

            if (BleManager.isSensorStateNormal()) {
                // 결과를 보여준다.
                showResult(
                    getString(R.string.msg_train_timeout),
                    EyeScreenView.MODE_NONE,
                    RESULT_NO_RESPONSE,
                    false
                )
            }
        }
    }

    /**
     * 결과를 보여준다.
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-25 오후 2:26
     **/
    fun showResult(msg: String, selectedMode: Int, result : Int, isRetry : Boolean) {

        if (this.timerJob != null) {
            this.timerJob!!.cancel()
        }

        if (this.reSendTimerJob != null) {
            this.reSendTimerJob!!.cancel()
        }

        if (!isRetry) {
            // 종료 시간 기록
            this.endTime = Calendar.getInstance().timeInMillis
        }

        if (checkNetwork()) {

            if (this.reSendSnackbar != null) {
                this.reSendSnackbar!!.dismiss()
            }

            if (result == RESULT_CORRECT) {
                // 정답 비프음
                Util.playBeep(requireContext(), R.raw.beep_single)
            } else if (result == RESULT_INCORRECT) {
                // 오답 비프음
                Util.playBeep(requireContext(), R.raw.beep_double)
            } else if (result == RESULT_NO_RESPONSE) {
                // 응답 없음 비프음
                Util.playBeep(requireContext(), R.raw.beep_long)
            }

            // 결과 db insert
            insertTrial(selectedMode, result)

            // 메세지 표시
            this.viewModel.showResult(msg)

            // 정답을 보여주고, 다음 문제로 이동한다.
            timerJob = GlobalScope.launch(Dispatchers.Main) {
                delay(INTERBAL_SHOW_RESULT)
                viewModel.hideResult()
                delay(INTERBAL_WAIT_TIME)
                nextStep(result)
            }
        } else {

            // 1초에 한번씩 다시 시도
            this.reSendTimerJob = GlobalScope.launch(Dispatchers.Main) {
                delay(INTERBAL_RE_SEND)
                showResult(msg, selectedMode, result, true)
            }

            if (this.reSendSnackbar == null || !this.reSendSnackbar!!.isShown) {
                this.reSendSnackbar = Util.showButtonSnackbar(
                    requireView(),
                    requireContext(),
                    requireContext().getString(R.string.msg_error_internet),
                    requireContext().getString(R.string.msg_re_send),
                    View.OnClickListener {
                        showResult(msg, selectedMode, result, true)
                    },
                    Snackbar.LENGTH_INDEFINITE
                )
            }

        }


    }

    /**
     * 선택된 값을 db에 저장한다.
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-26 오후 3:17
     **/
    fun insertTrial(selectedMode: Int, result: Int) {
        if (BleManager.isSensorStateNormal()) {
            var userSeqId = SharedPrefManager.getUserSeqId()    //  유저 시퀀스 ID
            var cycle = this.binding!!.esvScreen.frequency  // degree
            var sf =
                cycle  //  자극의 spatial frequency
            var degree = this.binding!!.esvScreen.visualAngleDegree // degree
            var size =
                this.binding!!.esvScreen.calPixcelWidth //  자극의 실제 크기 (반지름 길이 px 단위)
            var rotation : Float = 0.0f    //  자극의 빗금방향 (시계방향으로 회전된 각도 단위)
            var distance =
                this.binding!!.esvScreen.distance   //  화면과 사용자 사이 거리 (BLE장치가 출력한 거리, mm 단위)

            //  자극 표시 후 응답까지 걸린 시간 (second단위, 소수점 3자리까지 반올림)
            var duration = round((this.endTime - this.startTime) / 1000.toFloat() * 100) / 100
            var answer = ""     //  사용자의 응답 (sharp 또는 blunt)
            var pattern = ""    //  자극의 패턴 (sharp또는 blunt)
            var correct = ""    //  응답의 정답 여부 (T 또는 F)
            var json: JSONObject = JSONObject()

            if (this.binding!!.esvScreen.mode == EyeScreenView.MODE_TRAIN_BLUR) {
                pattern = EyeScreenView.MODE_TRAIN_BLUR_NM
            } else {
                pattern = EyeScreenView.MODE_TRAIN_SHARP_NM
            }

            if (selectedMode == EyeScreenView.MODE_TRAIN_BLUR) {
                answer = EyeScreenView.MODE_TRAIN_BLUR_NM
            } else if (selectedMode == EyeScreenView.MODE_TRAIN_SHARP) {
                answer = EyeScreenView.MODE_TRAIN_SHARP_NM
            } else if (selectedMode == EyeScreenView.MODE_TEST_TOP) {
                answer = EyeScreenView.MODE_TEST_TOP_NM
            } else if (selectedMode == EyeScreenView.MODE_TEST_LEFT) {
                answer = EyeScreenView.MODE_TEST_LEFT_NM
            } else if (selectedMode == EyeScreenView.MODE_TEST_RIGHT) {
                answer = EyeScreenView.MODE_TEST_RIGHT_NM
            } else if (selectedMode == EyeScreenView.MODE_TEST_BOTTOM) {
                answer = EyeScreenView.MODE_TEST_BOTTOM_NM
            } else {
                answer = EyeScreenView.MODE_NONE_NM
            }

            if (result == RESULT_CORRECT) {
                correct = "T"
            } else {
                correct = "F"
            }

            // 시력 진단과 훈련일때 rotaion을 설정한다.
            if (binding!!.esvScreen.screenMode == EyeScreenView.MODE_SCREEN_TEST) {
                rotation = this.binding!!.esvScreen.rotation
            } else {
                rotation = this.binding!!.esvScreen.rotation - 90
            }

            GlobalScope.launch {

                var apiUrl: String = ""

                // 종료 시간을 api 데이터 포맷에 맞게 설정
                var simpleFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm");
                var date: Date = Calendar.getInstance().time
                date.time = endTime
                var strEndTime : String = simpleFormatter.format(date).toString()

                // 시력 훈련
                if (binding!!.esvScreen.screenMode == EyeScreenView.MODE_SCREEN_TRAIN) {

                    apiUrl = Network.TRAIN_TRIAL
                    sumDistance = sumDistance + distance
                    sumSF = sumSF + sf.toInt()
                    numTrials = numTrials + 1

                    var trainTrial: TrainTrial = TrainTrial(
                        userSeqId,
                        sf.toString(),
                        degree.toString(),
                        cycle.toString(),
                        size.toString(),
                        rotation.toString(),
                        pattern,
                        distance.toString(),
                        answer,
                        duration.toString(),
                        correct,
                        beforBlockStep.toString()
                    )
                    trainTrial.instDtm = endTime

                    // 결과 db insert
                    DbManager.getInstance().processDao().insertTrainTrialInfo(trainTrial)


                    json.put("class", "trial")
                    json.put("userKey", trainTrial.userSeqId)
                    json.put("sf", trainTrial.sf)
                    json.put("size", trainTrial.size)
                    json.put("rotation", trainTrial.rotation)
                    json.put("pattern", trainTrial.pattern)
                    json.put("distance", trainTrial.distance)
                    json.put("answer", trainTrial.answer)
                    json.put("duration", trainTrial.duration)
                    json.put("correct", trainTrial.correct)
                    json.put("date", strEndTime)
                    json.put("visual_angle", trainTrial.degree)
                    val tag = "MyApp"
                    Log.d(tag, "--------------------------------------------------------------------------------   tial:$numTrials  SF: $sf, pattern: $pattern , answer: $answer, correct: $correct, result: $result")


                } else {

                    apiUrl = Network.TEST_TRIAL

                    // 시력 진단
                    var testTrial: TestTrial = TestTrial(
                        userSeqId,
                        size.toString(),
                        rotation.toString(),
                        distance.toString(),
                        answer,
                        duration.toString(),
                        correct,
                        beforBlockStep.toString(),
                        degree.toString()
                    )
                    testTrial.instDtm = endTime

                    sumDistance = sumDistance + distance
                    sumSF = sumSF + sf.toInt()
                    numTrials = numTrials + 1

                    // 결과 db insert
                    DbManager.getInstance().processDao().insertTestTrialInfo(testTrial)

                    json.put("class", "trial")
                    json.put("userKey", testTrial.userSeqId)
                    json.put("size", testTrial.size)
                    json.put("rotation", testTrial.rotation)
                    json.put("distance", testTrial.distance)
                    json.put("answer", testTrial.answer)
                    json.put("duration", testTrial.duration)
                    json.put("correct", testTrial.correct)
                    json.put("date", strEndTime)
                    json.put("visual_angle", testTrial.degree)

                }

                Timber.d(json.toString())

                // API 전송
                Network.POST(apiUrl, json, object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Timber.d(response.toString())
                    }
                })

            }
        }
    }

    /**
     * Block, Finish 정보 API으로 보낸다.
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-26 오후 3:17
     **/
    fun sendFinishInfo(classNm : String) : Boolean{

        if (checkNetwork()) {

            GlobalScope.launch {

                //  유저 시퀀스 ID
                var userSeqId = SharedPrefManager.getUserSeqId()

                // 현재 Block
                var block = (currentTrialStep / Constants.ONE_BLOCK_TRAIN_COUNT).toString()

                // 종료 시간을 api 데이터 포맷에 맞게 설정
                var simpleFormatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm");
                var date: Date = Calendar.getInstance().time
                date.time = endTime
                var strEndTime : String = simpleFormatter.format(date).toString()

                // 시각 훈련 모드
                if (binding!!.esvScreen.screenMode == EyeScreenView.MODE_SCREEN_TRAIN) {

                    // Block train 리스트
                    var trainList: List<TrainTrial> = ArrayList<TrainTrial>()
                    var apiUrl: String = ""
                    if (classNm.equals(CLASS_BLOCK_NM)) {
                        trainList = DbManager.getTrainBlockInfo(userSeqId, block)
                        apiUrl = Network.TRAIN_BLOCK
                    } else {
                        trainList = DbManager.getTodayTrainTrial(userSeqId)
                        apiUrl = Network.TRAIN_SESSION
                    }

                    if (trainList != null && trainList.size > 0) {

                        var sfStart =
                            trainList.get(0).sf                         //  시작 시점의 자극의 spatial frequency
                        var sfEnd =
                            trainList.get(trainList.size - 1).sf        //  종료 시점의 자극의 spatial frequency
                        var distanceStart =
                            trainList.get(0).distance                   //  시작 시점의 화면과 사용자 사이 거리 (BLE장치가 출력한 거리, mm 단위)
                        var distanceEnd =
                            trainList.get(trainList.size - 1).distance  //  종료 시점의 화면과 사용자 사이 거리 (BLE장치가 출력한 거리, mm 단위)
                        var resultSum =
                            0.0                             //  정해진 규칙 (동등수행선)에 따라 계산된 사용자의 정답 점수
                        var resultAvg = 0.0
                        var resultSize = 0.0
                        var durationAvg = 0.0                           //  자극 표시 후 응답까지 걸린 시간의 평균 (second단위, 소수점 3자리까지 반올림)
                        var durationSum = 0.0

                        // 정답 & 자극 응답 시간 평균 계산
                        for (train in trainList) {
                            if (train.correct.equals("T")) {
                                resultSum += Util.calculatorResult(
                                    (train.distance.toFloat() / 100),
                                    (train.size.toFloat() / train.cycle.toFloat())
                                )
                                resultSize += 1
                            }

                            durationSum += train.duration.toFloat()
                        }

                        // 자극 응답 평균
                        durationAvg = round((durationSum / trainList.size) * 100) / 100

                        // 정답 평균 점수
                        resultAvg = round((resultSum / resultSize) * 100) / 100

                        // showing average distance
//                        val averageDistance = sumDistance / numTrials
//                        sumDistance = 0
//                        numTrials = 0
//
//                        toast {
//                            "Average distance " + String.format("%.0f", averageDistance) + "mm"
//                        }

                        var json: JSONObject = JSONObject()
                        json.put("class", classNm)
                        json.put("userKey", userSeqId)
                        json.put("sf_start", sfStart)
                        json.put("sf_end", sfEnd)
                        json.put("distance_start", distanceStart)
                        json.put("distance_end", distanceEnd)
                        if (classNm.equals(CLASS_BLOCK_NM)) {
                            if (!resultSum.isNaN()) {
                                json.put("result", resultSum.toString())
                            } else {
                                json.put("result", "0")
                            }
                        } else {
                            if (!resultAvg.isNaN()) {
                                json.put("result", resultAvg.toString())
                            } else {
                                json.put("result", "0")
                            }

                        }

                        if (!durationAvg.isNaN()){
                            json.put("duration_avg", durationAvg.toString())
                        } else {
                            json.put("duration_avg", "0")
                        }

                        json.put("date", strEndTime)

                        Timber.d(json.toString())

                        // API 전송
                        Network.POST(apiUrl, json, object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                            }

                            override fun onResponse(call: Call, response: Response) {
                                Timber.d(response.toString())
                            }
                        })

                    }
                } else {    // 시력 진단 모드

                    // Block test 리스트
                    var testList: List<TestTrial> = ArrayList<TestTrial>()
                    var apiUrl: String = ""

                    if (classNm.equals(CLASS_BLOCK_NM)) {
                        testList = DbManager.getTestBlockInfo(userSeqId, block)
                        apiUrl = Network.TEST_BLOCK
                    } else {
                        testList = DbManager.getTodayTestTrial(userSeqId)
                        apiUrl = Network.TEST_SESSION
                    }

                    if (testList != null && testList.size > 0) {

                        var distanceStart =
                            testList.get(0).distance                    //  시작 시점의 화면과 사용자 사이 거리 (BLE장치가 출력한 거리, mm 단위)
                        var distanceEnd =
                            testList.get(testList.size - 1).distance    //  종료 시점의 화면과 사용자 사이 거리 (BLE장치가 출력한 거리, mm 단위)
                        var resultSum =
                            0.0                             //  정해진 규칙 (동등수행선)에 따라 계산된 사용자의 정답 점수
                        var resultAvg = 0.0
                        var resultSize = 0.0
                        var durationAvg =
                            0.0                           //  자극 표시 후 응답까지 걸린 시간의 평균 (second단위, 소수점 3자리까지 반올림)
                        var durationSum = 0.0

                        // 정답 & 자극 응답 시간 평균 계산
                        for (train in testList) {
                            if (train.correct.equals("T")) {
                                resultSum += Util.calculatorResult(
                                    (train.distance.toFloat() / 100),
                                    (train.distance.toFloat() / 100)
                                )

                            }

                            durationSum += train.duration.toFloat()
                        }

                        // 자극 응답 평균
                        durationAvg = round((durationSum / trainList.size) * 100) / 100

                        // 정답 평균 점수
                        resultAvg = round((resultSum / resultSize) * 100) / 100

//                        // showing average distance
//                        val averageDistance = sumDistance / numTrials
//                        sumDistance = 0
//                        numTrials = 0

//                        toast {
//                            "Average distance " + String.format("%.0f", averageDistance) + "mm"
//                        }

                        // 점수 계산
                        var blockData: List<TestTrial> = DbManager.getTodayTestTrial(SharedPrefManager.getUserSeqId())

                        var returnData: ArrayList<HashMap<String, Float>> =
                            FragHistory.calculatorTest(blockData) as ArrayList<HashMap<String, Float>>

                        var blockScore: HashMap<String, Float> = returnData.get(0)
                        var totalCount: HashMap<String, Float> = returnData.get(1)
                        var currentAvg: Float = EyeScreenView.TEST_VISUAL_ANGLE_DEGREE

                        for (key in blockScore.keys) {
                            if (totalCount.get(key)!!.toFloat() > 0) {
                                currentAvg = blockScore.get(key)!!.toFloat() / totalCount.get(key)!!
                                    .toFloat()
                            }
                        }

                        var json: JSONObject = JSONObject()
                        json.put("class", classNm)
                        json.put("userKey", userSeqId)
                        json.put("distance_start", distanceStart)
                        json.put("distance_end", distanceEnd)

                        if (classNm.equals(CLASS_BLOCK_NM)) {
                            if (!resultSum.isNaN()) {
                                json.put("result", resultSum.toString())
                            } else {
                                json.put("result", "0")
                            }
                        } else {
                            if (!resultAvg.isNaN()) {
                                json.put("result", resultAvg.toString())
                            } else {
                                json.put("result", "0")
                            }
                        }

                        if (!durationAvg.isNaN()) {
                            json.put("duration_avg", durationAvg.toString())
                        } else {
                            json.put("duration_avg", "0")
                        }
                        json.put("date", strEndTime)
                        json.put("visual_angle_avg", currentAvg.toString())

                        Timber.d(json.toString())

                        // API 전송
                        Network.POST(apiUrl, json, object : Callback {

                            override fun onFailure(call: Call, e: IOException) {
                            }

                            override fun onResponse(call: Call, response: Response) {
                                Timber.d(response.toString())
                            }
                        })

                    }


                }

            }

            return true

        } else {
            return false
        }
    }

    /**
     * 스탭 종료
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-25 오후 1:01
     **/
    fun stepFinish(result : Int) {

        // 종료 시간 기록
        this.endTime = Calendar.getInstance().timeInMillis

        // 오늘 1 session 끝냈으면 끝났다고 다이얼로그를 띄운다.
        if (getCurrentBlockStep() >= Constants.ONE_SESSION_BLOCK_COUNT) {
            showFinishSession(result, false)
        } else {
            showFinishBlock(result, false)
        }


    }

    /**
     * Block이 끝났다고 다이얼로그를 띄운다.
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-26 오후 7:30
     **/
    fun showFinishBlock(result: Int, isRetry: Boolean) {

        var isSendNetwork = false

        if (this.reSendTimerJob != null) {
            this.reSendTimerJob!!.cancel()
        }

        // block 정보 전송
        if (result != RESULT_START) {
            isSendNetwork = sendFinishInfo(CLASS_BLOCK_NM)

            if (!isSendNetwork) {

                // 1초에 한번씩 다시 시도
                this.reSendTimerJob = GlobalScope.launch(Dispatchers.Main) {
                    delay(INTERBAL_RE_SEND)
                    showFinishBlock(result, true)
                }

                if (this.reSendSnackbar == null || !this.reSendSnackbar!!.isShown) {
                    this.reSendSnackbar = Util.showButtonSnackbar(
                        requireView(),
                        requireContext(),
                        requireContext().getString(R.string.msg_error_internet),
                        requireContext().getString(R.string.msg_re_send),
                        View.OnClickListener {
                            showFinishBlock(result, true)
                        },
                        Snackbar.LENGTH_INDEFINITE
                    )
                    averageDistance = sumDistance / numTrials
                    averagesf = sumSF/numTrials
                    sumDistance = 0
                    sumSF = 0
                    numTrials = 0
                    val distanceMsg = "Average distance " + averageDistance.toString() + "mm."
                    toast(distanceMsg)
                    val sfMsg = "Average sf " + averagesf.toString()
                    toast(sfMsg)
                }

            } else {
                if (this.reSendSnackbar != null) {
                    this.reSendSnackbar!!.dismiss()
                }
            }

        } else {
            isSendNetwork = true
        }


        if (!isRetry) {

            this.binding!!.tvTitle.visibility = View.GONE
            this.binding!!.cvIntro.visibility = View.GONE
            this.binding!!.esvScreen.visibility = View.GONE

            var dialog: CommonDialog = CommonDialog()
            var showDelay : Long = 0

            // 다음 블럭 진행 다이얼로그
            if (this.binding!!.esvScreen.screenMode == EyeScreenView.MODE_SCREEN_TRAIN) {
                dialog.title = String.format(
                    getString(R.string.msg_dialog_train_finish_one_block_title),
                    getCurrentBlockStep() - 1
                )
                showDelay = 0
            } else {
                dialog.title = String.format(
                    getString(R.string.msg_dialog_test_finish_one_block_title),
                    getCurrentBlockStep() - 1
                )

                if (result != RESULT_START) {

                    // 결과 메세지 출력
                    showResultMsg()

                    // 결과 보여준 후, 일정 시간 후에 다음 진행
                    showDelay = INTERBAL_WAIT_TIME_TEST_RESULT

                } else {
                    showDelay = 0
                }
            }

            dialog.content = getString(R.string.msg_dialog_train_finish_one_block_content)
            dialog.leftButtonTitle =
                getString(R.string.msg_dialog_train_finish_one_block_left_button)
            dialog.rightButtonTitle =
                getString(R.string.msg_dialog_train_finish_one_block_right_button)

            dialog.onClickListener = object : CommonDialog.OnClickListener {

                // 되돌가기 클릭
                override fun leftClick() {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }

                // 다음 블럭 시작 클릭
                override fun rightClick() {
                    if (isSendNetwork) {
                        dialog.dismiss()

                        // 바로 시작이 아니라면
                        if (result != RESULT_START) {

                            // 쉬는 시간 동안 대기한 후, 시작한다.
                            var breakTime: String = SharedPrefManager.getBreakTime()
                            currentBreakTime = breakTime.replace("초", "").toInt()

                            var snackbar = Util.showSimpleSnackbar(
                                requireView(),
                                requireContext(),
                                String.format(
                                    requireContext().getString(R.string.msg_break_time),
                                    breakTime
                                ),
                                Snackbar.LENGTH_INDEFINITE
                            )

                            var tvContent =
                                snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

                            averageDistance = sumDistance / numTrials
                            averagesf = sumSF/numTrials
                            sumDistance = 0
                            sumSF = 0
                            numTrials = 0
                            val distanceMsg = "Average distance " + averageDistance.toString() + "mm."
                            toast(distanceMsg)
                            val sfMsg = "Average sf " + averagesf.toString()
                            toast(sfMsg)
                            timer(period = 1000, initialDelay = 1000) {

                                GlobalScope.launch(Dispatchers.Main) {
                                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                                        if (currentBreakTime <= 0) {
                                            snackbar.dismiss()
                                            nextStep(RESULT_START);
                                        } else {
                                            try {
                                                tvContent.text = String.format(
                                                    getString(R.string.msg_break_time),
                                                    currentBreakTime
                                                )
                                            } catch (ex: Exception) {
                                            }
                                        }
                                    }
                                }

                                currentBreakTime -= 1

                                if (currentBreakTime <= 0) {
                                    cancel()
                                }

                            }

                        } else {
                            currentBreakTime = 0
                            nextStep(RESULT_START);
                        }



                    }
                }

                // 백버튼 클릭
                override fun backClick() {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }

            }

            GlobalScope.launch(Dispatchers.Main) {
                delay(showDelay)
                viewModel.hideResult()
                try {
                    dialog.show(childFragmentManager, "blockFinishDialog")
                } catch (e : Exception) {

                }
            }

        }

    }

    /**
     * 세션이 끝났다고 다이얼로그를 띄운다.
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-26 오후 7:30
     **/
    fun showFinishSession(result: Int, isRetry: Boolean) {

        var isSendNetwork = false

        if (this.reSendTimerJob != null) {
            this.reSendTimerJob!!.cancel()
        }

        // session 정보 전송
        if (result != RESULT_START) {
            // 세션은 마지막 블럭을 마쳣기 때문에, 블럭과 세션을 동시에 처리한다.
            isSendNetwork = sendFinishInfo(CLASS_BLOCK_NM)

            if (!isSendNetwork) {

                // 1초에 한번씩 다시 시도
                this.reSendTimerJob = GlobalScope.launch(Dispatchers.Main) {
                    delay(INTERBAL_RE_SEND)
                    showFinishSession(result, true)
                }

                if (this.reSendSnackbar == null || !this.reSendSnackbar!!.isShown) {
                    this.reSendSnackbar = Util.showButtonSnackbar(
                        requireView(),
                        requireContext(),
                        requireContext().getString(R.string.msg_error_internet),
                        requireContext().getString(R.string.msg_re_send),
                        View.OnClickListener {
                            showFinishSession(result, true)
                        },
                        Snackbar.LENGTH_INDEFINITE
                    )
                }
            } else {

                if (this.reSendSnackbar != null) {
                    this.reSendSnackbar!!.dismiss()
                }

                averageDistance = sumDistance / numTrials
                averagesf = sumSF / numTrials
                sumDistance = 0
                sumSF = 0
                numTrials = 0
                val distanceMsg = "Average distance " + averageDistance.toString() + "mm."
                toast(distanceMsg)
                val sfMsg = "Average sf " + averagesf.toString()
                toast(sfMsg)

                sendFinishInfo(CLASS_SESSION_NM)
            }
        }

        if (!isRetry) {
            this.binding!!.tvTitle.visibility = View.GONE
            this.binding!!.cvIntro.visibility = View.GONE
            this.binding!!.esvScreen.visibility = View.GONE

            var dialog: CommonDialog = CommonDialog()
            var showDelay : Long = 0

            dialog.title = String.format(
                getString(R.string.msg_dialog_train_finish_one_session_title),
                getCurrentBlockStep()
            )

            if (this.binding!!.esvScreen.screenMode == EyeScreenView.MODE_SCREEN_TRAIN) {
                dialog.content = getString(R.string.msg_dialog_train_finish_one_session_content)
                dialog.rightButtonTitle =
                    getString(R.string.msg_dialog_train_finish_one_session_right_button)
            } else {
                dialog.content = getString(R.string.msg_dialog_test_finish_one_session_content)
                dialog.rightButtonTitle =
                    getString(R.string.msg_dialog_test_finish_one_session_right_button)

                if (result != RESULT_START) {

                    // 결과 메세지 출력
                    showResultMsg()
                    
                    // 결과 보여준 후, 일정 시간 후에 다음 진행
                    showDelay = INTERBAL_WAIT_TIME_TEST_RESULT

                } else {
                    showDelay = 0
                }
            }

            dialog.onClickListener = object : CommonDialog.OnClickListener {
                override fun leftClick() {
                }

                // 시력 훈련 마치기 클릭
                override fun rightClick() {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }

                // 백버튼 클릭
                override fun backClick() {
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
            }

            GlobalScope.launch(Dispatchers.Main) {
                delay(showDelay)
                viewModel.hideResult()
                dialog.show(childFragmentManager, "sessionFinishDialog")
            }

        }
    }

    /**
    * 결과 메세지 출력
    * @author 임성진
    * @version 1.0.0
    * @since 2021-08-09 오후 3:01
    **/
    fun showResultMsg(){

        GlobalScope.launch {

            // 점수 계산
            var blockData: List<TestTrial> = DbManager.getInstance()
                .processDao()
                .selectTestTrialInfoByBlock(
                    userSeqId = SharedPrefManager.getUserSeqId(),
                    block = (getCurrentBlockStep() - 1).toString()
                )

            var returnData: ArrayList<HashMap<String, Float>> =
                FragHistory.calculatorTest(blockData) as ArrayList<HashMap<String, Float>>

            var blockScore: HashMap<String, Float> = returnData.get(0)
            var totalCount: HashMap<String, Float> = returnData.get(1)
            var currentAvg: Float = EyeScreenView.TEST_VISUAL_ANGLE_DEGREE

            for (key in blockScore.keys) {
                if (totalCount.get(key)!!.toFloat() > 0) {
                    currentAvg = blockScore.get(key)!!.toFloat() / totalCount.get(key)!!
                        .toFloat()
                }
            }

            var msg = String.format(
                getString(R.string.msg_eye_test_result),
                user.get(0).userId,
                currentAvg
            )

            // 메세지 표시
            lifecycleScope.launch {
                viewModel.showResult(msg)
            }

        }
        
    }
    
    /**
     * 훈련 리스트 생성
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-25 오후 12:41
     **/
    fun createTrainList() {

        // 시력 훈련
        if (this.binding!!.esvScreen.screenMode == EyeScreenView.MODE_SCREEN_TRAIN) {

            // 40개씩 생성
            for (i in 1..BLUR_SIZE) {
                this.trainList.add(EyeScreenView.MODE_TRAIN_BLUR)
            }

            for (i in 1..SHARP_SIZE) {
                this.trainList.add(EyeScreenView.MODE_TRAIN_SHARP)
            }

        } else {    // 시력 진단

            // 20개씩 생성
            for (i in 1..TOP_SIZE) {
                this.trainList.add(EyeScreenView.MODE_TEST_TOP)
            }

            for (i in 1..LEFT_SIZE) {
                this.trainList.add(EyeScreenView.MODE_TEST_LEFT)
            }

            for (i in 1..RIGHT_SIZE) {
                this.trainList.add(EyeScreenView.MODE_TEST_RIGHT)
            }

            for (i in 1..BOTTOM_SIZE) {
                this.trainList.add(EyeScreenView.MODE_TEST_BOTTOM)
            }

        }

        // 랜덤 셔플
        this.trainList = this.trainList.shuffled() as ArrayList<Int>
    }

    /**
     * 돌아가기 click event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 2:39
     **/
    fun clickClose(view: View) {
        this.findNavController().navigateUp()
    }

    /**
     * 훈련시작 click event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 2:39
     **/
    fun clickStart(view: View) {
        if (BleManager.isConnected() && BleManager.isSensorStateNormal() && !this.isNaverStop) {

            if (this.sensorErrorMsg != null && this.sensorErrorMsg!!.isShown) {
                this.sensorErrorMsg!!.dismiss()
                this.sensorErrorMsg = null
            }

            // 훈련 리스트 랜덤 셔플
            createTrainList()

            // 시작 화면 변경
            this.viewModel.showTrainStartUi()

            // 훈련 시작
            nextStep(RESULT_START)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this.timerJob != null) {
            this.timerJob!!.cancel()
        }
    }

    /**
    * 네트워크 연결 상태 확인
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-14 오전 11:38
    **/
    private fun checkNetwork() : Boolean {
        val connectivityManager = requireActivity().getSystemService(ConnectivityManager::class.java)
        val activeNetwork = connectivityManager.activeNetwork
        if (activeNetwork != null) {
            return true
        } else {
            return false
        }
    }

    override fun failScan() {
    }

    override fun findBLe() {
    }

    override fun connectBle() {
    }

    override fun disconnect() {
        sensorError()
    }

    /**
    * 거리 변환시 호출
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-15 오후 2:05
    **/
    override fun distance(data: Int) {

        // 실시간 반영시 주석 해제
        /*GlobalScope.launch(Dispatchers.Main) {
            if (binding!!.esvScreen.isVisible &&
                    !trainList.isNullOrEmpty()) {
                viewModel.drawEyeScreen(EyeDrawVO(trainList.get(getTrainListIndex()), data, 0f))
            }
        }*/

    }

    /**
    * 센서 에러
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-14 오전 10:52
    **/
    override fun sensorError() {
        if (this.sensorErrorMsg == null) {
            this.sensorErrorMsg = Util.showButtonSnackbar(
                this.binding!!.root,
                this.binding!!.root.context,
                this.binding!!.root.context.getString(R.string.msg_error_sensor_error),
                requireContext().getString(R.string.msg_retry),
                View.OnClickListener {
                    findNavController().navigateUp()
                    sensorErrorMsg = null
                },
                Snackbar.LENGTH_INDEFINITE
            )
        }

        // 작업 중지
        if (this.timerJob != null) {
            init()
        }

        this.isNaverStop = true
    }

    override fun sensorNormal() {
  
    }

    override fun onStop() {
        super.onStop()
        BleManager.removeResult(this)
    }
}