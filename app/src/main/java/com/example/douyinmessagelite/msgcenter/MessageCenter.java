package com.example.douyinmessagelite.msgcenter;

import android.os.Handler;
import android.os.Looper;

import com.example.douyinmessagelite.data.model.Message;

import java.util.Random;

public class MessageCenter {

    // 回调接口
    public interface OnNewMessageListener {
        void onNewMessage(Message msg);
    }

    private static MessageCenter instance; // 单例模式：全局只有一个 MessageCenter

    private final Handler handler = new Handler(Looper.getMainLooper()); // 这是一个绑定到主线程（UI 线程）消息队列上的 Handler。
    private final Random random = new Random();
    private boolean running = false;
    private OnNewMessageListener listener;

    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            Message msg = generateRandomMessage();
            if (listener != null && msg != null) {
                listener.onNewMessage(msg);
            }

            // 5 秒后再次执行
            handler.postDelayed(this, 5000);
        }
    };

    private MessageCenter() {
    }

    public static synchronized MessageCenter getInstance() {
        if (instance == null) {
            instance = new MessageCenter();
        }
        return instance;
    }

    public void start(OnNewMessageListener listener) {
        this.listener = listener;
        if (running) return;
        running = true;
        handler.postDelayed(task, 5000); // 5 秒后开始第一次推送
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(task);
        listener = null;
    }

    // 模拟新消息
    private Message generateRandomMessage() {
        long id = System.currentTimeMillis(); // 简单起见，用时间戳当 id

        int type = random.nextInt(3); // 0,1,2
        Message msg = new Message();
        msg.setId(id);
        msg.setTimestamp(System.currentTimeMillis());
        msg.setUnread(true);
        msg.setPinned(false);
        msg.setRemark("");

        switch (type) {
            case Message.TYPE_SYSTEM_TEXT:
                msg.setType(Message.TYPE_SYSTEM_TEXT);
                msg.setNickname("系统通知");
                msg.setContent("你有一条新的系统提醒（模拟）");
                msg.setAvatarUrl("");
                msg.setImageUrl("");
                msg.setActionText("");
                break;

            case Message.TYPE_FRIEND_IMAGE:
                msg.setType(Message.TYPE_FRIEND_IMAGE);
                msg.setNickname(randomFriendName());
                msg.setContent("给你发来了一张新照片（模拟）");
                msg.setAvatarUrl("");
                msg.setImageUrl("local_mock_image");
                msg.setActionText("");
                break;

            case Message.TYPE_OPERATION:
            default:
                msg.setType(Message.TYPE_OPERATION);
                msg.setNickname("活动中心");
                msg.setContent("限时活动：完成任务即可领取奖励（模拟）");
                msg.setAvatarUrl("");
                msg.setImageUrl("");
                msg.setActionText(randomActionText());
                break;
        }

        return msg;
    }

    private String randomFriendName() {
        String[] names = {"好友 A", "好友 B", "小明", "小红", "老朋友"};
        return names[random.nextInt(names.length)];
    }

    private String randomActionText() {
        String[] actions = {"立即参与", "去领取", "查看详情", "马上抢"};
        return actions[random.nextInt(actions.length)];
    }
}
