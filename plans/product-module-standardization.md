# Kế hoạch Triển khai & Chuẩn hóa Product, Category, và Brand

## 1. Mục tiêu (Objective)
Đồng bộ các module `Category`, `Brand`, và `Product` theo sát tài liệu thiết kế single-vendor trong `multi-vendor-architecture.md`. Hoàn thiện Product Module từ Database, Entity đến Service và API để phục vụ cho các chức năng lọc, tìm kiếm và hiển thị trên giao diện.

## 2. Bối cảnh & Động lực (Background & Motivation)
Hiện tại hệ thống đã có khung cơ bản, nhưng bảng `categories` và `brands` đang thiếu các trường quan trọng (`slug`, `parent_id`, `active`), khiến việc thiết kế URL thân thiện (SEO) và làm danh mục đa cấp bị hạn chế. `Product` cũng thiếu các trường thống kê như `average_rating` và `review_count`. Việc chuẩn hóa ngay từ bây giờ giúp tránh phải refactor lớn khi ghép nối với Frontend.

## 3. Phạm vi thay đổi (Scope & Impact)
- **Database:** Tạo file migration mới (ví dụ: `V16__standardize_catalog_schema.sql`) để cập nhật cấu trúc bảng.
- **Category Module:** Cập nhật Entity, DTO, Service để hỗ trợ `slug`, `active`, và cấu trúc cây (đa cấp).
- **Brand Module:** Cập nhật Entity, DTO, Service để hỗ trợ `slug` và `active`.
- **Product Module:** Cập nhật Entity, hoàn thiện Repository (tìm kiếm theo slug, category đa cấp), Service, và REST API.

## 4. Các bước triển khai (Implementation Steps)

### Pha 1: Cập nhật Database (Migration)
Tạo script Flyway `V16__standardize_catalog_schema.sql`:
- **Bảng `categories`:** 
  - Thêm `slug` (VARCHAR(150), UNIQUE).
  - Thêm `parent_id` (INT, FOREIGN KEY tham chiếu chính nó).
  - Thêm `active` (BOOLEAN, mặc định TRUE).
- **Bảng `brands`:**
  - Thêm `slug` (VARCHAR(255), UNIQUE).
  - Thêm `active` (BOOLEAN, mặc định TRUE).
- **Bảng `products`:**
  - Thêm `slug` (VARCHAR(255), UNIQUE).
  - Thêm `average_rating` (DECIMAL(3,2), mặc định 0.0).
  - Thêm `review_count` (INT, mặc định 0).

### Pha 2: Cập nhật Entity & Base Logic
- **Category Entity:** Bổ sung `slug`, `@ManyToOne Category parent`, `@OneToMany List<Category> children`, và `active`.
- **Brand Entity:** Bổ sung `slug` và `active`.
- **Product Entity:** Bổ sung `slug`, `averageRating`, `reviewCount`.
- **Slug Generator Utils:** Viết một Class Utility để tự động tạo `slug` từ `name` (xóa dấu, thay khoảng trắng bằng gạch ngang).

### Pha 3: Cập nhật DTO & Service cho Category/Brand
- Sửa đổi các DTO (Request/Response) để có thêm `slug` và `parentId`.
- Cập nhật Category Service để trả về cấu trúc cây (Nested JSON) nếu cần.
- Cập nhật logic Save: Tự động gọi SlugUtils để tạo slug nếu người dùng không nhập.

### Pha 4: Hoàn thiện Product Module
- **ProductRepository:** Viết thêm các Query (hoặc dùng Specification) để:
  - Tìm sản phẩm theo `slug`.
  - Tìm sản phẩm thuộc một Category và tất cả Category con của nó (dùng `IN` list category IDs).
- **ProductService:** 
  - Cập nhật logic tạo slug tự động từ tên sản phẩm.
  - Xử lý lưu Ảnh và SKU đồng bộ.
- **ProductController:** Hoàn thiện các endpoint:
  - `GET /api/products`: Lấy danh sách (phân trang, lọc theo category, brand, giá).
  - `GET /api/products/{slug}`: Lấy chi tiết sản phẩm qua slug.
  - `POST /api/admin/products`: Admin tạo sản phẩm.

## 5. Rủi ro & Lưu ý (Migration Note)
- Đối với dữ liệu cũ đã có trong DB, file migration cần có câu lệnh `UPDATE` để điền slug tạm thời (ví dụ: lấy name làm slug) trước khi gán ràng buộc `NOT NULL` và `UNIQUE`.
- Ví dụ: `UPDATE categories SET slug = LOWER(REPLACE(name, ' ', '-')) WHERE slug IS NULL;`
