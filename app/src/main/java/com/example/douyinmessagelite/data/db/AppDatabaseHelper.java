package com.example.douyinmessagelite.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//数据库SQLite
public class AppDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "douyin_messages.db";
    private static final int DB_VERSION = 2;

    public static final String TABLE_MESSAGE = "messages";

    public AppDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    } // 构造函数

    // 建表
    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + TABLE_MESSAGE + " (" +
                "id INTEGER PRIMARY KEY," +
                "avatarUrl TEXT," +
                "nickname TEXT," +
                "content TEXT," +
                "timestamp INTEGER," +
                "type INTEGER," +
                "unread INTEGER," +
                "imageUrl TEXT," +
                "actionText TEXT," +
                "remark TEXT," +
                "isPinned INTEGER DEFAULT 0" +
                ")";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_MESSAGE +
                    " ADD COLUMN isPinned INTEGER DEFAULT 0");
        }
    }
}
