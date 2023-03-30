package kr.ac.unist

/**
 * 공용 상수 모음
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-26 오전 10:49
 **/
class Constants {

    companion object {

        // 1 SESSION은 하루동안 수행할 수 있다.
        val ONE_BLOCK_TRAIN_COUNT = 40     // 1 BLOCK = 80번 시행 (test value min = 4) default is 80
        val ONE_SESSION_BLOCK_COUNT = 11    // 1 SESSION = 10 BLOCK 시행  = (11 - 1) BLOCK (test value min = 3)

        // 자극 밝기 크기
        val BRIGHTNESS_SIZE = 15

    }

}