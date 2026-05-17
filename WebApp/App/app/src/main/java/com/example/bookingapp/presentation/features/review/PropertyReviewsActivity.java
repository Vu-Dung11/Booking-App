package com.example.bookingapp.presentation.features.review;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookingapp.core.utils.Resource;
import com.example.bookingapp.data.model.review.ReviewResponse;
import com.example.bookingapp.databinding.ActivityPropertyReviewsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PropertyReviewsActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_ID = "property_id";

    private ActivityPropertyReviewsBinding binding;
    private ReviewListViewModel viewModel;
    private ReviewAdapter adapter;
    private final List<ReviewResponse> items = new ArrayList<>();
    private Long propertyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPropertyReviewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        propertyId = getIntent().getLongExtra(EXTRA_PROPERTY_ID, -1);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new ReviewAdapter(items, false);
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviews.setAdapter(adapter);

        viewModel = new ViewModelProvider(this, new ReviewViewModelFactory(this))
                .get(ReviewListViewModel.class);

        viewModel.getSummaryState().observe(this, res -> {
            if (res.status == Resource.Status.SUCCESS && res.data != null) {
                binding.tvAvgRating.setText(String.format("%.1f", res.data.getAverageRating()));
                binding.rbAvg.setRating((float) res.data.getAverageRating());
                binding.tvTotalCount.setText(res.data.getTotalCount() + " đánh giá");
                renderDistribution(res.data.getDistribution(), res.data.getTotalCount());
            }
        });

        viewModel.getAccumulated().observe(this, list -> {
            items.clear();
            if (list != null) items.addAll(list);
            adapter.notifyDataSetChanged();
        });

        viewModel.getPageState().observe(this, res -> {
            ProgressBar p = binding.progressLoadMore;
            p.setVisibility(res.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);
        });

        if (propertyId > 0) viewModel.loadInitial(propertyId);
    }

    private void renderDistribution(Map<Integer, Long> dist, long total) {
        LinearLayout box = binding.layoutDistribution;
        box.removeAllViews();
        if (dist == null) return;
        for (int star = 5; star >= 1; star--) {
            long count = dist.getOrDefault(star, 0L);
            int pct = total > 0 ? (int) (count * 100 / total) : 0;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            int padV = dp(4);
            row.setPadding(0, padV, 0, padV);

            TextView label = new TextView(this);
            label.setText(star + "★");
            label.setTextSize(12);
            label.setTextColor(getResources().getColor(com.example.bookingapp.R.color.text_secondary, null));
            label.setWidth(dp(28));
            row.addView(label);

            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(6), 1f);
            lp.leftMargin = dp(6);
            lp.rightMargin = dp(6);
            bar.setLayoutParams(lp);
            bar.setMax(100);
            bar.setProgress(pct);
            row.addView(bar);

            TextView countTv = new TextView(this);
            countTv.setText(String.valueOf(count));
            countTv.setTextSize(12);
            countTv.setTextColor(getResources().getColor(com.example.bookingapp.R.color.text_hint, null));
            countTv.setWidth(dp(28));
            row.addView(countTv);

            box.addView(row, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
