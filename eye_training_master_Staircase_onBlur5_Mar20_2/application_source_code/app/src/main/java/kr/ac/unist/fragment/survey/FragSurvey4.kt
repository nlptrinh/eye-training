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
import kr.ac.unist.databinding.FragSurvey2Binding
import kr.ac.unist.databinding.FragSurvey3Binding
import kr.ac.unist.databinding.FragSurvey4Binding
import kr.ac.unist.fragment.signup.FragSignUp2Args
import kr.ac.unist.util.Util
import timber.log.Timber

/**
* SignUp 4 Screen
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragSurvey4 : Fragment() {

    // 바인딩
    lateinit var binding: FragSurvey4Binding

    // 선택한 번호
    var resultIndex: Int = -1

    // 정답 선택 Step
    var resultStep : Int = 0
    
    // 선택한 정답
    var resultList : ArrayList<String> = ArrayList<String>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.binding = DataBindingUtil.inflate<FragSurvey4Binding>(
                inflater, R.layout.frag_survey_4, container, false)
        this.binding.frag = this

        // init
        init()
        changeQuiz()

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
            resultList.add(resultIndex.toString())
            resultIndex  = -1
            changeQuiz()
        }
    }

    fun changeQuiz(){

        resultStep+=1
        clickQuiz(-1)

        when(this.resultStep) {

            1 -> {
                this.binding.tvMsg.text = getString(R.string.quiz_3_1)
                this.binding.tvQuiz1.text = "100"
                this.binding.tvQuiz2.text = "93"
                this.binding.tvQuiz3.text = "86"
                this.binding.tvQuiz4.text = "73"
            }
            2 -> {
                this.binding.tvMsg.text = getString(R.string.quiz_3_2)
                this.binding.tvQuiz1.text = "82"
                this.binding.tvQuiz2.text = "86"
                this.binding.tvQuiz3.text = "77"
                this.binding.tvQuiz4.text = "78"
            }
            3 -> {
                this.binding.tvMsg.text = getString(R.string.quiz_3_3)
                this.binding.tvQuiz1.text = "79"
                this.binding.tvQuiz2.text = "77"
                this.binding.tvQuiz3.text = "70"
                this.binding.tvQuiz4.text = "63"
            }
            4 -> {
                this.binding.tvMsg.text = getString(R.string.quiz_3_4)
                this.binding.tvQuiz1.text = "74"
                this.binding.tvQuiz2.text = "72"
                this.binding.tvQuiz3.text = "62"
                this.binding.tvQuiz4.text = "68"
            }
            5 -> {
                this.binding.tvMsg.text = getString(R.string.quiz_3_5)
                this.binding.tvQuiz1.text = "62"
                this.binding.tvQuiz2.text = "68"
                this.binding.tvQuiz3.text = "65"
                this.binding.tvQuiz4.text = "70"
            }
            else -> {
                var args = FragSurvey4Args.fromBundle(requireArguments())
                this.findNavController()
                    .navigate(FragSurvey4Directions.actionFragSurvey4ToFragSurvey5(args.answer3 + "," + this.resultList.joinToString(separator = ",")))
            }
        }

    }

    fun clickQuiz(index : Int) {

        Timber.d("%d", index)
        
        this.resultIndex = index

        if (index == -1) {
            this.binding.ivQuiz1.visibility = View.GONE
            (this.binding.ivQuiz1.parent as ViewGroup).setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            this.binding.ivQuiz2.visibility = View.GONE
            (this.binding.ivQuiz2.parent as ViewGroup).setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            this.binding.ivQuiz3.visibility = View.GONE
            (this.binding.ivQuiz3.parent as ViewGroup).setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            this.binding.ivQuiz4.visibility = View.GONE
            (this.binding.ivQuiz4.parent as ViewGroup).setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        } else {
            if (index == 1) {
                this.binding.ivQuiz1.visibility = View.VISIBLE
                (this.binding.ivQuiz1.parent as ViewGroup).setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.rgb_156200ee
                    )
                )
            } else {
                this.binding.ivQuiz1.visibility = View.GONE
                (this.binding.ivQuiz1.parent as ViewGroup).setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }

            if (index == 2) {
                this.binding.ivQuiz2.visibility = View.VISIBLE
                (this.binding.ivQuiz2.parent as ViewGroup).setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.rgb_156200ee
                    )
                )
            } else {
                this.binding.ivQuiz2.visibility = View.GONE
                (this.binding.ivQuiz2.parent as ViewGroup).setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }

            if (index == 3) {
                this.binding.ivQuiz3.visibility = View.VISIBLE
                (this.binding.ivQuiz3.parent as ViewGroup).setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.rgb_156200ee
                    )
                )
            } else {
                this.binding.ivQuiz3.visibility = View.GONE
                (this.binding.ivQuiz3.parent as ViewGroup).setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }

            if (index == 4) {
                this.binding.ivQuiz4.visibility = View.VISIBLE
                (this.binding.ivQuiz4.parent as ViewGroup).setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.rgb_156200ee
                    )
                )
            } else {
                this.binding.ivQuiz4.visibility = View.GONE
                (this.binding.ivQuiz4.parent as ViewGroup).setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
        }
    }

}