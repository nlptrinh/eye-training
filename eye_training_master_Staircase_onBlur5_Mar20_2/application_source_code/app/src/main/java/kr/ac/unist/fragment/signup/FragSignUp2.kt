package kr.ac.unist.fragment.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kr.ac.unist.R
import kr.ac.unist.databinding.FragForgetPasswordBinding
import kr.ac.unist.databinding.FragSignUp1Binding
import kr.ac.unist.databinding.FragSignUp2Binding
import kr.ac.unist.util.Util

/**
* SignUp 1 Screen
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragSignUp2 : Fragment() {

    // 바인딩
    lateinit var binding: FragSignUp2Binding
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.binding = DataBindingUtil.inflate<FragSignUp2Binding>(
                inflater, R.layout.frag_sign_up_2, container, false)
        this.binding.frag = this
        return this.binding.root
    }

    /**
     * next click event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-18 오후 5:15
     **/
    fun clickNext(view: View){

        Util.hideKeyboard(view)

        if (this.binding.etBrithday.text.isNullOrEmpty()) {
            Util.showSimpleSnackbar(this.binding.root, requireContext(), getString(R.string.msg_sign_insert_birthDay), Snackbar.LENGTH_SHORT)
        } else {

            var args = FragSignUp2Args.fromBundle(requireArguments())
            var brithDay : String = this.binding.etBrithday.text.toString()
            var sex : String = ""
            var eyeSurgery : String = ""

            var eyeSurgeryList : ArrayList<String> = ArrayList<String>()
            if (this.binding.cbLasik.isChecked) eyeSurgeryList.add(this.binding.cbLasik.tag.toString())
            if (this.binding.cbLasek.isChecked) eyeSurgeryList.add(this.binding.cbLasek.tag.toString())
            if (this.binding.cbEtc.isChecked) eyeSurgeryList.add(this.binding.cbEtc.tag.toString())
            if (this.binding.cbNone.isChecked) eyeSurgeryList.add(this.binding.cbNone.tag.toString())

            if (eyeSurgeryList.size == 0) {
                Util.showSimpleSnackbar(binding.root, requireContext(), getString(R.string.msg_sign_insert_eye_surgery), Snackbar.LENGTH_SHORT)
                return
            }

            eyeSurgery = eyeSurgeryList.joinToString(",")

            if (this.binding.rgSex.checkedRadioButtonId == this.binding.rbMan.id) {
                sex = "M"
            } else {
                sex = "F"
            }

            var action = FragSignUp2Directions.actionFragSignUp2ToFragSignUp3(args.id, args.password, brithDay, sex, eyeSurgery.replaceFirst(", ",""))
            this.findNavController().navigate(action)
        }

    }

}