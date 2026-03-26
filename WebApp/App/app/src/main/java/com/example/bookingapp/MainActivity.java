package com.example.bookingapp;

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
