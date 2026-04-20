-- Thêm ảnh sản phẩm
CREATE TABLE product_images (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL,
    url TEXT NOT NULL,
    is_main BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Thêm SKU cho sản phẩm đa biến thể
CREATE TABLE product_skus (
    id SERIAL PRIMARY KEY,
    product_id INT NOT NULL,
    sku_code VARCHAR(100) UNIQUE, -- Mã quản lý nội bộ (ví dụ: AO-THUN-RED-XL)
    tier_index VARCHAR(50), -- Lưu index của biến thể (ví dụ: [0, 1] tương ứng Màu Đỏ, Size XL)
    price DECIMAL(12, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Cập nhật bảng products: Gỡ bỏ price/stock trực tiếp, thêm trạng thái
ALTER TABLE products DROP COLUMN price;
ALTER TABLE products DROP COLUMN stock;
ALTER TABLE products ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
ALTER TABLE products ADD COLUMN brand VARCHAR(100);
