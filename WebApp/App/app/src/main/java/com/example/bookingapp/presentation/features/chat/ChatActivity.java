package com.example.bookingapp.presentation.features.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.chat.ChatMessage;
import com.example.bookingapp.data.model.chat.ChatResponse;
import com.example.bookingapp.data.model.chat.PropertyCard;
import com.example.bookingapp.databinding.ActivityChatBinding;
import com.example.bookingapp.presentation.features.views.PropertyDetailActivity;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_ID = "current_property_id";
    private static final String PREF_CHAT = "chat_prefs";
    private static final String KEY_SESSION = "chat_session_id";

    private ActivityChatBinding binding;
    private ChatViewModel viewModel;
    private ChatAdapter adapter;
    private final List<Object> items = new ArrayList<>();

    private String sessionId;
    private Long currentPropertyId;
    private boolean sending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentPropertyId = getIntent().hasExtra(EXTRA_PROPERTY_ID)
                ? getIntent().getLongExtra(EXTRA_PROPERTY_ID, -1L) : null;
        if (currentPropertyId != null && currentPropertyId <= 0) currentPropertyId = null;

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this, new ChatViewModelFactory(this)).get(ChatViewModel.class);

        adapter = new ChatAdapter(items);
        adapter.setOnPropertyCardClickListener(card -> {
            if (card.getPropertyId() == null) return;
            Intent i = new Intent(this, PropertyDetailActivity.class);
            i.putExtra(PropertyDetailActivity.EXTRA_PROPERTY_ID, card.getPropertyId());
            startActivity(i);
        });
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(lm);
        binding.rvMessages.setAdapter(adapter);

        binding.btnSend.setOnClickListener(v -> onSendClick());
        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                onSendClick();
                return true;
            }
            return false;
        });

        observe();
        bootstrapSession();
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(PREF_CHAT, Context.MODE_PRIVATE);
    }

    private void bootstrapSession() {
        sessionId = prefs().getString(KEY_SESSION, null);
        if (TextUtils.isEmpty(sessionId)) {
            viewModel.createSession();
        } else {
            viewModel.loadHistory(sessionId);
        }
    }

    private void observe() {
        viewModel.getSessionState().observe(this, res -> {
            if (res == null) return;
            if (res.status == Resource.Status.SUCCESS && res.data != null) {
                sessionId = res.data;
                prefs().edit().putString(KEY_SESSION, sessionId).apply();
                // Welcome message
                if (items.isEmpty()) {
                    ChatMessage welcome = new ChatMessage(ChatMessage.ROLE_ASSISTANT,
                            "Xin chào! Mình là trợ lý ảo của BookingApp. Mình có thể giúp bạn tìm homestay, "
                                    + "xem booking, hoặc trả lời các câu hỏi về thanh toán/huỷ phòng. Bạn cần hỗ trợ gì?");
                    items.add(welcome);
                    adapter.notifyItemInserted(items.size() - 1);
                    showSuggestions(List.of("Tìm homestay ở Đà Lạt", "Xem booking của tôi", "Chính sách huỷ phòng"));
                }
            } else if (res.status == Resource.Status.ERROR) {
                Toast.makeText(this, "Không tạo được session: " + res.message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getHistoryState().observe(this, res -> {
            if (res == null || res.status != Resource.Status.SUCCESS || res.data == null) return;
            items.clear();
            for (ChatMessage m : res.data) {
                if (m.getRole() != null && ChatMessage.ROLE_TOOL.equalsIgnoreCase(m.getRole())) continue;
                items.add(m);
            }
            adapter.notifyDataSetChanged();
            scrollToBottom();
        });

        viewModel.getSendState().observe(this, res -> {
            if (res == null) return;
            switch (res.status) {
                case LOADING:
                    // typing indicator already added in onSendClick
                    break;
                case SUCCESS:
                    removeTyping();
                    sending = false;
                    binding.btnSend.setEnabled(true);
                    if (res.data == null) {
                        Toast.makeText(this, "Phản hồi trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    handleReply(res.data);
                    break;
                case ERROR:
                    removeTyping();
                    sending = false;
                    binding.btnSend.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + res.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void onSendClick() {
        if (sending) return;
        String text = binding.etMessage.getText() == null ? "" : binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        if (TextUtils.isEmpty(sessionId)) {
            Toast.makeText(this, "Đang khởi tạo session, thử lại sau giây lát", Toast.LENGTH_SHORT).show();
            return;
        }

        items.add(new ChatMessage(ChatMessage.ROLE_USER, text));
        adapter.notifyItemInserted(items.size() - 1);

        items.add(ChatMessage.typing());
        adapter.notifyItemInserted(items.size() - 1);
        scrollToBottom();

        binding.etMessage.setText("");
        binding.chipGroupSuggestions.removeAllViews();
        binding.chipsScroll.setVisibility(View.GONE);
        sending = true;
        binding.btnSend.setEnabled(false);

        viewModel.sendMessage(sessionId, text, currentPropertyId);
    }

    private void handleReply(ChatResponse data) {
        if (!TextUtils.isEmpty(data.getReply())) {
            items.add(new ChatMessage(ChatMessage.ROLE_ASSISTANT, data.getReply()));
            adapter.notifyItemInserted(items.size() - 1);
        }
        if (data.getCards() != null) {
            for (PropertyCard c : data.getCards()) {
                items.add(c);
                adapter.notifyItemInserted(items.size() - 1);
            }
        }
        scrollToBottom();
        showSuggestions(data.getSuggestions());
    }

    private void showSuggestions(List<String> suggestions) {
        binding.chipGroupSuggestions.removeAllViews();
        if (suggestions == null || suggestions.isEmpty()) {
            binding.chipsScroll.setVisibility(View.GONE);
            return;
        }
        for (String s : suggestions) {
            Chip chip = new Chip(this);
            chip.setText(s);
            chip.setCheckable(false);
            chip.setOnClickListener(v -> {
                binding.etMessage.setText(s);
                onSendClick();
            });
            binding.chipGroupSuggestions.addView(chip);
        }
        binding.chipsScroll.setVisibility(View.VISIBLE);
    }

    private void removeTyping() {
        for (int i = items.size() - 1; i >= 0; i--) {
            Object o = items.get(i);
            if (o instanceof ChatMessage && ((ChatMessage) o).isTyping()) {
                items.remove(i);
                adapter.notifyItemRemoved(i);
                return;
            }
        }
    }

    private void scrollToBottom() {
        binding.rvMessages.post(() -> {
            if (!items.isEmpty()) binding.rvMessages.smoothScrollToPosition(items.size() - 1);
        });
    }
}
