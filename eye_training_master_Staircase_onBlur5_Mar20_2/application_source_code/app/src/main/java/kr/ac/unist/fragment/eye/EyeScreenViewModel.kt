package kr.ac.unist.fragment.eye

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
* 측정 화면 뷰 모델
* @author 임성진
* @version 1.0.0
* @since 2021-06-03 오후 2:43
**/
class EyeScreenViewModel : ViewModel() {

    /** data **/
    private var _resultText = MutableLiveData<String>()
    private var _resultTextVisible = MutableLiveData<Int>()
    private var _screenTitleText = MutableLiveData<String>()
    private var _titleText = MutableLiveData<String>()
    private var _startMessageText = MutableLiveData<String>()
    private var _startBtnText = MutableLiveData<String>()
    private var _trainVisible = MutableLiveData<Int>()
    private var _eyeScreenViewVisible = MutableLiveData<Int>()
    private var _screenTitleTextVisible = MutableLiveData<Int>()
    private var _screenTestGuideVisible = MutableLiveData<Int>()
    private var _introVisible = MutableLiveData<Int>()
    private var _drawEye = MutableLiveData<EyeDrawVO>()

    /** live data **/
    val resultText : LiveData<String> get() = _resultText                           // 결과 Text
    val resultTextVisible : LiveData<Int> get() = _resultTextVisible                // 결과 Text 숨김 여부
    val screenTitleText :  LiveData<String> get() = _screenTitleText                // 스크린 text
    val titleText : LiveData<String> get() = _titleText                             // 시작 제목 Text
    val startMessageText : LiveData<String> get() = _startMessageText               // 시작 내용 Text
    val startBtnText : LiveData<String> get() = _startBtnText                       // 시작 버튼 Text
    val trainVisible : LiveData<Int> get() = _trainVisible                          // Train 컨테이너 숨김 여부
    val eyeScreenViewVisible : LiveData<Int> get() = _eyeScreenViewVisible          // 측정 이미지 표시되는 뷰 숨김 여부
    val screenTitleTextVisible : LiveData<Int> get() = _screenTitleTextVisible      // 스크린 text 숨김 여부
    val screenTestGuideVisible : LiveData<Int> get() = _screenTestGuideVisible      // 시력 훈련 가이드 그림 숨김 여부
    val introVisible : LiveData<Int> get() = _introVisible                          // 인트로 화면 숨김 여부
    val drawEye : LiveData<EyeDrawVO> get() = _drawEye                              // 훈련 그림

    init {
        _eyeScreenViewVisible.value = View.GONE
        _screenTitleTextVisible.value = View.VISIBLE
        _introVisible.value = View.VISIBLE
    }

    /**
    * 훈련 그림 그리기
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-03 오후 3:24
    **/
    fun drawEyeScreen(eyeDrawVO : EyeDrawVO){
        _drawEye.value = eyeDrawVO
    }

    /**
    * 훈련 시작 화면 보이기
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-03 오후 3:12
    **/
    fun showTrainStartUi(){
        _screenTitleTextVisible.value = View.GONE
        _introVisible.value = View.GONE
        _eyeScreenViewVisible.value = View.GONE
    }

    /**
    * 시작 화면 텍스트 교체
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-03 오후 2:50
    **/
    fun changeStartContainerText(screenTitle : String, title : String, startMessage : String, startBtn : String, isVisible : Int, testGuideVisible : Int) {
        _screenTitleText.value = screenTitle
        _titleText.value = title
        _startMessageText.value = startMessage
        _startBtnText.value = startBtn
        _trainVisible.value = isVisible
        _screenTestGuideVisible.value = testGuideVisible
    }

    /**
    * 측정 뷰 보이기
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-03 오후 3:04
    **/
    fun showEyeScreenView(){
       _eyeScreenViewVisible.value = View.VISIBLE
    }

    /**
     * 측정 뷰 숨기기
     * @author 임성진
     * @version 1.0.0
     * @since 2021-06-03 오후 3:04
     **/
    fun hideEyeScreenView(){
        _eyeScreenViewVisible.value = View.GONE
    }

    /**
    * 시력 훈련 가이드 보이기
    * @author 임성진
    * @version 1.0.0
    * @since 2021-08-09 오후 1:00
    **/
    fun showTestGuide(){
        _screenTestGuideVisible.value = View.VISIBLE
    }

    /**
    * 시력 훈련 가이드 숨기기
    * @author 임성진
    * @version 1.0.0
    * @since 2021-08-09 오후 1:00
    **/
    fun hideTestGuide(){
        _screenTestGuideVisible.value = View.GONE
    }

    /**
    * 결과를 표시한다.
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-03 오후 2:20
    **/
    fun showResult(result : String){
        _resultText.value = result
        _resultTextVisible.value = View.VISIBLE
    }

    /**
    * 결과를 숨긴다
    * @author 임성진
    * @version 1.0.0
    * @since 2021-06-03 오후 2:28
    **/
    fun hideResult() {
        _resultTextVisible.value = View.GONE
    }

}