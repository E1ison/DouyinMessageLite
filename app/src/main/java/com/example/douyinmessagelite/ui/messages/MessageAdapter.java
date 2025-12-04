package com.example.douyinmessagelite.ui.messages;

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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private void applyHighlight(TextView tv, String text) {
        if (text == null) {
            tv.setText("");
            return;
        }
        if (highlightKeyword == null || highlightKeyword.isEmpty()) {
            tv.setText(text);
            return;
        }
        String lower = text.toLowerCase();
        int start = lower.indexOf(highlightKeyword);
        if (start < 0) {
            tv.setText(text);
            return;
        }
        android.text.SpannableString ss = new android.text.SpannableString(text);
        ss.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#FF6600")),
                start, start + highlightKeyword.length(),
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                start, start + highlightKeyword.length(),
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ss);
    }

    public interface OnItemClickListener {
        void onItemClick(View cardView,Message message);
    }

    private final List<Message> data = new ArrayList<>();
    private OnItemClickListener listener;

    private String highlightKeyword = "";

    public void setData(List<Message> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void setHighlightKeyword(String keyword) {
        if (keyword == null) {
            this.highlightKeyword = "";
        } else {
            this.highlightKeyword = keyword.toLowerCase();
        }
        notifyDataSetChanged();
    }
    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_text, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = data.get(position);
        holder.bind(msg);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(holder.cardRoot,msg);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        View cardRoot;
        TextView tvNickname, tvTime, tvContent, tvRemark;
        View viewUnreadDot;
        ImageView ivAvatar;
        ImageView ivImage;
        Button btnAction;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.cardRoot);
            tvNickname = itemView.findViewById(R.id.tvNickname);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvRemark = itemView.findViewById(R.id.tvRemark);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            ivImage = itemView.findViewById(R.id.ivImage);
            btnAction = itemView.findViewById(R.id.btnAction);
        }

        void bind(Message msg) {
            tvTime.setText(TimeUtils.formatTime(msg.getTimestamp()));
            applyHighlight(tvNickname, msg.getNickname());
            applyHighlight(tvContent, msg.getContent());

            if (msg.isUnread()) {
                viewUnreadDot.setVisibility(View.VISIBLE);
            } else {
                viewUnreadDot.setVisibility(View.GONE);
            }

            if (msg.getRemark() != null && !msg.getRemark().isEmpty()) {
                tvRemark.setVisibility(View.VISIBLE);
                tvRemark.setText("备注：" + msg.getRemark());
            } else {
                tvRemark.setVisibility(View.GONE);
            }
        }
    }
}
