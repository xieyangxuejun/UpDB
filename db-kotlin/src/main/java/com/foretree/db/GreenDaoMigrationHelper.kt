package com.foretree.db

import android.database.Cursor
import android.text.TextUtils
import org.greenrobot.greendao.AbstractDao
import org.greenrobot.greendao.database.Database
import org.greenrobot.greendao.internal.DaoConfig
import java.util.*
import kotlin.collections.ArrayList

/**
 * <p>
 *     green dao database upgrade
 *     </p>
 * Created by silen on 16/04/2018.
 */

class GreenDaoMigrationHelper(private var onMigrationListener: OnMigrationListener) {

    @SafeVarargs
    fun migrate(db: Database, vararg daoClasses: Class<out AbstractDao<*, *>>) {
        generateTempTables(db, *daoClasses)
        onMigrationListener.dropAllTables(db, true)
        onMigrationListener.createAllTables(db, false)
        restoreData(db, *daoClasses)
    }

    @SafeVarargs
    private fun generateTempTables(db: Database, vararg daoClasses: Class<out AbstractDao<*, *>>) {
        for (daoClass in daoClasses) {
            val daoConfig = DaoConfig(db, daoClass)

            val tableName = daoConfig.tablename
            val tempTableName = daoConfig.tablename + "_TEMP"
            val properties = ArrayList<String>()

            var createTableSql = "CREATE TABLE $tempTableName (%s);"

            val partySqlList = ArrayList<String>()
            daoConfig.properties.forEach {
                var type: String? = null
                try {
                    type = getTypeByClass(it.type)
                } catch (ignored: Exception) {
                }

                var party = it.columnName + " " + type
                if (getColumns2(db, tableName).contains(it.columnName)) {
                    //主键
                    if (it.primaryKey) party += " PRIMARY KEY AUTOINCREMENT "
                    properties.add(it.columnName)
                } else {
                    party += " DEFAULT 0"
                }
                partySqlList.add(party)
            }

            createTableSql = String.format(Locale.getDefault(), createTableSql, TextUtils.join(",", partySqlList))

            //CREATE TABLE USER_TEMP (_id INTEGER PRIMARY KEY,NAME TEXT);
            db.execSQL(createTableSql)

            val insertTableStringBuilder = "INSERT INTO " + tempTableName + " (" +
                    TextUtils.join(",", properties) +
                    ") SELECT " +
                    TextUtils.join(",", properties) +
                    " FROM " + tableName + ";"
            //INSERT INTO USER_TEMP (_id,NAME) SELECT _id,NAME FROM USER;
            db.execSQL(insertTableStringBuilder)
        }
    }

    @SafeVarargs
    private fun restoreData(db: Database, vararg daoClasses: Class<out AbstractDao<*, *>>) {
        for (daoClass in daoClasses) {
            val daoConfig = DaoConfig(db, daoClass)

            val tableName = daoConfig.tablename
            val tempTableName = daoConfig.tablename + "_TEMP"
            val properties = ArrayList<String>()

            daoConfig.properties.forEach {
                val columnName = it.columnName

                if (getColumns2(db, tempTableName).contains(columnName)) {
                    properties.add(columnName)
                }
            }

            val columns = TextUtils.join(",", properties)
            val insertTableStringBuilder = ("INSERT INTO "
                    + tableName + " (" + columns
                    + ") SELECT " + columns
                    + " FROM " + tempTableName + ";")
            //crash by age is not null
            db.execSQL(insertTableStringBuilder)
            db.execSQL("DROP TABLE $tempTableName")
        }
    }

    @SafeVarargs
    private fun getTypeByClass(type: Class<*>): String {
        if (type == String::class.java) {
            return "TEXT"
        }
        if (type == Boolean::class.java) {
            return "BOOLEAN"
        }
        return "INTEGER"
    }

    @SafeVarargs
    private fun getColumns(db: Database, tableName: String): List<String> {
        var columns: List<String> = ArrayList()
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("SELECT * FROM $tableName limit 1", null)
            if (cursor != null) {
                columns = ArrayList(Arrays.asList(*cursor.columnNames))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (cursor != null) cursor.close()
        }
        return columns
    }

    @SafeVarargs
    private fun getColumns2(db: Database, tableName: String):List<String> {
        return db.rawQuery("SELECT * FROM $tableName limit 1", null).let {
            var columns:List<String> = ArrayList()
            if (it != null){
                columns = ArrayList(Arrays.asList(*it.columnNames))
            }
            columns
        }
    }
}

interface OnMigrationListener {
    fun createAllTables(database: Database, ifNotExists: Boolean)
    fun dropAllTables(database: Database, ifExists: Boolean)
}