# BÁO CÁO ĐỒ ÁN TỐT NGHIỆP
## HỆ THỐNG QUẢN LÝ HOMESTAY BOOKINGAPP

---

## 1. TỔNG QUAN DỰ ÁN

### 1.1 Tên đề tài
**Xây dựng hệ thống đặt phòng homestay – BookingApp**

### 1.2 Mô tả
BookingApp là hệ thống quản lý và đặt phòng homestay trực tuyến, hỗ trợ ba nhóm người dùng:
- **Admin**: Quản trị toàn hệ thống
- **Host (Chủ homestay)**: Quản lý property, phòng, đặt phòng, doanh thu
- **Guest (Khách)**: Tìm kiếm, đặt phòng và thanh toán online

Hệ thống gồm 3 nền tảng:
1. **Backend REST API** – Spring Boot (Java)
2. **Web Admin Panel** – Angular (quản trị cho Admin/Host)
3. **Mobile App** – Android (dành cho khách đặt phòng)

---

## 2. CÔNG NGHỆ SỬ DỤNG

### 2.1 Backend
| Thành phần | Công nghệ |
|---|---|
| Framework | Spring Boot 4.0.3 |
| Ngôn ngữ | Java 17 |
| Cơ sở dữ liệu | MySQL 8 (schema: `booking_app`) |
| ORM | Spring Data JPA + Hibernate |
| Migration | Flyway (tích hợp, tạm thời tắt) |
| Bộ nhớ đệm | Redis |
| Xác thực | JWT (jjwt 0.11.5) + Spring Security |
| Thanh toán | VNPay (sandbox) |
| Lưu trữ ảnh | Cloudinary |
| Tài liệu API | SpringDoc OpenAPI (Swagger UI) |
| Build tool | Maven Wrapper (mvnw) |
| Tiện ích | Lombok, BCrypt |

### 2.2 Frontend Web (Admin Panel)
| Thành phần | Công nghệ |
|---|---|
| Framework | Angular 21 |
| Ngôn ngữ | TypeScript 5.9 (strict mode) |
| Kiến trúc | Standalone Components, SSR-ready |
| Testing | Vitest |
| Formatter | Prettier |
| Package manager | npm 11 |

### 2.3 Mobile App (Android)
| Thành phần | Công nghệ |
|---|---|
| Platform | Android |
| Ngôn ngữ | Java |
| Kiến trúc | MVVM |
| HTTP Client | Retrofit2 |
| Build | Gradle (Kotlin DSL) |

---

## 3. KIẾN TRÚC HỆ THỐNG

### 3.1 Mô hình tổng thể
```
[Android App] ──┐
                ├──► [Spring Boot REST API] ──► [MySQL DB]
[Angular Web]  ──┘         │                        │
                            ├──► [Redis Cache]       │
                            ├──► [Cloudinary CDN]    │
                            └──► [VNPay Gateway]     │
```

### 3.2 Cấu trúc thư mục Backend
```
src/main/java/com/example/bookingapp/
├── configuration/
│   ├── security/      # JWT Filter, Security Config, Swagger
│   ├── exception/     # AppException, GlobalExceptionHandler
│   ├── enm/           # ErrorCode enum
│   └── utils/         # JwtUtil, SecurityUtils, VNPayUtil
├── controller/        # 13 REST Controllers
├── service/           # 11 Business Logic Services
├── repository/        # 9 Spring Data JPA Repositories
├── entity/            # 9 JPA Entities
├── dto/               # Response DTOs
└── form/              # Request DTOs
```

---

## 4. CƠ SỞ DỮ LIỆU

### 4.1 Sơ đồ thực thể (ERD)

Hệ thống gồm **9 bảng** chính:

### 4.2 Chi tiết các Entity

#### User
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long | PK, auto-increment |
| email | String | Unique, not null |
| password | String | BCrypt hash |
| fullName | String | Họ tên |
| phoneNumber | String | Số điện thoại |
| role | Enum | `ADMIN`, `GUEST`, `HOST` |

#### Property (Homestay)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long | PK |
| host | User | FK → host_id |
| name | String | Tên homestay |
| description | String | Mô tả (TEXT) |
| address | String | Địa chỉ |
| city | String | Thành phố |
| country | String | Quốc gia |
| isActive | Boolean | Trạng thái hoạt động |

#### Room (Phòng)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long | PK |
| property | Property | FK → property_id |
| roomType | String | Loại phòng |
| capacity | Integer | Sức chứa (người) |
| basePrice | BigDecimal | Giá cơ bản/đêm |
| quantity | Integer | Tổng số phòng vật lý |

#### RoomInventory (Kho phòng theo ngày)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long | PK |
| room | Room | FK |
| inventoryDate | LocalDate | Ngày cụ thể |
| availableCount | Integer | Số phòng còn trống |

#### Booking (Đặt phòng)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long | PK |
| guest | User | FK → guest_id |
| room | Room | FK → room_id |
| checkInDate | LocalDate | Ngày nhận phòng |
| checkOutDate | LocalDate | Ngày trả phòng |
| totalPrice | BigDecimal | Tổng tiền |
| roomQuantity | Integer | Số phòng đặt |
| status | Enum | `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED` |
| createdAt | LocalDateTime | Thời điểm tạo |

#### Payment (Thanh toán)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long | PK |
| booking | Booking | OneToOne FK |
| amount | BigDecimal | Số tiền |
| paymentMethod | String | VNPay, CASH, ... |
| status | Enum | `PENDING`, `SUCCESS`, `FAILED`, `REFUNDED` |
| transactionId | String | Mã giao dịch từ cổng thanh toán |
| createdAt | LocalDateTime | Thời điểm thanh toán |

#### Review (Đánh giá)
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long | PK |
| booking | Booking | OneToOne FK |
| property | Property | FK |
| rating | Integer | Điểm (1-5) |
| comment | String | Nhận xét |
| createdAt | LocalDateTime | Thời điểm đánh giá |

#### PropertyImage / RoomImage
| Trường | Kiểu | Mô tả |
|---|---|---|
| id | Long | PK |
| property/room | FK | Liên kết |
| imageUrl | String | URL Cloudinary |
| isThumbnail | Boolean | Ảnh đại diện |

---

## 5. CHỨC NĂNG HỆ THỐNG

### 5.1 Quản lý xác thực (Authentication)
- Đăng ký tài khoản (Guest/Host)
- Đăng nhập, cấp phát JWT Token
- Mã hóa mật khẩu BCrypt
- Xác thực stateless qua JWT Filter

### 5.2 Quản lý Homestay (Host)
- CRUD Property (tạo, xem, sửa, xóa, bật/tắt hoạt động)
- CRUD Room trong từng Property
- Upload/xóa ảnh Property và Room lên Cloudinary
- Đặt ảnh thumbnail cho Property/Room

### 5.3 Quản lý Kho Phòng (Inventory)
- Tự động tạo inventory 90 ngày khi Host tạo phòng mới
- Cron Job hàng ngày (00:00) bổ sung ngày thứ 90
- Host xem calendar theo property (tối đa 90 ngày/query)
- Host cập nhật bulk số phòng trống theo khoảng ngày
- Host mở thêm inventory đến ngày tùy chọn (tối đa 1 năm)
- Drill-down xem danh sách booking của 1 phòng theo ngày

### 5.4 Tìm kiếm Homestay (Guest)
- Tìm kiếm theo: thành phố, ngày check-in/check-out, số khách
- Thuật toán: lọc phòng có inventory đủ số đêm và số lượng > 0
- Trả về danh sách Property kèm phòng phù hợp và giá thấp nhất

### 5.5 Đặt phòng (Booking)
- Khách tạo đơn đặt phòng → trạng thái PENDING
- Sử dụng **Pessimistic Lock** (SELECT FOR UPDATE) trên inventory để tránh race condition khi nhiều người đặt cùng lúc
- Trừ số phòng trong inventory ngay khi tạo đơn
- Lưu key Redis với TTL 15 phút để timeout tự động
- Cron Job mỗi 5 phút quét và hủy đơn PENDING quá 15 phút (dùng self-injection để @Transactional REQUIRES_NEW hoạt động đúng)
- Cron Job hàng ngày (14:00) tự động chuyển đơn CONFIRMED sang COMPLETED sau checkout

### 5.6 Thanh toán (Payment)
- Tích hợp **VNPay** sandbox: tạo URL thanh toán, xử lý callback return
- Kiểm tra chữ ký HMAC-SHA512 để xác thực callback từ VNPay
- Host xác nhận thanh toán thủ công (offline): CASH hoặc chuyển khoản
- Hủy booking: hoàn trả phòng vào inventory, đánh dấu REFUNDED nếu đã có payment SUCCESS

### 5.7 Đánh giá (Review)
- Chỉ khách của đơn đặt phòng COMPLETED mới được đánh giá
- Mỗi đơn chỉ được đánh giá 1 lần
- Property_id tự động lấy từ Booking (chống giả mạo)
- Host xem tất cả đánh giá của các property thuộc mình

### 5.8 Dashboard Host
- Thống kê: tổng property, property đang hoạt động
- Thống kê booking theo trạng thái (PENDING/CONFIRMED/COMPLETED/CANCELLED)
- Tổng doanh thu (tính từ booking CONFIRMED + COMPLETED)
- Tổng đánh giá và điểm trung bình

### 5.9 Quản lý Media
- Upload ảnh đơn lẻ lên Cloudinary (endpoint public)
- Upload nhiều ảnh cùng lúc cho Property/Room
- Xóa ảnh: xóa trên Cloudinary trước, sau đó xóa trong DB

---

## 6. API ENDPOINTS

### 6.1 Định dạng Response chuẩn
```json
{
  "code": 0,
  "message": "Success",
  "data": <T>
}
```

### 6.2 Danh sách API

#### Authentication – `/api/v1/auth` (PUBLIC)
| Method | Path | Mô tả |
|---|---|---|
| POST | `/register` | Đăng ký tài khoản |
| POST | `/login` | Đăng nhập, nhận JWT |

#### Properties – `/api/v1/properties` (PUBLIC GET)
| Method | Path | Mô tả |
|---|---|---|
| GET | `/` | Danh sách homestay (phân trang) |
| GET | `/{id}` | Chi tiết homestay |
| GET | `/{id}/detail` | Chi tiết kèm rooms |
| GET | `/search` | Tìm kiếm theo ngày/city/guests |

#### Host Portal – `/api/v1/host/properties` (HOST only)
| Method | Path | Mô tả |
|---|---|---|
| GET | `/` | Danh sách property của host |
| POST | `/` | Tạo property mới |
| PUT | `/{id}` | Cập nhật property |
| PATCH | `/{id}/deactivate` | Tắt hoạt động |
| PATCH | `/{id}/activate` | Bật hoạt động |
| DELETE | `/{id}` | Xóa property |
| POST | `/{id}/rooms` | Thêm phòng |
| PUT | `/{pid}/rooms/{rid}` | Sửa phòng |
| DELETE | `/{pid}/rooms/{rid}` | Xóa phòng |
| POST | `/{id}/images` | Upload ảnh property |
| DELETE | `/{pid}/images/{imgId}` | Xóa ảnh |
| PATCH | `/{pid}/images/{imgId}/thumbnail` | Đặt thumbnail |
| POST | `/{pid}/rooms/{rid}/images` | Upload ảnh phòng |
| GET | `/{pid}/calendar` | Xem calendar inventory |
| PATCH | `/{pid}/rooms/{rid}/inventory` | Bulk update inventory |
| POST | `/{pid}/rooms/{rid}/inventory/extend` | Mở thêm inventory |
| GET | `/{pid}/rooms/{rid}/bookings/by-date` | Booking theo ngày |

#### Host Bookings – `/api/v1/host/bookings` (HOST only)
| Method | Path | Mô tả |
|---|---|---|
| GET | `/` | Danh sách booking của host |
| GET | `/{id}` | Chi tiết booking |
| POST | `/{id}/confirm` | Xác nhận thanh toán offline |
| POST | `/{id}/cancel` | Hủy booking |

#### Bookings – `/api/v1/bookings` (AUTHENTICATED)
| Method | Path | Mô tả |
|---|---|---|
| GET | `/` | Danh sách booking (có lọc status) |
| GET | `/{id}` | Chi tiết booking |
| POST | `/` | Tạo đơn đặt phòng (GUEST) |
| POST | `/{id}/booking-completed` | Xác nhận checkout |

#### Payments – `/api/v1/payments` (PUBLIC)
| Method | Path | Mô tả |
|---|---|---|
| GET | `/vnpay-url` | Tạo URL thanh toán VNPay |
| GET | `/vnpay-return` | Callback từ VNPay |
| POST | `/callback` | Xử lý notification thanh toán |

#### Reviews – `/api/v1/reviews` (AUTHENTICATED)
| Method | Path | Mô tả |
|---|---|---|
| POST | `/` | Tạo đánh giá |

#### Media – `/api/v1/media` (PUBLIC)
| Method | Path | Mô tả |
|---|---|---|
| POST | `/upload` | Upload ảnh đơn lẻ |

---

## 7. BẢO MẬT HỆ THỐNG

### 7.1 Cơ chế xác thực
- **JWT (JSON Web Token)**: Stateless, không lưu session
- Token được gửi qua HTTP Header: `Authorization: Bearer <token>`
- `JwtAuthenticationFilter` chạy trước mọi request để xác thực

### 7.2 Phân quyền
- `GUEST`: Tìm kiếm, đặt phòng, thanh toán, đánh giá
- `HOST`: Quản lý property/room/inventory/booking
- `ADMIN`: Quản lý toàn bộ người dùng và hệ thống
- Sử dụng `@PreAuthorize("hasRole('HOST')")` tại Controller

### 7.3 CORS
- Cho phép origin: `http://localhost:4200` (Angular dev)
- Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD

### 7.4 Mã hóa mật khẩu
- BCryptPasswordEncoder

---

## 8. CÁC KỸ THUẬT NỔI BẬT

### 8.1 Pessimistic Lock – Tránh Race Condition
Khi nhiều khách đặt cùng phòng cùng lúc, hệ thống dùng `SELECT ... FOR UPDATE` (Pessimistic Lock) khóa các bản ghi `RoomInventory` trong suốt transaction. Luồng khác phải chờ đến khi transaction commit và nhả khóa.

```java
// RoomInventoryRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
List<RoomInventory> findAndLockInventoryByRoomAndDates(Long roomId, LocalDate checkIn, LocalDate checkOut);
```

### 8.2 Redis TTL – Timeout Đặt Phòng
Sau khi tạo đơn PENDING, hệ thống lưu key `booking:timeout:{id}` vào Redis với TTL 15 phút. Cron Job mỗi 5 phút quét DB tìm đơn PENDING quá 15 phút và hủy, đồng thời hoàn trả phòng vào inventory.

### 8.3 Self-Injection – @Transactional REQUIRES_NEW
Để tránh self-invocation khiến `@Transactional` bị bỏ qua, `BookingService` inject chính nó qua Spring Proxy:
```java
@Lazy @Autowired
private BookingService self;
```
Khi `runCleanup()` gọi `self.cleanupExpiredBookings()`, Spring Proxy đảm bảo transaction mới được tạo.

### 8.4 Cron Jobs
| Job | Lịch | Chức năng |
|---|---|---|
| `runCleanup` | Mỗi 5 phút | Hủy đơn PENDING quá hạn |
| `runAutoCheckout` | 14:00 hàng ngày | Tự động COMPLETED |
| `maintainDailyInventory` | 00:00 hàng ngày | Bổ sung inventory ngày mới |

### 8.5 Tích hợp VNPay
- Tạo URL thanh toán với tham số chuẩn VNPay, ký HMAC-SHA512
- Mã hóa bookingId vào `vnp_TxnRef` để nhận diện đơn khi callback
- Xác thực chữ ký callback để chống giả mạo
- Hết hạn thanh toán khớp với Redis TTL (15 phút)

### 8.6 Tích hợp Cloudinary
- Upload ảnh multipart vào folder theo loại (properties, rooms)
- Lưu `imageUrl` (secure_url) và `publicId` vào DB
- Xóa ảnh: gọi Cloudinary API trước, sau đó xóa DB

---

## 9. FRONTEND WEB (ANGULAR)

### 9.1 Cấu trúc
```
WebApp/Web/booking-web/src/app/
├── auth/
│   └── login/              # Trang đăng nhập HOST
├── core/
│   ├── guards/             # authGuard, roleGuard('HOST')
│   ├── interceptors/       # JWT Auth Interceptor
│   └── services/           # AuthService (JWT decode, login, logout)
├── admin/
│   ├── admin-layout/       # Sidebar + header layout wrapper
│   ├── dashboard/          # Tổng quan thống kê + bookings gần đây
│   ├── properties/
│   │   ├── property-list/  # Danh sách homestay
│   │   ├── property-detail/# Chi tiết + quản lý rooms + upload ảnh
│   │   └── calendar/       # Inventory calendar view
│   ├── bookings/
│   │   ├── booking-list/   # Danh sách đặt phòng, lọc theo status
│   │   └── booking-detail/ # Chi tiết đặt phòng, xác nhận/hủy
│   ├── payments/
│   │   └── payment-list/   # Danh sách thanh toán
│   └── reviews/
│       └── review-list/    # Danh sách đánh giá
└── shared/
    ├── directives/          # AnimateOnScrollDirective
    ├── models/              # ApiResponse, Page<T>
    └── services/            # ToastService
```

### 9.2 Routes (thực tế hiện tại)
```
/login                      → LoginComponent (PUBLIC)
/host                       → AdminLayoutComponent (canActivate: authGuard + roleGuard('HOST'))
  /dashboard                → DashboardComponent
  /properties               → PropertyListComponent
  /properties/:id           → PropertyDetailComponent
  /properties/:id/calendar  → CalendarComponent
  /bookings                 → BookingListComponent
  /bookings/:id             → BookingDetailComponent
  /payments                 → PaymentListComponent
  /reviews                  → ReviewListComponent
  (default)                 → redirect /dashboard
/admin                      → redirect /host  (backwards-compat)
/                           → redirect /login
```

### 9.3 Trạng thái tích hợp API (thực tế)
| Trang | Kết nối API | Tính năng |
|---|---|---|
| Login | ✅ | Gọi `/auth/login`, decode JWT, kiểm tra role HOST |
| Dashboard | ✅ | Gọi `DashboardService` (stats) + `BookingService` (5 booking gần nhất) |
| Danh sách Properties | ✅ | Phân trang, CRUD, bật/tắt hoạt động |
| Chi tiết Property | ✅ | Quản lý rooms, upload/xóa ảnh, đặt thumbnail |
| Calendar Inventory | ✅ | Xem/cập nhật số phòng theo ngày, mở thêm inventory |
| Danh sách Bookings | ✅ | Phân trang, lọc theo status, xác nhận/hủy inline |
| Chi tiết Booking | ✅ | Xem contact khách, lịch sử payment, TTL còn lại |
| Payments | ✅ | Xem booking CONFIRMED/COMPLETED (lọc thanh toán) |
| Reviews | ✅ | Xem đánh giá của tất cả property thuộc host |

### 9.4 Bảo mật Frontend
- **AuthGuard**: Kiểm tra token còn hạn, redirect `/login` nếu chưa đăng nhập
- **RoleGuard**: Chỉ cho phép role `HOST` vào `/host/**`, từ chối GUEST/ADMIN
- **JWT Interceptor**: Tự động gắn `Authorization: Bearer <token>` vào mọi HTTP request
- **Token lưu trữ**: `localStorage` (với kiểm tra `isPlatformBrowser` cho SSR)
- **Auto logout**: Xóa token và redirect về login khi hết hạn

### 9.5 Design System
- **Phong cách**: Mixpanel – tối giản, hiện đại, chuyên nghiệp
- **Font**: Inter (Google Fonts)
- **Màu chính**: `#7C5CFC` (tím), nền trắng `#FFFFFF`
- **Component**: Cards bo góc 12px, bảng không border ngoài, badge trạng thái pill-shape
- **Toast Notification**: Thông báo thành công/lỗi sau các thao tác
- **Animation**: Fade-in + slide-up khi scroll (IntersectionObserver directive)
- **Responsive**: Mobile (<768px) sidebar ẩn, Tablet (768-1024px) sidebar icon-only, Desktop (>1024px) full sidebar

---

## 10. MOBILE APP (ANDROID)

### 10.1 Kiến trúc MVVM
```
presentation/features/
├── auth/      # Đăng nhập, đăng ký
├── home/      # Trang chủ, danh sách homestay
├── booking/   # Đặt phòng
├── favorite/  # Yêu thích
├── profile/   # Thông tin cá nhân
└── views/     # Custom views
```

### 10.2 Kết nối API
- **Retrofit2**: HTTP client
- **AuthInterceptor**: Tự động thêm JWT vào header
- **RetrofitClient**: Singleton pattern với base URL

### 10.3 Tính năng Mobile
- Đăng nhập / Đăng ký
- Xem danh sách homestay (phân trang)
- Xem chi tiết homestay và phòng
- Đặt phòng (in development)

---

## 11. MÃ LỖI HỆ THỐNG

| Mã | Tên | Thông báo |
|---|---|---|
| 400 | INVALID_INPUT | Dữ liệu đầu vào không hợp lệ |
| 400 | ROOM_FULLY_BOOKED | Phòng đã được đặt hết |
| 401 | UNAUTHORIZED | Bạn không có quyền |
| 403 | NOT_PROPERTY_OWNER | Không phải chủ sở hữu |
| 404 | USER_NOT_FOUND | Không tìm thấy người dùng |
| 405 | INVALID_PASSWORD_OR_EMAIL | Sai mật khẩu hoặc email |
| 409 | ROOM_HAS_BOOKING | Phòng còn đơn đặt |
| 410 | INVALID_BOOKING_STATUS_FOR_CANCEL | Đơn đã hoàn tất/đã hủy |
| 411 | INVENTORY_EXCEEDS_CAPACITY | Số phòng trống vượt tổng |
| 412 | INVALID_DATE_RANGE | Khoảng ngày không hợp lệ |
| 505 | PROPERTY_NOT_FOUND | Không tìm thấy homestay |
| 506 | BOOKING_NOT_FOUND | Không tìm thấy Booking |
| 506 | CHECK_OUT_MUST_BE_AFTER_CHECK_IN | Ngày nhận trước ngày trả |
| 507 | ROOM_IS_NOT_FOUND | Không tìm thấy phòng |
| 508 | BOOKING_IS_NOT_COMPLETED | Chưa check-out xong |
| 509 | EXISTED_REVIEW_FOR_BOOKING | Đã đánh giá đơn này |
| 510 | NOT_IN_PENDING_STATUS | Đơn không ở trạng thái PENDING |
| 511 | FILE_EMPTY | Không nhận được file ảnh |

---

## 12. HƯỚNG PHÁT TRIỂN

### 12.1 Frontend (đã có mock data, cần kết nối API thật)
- `/admin/login` – Trang đăng nhập Admin/Host
- `/admin/dashboard` – Tổng quan thống kê (charts doanh thu, booking)
- `/admin/properties/:id` – Chi tiết property, quản lý rooms
- `/admin/payments` – Quản lý thanh toán
- `/admin/reviews` – Quản lý đánh giá

### 12.2 Tính năng có thể mở rộng
- Hệ thống thông báo realtime (WebSocket)
- Tìm kiếm nâng cao (lọc theo giá, loại phòng, đánh giá)
- Hệ thống chat giữa Host và Guest
- Tích hợp Google Maps cho vị trí homestay
- Refund tự động khi hủy (tích hợp VNPay refund API)
- Multi-language support (i18n)

---

## 13. CÀI ĐẶT VÀ CHẠY DỰ ÁN

### 13.1 Yêu cầu môi trường
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6+
- Node.js 20+ (cho Angular)
- Android Studio (cho Mobile)

### 13.2 Backend
```bash
# Cấu hình application.properties
# - spring.datasource.url, username, password
# - spring.data.redis.host, port
# - cloudinary.cloud-name, api-key, api-secret
# - vnpay.tmnCode, hashSecret, ...
# - app.jwt.secret, expiration

./mvnw spring-boot:run
# Swagger UI: http://localhost:8080/swagger-ui/index.html
```

### 13.3 Frontend Web
```bash
cd WebApp/Web/booking-web
npm install
npm run dev
# Chạy tại: http://localhost:4200
```

### 13.4 Mobile App
```bash
# Mở Android Studio
# Import project: WebApp/App/
# Cấu hình BASE_URL trong RetrofitClient.java
# Run on emulator hoặc physical device
```

---

## 14. ĐÁNH GIÁ KỸ THUẬT

### 14.1 Điểm mạnh
- **Concurrency Control**: Pessimistic Lock ngăn oversell khi nhiều người đặt đồng thời
- **Auto Timeout**: Redis TTL + Cron Job đảm bảo phòng được giải phóng nếu không thanh toán
- **Clean Architecture**: Phân tách rõ Controller/Service/Repository/Entity/DTO
- **Security toàn diện**: JWT stateless, BCrypt, RBAC trên cả Backend (Spring Security) và Frontend (AuthGuard + RoleGuard)
- **Payment Integration**: VNPay với xác thực chữ ký HMAC-SHA512, timeout khớp Redis TTL
- **Image Management**: Cloudinary CDN, upload nhiều ảnh, quản lý thumbnail
- **API Documentation**: Swagger UI tự động
- **Frontend hoàn chỉnh**: Angular Web Host Portal kết nối API thật toàn bộ, có auth/role guard, toast notification
- **Inventory Calendar**: Host quản lý được số phòng theo từng ngày trực quan, bulk update, extend

### 14.2 Hạn chế hiện tại
- **Mobile App còn hạn chế**: Android chỉ hỗ trợ xem danh sách homestay, chưa có đặt phòng và thanh toán
- **Chưa có thông báo realtime**: Không có WebSocket/SSE, host không nhận được thông báo ngay khi có booking mới
- **Cache Redis chưa tối ưu**: Hiện chỉ dùng Redis cho booking timeout TTL, chưa cache dữ liệu API
- **Chưa có unit test**: Không có bộ test tự động cho cả backend lẫn frontend
- **Chưa hỗ trợ refund tự động**: Khi host hủy booking đã thanh toán VNPay, hệ thống chỉ đánh dấu REFUNDED trong DB, không thực hiện hoàn tiền thật qua VNPay API
- **Payment List chưa chuyên biệt**: Trang `/payments` hiện dùng lại `BookingService` thay vì có service riêng cho payment

### 14.3 Tổng kết mức độ hoàn thành
| Thành phần | Mức độ |
|---|---|
| Backend REST API | ✅ Hoàn chỉnh |
| Xác thực & Phân quyền | ✅ Hoàn chỉnh |
| Quản lý Homestay/Phòng | ✅ Hoàn chỉnh |
| Đặt phòng + Race Condition | ✅ Hoàn chỉnh |
| Thanh toán VNPay | ✅ Hoàn chỉnh (sandbox) |
| Inventory Calendar | ✅ Hoàn chỉnh |
| Angular Web (Host Portal) | ✅ Hoàn chỉnh |
| Android Mobile App | 🔶 Một phần (xem homestay) |
| Unit / Integration Test | ❌ Chưa có |

---

*Tài liệu này được tổng hợp từ mã nguồn dự án BookingApp – Đồ án tốt nghiệp.*
