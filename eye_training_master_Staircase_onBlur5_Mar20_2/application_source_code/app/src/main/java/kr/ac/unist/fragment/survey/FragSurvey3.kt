package kr.ac.unist.fragment.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kr.ac.unist.R
import kr.ac.unist.databinding.FragSurvey3Binding
import kr.ac.unist.util.Util

/**
* SignUp 3 Screen
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragSurvey3 : Fragment() {

    // 바인딩
    lateinit var binding: FragSurvey3Binding

    // 선택한 번호
    var resultIndex: Int = -1;
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.binding = DataBindingUtil.inflate<FragSurvey3Binding>(
                inflater, R.layout.frag_survey_3, container, false)
        this.binding.frag = this

        return this.binding.root
    }

    override fun onStart() {
        super.onStart()

        // year item
        var items = range("2020", "2999", "")!!.toList()
        var adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        var spinner = this.binding.actvYear
        spinner.requestFocus()

        spinner.setAdapter(adapter)

        // mnonth item
        items = range("1", "12", "")!!.toList()
        adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        spinner = this.binding.actvMonth

        spinner.setAdapter(adapter)

        // day item
        items = range("1", "31", "")!!.toList()
        adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        spinner = this.binding.actvDay

        spinner.setAdapter(adapter)

        // Season item
        items = listOf("봄", "여름", "가을", "겨울")
        adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        spinner = this.binding.actvSeason

        spinner.setAdapter(adapter)

        // week item
        items = listOf("월", "화", "수", "목", "금", "토", "일")
        adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        spinner = this.binding.actvWeek

        spinner.setAdapter(adapter)

    }

    fun range(start: String, end: String, tag: String): List<String>? {
        var cur = start.toInt()
        val stop = end.toInt()
        val list: MutableList<String> = ArrayList()
        while (cur <= stop) {
            list.add((cur++).toString() + tag)
        }
        return list
    }


    /**
    * next click event
    * @author 임성진
    * @version 1.0.0
    * @since 2021-05-18 오후 5:15
    **/
    fun clickNext(view: View){

        var year : String = this.binding.actvYear.text.toString()
        var month : String = this.binding.actvMonth.text.toString()
        var day : String = this.binding.actvDay.text.toString()
        var season : String = this.binding.actvSeason.text.toString()
        var week : String = this.binding.actvWeek.text.toString()

        if (year.isNullOrEmpty() || month.isNullOrEmpty() || day.isNullOrEmpty() || season.isNullOrEmpty() || week.isNullOrEmpty()){
            Util.showSimpleSnackbar(binding.root, requireContext(), getString(R.string.msg_survey_select_answer), Snackbar.LENGTH_SHORT)
        } else if (year.equals("년도") || month.equals("월") || day.equals("일") || season.equals("계절") || week.equals("요일")){
            Util.showSimpleSnackbar(binding.root, requireContext(), getString(R.string.msg_survey_select_answer), Snackbar.LENGTH_SHORT)
        } else {
            var args = FragSurvey3Args.fromBundle(requireArguments())

            this.findNavController()
                .navigate(FragSurvey3Directions.actionFragSurvey3ToFragSurvey4(args.answer2 + "," + year + "," + month + "," + day +"," + season +"," + week))
        }
    }

}