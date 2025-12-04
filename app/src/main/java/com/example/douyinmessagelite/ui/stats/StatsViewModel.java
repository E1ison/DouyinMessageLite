package com.example.douyinmessagelite.ui.stats;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.douyinmessagelite.data.model.StatsData;
import com.example.douyinmessagelite.data.model.Message;
import com.example.douyinmessagelite.data.repository.MessageRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatsViewModel extends AndroidViewModel {

    private final MessageRepository repository;
    private final MutableLiveData<StatsData> statsLiveData = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public StatsViewModel(@NonNull Application application) {
        super(application);
        repository = new MessageRepository(application);
    }

    public LiveData<StatsData> getStatsLiveData() {
        return statsLiveData;
    }

    public void loadStats() {
        executor.execute(() -> {
            StatsData s = new StatsData();
            s.totalCount = repository.getTotalCount();
            s.unreadCount = repository.getUnreadCount();

            s.systemTotal = repository.getCountByType(Message.TYPE_SYSTEM_TEXT);
            s.systemRead = repository.getReadCountByType(Message.TYPE_SYSTEM_TEXT);

            s.friendTotal = repository.getCountByType(Message.TYPE_FRIEND_IMAGE);
            s.friendRead = repository.getReadCountByType(Message.TYPE_FRIEND_IMAGE);

            s.opTotal = repository.getCountByType(Message.TYPE_OPERATION);
            s.opRead = repository.getReadCountByType(Message.TYPE_OPERATION);

            statsLiveData.postValue(s);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}
