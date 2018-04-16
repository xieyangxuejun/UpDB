package com.foretree.db

import android.database.Cursor
import android.text.TextUtils
import android.util.Log
import org.greenrobot.greendao.AbstractDao
import org.greenrobot.greendao.database.Database
import org.greenrobot.greendao.internal.DaoConfig
import java.util.*

/**
 * <p>
 *     green dao database upgrade
 *     </p>
 * Created by silen on 16/04/2018.
 */

class GreenDaoMigrationHelper(var onMigrationListener: OnMigrationListener) {

    fun migrate(db: Database, vararg daoClasses: Class<out AbstractDao<*, *>>) {
        generateTempTables(db, *daoClasses)
        onMigrationListener.dropAllTables(db, true)
        onMigrationListener.createAllTables(db, false)
        restoreData(db, *daoClasses)
    }

    private fun generateTempTables(db: Database, vararg daoClasses: Class<out AbstractDao<*, *>>) {
        for (daoClass in daoClasses) {
            val daoConfig = DaoConfig(db, daoClass)

            val tableName = daoConfig.tablename
            val tempTableName = daoConfig.tablename + "_TEMP"
            val properties = ArrayList<String>()

            var createTableSql = "CREATE TABLE $tempTableName (%s);"

            val partySqlList = ArrayList<String>()
            for (p in daoConfig.properties) {

                var type: String? = null
                try {
                    type = getTypeByClass(p.type)
                } catch (ignored: Exception) {
                }

                var party = p.columnName + " " + type
                if (getColumns(db, tableName).contains(p.columnName)) {
                    //主键
                    if (p.primaryKey) party += " PRIMARY KEY AUTOINCREMENT "
                    properties.add(p.columnName)
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

    private fun restoreData(db: Database, vararg daoClasses: Class<out AbstractDao<*, *>>) {
        for (daoClass in daoClasses) {
            val daoConfig = DaoConfig(db, daoClass)

            val tableName = daoConfig.tablename
            val tempTableName = daoConfig.tablename + "_TEMP"
            val properties = ArrayList<String>()

            for (j in daoConfig.properties.indices) {
                val columnName = daoConfig.properties[j].columnName

                if (getColumns(db, tempTableName).contains(columnName)) {
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

    private fun getTypeByClass(type: Class<*>): String {
        if (type == String::class.java) {
            return "TEXT"
        }
        if (type == Boolean::class.java) {
            return "BOOLEAN"
        }
        return "INTEGER"
    }

    private fun getColumns(db: Database, tableName: String): List<String> {
        var columns: List<String> = ArrayList()
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("SELECT * FROM $tableName limit 1", null)
            if (cursor != null) {
                columns = ArrayList(Arrays.asList(*cursor!!.columnNames))
            }
        } catch (e: Exception) {
            Log.v(tableName, e.message, e)
            e.printStackTrace()
        } finally {
            if (cursor != null) cursor.close()
        }
        return columns
    }
}

interface OnMigrationListener {
    fun createAllTables(database: Database, ifNotExists: Boolean)
    fun dropAllTables(database: Database, ifExists: Boolean)
}