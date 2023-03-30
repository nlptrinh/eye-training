package kr.ac.unist.dialog

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.ac.unist.R
import kr.ac.unist.database.DbManager
import kr.ac.unist.databinding.DialogSettingBleBinding
import kr.ac.unist.databinding.DialogSettingDbBackupBinding
import kr.ac.unist.manager.BleManager
import kr.ac.unist.util.Util
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * 로컬 DB 백업 Dialog
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-18 오후 5:15
 **/
class SettingDbBackup : DialogFragment() {

    // 바인딩
    lateinit var binding: DialogSettingDbBackupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        this.binding = DataBindingUtil.inflate<DialogSettingDbBackupBinding>(
            inflater, R.layout.dialog_setting_db_backup, container, false)

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

        // 백업 시작
        checkVersion()

    }

    /**
    * 디바이스 버전 체크
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-14 오후 1:09
    **/
    fun checkVersion(){

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            checkPermission()
        } else {
            startBackup()
        }

    }

    /**
    * 백업 시작
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-14 오후 2:12
    **/
    fun startBackup(){
        GlobalScope.launch(Dispatchers.IO) {

            exportDatabase()

            // 너무 빨리 끝나면 문구가 안보여서, 조금 여유를 둔다.
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                dismiss()
            }, 2000)

        }
    }

    /**
     * 권한 체크
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-20 오후 2:39
     **/
    fun checkPermission() {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                10101
            )
        } else {
            startBackup()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 10101) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBackup()
            } else {
                Util.showSimpleSnackbar(binding.root, requireContext(), getString(R.string.msg_fail_backup_caus_permission), Snackbar.LENGTH_INDEFINITE)
                // 너무 빨리 끝나면 문구가 안보여서, 조금 여유를 둔다.
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    dismiss()
                }, 2000)
            }
        }
    }

    // 데이터 베이스 내보내기
    fun exportDatabase(){

        try {

            val currentDBPath =  DbManager.getInstance().openHelper.writableDatabase.path
            val currentDB = File(currentDBPath)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                var contentResolver: ContentResolver = requireActivity().contentResolver;

                var values: ContentValues = ContentValues()

                // LOCK
                values.put(MediaStore.Downloads.IS_PENDING, 1)

                var saveFile: ParcelFileDescriptor

                values.put(MediaStore.Downloads.DISPLAY_NAME, getFileName() + "_" + requireContext().getString(R.string.app_name) + ".sqlite")
                var uri : Uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!
                saveFile = contentResolver.openFileDescriptor(uri!!, "w", null)!!

                try {
                    val src = FileInputStream(currentDB).channel
                    val dst = FileOutputStream(saveFile.fileDescriptor).channel
                    dst.transferFrom(src, 0, src.size())
                    src.close()
                    dst.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                saveFile.close()

                // UnLock
                values.put(MediaStore.Downloads.IS_PENDING, 0);

                contentResolver.update(uri!!, values, null, null)

            } else {

               var saveFile: File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                        + "/", getFileName()  + "_" + requireContext().getString(R.string.app_name) + ".sqlite")

                // new file
                Util.newFile(saveFile)

                try {
                    val src = FileInputStream(currentDB).channel
                    val dst = FileOutputStream(saveFile).channel
                    dst.transferFrom(src, 0, src.size())
                    src.close()
                    dst.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

        } catch (e: IOException) {
            e.printStackTrace()
        }


    }

    /**
    * 저장할 파일 이름 반환
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-14 오후 12:48
    **/
    fun getFileName() : String {

        var simpleFormatter: SimpleDateFormat = SimpleDateFormat("yyyyMMddHHmmss");
        var date: Date = Calendar.getInstance().getTime()
        var strDate : String = simpleFormatter.format(date).toString()

        return strDate

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