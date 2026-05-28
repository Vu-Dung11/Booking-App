PROMPT: Bổ sung Admin Portal cho BookingApp
Bối cảnh dự án
Đây là đồ án tốt nghiệp BookingApp — hệ thống quản lý homestay. Hiện trạng:

Backend (Spring Boot 4 + MySQL + JWT): đã có User.Role enum gồm ADMIN, HOST, GUEST. JWT đã encode role và userId trong payload. Hiện có HostPropertyController, HostBookingController, HostDashboardController, HostReviewController phục vụ riêng HOST. Chưa có controller nào dành cho ADMIN.
Web Angular 21 (standalone components, WebApp/Web/booking-web/): login đang khoá cứng chỉ cho HOST (AuthService.ALLOWED_PORTAL_ROLES = ['HOST']). Routes hiện chỉ có /login và /host/*. Nếu user là ADMIN đăng nhập → bị reject ngay tại login.
Mobile Android: chỉ phục vụ GUEST, không liên quan tới task này.
Mục tiêu
Mở rộng web Angular thành portal đa role (HOST + ADMIN), trong đó ADMIN có 3 module mới:

Quản lý tài khoản — xem danh sách user (tất cả role), khoá/mở khoá, xem chi tiết, lọc theo role/trạng thái
Quản lý homestays — xem toàn bộ property của mọi host, bật/tắt isActive, xem chi tiết property+rooms+ảnh, xoá property vi phạm
Quản lý reviews — xem toàn bộ review, ẩn/xoá review vi phạm chính sách, xem context (booking + property + guest)
Yêu cầu chi tiết
A. Backend (Spring Boot)
A.1 Bổ sung field isActive (hoặc status) cho User
User chưa có cờ active. Thêm Boolean isActive (default true, column is_active). Khi admin "khoá" → set false. JwtAuthenticationFilter cần check isActive → nếu false thì throw UNAUTHORIZED (token vẫn còn hạn nhưng tài khoản đã bị khoá).
Nếu DB đang ddl-auto: validate, hãy soạn câu SQL ALTER TABLE riêng (commit kèm Flyway migration mới, đặt tên V{n}__add_user_is_active.sql trong src/main/resources/db/migration/).
A.2 Tạo các Admin controller (đặt cùng package controller/)
Tất cả endpoint dưới đây yêu cầu @PreAuthorize("hasRole('ADMIN')"). Tuân thủ format response ApiResponse<T> như các controller hiện có.

AdminUserController — /api/v1/admin/users

Method	Path	Mô tả
GET	/	Paginate users, query ?role=&keyword=&isActive=&page=&size= (keyword search email/fullName)
GET	/{id}	Chi tiết 1 user (kèm số property nếu HOST, số booking nếu GUEST)
PATCH	/{id}/lock	Khoá tài khoản (set isActive=false)
PATCH	/{id}/unlock	Mở khoá
GET	/stats	Tổng user, breakdown theo role, số bị khoá
AdminPropertyController — /api/v1/admin/properties

Method	Path	Mô tả
GET	/	Paginate tất cả property, query ?city=&isActive=&hostId=&keyword=&page=&size=
GET	/{id}	Chi tiết property (đầy đủ rooms, images, host info, tổng review, rating trung bình)
PATCH	/{id}/toggle-active	Bật/tắt isActive
DELETE	/{id}	Soft delete hoặc hard delete (chọn 1, document rõ trong service)
AdminReviewController — /api/v1/admin/reviews

Method	Path	Mô tả
GET	/	Paginate tất cả review, query ?rating=&propertyId=&guestId=&keyword=&page=&size= (keyword search trong comment)
GET	/{id}	Chi tiết review (kèm booking, property, guest)
DELETE	/{id}	Xoá review vi phạm
AdminDashboardController — /api/v1/admin/dashboard

Method	Path	Mô tả
GET	/stats	Tổng quan: total users, total properties, total bookings, total revenue, breakdown theo role/status
GET	/recent-activities	10 hoạt động gần nhất (booking mới, user mới, review mới)
A.3 Service layer
Tạo AdminUserService, AdminPropertyService, AdminReviewService, AdminDashboardService riêng (đừng dồn vào UserService hiện tại để tránh phá vỡ flow HOST/GUEST).
DTO mới đặt trong dto/admin/: AdminUserResponse, AdminPropertyResponse, AdminReviewResponse, AdminDashboardStatsResponse.
Form/request DTO trong form/admin/: LockUserRequest, ToggleActiveRequest (nếu cần body cho lý do khoá).
A.4 SecurityConfiguration
Mở configuration/security/SecurityConfiguration.java, thêm rule:


.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
Trước rule .anyRequest().authenticated().

A.5 Error codes mới (thêm vào configuration/enm/ErrorCode)
Code	Key	Message
515	USER_ALREADY_LOCKED	Tài khoản đã bị khoá
516	USER_ALREADY_ACTIVE	Tài khoản đang hoạt động
517	CANNOT_LOCK_SELF	Không thể tự khoá tài khoản admin của chính mình
518	REVIEW_NOT_FOUND	Không tìm thấy review
B. Frontend Angular (WebApp/Web/booking-web/)
B.1 Cập nhật AuthService
Đổi ALLOWED_PORTAL_ROLES từ ['HOST'] → ['HOST', 'ADMIN'].
Thêm computed isAdmin = computed(() => currentUser()?.role === 'ADMIN').
Sửa hàm login(): sau khi nhận token và verify role hợp lệ, trả về thêm role để component login biết redirect đi đâu (/host/dashboard hay /admin/dashboard).
B.2 Cập nhật LoginComponent
Sau login thành công: nếu role = HOST → navigate /host/dashboard; nếu ADMIN → /admin/dashboard.
Cập nhật message lỗi reason=forbidden: bỏ chữ "chủ homestay (HOST)" để generic hơn ("Tài khoản không có quyền truy cập portal này").
Optionally thêm hint UI nhỏ: "Portal dành cho Chủ homestay và Quản trị viên" dưới form.
B.3 Tạo route /admin/* mới (xoá redirect cũ /admin → /host)
Trong app.routes.ts:


{
  path: 'admin',
  loadComponent: () => import('./admin-portal/admin-layout/admin-layout.component').then(m => m.AdminPortalLayoutComponent),
  canActivate: [authGuard, roleGuard('ADMIN')],
  children: [
    { path: 'dashboard', loadComponent: ... },
    { path: 'users', loadComponent: ... },
    { path: 'users/:id', loadComponent: ... },
    { path: 'properties', loadComponent: ... },
    { path: 'properties/:id', loadComponent: ... },
    { path: 'reviews', loadComponent: ... },
    { path: 'reviews/:id', loadComponent: ... },
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
  ]
}
B.4 Cấu trúc thư mục mới (KHÔNG dùng chung với folder admin/ hiện tại của HOST — tránh nhầm lẫn)
Tạo folder riêng src/app/admin-portal/:


admin-portal/
├── admin-layout/admin-layout.component.{ts,html,css}   # Sidebar riêng cho ADMIN
├── dashboard/
│   ├── dashboard.component.{ts,html,css}
│   ├── models/admin-stats.model.ts
│   └── services/admin-dashboard.service.ts
├── users/
│   ├── user-list/user-list.component.{ts,html,css}
│   ├── user-detail/user-detail.component.{ts,html,css}
│   ├── models/admin-user.model.ts
│   └── services/admin-user.service.ts
├── properties/
│   ├── property-list/property-list.component.{ts,html,css}
│   ├── property-detail/property-detail.component.{ts,html,css}
│   ├── models/admin-property.model.ts
│   └── services/admin-property.service.ts
└── reviews/
    ├── review-list/review-list.component.{ts,html,css}
    ├── review-detail/review-detail.component.{ts,html,css}
    ├── models/admin-review.model.ts
    └── services/admin-review.service.ts
B.5 Sidebar ADMIN (AdminPortalLayoutComponent)
4 menu item:

Dashboard (icon: bar-chart)
Tài khoản (icon: users)
Homestays (icon: home)
Reviews (icon: message-square)
Footer sidebar: avatar admin + nút "Đăng xuất"
B.6 Pages cần build (đầy đủ chức năng, KHÔNG mock — gọi API thật)
/admin/dashboard — 4 stats card hàng trên (tổng users, properties, bookings, revenue) + 2 chart (booking theo tháng, revenue theo tháng) + bảng "Hoạt động gần đây" (recent activities).

/admin/users — Bảng paginate với cột: Avatar (initials), Email, Họ tên, Role badge, Trạng thái (Active/Locked), Ngày tạo, Action (xem chi tiết, khoá/mở khoá). Filter: dropdown role, dropdown trạng thái, search box (email/tên). Khi click khoá → modal confirm. Nếu khoá chính mình → backend trả CANNOT_LOCK_SELF → hiển thị toast.

/admin/users/:id — Card thông tin user + stats riêng (HOST: số property, GUEST: số booking, ADMIN: ngày tạo) + bảng "Hoạt động" liên quan (property/booking/review của user đó). Nút action: khoá/mở khoá.

/admin/properties — Card grid hoặc table (chọn grid để khác với HOST list). Mỗi card: thumbnail, tên, address, host name, badge isActive, rating trung bình, số rooms. Filter: city, host, isActive, search. Action: xem chi tiết, toggle active, xoá.

/admin/properties/:id — Tabs hoặc sections:

Info chung (description, address, host info)
Gallery ảnh
Bảng rooms (room type, capacity, basePrice, quantity)
Bảng bookings recent của property này
Bảng reviews của property này (kèm rating)
Action panel: toggle active, xoá property
/admin/reviews — Table: avatar guest, comment (truncate 100 chars), rating (★), property name, ngày, action (xem chi tiết, xoá). Filter: rating (1-5 sao), property, search trong comment.

/admin/reviews/:id — Card hiển thị full review + thông tin booking liên kết + thông tin guest + property. Nút xoá review (modal confirm).

B.7 Style & UX
Tuân thủ toàn bộ design system trong CLAUDE.md (Mixpanel style, font Inter, palette #7C5CFC primary, border-radius 8-12px, animation scroll). Đặc biệt:

Sidebar ADMIN nên có màu accent khác HOST một chút (ví dụ thêm chip "ADMIN" nhỏ cạnh logo) để admin biết mình đang ở portal nào — KHÔNG đổi hệ màu chính.
Mọi bảng dùng pattern table giống bookings hiện tại (no outer border, hover row bg #F8F9FA).
Mọi modal confirm dùng cùng component shared (nếu chưa có thì tạo ConfirmDialogComponent trong shared/components/).
Toast error/success dùng ToastService đã có sẵn.
B.8 Responsive & Animation
Mobile breakpoint < 768px: sidebar collapse thành hamburger.
Mỗi card/section dùng directive animate-on-scroll đã có sẵn trong shared/directives/.
C. Test & Verify
Tạo 1 user ADMIN test trong DB:

INSERT INTO users (email, password_hash, full_name, role, is_active)
VALUES ('admin@booking.local', '<bcrypt hash của 123456>', 'System Admin', 'ADMIN', 1);
Login bằng admin → phải redirect tới /admin/dashboard (không phải /host/dashboard).
Login bằng HOST → vẫn redirect /host/dashboard như cũ (không regression).
Login bằng ADMIN rồi truy cập /host/dashboard thủ công → bị roleGuard('HOST') chặn, kéo về login.
Khoá 1 user qua admin panel → user đó login lại phải bị từ chối.
Toggle active 1 property → property đó biến mất khỏi public search API (/api/v1/properties).
Ràng buộc tuyệt đối
KHÔNG sửa application.properties (chứa credentials).
KHÔNG đổi format ApiResponse<T> — mọi endpoint mới phải tuân thủ { code, message, data }.
KHÔNG dùng mock data ở các page mới — gọi API thật. Nếu backend chưa sẵn, build backend trước.
KHÔNG tạo NgModule — chỉ standalone components.
KHÔNG sửa folder admin/ hiện tại của HOST. Tạo folder mới admin-portal/ để tách biệt.
KHÔNG xoá hoặc đổi tên các HostXxxController hiện có.
Migration Flyway: nếu có thay đổi schema (is_active cho User), commit kèm file migration mới — không sửa file migration cũ.
Commit message bằng tiếng Anh, format feat(admin): ... / fix(admin): ....
Thứ tự triển khai đề xuất
Backend: thêm isActive cho User + migration → cập nhật JwtAuthenticationFilter check isActive
Backend: tạo AdminUserController + service + DTO → test bằng Postman/Swagger
Backend: tạo AdminPropertyController → test
Backend: tạo AdminReviewController → test
Backend: tạo AdminDashboardController → test
Frontend: sửa AuthService + LoginComponent để hỗ trợ ADMIN
Frontend: tạo admin-portal/ skeleton (layout + dashboard + routes)
Frontend: build trang users list/detail
Frontend: build trang properties list/detail
Frontend: build trang reviews list/detail
End-to-end test theo checklist mục C
Commit + push
Sau mỗi mốc, chạy ./mvnw spring-boot:run (backend) và npm start trong WebApp/Web/booking-web/ (frontend) để verify thủ công, chụp screenshot so sánh với Mixpanel style như rule trong CLAUDE.md.