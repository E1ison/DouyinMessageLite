package com.example.douyinmessagelite.ui.messages;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.douyinmessagelite.R;
import com.example.douyinmessagelite.data.model.Message;
import com.example.douyinmessagelite.data.prefs.PrefsManager;
import com.example.douyinmessagelite.msgcenter.MessageCenter;
import com.example.douyinmessagelite.ui.remark.RemarkActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;


public class MessageListActivity extends AppCompatActivity {
    private static final int REQ_POST_NOTI = 1002;
    private MessageListViewModel viewModel;
    private MessageAdapter adapter;
    private LinearLayout emptyView;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;

    private static final int REQ_REMARK = 1001;

    private MessageCenter messageCenter;
    private RecyclerView recyclerView;
    private android.widget.TextView tvUnreadBanner;

    private android.widget.EditText etSearch;
    private android.view.View loadingSkeleton;
    private android.widget.TextView emptyText;
    private android.widget.Button btnRetry;

    private android.widget.ImageButton btnStats;
    private android.widget.ImageButton btnSettings;
    private PrefsManager prefsManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefsManager = new PrefsManager(this);
        requestNotificationPermissionIfNeeded();
        startBackgroundMessageServiceIfNeeded();
        btnStats = findViewById(R.id.btnStats);
        btnSettings = findViewById(R.id.btnSettings);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        tvUnreadBanner = findViewById(R.id.tvUnreadBanner);
        etSearch = findViewById(R.id.etSearch);
        loadingSkeleton = findViewById(R.id.loadingSkeleton);
        emptyText = findViewById(R.id.emptyText);
        btnRetry = findViewById(R.id.btnRetry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MessageListViewModel.class);

        btnRetry.setOnClickListener(v -> {
            viewModel.refresh();
        });

        btnStats.setOnClickListener(v -> {
            startActivity(new android.content.Intent(
                    MessageListActivity.this,
                    com.example.douyinmessagelite.ui.stats.StatsActivity.class
            ));
        });

        btnSettings.setOnClickListener(v -> {
            startActivity(new android.content.Intent(
                    MessageListActivity.this,
                    com.example.douyinmessagelite.ui.settings.SettingsActivity.class
            ));
        });



        // 观察数据变化：刷新列表 & 空态 & 未读条
        viewModel.getMessagesLiveData().observe(this, messages -> {
            adapter.setData(messages);
            if (messages == null || messages.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
            }
            swipeRefresh.setRefreshing(false);
            updateUnreadBanner(messages);
        });

        viewModel.getLoadStatusLiveData().observe(this, status -> {
            if (status == null) return;

            switch (status) {
                case LOADING:
                    // 正在加载：如果当前列表是空的，就显示 skeleton；
                    if (adapter.getItemCount() == 0) {
                        loadingSkeleton.setVisibility(View.VISIBLE);
                    }
                    emptyView.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(true);
                    break;

                case SUCCESS:
                    // 成功：隐藏 skeleton、停止刷新
                    loadingSkeleton.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    // 如果成功但没有数据，并且不在搜索，显示“暂无消息”
                    if (adapter.getItemCount() == 0) {
                        emptyView.setVisibility(View.VISIBLE);
                        emptyText.setText("暂无消息");
                        btnRetry.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                    }
                    break;

                case ERROR_NETWORK:
                case ERROR_TIMEOUT:
                    // 失败：隐藏 skeleton、停止刷新；若目前没有数据，显示错误空态 + 重试
                    loadingSkeleton.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    if (adapter.getItemCount() == 0) {
                        emptyView.setVisibility(View.VISIBLE);
                        emptyText.setText(
                                status == LoadStatus.ERROR_TIMEOUT
                                        ? "请求超时，请重试"
                                        : "加载失败，请检查网络后重试"
                        );
                        btnRetry.setVisibility(View.VISIBLE);
                    }
                    break;

                case IDLE:
                default:
                    loadingSkeleton.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    break;
            }
        });



        swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;
                int lastVisible = lm.findLastVisibleItemPosition();
                int total = adapter.getItemCount();
                if (dy > 0 && lastVisible >= total - 2) {
                    viewModel.loadMore();
                }
            }
        });

        adapter.setOnItemClickListener((cardView, message) -> {
            // 点击：标记已读 + 打开备注页
            viewModel.markAsRead(message.getId());
            Intent intent = new Intent(MessageListActivity.this, RemarkActivity.class);
            intent.putExtra(RemarkActivity.EXTRA_MESSAGE_ID, message.getId());
            intent.putExtra(RemarkActivity.EXTRA_NICKNAME, message.getNickname());

            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            MessageListActivity.this,
                            cardView,
                            "message_card"   // 要和 item_message_text.xml 里的 transitionName 一致
                    );

            ActivityCompat.startActivityForResult(
                    MessageListActivity.this,
                    intent,
                    REQ_REMARK,
                    options.toBundle()
            );
        });


        // 消息中心单例
        messageCenter = MessageCenter.getInstance();

        swipeRefresh.setRefreshing(true);
        viewModel.init();

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString();
                viewModel.search(q);
                adapter.setHighlightKeyword(q);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


    }

    private void updateUnreadBanner(java.util.List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            tvUnreadBanner.setVisibility(View.GONE);
            return;
        }
        int count = 0;
        for (Message m : messages) {
            if (m.isUnread()) {
                count++;
            }
        }
        if (count > 0) {
            tvUnreadBanner.setVisibility(View.VISIBLE);
            tvUnreadBanner.setText("未读消息：" + count);
        } else {
            tvUnreadBanner.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (prefsManager.isCloudSyncEnabled()) {
            // 开启云同步：启动本地消息中心
            messageCenter.start(msg -> {
                // 收到新消息后：写库 + 列表头插入 + 滚到顶部
                viewModel.addNewMessage(msg);
                recyclerView.smoothScrollToPosition(0);
            });
        } else {
            // 关闭云同步：不再自动插入新消息
            messageCenter.stop();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 页面不可见时停止推送，避免泄漏
        messageCenter.stop();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_REMARK && resultCode == RESULT_OK && data != null) {
            long id = data.getLongExtra(RemarkActivity.EXTRA_MESSAGE_ID, -1);
            String remark = data.getStringExtra(RemarkActivity.EXTRA_REMARK);
            if (id != -1) {
                viewModel.updateRemark(id, remark);
            }
        }
    }

    private void startBackgroundMessageServiceIfNeeded() {
        if (prefsManager != null && prefsManager.isCloudSyncEnabled()) {
            android.content.Intent sIntent =
                    new android.content.Intent(this,
                            com.example.douyinmessagelite.msgcenter.MessageForegroundService.class);
            androidx.core.content.ContextCompat.startForegroundService(this, sIntent);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                androidx.core.app.ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTI
                );
            }
        }
    }


    private void stopBackgroundMessageService() {
        android.content.Intent sIntent =
                new android.content.Intent(this,
                        com.example.douyinmessagelite.msgcenter.MessageForegroundService.class);
        stopService(sIntent);
    }

}
