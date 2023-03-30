package kr.ac.unist.fragment.signup

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.ac.unist.R
import kr.ac.unist.custom.`interface`.DrawableClickListener
import kr.ac.unist.database.DbManager
import kr.ac.unist.database.entity.User
import kr.ac.unist.databinding.FragForgetPasswordBinding
import kr.ac.unist.databinding.FragSignUp1Binding
import kr.ac.unist.util.Util

/**
* SignUp 1 Screen
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragSignUp1 : Fragment() {

    // 바인딩
    lateinit var binding: FragSignUp1Binding
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.binding = DataBindingUtil.inflate<FragSignUp1Binding>(
                inflater, R.layout.frag_sign_up_1, container, false)
        this.binding.frag = this
        init()
        return this.binding.root
    }

    /**
     * 초기화
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 2:39
     **/
    fun init(){

        // 패스워드 전환 변경 리스너 등록
        this.binding.etPassword.setDrawableClickListener(object : DrawableClickListener {
            override fun onClick(target: DrawableClickListener.DrawablePosition?) {
                when (target) {
                    DrawableClickListener.DrawablePosition.RIGHT -> {
                        if (binding.etPassword.transformationMethod == PasswordTransformationMethod.getInstance()) {
                            binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        } else {
                            binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                        }
                    }
                    else -> {
                    }
                }
            }
        })

        this.binding.etPasswordRe.setDrawableClickListener(object : DrawableClickListener {
            override fun onClick(target: DrawableClickListener.DrawablePosition?) {
                when (target) {
                    DrawableClickListener.DrawablePosition.RIGHT -> {
                        if (binding.etPasswordRe.transformationMethod == PasswordTransformationMethod.getInstance()) {
                            binding.etPasswordRe.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        } else {
                            binding.etPasswordRe.transformationMethod = PasswordTransformationMethod.getInstance()
                        }
                    }
                    else -> {
                    }
                }
            }
        })
    }

    /**
    * next click event
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-18 오후 5:15
    **/
    fun clickNext(view: View){

        Util.hideKeyboard(view)

        if (this.binding.etId.text.isNullOrEmpty()) {
            Util.showSimpleSnackbar(this.binding.root, requireContext(), getString(R.string.msg_sign_insert_id), Snackbar.LENGTH_SHORT)
        } else if (this.binding.etPassword.text.isNullOrEmpty()) {
            Util.showSimpleSnackbar(this.binding.root, requireContext(), getString(R.string.msg_sign_insert_password), Snackbar.LENGTH_SHORT)
        } else if (this.binding.etPasswordRe.text.isNullOrEmpty()) {
            Util.showSimpleSnackbar(this.binding.root, requireContext(), getString(R.string.msg_sign_insert_password_re), Snackbar.LENGTH_SHORT)
        } else if (!this.binding.etPasswordRe.text.toString().equals(this.binding.etPassword.text.toString())) {
            Util.showSimpleSnackbar(this.binding.root, requireContext(), getString(R.string.msg_sign_insert_password_fail), Snackbar.LENGTH_SHORT)
        } else {

            GlobalScope.launch {

                var list : List<User> = DbManager.getInstance().userDao().findLoginId(binding.etId.text.toString())

                if (list.size > 0) {
                    Util.showSimpleSnackbar(binding.root, requireContext(), getString(R.string.msg_sign_insert_found_id), Snackbar.LENGTH_SHORT)
                } else {
                    var action = FragSignUp1Directions.actionFragSignUp1ToFragSignUp2(binding.etId.text.toString(), binding.etPassword.text.toString())
                    findNavController().navigate(action)
                }

            }


        }

    }

}