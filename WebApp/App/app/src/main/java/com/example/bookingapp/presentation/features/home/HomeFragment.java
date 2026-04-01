package com.example.bookingapp.presentation.features.home;

import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookingapp.core.base.BaseFragment;
import com.example.bookingapp.data.model.views.Category;
import com.example.bookingapp.data.model.views.Homestay;
import com.example.bookingapp.data.model.views.PropertyResponse;
import com.example.bookingapp.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseFragment<FragmentHomeBinding> {
    private HomeViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    private HomestayAdapter homestayAdapter;
    private List<Category> categoryList = new ArrayList<>();
    private List<Homestay> homestayList = new ArrayList<>();

    @Override
    protected Inflate<FragmentHomeBinding> getInflate() {
        return FragmentHomeBinding::inflate;
    }

    @Override
    protected void setupViews() {

        viewModel = new ViewModelProvider(this, new HomeViewModelFactory(requireContext()))
                .get(HomeViewModel.class);
        viewModel.loadProperties();

        LinearLayoutManager categoryLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        getBinding().rvCategory.setLayoutManager(categoryLinearLayoutManager);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        getBinding().rvHomestay.setLayoutManager(gridLayoutManager);


        // Data mẫu category
        categoryList.add(new Category("🏖️", "Biển", true));
        categoryList.add(new Category("🏔️", "Núi", false));
        categoryList.add(new Category("🏙️", "Thành phố", false));
        categoryList.add(new Category("🏡", "Villa", false));
        categoryList.add(new Category("🌅", "View đẹp", false));

        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            // TODO: lọc homestay theo category sau
        });
        getBinding().rvCategory.setAdapter(categoryAdapter);

// Data mẫu homestay
        homestayList.add(new Homestay(null, "Homestay Đà Lạt", "Đà Lạt, Lâm Đồng", 850000.0, 4.8, false));
        homestayList.add(new Homestay(null, "Villa Hội An", "Hội An, Quảng Nam", 1200000.0, 4.9, false));
        homestayList.add(new Homestay(null, "Bungalow Phú Quốc", "Phú Quốc, Kiên Giang", 950000.0, 4.7, false));
        homestayList.add(new Homestay(null, "Nhà gỗ Sapa", "Sapa, Lào Cai", 700000.0, 4.6, false));

        homestayAdapter = new HomestayAdapter(homestayList, homestay -> {
            // TODO: mở màn chi tiết sau
        });
        getBinding().rvHomestay.setAdapter(homestayAdapter);

    }


        @Override
        protected void observeViewModel () {
            viewModel.getPropertiesState().observe(getViewLifecycleOwner(), resource -> {
                switch (resource.status) {
                    case LOADING:
                        // TODO: hiện progress bar sau
                        break;

                    case SUCCESS:
                        if (resource.data != null && resource.data.getContent() != null) {
                            homestayList.clear();
                            for (PropertyResponse p : resource.data.getContent()) {
                                homestayList.add(new Homestay(
                                        null,
                                        p.getPropertyName(),
                                        p.getCity() + ", " + p.getAddress(),
                                        p.getMinPrice() != null ? p.getMinPrice().doubleValue() : 0,
                                        0.0,
                                        false
                                ));
                            }
                            homestayAdapter.notifyDataSetChanged();
                        }
                        break;

                    case ERROR:
                        Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        }
    }
