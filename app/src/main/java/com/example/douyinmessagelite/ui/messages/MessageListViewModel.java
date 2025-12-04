package com.example.douyinmessagelite.ui.messages;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.douyinmessagelite.data.model.Message;
import com.example.douyinmessagelite.data.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageListViewModel extends AndroidViewModel {

    private final MessageRepository repository;
    private final MutableLiveData<List<Message>> messagesLiveData =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<LoadStatus> loadStatusLiveData =
            new MutableLiveData<>(LoadStatus.IDLE);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private int currentPage = 0;

    public MessageListViewModel(@NonNull Application application) {
        super(application);
        repository = new MessageRepository(application);
    }

    public LiveData<List<Message>> getMessagesLiveData() {
        return messagesLiveData;
    }

    public LiveData<LoadStatus> getLoadStatusLiveData() {
        return loadStatusLiveData;
    }

    // 首次进入：初始化 + 刷新第一页
    public void init() {
        executor.execute(() -> {
            repository.initIfNeeded();
            refresh();
        });
    }

    public void refresh() {
        currentPage = 0;
        simulateNetwork(() -> {
            List<Message> page = repository.getMessages(0);
            messagesLiveData.postValue(page);
        });
    }

    public void loadMore() {
        simulateNetwork(() -> {
            int next = currentPage + 1;
            List<Message> page = repository.getMessages(next);
            if (page != null && !page.isEmpty()) {
                List<Message> cur = messagesLiveData.getValue();
                List<Message> merged = new ArrayList<>();
                if (cur != null) {
                    merged.addAll(cur);
                }
                merged.addAll(page);
                messagesLiveData.postValue(merged);
                currentPage = next;
            }
        });
    }

    // 模拟弱网 / 超时
    private interface Task {
        void run();
    }

    private void simulateNetwork(Task task) {
        loadStatusLiveData.postValue(LoadStatus.LOADING);
        handler.postDelayed(() -> {
            int r = random.nextInt(10); // 0..9
            if (r == 0) {
                loadStatusLiveData.postValue(LoadStatus.ERROR_TIMEOUT);
            } else if (r <= 2) {
                loadStatusLiveData.postValue(LoadStatus.ERROR_NETWORK);
            } else {
                executor.execute(() -> {
                    task.run();
                    loadStatusLiveData.postValue(LoadStatus.SUCCESS);
                });
            }
        }, 2000); // 2 秒延迟模拟网络
    }

    public void markAsRead(long id) {
        executor.execute(() -> {
            repository.updateUnread(id, false);
            List<Message> list = messagesLiveData.getValue();
            if (list != null) {
                for (Message m : list) {
                    if (m.getId() == id) {
                        m.setUnread(false);
                        break;
                    }
                }
                // 拷贝一份再 post，避免直接改原引用
                messagesLiveData.postValue(new ArrayList<>(list));
            }
        });
    }

    public void updateRemark(long id, String remark) {
        executor.execute(() -> {
            repository.updateRemark(id, remark);
            List<Message> list = messagesLiveData.getValue();
            if (list != null) {
                for (Message m : list) {
                    if (m.getId() == id) {
                        m.setRemark(remark);
                        break;
                    }
                }
                messagesLiveData.postValue(new ArrayList<>(list));
            }
        });
    }

    public void addNewMessage(Message newMsg) {
        executor.execute(() -> {
            List<Message> currentList = messagesLiveData.getValue();
            if (currentList != null && newMsg.getNickname() != null) {
                for (Message m : currentList) {
                    if (newMsg.getNickname().equals(m.getNickname())) {
                        // 没有新的头像就沿用旧头像
                        if ((newMsg.getAvatarUrl() == null || newMsg.getAvatarUrl().isEmpty())
                                && m.getAvatarUrl() != null) {
                            newMsg.setAvatarUrl(m.getAvatarUrl());
                        }
                        // 保留旧的备注和置顶状态
                        newMsg.setRemark(m.getRemark());
                        newMsg.setPinned(m.isPinned());
                        break;
                    }
                }
            }

            if (newMsg.getId() == 0L) {
                newMsg.setId(System.currentTimeMillis());
            }
            if (newMsg.getTimestamp() == 0L) {
                newMsg.setTimestamp(System.currentTimeMillis());
            }
            newMsg.setUnread(true);

            repository.insertMessage(newMsg);

            currentPage = 0;
            List<Message> firstPage = repository.getMessages(0);
            messagesLiveData.postValue(firstPage);
        });
    }

    public void search(String keyword) {
        final String k = keyword == null ? "" : keyword.trim();
        if (k.isEmpty()) {
            refresh();
            return;
        }
        loadStatusLiveData.postValue(LoadStatus.LOADING);
        executor.execute(() -> {
            List<Message> list = repository.searchMessages(k);
            messagesLiveData.postValue(list);
            loadStatusLiveData.postValue(LoadStatus.SUCCESS);
        });
    }
}
