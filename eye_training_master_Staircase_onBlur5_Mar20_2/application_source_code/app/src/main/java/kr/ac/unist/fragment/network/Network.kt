package kr.ac.unist.fragment.network

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


/**
* Network
* @author 임성진
* @version 1.0.0
* @since 2021-05-21 오후 5:46
**/
class Network {

    companion object {

        val MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        val BASE_URL = "https://eyetrainingdatabase-production.up.railway.app"                      // 기본 URL
        val REGISTER = BASE_URL + "/register"                   // 회원가입
        val QUIZE = BASE_URL + "/record/survey/"                // 퀴즈 정답 전송

        val TRAIN_TRIAL = BASE_URL + "/record/train/trial"      // 시력 훈련 - 시도 단위 기록 (trial)
        val TRAIN_BLOCK = BASE_URL + "/record/train/block"      // 시력 훈련 - 블럭단위 기록 (block)
        val TRAIN_SESSION = BASE_URL + "/record/train/session"  // 시력 훈련 - 세션 단위 기록 (session)

        val TEST_TRIAL = BASE_URL + "/record/test/trial"      // 시력 진단 - 시도 단위 기록 (trial)
        val TEST_BLOCK = BASE_URL + "/record/test/block"      // 시력 진단 - 블럭단위 기록 (block)
        val TEST_SESSION = BASE_URL + "/record/test/session"  // 시력 진단 - 세션 단위 기록 (session)

        // client
        lateinit var client: OkHttpClient

        fun init() {
            this.client = OkHttpClient()
        }

        /**
        * GET
        * @author 임성진
        * @version 1.0.0
        * @since 2021-05-21 오후 6:01
        **/
        fun GET(url: String, callback: Callback): Call {
            val request = Request.Builder()
                .url(url)
                .build()

            val call = client.newCall(request)
            call.enqueue(callback)
            return call
        }

        /**
        * POST
        * @author 임성진
        * @version 1.0.0
        * @since 2021-05-21 오후 6:01
        **/
        fun POST(url: String, json: JSONObject, callback: Callback): Call {

            val body = json.toString().toRequestBody(MEDIA_TYPE)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val call = client.newCall(request)
            call.enqueue(callback)
            return call
        }

    }

}