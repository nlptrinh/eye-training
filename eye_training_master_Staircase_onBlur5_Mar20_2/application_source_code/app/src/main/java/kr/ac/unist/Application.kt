package kr.ac.unist

import android.app.Application
import kr.ac.unist.fragment.network.Network
import kr.ac.unist.database.DbManager
import kr.ac.unist.fragment.eye.CoordinateInfo
import kr.ac.unist.manager.BleManager
import kr.ac.unist.manager.SharedPrefManager
import kr.ac.unist.util.Util
import timber.log.Timber

/**
* Application
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 12:26
**/
class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        // lsetting loeg library
        Timber.plant(Timber.DebugTree())

        // network init
        Network.init()

        // 간단 저장소 init
        SharedPrefManager.init(this)

        // 로컬 DB
        DbManager.init(this)

        // 블루투스 지원 여부 체크
        if (Util.hasBleSupport(this)) {
            // 블루투스
            BleManager.init(this)
        }
        
        // 좌표 정보를 미리 읽어 놓는다.
        CoordinateInfo.readData(applicationContext)
    }

}