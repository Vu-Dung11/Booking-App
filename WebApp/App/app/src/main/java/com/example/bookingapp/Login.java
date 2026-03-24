package com.example.bookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookingapp.databinding.ActivityLoginBinding;
import com.example.bookingapp.presentation.features.auth.AuthViewModel;
import com.example.bookingapp.presentation.features.auth.AuthViewModelFactory;

public class Login extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this, new AuthViewModelFactory())
                .get(AuthViewModel.class);

        setupViews();
        observeViewModel();
    }

    private void setupViews() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null
                    ? binding.etEmail.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null
                    ? binding.etPassword.getText().toString().trim() : "";

            if (email.isEmpty()) {
                binding.tilEmail.setError("Vui lòng nhập email");
                return;
            }
            if (password.isEmpty()) {
                binding.tilPassword.setError("Vui lòng nhập mật khẩu");
                return;
            }

            binding.tilEmail.setError(null);
            binding.tilPassword.setError(null);
            viewModel.login(email, password);
        });
    }

    private void observeViewModel() {
        viewModel.getLoginState().observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    binding.btnLogin.setEnabled(false);
                    binding.btnLogin.setText("Đang đăng nhập...");
                    break;

                case SUCCESS:
                    binding.btnLogin.setEnabled(true);
                    binding.btnLogin.setText(getString(R.string.btn_login));
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    break;

                case ERROR:
                    binding.btnLogin.setEnabled(true);
                    binding.btnLogin.setText(getString(R.string.btn_login));
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }
}
