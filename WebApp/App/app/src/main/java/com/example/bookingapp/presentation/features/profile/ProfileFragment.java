package com.example.bookingapp.presentation.features.profile;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.R;
import com.example.bookingapp.core.base.BaseFragment;
import com.example.bookingapp.data.model.user.UserResponse;
import com.example.bookingapp.databinding.FragmentProfileBinding;
import com.example.bookingapp.presentation.features.views.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    private ProfileViewModel viewModel;

    @Override
    protected Inflate<FragmentProfileBinding> getInflate() {
        return FragmentProfileBinding::inflate;
    }

    @Override
    protected void setupViews() {
        viewModel = new ViewModelProvider(this, new ProfileViewModelFactory(requireContext()))
                .get(ProfileViewModel.class);

        getBinding().rowBookingHistory.setOnClickListener(v -> {
            if (getActivity() == null) return;
            BottomNavigationView nav = getActivity().findViewById(R.id.bottomNavigation);
            if (nav != null) nav.setSelectedItemId(R.id.nav_booking);
        });

        getBinding().rowLogout.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Đăng xuất")
                        .setMessage("Đăng xuất khỏi tài khoản?")
                        .setPositiveButton("Đăng xuất", (d, w) -> logout())
                        .setNegativeButton("Hủy", null)
                        .show());

        getBinding().btnRelogin.setOnClickListener(v -> logout());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.loadMe();
    }

    @Override
    protected void observeViewModel() {
        viewModel.getMeState().observe(getViewLifecycleOwner(), res -> {
            switch (res.status) {
                case LOADING:
                    getBinding().progress.setVisibility(View.VISIBLE);
                    getBinding().layoutContent.setVisibility(View.GONE);
                    getBinding().layoutError.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    getBinding().progress.setVisibility(View.GONE);
                    if (res.data != null) {
                        bind(res.data);
                        getBinding().layoutContent.setVisibility(View.VISIBLE);
                        getBinding().layoutError.setVisibility(View.GONE);
                    }
                    break;
                case ERROR:
                    getBinding().progress.setVisibility(View.GONE);
                    String msg = res.message != null ? res.message : "";
                    if (msg.contains("401")) {
                        getBinding().layoutError.setVisibility(View.VISIBLE);
                        getBinding().layoutContent.setVisibility(View.GONE);
                    } else {
                        Snackbar.make(getBinding().getRoot(),
                                msg.isEmpty() ? "Lỗi tải thông tin" : msg,
                                Snackbar.LENGTH_LONG)
                                .setAction("Thử lại", v -> viewModel.loadMe())
                                .show();
                    }
                    break;
            }
        });
    }

    private void bind(UserResponse u) {
        String name = u.getFullName() != null && !u.getFullName().isEmpty() ? u.getFullName() : "Người dùng";
        getBinding().tvAvatar.setText(name.substring(0, 1).toUpperCase());
        getBinding().tvFullName.setText(name);
        getBinding().tvEmail.setText(u.getEmail() != null ? u.getEmail() : "");
//        getBinding().tvRole.setText(u.getRole() != null ? u.getRole() : "");

        getBinding().tvPhone.setText(u.getPhoneNumber() != null && !u.getPhoneNumber().isEmpty()
                ? u.getPhoneNumber() : "—");
        getBinding().tvEmailInfo.setText(u.getEmail() != null ? u.getEmail() : "—");
        getBinding().tvRoleInfo.setText(u.getRole() != null ? u.getRole() : "—");
    }

    private void logout() {
        requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
                .edit().remove("token").apply();
        Intent i = new Intent(requireContext(), LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        if (getActivity() != null) getActivity().finish();
        Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }
}
