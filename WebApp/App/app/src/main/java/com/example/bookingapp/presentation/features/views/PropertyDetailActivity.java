package com.example.bookingapp.presentation.features.views;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookingapp.data.model.views.RoomResponse;
import com.example.bookingapp.databinding.ActivityPropertyDetailBinding;
import com.example.bookingapp.presentation.features.home.PropertyDetailViewModel;
import com.example.bookingapp.presentation.features.home.PropertyDetailViewModelFactory;
import com.example.bookingapp.presentation.features.home.RoomAdapter;

import java.util.ArrayList;
import java.util.List;

public class PropertyDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_ID = "property_id";

    private ActivityPropertyDetailBinding binding;
    private PropertyDetailViewModel viewModel;
    private RoomAdapter roomAdapter;
    private final List<RoomResponse> roomList = new ArrayList<>();
    private Long propertyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPropertyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        propertyId = getIntent().getLongExtra(EXTRA_PROPERTY_ID, -1);

        setupToolbar();
        setupRecyclerView();
        setupViewModel();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        roomAdapter = new RoomAdapter(roomList, room -> {
            // TODO: mở màn đặt phòng sau
            Toast.makeText(this, "Chọn phòng: " + room.getRoomType(), Toast.LENGTH_SHORT).show();
        });
        binding.rvRooms.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRooms.setAdapter(roomAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this, new PropertyDetailViewModelFactory(this))
                .get(PropertyDetailViewModel.class);

        viewModel.getDetailState().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    binding.btnBookNow.setEnabled(false);
                    break;

                case SUCCESS:
                    binding.btnBookNow.setEnabled(true);
                    if (resource.data != null) {
                        binding.tvPropertyName.setText(resource.data.getName());
                        binding.tvPropertyAddress.setText(
                                resource.data.getCity() + ", " + resource.data.getAddress()
                        );
                        binding.tvPropertyDescription.setText(
                                resource.data.getDescription() != null
                                        ? resource.data.getDescription()
                                        : "Chưa có mô tả"
                        );

                        if (resource.data.getRooms() != null) {
                            roomList.clear();
                            roomList.addAll(resource.data.getRooms());
                            roomAdapter.notifyDataSetChanged();
                        }
                    }
                    break;

                case ERROR:
                    binding.btnBookNow.setEnabled(true);
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        if (propertyId != -1) {
            viewModel.loadDetail(propertyId);
        }
    }
}
