# DouyinMessageLite · 抖音风格消息列表 Demo

一个简化版的“抖音消息中心”客户端 Demo，包含消息会话列表、备注页、统计看板以及云同步模拟（前台/后台推送）。  
项目采用 **MVVM + Repository** 架构，数据持久化使用 **SQLite**，配置使用 **SharedPreferences**。
<img width="256" height="512" alt="Screenshot_20251204_183722" src="https://github.com/user-attachments/assets/034f3bda-9034-44da-a972-7331de678d83" />
<img width="256" height="512" alt="Screenshot_20251204_183745" src="https://github.com/user-attachments/assets/308e71d4-1f18-4135-b931-3df721070453" />
<img width="256" height="512" alt="Screenshot_20251204_184032" src="https://github.com/user-attachments/assets/3a436083-21ef-46d8-856c-d7180f18f17d" />
<img width="256" height="512" alt="Screenshot_20251204_194532" src="https://github.com/user-attachments/assets/17470877-15d3-442d-8922-9d711ff39901" />
<img width="256" height="512" alt="Screenshot_20251204_194543" src="https://github.com/user-attachments/assets/520a1ce0-609a-4284-9a41-2bfc44386b93" />

---

## 1. 功能概览

- 📥 **消息列表页（MessageListActivity）**
  - 会话列表展示（按昵称聚合 + 时间排序）
  - 未读红点提示 + 顶部“未读条数”横幅
  - 下拉刷新、上拉自动加载更多（分页）
  - 弱网 / 超时模拟 + Skeleton 占位 + 空态重试
  - 搜索昵称 / 消息内容，并对关键词进行高亮显示

- 📝 **备注页 / 会话详情（RemarkActivity）**
  - 展示某个会话的完整历史消息（按时间从早到晚）
  - 根据消息类型显示不同布局：系统文本、好友图片、运营消息按钮
  - 支持编辑本地备注并返回主列表同步更新
  - 自定义下滑退出动画 + 手势下滑关闭（仿底部弹出的卡片）

- 📊 **消息统计看板（StatsActivity）**
  - 统计总消息数、未读消息数
  - 统计不同类型消息（系统 / 好友图片 / 运营）的：
    - 总数
    - 已读数

- ⚙️ **设置页（SettingsActivity）**
  - 云同步开关（使用 `SwitchMaterial` 控制）
  - 打开：启动 `MessageForegroundService`，在后台持续模拟新消息推送
  - 关闭：停止前台服务，不再接收新消息

- 🔔 **消息中心 / 推送模拟（msgcenter）**
  - `MessageCenter`：在前台页面内使用 `Handler` 定时生成新消息，直接回调给列表 ViewModel
  - `MessageForegroundService`：前台 Service，每隔 5 秒写入一条新消息并通过通知栏提醒

---

## 2. 环境说明（Environment）
### 2.1 开发环境
| 环境项       | 要求                                                                 |
|--------------|----------------------------------------------------------------------|
| 操作系统     | Windows / macOS / Linux 均可                                         |
| IDE          | Android Studio（Giraffe / Hedgehog / Iguana 及以上版本）             |
| JDK          | JDK 8 或以上（推荐使用 AS 自带 JDK，无需单独安装）                   |
| Gradle       | 使用项目自带的 Gradle Wrapper（查看：gradle/wrapper/gradle-wrapper.properties） |

### 2.2 Android SDK 配置
以工程 `app/build.gradle` 配置为准，推荐版本：
- compileSdkVersion：33 或以上
- minSdkVersion：21 或以上
- targetSdkVersion：33 或以上

**首次打开项目缺失 SDK 处理**：
1. 打开 Android Studio → SDK Manager
2. 勾选对应版本的：
   - Android SDK Platform
   - Android SDK Build-Tools
3. 点击 Apply 等待安装完成

### 2.3 运行环境（设备）
#### 模拟器
- 推荐 Android 8.0+ 系统镜像（方便查看前台 Service 行为）

#### 真机
- 开启“开发者选项”和“USB 调试”
- 使用数据线连接电脑
- Android 13+ 设备需授予“通知权限”（项目已包含权限申请逻辑）

## 3. 运行指南（How to Run）
### 3.1 获取代码
#### 方法一：Git 克隆
```bash
git clone https://github.com/<your-username>/DouyinMessageLite.git
cd DouyinMessageLite

com.example.douyinmessagelite
├─ ui/                    # 界面层
│  ├─ messages/           # 消息列表页（Activity + ViewModel + Adapter）
│  ├─ remark/             # 备注页 / 会话历史（Activity + Adapter）
│  ├─ stats/              # 统计看板（Activity + ViewModel）
│  └─ settings/           # 设置页（云同步开关）
│
├─ data/                  # 数据层
│  ├─ model/              # 数据模型（Message、StatsData）
│  ├─ local/              # 本地 JSON 数据源（assets/mock_messages.json）
│  ├─ db/                 # SQLiteOpenHelper（AppDatabaseHelper）
│  ├─ prefs/              # SharedPreferences 封装（PrefsManager）
│  └─ repository/         # 统一数据入口（MessageRepository）
│
├─ msgcenter/             # 消息中心 / 推送模拟
│  ├─ MessageCenter               # 前台页面定时插入消息（单例）
│  └─ MessageForegroundService    # 后台前台服务 + 通知栏推送
│
└─ utils/
   └─ TimeUtils           # 消息时间文案格式化工具

