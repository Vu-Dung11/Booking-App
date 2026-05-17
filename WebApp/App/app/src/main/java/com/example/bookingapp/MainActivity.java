package com.example.bookingapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.bookingapp.databinding.ActivityMainBinding;
import com.example.bookingapp.presentation.features.booking.BookingFragment;
import com.example.bookingapp.presentation.features.favorite.FavoriteFragment;
import com.example.bookingapp.presentation.features.home.HomeFragment;
import com.example.bookingapp.presentation.features.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_TAB = "extra_open_tab";
    public static final String TAB_HOME = "home";
    public static final String TAB_FAVORITE = "favorite";
    public static final String TAB_BOOKING = "booking";
    public static final String TAB_PROFILE = "profile";

    private ActivityMainBinding binding;
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    // Khởi tạo sẵn 4 fragment, không tạo lại mỗi lần chuyển tab
    private final HomeFragment homeFragment = new HomeFragment();
    private final FavoriteFragment favoriteFragment = new FavoriteFragment();
    private final BookingFragment bookingFragment = new BookingFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupFragments();
        setupBottomNavigation();
        handleOpenTab(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleOpenTab(intent);
    }

    private void handleOpenTab(Intent intent) {
        if (intent == null) return;
        String tab = intent.getStringExtra(EXTRA_OPEN_TAB);
        if (tab == null) return;
        switch (tab) {
            case TAB_BOOKING:
                binding.bottomNavigation.setSelectedItemId(R.id.nav_booking);
                break;
            case TAB_FAVORITE:
                binding.bottomNavigation.setSelectedItemId(R.id.nav_favorite);
                break;
            case TAB_PROFILE:
                binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);
                break;
            case TAB_HOME:
            default:
                binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
                break;
        }
    }

    private void setupFragments() {
        // Add tất cả vào stack, chỉ show HomeFragment mặc định
        fragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, profileFragment).hide(profileFragment)
                .add(R.id.fragmentContainer, bookingFragment).hide(bookingFragment)
                .add(R.id.fragmentContainer, favoriteFragment).hide(favoriteFragment)
                .add(R.id.fragmentContainer, homeFragment)
                .commit();

        activeFragment = homeFragment;
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(homeFragment);
            } else if (id == R.id.nav_favorite) {
                switchFragment(favoriteFragment);
            } else if (id == R.id.nav_booking) {
                switchFragment(bookingFragment);
            } else if (id == R.id.nav_profile) {
                switchFragment(profileFragment);
            }
            return true;
        });
    }

    private void switchFragment(Fragment target) {
        if (target == activeFragment) return;
        fragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();
        activeFragment = target;
    }
}
