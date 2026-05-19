package com.example.bookingapp.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteDao {
    @Query("SELECT * FROM favorite_property ORDER BY savedAt DESC")
    LiveData<List<FavoriteProperty>> observeAll();

    @Query("SELECT propertyId FROM favorite_property")
    LiveData<List<Long>> observeAllIds();

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_property WHERE propertyId = :id)")
    boolean exists(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteProperty fav);

    @Query("DELETE FROM favorite_property WHERE propertyId = :id")
    void deleteById(long id);
}
