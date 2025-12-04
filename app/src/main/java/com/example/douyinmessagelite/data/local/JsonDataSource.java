package com.example.douyinmessagelite.data.local;

import android.content.Context;

import com.example.douyinmessagelite.data.model.Message;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
//从 assets 加载 mock 消息
public class JsonDataSource {

    private final Context context;

    public JsonDataSource(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<Message> loadMessagesFromAssets() {
        List<Message> result = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open("mock_messages.json");
            System.out.println(is);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            JSONArray array = new JSONArray(sb.toString());
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Message msg = new Message();
                msg.setId(obj.getLong("id"));
                msg.setAvatarUrl(obj.optString("avatarUrl", ""));
                msg.setNickname(obj.optString("nickname", ""));
                msg.setContent(obj.optString("content", ""));
//                msg.setTimestamp(obj.optLong("timestamp", System.currentTimeMillis()));
                long ts = obj.optLong("timestamp", System.currentTimeMillis());
                if (ts < 100000000000L) {
                    ts = ts * 1000L;
                }
                msg.setTimestamp(ts);
                msg.setType(obj.optInt("type", 0));
                msg.setUnread(obj.optBoolean("unread", true));
                msg.setImageUrl(obj.optString("imageUrl", ""));
                msg.setActionText(obj.optString("actionText", ""));
                msg.setRemark(obj.optString("remark", ""));
                msg.setPinned(false);
                result.add(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
