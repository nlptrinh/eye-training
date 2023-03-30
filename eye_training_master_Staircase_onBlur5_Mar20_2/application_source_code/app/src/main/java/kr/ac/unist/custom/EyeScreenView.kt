package kr.ac.unist.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kr.ac.unist.Constants
import kr.ac.unist.R
import kr.ac.unist.manager.BleManager
import kr.ac.unist.manager.SharedPrefManager
import kr.ac.unist.util.BoxBlur
import kr.ac.unist.util.Util
import kotlin.math.min
import kotlin.math.tan
import java.util.Random;
import kotlin.math.log

/**
* Train view
* @author 임성진
* @version 1.0.0
* @since 2021-05-24 오후 3:57
**/
class EyeScreenView @JvmOverloads constructor(context: Context,
                                              attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr), BleManager.OnResult {


    interface EyeScreenModeChangeListener {
        fun onModeChanged(mode: Int)
    }

    private var modeChangeListener: EyeScreenModeChangeListener? = null
    var mode = MODE_TRAIN_BLUR
        set(value) {
            field = value
            invalidate()
            modeChangeListener?.onModeChanged(value)
        }

    fun setModeChangeListener(listener: EyeScreenModeChangeListener) {
        modeChangeListener = listener
    }

    companion object {
        // 스크린 모드
        val MODE_SCREEN_TRAIN = 0                  // 시력 훈련
        val MODE_SCREEN_TEST = 1                   // 시력 진단

        //val TRAIN_VISUAL_ANGLE_DEGREE : Float = 6.0f     // 시력 훈련 8도 고정
        val TRAIN_VISUAL_ANGLE_DEGREE : Float = 20.0f     // 시력 훈련 8도 고정
        val TEST_VISUAL_ANGLE_DEGREE : Float = 2.0f      // 시력 진단 2도 부터 시작

        //val DEFAULT_FREQUENCY = 2.0f                // 2 cycle
        //val MIN_FREQUENCY = 0.75f                   // 최소 freq

        //liza
        val DEFAULT_FREQUENCY = 10.0f                // 2 cycle
        val MIN_FREQUENCY = 10.0f                  // 최소 freq
        val MAX_FREQUENCY = 100.0f

        val MIN_TEST_VISUAL_ANGLE_DEGREE = 0.1f     // 시력 진단 최소 0.1 degree
        val MAX_TEST_VISUAL_ANGLE_DEGREE = 2.0f     // 시력 진단 최대 2.0 degree

        val MODE_TRAIN_SHARP = 0                    // Square wave
        val MODE_TRAIN_BLUR = 1                     // Sine wave
        
        val MODE_TEST_TOP = 0                       // 열린방향 위쪽 각도
        val MODE_TEST_LEFT = 270                    // 열린방향 왼쪽 각도
        val MODE_TEST_RIGHT = 90                    // 열린방향 오른쪽 각도
        val MODE_TEST_BOTTOM = 180                  // 열린방향 아래 각도
        
        val MODE_NONE = 2                           // none
        val MODE_TRAIN_SHARP_NM = "sharp"
        val MODE_TRAIN_BLUR_NM = "blunt"

        val MODE_TEST_TOP_NM = "up"
        val MODE_TEST_LEFT_NM = "left"
        val MODE_TEST_RIGHT_NM = "right"
        val MODE_TEST_BOTTOM_NM = "down"
        val MODE_NONE_NM = "non"

    }

    // 스크린 모드
    var screenMode = MODE_SCREEN_TRAIN

    // 화면에 그려질 bitmap
    private var bitmap : Bitmap? = null
    private var sineBitmap : Bitmap? = null



    // 사인파 주파수
    var frequency : Float = DEFAULT_FREQUENCY

    // 센서 거리(mm)
    var distance : Int = 0

    // 그려질 MODE
    //var mode = MODE_TRAIN_BLUR

    // 계산된 pixcel width
    var calPixcelWidth = 0

    // 계산된 각도
    var calRadian = 0.0

    // 시력 훈련 : 고정 8, 시력 진단 : 시작 2도
    var visualAngleDegree = TRAIN_VISUAL_ANGLE_DEGREE

    // cycle 추가 요청한 횟수
    var addCycleCount : Int = 0

    // 원형 그라디언트를 위한
    private var gradientRadius = 0f
    private val gradientColors =
        intArrayOf(Color.BLACK, Color.TRANSPARENT, Color.TRANSPARENT)
    //private val gradientColors =
    //intArrayOf(Color.BLACK, Color.TRANSPARENT, Color.BLACK, Color.TRANSPARENT)
    private val gradientStops = floatArrayOf(0.6f, 1.0f,
        1.0f)
    //private val gradientStops = floatArrayOf(0.65f, 0.85f, 0.95f, 1.0f)
    //private val gradientStops = floatArrayOf(1.0f, 0.95f, 0.85f, 0.65f)
    // test
    /*private val gradientStops = floatArrayOf(1.0f,
        1.0f)*/

    // sine wave 그리기 여부
    public var isDrawSinewave = false

    init {
        // ble으로 부터 수신 받을 리스너 설정
        BleManager.addResult(this)
    }

    fun init(screenMode : Int){
        this.screenMode = screenMode

        if (screenMode == MODE_SCREEN_TRAIN) {
            visualAngleDegree = TRAIN_VISUAL_ANGLE_DEGREE
        } else {
            visualAngleDegree = TEST_VISUAL_ANGLE_DEGREE
        }

        // 화면에 그려질 bitmap
        this.bitmap = null

        // 사인파 주파수
        this.frequency = DEFAULT_FREQUENCY

        // 센서 거리(mm)
        this.distance = 0

        // 계산된 pixcel width
        this.calPixcelWidth = 0

        // 계산된 각도
        this.calRadian = 0.0

        // cycle 추가 요청한 횟수
        this.addCycleCount = 0

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    /**
    * 시작 Freq 설정
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-27 오후 6:50
    **/
    fun setStartFrequency(freq : String) {
        this.frequency = freq.toFloat()
    }

    override fun onDraw(canvas: Canvas?) {

        if (!BleManager.isSensorStateNormal()) {
            super.onDraw(canvas)
        } else {

            // frequncy 최소값 제한
            if (this.frequency <= MIN_FREQUENCY) {
                this.frequency = MIN_FREQUENCY
            }
            // liza
            if (this.frequency >= MAX_FREQUENCY) {
                this.frequency = MAX_FREQUENCY
            }
            // pixcel size와 각도를 구한다
            var result: List<Double> = calculatorPhysicalSize()
            var width = result.get(0).toInt()
            var radians = result.get(1)

            // 시력 훈련 freq 저장
            if (this.screenMode == MODE_SCREEN_TRAIN) {
                SharedPrefManager.setCurrentFrequncy(this.frequency)
            }

            if (width > 0) {

                this.calPixcelWidth = width

                // 시력 훈련 모드
                if (this.screenMode == MODE_SCREEN_TRAIN) {


                    var size = width * width // width * height (비율 유지)

                    this.calRadian = radians

                    // 화면에 그려질 비트맵 만들기
                    if (this.bitmap != null) {
                        this.bitmap!!.recycle()
                        this.bitmap = null
                    }

                    val random = Random()

                    if (mode == MODE_TRAIN_SHARP) {

                        val imageNames = arrayOf("i2", "i3", "i4", "i6", "i8", "i9", "i10")
                        //val imageNames = arrayOf("ri11_n", "ri10_n")
                        val randomIndex = (Math.random() * imageNames.size).toInt()
                        val selectedImageName = imageNames[randomIndex]
                        val tag = "selected_Image_in_Current_Trial (Sharp)"
                        Log.d(tag, "++++++++++++++++++++++++++++++++++++++++Selected image name: $selectedImageName")
                        val packageName = "kr.ac.unist"
                        val imageId = resources.getIdentifier(selectedImageName, "drawable", packageName)
                        val selectedImage = BitmapFactory.decodeResource(resources, imageId)
                        this.bitmap = BitmapFactory.decodeResource(resources, imageId)


                            } else {    // MODE_BLUR
                            val imageIndex = (this.frequency).toInt()
                            val imageNames = arrayOf("i2", "i3", "i4", "i6", "i8", "i9", "i10")

                            val randomIndex = (Math.random() * imageNames.size).toInt()
                            val randomImageName = imageNames[randomIndex]
                            //val selectedImageName = "i5_cutoff_50"
                            val selectedImageName = randomImageName + "_cutoff_" + imageIndex
                            val tag = "selected_Image_in_Current_Trial (Blur)"
                            Log.d(tag, "++++++++++++++++++++++++++++++++++++++++Selected image name: $selectedImageName")
                            val packageName = "kr.ac.unist"
                            val imageId = resources.getIdentifier(selectedImageName, "drawable", packageName)
                            val selectedImage = BitmapFactory.decodeResource(resources, imageId)
                            this.bitmap = BitmapFactory.decodeResource(resources, imageId)
                          }

                    //this.bitmap = Bitmap.createScaledBitmap(this.bitmap!!, width, width, false)
                    this.bitmap = Bitmap.createScaledBitmap(bitmap!!, width, width, false)

                    // 화면 그리기 (중앙)
                    var centreX = (canvas!!.width - bitmap!!.width) / 2
                    var centreY = (canvas!!.height - bitmap!!.height) / 2

                    canvas!!.drawBitmap(
                        bitmap!!,
                        centreX.toFloat(),
                        centreY.toFloat(),
                        null
                    )

                } else {

                    if (this.bitmap != null) {
                        this.bitmap!!.recycle()
                        this.bitmap = null
                    }

                    // drawable bitmap 받기
                    this.bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_icon_test)

                    // 크기를 재설정한다.
                    this.bitmap = Bitmap.createScaledBitmap(this.bitmap!!, width, width, false)

                    // 화면 그리기 (중앙)
                    var centreX = (canvas!!.width - bitmap!!.width) / 2
                    var centreY = (canvas!!.height - bitmap!!.height) / 2

                    canvas!!.drawBitmap(
                        bitmap!!,
                        centreX.toFloat(),
                        centreY.toFloat(),
                        null
                    )

                    // 그려질 각도 전환
                    rotation = mode.toFloat()

                }
            }
        }
    }

    /**
    * 그리기 시도
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-25 오후 12:28
    **/
    fun draw(mode: Int, distance: Int, addCycle: Float): Boolean {
        if (BleManager.isSensorStateNormal()) {

            this.mode = mode
            this.distance = distance

            if (addCycle > 0f) {
                this.addCycleCount += 1
                val tag = "MyApp"
                Log.d(tag, "++++++++++++++++++++++++++++++++++++++++  mode: train, cycle count: $addCycleCount, addcycle:$addCycle")
            } else if (addCycle < 0f) {
                this.addCycleCount = 2
                val tag = "MyApp"
                Log.d(tag, "++++++++++++++++++++++++++++++++++++++++  mode: train, cycle count: $addCycleCount, addcycle:$addCycle")
            }

            // Update values when in MODE_SCREEN_TRAIN
            if (this.screenMode == MODE_SCREEN_TRAIN) {
                when (this.mode) {
                    MODE_TRAIN_BLUR -> {
                        if ((this.addCycleCount % 2) == 0 && addCycle != 0f) {
                            if (addCycle < 0) {
                                this.frequency -= (addCycle * -1)
                                val tag = "MyApp"
                                Log.d(tag, "++++++++++++++++++++++++++++++++++++++++ mode:blur, frequency: $frequency ,addCycle is: $addCycle, addCycleCount: $addCycleCount")

                            } else {
                                this.frequency += addCycle
                                val tag = "MyApp"
                                Log.d(tag, "++++++++++++++++++++++++++++++++++++++++ mode:blur, frequency: $frequency ,addCycle is: $addCycle, addCycleCount: $addCycleCount")
                            }
                        }
                    }
                    MODE_TRAIN_SHARP -> {
                        //if ((this.addCycleCount % 2) != 0) {
                            this.addCycleCount = 1 //this.addCycleCount += 1
                            val tag = "MyApp"
                            Log.d(tag, "++++++++++++++++++++++++++++++++++++++++ mode:sharp, frequency: $frequency ,addCycle is: $addCycle, addCycleCount: $addCycleCount")
                        //}
                    }
                }
            }

            invalidate()
            return true
        } else {
            return false
        }
    }



    /**
    * 동그란 이미지 처리
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-25 오후 12:27
    **/
    fun getCroppedBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color

        gradientRadius = min(bitmap.width, bitmap.height) / 2f
        paint.shader = shaderFactory(gradientRadius, gradientRadius, gradientColors, gradientStops)

        canvas.drawCircle(
            (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
            (bitmap.width / 2).toFloat(), paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        paint.isAntiAlias = true
        paint.isDither = true
        paint.isFilterBitmap = true

        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    /**
    * 원형 그라디언트를 위한 Factory
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-26 오전 10:45
    **/
    private fun shaderFactory(centerX: Float, centerY: Float, colors: IntArray, stops: FloatArray) =
        RadialGradient(
            centerX, centerY, min(centerX, centerY), colors, stops, Shader.TileMode.CLAMP
        )

    /**
    * Physical size 정보 반환
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-26 오전 10:44
    **/
    fun calculatorPhysicalSize() : List<Double>{

        // 시력 진단 모드이고, VISUAL_ANGLE_DEGREE 가 최소 이하로 조정이 되지 않도록 한다.
        if (this.screenMode == MODE_SCREEN_TEST) {
            if (this.visualAngleDegree <= MIN_TEST_VISUAL_ANGLE_DEGREE){
                this.visualAngleDegree = MIN_TEST_VISUAL_ANGLE_DEGREE
            } else if (this.visualAngleDegree >= MAX_TEST_VISUAL_ANGLE_DEGREE){
                this.visualAngleDegree = MAX_TEST_VISUAL_ANGLE_DEGREE
            }
        }

        // 스마트폰 화면의 가로축 해상도
        var screenSizeInfo : List<Int> = Util.getDisplayWidth(context)
        var screenWidthPx = screenSizeInfo.get(0)
        var screenHightPx = screenSizeInfo.get(1) - (Util.heightNotibar*2)
        var screenWidthCm = Util.getPxToM(context, screenWidthPx) / 10

        // 단위(1cm)당 pixel 수
        var cmToPixcel = screenWidthPx / screenWidthCm

        // 각도
        var radians = Math.toRadians(this.visualAngleDegree.toDouble() / 2)

        // visual angle이 고정되었을 때 주어진 거리에 따른 자극의 크기 (cm)
        //var physicalSizeCm  = 1.00 // tan(radians) * (this.distance/10f)
        var physicalSizeCm  = tan(radians) * 50 //liza

        // 자극의 크기를 pixel으로 전환
        var physicalSizePixel = (physicalSizeCm*2) * cmToPixcel;

        if (this.distance == 0) {
            physicalSizePixel = screenHightPx.toDouble()
        } else if (physicalSizePixel > screenHightPx) {
            physicalSizePixel = screenHightPx.toDouble()
        }

        // 자극 pixcel 크기 및 각도 반환
        return listOf(physicalSizePixel, radians)

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
     * BlE으로 부터 측정된 거리
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-26 오전 10:44
     **/
    override fun distance(data: Int) {
        this.distance = data
    }

    /**
     * BlE 센서 오류
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-26 오전 10:44
     **/
    override fun sensorError() {
    }

    /**
     * BlE 센서 정상
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-26 오전 10:44
     **/
    override fun sensorNormal() {
    }

}