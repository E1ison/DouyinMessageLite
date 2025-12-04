package com.example.douyinmessagelite.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {

    private static final String PREF_NAME = "douyin_message_prefs";
    private static final String KEY_INIT = "has_init_from_json";

    private final SharedPreferences sp;

    public PrefsManager(Context context) {
        sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean hasInitFromJson() {
        return sp.getBoolean(KEY_INIT, false);
    }

    public void setInitFromJson(boolean value) {
        sp.edit().putBoolean(KEY_INIT, value).apply();
    }

    private static final String KEY_CLOUD_SYNC = "cloud_sync_enabled";

    // 默认开着
    public boolean isCloudSyncEnabled() {
        return sp.getBoolean(KEY_CLOUD_SYNC, true);
    }

    public void setCloudSyncEnabled(boolean enabled) {
        sp.edit().putBoolean(KEY_CLOUD_SYNC, enabled).apply();
    }

}
