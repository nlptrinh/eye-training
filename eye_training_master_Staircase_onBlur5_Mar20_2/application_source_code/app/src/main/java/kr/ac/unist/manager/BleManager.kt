package kr.ac.unist.manager

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.*
import kr.ac.unist.BuildConfig
import kr.ac.unist.util.BluetoothUtils.Companion.findResponseCharacteristic
import timber.log.Timber
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


/**
 * BLE 관련 매니저
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-13 오후 12:44
 **/
class BleManager {

    interface OnResult {
        // 스캔 실패
        fun failScan()

        // 블루투스 찾음
        fun findBLe()

        // 블루투스 연결 성공
        fun connectBle()

        // 블루투스 연결 끊김
        fun disconnect()
        
        // 측정된 거리
        fun distance(data : Int)

        // 센서 에러
        fun sensorError()
        
        // 센서 정상
        fun sensorNormal()
    }

    companion object {

        const val INIT_DISTANCE = "0"

        // 센서 정상 상태
        const val STATE_SENSOR_RUN = "0"
        const val STATE_SENSOR_NOT_RUN = "1"

        // 블루투스 요청 코드
        const val REQUEST_ENABLE_BT = 1010
        const val PERMISSION_REQ_LOCATION = 1011
        
        // 읽어들인 데이터 큐 최대 사이즈
        const val READ_DATA_QUEUE_MAX_SIEZ = 5

        // 재시도 횟수
        const val RETRY_CONNECT = 5;

        // BLU 이름
        const val BLE_NAME = "CHIPSEN"

        // 센서 정상 상태 코드
        const val BLE_STATE_NOMAL = "0"
        
        // uuid
        const val SERVICE_STRING = "0000fff0-0000-1000-8000-00805f9b34fb"
        const val CHARACTERISTIC_COMMAND_STRING = "0000fff2-0000-1000-8000-00805f9b34fb"
        const val CHARACTERISTIC_RESPONSE_STRING = "0000fff1-0000-1000-8000-00805f9b34fb"

        val SERVICE_UUID: UUID = UUID.fromString(SERVICE_STRING)

        // 스캔 시간
        private const val SCAN_PERIOD: Long = 20000L

        lateinit var context: Context

        lateinit var bleManager: BluetoothManager
        lateinit var adapter: BluetoothAdapter
        lateinit var scanFilterList: List<ScanFilter>
        lateinit var scanSettings: ScanSettings

        // ble 스캐너
        lateinit var bleScanner: BluetoothLeScanner

        // 스캔 중 여부
        var scanning: Boolean = false
        lateinit var scanJob : Job

        // 결과 리스너
        private val onResult: ArrayList<OnResult> = ArrayList<OnResult>()

        // gatt
        var bluetoothGatt: BluetoothGatt? = null

        // 블루투스 연결 상태
        var bleStatus: Int = BluetoothProfile.STATE_CONNECTED
        var bleSensorState : String = BLE_STATE_NOMAL

        // 이전 센서 상태
        var beforeSensorState : String = ""

        // 읽어들인 데이터 큐
        var readDataQueue : LinkedList<String> = LinkedList<String>()

        /**
         * 초기화
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:44
         **/
        fun init(context: Context) {
            this.context = context
            this.bleSensorState = BLE_STATE_NOMAL
//            getBlueManager()
//            getAdapter()
//            getScanner()
            this.scanFilterList = listOf(
                ScanFilter.Builder()
                    .setDeviceName(BLE_NAME)
                    .setServiceUuid(ParcelUuid(SERVICE_UUID)).build()
            )
            this.scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()
        }

        /**
         * 블루투스 매니저 반환
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:44
         **/
        fun getBlueManager() {
            this.bleManager =
                this.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }

        /**
         * 어뎁터 반환
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:44
         **/
        fun getAdapter() {
            this.adapter = this.bleManager.adapter
        }

        /**
         * 스캐너 반환
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:44
         **/
        fun getScanner() {
            this.bleScanner = this.adapter.getBluetoothLeScanner();
        }

        /**
         * 결과 리스너 추가
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:44
         **/
         fun addResult(resultListener: OnResult) {

            var result : Int = this.onResult.indexOf(resultListener)

            if (result != null && result >= 0) {
                this.onResult.removeAt(result)
            }

            this.onResult.add(resultListener)

        }
        /**
         * 결과 리스너 삭제
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:44
         **/
        fun removeResult(resultListener: OnResult) {

            var result : Int = this.onResult.indexOf(resultListener)

            if (result != null && result >= 0) {
                this.onResult.removeAt(result)
            }

        }

        /**
         * ble 스캔
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:44
         **/
        fun scanLeDevice(enable: Boolean, resultListener: OnResult) {

            // 리스너 등록
            addResult(resultListener)

            when (enable) {
                true -> {

                    if (::scanJob.isInitialized && scanJob != null) {
                        scanJob.cancel()
                    }

                    scanJob = GlobalScope.launch {
                        delay(SCAN_PERIOD)
                        if (scanning && isActive) {
                            scanning = false
                            bleScanner.stopScan(scanCallback)
                            resultListener.failScan()
                        }
                    }
                    scanning = true
                    this.bleScanner.stopScan(scanCallback)
                    this.bleScanner.startScan(this.scanFilterList, this.scanSettings, scanCallback)
                }
                else -> {
                    scanning = false
                    bleScanner.stopScan(scanCallback)
                }
            }
        }

        /**
         * ble 결과 callback
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-13 오후 12:44
         **/
        private val scanCallback: ScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                processResult(result)
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                for (result in results) {
                    processResult(result)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                if (onResult != null) {
                    for (i in 0 .. onResult.size - 1) {
                        onResult.get(i).failScan()
                    }
                }
            }

            private fun processResult(result: ScanResult) {
                if (scanning) {
                    scanning = false
                    bleScanner.stopScan(this)

                    if (onResult != null) {
                        for (i in 0 .. onResult.size - 1) {
                            onResult.get(i).findBLe()
                        }
                    }

                    Timber.d("ble info : %s", result.device.name)
                    Timber.d("ble info : %s", result.device.address)
                    bluetoothGatt = result.device.connectGatt(context, false, gattClientCallback)
                }
            }
        }

        /**
         * BLE gattClientCallback
         */
        private val gattClientCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                bleStatus = newState
                if (status == BluetoothGatt.GATT_FAILURE) {
                    disconnectGattServer()
                    return
                } else if (status != BluetoothGatt.GATT_SUCCESS) {
                    disconnectGattServer()
                    return
                }
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    if (onResult != null) {
                        for (i in 0 .. onResult.size - 1) {
                            onResult.get(i).connectBle()
                        }
                    }

                    Timber.d("Connected to the GATT server")
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    disconnectGattServer()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)

                // check if the discovery failed
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Timber.e("Device service discovery failed, status: $status")
                    return
                }

                // log for successful discovery
                Timber.d("Services discovery is successful")

                // find command characteristics from the GATT server
                val respCharacteristic = gatt?.let { findResponseCharacteristic(it) }
                // disconnect if the characteristic is not found
                if( respCharacteristic == null ) {
                    Timber.e("Unable to find cmd characteristic")
                    disconnectGattServer()
                    return
                }
                gatt.setCharacteristicNotification(respCharacteristic, true)

                // UUID for notification
                val descriptor:BluetoothGattDescriptor = respCharacteristic.descriptors[0]
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)

            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic
            ) {
                super.onCharacteristicChanged(gatt, characteristic)
                //Log.d(TAG, "characteristic changed: " + characteristic.uuid.toString())
                readCharacteristic(characteristic)
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                super.onCharacteristicWrite(gatt, characteristic, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Timber.d("Characteristic written successfully")
                } else {
                    Timber.e("Characteristic write unsuccessful, status: $status")
                    disconnectGattServer()
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                super.onCharacteristicRead(gatt, characteristic, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Timber.d("Characteristic read successfully")
                    readCharacteristic(characteristic)
                } else {
                    Timber.e("Characteristic read unsuccessful, status: $status")
                }
            }

            /**
             * Log the value of the characteristic
             * @param characteristic
             */
            private fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {

                val msg = characteristic.getStringValue(0)

                // max queue size check
                if (readDataQueue.size >= READ_DATA_QUEUE_MAX_SIEZ) {
                    // max size 초과시 하나 제거
                    readDataQueue.poll()
                }

                // validation check
                if (msg == null) return
                if (msg.length != 11) return
                if (msg.substring(4, 8).toIntOrNull() == null) return

                // add queue data
                readDataQueue.add(msg)

                var state : String  = STATE_SENSOR_NOT_RUN
                var distance : String = "0000"
                var data : String = ""

                // 에러 value 값 체크 ( 모두 에러라면 에러 )
                for (i in 0 .. readDataQueue.size - 1) {

                    data = readDataQueue.get(i)

                    // 센서가 하나라도 정상이라면, 비정상 센서 정보는 state에 담지 않는다.(정상 동작 유도)
                    if (!state.equals(STATE_SENSOR_RUN)) {
                        if (data.substring(2, 3).equals(STATE_SENSOR_RUN)) {
                            state = STATE_SENSOR_RUN
                        } else {
                            state = data.substring(2, 3)
                        }
                    }

                    // 최근 수신한 정상적인 센서 거리 값
                    if (data.substring(2, 3).equals(STATE_SENSOR_RUN)) {
                        distance = data.substring(4, 8)
                    }

                }

                // debug용
                if (BuildConfig.DEBUG) {
                    if (!state.equals(STATE_SENSOR_RUN)) {
                        Timber.d("+++ all buffer sensor error +++")
                    }
                }

                if (onResult != null) {
                    for (i in 0 .. onResult.size - 1) {

                        // 거리 전송
                        onResult.get(i).distance(distance.toInt())

                        if (!beforeSensorState.equals(state)) {
                            // 센서 에러 체크
                            if (state.equals(STATE_SENSOR_RUN)) {
                                onResult.get(i).sensorNormal()
                            } else {
                                onResult.get(i).sensorError()
                            }
                        }

                    }
                }

                beforeSensorState = state
                bleSensorState = state

            }


        }

        /**
         * Disconnect Gatt Server
         */
        fun disconnectGattServer() {
            Timber.d("Closing Gatt connection")
            // disconnect and close the gatt
            if (bluetoothGatt != null) {
                try {
                    bluetoothGatt!!.disconnect()
                    bluetoothGatt!!.close()
                    bluetoothGatt =  null
                    bleStatus = BluetoothProfile.STATE_DISCONNECTED

                    if (onResult != null) {
                        for (i in 0 .. onResult.size - 1) {
                            onResult.get(i).disconnect()
                        }
                    }

                    Timber.d("disconnect")
                }catch (e : Exception) {

                }
            }
        }

        // 연결 여부
        fun isConnected() : Boolean{
            if (this.bleStatus == BluetoothProfile.STATE_CONNECTED) {
                return true
            } else {
                return false
            }
        }

        // 센서 상태 정상 여부
        fun isSensorStateNormal() : Boolean{
            if (this.bleSensorState.equals(BLE_STATE_NOMAL)) {
                return true
            } else {
                return false
            }
        }
    }


}