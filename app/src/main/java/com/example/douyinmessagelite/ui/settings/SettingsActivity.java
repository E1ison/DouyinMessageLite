package com.example.douyinmessagelite.ui.settings;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.douyinmessagelite.R;
import com.example.douyinmessagelite.data.prefs.PrefsManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private PrefsManager prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = new PrefsManager(this);

        SwitchMaterial swCloud = findViewById(R.id.swCloudSync);
        TextView tvStatus = findViewById(R.id.tvCloudStatus);

        boolean enabled = prefs.isCloudSyncEnabled();
        swCloud.setChecked(enabled);
        tvStatus.setText(enabled
                ? "当前：云同步已开启（会持续接收新消息）"
                : "当前：云同步已关闭，仅本地消息");

        swCloud.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setCloudSyncEnabled(isChecked);

            android.content.Intent sIntent =
                    new android.content.Intent(this,
                            com.example.douyinmessagelite.msgcenter.MessageForegroundService.class);

            if (isChecked) {
                // 打开云同步 => 开启后台消息服务
                androidx.core.content.ContextCompat.startForegroundService(this, sIntent);
            } else {
                // 关闭云同步 => 停止后台消息服务
                stopService(sIntent);
            }
        });

    }
}
