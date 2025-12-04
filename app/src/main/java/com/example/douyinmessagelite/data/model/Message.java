package com.example.douyinmessagelite.data.model;

public class Message {

    public static final int TYPE_SYSTEM_TEXT = 0;   // 纯文本系统消息
    public static final int TYPE_FRIEND_IMAGE = 1;  // 好友发图
    public static final int TYPE_OPERATION = 2;     // 运营类消息

    private long id;
    private String avatarUrl;
    private String nickname;
    private String content;
    private long timestamp;     // 毫秒时间戳
    private int type;           // 消息体裁
    private boolean unread;     // 未读状态
    private String imageUrl;    // 好友发图用

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    private String actionText;  // 运营按钮文案，如“领取奖励”
    private String remark;      // 本地备注
    private boolean pinned;     // isPinned（为后面迁移预留）

    public Message() {}

}
