package com.example.bookingapp.presentation.features.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookingapp.core.utils.Formatter;
import com.example.bookingapp.data.model.views.RoomResponse;
import com.example.bookingapp.databinding.ActivityPropertyDetailBinding;
import com.example.bookingapp.presentation.features.booking.BookingCreateActivity;
import com.example.bookingapp.presentation.features.home.PropertyDetailViewModel;
import com.example.bookingapp.presentation.features.home.PropertyDetailViewModelFactory;
import com.example.bookingapp.presentation.features.home.RoomAdapter;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PropertyDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_ID = "property_id";

    private ActivityPropertyDetailBinding binding;
    private PropertyDetailViewModel viewModel;
    private RoomAdapter roomAdapter;
    private final List<RoomResponse> roomList = new ArrayList<>();
    private Long propertyId;

    private long checkInMillis;
    private long checkOutMillis;
    private int guests = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPropertyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        propertyId = getIntent().getLongExtra(EXTRA_PROPERTY_ID, -1);

        // Default: hôm nay → mai
        long todayUtcMidnight = MaterialDatePicker.todayInUtcMilliseconds();
        checkInMillis = todayUtcMidnight;
        checkOutMillis = todayUtcMidnight + TimeUnit.DAYS.toMillis(1);

        setupToolbar();
        setupRecyclerView();
        setupDateAndGuestPicker();
        setupViewModel();

        updateDateLabel();
        updateGuestsLabel();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        roomAdapter = new RoomAdapter(roomList, room -> openBooking(room));
        binding.rvRooms.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRooms.setAdapter(roomAdapter);
    }

    private void setupDateAndGuestPicker() {
        binding.btnPickDates.setOnClickListener(v -> showDateRangePicker());
        binding.btnGuestsMinus.setOnClickListener(v -> changeGuests(-1));
        binding.btnGuestsPlus.setOnClickListener(v -> changeGuests(+1));
    }

    private void showDateRangePicker() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Chọn ngày nhận - trả phòng")
                        .setSelection(new androidx.core.util.Pair<>(checkInMillis, checkOutMillis))
                        .setCalendarConstraints(constraints)
                        .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection.first != null && selection.second != null) {
                checkInMillis = selection.first;
                checkOutMillis = selection.second;
                if (checkOutMillis <= checkInMillis) {
                    checkOutMillis = checkInMillis + TimeUnit.DAYS.toMillis(1);
                }
                updateDateLabel();
                reloadDetail();
            }
        });
        picker.show(getSupportFragmentManager(), "date_range_picker");
    }

    private void changeGuests(int delta) {
        int next = guests + delta;
        if (next < 1) next = 1;
        if (next == guests) return;
        guests = next;
        updateGuestsLabel();
        reloadDetail();
    }

    private void updateDateLabel() {
        String in = Formatter.displayDate(Formatter.toApiDate(checkInMillis));
        String out = Formatter.displayDate(Formatter.toApiDate(checkOutMillis));
        binding.btnPickDates.setText(in + "  →  " + out);
    }

    private void updateGuestsLabel() {
        binding.tvGuests.setText(String.valueOf(guests));
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this, new PropertyDetailViewModelFactory(this))
                .get(PropertyDetailViewModel.class);

        viewModel.getDetailState().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    if (resource.data != null) {
                        binding.tvPropertyName.setText(resource.data.getName());
                        binding.tvPropertyAddress.setText(
                                resource.data.getCity() + ", " + resource.data.getAddress());
                        binding.tvPropertyDescription.setText(
                                resource.data.getDescription() != null
                                        ? resource.data.getDescription()
                                        : "Chưa có mô tả");

                        roomList.clear();
                        if (resource.data.getRooms() != null) {
                            roomList.addAll(resource.data.getRooms());
                        }
                        roomAdapter.notifyDataSetChanged();
                    }
                    break;
                case ERROR:
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        if (propertyId != -1) reloadDetail();
    }

    private void reloadDetail() {
        if (propertyId == null || propertyId <= 0) return;
        viewModel.loadDetail(propertyId,
                Formatter.toApiDate(checkInMillis),
                Formatter.toApiDate(checkOutMillis),
                guests);
    }

    private void openBooking(RoomResponse room) {
        if (propertyId == null || propertyId <= 0) {
            Toast.makeText(this, "Không xác định homestay", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, BookingCreateActivity.class);
        i.putExtra(BookingCreateActivity.EXTRA_PROPERTY_ID, propertyId);
        i.putExtra(BookingCreateActivity.EXTRA_ROOM_ID, room.getRoomId());
        i.putExtra(BookingCreateActivity.EXTRA_CHECK_IN, Formatter.toApiDate(checkInMillis));
        i.putExtra(BookingCreateActivity.EXTRA_CHECK_OUT, Formatter.toApiDate(checkOutMillis));
        if (room.getAvailableCount() != null) {
            i.putExtra(BookingCreateActivity.EXTRA_AVAILABLE_COUNT, room.getAvailableCount().intValue());
        }
        startActivity(i);
    }
}
