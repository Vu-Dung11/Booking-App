package com.example.bookingapp.presentation.features.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookingapp.core.utils.Formatter;
import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.review.ReviewResponse;
import com.example.bookingapp.data.model.views.RoomResponse;
import com.example.bookingapp.databinding.ActivityPropertyDetailBinding;
import com.example.bookingapp.presentation.features.booking.BookingCreateActivity;
import com.example.bookingapp.presentation.features.chat.ChatActivity;
import com.example.bookingapp.presentation.features.home.PropertyDetailViewModel;
import com.example.bookingapp.presentation.features.home.PropertyDetailViewModelFactory;
import com.example.bookingapp.presentation.features.home.RoomAdapter;
import com.example.bookingapp.presentation.features.review.PropertyReviewsActivity;
import com.example.bookingapp.presentation.features.review.ReviewAdapter;
import com.example.bookingapp.presentation.common.ImagePagerAdapter;
import com.example.bookingapp.presentation.features.review.ReviewListViewModel;
import com.example.bookingapp.presentation.features.review.ReviewViewModelFactory;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Collections;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PropertyDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_ID = "property_id";
    public static final String EXTRA_CHECK_IN = "check_in";
    public static final String EXTRA_CHECK_OUT = "check_out";
    public static final String EXTRA_GUESTS = "guests";

    private ActivityPropertyDetailBinding binding;
    private PropertyDetailViewModel viewModel;
    private ReviewListViewModel reviewListViewModel;
    private RoomAdapter roomAdapter;
    private ReviewAdapter reviewAdapter;
    private final List<RoomResponse> roomList = new ArrayList<>();
    private final List<ReviewResponse> previewReviews = new ArrayList<>();
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

        binding.fabChat.setOnClickListener(v -> {
            Intent i = new Intent(this, ChatActivity.class);
            if (propertyId != null && propertyId > 0) {
                i.putExtra(ChatActivity.EXTRA_PROPERTY_ID, propertyId);
            }
            startActivity(i);
        });

        // Default: hôm nay → mai
        long todayUtcMidnight = MaterialDatePicker.todayInUtcMilliseconds();
        checkInMillis = todayUtcMidnight;
        checkOutMillis = todayUtcMidnight + TimeUnit.DAYS.toMillis(1);

        String extraIn = getIntent().getStringExtra(EXTRA_CHECK_IN);
        String extraOut = getIntent().getStringExtra(EXTRA_CHECK_OUT);
        if (extraIn != null && extraOut != null) {
            long in = Formatter.fromApiDate(extraIn);
            long out = Formatter.fromApiDate(extraOut);
            if (in > 0 && out > in) {
                checkInMillis = in;
                checkOutMillis = out;
            }
        }
        int extraGuests = getIntent().getIntExtra(EXTRA_GUESTS, 0);
        if (extraGuests > 0) guests = extraGuests;

        setupToolbar();
        setupRecyclerView();
        setupDateAndGuestPicker();
        setupViewModel();
        setupReviewSection();

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

                        setupPropertyPager(resource.data);

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

    private void setupPropertyPager(com.example.bookingapp.data.model.views.PropertyDetailResponse detail) {
        java.util.List<String> urls = detail.getImageUrls();
        if (urls == null || urls.isEmpty()) {
            urls = detail.getThumbnailUrl() != null
                    ? java.util.Collections.singletonList(detail.getThumbnailUrl())
                    : Collections.emptyList();
        }
        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(urls);
        binding.vpPropertyImages.setAdapter(pagerAdapter);
        if (urls.size() > 1) {
            new TabLayoutMediator(binding.dotsIndicator, binding.vpPropertyImages,
                    (tab, pos) -> {}).attach();
            binding.dotsIndicator.setVisibility(View.VISIBLE);
        } else {
            binding.dotsIndicator.setVisibility(View.GONE);
        }
    }

    private void setupReviewSection() {
        if (propertyId == null || propertyId <= 0) return;
        reviewAdapter = new ReviewAdapter(previewReviews, true);
        binding.rvReviewsPreview.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviewsPreview.setAdapter(reviewAdapter);

        binding.btnSeeAllReviews.setOnClickListener(v -> {
            Intent i = new Intent(this, PropertyReviewsActivity.class);
            i.putExtra(PropertyReviewsActivity.EXTRA_PROPERTY_ID, propertyId);
            startActivity(i);
        });

        reviewListViewModel = new ViewModelProvider(this, new ReviewViewModelFactory(this))
                .get(ReviewListViewModel.class);

        reviewListViewModel.getSummaryState().observe(this, res -> {
            if (res.status == Resource.Status.SUCCESS && res.data != null) {
                long total = res.data.getTotalCount();
                if (total > 0) {
                    binding.tvReviewHeader.setText(String.format("★ %.1f · %d đánh giá",
                            res.data.getAverageRating(), total));
                    binding.tvReviewEmpty.setVisibility(View.GONE);
                    binding.btnSeeAllReviews.setVisibility(total > 3 ? View.VISIBLE : View.GONE);
                    binding.btnSeeAllReviews.setText("Xem tất cả " + total + " đánh giá");
                } else {
                    binding.tvReviewHeader.setText("");
                    binding.tvReviewEmpty.setVisibility(View.VISIBLE);
                    binding.btnSeeAllReviews.setVisibility(View.GONE);
                }
            }
        });

        reviewListViewModel.getAccumulated().observe(this, list -> {
            previewReviews.clear();
            if (list != null) {
                int take = Math.min(3, list.size());
                for (int i = 0; i < take; i++) previewReviews.add(list.get(i));
            }
            reviewAdapter.notifyDataSetChanged();
        });

        reviewListViewModel.loadInitial(propertyId);
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
