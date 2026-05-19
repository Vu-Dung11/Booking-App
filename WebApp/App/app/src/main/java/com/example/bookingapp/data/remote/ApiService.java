package com.example.bookingapp.data.remote;

import com.example.bookingapp.data.model.ApiResponse;
import com.example.bookingapp.data.model.PageResponse;
import com.example.bookingapp.data.model.auth.AuthResponse;
import com.example.bookingapp.data.model.auth.LoginRequest;
import com.example.bookingapp.data.model.auth.RegisterRequest;
import com.example.bookingapp.data.model.booking.Booking;
import com.example.bookingapp.data.model.booking.BookingRequest;
import com.example.bookingapp.data.model.review.ReviewRequest;
import com.example.bookingapp.data.model.review.ReviewResponse;
import com.example.bookingapp.data.model.review.ReviewSummaryResponse;
import com.example.bookingapp.data.model.user.UserResponse;
import com.example.bookingapp.data.model.views.PropertyDetailResponse;
import com.example.bookingapp.data.model.views.PropertyResponse;
import com.example.bookingapp.data.model.views.PropertySearchResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // ===== Auth =====
    @POST("api/v1/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @POST("api/v1/auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);

    // ===== Properties =====
    @GET("api/v1/properties")
    Call<ApiResponse<PageResponse<PropertyResponse>>> getAllProperties(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v1/properties/{id}/detail")
    Call<ApiResponse<PropertyDetailResponse>> getPropertyDetail(
            @Path("id") Long id,
            @Query("checkIn") String checkIn,
            @Query("checkOut") String checkOut,
            @Query("guests") Integer guests
    );

    @GET("api/v1/properties/{id}")
    Call<ApiResponse<Object>> getPropertyById(@Path("id") Long id);

    @GET("api/v1/properties/cities")
    Call<ApiResponse<List<String>>> getCities();

    @GET("api/v1/rooms/{id}/images")
    Call<ApiResponse<List<String>>> getRoomImages(@Path("id") Long id);

    @GET("api/v1/properties/search")
    Call<ApiResponse<List<PropertySearchResponse>>> searchProperties(
            @Query("city") String city,
            @Query("checkIn") String checkIn,
            @Query("checkOut") String checkOut,
            @Query("guests") Integer guests
    );

    // ===== Users =====
    @GET("api/v1/users/me")
    Call<ApiResponse<UserResponse>> getMe();

    @GET("api/v1/users/{id}")
    Call<ApiResponse<UserResponse>> getUserById(@Path("id") Long id);

    // ===== Bookings =====
    /** Guest xem các đơn của chính mình. */
    @GET("api/v1/bookings/my")
    Call<ApiResponse<PageResponse<Booking>>> getBookings(
            @Query("status") String status,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v1/bookings/{id}")
    Call<ApiResponse<Booking>> getBookingById(@Path("id") Long id);

    @POST("api/v1/bookings")
    Call<ApiResponse<Booking>> createBooking(@Body BookingRequest request);

    @POST("api/v1/bookings/{id}/booking-completed")
    Call<ApiResponse<String>> completeBooking(@Path("id") Long id);

    @POST("api/v1/bookings/{id}/cancel")
    Call<ApiResponse<Booking>> cancelBooking(@Path("id") Long id);

    // ===== Payments =====
    @GET("api/v1/payments/vnpay-url")
    Call<ApiResponse<String>> getVnpayUrl(@Query("bookingId") Long bookingId);

    @POST("api/v1/payments/callback")
    Call<ApiResponse<String>> paymentCallback(@Body com.example.bookingapp.data.model.payment.PaymentCallbackRequest request);

    // ===== Reviews =====
    @POST("api/v1/reviews")
    Call<ApiResponse<String>> createReview(@Body ReviewRequest request);

    @GET("api/v1/properties/{id}/reviews")
    Call<ApiResponse<PageResponse<ReviewResponse>>> getReviewsByProperty(
            @Path("id") Long id,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v1/properties/{id}/reviews/summary")
    Call<ApiResponse<ReviewSummaryResponse>> getReviewSummary(@Path("id") Long id);

    @GET("api/v1/reviews/check")
    Call<ApiResponse<Boolean>> checkReviewExists(@Query("bookingId") Long bookingId);
}
