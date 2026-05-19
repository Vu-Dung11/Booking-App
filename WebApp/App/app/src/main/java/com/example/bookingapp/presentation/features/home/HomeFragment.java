package com.example.bookingapp.presentation.features.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bookingapp.R;
import com.example.bookingapp.core.base.BaseFragment;
import com.example.bookingapp.core.utils.Formatter;
import com.example.bookingapp.data.model.views.Category;
import com.example.bookingapp.data.model.views.Homestay;
import com.example.bookingapp.data.model.views.PropertyResponse;
import com.example.bookingapp.databinding.FragmentHomeBinding;
import com.example.bookingapp.presentation.features.favorite.FavoriteViewModel;
import com.example.bookingapp.presentation.features.favorite.FavoriteViewModelFactory;
import com.example.bookingapp.presentation.features.search.SearchResultActivity;
import com.example.bookingapp.presentation.features.search.SearchViewModel;
import com.example.bookingapp.presentation.features.search.SearchViewModelFactory;
import com.example.bookingapp.presentation.features.views.PropertyDetailActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends BaseFragment<FragmentHomeBinding> {
    private HomeViewModel viewModel;
    private SearchViewModel searchViewModel;
    private FavoriteViewModel favoriteViewModel;
    private CategoryAdapter categoryAdapter;
    private HomestayAdapter homestayAdapter;
    private final List<Category> categoryList = new ArrayList<>();
    private final List<Homestay> homestayList = new ArrayList<>();

    private String selectedCity;
    private long checkInMillis;
    private long checkOutMillis;
    private int guests = 1;

    private List<String> cachedCities = new ArrayList<>();

    private static final String PREF_SEARCH = "search_history";
    private static final String KEY_HISTORY = "history";
    private static final int HISTORY_MAX = 5;
    private static final List<String> SUGGESTED_CITIES =
            Arrays.asList("Đà Nẵng", "Hà Nội", "Hồ Chí Minh", "Đà Lạt", "Phú Quốc");

    @Override
    protected Inflate<FragmentHomeBinding> getInflate() {
        return FragmentHomeBinding::inflate;
    }

    @Override
    protected void setupViews() {
        viewModel = new ViewModelProvider(this, new HomeViewModelFactory(requireContext()))
                .get(HomeViewModel.class);
        viewModel.loadProperties();

        searchViewModel = new ViewModelProvider(this, new SearchViewModelFactory(requireContext()))
                .get(SearchViewModel.class);
        searchViewModel.loadCities();

        favoriteViewModel = new ViewModelProvider(this, new FavoriteViewModelFactory(requireContext()))
                .get(FavoriteViewModel.class);

        long today = MaterialDatePicker.todayInUtcMilliseconds();
        checkInMillis = today;
        checkOutMillis = today + TimeUnit.DAYS.toMillis(1);
        updateDateLabel();
        updateGuestLabel();

        LinearLayoutManager categoryLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        getBinding().rvCategory.setLayoutManager(categoryLinearLayoutManager);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        getBinding().rvHomestay.setLayoutManager(gridLayoutManager);

        categoryList.add(new Category("🏖️", "Biển", true));
        categoryList.add(new Category("🏔️", "Núi", false));
        categoryList.add(new Category("🏙️", "Thành phố", false));
        categoryList.add(new Category("🏡", "Villa", false));
        categoryList.add(new Category("🌅", "View đẹp", false));

        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            // TODO: lọc homestay theo category sau
        });
        getBinding().rvCategory.setAdapter(categoryAdapter);

        homestayAdapter = new HomestayAdapter(homestayList, homestay -> {
            if (homestay.getPropertyId() != null) {
                Intent intent = new Intent(getContext(), PropertyDetailActivity.class);
                intent.putExtra(PropertyDetailActivity.EXTRA_PROPERTY_ID, homestay.getPropertyId());
                startActivity(intent);
            }
        });
        homestayAdapter.setOnFavoriteToggle((homestay, wasFavorite) -> {
            if (homestay.getPropertyId() == null) return;
            favoriteViewModel.toggle(
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
        getBinding().rvHomestay.setAdapter(homestayAdapter);

        setupSuggestedChips();
        getBinding().layoutSearchLocation.setOnClickListener(v -> showCityPicker());
        getBinding().layoutSearchDate.setOnClickListener(v -> showDateRangePicker());
        getBinding().layoutSearchGuest.setOnClickListener(v -> showGuestSheet());
        getBinding().btnSearch.setOnClickListener(v -> performSearch());
    }

    private void showCityPicker() {
        List<String> recent = loadHistory();
        List<String> base = (cachedCities == null || cachedCities.isEmpty()) ? SUGGESTED_CITIES : cachedCities;

        List<String> items = new ArrayList<>();
        for (String r : recent) items.add("🕘  " + r);
        for (String c : base) items.add(c);

        if (items.isEmpty()) {
            Toast.makeText(getContext(), "Đang tải danh sách thành phố…", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] arr = items.toArray(new String[0]);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(recent.isEmpty() ? "Chọn địa điểm" : "Tìm gần đây & gợi ý")
                .setItems(arr, (dialog, which) -> {
                    String picked = arr[which];
                    if (picked.startsWith("🕘")) picked = picked.replace("🕘", "").trim();
                    selectedCity = picked;
                    getBinding().tvSearchLocation.setText(selectedCity);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupSuggestedChips() {
        getBinding().chipGroupCities.removeAllViews();
        for (String c : SUGGESTED_CITIES) {
            Chip chip = new Chip(requireContext());
            chip.setText(c);
            chip.setClickable(true);
            chip.setCheckable(false);
            chip.setChipBackgroundColorResource(android.R.color.white);
            chip.setOnClickListener(v -> {
                selectedCity = c;
                getBinding().tvSearchLocation.setText(c);
                performSearch();
            });
            getBinding().chipGroupCities.addView(chip);
        }
    }

    private SharedPreferences prefs() {
        return requireContext().getSharedPreferences(PREF_SEARCH, Context.MODE_PRIVATE);
    }

    private List<String> loadHistory() {
        String raw = prefs().getString(KEY_HISTORY, "");
        List<String> out = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return out;
        for (String s : raw.split("\\|\\|")) {
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    private void saveToHistory(String city) {
        if (city == null || city.isEmpty()) return;
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(city);
        set.addAll(loadHistory());
        List<String> trimmed = new ArrayList<>(set);
        if (trimmed.size() > HISTORY_MAX) trimmed = trimmed.subList(0, HISTORY_MAX);
        prefs().edit().putString(KEY_HISTORY, String.join("||", trimmed)).apply();
    }

    private void showDateRangePicker() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Chọn ngày nhận - trả phòng")
                .setSelection(new Pair<>(checkInMillis, checkOutMillis))
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
            }
        });
        picker.show(getParentFragmentManager(), "home_date_picker");
    }

    private void showGuestSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.sheet_guest_picker, (ViewGroup) getView(), false);
        TextView tvCount = view.findViewById(R.id.tvGuestCount);
        tvCount.setText(String.valueOf(guests));
        view.findViewById(R.id.btnMinus).setOnClickListener(v -> {
            if (guests > 1) {
                guests--;
                tvCount.setText(String.valueOf(guests));
            }
        });
        view.findViewById(R.id.btnPlus).setOnClickListener(v -> {
            guests++;
            tvCount.setText(String.valueOf(guests));
        });
        view.findViewById(R.id.btnDone).setOnClickListener(v -> {
            updateGuestLabel();
            sheet.dismiss();
        });
        sheet.setContentView(view);
        sheet.show();
    }

    private void updateDateLabel() {
        String in = Formatter.displayDate(Formatter.toApiDate(checkInMillis));
        String out = Formatter.displayDate(Formatter.toApiDate(checkOutMillis));
        getBinding().tvSearchDate.setText(in + " → " + out);
    }

    private void updateGuestLabel() {
        getBinding().tvSearchGuest.setText(guests + " khách");
    }

    private void performSearch() {
        if (selectedCity == null || selectedCity.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn địa điểm", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkOutMillis <= checkInMillis) {
            Toast.makeText(getContext(), "Ngày trả phòng phải sau ngày nhận", Toast.LENGTH_SHORT).show();
            return;
        }
        saveToHistory(selectedCity);
        Intent i = new Intent(getContext(), SearchResultActivity.class);
        i.putExtra(SearchResultActivity.EXTRA_CITY, selectedCity);
        i.putExtra(SearchResultActivity.EXTRA_CHECK_IN, Formatter.toApiDate(checkInMillis));
        i.putExtra(SearchResultActivity.EXTRA_CHECK_OUT, Formatter.toApiDate(checkOutMillis));
        i.putExtra(SearchResultActivity.EXTRA_GUESTS, guests);
        startActivity(i);
    }

    @Override
    protected void observeViewModel() {
        viewModel.getPropertiesState().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    Log.d("HOME", "Data: " + resource.data);
                    if (resource.data != null && resource.data.getContent() != null) {
                        homestayList.clear();
                        for (PropertyResponse p : resource.data.getContent()) {
                            homestayList.add(new Homestay(
                                    p.getPropertyId(),
                                    p.getThumbnailUrl(),
                                    p.getPropertyName(),
                                    p.getCity() + ", " + p.getAddress(),
                                    p.getMinPrice() != null ? p.getMinPrice().doubleValue() : 0,
                                    p.getAverageRating(),
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

        searchViewModel.getCitiesState().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == com.example.bookingapp.core.utils.Resource.Status.SUCCESS
                    && resource.data != null) {
                cachedCities = new ArrayList<>(resource.data);
            }
        });

        favoriteViewModel.getFavoriteIds().observe(getViewLifecycleOwner(), ids ->
                homestayAdapter.submitFavoriteIds(ids != null ? new HashSet<>(ids) : new HashSet<>()));
    }
}
