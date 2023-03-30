package kr.ac.unist.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import kr.ac.unist.R
import kr.ac.unist.databinding.DialogSettingBreakTimeBinding
import kr.ac.unist.manager.SharedPrefManager

/**
 * 블루 사이 휴식 시간 설정 Dialog
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-18 오후 5:15
 **/
class SettingBreakTime : DialogFragment() {

    // 바인딩
    lateinit var binding: DialogSettingBreakTimeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        this.binding = DataBindingUtil.inflate<DialogSettingBreakTimeBinding>(
            inflater, R.layout.dialog_setting_break_time, container, false)

        this.binding.dialog = this

        setCancelable(false)



        return this.binding.root

    }

    override fun onStart() {
        super.onStart()
        val width = requireContext().resources.getDimension(R.dimen.dialog_size_width).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // item
        val items = listOf("10초", "20초", "30초", "40초", "50초", "60초",
            "70초", "80초", "90초", "100초", "110초", "120초",
            "130초", "140초", "150초", "160초", "170초", "180초")

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        var spinner = this.binding.actvList

        spinner.setAdapter(adapter)

        var breakTime : String = SharedPrefManager.getBreakTime()

        var findIndex = items.indexOf(breakTime)

        spinner.setText(items[findIndex], false)

        spinner.requestFocus()

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
        SharedPrefManager.setBreakTime(this.binding.actvList.text.toString())
        dismiss()
    }
}