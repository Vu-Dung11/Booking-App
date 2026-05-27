package com.example.bookingapp.presentation.features.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookingapp.R;
import com.example.bookingapp.data.model.views.Homestay;
import com.example.bookingapp.databinding.ItemHomestayBinding;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomestayAdapter extends RecyclerView.Adapter<HomestayAdapter.ViewHolder> {

    public interface OnHomestayClickListener {
        void onHomestayClick(Homestay homestay);
    }

    public interface OnFavoriteToggle {
        void onToggle(Homestay homestay, boolean wasFavorite);
    }

    private final List<Homestay> homestays;
    private final OnHomestayClickListener listener;
    private OnFavoriteToggle favoriteToggle;
    private Set<Long> favoriteIds = new HashSet<>();

    public HomestayAdapter(List<Homestay> homestays, OnHomestayClickListener listener) {
        this.homestays = homestays;
        this.listener = listener;
    }

    public void setOnFavoriteToggle(OnFavoriteToggle cb) { this.favoriteToggle = cb; }

    public void submitFavoriteIds(Set<Long> ids) {
        this.favoriteIds = ids != null ? ids : new HashSet<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemHomestayBinding binding;

        public ViewHolder(ItemHomestayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomestayBinding binding = ItemHomestayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Homestay homestay = homestays.get(position);

        holder.binding.tvHomestayName.setText(homestay.getName());
        holder.binding.tvHomestayLocation.setText(homestay.getLocation());

        Double rating = homestay.getRating();
        if (rating != null && rating > 0) {
            holder.binding.tvRating.setText(String.format(Locale.US, "%.1f", rating));
            holder.binding.tvRating.setBackgroundResource(R.drawable.bg_rating);
            holder.binding.tvRating.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        } else {
            holder.binding.tvRating.setText(" ");
//            holder.binding.tvRating.setBackgroundResource(0);
//            holder.binding.tvRating.setTextColor(
//                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_hint));
        }

        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        Double price = homestay.getPrice();
        holder.binding.tvPrice.setText(
                (price != null && price > 0)
                        ? formatter.format(price) + "đ"
                        : "Liên hệ");

        Glide.with(holder.itemView)
                .load(homestay.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.binding.ivHomestay);

        boolean isFavorite = homestay.getPropertyId() != null && favoriteIds.contains(homestay.getPropertyId());
        updateFavoriteIcon(holder, isFavorite);

        holder.binding.ivFavorite.setOnClickListener(v -> {
            boolean nowFavorite = !(homestay.getPropertyId() != null && favoriteIds.contains(homestay.getPropertyId()));
            updateFavoriteIcon(holder, nowFavorite);
            if (favoriteToggle != null) favoriteToggle.onToggle(homestay, !nowFavorite);
        });

        holder.itemView.setOnClickListener(v -> listener.onHomestayClick(homestay));
    }

    private void updateFavoriteIcon(ViewHolder holder, boolean isFavorite) {
        if (isFavorite) {
            holder.binding.ivFavorite.setImageResource(R.drawable.ic_favorite);
        } else {
            holder.binding.ivFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
        holder.binding.ivFavorite.setColorFilter(null);
    }

    @Override
    public int getItemCount() { return homestays.size(); }
}
