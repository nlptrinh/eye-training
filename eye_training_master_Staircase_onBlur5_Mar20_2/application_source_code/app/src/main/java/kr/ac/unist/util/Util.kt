package kr.ac.unist.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Point
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.android.material.snackbar.Snackbar
import kr.ac.unist.R
import kr.ac.unist.fragment.eye.CoordinateInfo
import kr.ac.unist.manager.SharedPrefManager
import java.io.File
import java.io.IOException


/**
 * 유틸
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-13 오후 12:44
 **/
class Util {

    companion object {

        var DEFAULT_SYSTEM_UI_VISIBILITY = -1
        
        // 노티 높이
        var heightNotibar : Int = 0

        /**
        * 초기화
        * @author 임성진
        * @version 1.0.0
        * @since 2021-05-26 오후 12:40
        **/
        fun init(systemUiVisibility : Int){
            this.DEFAULT_SYSTEM_UI_VISIBILITY = systemUiVisibility
        }

        /**
         * 메세지 Snackbar
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:44
         **/
        fun showSimpleSnackbar (view: View, context: Context, msg: String, time: Int) : Snackbar {
            var snackbar = Snackbar.make(view, msg, time)
            snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.black))
            snackbar.setTextColor(ContextCompat.getColor(context, R.color.white))
            snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.rgb_bb86fc))
            snackbar.show()
            return snackbar
        }

        /**
         * 버튼 메세지 Snackbar
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:44
         **/
        fun showButtonSnackbar (view: View, context: Context, msg: String, btnMsg:String, listener : View.OnClickListener, time: Int) : Snackbar {

            var snackbar = Snackbar.make(view, msg, time)
            snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.black))
            snackbar.setAction(btnMsg, listener)
            snackbar.setTextColor(ContextCompat.getColor(context, R.color.white))
            snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.rgb_bb86fc))
            snackbar.show()
            return snackbar
        }

        /**
         * 키보드 숨기기
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:44
         **/
        fun hideKeyboard(view: View) {
            val inputMethodManager = view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        /**
        * 디바이스 넓이 가져오기
        * @author 임성진
        * @version 1.0.0
        * @since 2021-05-25 오후 4:07
        **/
        fun getDisplayWidth(context: Context): List<Int> {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val width: Int
            val height: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = wm.currentWindowMetrics
                val windowInsets: WindowInsets = windowMetrics.windowInsets

                val insets = windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
                val insetsWidth = insets.right + insets.left
                val insetsHeight = insets.top + insets.bottom

                val b = windowMetrics.bounds
                width = b.width() - insetsWidth
                height = b.height() - insetsHeight
            } else {
                val size = Point()
                val display = wm.defaultDisplay // deprecated in API 30
                display?.getSize(size) // deprecated in API 30
                width = size.x
                height = size.y
            }

            return listOf(width, height)
        }

        /**
        * px을 mm으로 변환
        * @author 임성진
        * @version 1.0.0
        * @since 2021-05-25 오후 4:12
        **/
        fun getPxToM(context: Context, px: Int): Float {
            val dm = context.resources.displayMetrics
            return px / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, dm)
        }

        /**
        * full screen 변경
        * @author 임성진
        * @version 1.0.0
        * @since 2021-05-26 오후 12:38
        **/
        fun settingFullScreen(activity : Activity, isFullScreen : Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.window.setDecorFitsSystemWindows(!isFullScreen)
            } else {
                if (isFullScreen) {
                    activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                } else {
                    activity.window.decorView.systemUiVisibility = DEFAULT_SYSTEM_UI_VISIBILITY
                }
            }
        }

        /**
        * csv file 읽기
        * @author 임성진
        * @version 1.0.0
        * @since 2021-06-04 오후 1:46
        **/
        fun readCsvFile(context: Context, rawId : Int) :  List<List<String>>{
            var inputStream = context.resources.openRawResource(rawId)
            return csvReader().readAll(inputStream)
        }

        /**
        * 최대값 찾기
        * @author 임성진
        * @version 1.0.0
        * @since 2021-06-04 오후 2:19
        **/
        fun getMax(A: Array<Float>): Float {
            var max = Float.MIN_VALUE
            for (i in A) {
                max = max.coerceAtLeast(i)
            }
            return max
        }

        /**
        * 최소값 찾기
        * @author 임성진
        * @version 1.0.0
        * @since 2021-06-04 오후 2:19
        **/
        fun getMin(A: Array<Float>): Float {
            var min = Float.MAX_VALUE
            for (i in A) {
                min = min.coerceAtMost(i)
            }
            return min
        }

        fun getMinOver0(A: Array<Float>): Float {
            var min = Float.MAX_VALUE
            for (i in A) {
                if (i >= 0) {
                    min = min.coerceAtMost(i)
                }
            }
            return min
        }

        /**
        * 파일 만들기
        * @author 임성진
        * @version 1.0.0
        * @since 2021-06-14 오후 1:04
        **/
        fun newFile(newFile : File){
            if (!newFile!!.exists()) {
                try {
                    newFile!!.parentFile.mkdirs()
                    newFile!!.createNewFile()
                } catch (e: IOException) {
                    throw e;
                }
            }
        }

        /**
        * Beep Sound play
        * @author 임성진
        * @version 1.0.0
        * @since 2021-06-16 오후 5:13
        **/
        fun playBeep(context : Context, soundResId : Int){
            var volume : Float = SharedPrefManager.getBeepVolume().toFloat() / 100
            val mp: MediaPlayer = MediaPlayer.create(context, soundResId)
            mp.setVolume(volume, volume)
            mp.setOnCompletionListener(OnCompletionListener { mediaPlayer ->
                mediaPlayer.stop()
                mediaPlayer.release()
            })
            mp.start()
        }

        /**
         * Beep Sound play
         * @author 임성진
         * @version 1.0.0
         * @since 2021-06-16 오후 5:13
         **/
        fun playBeepWithVolumn(context : Context, soundResId : Int, iVolume : Int){
            var volume : Float = iVolume.toFloat() / 100
            val mp: MediaPlayer = MediaPlayer.create(context, soundResId)
            mp.setVolume(volume, volume)
            mp.start()
        }

        /**
         * 정답 점수 계산
         * @see wavelength : 1/(spatial frequency) x (자극의 픽셀 사이즈 / 6visual angle)
         * @author 임성진
         * @version 1.0.0
         * @since 2021-06-03 오후 5:03
         **/
        fun calculatorResult(fDistance : Float, wavelength : Float) : Int{

            /** 1. distance와 가장 가까운 x축 인덱스를 뽑습니다 **/
            var xSize : Int = CoordinateInfo.xcooridates.get(0).size - 1
            var i : Int = 0
            var xList : ArrayList<Float> = ArrayList<Float>()

            for (i in 0 .. xSize) {
                xList.add(CoordinateInfo.xcooridates.get(0).get(i).toFloat() - fDistance)
            }

            // 찾은 최소 값
            var minValue : Float = getMinOver0(xList.toFloatArray().toTypedArray())

            if (minValue ==  Float.MAX_VALUE) {
                minValue = xList.get(xList.size - 1)
            }

            // 최소 값 index 위치
            var minValueIndex : Int = xList.indexOf(minValue)

            /** 2. 위에서 구한x 축에서 epl의 y값들을 모두 가져옵니다 **/
            var yList : ArrayList<Float> = ArrayList<Float>()
            xSize = CoordinateInfo.ycooridates.size - 1

            for(i in 0 .. xSize) {
                yList.add(CoordinateInfo.ycooridates.get(i).get(minValueIndex).toFloatOrNull()?:0.0f)
            }

            /** 3. 20개의 epl이 가지는 y값과 input2의 wavelength를 비교해서 가장 가까운 선의 인덱스를 뽑습니다 **/
            var ySize : Int = yList.size - 1
            var resultList : ArrayList<Float> = ArrayList<Float>()

            for (i in 0 .. ySize) {
                resultList.add(yList.get(i) - wavelength)
            }

            // 찾은 최소 값
            minValue = getMinOver0(resultList.toFloatArray().toTypedArray())

            if (minValue ==  Float.MAX_VALUE) {
                minValue = resultList.get(resultList.size - 1)
            }

            // 최소 값 index 위치
            minValueIndex = resultList.indexOf(minValue)

            // index 위치가 점수가 된다.
            return (minValueIndex + 1) // 결과는 index 1 부터 시작하므로
        }

        /**
        * 노티바 높이 구하기
        * @author 임성진
        * @version 1.0.0
        * @since 2021-07-12 오전 10:10
        **/
        fun settingNotificationHeight(ctx : Context) {
            var result = 0
            val resourceId: Int =
                ctx.getResources().getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = ctx.getResources().getDimensionPixelSize(resourceId)
            }
            this.heightNotibar = result
        }

        /**
        * BLE 지원 여부 반환
        * @author 임성진
        * @version 1.0.0
        * @since 2021-07-20 오후 12:29
        **/
        fun hasBleSupport(ctx: Context) : Boolean{
            var pm: PackageManager = ctx.getPackageManager()
            return pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        }

    }
}