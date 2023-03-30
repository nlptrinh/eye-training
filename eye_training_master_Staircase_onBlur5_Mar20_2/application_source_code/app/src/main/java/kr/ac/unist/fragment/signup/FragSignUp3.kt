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
import kr.ac.unist.databinding.FragSignUp3Binding
import kr.ac.unist.database.entity.User
import kr.ac.unist.fragment.network.Network
import kr.ac.unist.database.DbManager
import kr.ac.unist.manager.SharedPrefManager
import kr.ac.unist.util.Util
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/**
* SignUp 1 Screen
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragSignUp3 : Fragment() {

    // 바인딩
    lateinit var binding: FragSignUp3Binding
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.binding = DataBindingUtil.inflate<FragSignUp3Binding>(
                inflater, R.layout.frag_sign_up_3, container, false)
        this.binding.frag = this
        return this.binding.root
    }

    /**
     * complate click event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-18 오후 5:15
     **/
    fun clickComplate(view: View) {

        Util.hideKeyboard(view)

        var eyesight : String = "0"
        var lens : String = ""
        var disorder : String = ""
        var args = FragSignUp3Args.fromBundle(requireArguments())

        if (!this.binding.etEyesight.text.isNullOrEmpty()) {
            eyesight = this.binding.etEyesight.text.toString()
        } else {
            Util.showSimpleSnackbar(binding.root, requireContext(), getString(R.string.msg_sign_insert_eye_sight), Snackbar.LENGTH_SHORT)
            return
        }

        var lenList : ArrayList<String> = ArrayList<String>()
        if (this.binding.cbCheck1.isChecked) lenList.add(this.binding.cbCheck1.text.toString())
        if (this.binding.cbCheck2.isChecked) lenList.add(this.binding.cbCheck2.text.toString())
        if (this.binding.cbCheck3.isChecked) lenList.add(this.binding.cbCheck3.text.toString())
        if (this.binding.cbCheck4.isChecked) lenList.add(this.binding.cbCheck4.text.toString())
        if (this.binding.cbCheck5.isChecked) lenList.add(this.binding.cbCheck5.text.toString())

        if (lenList.size == 0) {
            Util.showSimpleSnackbar(binding.root, requireContext(), getString(R.string.msg_sign_insert_eye_list), Snackbar.LENGTH_SHORT)
            return
        }

        lens = lenList.joinToString(",")

        if (this.binding.rgDisorder.id == this.binding.rbYes.id) {
            disorder = "T"
        } else {
            disorder = "F"
        }

        var json : JSONObject = JSONObject()
        json.put("ID", args.id)
        json.put("PW", args.password)
        json.put("sex", args.sex)
        json.put("surgery", args.eyeSurgery)
        json.put("eyesight", eyesight)
        json.put("lens", lens)
        json.put("disorder", disorder)

        Network.POST(Network.REGISTER, json, object: Callback {

            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                var json = JSONObject(responseData)

                var userKey = json.optString("userKey")

                var userInfo : User = User(args.id,
                    args.password,
                    userKey,
                    args.sex,
                    args.eyeSurgery,
                    eyesight,
                    lens,
                    disorder,
                    args.birthday,
                "N")

                // 유저 정보 삽입
                var seqId : Long = DbManager.getInstance().userDao().insertUserInfo(userInfo)
                SharedPrefManager.setUserSeqId(seqId.toString())

                // 퀴즈 이동
                findNavController().navigate(FragSignUp3Directions.actionFragSignUp3ToFragSurvey1())
            }
        })
    }

}