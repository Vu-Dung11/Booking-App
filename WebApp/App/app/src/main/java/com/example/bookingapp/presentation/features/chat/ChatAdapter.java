package com.example.bookingapp.presentation.features.chat;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookingapp.R;
import com.example.bookingapp.data.model.chat.ChatMessage;
import com.example.bookingapp.data.model.chat.PropertyCard;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnPropertyCardClickListener {
        void onCardClick(PropertyCard card);
    }

    private static final int TYPE_USER = 1;
    private static final int TYPE_BOT = 2;
    private static final int TYPE_TYPING = 3;
    private static final int TYPE_CARD = 4;

    private final List<Object> items; // ChatMessage or PropertyCard
    private OnPropertyCardClickListener cardClickListener;

    public ChatAdapter(List<Object> items) {
        this.items = items;
    }

    public void setOnPropertyCardClickListener(OnPropertyCardClickListener l) {
        this.cardClickListener = l;
    }

    @Override
    public int getItemViewType(int position) {
        Object o = items.get(position);
        if (o instanceof PropertyCard) return TYPE_CARD;
        ChatMessage m = (ChatMessage) o;
        if (m.isTyping()) return TYPE_TYPING;
        if (m.isUser()) return TYPE_USER;
        return TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_USER:
                return new TextHolder(inf.inflate(R.layout.item_chat_user, parent, false));
            case TYPE_TYPING:
                return new TypingHolder(inf.inflate(R.layout.item_chat_typing, parent, false));
            case TYPE_CARD:
                return new CardHolder(inf.inflate(R.layout.item_chat_property_card, parent, false));
            case TYPE_BOT:
            default:
                return new TextHolder(inf.inflate(R.layout.item_chat_bot, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        Object o = items.get(position);
        if (h instanceof TextHolder) {
            ((TextHolder) h).bind((ChatMessage) o);
        } else if (h instanceof TypingHolder) {
            ((TypingHolder) h).bind();
        } else if (h instanceof CardHolder) {
            ((CardHolder) h).bind((PropertyCard) o, cardClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ============================================================

    static class TextHolder extends RecyclerView.ViewHolder {
        final TextView tvContent;
        TextHolder(View v) {
            super(v);
            tvContent = v.findViewById(R.id.tvContent);
        }
        void bind(ChatMessage m) {
            tvContent.setText(m.getContent() == null ? "" : m.getContent());
        }
    }

    static class TypingHolder extends RecyclerView.ViewHolder {
        TypingHolder(View v) {
            super(v);
            animateDot(v.findViewById(R.id.tvDot1), 0);
            animateDot(v.findViewById(R.id.tvDot2), 150);
            animateDot(v.findViewById(R.id.tvDot3), 300);
        }
        void bind() { /* nothing */ }

        private void animateDot(View dot, long startDelay) {
            ObjectAnimator a = ObjectAnimator.ofFloat(dot, "alpha", 0.3f, 1f, 0.3f);
            a.setDuration(900);
            a.setStartDelay(startDelay);
            a.setRepeatCount(ObjectAnimator.INFINITE);
            a.setInterpolator(new LinearInterpolator());
            a.start();
        }
    }

    static class CardHolder extends RecyclerView.ViewHolder {
        final ImageView ivThumb;
        final TextView tvName, tvCity, tvPrice;
        final View root;

        CardHolder(View v) {
            super(v);
            ivThumb = v.findViewById(R.id.ivThumbnail);
            tvName = v.findViewById(R.id.tvName);
            tvCity = v.findViewById(R.id.tvCity);
            tvPrice = v.findViewById(R.id.tvPrice);
            root = v.findViewById(R.id.cardRoot);
        }

        void bind(PropertyCard c, OnPropertyCardClickListener listener) {
            tvName.setText(c.getName() == null ? "Homestay" : c.getName());
            tvCity.setText(c.getCity() == null ? "" : c.getCity());
            BigDecimal p = c.getMinPrice();
            if (p != null && p.signum() > 0) {
                tvPrice.setText("Từ " + formatVnd(p) + " / đêm");
                tvPrice.setVisibility(View.VISIBLE);
            } else {
                tvPrice.setVisibility(View.GONE);
            }

            Context ctx = ivThumb.getContext();
            if (!TextUtils.isEmpty(c.getThumbnailUrl())) {
                Glide.with(ctx).load(c.getThumbnailUrl()).centerCrop().into(ivThumb);
            } else {
                ivThumb.setImageDrawable(null);
            }

            root.setOnClickListener(v -> {
                if (listener != null) listener.onCardClick(c);
            });
        }

        private String formatVnd(BigDecimal v) {
            NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
            return nf.format(v) + "đ";
        }
    }
}
