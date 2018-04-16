package com.foretree.updb

import android.content.Context
import com.foretree.updb.db.greendao.DaoMaster
import com.foretree.updb.db.greendao.DaoSession
import com.foretree.updb.db.greendao.UserDao

/**
 * database manager
 * Created by silen on 13/04/2018.
 */
object DatabaseManager {
    private var mDaoSession: DaoSession?=null

    fun initDB(context: Context) {
        val helper = GreenDaoOpenHelper(context, "user.db", UserDao::class.java)
        val db = helper.writableDb
        mDaoSession = DaoMaster(db).newSession()
    }

    fun getDaoSession(): DaoSession? = mDaoSession
}