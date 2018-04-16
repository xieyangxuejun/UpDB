package com.foretree.db;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.internal.DaoConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by silen on 2018/4/10
 */

public class MigrationHelper {

    public interface OnMigrationListener {
        void createAllTables(Database database, boolean ifNotExists);
        void dropAllTables(Database database, boolean ifExists);
    }

    private OnMigrationListener mListener;

    public MigrationHelper(@NonNull OnMigrationListener listener) {
        this.mListener = listener;
    }


    public void migrate(Database db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        generateTempTables(db, daoClasses);
        mListener.dropAllTables(db, true);
        mListener.createAllTables(db, false);
        restoreData(db, daoClasses);
    }

    private void generateTempTables2(Database db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        for (Class<? extends AbstractDao<?, ?>> daoClass : daoClasses) {
            //rename old database
            DaoConfig daoConfig = new DaoConfig(db, daoClass);
            String tableName = daoConfig.tablename;
            String tempTableName = tableName.concat("_TEMP");
            String renameSql = "ALTER TABLE " + tableName + " RENAME TO " + tempTableName;
            db.execSQL(renameSql);

            //add new columns
            //sql: ALTER TABLE USER_TEMP ADD COLUMN
            String alterStr = "";
            for (Property p : daoConfig.properties) {
                String columnName = p.columnName;
                if (getColumns(db, tableName).contains(columnName)) continue;


            }
//            String addColumnsSql = "ALTER TABLE " + tableName + " ADD COLUMN " +
        }
    }

    private void generateTempTables(Database db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        for (Class<? extends AbstractDao<?, ?>> daoClass : daoClasses) {
            DaoConfig daoConfig = new DaoConfig(db, daoClass);

            String tableName = daoConfig.tablename;
            String tempTableName = daoConfig.tablename.concat("_TEMP");
            ArrayList<String> properties = new ArrayList<>();

            String createTableSql = "CREATE TABLE " + tempTableName + " (%s);";

            List<String> partySqlList = new ArrayList<>();
            for (Property p : daoConfig.properties) {

                String type = null;
                try {
                    type = getTypeByClass(p.type);
                } catch (Exception ignored) {
                }
                String party = p.columnName + " " + type;
                if (getColumns(db, tableName).contains(p.columnName)) {
                    //主键
                    if (p.primaryKey) party += " PRIMARY KEY AUTOINCREMENT ";
                    properties.add(p.columnName);
                } else {
                    party += " DEFAULT 0";
                }
                partySqlList.add(party);
            }

            createTableSql = String.format(Locale.getDefault(), createTableSql, TextUtils.join(",", partySqlList));

            //CREATE TABLE USER_TEMP (_id INTEGER PRIMARY KEY,NAME TEXT);
            db.execSQL(createTableSql);

            String insertTableStringBuilder = "INSERT INTO " + tempTableName + " (" +
                    TextUtils.join(",", properties) +
                    ") SELECT " +
                    TextUtils.join(",", properties) +
                    " FROM " + tableName + ";";
            //INSERT INTO USER_TEMP (_id,NAME) SELECT _id,NAME FROM USER;
            db.execSQL(insertTableStringBuilder);
        }
    }

    private void restoreData(Database db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        for (Class<? extends AbstractDao<?, ?>> daoClass : daoClasses) {
            DaoConfig daoConfig = new DaoConfig(db, daoClass);

            String tableName = daoConfig.tablename;
            String tempTableName = daoConfig.tablename.concat("_TEMP");
            List<String> properties = new ArrayList<>();

            for (int j = 0; j < daoConfig.properties.length; j++) {
                String columnName = daoConfig.properties[j].columnName;

                if (getColumns(db, tempTableName).contains(columnName)) {
                    properties.add(columnName);
                }
            }

            String columns = TextUtils.join(",", properties);
            String insertTableStringBuilder = "INSERT INTO "
                    + tableName + " (" + columns
                    + ") SELECT " + columns
                    + " FROM " + tempTableName + ";";
            //crash by age is not null
            db.execSQL(insertTableStringBuilder);
            db.execSQL("DROP TABLE " + tempTableName);
        }
    }

    private String getTypeByClass(Class<?> type) throws Exception {
        if (type.equals(String.class)) {
            return "TEXT";
        }
        if (type.equals(Boolean.class)) {
            return "BOOLEAN";
        }
        return "INTEGER";
    }

    private List<String> getColumns(Database db, String tableName) {
        List<String> columns = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " limit 1", null);
            if (cursor != null) {
                columns = new ArrayList<>(Arrays.asList(cursor.getColumnNames()));
            }
        } catch (Exception e) {
            Log.v(tableName, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return columns;
    }
}
