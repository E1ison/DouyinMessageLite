package com.example.douyinmessagelite.ui.stats;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.douyinmessagelite.R;
import com.example.douyinmessagelite.data.model.StatsData;

public class StatsActivity extends AppCompatActivity {

    private TextView tvTotal, tvUnread, tvSystem, tvFriend, tvOp;
    private StatsViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        tvTotal = findViewById(R.id.tvTotal);
        tvUnread = findViewById(R.id.tvUnread);
        tvSystem = findViewById(R.id.tvSystem);
        tvFriend = findViewById(R.id.tvFriend);
        tvOp = findViewById(R.id.tvOp);

        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);
        viewModel.getStatsLiveData().observe(this, stats -> {
            if (stats == null) return;
            bindStats(stats);
        });

        viewModel.loadStats();
    }

    private void bindStats(StatsData s) {
        tvTotal.setText("总消息数：" + s.totalCount);
        tvUnread.setText("未读消息数：" + s.unreadCount);

        tvSystem.setText("系统消息：已读 " + s.systemRead + " / 总 " + s.systemTotal);
        tvFriend.setText("好友图片消息：已读 " + s.friendRead + " / 总 " + s.friendTotal);
        tvOp.setText("运营消息：已读 " + s.opRead + " / 总 " + s.opTotal);
    }
}
