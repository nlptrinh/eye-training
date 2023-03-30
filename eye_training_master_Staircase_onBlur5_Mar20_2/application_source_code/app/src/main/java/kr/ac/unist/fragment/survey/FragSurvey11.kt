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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.ac.unist.R
import kr.ac.unist.database.DbManager
import kr.ac.unist.database.entity.User
import kr.ac.unist.databinding.*
import kr.ac.unist.fragment.network.Network
import kr.ac.unist.manager.SharedPrefManager
import kr.ac.unist.util.Util
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.util.*

/**
 * SignUp 11 Screen
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-13 오후 2:39
 **/
class FragSurvey11 : Fragment() {

    // 바인딩
    lateinit var binding: FragSurvey11Binding

    // 선택한 번호
    var resultIndex: Int = -1;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.binding = DataBindingUtil.inflate<FragSurvey11Binding>(
            inflater, R.layout.frag_survey_11, container, false
        )
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
    fun init() {

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
    fun clickNext(view: View) {

        if (this.resultIndex == -1) {
            Util.showSimpleSnackbar(
                binding.root,
                requireContext(),
                getString(R.string.msg_survey_select_answer),
                Snackbar.LENGTH_SHORT
            )
        } else {

            var args = FragSurvey11Args.fromBundle(requireArguments())

            var answerList: List<String> = args.answer10.split(",")
            lateinit var user: List<User>

            var jop = GlobalScope.launch {
                user = DbManager.getInstance().userDao().findUser(SharedPrefManager.getUserSeqId())
            }

            jop.invokeOnCompletion {

                var json: JSONObject = JSONObject()
                json.put("userKey", user[0].userKey)
                json.put("summary", 10)

                if (answerList[0].equals("2")) {
                    json.put("quiz1", "T")
                } else {
                    json.put("quiz1", "F")
                }

                // 퀴즈2 정답 구하기
                var cal: Calendar = Calendar.getInstance()
                var year: Int = cal.get(Calendar.YEAR)
                var month: Int = cal.get(Calendar.MONTH) + 1
                var day: Int = cal.get(Calendar.DATE)
                var dayOFWeek: Int = cal.get(Calendar.DAY_OF_WEEK)

                var pass: Boolean = false;

                if (year.toString().equals(answerList[1]) &&
                    month.toString().equals(answerList[2]) &&
                    day.toString().equals(answerList[3])
                ) {

                    // 요일 검사
                    when (answerList[5]) {
                        "월" -> {
                            if (dayOFWeek == 2) pass = true
                        }
                        "화" -> {
                            if (dayOFWeek == 3) pass = true
                        }
                        "수" -> {
                            if (dayOFWeek == 4) pass = true
                        }
                        "목" -> {
                            if (dayOFWeek == 5) pass = true
                        }
                        "금" -> {
                            if (dayOFWeek == 6) pass = true
                        }
                        "토" -> {
                            if (dayOFWeek == 7) pass = true
                        }
                        "일" -> {
                            if (dayOFWeek == 1) pass = true
                        }
                    }

                    // 계절 검사
                    if (pass) {
                        pass = false
                        if (month.toString().equals("1") || month.toString().equals("2") || month.toString().equals("11") || month.toString().equals(
                                "12"
                            )
                        ) {
                            // 겨울
                            if (answerList[4].equals("겨울")) pass = true
                        } else if (month.toString().equals("3") || month.toString().equals("4") || month.toString().equals("5")) {
                            // 봄
                            if (answerList[4].equals("봄")) pass = true
                        } else if (month.toString().equals("6") || month.toString().equals("7") || month.toString().equals("8")) {
                            // 여름
                            if (answerList[4].equals("여름")) pass = true
                        } else if (month.toString().equals("9") || month.toString().equals("10")) {
                            // 가을
                            if (answerList[4].equals("가을")) pass = true
                        }
                    }
                }

                if (pass) {
                    json.put("quiz2", "T")
                } else {
                    json.put("quiz2", "F")
                }

                if (answerList[6].equals("2") && answerList[7].equals("2") &&
                    answerList[8].equals("1") && answerList[9].equals("2") &&
                    answerList[10].equals("3")) {
                    json.put("quiz3", "T")
                } else {
                    json.put("quiz3", "F")
                }

                if (answerList[11].equals("3")) {
                    json.put("quiz4", "T")
                } else {
                    json.put("quiz4", "F")
                }

                if (answerList[12].equals("2")) {
                    json.put("quiz5", "T")
                } else {
                    json.put("quiz5", "F")
                }

                if (answerList[13].equals("1")) {
                    json.put("quiz6", "T")
                } else {
                    json.put("quiz6", "F")
                }

                if (answerList[14].equals("2")) {
                    json.put("quiz7", "T")
                } else {
                    json.put("quiz7", "F")
                }

                if (answerList[15].equals("1")) {
                    json.put("quiz8", "T")
                } else {
                    json.put("quiz8", "F")
                }

                if (answerList[16].equals("3")) {
                    json.put("quiz9", "T")
                } else {
                    json.put("quiz9", "F")
                }

                if (this.resultIndex.toString().equals("2")) {
                    json.put("quiz10", "T")
                } else {
                    json.put("quiz10", "F")
                }

                Network.POST(Network.QUIZE, json, object : Callback {

                    override fun onFailure(call: Call, e: IOException) {
                    }

                    override fun onResponse(call: Call, response: Response) {

                        user[0].firstAnswer = "Y"
                        DbManager.getInstance().userDao().updateUserInfo(user[0])

                        // 메인 이동
                        findNavController().navigate(FragSurvey11Directions.actionFragSurvey11ToFragMain())
                    }
                })

            }


        }


    }

    fun clickQuiz(index: Int) {

        Timber.d("%d", index)

        this.resultIndex = index

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