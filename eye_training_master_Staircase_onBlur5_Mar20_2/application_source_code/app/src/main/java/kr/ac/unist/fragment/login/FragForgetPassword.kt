package kr.ac.unist.fragment.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import kr.ac.unist.R
import kr.ac.unist.databinding.FragForgetPasswordBinding
import kr.ac.unist.util.Util

/**
* Forget Password
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragForgetPassword : Fragment() {

    // 바인딩
    lateinit var binding: FragForgetPasswordBinding
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Util.settingFullScreen(requireActivity(), false)
        this.binding = DataBindingUtil.inflate<FragForgetPasswordBinding>(
                inflater, R.layout.frag_forget_password, container, false)
        return this.binding.root
    }

}