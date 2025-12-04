package com.example.douyinmessagelite.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.douyinmessagelite.data.db.AppDatabaseHelper;
import com.example.douyinmessagelite.data.local.JsonDataSource;
import com.example.douyinmessagelite.data.model.Message;
import com.example.douyinmessagelite.data.prefs.PrefsManager;

import java.util.ArrayList;
import java.util.List;
//统一对外的数据接口
public class MessageRepository {

    private static final int PAGE_SIZE = 20; // 分页大小

    private final AppDatabaseHelper dbHelper;
    private final JsonDataSource jsonDataSource;
    private final PrefsManager prefsManager;

    public MessageRepository(Context context) {
        this.dbHelper = new AppDatabaseHelper(context);
        this.jsonDataSource = new JsonDataSource(context);
        this.prefsManager = new PrefsManager(context);
    }

    // 首次冷启动：如果还没初始化过，则从 JSON 导入数据到 SQLite
    public void initIfNeeded() {
        if (!prefsManager.hasInitFromJson()) {
            List<Message> list = jsonDataSource.loadMessagesFromAssets();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (Message msg : list) {
                    insertOrReplace(db, msg);
                }
                db.setTransactionSuccessful();
                prefsManager.setInitFromJson(true);
            } finally {
                db.endTransaction();
            }
        }
    }

    public void insertMessage(Message msg) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        insertOrReplace(db, msg);
    }



    private void insertOrReplace(SQLiteDatabase db, Message msg) {
        ContentValues cv = new ContentValues();
        cv.put("id", msg.getId());
        cv.put("avatarUrl", msg.getAvatarUrl());
        cv.put("nickname", msg.getNickname());
        cv.put("content", msg.getContent());
        cv.put("timestamp", msg.getTimestamp());
        cv.put("type", msg.getType());
        cv.put("unread", msg.isUnread() ? 1 : 0);
        cv.put("imageUrl", msg.getImageUrl());
        cv.put("actionText", msg.getActionText());
        cv.put("remark", msg.getRemark());
        cv.put("isPinned", msg.isPinned() ? 1 : 0);
        db.insertWithOnConflict(AppDatabaseHelper.TABLE_MESSAGE, null,
                cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private Message buildMessageFromCursor(Cursor cursor) {
        Message msg = new Message();
        msg.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        msg.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow("avatarUrl")));
        msg.setNickname(cursor.getString(cursor.getColumnIndexOrThrow("nickname")));
        msg.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
        msg.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")));
        msg.setType(cursor.getInt(cursor.getColumnIndexOrThrow("type")));
        msg.setUnread(cursor.getInt(cursor.getColumnIndexOrThrow("unread")) == 1);
        msg.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("imageUrl")));
        msg.setActionText(cursor.getString(cursor.getColumnIndexOrThrow("actionText")));
        msg.setRemark(cursor.getString(cursor.getColumnIndexOrThrow("remark")));
        msg.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow("isPinned")) == 1);
        return msg;
    }
    // 会话列表分页逻辑
    public List<Message> getMessages(int pageIndex) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 先把所有历史消息按时间倒序取出来
        List<Message> all = new ArrayList<>();
        Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_MESSAGE,
                null,
                null,
                null,
                null,
                null,
                "timestamp DESC",   // 最新的在前
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Message msg = new Message();
                msg.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                msg.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow("avatarUrl")));
                msg.setNickname(cursor.getString(cursor.getColumnIndexOrThrow("nickname")));
                msg.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
                msg.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")));
                msg.setType(cursor.getInt(cursor.getColumnIndexOrThrow("type")));
                msg.setUnread(cursor.getInt(cursor.getColumnIndexOrThrow("unread")) == 1);
                msg.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("imageUrl")));
                msg.setActionText(cursor.getString(cursor.getColumnIndexOrThrow("actionText")));
                msg.setRemark(cursor.getString(cursor.getColumnIndexOrThrow("remark")));
                msg.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow("isPinned")) == 1);
                all.add(msg);
            }
            cursor.close();
        }

        java.util.LinkedHashMap<String, Message> map = new java.util.LinkedHashMap<>();
        for (Message m : all) {
            String key = m.getNickname();
            if (key == null) key = "";
            if (!map.containsKey(key)) {
                // all 是按 timestamp DESC 排序的，所以第一次遇到就是最新的
                map.put(key, m);
            }
        }

        java.util.List<Message> conversations = new java.util.ArrayList<>(map.values());
        java.util.Collections.sort(conversations, (a, b) -> {
            if (a.isPinned() != b.isPinned()) {
                return a.isPinned() ? -1 : 1;   // 置顶在前
            }
            return Long.compare(b.getTimestamp(), a.getTimestamp()); // 时间新的在前
        });

        int from = pageIndex * PAGE_SIZE;
        if (from >= conversations.size()) {
            return new ArrayList<>();
        }
        int to = Math.min(from + PAGE_SIZE, conversations.size());
        return new ArrayList<>(conversations.subList(from, to));
    }




    public void updateUnread(long messageId, boolean unread) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("unread", unread ? 1 : 0);
        db.update(AppDatabaseHelper.TABLE_MESSAGE, cv,
                "id=?", new String[]{String.valueOf(messageId)});
    }

    public void updateRemark(long messageId, String remark) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("remark", remark);
        db.update(AppDatabaseHelper.TABLE_MESSAGE, cv,
                "id=?", new String[]{String.valueOf(messageId)});
    }

    public Message getMessageById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_MESSAGE,
                null,
                "id=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            Message msg = buildMessageFromCursor(cursor);
            cursor.close();
            return msg;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public List<Message> getMessagesByNickname(String nickname) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Message> result = new ArrayList<>();

        Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_MESSAGE,
                null,
                "nickname=?",
                new String[]{nickname},
                null,
                null,
                "timestamp ASC"   // 按时间顺序从早到晚
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Message msg = new Message();
                msg.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                msg.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow("avatarUrl")));
                msg.setNickname(cursor.getString(cursor.getColumnIndexOrThrow("nickname")));
                msg.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
                msg.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")));
                msg.setType(cursor.getInt(cursor.getColumnIndexOrThrow("type")));
                msg.setUnread(cursor.getInt(cursor.getColumnIndexOrThrow("unread")) == 1);
                msg.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("imageUrl")));
                msg.setActionText(cursor.getString(cursor.getColumnIndexOrThrow("actionText")));
                msg.setRemark(cursor.getString(cursor.getColumnIndexOrThrow("remark")));
                msg.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow("isPinned")) == 1);
                result.add(msg);
            }
            cursor.close();
        }
        return result;
    }


    public List<Message> searchMessages(String keyword) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Message> result = new ArrayList<>();

        String like = "%" + keyword + "%";

        Cursor cursor = db.query(
                AppDatabaseHelper.TABLE_MESSAGE,
                null,
                "nickname LIKE ? OR content LIKE ?",
                new String[]{like, like},
                null,
                null,
                "isPinned DESC, timestamp DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Message msg = buildMessageFromCursor(cursor);
                result.add(msg);
            }
            cursor.close();
        }

        return result;
    }

    // 总消息数
    public int getTotalCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + AppDatabaseHelper.TABLE_MESSAGE, null);
        try {
            if (c.moveToFirst()) return c.getInt(0);
        } finally {
            if (c != null) c.close();
        }
        return 0;
    }

    // 未读消息数
    public int getUnreadCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + AppDatabaseHelper.TABLE_MESSAGE + " WHERE unread = 1",
                null
        );
        try {
            if (c.moveToFirst()) return c.getInt(0);
        } finally {
            if (c != null) c.close();
        }
        return 0;
    }

    // 某个 type 的总数
    public int getCountByType(int type) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + AppDatabaseHelper.TABLE_MESSAGE + " WHERE type = ?",
                new String[]{String.valueOf(type)}
        );
        try {
            if (c.moveToFirst()) return c.getInt(0);
        } finally {
            if (c != null) c.close();
        }
        return 0;
    }

    // 某个 type 的已读数
    public int getReadCountByType(int type) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + AppDatabaseHelper.TABLE_MESSAGE +
                        " WHERE type = ? AND unread = 0",
                new String[]{String.valueOf(type)}
        );
        try {
            if (c.moveToFirst()) return c.getInt(0);
        } finally {
            if (c != null) c.close();
        }
        return 0;
    }

}
