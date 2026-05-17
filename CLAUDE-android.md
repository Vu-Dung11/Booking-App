# BookingApp Android — Hướng dẫn viết code

File này dùng cho Claude (và developer) khi làm việc với module Android tại
[`WebApp/App/`](WebApp/App/). Bổ sung cho [CLAUDE.md](CLAUDE.md) gốc của project —
phần backend, entities, API endpoints xem ở đó.

> Đọc kèm [CLAUDE.md](CLAUDE.md) để biết shape của API response, error codes,
> field name backend (đặc biệt `BookingRequest.checkIn/checkOut`, không phải
> `checkInDate/checkOutDate`).

---

## Stack & version

- **Language:** Java (không phải Kotlin — dù gradle có Kotlin plugin)
- **Min SDK:** 24, **Target/Compile SDK:** 36, **JVM target:** 11
- **Build:** Gradle 8.13 + AGP 8.13.2 (version catalog ở
  [gradle/libs.versions.toml](WebApp/App/gradle/libs.versions.toml))
- **Pattern:** MVVM thuần — ViewModel + LiveData + Repository, **không Hilt/Dagger**
- **DI:** Manual qua `*ViewModelFactory` (mỗi feature tự tạo)
- **HTTP:** Retrofit 2.11 + OkHttp 4.12 + Gson converter
- **UI:** Material Components 1.13 + ViewBinding (KHÔNG dùng DataBinding/Compose)
- **Async:** Retrofit `Call<T>.enqueue` (không dùng coroutines / RxJava)
- **Auth:** JWT lưu trong `SharedPreferences("auth")` key `"token"`, gắn vào
  request bởi [AuthInterceptor](WebApp/App/app/src/main/java/com/example/bookingapp/data/remote/AuthInterceptor.java).

Nếu task yêu cầu thêm dep mới (Glide, Room, EncryptedSharedPreferences, …), thêm
vào [build.gradle.kts](WebApp/App/app/build.gradle.kts) trực tiếp + version catalog
nếu là lib chính. Hỏi user trước khi thêm.

---

## Cấu trúc package

```
com.example.bookingapp/
├── MainActivity.java                       # Bottom-nav host + EXTRA_OPEN_TAB switch
├── core/
│   ├── base/BaseFragment.java              # Fragment<VB extends ViewBinding> abstract
│   └── utils/
│       ├── Resource.java                   # Resource<T> { status, data, message }
│       └── Formatter.java                  # currency VND, date dd/MM/yyyy ↔ yyyy-MM-dd
├── data/
│   ├── model/
│   │   ├── ApiResponse.java                # { status, message, data }
│   │   ├── PageResponse.java               # Page<T> shape của backend
│   │   ├── auth/                           # LoginRequest, RegisterRequest, AuthResponse
│   │   ├── booking/                        # Booking (+ nested Guest/Room/Property), BookingRequest
│   │   ├── payment/                        # PaymentUrlResponse
│   │   ├── review/                         # ReviewRequest
│   │   ├── user/                           # UserResponse
│   │   └── views/                          # PropertyResponse, PropertyDetailResponse,
│   │                                       # PropertySearchResponse, RoomResponse, Homestay, Category
│   ├── remote/
│   │   ├── ApiService.java                 # Tất cả endpoint Retrofit
│   │   ├── AuthInterceptor.java            # Gắn Bearer token
│   │   └── RetrofitClient.java             # singleton, BASE_URL = http://10.0.2.2:8080/
│   └── repository/
│       ├── AuthRepository.java
│       ├── BookingRepository.java
│       └── PropertyRepository.java
└── presentation/features/
    ├── auth/                               # AuthViewModel + Factory
    ├── booking/                            # BookingFragment, BookingAdapter,
    │                                       # BookingDetail/Create Activity + ViewModel + Factory
    ├── favorite/FavoriteFragment.java      # (chưa làm)
    ├── home/                               # HomeFragment, HomestayAdapter, CategoryAdapter,
    │                                       # HomeViewModel + Factory, PropertyDetailViewModel + Factory,
    │                                       # RoomAdapter
    ├── payment/PaymentActivity.java        # WebView VNPay
    ├── profile/ProfileFragment.java        # (chưa làm)
    └── views/                              # LoginActivity, RegisterActivity, PropertyDetailActivity
```

**Quy ước đặt tên file** đã có sẵn (giữ nguyên, không refactor):
- `*Activity` / `*Fragment` cho UI
- `*ViewModel` + `*ViewModelFactory` cho từng feature
- `*Adapter` cho RecyclerView
- `*Response` cho DTO từ server, `*Request` cho body gửi đi
- Model entity backend (Booking, …) đặt thẳng `data/model/<entity>/`

---

## Pattern bắt buộc

### 1. Resource<T> + LiveData cho mọi state bất đồng bộ

```java
private final MutableLiveData<Resource<Booking>> bookingState = new MutableLiveData<>();
public LiveData<Resource<Booking>> getBookingState() { return bookingState; }
```

Trong Activity/Fragment, **luôn switch 3 case** `LOADING / SUCCESS / ERROR`:

```java
viewModel.getBookingState().observe(this, res -> {
    switch (res.status) {
        case LOADING:
            adapter.submit(null);                  // CLEAR list cũ tránh flash
            binding.progress.setVisibility(View.VISIBLE);
            binding.emptyView.setVisibility(View.GONE);
            break;
        case SUCCESS:
            binding.progress.setVisibility(View.GONE);
            if (res.data != null && !res.data.isEmpty()) { /* bind */ }
            else { binding.emptyView.setVisibility(View.VISIBLE); }
            break;
        case ERROR:
            binding.progress.setVisibility(View.GONE);
            Snackbar.make(binding.getRoot(), res.message != null ? res.message : "Lỗi",
                    Snackbar.LENGTH_LONG).setAction("Thử lại", v -> viewModel.refresh()).show();
            break;
    }
});
```

### 2. Repository signature

Repository **nhận `MutableLiveData<Resource<T>>`** từ ViewModel rồi set value vào.
KHÔNG return `LiveData`/`Call`/`Observable`. Pattern:

```java
public void getBookings(String status, int page, int size,
                        MutableLiveData<Resource<PageResponse<Booking>>> state) {
    state.setValue(Resource.loading());
    apiService.getBookings(status, page, size).enqueue(new Callback<>() {
        @Override
        public void onResponse(Call<...> call, Response<...> response) {
            if (response.isSuccessful() && response.body() != null) {
                state.setValue(Resource.success(response.body().getData()));
            } else {
                state.setValue(Resource.error("Lỗi HTTP: " + response.code(), null));
            }
        }
        @Override
        public void onFailure(Call<...> call, Throwable t) {
            state.setValue(Resource.error(t.getLocalizedMessage(), null));
        }
    });
}
```

### 3. ViewModelFactory tự build dependency

KHÔNG có Hilt. Mỗi feature có Factory tự instantiate Repository:

```java
public class BookingViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public BookingViewModelFactory(Context context) {
        this.context = context.getApplicationContext();    // tránh leak Activity context
    }
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        BookingRepository repo = new BookingRepository(RetrofitClient.getApiService(context));
        if (modelClass.isAssignableFrom(BookingDetailViewModel.class)) {
            return (T) new BookingDetailViewModel(repo);
        }
        return (T) new BookingViewModel(repo);
    }
}
```

**1 Factory có thể tạo nhiều VM cùng feature** (xem [BookingViewModelFactory](WebApp/App/app/src/main/java/com/example/bookingapp/presentation/features/booking/BookingViewModelFactory.java))
— check `modelClass.isAssignableFrom(...)` để rẽ nhánh.

### 4. Fragment phải kế thừa BaseFragment<VB>

```java
public class BookingFragment extends BaseFragment<FragmentBookingBinding> {
    @Override protected Inflate<FragmentBookingBinding> getInflate() {
        return FragmentBookingBinding::inflate;
    }
    @Override protected void setupViews() { /* khởi tạo */ }
    @Override protected void observeViewModel() { /* observer LiveData */ }
}
```

`getBinding()` trả `VB` đã inflate; tự null-out trong `onDestroyView` (do BaseFragment lo).

### 5. ApiService — match đúng field name backend

Backend dùng Lombok `@Getter`/`@Setter` ⇒ Gson serialize theo tên field Java
**chữ thường**, KHÔNG snake_case. Đặc biệt cẩn thận:

| Backend (Java field) | Android model | Note |
|---|---|---|
| `BookingRequest.checkIn` / `checkOut` | dùng `checkIn` / `checkOut` | KHÔNG `checkInDate` |
| `SearchRequest.city/checkIn/checkOut/guests` | y hệt | |
| `Booking.checkInDate` / `checkOutDate` (response) | `checkInDate` / `checkOutDate` | request và response khác tên — đọc kỹ |

Khi thêm endpoint mới, đối chiếu file `src/main/java/com/example/bookingapp/form/*Request.java`
và `entity/*.java` trước khi viết model.

---

## Quy ước UI

### Theme & resources
- Color tokens ở [colors.xml](WebApp/App/app/src/main/res/values/colors.xml) — primary
  `#1A6FE8`, accent `#FF6B35`, text/bg theo Mixpanel-like palette.
- Status pill colors ở [colors_status.xml](WebApp/App/app/src/main/res/values/colors_status.xml).
- Drawable `bg_status_pill.xml` dùng `backgroundTint` để đổi màu pill từ code
  (xem `BookingAdapter.applyStatus`).

### Layout
- Dùng **MaterialCardView** (cornerRadius 12dp, elevation 2dp), KHÔNG `androidx.cardview.widget.CardView` cũ.
- Dùng **MaterialButton**: primary `app:backgroundTint="@color/primary"`, outlined
  với `style="@style/Widget.Material3.Button.OutlinedButton"` + `app:strokeColor`.
- **TabLayout** (Material) cho tab-style navigation trong fragment.
- **MaterialAutoCompleteTextView** trong `TextInputLayout` với style
  `Widget.Material3.TextInputLayout.OutlinedBox.ExposedDropdownMenu` cho dropdown.
- **MaterialDatePicker** cho chọn ngày — millis là **UTC midnight**, format ra
  string `yyyy-MM-dd` phải dùng `Formatter.toApiDate(millis)` (có TimeZone UTC).

### Format
- Tiền: `Formatter.currency(BigDecimal)` → `"1.234.567 đ"` (locale vi-VN).
- Ngày hiển thị: `Formatter.displayDate("2026-05-14")` → `"14/05/2026"`.
- Date gửi API: `Formatter.toApiDate(millis)` → `"2026-05-14"`.

### State chuẩn cho mọi screen async
- **Loading:** ProgressBar center, gọi `adapter.submit(null)` nếu là list.
- **Empty:** TextView centered + icon mờ, hiện khi data rỗng.
- **Error:** Snackbar long + action "Thử lại".

---

## Manifest & flow điều hướng

### Đăng ký Activity mới
Phải thêm vào [AndroidManifest.xml](WebApp/App/app/src/main/AndroidManifest.xml):

```xml
<activity android:name=".presentation.features.<feature>.<Name>Activity"
          android:exported="false" />
```

`exported="false"` cho mọi activity nội bộ. LoginActivity là LAUNCHER (`exported="true"`).

### Mở tab cụ thể từ Activity ngoài MainActivity

```java
Intent i = new Intent(this, MainActivity.class);
i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
i.putExtra(MainActivity.EXTRA_OPEN_TAB, MainActivity.TAB_BOOKING);
startActivity(i);
```

`MainActivity` xử lý ở cả `onCreate` và `onNewIntent`.

### Truyền dữ liệu giữa screen
- Dùng `Intent.putExtra` với primitive (`Long`, `String`) — KHÔNG Parcelable cho
  toàn entity. Ví dụ: chỉ truyền `propertyId`, screen tự fetch lại detail.
- Public constants `EXTRA_*` đặt trong Activity nhận (vd `PropertyDetailActivity.EXTRA_PROPERTY_ID`).

---

## Gotcha & lessons learned

### 2. Emulator network
- Backend Spring Boot `localhost:8080` ⇒ Android emulator gọi qua `10.0.2.2:8080`.
- Hard-coded trong [RetrofitClient.java](WebApp/App/app/src/main/java/com/example/bookingapp/data/remote/RetrofitClient.java).
  Khi test trên thiết bị thật, đổi BASE_URL hoặc dùng ngrok.
- `usesCleartextTraffic="true"` đã bật trong Manifest để cho phép HTTP (sandbox).

### 3. MaterialDatePicker timezone
Selection trả millis ở **UTC midnight**. Nếu format bằng SimpleDateFormat
default (locale TZ) thì sẽ bị lệch 1 ngày khi TZ ≠ UTC. Luôn dùng
`Formatter.toApiDate(millis)` đã set TimeZone UTC.

### 4. RecyclerView LayoutManager qua XML
Trong layout dùng:
```xml
app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
```
KHÔNG set `android:orientation` trên RecyclerView (không có thuộc tính đó cho
RecyclerView trực tiếp).

### 5. Booking-scoped vs admin-scoped endpoint
- `GET /api/v1/bookings/my` — guest xem đơn của chính mình.
- `GET /api/v1/bookings` — chỉ ADMIN (đã khoá).
- App **luôn dùng `/my`** trong [ApiService.getBookings](WebApp/App/app/src/main/java/com/example/bookingapp/data/remote/ApiService.java).

### 6. Tránh leak Activity context trong Factory
Factory nhận Context thì lưu `context.getApplicationContext()`. Repository chỉ
cần app context để init Retrofit.

### 7. Refresh khi quay lại fragment
BookingFragment override `onResume` gọi `viewModel.refresh()` để pick up trạng
thái mới (vd: đơn vừa được CONFIRMED qua VNPay return). Áp dụng tương tự cho
mọi screen có data thay đổi từ ngoài.

---

## Quy trình thêm 1 feature mới

Ví dụ thêm feature "Notifications":

1. **Models:** `data/model/notification/Notification.java` + `NotificationResponse.java`.
2. **API:** Thêm method vào [ApiService](WebApp/App/app/src/main/java/com/example/bookingapp/data/remote/ApiService.java) — đối chiếu backend Controller trước.
3. **Repository:** `data/repository/NotificationRepository.java` theo pattern
   nhận `MutableLiveData<Resource<T>>`.
4. **ViewModel + Factory:** `presentation/features/notification/`.
5. **Layout:** `fragment_notification.xml` + `item_notification.xml`, theo
   palette + Material components.
6. **Fragment/Activity:** extend `BaseFragment<VB>`; observe 3 state.
7. **Manifest:** đăng ký Activity mới (nếu có).
8. **Bottom nav menu** (nếu thêm tab): cập nhật `menu/bottom_nav_menu.xml` +
   `MainActivity.setupBottomNavigation`.
9. **Build:** `cd WebApp/App && ./gradlew assembleDebug` (Windows: `gradlew.bat`).

---

## Rule bắt buộc

- **KHÔNG dùng Kotlin** — Java thuần.
- **KHÔNG tạo file test** trừ khi được yêu cầu.
- **KHÔNG đổi BASE_URL** trong RetrofitClient mà không hỏi.
- **KHÔNG sửa application.properties backend** từ task Android.
- **KHÔNG thêm dep mới** (Hilt, Coroutines, Room, Glide, …) mà không hỏi user.
- Mọi async call → state qua `Resource<T>` + LiveData; **không** trả `Call<T>`
  ra ngoài Repository.
- Build phải pass `./gradlew assembleDebug` sau mỗi sub-task lớn; báo lại trước
  khi sang task kế tiếp khi user yêu cầu làm tuần tự.
- Code comment chỉ khi WHY non-obvious (workaround / spec quirk). Không comment
  "what" — tên biến/hàm phải đủ rõ.
- Commit message tiếng Anh (theo CLAUDE.md gốc).

---

## Tham chiếu nhanh — endpoint backend đang dùng

| Android caller | Method | Path | Auth |
|---|---|---|---|
| AuthRepository.login | POST | `/api/v1/auth/login` | public |
| AuthRepository.register | POST | `/api/v1/auth/register` | public |
| PropertyRepository.getAllProperties | GET | `/api/v1/properties?page=&size=` | public |
| PropertyRepository.getPropertyDetail | GET | `/api/v1/properties/{id}/detail` | public |
| ApiService.searchProperties | GET | `/api/v1/properties/search?city=&checkIn=&checkOut=&guests=` | public |
| ApiService.getUserById | GET | `/api/v1/users/{id}` | auth |
| BookingRepository.getBookings | GET | `/api/v1/bookings/my?status=&page=&size=` | auth |
| BookingRepository.getBookingById | GET | `/api/v1/bookings/{id}` | auth (owner/host/admin) |
| BookingRepository.createBooking | POST | `/api/v1/bookings` | GUEST |
| BookingRepository.completeBooking | POST | `/api/v1/bookings/{id}/booking-completed` | auth |
| BookingRepository.cancelBooking | POST | `/api/v1/bookings/{id}/cancel` | auth (owner, PENDING) |
| BookingRepository.getVnpayUrl | GET | `/api/v1/payments/vnpay-url?bookingId=` | public |
| ApiService.createReview | POST | `/api/v1/reviews` | auth |

Đầy đủ + DTO shape xem [CLAUDE.md § API Endpoints](CLAUDE.md).
