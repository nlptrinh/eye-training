package kr.ac.unist.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 유저 Entity
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-18 오후 5:15
 **/
@Entity
class User(
    var userId: String,     //  아이디
    var password: String,   //  비밀번호
    var userKey: String,    //  유저 키
    var sex: String,        //  성별
    var surgery: String,    //  눈 수술 내역
    var eyesight: String,   //  교정 시력
    var lens: String,       //  안과 수술 경험 유무 (돋보기, 근시교정, 원시교정, 다초점, 없음) 중복 선택의 경우 쉼표로 구분
    var disorder: String,   // 뇌질환 및 정신질환 유무 (T 또는 F)
    var birthDay: String,   // 생년월일
    var firstAnswer: String) {  // 퀴즈 제출 여부
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}