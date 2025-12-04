package com.example.douyinmessagelite.ui.remark;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.douyinmessagelite.R;
import com.example.douyinmessagelite.data.model.Message;
import com.example.douyinmessagelite.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_SYSTEM = 0;      // 纯文本（系统）
    private static final int VIEW_FRIEND = 1;      // 好友发图
    private static final int VIEW_OPERATION = 2;   // 运营按钮

    private final List<Message> data = new ArrayList<>();

    public void setData(List<Message> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Message m = data.get(position);
        if (m.getType() == Message.TYPE_FRIEND_IMAGE) {
            return VIEW_FRIEND;
        } else if (m.getType() == Message.TYPE_OPERATION) {
            return VIEW_OPERATION;
        } else {
            return VIEW_SYSTEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_FRIEND) {
            View v = inflater.inflate(R.layout.item_chat_friend_image, parent, false);
            return new FriendVH(v);
        } else if (viewType == VIEW_OPERATION) {
            View v = inflater.inflate(R.layout.item_chat_operation, parent, false);
            return new OperationVH(v);
        } else {
            View v = inflater.inflate(R.layout.item_chat_system, parent, false);
            return new SystemVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message m = data.get(position);
        int type = getItemViewType(position);
        if (type == VIEW_FRIEND) {
            ((FriendVH) holder).bind(m);
        } else if (type == VIEW_OPERATION) {
            ((OperationVH) holder).bind(m);
        } else {
            ((SystemVH) holder).bind(m);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // 1）系统纯文本
    static class SystemVH extends RecyclerView.ViewHolder {
        TextView tvTime, tvContent;
        SystemVH(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
        void bind(Message m) {
            tvTime.setText(TimeUtils.formatTime(m.getTimestamp()));
            tvContent.setText(m.getContent());
        }
    }

    // 2）好友发图
    static class FriendVH extends RecyclerView.ViewHolder {
        TextView tvTime, tvContent;
        ImageView ivImage;
        FriendVH(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvContent = itemView.findViewById(R.id.tvContent);
            ivImage = itemView.findViewById(R.id.ivImage);
        }
        void bind(Message m) {
            tvTime.setText(TimeUtils.formatTime(m.getTimestamp()));
            tvContent.setText(m.getContent());
            // 这里先用占位图，之后可以根据 imageUrl 用 Glide 加载真图
            ivImage.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    // 3）运营类消息
    static class OperationVH extends RecyclerView.ViewHolder {
        TextView tvTime, tvContent;
        Button btnAction;
        OperationVH(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvContent = itemView.findViewById(R.id.tvContent);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
        void bind(Message m) {
            tvTime.setText(TimeUtils.formatTime(m.getTimestamp()));
            tvContent.setText(m.getContent());

            String action = m.getActionText();
            if (action == null || action.isEmpty()) {
                action = "立即查看";
            }
            final String actionText = action;
            btnAction.setText(actionText);
            btnAction.setOnClickListener(v -> {
                android.widget.Toast.makeText(
                        itemView.getContext(),
                        "点击运营按钮：" + actionText,
                        android.widget.Toast.LENGTH_SHORT
                ).show();
            });
        }
    }
}
