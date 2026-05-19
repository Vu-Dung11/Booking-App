package com.example.bookingapp.presentation.features.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.MainActivity;
import com.example.bookingapp.core.utils.Formatter;
import com.example.bookingapp.data.model.booking.BookingRequest;
import com.example.bookingapp.data.model.views.PropertyDetailResponse;
import com.example.bookingapp.data.model.views.RoomResponse;
import com.example.bookingapp.databinding.ActivityBookingCreateBinding;
import com.example.bookingapp.presentation.common.ImagePagerAdapter;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BookingCreateActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_ID = "extra_property_id";
    public static final String EXTRA_ROOM_ID = "extra_room_id";
    public static final String EXTRA_CHECK_IN = "extra_check_in";          // "yyyy-MM-dd"
    public static final String EXTRA_CHECK_OUT = "extra_check_out";        // "yyyy-MM-dd"
    public static final String EXTRA_AVAILABLE_COUNT = "extra_available";  // int, 0/absent = không giới hạn

    private ActivityBookingCreateBinding binding;
    private BookingCreateViewModel viewModel;
    private ImagePagerAdapter imagePagerAdapter;

    private final List<RoomResponse> rooms = new ArrayList<>();
    private RoomResponse selectedRoom;
    private Long checkInMillis = null;
    private Long checkOutMillis = null;
    private int quantity = 1;
    /** Trần số phòng cho phép đặt — từ availableCount của room đã chọn. 0 = không giới hạn. */
    private int maxQuantity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        long propertyId = getIntent().getLongExtra(EXTRA_PROPERTY_ID, -1L);
        long preselectRoomId = getIntent().getLongExtra(EXTRA_ROOM_ID, -1L);
        String preCheckIn = getIntent().getStringExtra(EXTRA_CHECK_IN);
        String preCheckOut = getIntent().getStringExtra(EXTRA_CHECK_OUT);
        maxQuantity = getIntent().getIntExtra(EXTRA_AVAILABLE_COUNT, 0);
        if (propertyId <= 0) { finish(); return; }

        // Pre-fill ngày từ Intent (cho phép sửa sau)
        if (preCheckIn != null) {
            long m = Formatter.fromApiDate(preCheckIn);
            if (m > 0) {
                checkInMillis = m;
                binding.btnCheckIn.setText(Formatter.displayDate(preCheckIn));
            }
        }
        if (preCheckOut != null) {
            long m = Formatter.fromApiDate(preCheckOut);
            if (m > 0) {
                checkOutMillis = m;
                binding.btnCheckOut.setText(Formatter.displayDate(preCheckOut));
            }
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this, new BookingCreateViewModelFactory(this))
                .get(BookingCreateViewModel.class);

        imagePagerAdapter = new ImagePagerAdapter(new ArrayList<>());
        binding.vpRoomImages.setAdapter(imagePagerAdapter);
        new TabLayoutMediator(binding.dotsIndicator, binding.vpRoomImages,
                (tab, pos) -> {}).attach();

        binding.btnCheckIn.setOnClickListener(v -> pickDate(true));
        binding.btnCheckOut.setOnClickListener(v -> pickDate(false));
        binding.btnMinus.setOnClickListener(v -> changeQuantity(-1));
        binding.btnPlus.setOnClickListener(v -> changeQuantity(+1));
        binding.btnSubmit.setOnClickListener(v -> submit());

        observe(preselectRoomId);
        // Truyền dates xuống endpoint detail để có availableCount cho rooms khi
        // dropdown đổi sang room khác.
        viewModel.loadDetail(propertyId, preCheckIn, preCheckOut, null);
    }

    private void observe(long preselectRoomId) {
        viewModel.getDetailState().observe(this, res -> {
            switch (res.status) {
                case LOADING:
                    binding.progress.setVisibility(View.VISIBLE);
                    binding.content.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progress.setVisibility(View.GONE);
                    if (res.data != null) {
                        bindDetail(res.data, preselectRoomId);
                        binding.content.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    binding.progress.setVisibility(View.GONE);
                    Snackbar.make(binding.getRoot(), res.message != null ? res.message : "Lỗi", Snackbar.LENGTH_LONG).show();
                    break;
            }
        });

        viewModel.getRoomImagesState().observe(this, res -> {
            if (res == null) return;
            if (res.status == com.example.bookingapp.core.utils.Resource.Status.SUCCESS && res.data != null) {
                imagePagerAdapter.submit(res.data);
                binding.dotsIndicator.setVisibility(res.data.size() > 1 ? View.VISIBLE : View.GONE);
            } else if (res.status == com.example.bookingapp.core.utils.Resource.Status.ERROR) {
                imagePagerAdapter.submit(new ArrayList<>());
                binding.dotsIndicator.setVisibility(View.GONE);
            }
        });

        viewModel.getCreateState().observe(this, res -> {
            switch (res.status) {
                case LOADING:
                    binding.btnSubmit.setEnabled(false);
                    binding.btnSubmit.setText("Đang tạo đơn...");
                    break;
                case SUCCESS:
                    binding.btnSubmit.setEnabled(true);
                    binding.btnSubmit.setText("Đặt phòng");
                    if (res.data != null) {
                        showHoldDialog(res.data.getId());
                    }
                    break;
                case ERROR:
                    binding.btnSubmit.setEnabled(true);
                    binding.btnSubmit.setText("Đặt phòng");
                    Snackbar.make(binding.getRoot(), res.message != null ? res.message : "Lỗi đặt phòng", Snackbar.LENGTH_LONG).show();
                    break;
            }
        });
    }

    /**
     * Sau khi tạo booking thành công, hiển thị dialog giải thích "giữ phòng tạm thời"
     * thay vì mở thẳng PaymentActivity. User chủ động vào tab Booking để thanh toán.
     */
    private void showHoldDialog(Long bookingId) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Đã giữ phòng tạm thời")
                .setMessage("Đơn đặt phòng #" + bookingId + " đang ở trạng thái CHỜ THANH TOÁN.\n\n"
                        + "Vui lòng hoàn tất thanh toán trong vòng 15 phút. "
                        + "Quá hạn đơn sẽ tự động bị huỷ.")
                .setCancelable(false)
                .setPositiveButton("Đi đến đơn của tôi", (d, w) -> {
                    Intent i = new Intent(this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.putExtra(MainActivity.EXTRA_OPEN_TAB, MainActivity.TAB_BOOKING);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("Đóng", (d, w) -> finish())
                .show();
    }

    private void bindDetail(PropertyDetailResponse d, long preselectRoomId) {
        binding.tvPropertyName.setText(d.getName());
        binding.tvPropertyAddress.setText(d.getCity() + ", " + d.getAddress());

        rooms.clear();
        if (d.getRooms() != null) rooms.addAll(d.getRooms());

        List<String> labels = new ArrayList<>();
        for (RoomResponse r : rooms) {
            labels.add(r.getRoomType() + " — " + Formatter.currency(r.getPrice()) + "/đêm");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, labels);
        binding.spRoom.setAdapter(adapter);
        binding.spRoom.setOnItemClickListener((parent, view, position, id) -> {
            selectedRoom = rooms.get(position);
            updateMaxQuantityFromRoom();
            recomputeTotal();
            if (selectedRoom.getRoomId() != null) viewModel.loadRoomImages(selectedRoom.getRoomId());
        });

        int idx = 0;
        if (preselectRoomId > 0) {
            for (int i = 0; i < rooms.size(); i++) {
                if (rooms.get(i).getRoomId() != null && rooms.get(i).getRoomId() == preselectRoomId) {
                    idx = i; break;
                }
            }
        }
        if (!rooms.isEmpty()) {
            selectedRoom = rooms.get(idx);
            binding.spRoom.setText(labels.get(idx), false);
            updateMaxQuantityFromRoom();
            recomputeTotal();
            if (selectedRoom.getRoomId() != null) viewModel.loadRoomImages(selectedRoom.getRoomId());
        }
    }

    /** Khi user đổi room, lấy availableCount từ room đó (nếu có). */
    private void updateMaxQuantityFromRoom() {
        if (selectedRoom != null && selectedRoom.getAvailableCount() != null) {
            maxQuantity = selectedRoom.getAvailableCount();
        }
        if (maxQuantity > 0 && quantity > maxQuantity) {
            quantity = maxQuantity;
            binding.tvQuantity.setText(String.valueOf(quantity));
        }
    }

    private void pickDate(boolean isCheckIn) {
        CalendarConstraints.Builder cc = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now());
        if (!isCheckIn && checkInMillis != null) {
            cc.setStart(checkInMillis + TimeUnit.DAYS.toMillis(1));
        }
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isCheckIn ? "Chọn ngày nhận phòng" : "Chọn ngày trả phòng")
                .setCalendarConstraints(cc.build())
                .build();
        picker.addOnPositiveButtonClickListener(millis -> {
            if (isCheckIn) {
                checkInMillis = millis;
                binding.btnCheckIn.setText(Formatter.displayDate(Formatter.toApiDate(millis)));
                if (checkOutMillis != null && checkOutMillis <= checkInMillis) {
                    checkOutMillis = null;
                    binding.btnCheckOut.setText("Chọn ngày");
                }
            } else {
                checkOutMillis = millis;
                binding.btnCheckOut.setText(Formatter.displayDate(Formatter.toApiDate(millis)));
            }
            recomputeTotal();
        });
        picker.show(getSupportFragmentManager(), "date_picker");
    }

    private void changeQuantity(int delta) {
        int next = quantity + delta;
        if (next < 1) next = 1;
        if (maxQuantity > 0 && next > maxQuantity) {
            Toast.makeText(this, "Tối đa " + maxQuantity + " phòng", Toast.LENGTH_SHORT).show();
            next = maxQuantity;
        }
        quantity = next;
        binding.tvQuantity.setText(String.valueOf(quantity));
        recomputeTotal();
    }

    private long nights() {
        if (checkInMillis == null || checkOutMillis == null) return 0;
        long diff = checkOutMillis - checkInMillis;
        long n = TimeUnit.MILLISECONDS.toDays(diff);
        return Math.max(0, n);
    }

    private void recomputeTotal() {
        long n = nights();
        binding.tvNights.setText(String.valueOf(n));
        if (selectedRoom != null && selectedRoom.getPrice() != null && n > 0) {
            BigDecimal total = selectedRoom.getPrice()
                    .multiply(BigDecimal.valueOf(n))
                    .multiply(BigDecimal.valueOf(quantity));
            binding.tvTotal.setText(Formatter.currency(total));
        } else {
            binding.tvTotal.setText(Formatter.currency(BigDecimal.ZERO));
        }
    }

    private void submit() {
        if (selectedRoom == null) {
            Toast.makeText(this, "Vui lòng chọn phòng", Toast.LENGTH_SHORT).show(); return;
        }
        if (checkInMillis == null || checkOutMillis == null || nights() <= 0) {
            Toast.makeText(this, "Vui lòng chọn ngày hợp lệ", Toast.LENGTH_SHORT).show(); return;
        }
        BookingRequest req = new BookingRequest(
                selectedRoom.getRoomId(),
                Formatter.toApiDate(checkInMillis),
                Formatter.toApiDate(checkOutMillis),
                quantity
        );
        viewModel.submit(req);
    }
}
