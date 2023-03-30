package kr.ac.unist.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kr.ac.unist.database.entity.*

/**
 * 시력 훈련/진단 DAO
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-18 오후 5:15
 **/
@Dao
interface ProcessDao {

    @Insert
    fun insertTrainTrialInfo(trainTrial: TrainTrial) : Long

    @Insert
    fun insertTestTrialInfo(testTrial: TestTrial) : Long

    @Query("SELECT * FROM (SELECT * FROM TrainTrial WHERE userSeqId = :userSeqId ORDER BY CAST(block AS INT)) ORDER BY instDtm")
    fun selectTrainTrialInfo(userSeqId: String) : List<TrainTrial>;

    @Query("SELECT * FROM (SELECT * FROM TestTrial WHERE userSeqId = :userSeqId ORDER BY CAST(block AS INT)) ORDER BY instDtm")
    fun selectTestTrialInfo(userSeqId: String) : List<TestTrial>;

    @Query("SELECT * FROM TestTrial WHERE userSeqId = :userSeqId and block = :block ORDER BY CAST(block AS INT), instDtm")
    fun selectTestTrialInfoByBlock(userSeqId: String, block: String) : List<TestTrial>;

    @Query("SELECT * FROM TrainTrial WHERE userSeqId = :userSeqId and instDtm between :start and :end order by instDtm asc")
    fun selectLatestTrainTrialInfo(userSeqId: String, start : Long, end : Long) : List<TrainTrial>

    @Query("SELECT * FROM TestTrial WHERE userSeqId = :userSeqId and instDtm between :start and :end order by instDtm asc")
    fun selectLatestTestTrialInfo(userSeqId: String, start : Long, end : Long) : List<TestTrial>

    @Query("SELECT * FROM TrainTrial WHERE userSeqId = :userSeqId and block = :block and instDtm between :start and :end")
    fun selectTodayAndBlockTrainTrialInfo(userSeqId: String, start : Long, end : Long, block : String) : List<TrainTrial>

    @Query("SELECT * FROM TestTrial WHERE userSeqId = :userSeqId and block = :block and instDtm between :start and :end")
    fun selectTodayAndBlockTestTrialInfo(userSeqId: String, start : Long, end : Long, block : String) : List<TestTrial>

    @Query("SELECT * FROM TrainTrial WHERE userSeqId = :userSeqId and instDtm between :start and :end")
    fun selectTodayTrainTrial(userSeqId: String, start : Long, end : Long) : List<TrainTrial>

    @Query("SELECT * FROM TestTrial WHERE userSeqId = :userSeqId and instDtm between :start and :end")
    fun selectTodayTestTrial(userSeqId: String, start : Long, end : Long) : List<TestTrial>
    
    @Query("DELETE FROM TrainTrial")
    fun deleteTrainTrial()

    @Query("DELETE FROM TestTrial")
    fun deleteTestTrial()

    @Query("SELECT count(*) FROM TrainTrial WHERE userSeqId = :userSeqId and instDtm between :start and :end")
    fun getTrainTrialCount(userSeqId: String, start : Long, end : Long) : Int

    @Query("SELECT count(*) FROM TestTrial WHERE userSeqId = :userSeqId and instDtm between :start and :end")
    fun getTestTrialCount(userSeqId: String, start : Long, end : Long) : Int

}