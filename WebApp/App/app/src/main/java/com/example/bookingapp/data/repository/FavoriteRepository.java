package com.example.bookingapp.data.repository;

import androidx.lifecycle.LiveData;

import com.example.bookingapp.data.local.FavoriteDao;
import com.example.bookingapp.data.local.FavoriteProperty;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteRepository {
    private final FavoriteDao dao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public FavoriteRepository(FavoriteDao dao) {
        this.dao = dao;
    }

    public LiveData<List<FavoriteProperty>> observeAll() { return dao.observeAll(); }
    public LiveData<List<Long>> observeAllIds() { return dao.observeAllIds(); }

    /** Toggle off-main-thread. Trả về isAdded callback nếu cần phân biệt cho UI. */
    public void toggle(FavoriteProperty fav, OnToggleResult cb) {
        io.execute(() -> {
            boolean existed = dao.exists(fav.propertyId);
            if (existed) {
                dao.deleteById(fav.propertyId);
            } else {
                dao.insert(fav);
            }
            if (cb != null) cb.onResult(!existed);
        });
    }

    public interface OnToggleResult {
        void onResult(boolean isAdded);
    }
}
