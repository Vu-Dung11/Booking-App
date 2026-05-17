package com.example.bookingapp.presentation.features.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingapp.R;
import com.example.bookingapp.core.utils.Formatter;
import com.example.bookingapp.data.model.review.ReviewResponse;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    private final List<ReviewResponse> items;
    private final boolean preview;

    public ReviewAdapter(List<ReviewResponse> items, boolean preview) {
        this.items = items;
        this.preview = preview;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ReviewResponse r = items.get(position);
        String name = r.getGuestName() != null ? r.getGuestName() : "Khách";
        h.tvName.setText(name);
        h.tvAvatar.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());
        h.tvDate.setText(formatDate(r.getCreatedAt()));
        h.rbRating.setRating(r.getRating() != null ? r.getRating() : 0);
        h.tvComment.setText(r.getComment() != null ? r.getComment() : "");
        h.tvComment.setMaxLines(preview ? 3 : Integer.MAX_VALUE);
    }

    private String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        return Formatter.displayDate(iso.substring(0, 10));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvDate, tvComment;
        RatingBar rbRating;
        VH(@NonNull View v) {
            super(v);
            tvAvatar = v.findViewById(R.id.tvAvatar);
            tvName = v.findViewById(R.id.tvName);
            tvDate = v.findViewById(R.id.tvDate);
            tvComment = v.findViewById(R.id.tvComment);
            rbRating = v.findViewById(R.id.rbRating);
        }
    }
}
