package kr.ac.unist.fragment.login

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kr.ac.unist.databinding.FragLoginBinding
import kr.ac.unist.manager.SharedPrefManager
import kr.ac.unist.util.Util


/**
* Login Screen
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragLogin : Fragment() {

    // 바인딩
    lateinit var binding: FragLoginBinding
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Util.settingFullScreen(requireActivity(), false)
        this.binding = DataBindingUtil.inflate<FragLoginBinding>(
                inflater, R.layout.frag_login, container, false)
        this.binding.frag = this

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
        return this.binding.root
    }


    /**
     * Forget Password click
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 2:39
     **/
    fun clickForgetPassword(view: View) {
        this.findNavController().navigate(FragLoginDirections.actionFragLoginToFragForgetPasswrod())
    }

    /**
     * Login click
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 2:39
     **/
    fun clickLogin(view: View) {

        Util.hideKeyboard(view)

        var id : String = this.binding.etId.text.toString()
        var password : String = this.binding.etPassword.text.toString()
        
        if (id.isNullOrEmpty()) {
            Util.showSimpleSnackbar(this.binding.root, requireContext(), getString(R.string.msg_login_insert_id), Snackbar.LENGTH_SHORT)
        } else if (password.isNullOrEmpty()) {
            Util.showSimpleSnackbar(this.binding.root, requireContext(), getString(R.string.msg_login_insert_password), Snackbar.LENGTH_SHORT)
        } else {
            GlobalScope.launch {
                var user : List<User> = DbManager.getInstance().userDao().findLoginUser(
                    id,
                    password
                )

                if (user.size > 0) {
                    SharedPrefManager.setUserSeqId(user.get(0).id.toString())
                    findNavController().navigate(FragLoginDirections.actionFragLoginToFragMain())
                } else {
                    Util.showSimpleSnackbar(binding.root, requireContext(), getString(R.string.msg_login_insert_not_found_user), Snackbar.LENGTH_SHORT)
                }
            }
        }

    }

}