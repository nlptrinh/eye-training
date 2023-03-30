package kr.ac.unist.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kr.ac.unist.database.entity.User

/**
 * 유저 DAO
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-18 오후 5:15
 **/
@Dao
interface UserDao {

    @Query("SELECT * FROM User")
    fun getAll(): List<User>

    @Query("SELECT * FROM User WHERE userId = :userId and password = :password")
    fun findLoginUser(userId: String, password: String) : List<User>

    @Query("SELECT * FROM User WHERE id = :userSeq")
    fun findUser(userSeq: String) : List<User>

    @Query("SELECT * FROM User WHERE userId = :userId")
    fun findLoginId(userId: String) : List<User>

    @Insert
    fun insertUserInfo(userInfo: User) : Long

    @Update
    fun updateUserInfo(userInfo: User)

}