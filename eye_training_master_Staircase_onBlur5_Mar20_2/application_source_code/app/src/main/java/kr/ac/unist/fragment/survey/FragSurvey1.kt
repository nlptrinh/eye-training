package kr.ac.unist.fragment.survey

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kr.ac.unist.R
import kr.ac.unist.databinding.FragSurvey1Binding

/**
* SignUp 1 Screen
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragSurvey1 : Fragment() {

    // 바인딩
    lateinit var binding: FragSurvey1Binding
    private lateinit var callback: OnBackPressedCallback

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.binding = DataBindingUtil.inflate<FragSurvey1Binding>(
                inflater, R.layout.frag_survey_1, container, false)
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
        this.findNavController().navigate(FragSurvey1Directions.actionFragSurvey1ToFragSurvey2())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(FragSurvey1Directions.actionFragSurvey1ToFragIntro())
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }
}