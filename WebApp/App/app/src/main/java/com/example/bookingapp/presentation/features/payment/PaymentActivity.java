package com.example.bookingapp.presentation.features.payment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.MainActivity;
import com.example.bookingapp.databinding.ActivityPaymentBinding;
import com.example.bookingapp.presentation.features.booking.BookingDetailViewModel;
import com.example.bookingapp.presentation.features.booking.BookingViewModelFactory;
import com.google.android.material.snackbar.Snackbar;

import java.math.BigDecimal;

public class PaymentActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID = "extra_booking_id";
    private static final String RETURN_PATH = "/api/v1/payments/vnpay-return";

    private ActivityPaymentBinding binding;
    private BookingDetailViewModel viewModel;
    private Long bookingId;
    private boolean resultShown = false;
    private boolean callbackFired = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1L);
        if (bookingId <= 0) { finish(); return; }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this, new BookingViewModelFactory(this))
                .get(BookingDetailViewModel.class);

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setDomStorageEnabled(true);
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return interceptReturn(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return interceptReturn(Uri.parse(url));
            }
        });

        viewModel.getPaymentUrlState().observe(this, res -> {
            switch (res.status) {
                case LOADING:
                    binding.progress.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progress.setVisibility(View.GONE);
                    if (res.data != null && !res.data.isEmpty()) {
                        binding.webView.setVisibility(View.VISIBLE);
                        binding.webView.loadUrl(res.data);
                    } else {
                        showResult(false, "Không nhận được link thanh toán");
                    }
                    break;
                case ERROR:
                    binding.progress.setVisibility(View.GONE);
                    Snackbar.make(binding.getRoot(),
                                    res.message != null ? res.message : "Lỗi tải link thanh toán",
                                    Snackbar.LENGTH_LONG)
                            .setAction("Thử lại", v -> viewModel.requestVnpayUrl(bookingId))
                            .show();
                    break;
            }
        });

        viewModel.getCallbackState().observe(this, res -> {
            if (res == null) return;
            switch (res.status) {
                case LOADING:
                    binding.progress.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progress.setVisibility(View.GONE);
                    showResult(true, "Thanh toán thành công. Đơn #" + bookingId + " đã được xác nhận.");
                    break;
                case ERROR:
                    binding.progress.setVisibility(View.GONE);
                    showResult(false, "Backend không cập nhật được trạng thái: "
                            + (res.message != null ? res.message : ""));
                    break;
            }
        });

        viewModel.requestVnpayUrl(bookingId);
    }

    /**
     * Bắt redirect của VNPay về returnUrl. WebView KHÔNG cần thực sự load URL đó
     * (vì có thể là localhost / domain unreach). Thay vào đó: đọc vnp_ResponseCode
     * từ chính URL và gọi backend POST /api/v1/payments/callback để update booking.
     */
    private boolean interceptReturn(Uri uri) {
        if (uri == null) return false;
        String path = uri.getPath();
        if (path == null || !path.contains(RETURN_PATH)) return false;
        if (callbackFired) return true;
        callbackFired = true;

        String code = uri.getQueryParameter("vnp_ResponseCode");
        String txnRef = uri.getQueryParameter("vnp_TxnRef");
        String amountStr = uri.getQueryParameter("vnp_Amount");

        binding.webView.setVisibility(View.GONE);

        if ("00".equals(code)) {
            // VNPay sandbox amount = VND * 100
            BigDecimal amount = BigDecimal.ZERO;
            if (amountStr != null) {
                try {
                    amount = new BigDecimal(amountStr).movePointLeft(2);
                } catch (NumberFormatException ignored) {}
            }
            viewModel.notifyPaymentSuccess(bookingId, txnRef, amount);
        } else {
            showResult(false, "Thanh toán thất bại hoặc bị huỷ (mã " + code + ").");
        }
        return true; // consume URL - không cần WebView GET tới returnUrl
    }

    private void showResult(boolean success, String message) {
        if (resultShown) return;
        resultShown = true;
        binding.webView.setVisibility(View.GONE);
        binding.progress.setVisibility(View.GONE);
        binding.resultView.setVisibility(View.VISIBLE);
        binding.ivResult.setImageResource(success
                ? android.R.drawable.checkbox_on_background
                : android.R.drawable.stat_notify_error);
        binding.tvResult.setText(success ? "Thanh toán thành công" : "Thanh toán không thành công");
        binding.tvResultMessage.setText(message);
        binding.btnDone.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra(MainActivity.EXTRA_OPEN_TAB, MainActivity.TAB_BOOKING);
            startActivity(i);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        if (!resultShown && binding.webView.canGoBack()) {
            binding.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
