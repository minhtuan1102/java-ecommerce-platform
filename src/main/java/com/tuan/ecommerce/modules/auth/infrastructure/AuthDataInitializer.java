package com.tuan.ecommerce.modules.auth.infrastructure;

import com.tuan.ecommerce.modules.auth.domain.Role;
import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.role.RoleRepository;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import com.tuan.ecommerce.modules.category.domain.Category;
import com.tuan.ecommerce.modules.category.infrastructure.persistence.CategoryRepository;
import com.tuan.ecommerce.modules.brand.domain.Brand;
import com.tuan.ecommerce.modules.brand.infrastructure.persistence.BrandRepository;
import com.tuan.ecommerce.modules.product.domain.Product;
import com.tuan.ecommerce.modules.product.domain.ProductImage;
import com.tuan.ecommerce.modules.product.domain.ProductSKU;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public AuthDataInitializer(RoleRepository roleRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               CategoryRepository categoryRepository,
                               BrandRepository brandRepository,
                               ProductRepository productRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedUsers();
        Map<String, Category> categoriesByName = seedCategories();
        Brand defaultBrand = seedDefaultBrand();
        seedCatalogProducts(defaultBrand, categoriesByName);
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            System.out.println("Seed: Creating default roles");
            roleRepository.save(new Role("ROLE_CUSTOMER"));
            roleRepository.save(new Role("ROLE_ADMIN"));
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            System.out.println("Seed: Creating default users");
            Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                    .orElseThrow(() -> new RuntimeException("Role CUSTOMER not found"));
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));

            User admin = User.builder()
                    .username("admin")
                    .email("admin@ecommerce.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of(adminRole))
                    .active(true)
                    .build();

            User customer = User.builder()
                    .username("customer")
                    .email("customer@ecommerce.com")
                    .password(passwordEncoder.encode("customer123"))
                    .roles(Set.of(customerRole))
                    .active(true)
                    .build();

            userRepository.saveAll(List.of(admin, customer));
        }
    }

    private Map<String, Category> seedCategories() {
        Map<String, Category> result = new HashMap<>();

        if (categoryRepository.count() == 0) {
            System.out.println("Seed: Creating default categories");
            Category electronics = createCategory("Electronics", "Electronic devices and accessories");
            Category smartphones = createCategory("Smartphones", "Mobile phones", electronics);
            Category laptops = createCategory("Laptops", "Laptops and computers", electronics);

            Category fashion = createCategory("Fashion", "Clothing and apparel");
            Category mensFashion = createCategory("Men's Fashion", "Men's clothing", fashion);
            Category womensFashion = createCategory("Women's Fashion", "Women's clothing", fashion);

            categoryRepository.saveAll(List.of(electronics, smartphones, laptops, fashion, mensFashion, womensFashion));

            result.put("Smartphones", smartphones);
            result.put("Laptops", laptops);
            result.put("Men's Fashion", mensFashion);
            result.put("Women's Fashion", womensFashion);
        } else {
            List<Category> allCategories = categoryRepository.findAll();
            for (Category cat : allCategories) {
                result.put(cat.getName(), cat);
            }
        }

        return result;
    }

    private Category createCategory(String name, String description) {
        return createCategory(name, description, null);
    }

    private Category createCategory(String name, String description, Category parent) {
        return Category.builder()
                .name(name)
                .description(description)
                .parent(parent)
                .active(true)
                .build();
    }

    private Brand seedDefaultBrand() {
        return brandRepository.findByName("Generic")
                .orElseGet(() -> brandRepository.save(Brand.builder()
                        .name("Generic")
                        .description("Generic brand")
                        .logoUrl("https://via.placeholder.com/150")
                        .build()));
    }

    private void seedCatalogProducts(Brand brand, Map<String, Category> categoriesByName) {
        Set<String> existingProductNames = productRepository.findAll().stream()
                .map(Product::getName)
                .collect(Collectors.toSet());

        if (!existingProductNames.contains("iPhone 15 Pro Max")) {
            seedProductIfMissing(brand,
                    "iPhone 15 Pro Max",
                    "Latest Apple flagship smartphone with titanium design.",
                    categoriesByName.get("Smartphones"),
                    "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-15-pro-max-blue-titanium-select",
                    new BigDecimal("1199.00"), 50);
        }

        if (!existingProductNames.contains("MacBook Pro M3 14-inch")) {
            seedProductIfMissing(brand,
                    "MacBook Pro M3 14-inch",
                    "Supercharged by M3, M3 Pro, and M3 Max chips.",
                    categoriesByName.get("Laptops"),
                    "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/mbp14-spacegray-select-202310",
                    new BigDecimal("1599.00"), 30);
        }

        if (!existingProductNames.contains("Classic Cotton T-Shirt")) {
            seedProductIfMissing(brand,
                    "Classic Cotton T-Shirt",
                    "Comfortable 100% cotton t-shirt for everyday wear.",
                    categoriesByName.get("Men's Fashion"),
                    "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=500&q=80",
                    new BigDecimal("19.99"), 200);
        }

        if (!existingProductNames.contains("Floral Summer Dress")) {
            seedProductIfMissing(brand,
                    "Floral Summer Dress",
                    "Light and breezy dress perfect for summer days.",
                    categoriesByName.get("Women's Fashion"),
                    "https://images.unsplash.com/photo-1572804013309-82a89b4f959c?w=500&q=80",
                    new BigDecimal("39.99"), 100);
        }
    }

    private void seedProductIfMissing(Brand brand,
                                      String name,
                                      String description,
                                      Category category,
                                      String imageUrl,
                                      BigDecimal price,
                                      int stock) {
        
        System.out.println("Seed: Creating product - " + name);

        Product product = Product.builder()
                .name(name)
                .description(description)
                .category(category)
                .brand(brand)
                .active(true)
                .build();

        List<ProductImage> images = new ArrayList<>();
        images.add(ProductImage.builder()
                .product(product)
                .url(imageUrl)
                .main(true)
                .build());
        product.setImages(images);

        List<ProductSKU> skus = new ArrayList<>();
        skus.add(ProductSKU.builder()
                .product(product)
                .skuCode("SKU-" + name.replaceAll("\\s+", "-").toUpperCase())
                .price(price)
                .stock(stock)
                .build());
        product.setSkus(skus);

        productRepository.save(product);
    }
}