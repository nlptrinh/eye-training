package kr.ac.unist.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import kr.ac.unist.R
import kr.ac.unist.databinding.DialogCommonDialogBinding
import timber.log.Timber


/**
* 시력 측정 중일때 사용하는 공통 다이얼로그
* @author 임성진
* @version 1.0.0
* @since 2021-05-26 오후 5:29
**/
class CommonDialog : DialogFragment() {

    interface OnClickListener {
        fun leftClick()
        fun rightClick()
        fun backClick()
    }

    // 바인딩
    lateinit var binding: DialogCommonDialogBinding

    // 제목
    var title : String = ""

    // 내용
    var content : String = ""

    // 왼쪽 버튼 이름
    var leftButtonTitle : String = ""

    // 오른쪽 버튼 이름
    var rightButtonTitle : String = ""

    // 클릭 리스너
    var onClickListener : OnClickListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                if (onClickListener != null) {
                    onClickListener!!.backClick()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.binding = DataBindingUtil.inflate<DialogCommonDialogBinding>(
            inflater, R.layout.dialog_common_dialog, container, false)

        this.binding.dialog = this

        // 제목
        if (!this.title.isNullOrEmpty()) {
            this.binding.tvTitle.text = this.title
        } else {
            this.binding.tvTitle.visibility = View.GONE
        }

        // 내용
        if (!this.content.isNullOrEmpty()) {
            this.binding.tvContent.text = this.content
        } else {
            this.binding.tvContent.visibility = View.GONE
        }

        // 왼쪽 버튼
        if (!this.leftButtonTitle.isNullOrEmpty()) {
            this.binding.tvLefTButton.text = this.leftButtonTitle
        } else {
            this.binding.tvLefTButton.visibility = View.GONE
        }

        // 오른쪽 버튼
        if (!this.rightButtonTitle.isNullOrEmpty()) {
            this.binding.tvRightButton.text = this.rightButtonTitle
        } else {
            this.binding.tvRightButton.visibility = View.GONE
        }

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

    }

    /**
    * 왼쪽 버튼 클릭
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-26 오후 5:53
    **/
    fun clickLeftButton(view :View) {

        if (this.onClickListener != null) {
            this.onClickListener!!.leftClick()
        }

    }

    /**
    * 오른쪽 버튼 클릭
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-26 오후 5:53
    **/
    fun clickRightButton(view :View) {

        if (this.onClickListener != null) {
            this.onClickListener!!.rightClick()
        }

    }



}