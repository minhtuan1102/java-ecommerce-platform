-- Xóa dữ liệu cũ nếu có để tránh lỗi khóa ngoại
DELETE FROM cart_items;
DELETE FROM order_items;

-- Cập nhật bảng cart_items
ALTER TABLE cart_items DROP CONSTRAINT cart_items_product_id_fkey;
ALTER TABLE cart_items RENAME COLUMN product_id TO sku_id;
ALTER TABLE cart_items ADD CONSTRAINT cart_items_sku_id_fkey FOREIGN KEY (sku_id) REFERENCES product_skus(id) ON DELETE CASCADE;

-- Cập nhật bảng order_items
ALTER TABLE order_items DROP CONSTRAINT order_items_product_id_fkey;
ALTER TABLE order_items RENAME COLUMN product_id TO sku_id;
ALTER TABLE order_items ADD CONSTRAINT order_items_sku_id_fkey FOREIGN KEY (sku_id) REFERENCES product_skus(id) ON DELETE RESTRICT;
