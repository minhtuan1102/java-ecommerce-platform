-- Normalize legacy cart/order schemas that still keep product_id after SKU migration.
-- Current code writes cart/order items by sku_id only.
DO $$
DECLARE
    rec RECORD;
BEGIN
    -- cart_items.product_id cleanup
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'cart_items'
          AND column_name = 'product_id'
    ) THEN
        FOR rec IN
            SELECT c.conname
            FROM pg_constraint c
            JOIN pg_class t ON t.oid = c.conrelid
            JOIN pg_namespace n ON n.oid = t.relnamespace
            JOIN unnest(c.conkey) AS col(attnum) ON TRUE
            JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = col.attnum
            WHERE n.nspname = 'public'
              AND t.relname = 'cart_items'
              AND a.attname = 'product_id'
        LOOP
            EXECUTE format('ALTER TABLE public.cart_items DROP CONSTRAINT IF EXISTS %I', rec.conname);
        END LOOP;

        ALTER TABLE public.cart_items DROP COLUMN IF EXISTS product_id;
    END IF;

    -- order_items.product_id cleanup
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'order_items'
          AND column_name = 'product_id'
    ) THEN
        FOR rec IN
            SELECT c.conname
            FROM pg_constraint c
            JOIN pg_class t ON t.oid = c.conrelid
            JOIN pg_namespace n ON n.oid = t.relnamespace
            JOIN unnest(c.conkey) AS col(attnum) ON TRUE
            JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = col.attnum
            WHERE n.nspname = 'public'
              AND t.relname = 'order_items'
              AND a.attname = 'product_id'
        LOOP
            EXECUTE format('ALTER TABLE public.order_items DROP CONSTRAINT IF EXISTS %I', rec.conname);
        END LOOP;

        ALTER TABLE public.order_items DROP COLUMN IF EXISTS product_id;
    END IF;

    -- Ensure sku foreign keys exist
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'cart_items'
          AND column_name = 'sku_id'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'public'
          AND table_name = 'cart_items'
          AND constraint_name = 'cart_items_sku_id_fkey'
    ) THEN
        ALTER TABLE public.cart_items
            ADD CONSTRAINT cart_items_sku_id_fkey
                FOREIGN KEY (sku_id) REFERENCES product_skus(id) ON DELETE CASCADE;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'order_items'
          AND column_name = 'sku_id'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = 'public'
          AND table_name = 'order_items'
          AND constraint_name = 'order_items_sku_id_fkey'
    ) THEN
        ALTER TABLE public.order_items
            ADD CONSTRAINT order_items_sku_id_fkey
                FOREIGN KEY (sku_id) REFERENCES product_skus(id) ON DELETE RESTRICT;
    END IF;
END
$$;

