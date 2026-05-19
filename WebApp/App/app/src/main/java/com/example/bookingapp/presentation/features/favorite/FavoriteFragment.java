package com.example.bookingapp.presentation.features.favorite;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.bookingapp.R;
import com.example.bookingapp.core.base.BaseFragment;
import com.example.bookingapp.data.local.FavoriteProperty;
import com.example.bookingapp.data.model.views.Homestay;
import com.example.bookingapp.databinding.FragmentFavoriteBinding;
import com.example.bookingapp.presentation.features.home.HomestayAdapter;
import com.example.bookingapp.presentation.features.views.PropertyDetailActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FavoriteFragment extends BaseFragment<FragmentFavoriteBinding> {

    private FavoriteViewModel viewModel;
    private HomestayAdapter adapter;
    private final List<Homestay> items = new ArrayList<>();

    @Override
    protected Inflate<FragmentFavoriteBinding> getInflate() {
        return FragmentFavoriteBinding::inflate;
    }

    @Override
    protected void setupViews() {
        viewModel = new ViewModelProvider(this, new FavoriteViewModelFactory(requireContext()))
                .get(FavoriteViewModel.class);

        adapter = new HomestayAdapter(items, homestay -> {
            if (homestay.getPropertyId() != null) {
                Intent i = new Intent(getContext(), PropertyDetailActivity.class);
                i.putExtra(PropertyDetailActivity.EXTRA_PROPERTY_ID, homestay.getPropertyId());
                startActivity(i);
            }
        });
        adapter.setOnFavoriteToggle((homestay, wasFavorite) -> {
            if (homestay.getPropertyId() == null) return;
            viewModel.toggle(
                    homestay.getPropertyId(),
                    homestay.getName(),
                    null,
                    homestay.getLocation(),
                    isAdded -> {
                        if (getContext() == null) return;
                        getBinding().getRoot().post(() ->
                                Toast.makeText(getContext(),
                                        isAdded ? "Đã lưu" : "Đã bỏ yêu thích",
                                        Toast.LENGTH_SHORT).show());
                    });
        });

        getBinding().rvFavorites.setLayoutManager(new GridLayoutManager(getContext(), 2));
        getBinding().rvFavorites.setAdapter(adapter);

        getBinding().btnExplore.setOnClickListener(v -> {
            if (getActivity() == null) return;
            BottomNavigationView nav = getActivity().findViewById(R.id.bottomNavigation);
            if (nav != null) nav.setSelectedItemId(R.id.nav_home);
        });
    }

    @Override
    protected void observeViewModel() {
        viewModel.getFavorites().observe(getViewLifecycleOwner(), list -> {
            items.clear();
            HashSet<Long> ids = new HashSet<>();
            if (list != null) {
                for (FavoriteProperty f : list) {
                    items.add(new Homestay(
                            f.getPropertyId(),
                            f.getThumbnailUrl(),
                            f.getName(),
                            f.getCity() != null ? f.getCity() : "",
                            0.0,
                            0.0,
                            true
                    ));
                    ids.add(f.getPropertyId());
                }
            }
            adapter.submitFavoriteIds(ids);
            adapter.notifyDataSetChanged();

            boolean empty = items.isEmpty();
            getBinding().layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            getBinding().rvFavorites.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }
}
