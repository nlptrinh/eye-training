package kr.ac.unist.database

import android.app.admin.DevicePolicyManager
import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kr.ac.unist.database.dao.ProcessDao
import kr.ac.unist.database.dao.UserDao
import kr.ac.unist.database.entity.*
import java.util.*

/**
 * Database
 * @author 임성진
 * @version 1.0.0
 * @since 2021-05-13 오후 12:51
 **/
@Database(entities = [User::class, TestTrial::class, TrainTrial::class], version = 2)
abstract class DbManager : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun processDao(): ProcessDao

    companion object {

        val DB_NANE: String = "eye"

        lateinit var db: DbManager;

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE TestTrial ADD COLUMN degree TEXT NOT NULL DEFAULT '0'")
            }
        }

        fun init(context: Context) {
            db = Room.databaseBuilder(context, DbManager::class.java, DB_NANE)
                .addMigrations(MIGRATION_1_2)
                .build()
        }

        fun getInstance(): DbManager {
            return db
        }

        /**
         * 오늘 진행한 Train trial 개수 반환
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-26 오후 4:44
         **/
        fun getTodayTrainTrialCount(userSeqId: String): Int {

            var calStart: Calendar = Calendar.getInstance()
            var calEnd: Calendar = Calendar.getInstance()

            calStart.set(Calendar.HOUR_OF_DAY, 0)
            calStart.set(Calendar.MINUTE, 0)
            calStart.set(Calendar.SECOND, 0)

            calEnd.set(Calendar.HOUR_OF_DAY, 23)
            calEnd.set(Calendar.MINUTE, 59)
            calEnd.set(Calendar.SECOND, 59)

            return getInstance().processDao()
                .getTrainTrialCount(userSeqId, calStart.timeInMillis, calEnd.timeInMillis)

        }

        /**
         * 오늘 진행한 Test trial 개수 반환
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-26 오후 4:44
         **/
        fun getTodayTestTrialCount(userSeqId: String): Int {

            var calStart: Calendar = Calendar.getInstance()
            var calEnd: Calendar = Calendar.getInstance()

            calStart.set(Calendar.HOUR_OF_DAY, 0)
            calStart.set(Calendar.MINUTE, 0)
            calStart.set(Calendar.SECOND, 0)

            calEnd.set(Calendar.HOUR_OF_DAY, 23)
            calEnd.set(Calendar.MINUTE, 59)
            calEnd.set(Calendar.SECOND, 59)

            return getInstance().processDao()
                .getTestTrialCount(userSeqId, calStart.timeInMillis, calEnd.timeInMillis)

        }

        /**
         * 오늘 Train Block 정보를 가져온다.
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-26 오후 4:44
         **/
        fun getTrainBlockInfo(userSeqId: String, block: String): List<TrainTrial> {

            var calStart: Calendar = Calendar.getInstance()
            var calEnd: Calendar = Calendar.getInstance()

            calStart.set(Calendar.HOUR_OF_DAY, 0)
            calStart.set(Calendar.MINUTE, 0)
            calStart.set(Calendar.SECOND, 0)

            calEnd.set(Calendar.HOUR_OF_DAY, 23)
            calEnd.set(Calendar.MINUTE, 59)
            calEnd.set(Calendar.SECOND, 59)

            return getInstance().processDao().selectTodayAndBlockTrainTrialInfo(
                userSeqId,
                calStart.timeInMillis,
                calEnd.timeInMillis,
                block
            )

        }

        /**
         * 오늘 Test Block 정보를 가져온다.
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-26 오후 4:44
         **/
        fun getTestBlockInfo(userSeqId: String, block: String): List<TestTrial> {

            var calStart: Calendar = Calendar.getInstance()
            var calEnd: Calendar = Calendar.getInstance()

            calStart.set(Calendar.HOUR_OF_DAY, 0)
            calStart.set(Calendar.MINUTE, 0)
            calStart.set(Calendar.SECOND, 0)

            calEnd.set(Calendar.HOUR_OF_DAY, 23)
            calEnd.set(Calendar.MINUTE, 59)
            calEnd.set(Calendar.SECOND, 59)

            return getInstance().processDao().selectTodayAndBlockTestTrialInfo(
                userSeqId,
                calStart.timeInMillis,
                calEnd.timeInMillis,
                block
            )

        }

        /**
         * 오늘 Train 정보를 가져온다.
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-26 오후 4:44
         **/
        fun getTodayTrainTrial(userSeqId: String): List<TrainTrial> {

            var calStart: Calendar = Calendar.getInstance()
            var calEnd: Calendar = Calendar.getInstance()

            calStart.set(Calendar.HOUR_OF_DAY, 0)
            calStart.set(Calendar.MINUTE, 0)
            calStart.set(Calendar.SECOND, 0)

            calEnd.set(Calendar.HOUR_OF_DAY, 23)
            calEnd.set(Calendar.MINUTE, 59)
            calEnd.set(Calendar.SECOND, 59)

            return getInstance().processDao()
                .selectTodayTrainTrial(userSeqId, calStart.timeInMillis, calEnd.timeInMillis)

        }

        /**
         * 오늘 Test 정보를 가져온다.
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-26 오후 4:44
         **/
        fun getTodayTestTrial(userSeqId: String): List<TestTrial> {

            var calStart: Calendar = Calendar.getInstance()
            var calEnd: Calendar = Calendar.getInstance()

            calStart.set(Calendar.HOUR_OF_DAY, 0)
            calStart.set(Calendar.MINUTE, 0)
            calStart.set(Calendar.SECOND, 0)

            calEnd.set(Calendar.HOUR_OF_DAY, 23)
            calEnd.set(Calendar.MINUTE, 59)
            calEnd.set(Calendar.SECOND, 59)

            return getInstance().processDao()
                .selectTodayTestTrial(userSeqId, calStart.timeInMillis, calEnd.timeInMillis)

        }

        /**
         * 최근 7일 Train 정보를 가져온다.
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-26 오후 4:44
         **/
        fun getLatestTrainTrial(userSeqId: String): List<TrainTrial> {

            var calStart: Calendar = Calendar.getInstance()
            var calEnd: Calendar = Calendar.getInstance()

            calStart.add(Calendar.DAY_OF_YEAR, -6);
            calStart.set(Calendar.HOUR_OF_DAY, 0)
            calStart.set(Calendar.MINUTE, 0)
            calStart.set(Calendar.SECOND, 0)

            calEnd.set(Calendar.HOUR_OF_DAY, 23)
            calEnd.set(Calendar.MINUTE, 59)
            calEnd.set(Calendar.SECOND, 59)

            return getInstance().processDao()
                .selectLatestTrainTrialInfo(userSeqId, calStart.timeInMillis, calEnd.timeInMillis)

        }

        /**
         * 최근 7일 test 정보를 가져온다.
         * @author 임성진
         * @version 1.0.0
         * @since 2021-05-26 오후 4:44
         **/
        fun getLatestTestTrial(userSeqId: String): List<TestTrial> {

            var calStart: Calendar = Calendar.getInstance()
            var calEnd: Calendar = Calendar.getInstance()

            calStart.add(Calendar.DAY_OF_YEAR, -6);
            calStart.set(Calendar.HOUR_OF_DAY, 0)
            calStart.set(Calendar.MINUTE, 0)
            calStart.set(Calendar.SECOND, 0)

            calEnd.set(Calendar.HOUR_OF_DAY, 23)
            calEnd.set(Calendar.MINUTE, 59)
            calEnd.set(Calendar.SECOND, 59)

            return getInstance().processDao()
                .selectLatestTestTrialInfo(userSeqId, calStart.timeInMillis, calEnd.timeInMillis)

        }

        /**
         * Train 정보를 가져온다.
         * @author 임성진
         * @version 1.0.0
         * @since 2021-07-02 오후 3:49
         **/
        fun getTrainTrial(userSeqId: String): List<TrainTrial> {
            return getInstance().processDao().selectTrainTrialInfo(userSeqId)
        }

        /**
         * Test 정보를 가져온다.
         * @author 임성진
         * @version 1.0.0
         * @since 2021-07-02 오후 3:49
         **/
        fun getTestTrial(userSeqId: String): List<TestTrial> {
            return getInstance().processDao().selectTestTrialInfo(userSeqId)
        }

    }


}