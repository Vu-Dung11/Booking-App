package com.example.bookingapp.data.model.auth;

public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;

    public RegisterRequest(String email, String password, String fullName, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }
}
