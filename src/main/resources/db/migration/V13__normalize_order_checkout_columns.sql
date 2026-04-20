-- Ensure checkout-required order columns exist on legacy databases.
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS shipping_address VARCHAR(500) NOT NULL DEFAULT '';

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20) NOT NULL DEFAULT '';

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(20) NOT NULL DEFAULT 'COD';

UPDATE orders
SET payment_method = 'COD'
WHERE payment_method IS NULL OR TRIM(payment_method) = '';

