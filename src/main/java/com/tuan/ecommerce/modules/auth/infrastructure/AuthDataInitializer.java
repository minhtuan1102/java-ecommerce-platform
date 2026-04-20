package com.tuan.ecommerce.modules.auth.infrastructure;

import com.tuan.ecommerce.modules.auth.domain.Role;
import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.role.RoleRepository;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import com.tuan.ecommerce.modules.cart.domain.Cart;
import com.tuan.ecommerce.modules.cart.domain.CartItem;
import com.tuan.ecommerce.modules.cart.infrastructure.persistence.CartRepository;
import com.tuan.ecommerce.modules.category.domain.Category;
import com.tuan.ecommerce.modules.category.infrastructure.persistence.CategoryRepository;
import com.tuan.ecommerce.modules.product.domain.Product;
import com.tuan.ecommerce.modules.product.domain.ProductApprovalStatus;
import com.tuan.ecommerce.modules.product.domain.ProductImage;
import com.tuan.ecommerce.modules.product.domain.ProductSKU;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductRepository;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductSkuJpaRepository;
import com.tuan.ecommerce.modules.shop.domain.Shop;
import com.tuan.ecommerce.modules.shop.domain.ShopStatus;
import com.tuan.ecommerce.modules.shop.infrastructure.persistence.ShopRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final ProductSkuJpaRepository productSkuJpaRepository;
    private final CartRepository cartRepository;
    private final JdbcTemplate jdbcTemplate;

    public AuthDataInitializer(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder,
                               CategoryRepository categoryRepository,
                               ShopRepository shopRepository,
                               ProductRepository productRepository,
                               ProductSkuJpaRepository productSkuJpaRepository,
                               CartRepository cartRepository,
                               JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
        this.productSkuJpaRepository = productSkuJpaRepository;
        this.cartRepository = cartRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        normalizeLegacyCartOrderItemColumnsIfNeeded();
        normalizeLegacyOrderCheckoutColumnsIfNeeded();

        // Seed roles
        Role userRole = seedRole("ROLE_USER");
        Role adminRole = seedRole("ROLE_ADMIN");
        Role sellerRole = seedRole("ROLE_SELLER");

        User admin = seedUser("admin", "admin@ecommerce.com", "admin123", Set.of(userRole, adminRole));
        User seller = seedUser("seller", "seller@ecommerce.com", "seller123", Set.of(userRole, sellerRole));
        User buyer = seedUser("buyer", "buyer@ecommerce.com", "buyer123", Set.of(userRole));

        try {
            seedMarketplaceData(seller, buyer);
        } catch (RuntimeException ex) {
            System.out.println("Seed: skipped marketplace demo data because schema is not compatible yet. " + ex.getMessage());
        }

        // Keep reference so variables are not optimized away in future edits.
        if (admin.getId() == null) {
            throw new IllegalStateException("Admin seed failed");
        }
    }

    private Role seedRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = Role.builder().name(roleName).build();
                    System.out.println("Seed: Created role " + roleName);
                    return roleRepository.save(role);
                });
    }

    private User seedUser(String username, String email, String rawPassword, Set<Role> roles) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    User user = User.builder()
                            .username(username)
                            .email(email)
                            .password(passwordEncoder.encode(rawPassword))
                            .roles(roles)
                            .build();
                    System.out.println("Seed: Created user " + email);
                    return userRepository.save(user);
                });
    }

    private void seedMarketplaceData(User seller, User buyer) {
        normalizeLegacyProductColumnsIfNeeded();

        Map<String, String> categoryDefinitions = new LinkedHashMap<>();
        categoryDefinitions.put("Electronics", "Phones, accessories and electronic devices");
        categoryDefinitions.put("Fashion", "Clothing, shoes and accessories");
        categoryDefinitions.put("Home & Living", "Furniture, decor and home essentials");
        categoryDefinitions.put("Beauty", "Skincare, makeup and personal care");
        categoryDefinitions.put("Sports", "Fitness, outdoor and sports equipment");
        categoryDefinitions.put("Books", "Books, comics and educational materials");
        categoryDefinitions.put("Toys", "Toys and games for kids and families");
        categoryDefinitions.put("Grocery", "Food, beverages and daily grocery supplies");
        categoryDefinitions.put("Health", "Health devices and wellness products");
        categoryDefinitions.put("Automotive", "Car and motorbike accessories");

        Map<String, Category> categoriesByName = categoryDefinitions.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> seedCategory(entry.getKey(), entry.getValue()),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        Shop demoShop = seedShopForSeller(seller);

        if (hasLegacyNotNullProductColumns()) {
            System.out.println("Seed: product/cart demo data skipped (products.price or products.stock is still NOT NULL).");
            return;
        }

        seedCatalogProducts(demoShop, categoriesByName);
        seedBuyerCart(buyer);
    }

    private void seedCatalogProducts(Shop shop, Map<String, Category> categoriesByName) {
        Set<String> existingProductNames = productRepository.findByShopId(shop.getId()).stream()
                .map(product -> product.getName().toLowerCase())
                .collect(Collectors.toSet());

        int createdCount = 0;
        int categoryIndex = 0;
        for (Map.Entry<String, Category> entry : categoriesByName.entrySet()) {
            categoryIndex++;
            String categoryName = entry.getKey();
            Category category = entry.getValue();
            String categoryCode = categoryCode(categoryName, categoryIndex);

            for (int productIndex = 1; productIndex <= 20; productIndex++) {
                String productName = String.format("%s Sample Product %02d", categoryName, productIndex);
                if (existingProductNames.contains(productName.toLowerCase())) {
                    continue;
                }

                List<String> imageUrls = List.of(
                        String.format("https://picsum.photos/seed/%s-%02d-main/800/800", categoryCode.toLowerCase(), productIndex),
                        String.format("https://picsum.photos/seed/%s-%02d-alt/800/800", categoryCode.toLowerCase(), productIndex)
                );

                List<ProductSKU> skus = buildSampleSkus(categoryCode, productIndex);

                seedProductIfMissing(
                        shop,
                        category,
                        productName,
                        String.format("%s demo item %02d for manual test scenarios", categoryName, productIndex),
                        String.format("Brand-%s", categoryCode),
                        imageUrls,
                        skus
                );

                existingProductNames.add(productName.toLowerCase());
                createdCount++;
            }
        }

        if (createdCount > 0) {
            System.out.println("Seed: Created " + createdCount + " sample products across 10 categories");
        }
    }

    private List<ProductSKU> buildSampleSkus(String categoryCode, int productIndex) {
        String baseSku = String.format("%s-%02d", categoryCode, productIndex);
        List<ProductSKU> skus = new ArrayList<>();
        skus.add(ProductSKU.builder()
                .skuCode(baseSku + "-STD")
                .tierIndex("STD")
                .price(basePrice(productIndex, 1))
                .stock(30 + (productIndex % 15))
                .build());
        skus.add(ProductSKU.builder()
                .skuCode(baseSku + "-PLUS")
                .tierIndex("PLUS")
                .price(basePrice(productIndex, 2))
                .stock(20 + (productIndex % 12))
                .build());
        skus.add(ProductSKU.builder()
                .skuCode(baseSku + "-MAX")
                .tierIndex("MAX")
                .price(basePrice(productIndex, 3))
                .stock(10 + (productIndex % 10))
                .build());
        return skus;
    }

    private BigDecimal basePrice(int productIndex, int variantIndex) {
        int base = 90000 + (productIndex * 7000);
        int multiplier = switch (variantIndex) {
            case 1 -> 1;
            case 2 -> 2;
            default -> 3;
        };
        return BigDecimal.valueOf((long) base * multiplier);
    }

    private String categoryCode(String categoryName, int fallbackIndex) {
        String lettersOnly = categoryName.replaceAll("[^A-Za-z]", "").toUpperCase();
        if (lettersOnly.length() >= 3) {
            return lettersOnly.substring(0, 3);
        }
        return String.format("CAT%02d", fallbackIndex);
    }

    private Category seedCategory(String name, String description) {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Category category = Category.builder()
                            .name(name)
                            .description(description)
                            .build();
                    System.out.println("Seed: Created category " + name);
                    return categoryRepository.save(category);
                });
    }

    private Shop seedShopForSeller(User seller) {
        return shopRepository.findByOwnerId(seller.getId())
                .orElseGet(() -> {
                    Shop shop = Shop.builder()
                            .name("Demo Seller Shop")
                            .description("Sample shop for end-to-end testing")
                            .owner(seller)
                            .status(ShopStatus.ACTIVE)
                            .build();
                    System.out.println("Seed: Created demo shop");
                    return shopRepository.save(shop);
                });
    }

    private void seedProductIfMissing(Shop shop,
                                      Category category,
                                      String productName,
                                      String description,
                                      String brand,
                                      List<String> imageUrls,
                                      List<ProductSKU> skuTemplates) {
        boolean exists = productRepository.findByShopId(shop.getId()).stream()
                .anyMatch(product -> product.getName().equalsIgnoreCase(productName));
        if (exists) {
            return;
        }

        Product product = Product.builder()
                .name(productName)
                .description(description)
                .brand(brand)
                .active(true)
                .approvalStatus(ProductApprovalStatus.APPROVED)
                .reviewNote("Auto approved seed data")
                .category(category)
                .shop(shop)
                .build();

        List<ProductImage> images = imageUrls.stream()
                .map(url -> ProductImage.builder()
                        .product(product)
                        .url(url)
                        .main(url.equals(imageUrls.get(0)))
                        .build())
                .toList();

        List<ProductSKU> skus = skuTemplates.stream()
                .map(template -> ProductSKU.builder()
                        .product(product)
                        .skuCode(template.getSkuCode())
                        .tierIndex(template.getTierIndex())
                        .price(template.getPrice())
                        .stock(template.getStock())
                        .build())
                .toList();

        product.setImages(images);
        product.setSkus(skus);

        productRepository.save(product);
        System.out.println("Seed: Created product " + productName);
    }

    private void seedBuyerCart(User buyer) {
        Cart cart = cartRepository.findByUserId(buyer.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(buyer).build();
                    return cartRepository.save(newCart);
                });

        if (!cart.getItems().isEmpty()) {
            return;
        }

        productSkuJpaRepository.findAll().stream()
                .filter(sku -> sku.getSkuCode() != null && sku.getSkuCode().endsWith("-STD"))
                .limit(2)
                .forEach(sku -> cart.getItems().add(CartItem.builder()
                        .cart(cart)
                        .sku(sku)
                        .quantity(1)
                        .build()));

        if (!cart.getItems().isEmpty()) {
            cartRepository.save(cart);
            System.out.println("Seed: Added sample items to buyer cart");
        }
    }

    private boolean hasLegacyNotNullProductColumns() {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = 'products'
                  AND column_name IN ('price', 'stock')
                  AND is_nullable = 'NO'
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null && count > 0;
    }

    private void normalizeLegacyProductColumnsIfNeeded() {
        String sql = """
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
                """;

        jdbcTemplate.execute(sql);
    }

    private void normalizeLegacyCartOrderItemColumnsIfNeeded() {
        String sql = """
                DO $$
                DECLARE
                    rec RECORD;
                BEGIN
                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
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

                    IF EXISTS (
                        SELECT 1 FROM information_schema.columns
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
                END
                $$;
                """;

        jdbcTemplate.execute(sql);
    }

    private void normalizeLegacyOrderCheckoutColumnsIfNeeded() {
        String sql = """
                ALTER TABLE orders
                    ADD COLUMN IF NOT EXISTS shipping_address VARCHAR(500) NOT NULL DEFAULT '';

                ALTER TABLE orders
                    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20) NOT NULL DEFAULT '';

                ALTER TABLE orders
                    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(20) NOT NULL DEFAULT 'COD';

                UPDATE orders
                SET payment_method = 'COD'
                WHERE payment_method IS NULL OR TRIM(payment_method) = '';
                """;

        jdbcTemplate.execute(sql);
    }
}
