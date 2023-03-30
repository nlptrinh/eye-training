package kr.ac.unist.fragment.eye

import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.ac.unist.R
import kr.ac.unist.util.Util
import timber.log.Timber

/**
* 좌표 정보
* @author 임성진
* @version 1.0.0
* @since 2021-06-04 오후 2:03
**/
class CoordinateInfo {

    companion object {

        // x 좌표 정보
        lateinit var xcooridates : List<List<String>>

        // y 좌표 정보
        lateinit var ycooridates : List<List<String>>

        /**
        * x, y 좌표 정보를 읽는다.
        * @author 임성진
        * @version 1.0.0
        * @since 2021-06-04 오후 2:06
        **/
        fun readData(context : Context){

            GlobalScope.launch {
                // x 좌표 정보 셋팅
                xcooridates = Util.readCsvFile(context, R.raw.xcoordinates)

                // y 좌표 정보 셋팅
                ycooridates = Util.readCsvFile(context, R.raw.ycoordinates)
            }

        }

    }

}