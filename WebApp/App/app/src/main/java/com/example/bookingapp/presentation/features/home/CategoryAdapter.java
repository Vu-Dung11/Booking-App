package com.example.bookingapp.presentation.features.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingapp.R;
import com.example.bookingapp.data.model.views.Category;
import com.example.bookingapp.databinding.ItemCategoryBinding;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{

    private final List<Category> categories;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }
    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCategoryBinding binding;

        public ViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.binding.tvCategoryIcon.setText(category.getIcon());
        holder.binding.tvCategoryName.setText(category.getName());

        // Highlight nếu đang được chọn
        if (category.isSelected()) {
            holder.binding.cardCategory.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_light));
            holder.binding.tvCategoryName.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
        } else {
            holder.binding.cardCategory.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.surface_variant));
            holder.binding.tvCategoryName.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
        }

        // Xử lý click
        holder.itemView.setOnClickListener(v -> {
            for (Category c : categories) c.setSelected(false);
            category.setSelected(true);
            notifyDataSetChanged();
            listener.onCategoryClick(category);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
