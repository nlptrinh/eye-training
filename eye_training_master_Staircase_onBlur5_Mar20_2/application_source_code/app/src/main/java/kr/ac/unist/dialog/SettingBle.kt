package kr.ac.unist.dialog

import android.Manifest
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import kr.ac.unist.R
import kr.ac.unist.databinding.DialogSettingBleBinding
import kr.ac.unist.manager.BleManager
import kr.ac.unist.manager.BleManager.Companion.PERMISSION_REQ_LOCATION
import kr.ac.unist.manager.BleManager.Companion.REQUEST_ENABLE_BT
import timber.log.Timber


/**
 * BLE 장치 설정 Dialog
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-18 오후 5:15
 **/
class SettingBle : DialogFragment() {

    // 바인딩
    lateinit var binding: DialogSettingBleBinding

    // BLE 연결 재시도 횟수
    var retryBleConnectCount = 0;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        this.binding = DataBindingUtil.inflate<DialogSettingBleBinding>(
            inflater, R.layout.dialog_setting_ble, container, false)

        this.binding.dialog = this

        setCancelable(false)

        return this.binding.root

    }

    override fun onStart() {
        super.onStart()

        val width = requireContext()!!.resources.getDimension(R.dimen.dialog_size_width).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // loading bar animation start
        val rotate = RotateAnimation(
            0F,
            360F,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = 1000
        rotate.interpolator = LinearInterpolator()
        rotate.fillAfter = true
        rotate.setRepeatCount(Animation.INFINITE)

        this.binding.ivLoading.startAnimation(rotate)

        // ble 찾기 시작
        if(BleManager.isConnected()) {
            showConnectBle()
        } else {
            showFindBle(getString(R.string.msg_stting_ble_power_on))
        }
    }

    /**
    * BLE 찾는 중 UI 활성화
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-20 오후 2:39
    **/
    fun showFindBle(message : String){
        this.binding.tvMessage.text = message
        this.binding.llFindBle.visibility = View.VISIBLE
        this.binding.tvClose.visibility = View.VISIBLE
        this.binding.tvComplate.visibility = View.GONE
        this.binding.llLink.visibility = View.GONE
        this.binding.llConnectBle.visibility = View.GONE
        this.binding.llFail.visibility = View.GONE

        // 블루투스 활성화 요청
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            // 권한 체크
            checkPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQ_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan()
            } else {
                showFailBle()
            }
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
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQ_LOCATION)
        } else {
            startScan()
        }
    }

    /**
     * 스캔 시작
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오후 2:39
     **/
    private fun startScan(){
        // 스캔 시작
        BleManager.scanLeDevice(true, object : BleManager.OnResult{
            
            // 연결 실패
            override fun failScan() {
                if (activity != null) {
                    requireActivity().runOnUiThread(Runnable {
                        showFailBle()
                    })
                }
            }

            // 블루투스 찾음
            override fun findBLe() {
                if (activity != null) {
                    requireActivity().runOnUiThread(Runnable {
                        showFindBle(getString(R.string.msg_stting_ble_find))
                    })
                }
            }

            // 연결됨
            override fun connectBle() {
                if (activity != null) {
                    requireActivity().runOnUiThread(Runnable {
                        showConnectBle()
                    })
                }
            }

            // 연결 끊김
            override fun disconnect() {
                if (retryBleConnectCount > BleManager.RETRY_CONNECT) {
                    retryBleConnectCount=0
                    if (activity != null) {
                        requireActivity().runOnUiThread(Runnable {
                            showFailBle()
                        })
                    }
                } else {
                    retryBleConnectCount+=1
                    Timber.d("retry connect %s", retryBleConnectCount)
                    if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                        startScan()
                    }
                }
            }

            override fun distance(data: Int) {
            }

            override fun sensorError() {
            }

            override fun sensorNormal() {
            }
        })
    }

    /**
     * BLE 찾기 성공 UI 활성화
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오후 2:39
     **/
    fun showConnectBle(){
        this.binding.tvMessage.text = "BLE 장치의 전원을 켜주세요."
        this.binding.llFindBle.visibility = View.GONE
        this.binding.tvClose.visibility = View.GONE
        this.binding.tvComplate.visibility = View.VISIBLE
        this.binding.llLink.visibility = View.GONE
        this.binding.llConnectBle.visibility = View.VISIBLE
        this.binding.llFail.visibility = View.GONE
        this.retryBleConnectCount= 0
    }

    /**
     * BLE 찾기 실패 UI 활성화
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오후 2:39
     **/
    fun showFailBle(){
        this.binding.tvMessage.text = "BLE 장치를 찾을 수 없습니다."
        this.binding.llFindBle.visibility = View.GONE
        this.binding.tvClose.visibility = View.GONE
        this.binding.tvComplate.visibility = View.GONE
        this.binding.llLink.visibility = View.VISIBLE
        this.binding.llConnectBle.visibility = View.GONE
        this.binding.llFail.visibility = View.VISIBLE
        isCancelable = true
        this.retryBleConnectCount = 0
    }

    override fun onStop() {
        super.onStop()
    }

    /**
     * 돌아가기 event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-18 오후 5:15
     **/
    fun clickClose(view: View) {
        dismiss()
    }

    /**
     * 완료 event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-18 오후 5:15
     **/
    fun clickComplate(view: View) {
        dismiss()
    }

    /**
     * 로그 리포트 및 장치 안내 event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-18 오후 5:15
     **/
    fun clickLink(view: View) {
        dismiss()
    }
}