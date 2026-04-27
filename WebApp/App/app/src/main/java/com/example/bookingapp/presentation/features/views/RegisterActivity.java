package com.example.bookingapp.presentation.features.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.databinding.ActivityRegisterBinding;
import com.example.bookingapp.presentation.features.auth.AuthViewModel;
import com.example.bookingapp.presentation.features.auth.AuthViewModelFactory;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this, new AuthViewModelFactory(this))
                .get(AuthViewModel.class);

        setupViews();
        observeViewModel();
    }

    private void setupViews() {
        binding.btnRegister.setOnClickListener(v -> {
            String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
            String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

            if (fullName.isEmpty()) {
                binding.tilFullName.setError("Vui lòng nhập họ và tên");
                return;
            }
            if (email.isEmpty()) {
                binding.tilEmail.setError("Vui lòng nhập email");
                return;
            }
            if (password.isEmpty()) {
                binding.tilPassword.setError("Vui lòng nhập mật khẩu");
                return;
            }
            if (password.length() < 6) {
                binding.tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                return;
            }
            if (!password.equals(confirmPassword)) {
                binding.tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
                return;
            }

            binding.tilFullName.setError(null);
            binding.tilEmail.setError(null);
            binding.tilPassword.setError(null);
            binding.tilConfirmPassword.setError(null);

            viewModel.register(email, password, fullName, phone.isEmpty() ? null : phone);
        });

        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        viewModel.getRegisterState().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    binding.btnRegister.setEnabled(false);
                    binding.btnRegister.setText("Đang đăng ký...");
                    break;

                case SUCCESS:
                    binding.btnRegister.setEnabled(true);
                    binding.btnRegister.setText("Đăng ký");
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                    break;

                case ERROR:
                    binding.btnRegister.setEnabled(true);
                    binding.btnRegister.setText("Đăng ký");
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
}
