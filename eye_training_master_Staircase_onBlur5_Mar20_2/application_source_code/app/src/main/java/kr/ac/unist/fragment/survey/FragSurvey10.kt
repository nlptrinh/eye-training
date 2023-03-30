package kr.ac.unist.fragment.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kr.ac.unist.R
import kr.ac.unist.databinding.*
import kr.ac.unist.util.Util
import timber.log.Timber

/**
* SignUp 10 Screen
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragSurvey10 : Fragment() {

    // 바인딩
    lateinit var binding: FragSurvey10Binding

    // 선택한 번호
    var resultIndex: Int = -1;
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.binding = DataBindingUtil.inflate<FragSurvey10Binding>(
                inflater, R.layout.frag_survey_10, container, false)
        this.binding.frag = this

        // init
        init()

        return this.binding.root
    }

    /**
     * init
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-18 오후 5:15
     **/
    fun init(){

        this.binding.ivQuiz1.visibility = View.GONE
        this.binding.ivQuiz2.visibility = View.GONE
        this.binding.ivQuiz3.visibility = View.GONE
        this.binding.ivQuiz4.visibility = View.GONE

    }


    /**
    * next click event
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-18 오후 5:15
    **/
    fun clickNext(view: View){
        if (this.resultIndex == -1) {
            Util.showSimpleSnackbar(binding.root, requireContext(), getString(R.string.msg_survey_select_answer), Snackbar.LENGTH_SHORT)
        } else {
            var args = FragSurvey10Args.fromBundle(requireArguments())
            this.findNavController()
                .navigate(FragSurvey10Directions.actionFragSurvey10ToFragSurvey11(args.answer9 + "," + this.resultIndex.toString()))
        }
    }

    fun clickQuiz(index : Int) {

        Timber.d("%d", index)
        
        this.resultIndex = index

        if (index == 1) {
            this.binding.ivQuiz1.visibility = View.VISIBLE
            (this.binding.ivQuiz1.parent as ViewGroup).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rgb_156200ee))
        } else {
            this.binding.ivQuiz1.visibility = View.GONE
            (this.binding.ivQuiz1.parent as ViewGroup).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        if (index == 2) {
            this.binding.ivQuiz2.visibility = View.VISIBLE
            (this.binding.ivQuiz2.parent as ViewGroup).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rgb_156200ee))
        } else {
            this.binding.ivQuiz2.visibility = View.GONE
            (this.binding.ivQuiz2.parent as ViewGroup).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        if (index == 3) {
            this.binding.ivQuiz3.visibility = View.VISIBLE
            (this.binding.ivQuiz3.parent as ViewGroup).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rgb_156200ee))
        } else {
            this.binding.ivQuiz3.visibility = View.GONE
            (this.binding.ivQuiz3.parent as ViewGroup).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        if (index == 4) {
            this.binding.ivQuiz4.visibility = View.VISIBLE
            (this.binding.ivQuiz4.parent as ViewGroup).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rgb_156200ee))
        } else {
            this.binding.ivQuiz4.visibility = View.GONE
            (this.binding.ivQuiz4.parent as ViewGroup).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

    }

}