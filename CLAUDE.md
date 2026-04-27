# BookingApp - Homestay Management Platform

## Project Overview

Hệ thống quản lý homestay dành cho **Admin** và **Chủ homestay (Host)**. Website cho phép chủ homestay quản lý property, tiếp nhận đặt phòng, theo dõi thanh toán và vận hành homestay. Đây là đồ án tốt nghiệp (DATN).

## Tech Stack

### Backend (root `/`)
- **Framework:** Spring Boot 4.0.3 (Java 17)
- **Database:** MySQL 8 (`booking_app`)
- **ORM:** Spring Data JPA + Hibernate (ddl-auto: validate)
- **Migration:** Flyway (currently disabled)
- **Cache:** Redis
- **Auth:** JWT (jjwt 0.11.5) + Spring Security
- **Payment:** VNPay sandbox
- **Image Upload:** Cloudinary
- **API Docs:** SpringDoc OpenAPI (Swagger UI)
- **Build:** Maven Wrapper (`./mvnw`)

### Frontend Web (Admin Panel) — `WebApp/Web/booking-web/`
- **Framework:** Angular 21 (standalone components, SSR-ready)
- **Language:** TypeScript 5.9 (strict mode)
- **Test:** Vitest
- **Formatter:** Prettier
- **Package Manager:** npm 11

### Mobile App — `WebApp/App/`
- **Platform:** Android (Java, MVVM pattern)

## Project Structure

```
bookingapp/
├── src/main/java/com/example/bookingapp/   # Spring Boot backend
│   ├── configuration/
│   │   ├── security/         # JwtAuthenticationFilter, SecurityConfiguration, SwaggerConfig
│   │   ├── exception/        # AppException, ErrorHandler, ErrorResponse, GlobalExceptionHandler
│   │   ├── enm/              # ErrorCode enum
│   │   └── utils/            # JwtUtil, SecurityUtils, VNPayUtil
│   ├── controller/           # REST controllers (8 controllers)
│   ├── service/              # Business logic
│   ├── services/             # AuthService (legacy naming)
│   ├── repository/           # Spring Data JPA repositories
│   ├── entity/               # JPA entities (9 entities)
│   ├── dto/                  # Response DTOs
│   └── form/                 # Request DTOs / form objects
├── src/main/resources/
│   └── application.properties
├── WebApp/
│   ├── Web/booking-web/      # Angular admin panel
│   │   └── src/app/
│   │       ├── admin/
│   │       │   ├── admin-layout/   # Sidebar layout wrapper
│   │       │   ├── users/          # model, service, user-list component
│   │       │   ├── properties/     # model, service, property-list component
│   │       │   ├── rooms/          # model only
│   │       │   └── bookings/       # model, service, booking-list component
│   │       ├── app.routes.ts
│   │       └── app.config.ts
│   └── App/                  # Android mobile app
└── pom.xml
```

---

## Entities (Database Schema)

### User
| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK, auto-increment |
| email | String | unique, not null |
| password | String | column: `password_hash` |
| fullName | String | column: `full_name` |
| phoneNumber | String | |
| role | Enum | `ADMIN`, `GUEST`, `HOST` |

### Property
| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| host | User | FK `host_id`, ManyToOne LAZY |
| name | String | not null |
| description | String | TEXT |
| address | String | not null |
| city | String | not null |
| country | String | not null |
| isActive | Boolean | column: `is_active`, default true |

### Room
| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| property | Property | FK `property_id`, ManyToOne LAZY |
| roomType | String | column: `room_type` |
| capacity | Integer | |
| basePrice | BigDecimal | column: `base_price` |
| quantity | Integer | |

### RoomInventory
| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| room | Room | FK |
| date | LocalDate | |
| availableQuantity | Integer | |

### Booking
| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| guest | User | FK `guest_id`, ManyToOne LAZY |
| room | Room | FK `room_id`, ManyToOne LAZY |
| checkInDate | LocalDate | column: `check_in_date` |
| checkOutDate | LocalDate | column: `check_out_date` |
| totalPrice | BigDecimal | column: `total_price` |
| roomQuantity | Integer | column: `room_quantity` |
| status | Enum | `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED` |
| createdAt | LocalDateTime | auto-set @PrePersist |

### Payment
| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| booking | Booking | OneToOne, FK `booking_id` |
| amount | BigDecimal | |
| paymentMethod | String | VNPay, Momo, etc. |
| status | Enum | `PENDING`, `SUCCESS`, `FAILED` |
| transactionId | String | ma giao dich tu cong thanh toan |
| createdAt | LocalDateTime | |

### Review
| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| booking | Booking | OneToOne, FK `booking_id`, unique |
| property | Property | ManyToOne, FK `property_id` |
| rating | Integer | not null |
| comment | String | TEXT |
| createdAt | LocalDateTime | auto-set @PrePersist |

### PropertyImage
| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| property | Property | FK |
| imageUrl | String | Cloudinary URL |
| isThumbnail | Boolean | |

### RoomImage
| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| room | Room | FK |
| imageUrl | String | Cloudinary URL |
| isThumbnail | Boolean | |

---

## API Endpoints

### Response Format
Tất cả API trả về `ApiResponse<T>`:
```json
{ "code": 0, "message": "Success", "data": <T> }
```
Error: `{ "code": <errorCode>, "message": "<errorMessage>", "data": null }`

### Auth — `/api/v1/auth` (PUBLIC)
| Method | Path | Body | Response | Notes |
|--------|------|------|----------|-------|
| POST | `/register` | `RegisterRequest` { email, password, fullName, phoneNumber } | `AuthResponse` { token } | |
| POST | `/login` | `LoginRequest` { email, password } | `AuthResponse` { token } | |

### Users — `/api/v1/users` (AUTHENTICATED)
| Method | Path | Params | Response | Notes |
|--------|------|--------|----------|-------|
| GET | `/` | `?role=HOST&page=0&size=10` | `Page<UserResponse>` | role optional, paginated |
| GET | `/{id}` | | `UserResponse` { id, email, fullName, phoneNumber, role } | |

### Properties — `/api/v1/properties`
| Method | Path | Auth | Body/Params | Response | Notes |
|--------|------|------|-------------|----------|-------|
| GET | `/` | PUBLIC | `?page=0&size=10` | `Page<Property>` | Paginated, returns full entity |
| GET | `/{id}` | PUBLIC | | `Property` | |
| GET | `/{id}/detail` | PUBLIC | | `PropertyDetailResponse` { propertyId, name, description, address, city, country, rooms[] } | rooms = RoomSearchResponse[] |
| GET | `/search` | PUBLIC | `SearchRequest` as query params | `List<PropertySearchResponse>` { propertyId, propertyName, address, city, minPrice, availableRooms[] } | |
| POST | `/create` | HOST | `PropertyRequest` { name, description, address, city, country } | `Property` | @PreAuthorize HOST |
| POST | `/{propertyId}/rooms` | HOST | `RoomRequest` { roomType, capacity, basePrice, quantity } | `Room` | @PreAuthorize HOST |

### Property Images — `/api/v1/properties`
| Method | Path | Auth | Body | Response | Notes |
|--------|------|------|------|----------|-------|
| POST | `/{propertyId}/images` | HOST | multipart `files` | `List<{id, imageUrl, isThumbnail}>` | Multiple files upload |

### Room Images — `/api/v1/rooms`
| Method | Path | Auth | Body | Response | Notes |
|--------|------|------|------|----------|-------|
| POST | `/{roomId}/images` | HOST | multipart `files` | `List<{id, imageUrl, isThumbnail}>` | Multiple files upload |

### Bookings — `/api/v1/bookings`
| Method | Path | Auth | Body/Params | Response | Notes |
|--------|------|------|-------------|----------|-------|
| GET | `/` | AUTH | `?status=PENDING&page=0&size=10` | `Page<Booking>` | status optional, paginated |
| GET | `/{id}` | AUTH | | `Booking` | Full entity with guest, room, room.property |
| POST | `/` | GUEST | `BookingRequest` | `Booking` | @PreAuthorize GUEST |
| POST | `/{id}/booking-completed` | AUTH | | `String` | Confirm check-out |

### Payments — `/api/v1/payments` (PUBLIC)
| Method | Path | Params/Body | Response | Notes |
|--------|------|-------------|----------|-------|
| GET | `/vnpay-url` | `?bookingId=1` | `String` (VNPay URL) | Generate payment URL |
| GET | `/vnpay-return` | VNPay callback params | `String` | VNPay redirect callback |
| POST | `/callback` | `PaymentCallbackRequest` | `String` | Payment notification |

### Reviews — `/api/v1/reviews` (AUTHENTICATED)
| Method | Path | Body | Response | Notes |
|--------|------|------|----------|-------|
| POST | `/` | `ReviewRequest` { bookingId, rating, comment } | `String` | Only COMPLETED bookings |

### Media — `/api/v1/media` (PUBLIC)
| Method | Path | Body | Response | Notes |
|--------|------|------|----------|-------|
| POST | `/upload` | multipart `file` | `{ status, message, imageUrl, publicId }` | Generic image upload |

### Security Config
- **Public endpoints:** `/api/v1/auth/**`, GET `/api/v1/properties/**`, `/swagger-ui/**`, `/api/v1/media/**`, `/api/v1/payments/**`
- **Authenticated:** Tất cả endpoint còn lại
- **Role-based:** `@PreAuthorize("hasRole('HOST')")` cho create property, add room, upload images
- **Stateless:** JWT, no session
- **CORS:** Currently disabled (cần bật khi kết nối frontend)

### Error Codes
| Code | Key | Message |
|------|-----|---------|
| 400 | INVALID_INPUT | Du lieu dau vao khong hop le |
| 400 | ROOM_FULLY_BOOKED | Phong da duoc dat het |
| 401 | UNAUTHORIZED | Ban khong co quyen |
| 404 | USER_NOT_FOUND | Khong tim thay nguoi dung |
| 405 | INVALID_PASSWORD_OR_EMAIL | Sai mat khau hoac email |
| 405 | USER_NOT_AUTHENTICATED | User not authenticated |
| 505 | PROPERTY_NOT_FOUND | Khong tim thay homestay |
| 506 | BOOKING_NOT_FOUND | Khong tim thay Booking |
| 506 | CHECK_OUT_MUST_BE_AFTER_CHECK_IN | Ngay nhan phong phai truoc ngay tra phong |
| 507 | ROOM_IS_NOT_FOUND | Khong tim thay phong |
| 507 | NOT_YOUR_BOOKING | Khong the danh gia don hang cua nguoi khac |
| 508 | BOOKING_IS_NOT_COMPLETED | Chi duoc danh gia sau khi check-out |
| 509 | EXISTED_REVIEW_FOR_BOOKING | Don hang da duoc danh gia |
| 510 | NOT_IN_PENDING_STATUS | Don hang khong o trang thai cho thanh toan |
| 511 | FILE_EMPTY | Khong nhan duoc file anh |

---

## Frontend Angular — Current State

### Existing Components (all use mock data, not connected to real API)
- `AdminLayoutComponent` — sidebar + header + router-outlet wrapper
- `UserListComponent` — table with mock users
- `PropertyListComponent` — card grid with mock properties
- `BookingListComponent` — table with mock bookings

### Existing Services (all return mock Observable, no HTTP calls)
- `UserService` — `getUsers()` returns mock array
- `PropertyService` — `getProperties()` returns mock array
- `BookingService` — `getBookings()` returns mock array

### Existing Models
- `User` { id, email, fullName, phoneNumber?, role }
- `Property` { id, host: User, name, description, address, city, country, isActive }
- `Room` { id, property: Property, roomType, capacity, basePrice, quantity }
- `Booking` { id, guest: User, room: Room, checkInDate, checkOutDate, totalPrice, roomQuantity, status, createdAt }

### Current CSS Variables (styles.css)
```
--primary-color: #6366f1    --secondary-color: #a855f7
--success-color: #10b981    --warning-color: #f59e0b     --danger-color: #ef4444
--bg-color: #f8fafc         --sidebar-bg: #ffffff        --surface-color: #ffffff
--text-primary: #0f172a     --text-secondary: #64748b    --text-tertiary: #94a3b8
--border-color: #e2e8f0
```

### Routes (app.routes.ts)
```
/admin          -> AdminLayoutComponent (wrapper)
  /users        -> UserListComponent (lazy)
  /properties   -> PropertyListComponent (lazy)
  /bookings     -> BookingListComponent (lazy)
  (default)     -> redirect to /users
/               -> redirect to /admin
```

---

## Admin Panel Pages

### Implemented (mock data)
- `/admin/users` — Table with user info, role badges, search/filter
- `/admin/properties` — Card grid with status badges, host info
- `/admin/bookings` — Table with guest info, dates, amounts, status badges

### Planned
- `/admin/dashboard` — Tong quan (stats cards, charts: revenue, bookings, occupancy)
- `/admin/properties/:id` — Chi tiet property + quan ly rooms
- `/admin/payments` — Quan ly thanh toan
- `/admin/reviews` — Quan ly danh gia
- `/login` — Trang dang nhap cho admin/host

---

## Design System & UI Guidelines

Theo phong cach **Mixpanel** — toi gian, hien dai, chuyen nghiep.

### Design Principles
- **Clean & Minimal:** Moi element phai co muc dich ro rang, loai bo moi yeu to thua
- **Whitespace Generously:** Su dung nhieu khoang trang giua cac section, card, element
- **Subtle Depth:** Dung shadow nhe thay vi border cung
- **Smooth Interactions:** Moi hover/transition deu co `transition: all 0.2s ease`

### Color Palette
- **Background:** `#FFFFFF` (main), `#F8F9FA` (secondary/sidebar), `#F1F3F5` (card bg)
- **Text:** `#1A1A2E` (heading), `#495057` (body), `#868E96` (muted/placeholder)
- **Primary:** `#7C5CFC` (purple — action buttons, active states, links)
- **Primary Hover:** `#6A4AE8`
- **Success:** `#51CF66`
- **Warning:** `#FFC078`
- **Danger:** `#FF6B6B`
- **Border:** `#E9ECEF`

### Typography
- **Font:** `Inter` (Google Fonts), fallback `system-ui, -apple-system, sans-serif`
- **Heading:** 600 weight, tracking `-0.02em`
- **Body:** 400 weight, `14px`-`15px` base size
- **Small/Label:** 500 weight, `12px`-`13px`, uppercase letter-spacing `0.05em`

### Component Patterns

**Sidebar Navigation:**
- Width: `240px` fixed, bg `#FAFBFC`
- Logo top, nav items with SVG icon + label
- Active state: bg `#F0EBFF`, text `#7C5CFC`, border-left `3px solid #7C5CFC`
- Hover: bg `#F1F3F5`
- Collapsible on mobile (hamburger toggle)

**Data Tables:**
- No outer border, bottom border `1px solid #F1F3F5` between rows
- Header: uppercase, `12px`, `500` weight, color `#868E96`
- Row hover: bg `#F8F9FA`
- Pagination minimal, bottom-right

**Cards & Containers:**
- Border-radius: `12px`, padding: `24px`
- Background: `#FFFFFF`
- Shadow: `0 1px 3px rgba(0,0,0,0.04), 0 1px 2px rgba(0,0,0,0.06)`

**Buttons:**
- Primary: bg `#7C5CFC`, text white, border-radius `8px`, padding `10px 20px`
- Secondary/Ghost: bg transparent, border `1px solid #DEE2E6`, text `#495057`
- Font-weight: `500`, font-size `14px`

**Form Inputs:**
- Border: `1px solid #DEE2E6`, border-radius `8px`
- Padding: `10px 14px`
- Focus: border `#7C5CFC`, ring `0 0 0 3px rgba(124,92,252,0.1)`

**Status Badges:**
- Pill shape: border-radius `20px`, padding `4px 12px`, font-size `12px`, font-weight `500`
- Confirmed: bg `#ECFDF3`, text `#12B76A`
- Pending: bg `#FFF8E1`, text `#F59E0B`
- Cancelled: bg `#FEF3F2`, text `#F04438`
- Completed: bg `#F0EBFF`, text `#7C5CFC`

**Stats Cards (Dashboard):**
- Stat number: `28px`-`32px`, font-weight `700`
- Label: `13px`, color `#868E96`
- Trend indicator: icon arrow + percentage, green/red

### Layout
- Sidebar (240px fixed left) + Main content area
- Main content: max-width `1200px`, padding `32px`
- Page header: Title (`24px`, `600`) + muted description + action button (top-right)
- Sections gap: `24px`

---

## Development Rules (BAT BUOC)

### Quy tac chung
- Standalone components only (khong dung NgModule)
- Moi feature: `models/` (interfaces), `services/` (API calls), component files
- API service: inject `HttpClient`, return `Observable`, handle error voi `catchError`
- Khong tao file test tru khi duoc yeu cau
- Giu nguyen cau truc package backend (controller/service/repository/entity/dto/form)
- Khong sua `application.properties` tru khi duoc yeu cau (chua credentials)
- Commit message bang tieng Anh

### Quy tac UI bat buoc
- **Sau moi thay doi lon, chup screenshot va so sanh voi design goc (Mixpanel style)**
- **Website phai mobile-friendly:** responsive layout, sidebar collapse tren mobile, touch-friendly tap targets (min 44px)
- **Moi section phai co animation khi scroll:** su dung IntersectionObserver hoac Angular animation, fade-in + slide-up khi element vao viewport

### Responsive Breakpoints
```
Mobile:  < 768px   — sidebar hidden (hamburger toggle), single column, stacked cards
Tablet:  768-1024px — sidebar collapsed (icons only), 2-column grid
Desktop: > 1024px   — full sidebar, multi-column layout
```

### Scroll Animation Pattern
Moi section/card/table khi vao viewport:
```css
/* Initial state */
.animate-on-scroll {
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}

/* When visible */
.animate-on-scroll.visible {
  opacity: 1;
  transform: translateY(0);
}
```
Su dung IntersectionObserver directive hoac Angular signal de toggle class `.visible`.
Stagger delay cho list items: `transition-delay: calc(var(--index) * 0.05s)`
