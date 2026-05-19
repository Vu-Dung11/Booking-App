package com.example.bookingapp.presentation.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.VH> {

    private List<String> urls;

    public ImagePagerAdapter(List<String> urls) {
        this.urls = urls != null ? urls : new ArrayList<>();
    }

    public void submit(List<String> newUrls) {
        this.urls = newUrls != null ? newUrls : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView iv = new ImageView(parent.getContext());
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new VH(iv);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String url = urls.get(position);
        Glide.with(h.itemView)
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into((ImageView) h.itemView);
    }

    @Override
    public int getItemCount() { return urls.size(); }

    static class VH extends RecyclerView.ViewHolder {
        VH(@NonNull View v) { super(v); }
    }
}
