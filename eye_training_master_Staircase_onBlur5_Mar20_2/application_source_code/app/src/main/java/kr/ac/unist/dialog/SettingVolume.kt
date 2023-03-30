package kr.ac.unist.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import kr.ac.unist.R
import kr.ac.unist.databinding.DialogSettingBreakTimeBinding
import kr.ac.unist.databinding.DialogSettingVolumnBinding
import kr.ac.unist.manager.SharedPrefManager
import kr.ac.unist.util.Util

/**
 * 알림음 크기 조절 설정 Dialog
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-18 오후 5:15
 **/
class SettingVolume : DialogFragment(), SeekBar.OnSeekBarChangeListener {

    // 바인딩
    lateinit var binding: DialogSettingVolumnBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        this.binding = DataBindingUtil.inflate<DialogSettingVolumnBinding>(
            inflater, R.layout.dialog_setting_volumn, container, false)

        this.binding.dialog = this

        setCancelable(false)

        // 저장된 볼륨 설정
        this.binding.sbVolume.progress = SharedPrefManager.getBeepVolume()
        
        // 볼륨 조절 리스너 등록
        this.binding.sbVolume.setOnSeekBarChangeListener(this)

        return this.binding.root

    }

    override fun onStart() {
        super.onStart()
        val width = requireContext().resources.getDimension(R.dimen.dialog_size_width).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
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
     * 설정하기 event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-18 오후 5:15
     **/
    fun clickSave(view: View) {
        // 볼륨 저장
        SharedPrefManager.setBeepVolume(this.binding!!.sbVolume.progress)
        dismiss()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    /**
     * 볼륨 조절시, 볼륨 저장
     * @author 임성진
     * @version 1.0.0
     * @since 2021-06-16 오후 5:47
     **/
    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (seekBar != null) {
            Util.playBeepWithVolumn(requireContext(), R.raw.beep_single, seekBar!!.progress)
        }
    }
}