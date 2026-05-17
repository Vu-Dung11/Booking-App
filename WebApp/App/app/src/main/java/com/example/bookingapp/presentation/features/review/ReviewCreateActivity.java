package com.example.bookingapp.presentation.features.review;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.core.utils.Formatter;
import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.databinding.ActivityReviewCreateBinding;
import com.google.android.material.snackbar.Snackbar;

public class ReviewCreateActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "booking_id";
    public static final String EXTRA_PROPERTY_NAME = "property_name";
    public static final String EXTRA_ROOM_THUMB_URL = "room_thumb_url";
    public static final String EXTRA_CHECK_IN = "check_in";
    public static final String EXTRA_CHECK_OUT = "check_out";

    private static final String[] LABELS = {
            "", "Tệ", "Không hài lòng", "Bình thường", "Tốt", "Tuyệt vời"
    };

    private ActivityReviewCreateBinding binding;
    private ReviewCreateViewModel viewModel;
    private Long bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1L);
        if (bookingId <= 0) { finish(); return; }

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.tvName.setText(getIntent().getStringExtra(EXTRA_PROPERTY_NAME));
        String in = getIntent().getStringExtra(EXTRA_CHECK_IN);
        String out = getIntent().getStringExtra(EXTRA_CHECK_OUT);
        if (in != null && out != null) {
            binding.tvDates.setText(Formatter.displayDate(in) + " → " + Formatter.displayDate(out));
        }

        binding.rbStars.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            int r = (int) rating;
            binding.tvRatingLabel.setText(r >= 1 && r <= 5 ? LABELS[r] : "");
            binding.btnSubmit.setEnabled(r >= 1);
        });

        viewModel = new ViewModelProvider(this, new ReviewViewModelFactory(this))
                .get(ReviewCreateViewModel.class);

        viewModel.getSubmitState().observe(this, res -> {
            if (res == null) return;
            switch (res.status) {
                case LOADING:
                    binding.btnSubmit.setEnabled(false);
                    break;
                case SUCCESS:
                    Toast.makeText(this, "Cảm ơn đánh giá!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                    break;
                case ERROR:
                    binding.btnSubmit.setEnabled(binding.rbStars.getRating() >= 1);
                    String msg = res.message != null ? res.message : "Lỗi gửi đánh giá";
                    if (msg.contains("509") || msg.toLowerCase().contains("existed")) {
                        Toast.makeText(this, "Đơn này đã được đánh giá rồi", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
                    }
                    break;
            }
        });

        binding.btnSubmit.setOnClickListener(v -> {
            int rating = (int) binding.rbStars.getRating();
            if (rating < 1) return;
            String comment = binding.etComment.getText() != null
                    ? binding.etComment.getText().toString().trim() : "";
            viewModel.submit(bookingId, rating, comment);
        });
    }
}
