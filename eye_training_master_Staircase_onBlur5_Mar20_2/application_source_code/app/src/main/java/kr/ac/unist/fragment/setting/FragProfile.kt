package kr.ac.unist.fragment.setting

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
import kr.ac.unist.databinding.FragProfileBinding
import kr.ac.unist.manager.SharedPrefManager
import kr.ac.unist.util.Util

/**
* 개인정보 조회/수정 Screen
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragProfile : Fragment() {

    // 바인딩
    lateinit var binding: FragProfileBinding
    lateinit var user : List<User>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Util.settingFullScreen(requireActivity(), false)
        this.binding = DataBindingUtil.inflate<FragProfileBinding>(
                inflater, R.layout.frag_profile, container, false)
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

        this.binding.etId.keyListener = null

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

        // 기본 정보 셋팅
        var job = GlobalScope.launch {
            user = DbManager.getInstance().userDao().findUser(SharedPrefManager.getUserSeqId())
        }

        job.invokeOnCompletion {
            if (user.size == 0) {
            } else {
                var userInfo : User = user.get(0)

                requireActivity().runOnUiThread {

                    binding.etId.setText(userInfo.userId)
                    binding.etBrithday.setText(userInfo.birthDay)
                    binding.etEyesight.setText(userInfo.eyesight)

                    if(userInfo.sex.equals("M")) {
                        binding.rgSex.check(R.id.rb_man)
                    } else {
                        binding.rgSex.check(R.id.rb_woman)
                    }

                    var eyeSurgerys = userInfo.surgery.split(",")

                    eyeSurgerys.forEach {
                        when (it) {
                            binding.cbLasik.tag.toString() -> binding.cbLasik.isChecked = true
                            binding.cbLasek.tag.toString() -> binding.cbLasek.isChecked = true
                            binding.cbEtc.tag.toString() -> binding.cbEtc.isChecked = true
                            binding.cbNone.tag.toString() -> binding.cbNone.isChecked = true
                        }
                    }

                    var lens = userInfo.lens.split(",")

                    lens.forEach {
                        when (it) {
                            binding.cbCheck1.text.toString() -> binding.cbCheck1.isChecked = true
                            binding.cbCheck2.text.toString() -> binding.cbCheck2.isChecked = true
                            binding.cbCheck3.text.toString() -> binding.cbCheck3.isChecked = true
                            binding.cbCheck4.text.toString() -> binding.cbCheck4.isChecked = true
                            binding.cbCheck5.text.toString() -> binding.cbCheck5.isChecked = true
                        }
                    }

                    if (userInfo.disorder.equals("T")) {
                        binding.rgDisorder.check(R.id.rb_yes)
                    } else {
                        binding.rgDisorder.check(R.id.rb_no)
                    }

                }

            }
        }
    }

    /**
    * save click event
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-18 오후 5:15
    **/
    fun clickSave(view: View){

        Util.hideKeyboard(view)

        if (this.binding.etId.text.isNullOrEmpty()) {
            Util.showSimpleSnackbar(this.binding.root, requireContext(), getString(R.string.msg_sign_insert_id), Snackbar.LENGTH_SHORT)
        } else if (this.binding.etPassword.text.isNullOrEmpty()) {
            Util.showSimpleSnackbar(this.binding.root, requireContext(), getString(R.string.msg_sign_insert_password), Snackbar.LENGTH_SHORT)
        } else if (this.binding.etPasswordRe.text.isNullOrEmpty()) {
            Util.showSimpleSnackbar(this.binding.root, requireContext(), getString(R.string.msg_sign_insert_password_re), Snackbar.LENGTH_SHORT)
        } else if (!this.binding.etPasswordRe.text.toString().equals(this.binding.etPassword.text.toString())) {
            Util.showSimpleSnackbar(this.binding.root, requireContext(), getString(R.string.msg_sign_insert_password_fail), Snackbar.LENGTH_SHORT)
        } else if (this.binding.etBrithday.text.isNullOrEmpty()) {
            Util.showSimpleSnackbar(this.binding.root, requireContext(), getString(R.string.msg_sign_insert_birthDay), Snackbar.LENGTH_SHORT)
        } else {

            var brithDay : String = this.binding.etBrithday.text.toString()
            var sex : String = ""
            var eyeSurgery : String = ""
            var eyesight : String = "0"
            var lens : String = ""
            var disorder : String = ""

            var eyeSurgeryList : ArrayList<String> = ArrayList<String>()
            if (this.binding.cbLasik.isChecked) eyeSurgeryList.add(this.binding.cbLasik.tag.toString())
            if (this.binding.cbLasek.isChecked) eyeSurgeryList.add(this.binding.cbLasek.tag.toString())
            if (this.binding.cbEtc.isChecked) eyeSurgeryList.add(this.binding.cbEtc.tag.toString())
            if (this.binding.cbNone.isChecked) eyeSurgeryList.add(this.binding.cbNone.tag.toString())

            eyeSurgery = eyeSurgeryList.joinToString(",")

            if (this.binding.rgSex.checkedRadioButtonId == this.binding.rbMan.id) {
                sex = "M"
            } else {
                sex = "F"
            }

            if (!this.binding.etEyesight.text.isNullOrEmpty()) eyesight = this.binding.etEyesight.text.toString()

            var lenList : ArrayList<String> = ArrayList<String>()
            if (this.binding.cbCheck1.isChecked) lenList.add(this.binding.cbCheck1.text.toString())
            if (this.binding.cbCheck2.isChecked) lenList.add(this.binding.cbCheck2.text.toString())
            if (this.binding.cbCheck3.isChecked) lenList.add(this.binding.cbCheck3.text.toString())
            if (this.binding.cbCheck4.isChecked) lenList.add(this.binding.cbCheck4.text.toString())
            if (this.binding.cbCheck5.isChecked) lenList.add(this.binding.cbCheck5.text.toString())
            lens = lenList.joinToString(",")

            if (this.binding.rgDisorder.id == this.binding.rbYes.id) {
                disorder = "T"
            } else {
                disorder = "F"
            }

            var userInfo : User = user.get(0)

            userInfo.password = this.binding.etPasswordRe.text.toString()
            userInfo.birthDay = brithDay
            userInfo.sex = sex
            userInfo.eyesight = eyesight
            userInfo.surgery = eyeSurgery
            userInfo.lens = lens
            userInfo.disorder = disorder

            GlobalScope.launch {
                // 정보 업데이트
                DbManager.getInstance().userDao().updateUserInfo(userInfo)

                Util.showSimpleSnackbar(binding.root, requireContext(), getString(R.string.msg_save_profile), Snackbar.LENGTH_SHORT)
            }

        }
    }
    /**
     * close click event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-18 오후 5:15
     **/
    fun clickClose(view: View){
        this.findNavController().navigateUp()
    }
}