-- Some local databases still keep legacy NOT NULL constraints on products.price/products.stock.
-- Current domain model writes SKU-level price/stock, so legacy columns must be nullable.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'products'
          AND column_name = 'price'
          AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE products ALTER COLUMN price DROP NOT NULL;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'products'
          AND column_name = 'stock'
          AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE products ALTER COLUMN stock DROP NOT NULL;
    END IF;
END
$$;

