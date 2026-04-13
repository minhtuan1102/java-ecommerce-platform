# Ecommerce Platform

Roadmap MVP 4 tuần cho dự án cá nhân:
- Backend Java Spring Boot theo module.
- JWT đầy đủ: access token + refresh token.
- PostgreSQL.
- VueJS tối thiểu để demo end-to-end.
- Redis + RabbitMQ + Payment ở mức vừa đủ cho MVP.

## Current Status

Hiện tại đã có module `Category`:
- `POST /api/v1/categories`
- `GET /api/v1/categories`
- `GET /api/v1/categories/{id}`
- `PUT /api/v1/categories/{id}`
- `DELETE /api/v1/categories/{id}`

## MVP Scope

- Backend: `auth`, `category`, `product`, `cart`, `order` (bản tối thiểu).
- Security: `register`, `login`, `refresh`, `logout`.
- Database: PostgreSQL (schema tạo qua JPA/SQL script trong giai đoạn MVP).
- Frontend: login, product list, cart, checkout, category basic page.
- Integration: Redis cache cơ bản, RabbitMQ event cơ bản, Payment sandbox.

---

## Sprint Checklist (Lean)

Mỗi sprint đều có output chạy được.

### Sprint 1 (Week 1) - Auth Skeleton + PostgreSQL

#### Mục tiêu tuần
- [x] Cấu hình PostgreSQL local ổn định.
- [x] Tạo bảng `users`, `roles`, `user_roles`, `refresh_tokens`.
- [x] Tạo skeleton module `auth` (entity + repository + service + controller).
- [x] Seed role cơ bản (`ROLE_USER`, `ROLE_ADMIN`) + 1 admin local.

#### Output cần có
- [x] App boot được.
- [x] DB tạo schema và đọc/ghi user được.

#### Day 1-7

Day 1 - Kết nối DB
- [x] Tạo database `ecommerce` local.
- [x] Cập nhật `src/main/resources/application.properties`.
- [x] Chạy app, xác nhận kết nối DB thành công.
Deliverable: backend boot không lỗi datasource.

Day 2 - Tạo schema auth
- [x] Tạo file SQL (ví dụ `src/main/resources/sql/01_auth_schema.sql`).
- [x] Tạo 4 bảng auth + PK/FK/index tối thiểu.
- [x] Chạy script và kiểm tra bảng trong PostgreSQL.
Deliverable: có đủ bảng `users/roles/user_roles/refresh_tokens`.

Day 3 - Khung module auth
- [x] Tạo package `src/main/java/com/tuan/ecommerce/modules/auth`.
- [x] Tạo 4 lớp chính theo pattern hiện tại: `api/application/domain/infrastructure`.
- [x] Tạo class khung: `AuthController`, `AuthService`, entity/repository.
Deliverable: project compile với module auth mới.

Day 4 - Entity mapping
- [x] Hoàn thiện entity `User`, `Role`, `RefreshToken`.
- [x] Map quan hệ user-role và user-refresh-token.
- [x] Tạo JPA repository tương ứng.
Deliverable: đọc/ghi user + role qua repository.

Day 5 - API skeleton
- [x] Tạo `GET /api/v1/auth/health`.
- [x] Tạo khung `POST /api/v1/auth/register` và `POST /api/v1/auth/login`.
- [x] Tạo DTO request/response cơ bản.
Deliverable: endpoint auth trả JSON đúng format.

Day 6 - Seed dữ liệu
- [x] Seed `ROLE_USER`, `ROLE_ADMIN`.
- [x] Seed 1 admin local để test nhanh.
- [x] Xác nhận admin có role `ROLE_ADMIN` trong `user_roles`.
Deliverable: DB có dữ liệu seed sẵn cho Sprint 2.

Day 7 - Test nền + dọn code
- [x] Viết test cơ bản cho auth service/repository.
- [x] Refactor naming/package đồng nhất với module `category`.
- [x] Tick lại checklist Sprint 1.
Deliverable: test nền pass, sẵn sàng vào JWT thật.

---

### Sprint 2 (Week 2) - Register/Login + Access Token

#### Mục tiêu tuần
- [ ] Thêm Spring Security config.
- [ ] Implement `POST /api/v1/auth/register`.
- [ ] Implement `POST /api/v1/auth/login`.
- [ ] Tạo access token JWT + middleware verify.
- [ ] Bảo vệ endpoint category bằng JWT.
- [ ] Thêm test cơ bản cho register/login.

#### Output cần có
- [ ] Đăng ký và đăng nhập được.
- [ ] Endpoint private yêu cầu token hợp lệ.

#### Day 1-7

Day 1 - Security cơ bản
- [ ] Tạo `SecurityConfig` với public/private endpoint.
- [ ] Cho phép `/api/v1/auth/**` public, còn lại private.
Deliverable: app chạy với security filter chain.

Day 2 - Register API
- [ ] Validate input register.
- [ ] Hash password bằng BCrypt.
- [ ] Lưu user với role mặc định `ROLE_USER`.
Deliverable: `POST /register` hoạt động.

Day 3 - Login API
- [ ] Xác thực email/password.
- [ ] Trả JWT access token khi login thành công.
- [ ] Trả lỗi 401 khi sai thông tin.
Deliverable: `POST /login` hoạt động.

Day 4 - JWT middleware
- [ ] Tạo filter đọc token từ header Authorization.
- [ ] Verify token và set authentication context.
- [ ] Chuẩn hóa format lỗi auth.
Deliverable: endpoint private nhận token đúng.

Day 5 - Bảo vệ category
- [ ] Áp JWT cho endpoint `Category`.
- [ ] Test thủ công bằng Postman/curl.
- [ ] Chốt policy endpoint nào public/private.
Deliverable: category đã được bảo vệ theo policy.

Day 6 - Test auth flow
- [ ] Viết test cho register/login.
- [ ] Kiểm tra case email trùng và password sai.
- [ ] Kiểm tra endpoint private khi thiếu token.
Deliverable: auth flow test pass mức cơ bản.

Day 7 - Cleanup tuần 2
- [ ] Dọn code và message lỗi.
- [ ] Cập nhật README mục tiến độ Sprint 2.
- [ ] Chốt danh sách việc cho refresh token tuần 3.
Deliverable: code gọn, sẵn sàng vào refresh.

---

### Sprint 3 (Week 3) - Refresh Token + Product + Cart + Redis

#### Mục tiêu tuần
- [ ] Implement `POST /api/v1/auth/refresh` (rotate refresh token).
- [ ] Implement `POST /api/v1/auth/logout` (revoke refresh token).
- [ ] Tạo module `product` CRUD cơ bản (gắn `category_id`).
- [ ] Tạo module `cart`: add/update/remove item + subtotal.
- [ ] Thêm Redis cache cho endpoint đọc nhiều.
- [ ] Thêm test cho auth refresh + cart.

#### Output cần có
- [ ] Login -> refresh -> logout chạy đúng.
- [ ] User xem product và thao tác giỏ hàng được.
- [ ] Endpoint list product/category có cache hoạt động.

#### Day 1-7

Day 1 - Refresh token model
- [ ] Hoàn thiện entity/table refresh token.
- [ ] Thiết kế expire + revoke flag.
- [ ] Gắn refresh token với user.
Deliverable: model refresh token sẵn sàng dùng.

Day 2 - API refresh
- [ ] Implement `POST /api/v1/auth/refresh`.
- [ ] Rotate token mới, vô hiệu token cũ.
- [ ] Trả access token mới.
Deliverable: refresh flow hoạt động.

Day 3 - API logout
- [ ] Implement `POST /api/v1/auth/logout`.
- [ ] Revoke refresh token hiện tại.
- [ ] Chặn refresh sau logout.
Deliverable: logout hủy token thành công.

Day 4 - Module product
- [ ] Tạo entity/repository/service/controller cho product.
- [ ] CRUD cơ bản + validate dữ liệu.
- [ ] Link product với category.
Deliverable: API product CRUD chạy được.

Day 5 - Module cart
- [ ] Tạo cart và cart item.
- [ ] Add/update/remove item.
- [ ] Tính subtotal.
Deliverable: cart API hoạt động.

Day 6 - Redis cache
- [ ] Cấu hình Redis.
- [ ] Cache danh sách product/category (TTL cơ bản).
- [ ] Evict cache khi create/update/delete.
Deliverable: có cache cho endpoint đọc nhiều.

Day 7 - Test + cleanup
- [ ] Viết test cho refresh + cart flow.
- [ ] Dọn code, xử lý lỗi edge case chính.
- [ ] Cập nhật checklist tuần 3.
Deliverable: tuần 3 ổn định để vào frontend + payment.

---

### Sprint 4 (Week 4) - VueJS + Checkout + Payment + RabbitMQ

#### Mục tiêu tuần
- [ ] Tạo frontend Vue (Vite).
- [ ] Tạo page: login, product list, cart, checkout, category basic.
- [ ] Axios interceptor attach access token + auto refresh.
- [ ] Route guard cho page cần auth.
- [ ] Tích hợp payment sandbox.
- [ ] Publish event order qua RabbitMQ (`order_created`).
- [ ] Viết hướng dẫn chạy local backend + frontend.

#### Output cần có
- [ ] Demo full flow: login -> xem product -> add cart -> checkout + payment sandbox.
- [ ] Sau checkout publish được event `order_created`.

#### Day 1-7

Day 1 - Setup Vue
- [ ] Khởi tạo frontend bằng Vite.
- [ ] Cấu hình router + base layout.
- [ ] Tạo service gọi API backend.
Deliverable: frontend chạy local và gọi được API health.

Day 2 - Login UI
- [ ] Tạo màn login.
- [ ] Lưu access token theo strategy đã chọn.
- [ ] Xử lý lỗi login cơ bản.
Deliverable: login từ UI thành công.

Day 3 - Product + Category UI
- [ ] Tạo trang product list.
- [ ] Tạo trang category basic.
- [ ] Thêm trạng thái loading/empty/error.
Deliverable: xem dữ liệu product/category từ UI.

Day 4 - Cart UI
- [ ] Tạo trang cart.
- [ ] Add/update/remove item từ UI.
- [ ] Hiển thị subtotal.
Deliverable: thao tác giỏ hàng hoàn chỉnh trên UI.

Day 5 - Auto refresh token
- [ ] Cấu hình Axios interceptor.
- [ ] Tự refresh access token khi 401 hợp lệ.
- [ ] Redirect login khi refresh thất bại.
Deliverable: UX auth mượt khi token hết hạn.

Day 6 - Payment sandbox + RabbitMQ
- [ ] Tích hợp payment sandbox mức cơ bản.
- [ ] Tạo callback/webhook xử lý thanh toán.
- [ ] Publish `order_created` khi checkout thành công.
Deliverable: checkout có payment và phát event.

Day 7 - Final demo + tài liệu
- [ ] Test end-to-end toàn flow.
- [ ] Ghi hướng dẫn run local backend/frontend trong README.
- [ ] Chụp ảnh/GIF demo (nếu cần portfolio).
Deliverable: bản MVP demo được trọn luồng.

---

## Definition of Done (MVP)

- [ ] JWT full flow hoạt động: register/login/refresh/logout.
- [ ] Category + Product + Cart + Order chạy end-to-end mức cơ bản.
- [ ] Redis cache áp dụng cho endpoint read-heavy chính.
- [ ] Payment sandbox chạy được luồng thanh toán tối thiểu.
- [ ] RabbitMQ publish được ít nhất 1 event order.
- [ ] Frontend Vue gọi API thật và demo được flow chính.

---

## Optional Backlog (Sau MVP)

- Mở rộng payment gateway production (retry, reconciliation).
- RabbitMQ consumer xử lý async (email, notification, stock sync).
- Redis nâng cao (rate limit, session, distributed lock).
- CI/CD + deploy cloud.

