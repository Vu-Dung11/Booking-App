package com.example.bookingapp.presentation.features.booking;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingapp.R;
import com.example.bookingapp.core.utils.Formatter;
import com.example.bookingapp.data.model.booking.Booking;

import java.util.ArrayList;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingVH> {

    public interface OnClick { void onClick(Booking booking); }

    private final List<Booking> items = new ArrayList<>();
    private final OnClick onClick;

    public BookingAdapter(OnClick onClick) { this.onClick = onClick; }

    public void submit(List<Booking> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingVH h, int position) {
        Booking b = items.get(position);
        String propertyName = b.getRoom() != null && b.getRoom().getProperty() != null
                ? b.getRoom().getProperty().getName() : "Homestay";
        String roomType = b.getRoom() != null ? b.getRoom().getRoomType() : "";

        h.tvPropertyName.setText(propertyName);
        h.tvRoomType.setText(roomType + " × " + (b.getRoomQuantity() != null ? b.getRoomQuantity() : 1));
        h.tvDates.setText(Formatter.displayDate(b.getCheckInDate()) + "  →  " + Formatter.displayDate(b.getCheckOutDate()));
        h.tvTotalPrice.setText(Formatter.currency(b.getTotalPrice()));

        applyStatus(h.tvStatus, b.getStatus());

        h.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(b);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static void applyStatus(TextView tv, String status) {
        int bgRes, txtRes;
        String label;
        if (status == null) status = "";
        switch (status) {
            case "CONFIRMED":
                bgRes = R.color.status_confirmed_bg; txtRes = R.color.status_confirmed_text; label = "Đã xác nhận"; break;
            case "COMPLETED":
                bgRes = R.color.status_completed_bg; txtRes = R.color.status_completed_text; label = "Hoàn thành"; break;
            case "CANCELLED":
                bgRes = R.color.status_cancelled_bg; txtRes = R.color.status_cancelled_text; label = "Đã hủy"; break;
            case "PENDING":
            default:
                bgRes = R.color.status_pending_bg; txtRes = R.color.status_pending_text; label = "Đang chờ"; break;
        }
        tv.setText(label);
        tv.setTextColor(ContextCompat.getColor(tv.getContext(), txtRes));
        tv.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(tv.getContext(), bgRes)));
    }

    static class BookingVH extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvPropertyName, tvRoomType, tvDates, tvTotalPrice, tvStatus;
        BookingVH(@NonNull View v) {
            super(v);
            ivThumb = v.findViewById(R.id.ivThumb);
            tvPropertyName = v.findViewById(R.id.tvPropertyName);
            tvRoomType = v.findViewById(R.id.tvRoomType);
            tvDates = v.findViewById(R.id.tvDates);
            tvTotalPrice = v.findViewById(R.id.tvTotalPrice);
            tvStatus = v.findViewById(R.id.tvStatus);
        }
    }
}
