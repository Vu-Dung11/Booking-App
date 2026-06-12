package com.example.bookingapp.data.remote;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.bookingapp.presentation.features.views.LoginActivity;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class AuthInterceptor implements Interceptor {
    private final Context context;

    // Tranh kich hoat dang xuat nhieu lan khi co nhieu request 401 cung luc
    private static final AtomicBoolean handlingUnauthorized = new AtomicBoolean(false);

    public AuthInterceptor(Context context) {
        // Dung applicationContext de tranh memory leak va de start Activity tu nen
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String token = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("token", null);

        Request request = chain.request();
        if (token != null) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }

        Response response = chain.proceed(request);

        // Token het han / khong hop le -> tu dong dang xuat ve man hinh dang nhap.
        // Backend (Spring Security) tra 403 cho request anonymous khi token het han,
        // hoac 401 neu da cau hinh AuthenticationEntryPoint -> bat ca hai.
        // Bo qua cac endpoint auth (login/register) vi loi o day la sai thong tin dang nhap.
        if ((response.code() == 401 || response.code() == 403)
                && token != null && !isAuthEndpoint(request)) {
            handleUnauthorized();
        }

        return response;
    }

    private boolean isAuthEndpoint(Request request) {
        return request.url().encodedPath().contains("/api/v1/auth/");
    }

    private void handleUnauthorized() {
        // Chi xu ly 1 lan cho moi dot het han token
        if (!handlingUnauthorized.compareAndSet(false, true)) {
            return;
        }

        // Xoa token cu
        context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                .edit().remove("token").apply();

        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context,
                    "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại",
                    Toast.LENGTH_LONG).show();

            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("session_expired", true);
            context.startActivity(intent);

            // Mo khoa sau 2s de cho phep xu ly cho phien dang nhap tiep theo
            new Handler(Looper.getMainLooper()).postDelayed(
                    () -> handlingUnauthorized.set(false), 2000);
        });
    }
}
