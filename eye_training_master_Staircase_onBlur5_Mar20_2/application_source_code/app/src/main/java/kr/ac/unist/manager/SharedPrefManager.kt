package kr.ac.unist.manager

import android.annotation.SuppressLint
import android.content.Context
import kr.ac.unist.fragment.eye.FragEyeScreen

/**
* 간단 저장소
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 12:44
**/
class SharedPrefManager {

    companion object {

        private val USER_INFO = "USER_INFO";        // 유저 정보
        private val USER_SEQ_ID = "USER_SEQ_ID";    // 유저 DB SEQ ID
        private val BREAK_TIME = "BREAK_TIME";      // 블루 사이 휴식 시간
        private val BEEP_VOLUME = "VOLUME";         // 비프 소리 볼륨
        private val CURRENT_FREQ = "CURRENT_FREQ";  // 현재 frequency

        private lateinit var context : Context;              // context

        /**
         * 생성자
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:55
         **/
        fun init(context: Context) {
            Companion.context = context;
        }


        /**
         * user Seq id 저장
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:51
         **/
        fun setUserSeqId(userId: String) {
            @SuppressLint("WrongConstant") val prefs = context.getSharedPreferences(
                USER_INFO,
                Context.MODE_PRIVATE
            )

            val editor = prefs.edit()

            editor.putString(USER_SEQ_ID, userId)
            editor.commit()
        }

        /**
         * User Seq Id 반환
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:51
         **/
        fun getUserSeqId() : String{
            var userId : String = ""

            try {
                @SuppressLint("WrongConstant") val prefs = context.getSharedPreferences(
                    USER_INFO,
                    Context.MODE_PRIVATE
                )
                userId = prefs.getString(USER_SEQ_ID, "").toString()
            } catch (ex: Exception) {
                userId = ""
            } finally {
                return userId
            }
        }

        /**
         * 블루 사이 휴식 시간 저장
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:51
         **/
        fun setBreakTime(breakTime: String) {

            var userId =  getUserSeqId()

            @SuppressLint("WrongConstant") val prefs = context.getSharedPreferences(
                userId,
                Context.MODE_PRIVATE
            )

            val editor = prefs.edit()

            editor.putString(BREAK_TIME, breakTime)
            editor.commit()
        }

        /**
         * 블루 사이 휴식 시간 반환
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:51
         **/
        fun getBreakTime() : String{

            var userId =  getUserSeqId()

            var breakTime : String = "10초"

            try {
                @SuppressLint("WrongConstant") val prefs = context.getSharedPreferences(
                    userId,
                    Context.MODE_PRIVATE
                )
                breakTime = prefs.getString(BREAK_TIME, "10초").toString()
            } catch (ex: Exception) {
            } finally {
                return breakTime
            }
        }

        /**
        * 비프 볼륨 설정
        * @author 임성진
        * @version 1.0.0
        * @since 2021-06-16 오후 5:42
        **/
        fun setBeepVolume(volume: Int) {

            var userId =  getUserSeqId()

            @SuppressLint("WrongConstant") val prefs = context.getSharedPreferences(
                userId,
                Context.MODE_PRIVATE
            )

            val editor = prefs.edit()

            editor.putInt(BEEP_VOLUME, volume)
            editor.commit()
        }

        /**
         * 비프 볼륨 반환
         * @author 임성진
         * @version 1.0.0
         * @since 2021-06-16 오후 5:42
         **/
        fun getBeepVolume() : Int{

            var userId =  getUserSeqId()

            var beepVolume : Int = 50

            try {
                @SuppressLint("WrongConstant") val prefs = context.getSharedPreferences(
                    userId,
                    Context.MODE_PRIVATE
                )
                beepVolume = prefs.getInt(BEEP_VOLUME, beepVolume)
            } catch (ex: Exception) {
            } finally {
                return beepVolume
            }
        }

        /**
        * 현재 훈련 진행 중인 Frequency
        * @author 임성진
        * @version 1.0.0
        * @since 2021-06-28 오후 12:32
        **/
        fun setCurrentFrequncy(freq: Float) {

            var userId =  getUserSeqId()

            @SuppressLint("WrongConstant") val prefs = context.getSharedPreferences(
                userId,
                Context.MODE_PRIVATE
            )

            val editor = prefs.edit()

            editor.putFloat(CURRENT_FREQ, freq)
            editor.commit()
        }

        /**
         * 현재 훈련 진행 중인 Frequency
         * @author 임성진
         * @version 1.0.0
         * @since 2021-06-28 오후 12:32
         **/
        fun getCurrentFrequncy() : Float{

            var userId =  getUserSeqId()

            var currentFrequency : Float = FragEyeScreen.DEFAULT_CYCLE

            try {
                @SuppressLint("WrongConstant") val prefs = context.getSharedPreferences(
                    userId,
                    Context.MODE_PRIVATE
                )
                currentFrequency = prefs.getFloat(CURRENT_FREQ, currentFrequency)
            } catch (ex: Exception) {
            } finally {
                return currentFrequency
            }
        }

    }

}