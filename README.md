# 🛒 Ecommerce Platform (Marketplace Model)

Dự án xây dựng hệ thống thương mại điện tử mô phỏng mô hình **Shopee**, tập trung vào kiến trúc sạch (Clean Architecture) và quy trình nghiệp vụ thực tế giữa Người mua, Người bán và Quản trị viên.

## 🏗 System Architecture (Modular Monolith)
- **Core Modules**: `Auth`, `Category`, `Product`, `Cart`, `Order`, `Inventory`, `Shop`.
- **Layers**: API ➔ Application ➔ Domain ➔ Infrastructure.

---

## 👥 User Roles & Permissions

| Role | Description | Core Features |
| :--- | :--- | :--- |
| **Admin** | Quản trị sàn | Quản lý Danh mục, Phê duyệt Shop/Sản phẩm, Thống kê toàn sàn. |
| **Seller** | Người bán hàng | Đăng ký gian hàng (Shop), Quản lý Sản phẩm/SKU, Xử lý đơn hàng. |
| **Buyer** | Người mua hàng | Tìm kiếm sản phẩm, Giỏ hàng, Đặt hàng, Theo dõi đơn hàng. |

---

## 📅 Lộ trình chi tiết theo từng Sprint

### 📌 Giai đoạn 1: Nền tảng (Foundation) - Đang thực hiện

#### **Sprint 1 (Week 1) - Auth Skeleton + PostgreSQL (Hoàn thành)**
*Mục tiêu tuần: Khởi tạo project và kết nối DB, thiết kế cơ sở User/Role.*
- [x] Day 1: Kết nối DB PostgreSQL local, boot app không lỗi.
- [x] Day 2: Tạo schema auth (users, roles, user_roles, refresh_tokens).
- [x] Day 3: Khung module auth (api/application/domain/infrastructure).
- [x] Day 4: Entity mapping (User, Role, RefreshToken) và Repository.
- [x] Day 5: API skeleton (`/health`, `/register`, `/login`).
- [x] Day 6: Seed dữ liệu (`ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN` và 1 admin account).
- [x] Day 7: Test nền và dọn code.

#### **Sprint 2 (Week 2) - Register/Login + Access Token**
*Mục tiêu tuần: Triển khai Spring Security và luồng JWT.*
- [x] Day 1: Tạo `SecurityConfig` với public/private endpoint.
- [x] Day 2: Implement `POST /api/v1/auth/register` (BCrypt password).
- [x] Day 3: Implement `POST /api/v1/auth/login` (Trả JWT access token).
- [x] Day 4: JWT middleware (Filter đọc Authorization header, Verify token).
- [x] Day 5: Bảo vệ endpoint Category bằng JWT theo policy.
- [x] Day 6: Viết test cho register/login.
- [x] Day 7: Dọn code, xử lý error format chung, chuẩn bị vào Marketplace.

---

### 🚀 Giai đoạn 2: Marketplace Activation (Mở rộng giống Shopee)

#### **Sprint 3 (Week 3): Module Shop & Seller Registration**
*Mục tiêu tuần: Cho phép người dùng bình thường đăng ký mở shop và nhận quyền ROLE_SELLER.*
- [x] Day 1-2: Thiết kế Database cho `Shop` (name, owner_id, status) & Flyway migration.
- [x] Day 3: API `POST /api/v1/shops` (Đăng ký shop & Tự động cập nhật `ROLE_SELLER`).
- [x] Day 4: API `GET /api/v1/shops/my-shop` (Quản lý thông tin gian hàng).
- [x] Day 5: Security: Cấu hình phân quyền truy cập endpoint Seller.
- [x] Day 6-7: Viết Integration Test cho luồng đăng ký shop.

#### **Sprint 4 (Week 4): Module Product & SKU Variations**
*Mục tiêu tuần: Hệ thống sản phẩm đa biến thể (Màu sắc, Kích cỡ) như Shopee.*
- [ ] Day 1-2: Thiết kế Schema `Product` & `ProductSKU` (Biến thể kèm giá/kho riêng).
- [ ] Day 3-4: API Seller: `POST /api/v1/seller/products` (Tạo sản phẩm & SKU).
- [ ] Day 5: API Seller: Quản lý danh sách và trạng thái ẩn/hiện sản phẩm.
- [ ] Day 6: Media: Tích hợp Upload ảnh sản phẩm (Cloudinary/S3).
- [ ] Day 7: Validation: Ràng buộc logic Shop - Product - Category.

#### **Sprint 5 (Week 5): Smart Cart & Marketplace Checkout**
*Mục tiêu tuần: Giỏ hàng hỗ trợ nhiều shop và xử lý trừ kho an toàn.*
- [ ] Day 1-2: Module Cart: Lưu trữ giỏ hàng, tính toán giá real-time từ SKU.
- [ ] Day 3: API Checkout Preview: Gom nhóm sản phẩm theo Shop & Tính phí ship.
- [ ] Day 4-5: API `POST /api/v1/orders`: Xử lý trừ kho an toàn (Pessimistic Locking).
- [ ] Day 6: Mock Payment: Tích hợp quy trình thanh toán giả lập.
- [ ] Day 7: Test: Kiểm tra toàn bộ luồng từ giỏ hàng đến khi tạo đơn.

#### **Sprint 6 (Week 6): Order Workflow & Admin Dashboard**
*Mục tiêu tuần: Quy trình trạng thái đơn hàng và công cụ quản trị.*
- [ ] Day 1-2: Order State Machine: Quy trình `PENDING` -> `SHIPPING` -> `DELIVERED`.
- [ ] Day 3: Buyer View: Theo dõi lộ trình và trạng thái đơn hàng.
- [ ] Day 4: Admin API: Thống kê doanh thu, quản lý danh sách người dùng.
- [ ] Day 5: Admin Review: Phê duyệt/Khóa sản phẩm và Shop vi phạm.
- [ ] Day 6-7: Documentation: Hoàn thiện Swagger API & Hướng dẫn sử dụng.

---

## 🛠 Tech Stack
- **Backend**: Java 17, Spring Boot 3, Spring Security.
- **Database**: PostgreSQL (Persistence), Redis (Caching).
- **Migration**: Flyway (Quản lý version database).
