package com.example.bookingapp.presentation.features.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingapp.R;
import com.example.bookingapp.core.utils.Formatter;
import com.example.bookingapp.data.model.views.PropertySearchResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.VH> {

    public interface OnClick { void onClick(PropertySearchResponse item); }
    public interface OnFavoriteToggle { void onToggle(PropertySearchResponse item, boolean wasFavorite); }

    private final List<PropertySearchResponse> items;
    private final OnClick onClick;
    private OnFavoriteToggle favoriteToggle;
    private Set<Long> favoriteIds = new HashSet<>();

    public SearchResultAdapter(List<PropertySearchResponse> items, OnClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    public void setOnFavoriteToggle(OnFavoriteToggle cb) { this.favoriteToggle = cb; }

    public void submitFavoriteIds(Set<Long> ids) {
        this.favoriteIds = ids != null ? ids : new HashSet<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PropertySearchResponse it = items.get(position);
        h.tvName.setText(it.getPropertyName());
        h.tvAddress.setText((it.getCity() == null ? "" : it.getCity() + ", ") + it.getAddress());
        int n = it.getAvailableRooms() == null ? 0 : it.getAvailableRooms().size();
        h.tvRoomCount.setText("Còn " + n + " loại phòng");
        h.tvPrice.setText("Từ " + Formatter.currency(it.getMinPrice()) + "/đêm");

        h.ivThumbnail.setImageResource(R.drawable.loadingscreen);

        boolean isFav = it.getPropertyId() != null && favoriteIds.contains(it.getPropertyId());
        h.ivFavorite.setImageResource(isFav
                ? R.drawable.ic_favorite
                : R.drawable.ic_favorite_border);

        h.ivFavorite.setOnClickListener(v -> {
            boolean nowFav = !(it.getPropertyId() != null && favoriteIds.contains(it.getPropertyId()));
            h.ivFavorite.setImageResource(nowFav
                    ? R.drawable.ic_favorite
                    : R.drawable.ic_favorite_border);
            if (favoriteToggle != null) favoriteToggle.onToggle(it, !nowFav);
        });

        h.itemView.setOnClickListener(v -> onClick.onClick(it));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivThumbnail, ivFavorite;
        TextView tvName, tvAddress, tvRoomCount, tvPrice;
        VH(@NonNull View v) {
            super(v);
            ivThumbnail = v.findViewById(R.id.ivThumbnail);
            ivFavorite = v.findViewById(R.id.ivFavorite);
            tvName = v.findViewById(R.id.tvName);
            tvAddress = v.findViewById(R.id.tvAddress);
            tvRoomCount = v.findViewById(R.id.tvRoomCount);
            tvPrice = v.findViewById(R.id.tvPrice);
        }
    }
}
