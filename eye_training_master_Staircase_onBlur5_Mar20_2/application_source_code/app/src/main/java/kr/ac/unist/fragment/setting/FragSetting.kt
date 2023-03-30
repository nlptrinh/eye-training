package kr.ac.unist.fragment.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.ac.unist.R
import kr.ac.unist.database.DbManager
import kr.ac.unist.databinding.FragSettingBinding
import kr.ac.unist.dialog.SettingBle
import kr.ac.unist.dialog.SettingBreakTime
import kr.ac.unist.dialog.SettingDbBackup
import kr.ac.unist.dialog.SettingVolume
import kr.ac.unist.fragment.eye.FragEyeScreen
import kr.ac.unist.manager.BleManager
import kr.ac.unist.manager.SharedPrefManager
import kr.ac.unist.util.Util


/**
* 환경 설정 Screen
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragSetting : Fragment() {

    // 바인딩
    lateinit var binding: FragSettingBinding
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Util.settingFullScreen(requireActivity(), false)
        this.binding = DataBindingUtil.inflate<FragSettingBinding>(
                inflater, R.layout.frag_setting, container, false)
        this.binding.frag = this
        
//        if (BleManager.isConnected()) {
//            this.binding.tvConnectedBle.visibility = View.GONE
//        } else {
//            this.binding.tvConnectedBle.visibility = View.GONE
//        }
//
        return this.binding.root
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

    /**
     * 블록 사이 휴식 시간 설정 event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-18 오후 5:15
     **/
    fun clickBreakTime(view: View) {
        var dialog = SettingBreakTime()
        dialog.show(childFragmentManager, "BreakTime")
    }

    /**
    * 알림음 크기 조절 event
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-20 오전 9:42
    **/
    fun clickVolumn(view: View) {
        var dialog = SettingVolume()
        dialog.show(childFragmentManager, "Volumn")
    }

    /**
     * BLE 설정 event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오전 9:42
     **/
    fun clickBle(view: View) {
        var dialog = SettingBle()
        dialog.show(childFragmentManager, "BLE")
    }


    /**
     * BLE DB Backup event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오전 9:42
     **/
    fun clickDbBackup(view: View) {
        var dialog = SettingDbBackup()
        dialog.show(childFragmentManager, "DB_BACKUP")
    }

    /**
     * BLE DB Clean event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오전 9:42
     **/
    fun clickDbClean(view: View) {

        GlobalScope.launch {
            DbManager.getInstance().processDao().deleteTrainTrial()
            DbManager.getInstance().processDao().deleteTestTrial()
            SharedPrefManager.setCurrentFrequncy(FragEyeScreen.DEFAULT_CYCLE)
            GlobalScope.launch(Dispatchers.Main) {
                Util.showSimpleSnackbar(binding.root, requireContext(), getString(R.string.msg_db_clean), Snackbar.LENGTH_SHORT)
            }
        }

    }

    /**
     * 로그아웃 event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오전 9:42
     **/
    fun clickLogout(view: View) {
        SharedPrefManager.setUserSeqId("")
        BleManager.disconnectGattServer()
        this.findNavController().navigate(FragSettingDirections.actionFragSettingToFragIntro())
    }
}