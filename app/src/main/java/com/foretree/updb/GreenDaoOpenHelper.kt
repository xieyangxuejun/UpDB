package com.foretree.updb

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.foretree.db.GreenDaoMigrationHelper
import com.foretree.db.OnMigrationListener
import com.foretree.updb.db.greendao.DaoMaster
import org.greenrobot.greendao.AbstractDao
import org.greenrobot.greendao.database.Database

/**
 * Created by silen on 13/04/2018.
 */

class GreenDaoOpenHelper : DaoMaster.OpenHelper {
    private var clazz: Class<out AbstractDao<*, *>>? = null

    constructor(context: Context, name: String, c: Class<out AbstractDao<*, *>>) : super(context, name) {
        this.clazz = c
    }

    constructor(context: Context, name: String) : super(context, name)
    constructor(context: Context, name: String, cursorFactory: SQLiteDatabase.CursorFactory) : super(context, name, cursorFactory)

    override fun onUpgrade(db: Database, oldVersion: Int, newVersion: Int) {
        super.onUpgrade(db, oldVersion, newVersion) //do nothing
        GreenDaoMigrationHelper(object : OnMigrationListener {
            override fun createAllTables(database: Database, ifNotExists: Boolean) {
                DaoMaster.createAllTables(database, ifNotExists)
            }

            override fun dropAllTables(database: Database, ifExists: Boolean) {
                DaoMaster.dropAllTables(database, ifExists)
            }

        }).migrate(db, clazz!!)
    }
}