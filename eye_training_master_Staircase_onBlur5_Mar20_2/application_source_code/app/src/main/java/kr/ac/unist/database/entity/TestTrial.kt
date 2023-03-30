package kr.ac.unist.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * 시력 훈련 Train Entity
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-18 오후 5:15
 **/
@Entity
class  TestTrial(
    var userSeqId: String,  //  유저 시퀀스 ID
    var size: String,       //  자극의 실제 크기 (반지름 길이 px 단위)
    var rotation: String,   //  자극의 빗금방향 (시계방향으로 회전된 각도 단위)
    var distance: String,   //  화면과 사용자 사이 거리 (BLE장치가 출력한 거리, mm 단위)
    var answer: String,     //  사용자의 응답 (up, right, left 또는 down)
    var duration: String,   //  자극 표시 후 응답까지 걸린 시간 (second단위, 소수점 3자리까지 반올림)
    var correct: String,    //  응답의 정답 여부 (T 또는 F)
    var block: String,       //  블럭 단위
    var degree: String = "0"     //  visual angle degree
) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
    var instDtm: Long = Calendar.getInstance().timeInMillis
}