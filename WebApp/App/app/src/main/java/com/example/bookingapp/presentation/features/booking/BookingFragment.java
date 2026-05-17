package com.example.bookingapp.presentation.features.booking;

import android.content.Intent;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookingapp.core.base.BaseFragment;
import com.example.bookingapp.databinding.FragmentBookingBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

public class BookingFragment extends BaseFragment<FragmentBookingBinding> {

    private static final String[] TAB_LABELS = {"Tất cả", "Đang chờ", "Đã xác nhận", "Hoàn thành", "Đã hủy"};
    private static final String[] TAB_STATUS = {null, "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"};

    private BookingViewModel viewModel;
    private BookingAdapter adapter;

    @Override
    protected Inflate<FragmentBookingBinding> getInflate() {
        return FragmentBookingBinding::inflate;
    }

    @Override
    protected void setupViews() {
        viewModel = new ViewModelProvider(this, new BookingViewModelFactory(requireContext()))
                .get(BookingViewModel.class);

        adapter = new BookingAdapter(booking -> {
            Intent i = new Intent(requireContext(), BookingDetailActivity.class);
            i.putExtra(BookingDetailActivity.EXTRA_BOOKING_ID, booking.getId());
            startActivity(i);
        });
        getBinding().rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        getBinding().rvBookings.setAdapter(adapter);

        for (String label : TAB_LABELS) {
            getBinding().tabLayout.addTab(getBinding().tabLayout.newTab().setText(label));
        }
        getBinding().tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                viewModel.loadBookings(TAB_STATUS[tab.getPosition()]);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) { viewModel.refresh(); }
        });

        viewModel.loadBookings(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.refresh();
    }

    @Override
    protected void observeViewModel() {
        viewModel.getBookingsState().observe(getViewLifecycleOwner(), res -> {
            switch (res.status) {
                case LOADING:
                    adapter.submit(null);
                    getBinding().progress.setVisibility(View.VISIBLE);
                    getBinding().emptyView.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    getBinding().progress.setVisibility(View.GONE);
                    if (res.data != null && res.data.getContent() != null && !res.data.getContent().isEmpty()) {
                        adapter.submit(res.data.getContent());
                        getBinding().emptyView.setVisibility(View.GONE);
                        getBinding().rvBookings.setVisibility(View.VISIBLE);
                    } else {
                        adapter.submit(null);
                        getBinding().emptyView.setVisibility(View.VISIBLE);
                    }
                    break;
                case ERROR:
                    getBinding().progress.setVisibility(View.GONE);
                    Snackbar.make(getBinding().getRoot(),
                                    res.message != null ? res.message : "Lỗi tải đơn", Snackbar.LENGTH_LONG)
                            .setAction("Thử lại", v -> viewModel.refresh())
                            .show();
                    break;
            }
        });
    }
}
