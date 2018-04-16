package com.foretree.db

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import java.util.*

/**
 * <p>
 *     SQLitOpenHelper 数据迁移
 *     </p>
 * Created by silen on 16/04/2018.
 */
class AndroidMigrationHelper {
    /**
     * 升级入口
     *
     * @param db db
     * @param openHelper 重新建表时需要用上
     */
    fun upgrade(db: SQLiteDatabase, openHelper: SQLiteOpenHelper) {
        val tables = queryAllTables(db)

        for (tableName in tables) {
            if ("android_metadata" == tableName || "sqlite_sequence" == tableName) {//表的元数据，过滤
                continue
            }
            val tempTableName = tableName + "_Temp"
            createTempTables(db, tableName, tempTableName)//创建临时表
            dropTable(db, tableName)//删除原表
            openHelper.onCreate(db)//创建新表
            restoreData(db, tableName, tempTableName)//恢复原表数据
            dropTable(db, tempTableName)//删除临时表
        }
    }


    /**
     * 创建零时表
     */
    private fun createTempTables(db: SQLiteDatabase, tableName: String, tempTableName: String) {
        copyTable(db, tableName, tempTableName)
    }


    /**
     * 删除所有表
     *
     * @param db
     */
    private fun dropTable(db: SQLiteDatabase, tableName: String) {

        val sql = "DROP TABLE IF EXISTS $tableName"

        db.execSQL(sql)

    }


    /**
     * 查询数据库中所有表名
     *
     * @param db
     * @return
     */
    private fun queryAllTables(db: SQLiteDatabase): List<String> {
        val list = ArrayList<String>()
        val sql = "SELECT name FROM SQLITE_MASTER WHERE type='table' ORDER BY name"
        val cursor = db.rawQuery(sql, null)
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0))
        }
        cursor.close()
        return list
    }


    /**
     * 复制表及内容
     *
     * @param db
     * @param oldTableName
     * @param newTableName
     */
    private fun copyTable(db: SQLiteDatabase, oldTableName: String, newTableName: String) {

        val sql = "CREATE TABLE IF NOT EXISTS $newTableName AS SELECT * FROM $oldTableName"
        db.execSQL(sql)
    }


    /**
     * 从临时表中恢复数据
     *
     * @param db
     * @param tableName     需要恢复的表
     * @param tableNameTemp 临时表
     */
    private fun restoreData(db: SQLiteDatabase, tableName: String, tableNameTemp: String) {


        val columns = TextUtils.join(",", queryColumns(db, tableNameTemp))

        val sql = "INSERT INTO $tableName($columns) SELECT $columns FROM $tableNameTemp"

        db.execSQL(sql)

    }

    /**
     * 获取表中所有字段名
     *
     * @return
     */
    private fun queryColumns(db: SQLiteDatabase, tableName: String): Array<String> {

        val sql = "SELECT * FROM $tableName"
        val cursor = db.rawQuery(sql, null)
        val columnNames = cursor.columnNames
        cursor.close()

        return columnNames
    }
}