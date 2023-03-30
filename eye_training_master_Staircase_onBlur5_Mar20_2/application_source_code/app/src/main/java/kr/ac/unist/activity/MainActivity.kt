package kr.ac.unist.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.ac.unist.R
import kr.ac.unist.database.DbManager
import kr.ac.unist.databinding.ActivityMainBinding
import kr.ac.unist.manager.BleManager
import kr.ac.unist.util.Util
import java.io.IOException

/**
* 메인 화면
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 12:40
**/
class MainActivity : AppCompatActivity() {

    var textView: TextView? = null
    var context: Context? = null
    var F = 1f //focal length
    var sensorX = 0f
    var sensorY //camera sensor dimensions
            = 0f
    var angleX = 0f
    var angleY = 0f

    // 메인 바인딩
    lateinit var binding: ActivityMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        // util init
        Util.init(window.decorView.systemUiVisibility)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState)



        this.binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        // test
        /*GlobalScope.launch {
            DbManager.getInstance().processDao().deleteTrainTrial()
            DbManager.getInstance().processDao().deleteTestTrial()
        }*/
//        setContentView(R.layout.activity_main)
        context = applicationContext
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
            Toast.makeText(this, "Grant Permission and restart app", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Camera Access Granted", Toast.LENGTH_SHORT).show()
            val camera = frontCam()
            val campar = camera!!.parameters
            F = campar.focalLength
            angleX = campar.horizontalViewAngle
            angleY = campar.verticalViewAngle
            sensorX = (Math.tan(Math.toRadians((angleX / 2).toDouble())) * 2 * F).toFloat()
            sensorY = (Math.tan(Math.toRadians((angleY / 2).toDouble())) * 2 * F).toFloat()
            camera.stopPreview()
            camera.release()
//            textView = findViewById(R.id.text)
            createCameraSource()
        }
    }
    private fun frontCam(): Camera? {
        var cameraCount = 0
        var cam: Camera? = null
        val cameraInfo = Camera.CameraInfo()
        cameraCount = Camera.getNumberOfCameras()
        for (camIdx in 0 until cameraCount) {
            Camera.getCameraInfo(camIdx, cameraInfo)
            Log.v("CAMID", camIdx.toString() + "")
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx)
                } catch (e: RuntimeException) {
                    Log.e("FAIL", "Camera failed to open: " + e.localizedMessage)
                }
            }
        }
        return cam
    }

    fun createCameraSource() {
        val detector = FaceDetector.Builder(this)
            .setTrackingEnabled(true)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .setMode(FaceDetector.ACCURATE_MODE) // FAST_MODE
            .build()
        detector.setProcessor(LargestFaceFocusingProcessor(detector, FaceTracker()))
        val cameraSource = CameraSource.Builder(this, detector)
            .setFacing(CameraSource.CAMERA_FACING_FRONT)
            .setRequestedFps(30.0f)
            .build()
        println(cameraSource.previewSize)
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            cameraSource.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun showStatus(message: String?) {
//        runOnUiThread { textView!!.text = message }
        runOnUiThread{ Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }

    public inner class FaceTracker public constructor() :
        Tracker<Face>() {
        override fun onUpdate(detections: Detector.Detections<Face>, face: Face) {
            val leftEyePos = face.landmarks[LEFT_EYE].position
            val rightEyePos = face.landmarks[RIGHT_EYE].position
            val deltaX = Math.abs(leftEyePos.x - rightEyePos.x)
            val deltaY = Math.abs(leftEyePos.y - rightEyePos.y)
            distance = if (deltaX >= deltaY) {
                F * (AVERAGE_EYE_DISTANCE / sensorX) * (IMAGE_WIDTH / deltaX)
            } else {
                F * (AVERAGE_EYE_DISTANCE / sensorY) * (IMAGE_HEIGHT / deltaY)
            }
//            showStatus("distance: " + String.format("%.0f", distance) + "mm")
//            showStatus("Face detected, distance: " + String.format("%.0f", distance) + "mm")
//            if (faceDet < 5){
//                showStatus("Face detected, distance: " + String.format("%.0f", distance) + "mm")
//                isFaceDetected = true
//                faceDet++
//            }
        }

        override fun onMissing(detections: Detector.Detections<Face>) {
            super.onMissing(detections)
//            showStatus("face not detected")
            isFaceDetected = false
            faceDet = 0
        }

        override fun onDone() {
            super.onDone()
        }
    }

    companion object {
        const val IMAGE_WIDTH = 1024
        const val IMAGE_HEIGHT = 1024
        const val RIGHT_EYE = 0
        const val LEFT_EYE = 1
        const val AVERAGE_EYE_DISTANCE = 63 // in mm
        var distance = 0f
        var faceDet = 0
        var isFaceDetected = false
    }


    override fun onDestroy() {
        super.onDestroy()
        // ble 중지
        BleManager.disconnectGattServer()
    }

}