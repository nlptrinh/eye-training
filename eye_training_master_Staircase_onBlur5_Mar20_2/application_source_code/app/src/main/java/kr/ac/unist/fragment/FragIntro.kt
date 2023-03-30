package kr.ac.unist.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kr.ac.unist.R
import kr.ac.unist.databinding.FragIntroBinding
import kr.ac.unist.fragment.network.Network
import kr.ac.unist.util.Util
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/**
* Intro Screen
* @author 임성진
* @version 1.0.0
* @since 2021-05-13 오후 2:39
**/
class FragIntro : Fragment() {

    // 바인딩
    lateinit var binding: FragIntroBinding
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Util.settingFullScreen(requireActivity(), false)
        this.binding = DataBindingUtil.inflate<FragIntroBinding>(
                inflater, R.layout.frag_intro, container, false)
        this.binding.frag = this
        return this.binding.root
    }

    fun testGet(){

        //val url = "http://192.168.58.104/api/main/myAddress.do"
        val url = "http://192.168.0.27/api/main/myAddress.do" //liza

        Network.GET(url, object: Callback{

            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                var json = JSONObject(responseData)
            }
        })

    }

    fun testPost(){

        //val url = "http://192.168.58.104/api/main/myAddress.do"

        val url = "http://3.36.18.22/register"
        var json : JSONObject = JSONObject()
        json.put("ID", "1")

        Network.POST(url, json, object: Callback{

            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                var json = JSONObject(responseData)
            }
        })

    }

    /**
     * login click event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 2:39
     **/
    fun clickLoginButton(view : View){
        this.findNavController().navigate(FragIntroDirections.actionFragIntroToFragLogin())
    }

    /**
     * sign up click event
     * @author 임성진
     * @version 1.0.0
     * @since 2021-05-13 오후 2:39
     **/
    fun clickSignUp(view: View) {
        this.findNavController().navigate(FragIntroDirections.actionFragIntroToFragSignUp1())
    }
}