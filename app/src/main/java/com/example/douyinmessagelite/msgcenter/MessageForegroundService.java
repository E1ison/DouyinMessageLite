package com.example.douyinmessagelite.msgcenter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.douyinmessagelite.R;
import com.example.douyinmessagelite.data.model.Message;
import com.example.douyinmessagelite.data.repository.MessageRepository;
import com.example.douyinmessagelite.ui.messages.MessageListActivity;

import java.util.Random;

public class MessageForegroundService extends Service {

    private static final String CHANNEL_ID_SERVICE = "msg_service_channel"; //服务本身的常驻通知
    private static final String CHANNEL_ID_NEW_MSG = "msg_new_message_channel"; //新消息提醒

    private Handler handler;
    private Runnable task;
    private boolean running = false;
    private Random random;
    private MessageRepository repository;

    // Android Service，
    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        random = new Random();
        repository = new MessageRepository(getApplicationContext());

        createNotificationChannels();

        // 前台常驻通知
        Notification ongoing = new NotificationCompat.Builder(this, CHANNEL_ID_SERVICE)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("正在模拟接收新消息")
                .setOngoing(true)
                .build();

        startForeground(1, ongoing);

        task = new Runnable() {
            @Override
            public void run() {
                if (!running) return;

                Message msg = generateRandomMessage();

                repository.insertMessage(msg);

                showNewMessageNotification(msg);

                handler.postDelayed(this, 5000);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        handler.post(task);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        handler.removeCallbacks(task);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);

            // 前台服务
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID_SERVICE,
                    "消息中心服务",
                    NotificationManager.IMPORTANCE_MIN
            );
            serviceChannel.setSound(null, null);
            serviceChannel.enableVibration(false);
            nm.createNotificationChannel(serviceChannel);

            // 新消息提醒
            NotificationChannel msgChannel = new NotificationChannel(
                    CHANNEL_ID_NEW_MSG,
                    "新消息提醒",
                    NotificationManager.IMPORTANCE_LOW
            );
            msgChannel.setSound(null, null);
            msgChannel.enableVibration(false);
            nm.createNotificationChannel(msgChannel);
        }
    }


    private void showNewMessageNotification(Message msg) {
        //先检查通知权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        // 点击通知回到消息列表页
        Intent intent = new Intent(this, MessageListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = msg.getNickname();
        String content = msg.getContent();
        if (content == null || content.isEmpty()) {
            content = "发来了一条新消息";
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_NEW_MSG)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        int id = (int) (System.currentTimeMillis() & 0xFFFFFFF);
        NotificationManagerCompat.from(this).notify(id, notification);
    }

    private Message generateRandomMessage() {
        long id = System.currentTimeMillis();

        int type = random.nextInt(3);
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
                msg.setContent("你有一条新的系统提醒（后台推送）");
                msg.setAvatarUrl("");
                msg.setImageUrl("");
                msg.setActionText("");
                break;

            case Message.TYPE_FRIEND_IMAGE:
                msg.setType(Message.TYPE_FRIEND_IMAGE);
                msg.setNickname(randomFriendName());
                msg.setContent("给你发来了一张新照片（后台推送）");
                msg.setAvatarUrl("");
                msg.setImageUrl("local_mock_image");
                msg.setActionText("");
                break;

            case Message.TYPE_OPERATION:
            default:
                msg.setType(Message.TYPE_OPERATION);
                msg.setNickname("活动中心");
                msg.setContent("限时活动：完成任务即可领取奖励（后台推送）");
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
