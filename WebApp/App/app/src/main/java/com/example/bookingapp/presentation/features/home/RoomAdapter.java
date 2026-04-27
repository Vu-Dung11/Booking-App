package com.example.bookingapp.presentation.features.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingapp.data.model.views.RoomResponse;
import com.example.bookingapp.databinding.ItemRoomBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(RoomResponse room);
    }

    private final List<RoomResponse> rooms;
    private final OnRoomClickListener listener;

    public RoomAdapter(List<RoomResponse> rooms, OnRoomClickListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemRoomBinding binding;

        public ViewHolder(ItemRoomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRoomBinding binding = ItemRoomBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomResponse room = rooms.get(position);

        holder.binding.tvRoomType.setText(room.getRoomType());
        holder.binding.tvRoomCapacity.setText(room.getCapacity() + " khách");

        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        holder.binding.tvRoomPrice.setText(formatter.format(room.getPrice()) + "đ");

        holder.itemView.setOnClickListener(v -> listener.onRoomClick(room));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }
}
