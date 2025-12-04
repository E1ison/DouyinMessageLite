package com.example.douyinmessagelite.ui.remark;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.douyinmessagelite.R;
import com.example.douyinmessagelite.data.model.Message;
import com.example.douyinmessagelite.data.repository.MessageRepository;
import com.example.douyinmessagelite.utils.TimeUtils;
import com.google.android.material.button.MaterialButton;

import android.view.View;
import android.view.MotionEvent; // 如果 IDE 没自动补就写上


import java.util.ArrayList;
import java.util.List;

public class RemarkActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE_ID = "extra_message_id";
    public static final String EXTRA_NICKNAME = "extra_nickname";
    public static final String EXTRA_REMARK = "extra_remark";

    public static final String EXTRA_CONTENT = "extra_content";
    public static final String EXTRA_TIMESTAMP = "extra_timestamp";

    private long messageId;
    private String nickname;
    private MessageRepository repository;
    private EditText etRemark;
    private HistoryAdapter historyAdapter;

    private View rootView;
    private float downY;
    private boolean dragging = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remark);
        rootView = findViewById(R.id.rootRemark);
        setupSwipeToDismiss();

        TextView tvNickname = findViewById(R.id.tvNickname);
        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        etRemark = findViewById(R.id.etRemark);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);

        repository = new MessageRepository(this);

        messageId = getIntent().getLongExtra(EXTRA_MESSAGE_ID, -1);
        nickname = getIntent().getStringExtra(EXTRA_NICKNAME);
        tvNickname.setText(nickname);

        Message current = repository.getMessageById(messageId);
        if (current != null && current.getRemark() != null) {
            etRemark.setText(current.getRemark());
        }

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);          // 默认从底部（最新）开始看
        rvHistory.setLayoutManager(lm);

        historyAdapter = new HistoryAdapter();
        rvHistory.setAdapter(historyAdapter);

        List<Message> history = repository.getMessagesByNickname(nickname);
        historyAdapter.setData(history);

        if (history != null && !history.isEmpty()) {
            rvHistory.post(() -> rvHistory.scrollToPosition(history.size() - 1));
        }

        btnSave.setOnClickListener(v -> {
            String remark = etRemark.getText().toString();
            repository.updateRemark(messageId, remark);

            Intent data = new Intent();
            data.putExtra(EXTRA_MESSAGE_ID, messageId);
            data.putExtra(EXTRA_REMARK, remark);
            setResult(RESULT_OK, data);
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.activity_slide_out_down);
    }

    private void setupSwipeToDismiss() {
        rootView.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    downY = event.getRawY();
                    dragging = false;
                    return true;
                case android.view.MotionEvent.ACTION_MOVE:
                    float dy = event.getRawY() - downY;
                    if (dy > 0) {
                        dragging = true;
                        rootView.setTranslationY(dy);
                        float alpha = 1f - Math.min(1f, dy / rootView.getHeight());
                        rootView.setAlpha(alpha);
                    }
                    return true;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    if (!dragging) {
                        rootView.setTranslationY(0);
                        rootView.setAlpha(1f);
                        return false;
                    }
                    float totalDy = event.getRawY() - downY;
                    if (totalDy > rootView.getHeight() * 0.25f) {
                        finish();
                    } else {
                        rootView.animate()
                                .translationY(0)
                                .alpha(1f)
                                .setDuration(200)
                                .start();
                    }
                    return true;
            }
            return false;
        });
    }

}
