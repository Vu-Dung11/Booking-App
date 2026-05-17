package com.example.bookingapp.presentation.features.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.R;
import com.example.bookingapp.core.utils.Formatter;
import com.example.bookingapp.data.model.booking.Booking;
import com.example.bookingapp.databinding.ActivityBookingDetailBinding;
import com.example.bookingapp.presentation.features.payment.PaymentActivity;
import com.google.android.material.snackbar.Snackbar;

public class BookingDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "extra_booking_id";

    private ActivityBookingDetailBinding binding;
    private BookingDetailViewModel viewModel;
    private Long bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1L);
        if (bookingId <= 0) { finish(); return; }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this, new BookingViewModelFactory(this))
                .get(BookingDetailViewModel.class);

        observe();
        viewModel.loadBooking(bookingId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null && bookingId != null && bookingId > 0) {
            viewModel.loadBooking(bookingId);
        }
    }

    private void observe() {
        viewModel.getBookingState().observe(this, res -> {
            switch (res.status) {
                case LOADING:
                    binding.progress.setVisibility(View.VISIBLE);
                    binding.content.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progress.setVisibility(View.GONE);
                    if (res.data != null) {
                        bind(res.data);
                        binding.content.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    binding.progress.setVisibility(View.GONE);
                    Snackbar.make(binding.getRoot(),
                                    res.message != null ? res.message : "Lỗi tải đơn", Snackbar.LENGTH_LONG)
                            .setAction("Thử lại", v -> viewModel.loadBooking(bookingId))
                            .show();
                    break;
            }
        });

        viewModel.getCompleteState().observe(this, res -> {
            if (res == null) return;
            switch (res.status) {
                case SUCCESS:
                    Toast.makeText(this, "Đã xác nhận trả phòng", Toast.LENGTH_SHORT).show();
                    viewModel.loadBooking(bookingId);
                    break;
                case ERROR:
                    Toast.makeText(this, res.message != null ? res.message : "Lỗi", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        viewModel.getCancelState().observe(this, res -> {
            if (res == null) return;
            switch (res.status) {
                case LOADING:
                    binding.btnCancel.setEnabled(false);
                    break;
                case SUCCESS:
                    binding.btnCancel.setEnabled(true);
                    Toast.makeText(this, "Đã hủy đơn", Toast.LENGTH_SHORT).show();
                    viewModel.loadBooking(bookingId);
                    break;
                case ERROR:
                    binding.btnCancel.setEnabled(true);
                    Toast.makeText(this, res.message != null ? res.message : "Không hủy được đơn", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // PaymentActivity tự lấy URL và load WebView; không cần observe ở đây nữa.
    }

    private void bind(Booking b) {
        String name = b.getRoom() != null && b.getRoom().getProperty() != null
                ? b.getRoom().getProperty().getName() : "Homestay";
        String addr = b.getRoom() != null && b.getRoom().getProperty() != null
                ? b.getRoom().getProperty().getAddress() + ", " + b.getRoom().getProperty().getCity()
                : "";

        binding.tvPropertyName.setText(name);
        binding.tvAddress.setText(addr);
        BookingAdapter.applyStatus(binding.tvStatus, b.getStatus());
        binding.tvTotalPrice.setText(Formatter.currency(b.getTotalPrice()));

        setRow(R.id.rowRoom, "Loại phòng", b.getRoom() != null ? b.getRoom().getRoomType() : "-");
        setRow(R.id.rowCheckIn, "Nhận phòng", Formatter.displayDate(b.getCheckInDate()));
        setRow(R.id.rowCheckOut, "Trả phòng", Formatter.displayDate(b.getCheckOutDate()));
        setRow(R.id.rowQuantity, "Số phòng", String.valueOf(b.getRoomQuantity() != null ? b.getRoomQuantity() : 1));

        boolean isPending = "PENDING".equals(b.getStatus());
        binding.btnPay.setVisibility(isPending ? View.VISIBLE : View.GONE);
        binding.btnCancel.setVisibility(isPending ? View.VISIBLE : View.GONE);
        binding.btnCheckout.setVisibility("CONFIRMED".equals(b.getStatus()) ? View.VISIBLE : View.GONE);
        binding.btnReview.setVisibility("COMPLETED".equals(b.getStatus()) ? View.VISIBLE : View.GONE);

        binding.btnPay.setOnClickListener(v -> {
            Intent i = new Intent(this, PaymentActivity.class);
            i.putExtra(PaymentActivity.EXTRA_BOOKING_ID, b.getId());
            startActivity(i);
        });
        binding.btnCancel.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Hủy đơn?")
                .setMessage("Bạn chắc chắn muốn hủy đơn đặt phòng này?")
                .setPositiveButton("Hủy đơn", (d, w) -> viewModel.cancelBooking(b.getId()))
                .setNegativeButton("Đóng", null)
                .show());
        binding.btnCheckout.setOnClickListener(v -> viewModel.completeBooking(b.getId()));
        binding.btnReview.setOnClickListener(v ->
                Toast.makeText(this, "Tính năng review sẽ có ở Ưu tiên 3", Toast.LENGTH_SHORT).show());
    }

    private void setRow(int rowId, String label, String value) {
        View row = findViewById(rowId);
        ((TextView) row.findViewById(R.id.rowLabel)).setText(label);
        ((TextView) row.findViewById(R.id.rowValue)).setText(value);
    }
}
