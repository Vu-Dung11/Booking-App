package com.example.bookingapp.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_property")
public class FavoriteProperty {
    @PrimaryKey
    public long propertyId;

    @NonNull
    public String name = "";

    public String thumbnailUrl;

    public String city;

    public long savedAt;

    public FavoriteProperty() {}

    public FavoriteProperty(long propertyId, @NonNull String name, String thumbnailUrl, String city, long savedAt) {
        this.propertyId = propertyId;
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.city = city;
        this.savedAt = savedAt;
    }

    public long getPropertyId() { return propertyId; }
    public String getName() { return name; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getCity() { return city; }
    public long getSavedAt() { return savedAt; }
}
