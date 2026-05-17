package com.example.bookingapp.presentation.features.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookingapp.core.utils.Formatter;
import com.example.bookingapp.data.model.views.PropertySearchResponse;
import com.example.bookingapp.databinding.ActivitySearchResultBinding;
import com.example.bookingapp.presentation.features.views.PropertyDetailActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {

    public static final String EXTRA_CITY = "city";
    public static final String EXTRA_CHECK_IN = "check_in";
    public static final String EXTRA_CHECK_OUT = "check_out";
    public static final String EXTRA_GUESTS = "guests";

    private ActivitySearchResultBinding binding;
    private SearchViewModel viewModel;
    private SearchResultAdapter adapter;
    private final List<PropertySearchResponse> items = new ArrayList<>();

    private String city;
    private String checkIn;
    private String checkOut;
    private int guests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        city = getIntent().getStringExtra(EXTRA_CITY);
        checkIn = getIntent().getStringExtra(EXTRA_CHECK_IN);
        checkOut = getIntent().getStringExtra(EXTRA_CHECK_OUT);
        guests = getIntent().getIntExtra(EXTRA_GUESTS, 1);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.tvSummary.setText(buildSummary());

        adapter = new SearchResultAdapter(items, this::openDetail);
        binding.rvResults.setLayoutManager(new LinearLayoutManager(this));
        binding.rvResults.setAdapter(adapter);

        viewModel = new ViewModelProvider(this, new SearchViewModelFactory(this))
                .get(SearchViewModel.class);
        viewModel.getResultsState().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    binding.progress.setVisibility(View.VISIBLE);
                    binding.layoutEmpty.setVisibility(View.GONE);
                    items.clear();
                    adapter.notifyDataSetChanged();
                    break;
                case SUCCESS:
                    binding.progress.setVisibility(View.GONE);
                    items.clear();
                    if (resource.data != null) items.addAll(resource.data);
                    adapter.notifyDataSetChanged();
                    binding.layoutEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    break;
                case ERROR:
                    binding.progress.setVisibility(View.GONE);
                    binding.layoutEmpty.setVisibility(View.GONE);
                    Snackbar.make(binding.getRoot(),
                            resource.message != null ? resource.message : "Lỗi tìm kiếm",
                            Snackbar.LENGTH_LONG)
                            .setAction("Thử lại", v -> viewModel.refresh())
                            .show();
                    break;
            }
        });

        viewModel.search(city, checkIn, checkOut, guests);
    }

    private String buildSummary() {
        return (city != null ? city : "")
                + " • " + Formatter.displayDate(checkIn) + " → " + Formatter.displayDate(checkOut)
                + " • " + guests + " khách";
    }

    private void openDetail(PropertySearchResponse it) {
        if (it.getPropertyId() == null) return;
        Intent i = new Intent(this, PropertyDetailActivity.class);
        i.putExtra(PropertyDetailActivity.EXTRA_PROPERTY_ID, it.getPropertyId());
        i.putExtra(PropertyDetailActivity.EXTRA_CHECK_IN, checkIn);
        i.putExtra(PropertyDetailActivity.EXTRA_CHECK_OUT, checkOut);
        i.putExtra(PropertyDetailActivity.EXTRA_GUESTS, guests);
        startActivity(i);
    }
}
