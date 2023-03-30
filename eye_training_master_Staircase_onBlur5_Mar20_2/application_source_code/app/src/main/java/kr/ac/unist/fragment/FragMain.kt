package kr.ac.unist.fragment

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.ac.unist.R
import kr.ac.unist.custom.EyeScreenView
import kr.ac.unist.database.DbManager
import kr.ac.unist.database.entity.User
import kr.ac.unist.databinding.FragMainBinding
import kr.ac.unist.manager.BleManager
import kr.ac.unist.manager.SharedPrefManager
import kr.ac.unist.util.Util
import timber.log.Timber
import java.lang.Exception

/**
* Main Screen
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragMain : Fragment(), BleManager.OnResult {

    // 바인딩
    var binding: FragMainBinding? = null

    // BLE 연결 재시도 횟수
    var retryBleConnectCount = 0;

    // 센서 에러 메세지
    var sensorErrorMsg : Snackbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Util.settingFullScreen(requireActivity(), false)

        // 바인딩
        if (this.binding == null) {
            this.binding = DataBindingUtil.inflate<FragMainBinding>(
                inflater, R.layout.frag_main, container, false
            )

            this.binding!!.frag = this

            BleManager.addResult(this)

            // 노티 높이 셋팅
            Util.settingNotificationHeight(requireActivity())

            // 스크린 이동
            if (moveScreen()){

                // 초기화
                init();

            }

            // 유저 정보가 없으면 unist 백도어 삽입
            GlobalScope.launch {
                var firstChceckUser : List<User> = DbManager.getInstance().userDao().getAll()
                if (firstChceckUser.size == 0) {
                    DbManager.getInstance().userDao().insertUserInfo(User("unist",
                        "unist",
                        "unist",
                        "M",
                        "LASIk,etc",
                        "1.7",
                        "돋보기 안경,다초점,다초점 안경",
                        "F",
                        "070913",
                        "N"))
                }
            }
        }

        return this.binding!!.root
    }


    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

    // 스낵바 테스트
    fun test(){
        var snackbar = Snackbar.make(this.binding!!.mainLayout, "확인", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("해결하기", View.OnClickListener {  })
        snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.black))
        snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.rgb_bb86fc))
        snackbar.show()
    }

    /**
     * 초기화
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 1:04
     **/
    fun init(){

        GlobalScope.launch {
            var user : List<User> = DbManager.getInstance().userDao().findUser(SharedPrefManager.getUserSeqId())

            if (user.size == 0) {
                findNavController().navigate(FragMainDirections.actionFragMainToFragIntro())
            } else if(user.get(0).firstAnswer.equals("Y")){
                binding!!.tvName.text = String.format(getString(R.string.user_name, user.get(0).userId))

                retryBleConnectCount = 0

                // 기기가 블루투스를 지원하는지 체크한다.
                if (Util.hasBleSupport(requireContext())) {

                    // 블루투스 활성화 요청
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, BleManager.REQUEST_ENABLE_BT)

                } else {

                    Util.showSimpleSnackbar(binding!!.root,
                        requireContext(),
                        getString(R.string.msg_main_ble_no_supoort), Snackbar.LENGTH_LONG)

                }

            } else {
                findNavController().navigate(FragMainDirections.actionFragMainToFragSurvey1())
            }
        }
    }

    /**
     * 스크린 이동
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 3:06
     **/
    fun moveScreen() : Boolean{

         // 간단 저장소에서 유저 시퀀스 ID를 받는다.
         var userSeqId = SharedPrefManager.getUserSeqId()

         // 없다면, 로그인 화면으로 이동한다.
         if (userSeqId.isEmpty()) {
             this.findNavController().navigate(FragMainDirections.actionFragMainToFragIntro())
             return false
         }

        return true

    }

    /**
     * 개인정보 조회/수정 이동
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 3:06
     **/
    fun clickProfile(view: View) {
        this.findNavController().navigate(FragMainDirections.actionFragMainToFragProfile())
    }

    /**
     * 환경 설정 이동
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 3:06
     **/
    fun clickSetting(view: View) {
        this.findNavController().navigate(FragMainDirections.actionFragMainToFragSetting())
    }

    /**
     * 훈련 기록 이동
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 3:06
     **/
    fun clickHistory(view: View) {
        this.findNavController().navigate(FragMainDirections.actionFragMainToFragHistory())
    }

    /**
     * 시력 훈련 이동
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 3:06
     **/
    fun clickTrain(view: View) {
        if (BleManager.isConnected()) {
            this.findNavController().navigate(FragMainDirections.actionFragMainToFragEyeScreen(EyeScreenView.MODE_SCREEN_TRAIN))
        }
    }

    /**
     * 시력 진단 이동
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 3:06
     **/
    fun clickTest(view: View) {
        if (BleManager.isConnected()) {
            this.findNavController().navigate(FragMainDirections.actionFragMainToFragEyeScreen(EyeScreenView.MODE_SCREEN_TEST))
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BleManager.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            // 권한 체크
            checkPermission()
        }
    }

    /**
     * 권한 체크
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오후 2:39
     **/
    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                BleManager.PERMISSION_REQ_LOCATION
            )
        } else {
            startScan()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == BleManager.PERMISSION_REQ_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan()
            } else {
                showFailBle()
            }
        }
    }

    /**
     * BLE 장치 연결 실패
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오후 2:39
     **/
    fun showFailBle(){
        Util.showButtonSnackbar(this.binding!!.root,
            requireContext(),
            getString(R.string.msg_main_ble_not_found),
            getString(R.string.msg_main_ble_solve),
            View.OnClickListener {
                this.findNavController().navigate(FragMainDirections.actionFragMainToFragSetting())
            }, Snackbar.LENGTH_INDEFINITE)

        binding!!.tvTest.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rgb_61000000))
        binding!!.tvTrain.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rgb_61000000))
    }

    /**
     * BLE 장치 연결 성공
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오후 2:39
     **/
    fun showConnectBle(){
        if (activity != null) {
            requireActivity().runOnUiThread(Runnable {
                Util.showSimpleSnackbar(binding!!.root,
                    requireContext(),
                    getString(R.string.msg_main_ble_connect), Snackbar.LENGTH_LONG)

                // ble state ui
                showConnectState()
            })
        }
    }

    /**
    * ble 연결 상태에 따라서 오른쪽 상단 프로파일을 보여주고 안보여주고를 설정한다
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-28 오후 4:51
    **/
    fun showConnectState(){
        GlobalScope.launch(Dispatchers.Main) {

            try {
                if (BleManager.isConnected()) {
                    binding!!.tvConnectedBle.visibility = View.VISIBLE
                    binding!!.ivProfile.visibility = View.GONE

                    binding!!.tvTest.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.rgb_333333
                        )
                    )
                    binding!!.tvTrain.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.rgb_333333
                        )
                    )
                } else {
                    binding!!.tvConnectedBle.visibility = View.GONE
                    binding!!.ivProfile.visibility = View.VISIBLE
                    binding!!.tvTest.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.rgb_61000000
                        )
                    )
                    binding!!.tvTrain.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.rgb_61000000
                        )
                    )
                }
            } catch (e : Exception){

            }
        }
    }

    override fun onResume() {
        super.onResume()
        showConnectState()
    }
    /**
     * 스캔 시작
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오후 2:39
     **/
    private fun startScan(){

        if (!BleManager.isConnected()) {
            if (activity != null) {
                requireActivity().runOnUiThread(Runnable {
                    Util.showSimpleSnackbar(
                        binding!!.root,
                        requireContext(),
                        getString(R.string.msg_main_ble_find), Snackbar.LENGTH_INDEFINITE
                    )
                })
            }

            // 스캔 시작
            BleManager.scanLeDevice(true, this)
        } else {
            showConnectBle()
        }
    }

    /**
    * 연결 실패
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-15 오전 11:24
    **/
    override fun failScan() {
        disconnect()
    }

    /**
    * 블루투스 찾음
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-15 오전 11:24
    **/
    override fun findBLe() {
    }

    /**
    * 연결됨
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-15 오전 11:24
    **/
    override fun connectBle() {
        showConnectBle()
    }

    /**
    * 연결 끊김
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-15 오전 11:24
    **/
    override fun disconnect() {
        if (retryBleConnectCount >= BleManager.RETRY_CONNECT) {
            retryBleConnectCount = 0
            if (activity != null) {
                GlobalScope.launch(Dispatchers.Main) {
                    showFailBle()
                }
            }
        } else {
            retryBleConnectCount += 1
            Timber.d("retry connect %s", retryBleConnectCount)
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                startScan()
            }
            GlobalScope.launch(Dispatchers.Main) {
                showConnectState()
            }
        }
    }

    /**
    * 측정된 거리
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-15 오전 11:24
    **/
    override fun distance(data: Int) {
    }

    var isBeforeSensorError : Boolean = false

    /**
    * 센서 에러
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-15 오전 11:24
    **/
    override fun sensorError() {
        /*if (this.sensorErrorMsg == null) {
            this.sensorErrorMsg = Util.showButtonSnackbar(
                binding!!.root,
                requireContext(),
                requireContext().getString(R.string.msg_error_sensor_error),
                requireContext().getString(R.string.msg_retry),
                View.OnClickListener {
                    // 상태 이상시, 연결 끊기
                    BleManager.disconnectGattServer()
                    sensorErrorMsg = null
                },
                Snackbar.LENGTH_INDEFINITE
            )

            showConnectState()
        }
        isBeforeSensorError = true*/
    }

    /**
    * 센서 정상 상태
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-15 오전 11:25
    **/
    override fun sensorNormal() {
/*        if (isBeforeSensorError) {
            isBeforeSensorError = false
            if (this.sensorErrorMsg != null) {
                this.sensorErrorMsg!!.dismiss()
                this.sensorErrorMsg = null
            }
            showConnectState()
        }*/
    }

}