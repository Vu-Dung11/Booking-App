package com.example.bookingapp.presentation.features.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingapp.R;
import com.example.bookingapp.data.model.views.Homestay;
import com.example.bookingapp.databinding.ItemHomestayBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HomestayAdapter extends RecyclerView.Adapter<HomestayAdapter.ViewHolder> {

    public interface OnHomestayClickListener {
        void onHomestayClick(Homestay homestay);
    }

    private final List<Homestay> homestays;
    private final OnHomestayClickListener listener;

    public HomestayAdapter(List<Homestay> homestays, OnHomestayClickListener listener) {
        this.homestays = homestays;
        this.listener = listener;
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
        holder.binding.tvRating.setText("⭐ " + homestay.getRating());

        // Format giá tiền
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        holder.binding.tvPrice.setText(formatter.format(homestay.getPrice()) + "đ");

        // Trạng thái yêu thích
        updateFavoriteIcon(holder, homestay.isFavorite());

        // Click yêu thích
        holder.binding.ivFavorite.setOnClickListener(v -> {
            homestay.setFavorite(!homestay.isFavorite());
            updateFavoriteIcon(holder, homestay.isFavorite());
        });

        // Click vào card
        holder.itemView.setOnClickListener(v -> listener.onHomestayClick(homestay));
    }

    private void updateFavoriteIcon(ViewHolder holder, boolean isFavorite) {
        if (isFavorite) {
            holder.binding.ivFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            holder.binding.ivFavorite.setColorFilter(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.accent));
        } else {
            holder.binding.ivFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            holder.binding.ivFavorite.setColorFilter(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.accent));
        }
    }

    @Override
    public int getItemCount() {
        return homestays.size();
    }
}
