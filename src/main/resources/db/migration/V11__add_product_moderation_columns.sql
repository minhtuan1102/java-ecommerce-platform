ALTER TABLE products
    ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS review_note VARCHAR(500);

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS approved_by INT;

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'public'
          AND table_name = 'products'
          AND constraint_name = 'fk_products_approved_by_users'
    ) THEN
        ALTER TABLE products
            ADD CONSTRAINT fk_products_approved_by_users
                FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END
$$;

UPDATE products
SET approval_status = 'APPROVED'
WHERE approval_status IS NULL;

