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
import com.tuan.ecommerce.modules.product.domain.ProductApprovalStatus;
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
        Map<String, Brand> brandsByName = seedBrands();
        seedCatalogProducts(brandsByName, categoriesByName);
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role("ROLE_CUSTOMER"));
            roleRepository.save(new Role("ROLE_ADMIN"));
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
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
        Category electronics = ensureCategoryExists("Điện tử", "Thiết bị điện tử và phụ kiện", null);
        ensureCategoryExists("Điện thoại", "Điện thoại di động và smartphone", electronics);
        ensureCategoryExists("Máy tính", "Máy tính xách tay và máy tính để bàn", electronics);
        ensureCategoryExists("Tivi", "Smart TV và hệ thống giải trí", electronics);

        Category appliances = ensureCategoryExists("Điện gia dụng", "Các thiết bị điện gia dụng trong gia đình", null);
        ensureCategoryExists("Đồ dùng nhà bếp", "Tủ lạnh, lò vi sóng, v.v.", appliances);
        ensureCategoryExists("Thiết bị làm sạch", "Máy hút bụi, máy lọc không khí", appliances);

        Category fashion = ensureCategoryExists("Thời trang", "Quần áo, giày dép và phụ kiện", null);
        ensureCategoryExists("Thời trang nam", "Trang phục và phụ kiện dành cho nam", fashion);
        ensureCategoryExists("Thời trang nữ", "Trang phục và phụ kiện dành cho nữ", fashion);

        Category home = ensureCategoryExists("Nhà cửa", "Đồ dùng và trang trí nhà cửa", null);
        ensureCategoryExists("Nội thất", "Bàn ghế, kệ tủ và đồ nội thất", home);
        ensureCategoryExists("Trang trí", "Đèn, tranh và vật dụng trang trí", home);

        Map<String, Category> result = new HashMap<>();
        List<Category> allCategories = categoryRepository.findAll();
        for (Category cat : allCategories) {
            result.put(cat.getName(), cat);
        }
        return result;
    }

    private Category ensureCategoryExists(String name, String description, Category parent) {
        return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Category category = Category.builder()
                            .name(name)
                            .description(description)
                            .parent(parent)
                            .active(true)
                            .build();
                    category.generateSlug();
                    return categoryRepository.save(category);
                });
    }

    private Map<String, Brand> seedBrands() {
        List<Brand> brands = List.of(
                ensureBrandExists("Generic", "Thương hiệu chung", "https://via.placeholder.com/150"),
                ensureBrandExists("Apple", "Thiết bị công nghệ Apple", "https://via.placeholder.com/150?text=Apple"),
                ensureBrandExists("Samsung", "Thiết bị điện tử và gia dụng Samsung", "https://via.placeholder.com/150?text=Samsung"),
                ensureBrandExists("Sony", "Thiết bị nghe nhìn Sony", "https://via.placeholder.com/150?text=Sony"),
                ensureBrandExists("Sharp", "Thiết bị gia dụng Sharp", "https://via.placeholder.com/150?text=Sharp"),
                ensureBrandExists("Anker", "Phụ kiện sạc và thiết bị thông minh", "https://via.placeholder.com/150?text=Anker"),
                ensureBrandExists("Uniqlo", "Thời trang cơ bản hằng ngày", "https://via.placeholder.com/150?text=Uniqlo"),
                ensureBrandExists("IKEA", "Nội thất và đồ dùng nhà cửa", "https://via.placeholder.com/150?text=IKEA")
        );

        return brands.stream().collect(Collectors.toMap(Brand::getName, brand -> brand));
    }

    private Brand ensureBrandExists(String name, String description, String logoUrl) {
        return brandRepository.findByName(name)
                .orElseGet(() -> {
                    Brand brand = Brand.builder()
                            .name(name)
                            .description(description)
                            .logoUrl(logoUrl)
                            .build();
                    brand.generateSlug();
                    return brandRepository.save(brand);
                });
    }

    private void seedCatalogProducts(Map<String, Brand> brandsByName, Map<String, Category> categoriesByName) {
        Set<String> existingProductNames = productRepository.findAll().stream()
                .map(Product::getName)
                .collect(Collectors.toSet());

        if (!existingProductNames.contains("iPhone 15 Pro Max")) {
            seedProductIfMissing(brandsByName.get("Apple"),
                    "iPhone 15 Pro Max",
                    "Điện thoại flagship mới nhất của Apple với thiết kế titan.",
                    categoriesByName.get("Điện thoại"),
                    "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-15-pro-max-blue-titanium-select",
                    new BigDecimal("34990000"), 50);
        }

        if (!existingProductNames.contains("MacBook Pro M3 14 inch")) {
            seedProductIfMissing(brandsByName.get("Apple"),
                    "MacBook Pro M3 14 inch",
                    "Mạnh mẽ vượt trội với chip M3, M3 Pro và M3 Max.",
                    categoriesByName.get("Máy tính"),
                    "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/mbp14-spacegray-select-202310",
                    new BigDecimal("49990000"), 30);
        }

        if (!existingProductNames.contains("Tivi Sony Bravia 4K 55 inch")) {
            seedProductIfMissing(brandsByName.get("Sony"),
                    "Tivi Sony Bravia 4K 55 inch",
                    "Trải nghiệm chất lượng hình ảnh 4K HDR tuyệt đỉnh cùng các tính năng Smart TV.",
                    categoriesByName.get("Tivi"),
                    "https://via.placeholder.com/500x500?text=Sony+Bravia+TV",
                    new BigDecimal("15990000"), 20);
        }

        if (!existingProductNames.contains("Tủ lạnh Samsung Inverter 400L")) {
            seedProductIfMissing(brandsByName.get("Samsung"),
                    "Tủ lạnh Samsung Inverter 400L",
                    "Máy nén biến tần kỹ thuật số tiết kiệm điện với công nghệ Twin Cooling Plus.",
                    categoriesByName.get("Đồ dùng nhà bếp"),
                    "https://via.placeholder.com/500x500?text=Samsung+Fridge",
                    new BigDecimal("12500000"), 15);
        }

        if (!existingProductNames.contains("Máy lọc không khí Sharp KC-G40EV-W")) {
            seedProductIfMissing(brandsByName.get("Sharp"),
                    "Máy lọc không khí Sharp KC-G40EV-W",
                    "Công nghệ Plasmacluster Ion giúp không khí sạch và trong lành.",
                    categoriesByName.get("Thiết bị làm sạch"),
                    "https://via.placeholder.com/500x500?text=Sharp+Air+Purifier",
                    new BigDecimal("6800000"), 40);
        }

        if (!existingProductNames.contains("Samsung Galaxy S24 Ultra")) {
            seedProductIfMissing(brandsByName.get("Samsung"),
                    "Samsung Galaxy S24 Ultra",
                    "Smartphone cao cấp với màn hình Dynamic AMOLED, bút S Pen và camera zoom xa.",
                    categoriesByName.get("Điện thoại"),
                    "https://via.placeholder.com/500x500?text=Galaxy+S24+Ultra",
                    new BigDecimal("28990000"), 35);
        }

        if (!existingProductNames.contains("Tai nghe Sony WH-1000XM5")) {
            seedProductIfMissing(brandsByName.get("Sony"),
                    "Tai nghe Sony WH-1000XM5",
                    "Tai nghe chống ồn chủ động, pin dài và chất âm chi tiết.",
                    categoriesByName.get("Điện tử"),
                    "https://via.placeholder.com/500x500?text=Sony+WH-1000XM5",
                    new BigDecimal("7990000"), 25);
        }

        if (!existingProductNames.contains("Sạc nhanh Anker Nano 65W")) {
            seedProductIfMissing(brandsByName.get("Anker"),
                    "Sạc nhanh Anker Nano 65W",
                    "Củ sạc GaN nhỏ gọn hỗ trợ sạc nhanh laptop, tablet và điện thoại.",
                    categoriesByName.get("Điện tử"),
                    "https://via.placeholder.com/500x500?text=Anker+Nano+65W",
                    new BigDecimal("890000"), 120);
        }

        if (!existingProductNames.contains("Áo thun nam cotton basic")) {
            seedProductIfMissing(brandsByName.get("Uniqlo"),
                    "Áo thun nam cotton basic",
                    "Áo thun cotton mềm, form dễ mặc cho sinh hoạt hằng ngày.",
                    categoriesByName.get("Thời trang nam"),
                    "https://via.placeholder.com/500x500?text=Men+Cotton+T-Shirt",
                    new BigDecimal("249000"), 200);
        }

        if (!existingProductNames.contains("Váy midi nữ dáng suông")) {
            seedProductIfMissing(brandsByName.get("Uniqlo"),
                    "Váy midi nữ dáng suông",
                    "Váy midi nhẹ, dáng suông thanh lịch cho đi làm và đi chơi.",
                    categoriesByName.get("Thời trang nữ"),
                    "https://via.placeholder.com/500x500?text=Midi+Dress",
                    new BigDecimal("599000"), 80);
        }

        if (!existingProductNames.contains("Kệ sách IKEA Billy 5 tầng")) {
            seedProductIfMissing(brandsByName.get("IKEA"),
                    "Kệ sách IKEA Billy 5 tầng",
                    "Kệ sách 5 tầng tối giản, dễ phối với phòng khách hoặc góc làm việc.",
                    categoriesByName.get("Nội thất"),
                    "https://via.placeholder.com/500x500?text=IKEA+Billy+Shelf",
                    new BigDecimal("1890000"), 45);
        }

        if (!existingProductNames.contains("Đèn bàn LED chống cận")) {
            seedProductIfMissing(brandsByName.get("Generic"),
                    "Đèn bàn LED chống cận",
                    "Đèn bàn LED nhiều mức sáng, phù hợp học tập và làm việc tại nhà.",
                    categoriesByName.get("Trang trí"),
                    "https://via.placeholder.com/500x500?text=LED+Desk+Lamp",
                    new BigDecimal("459000"), 90);
        }

        approveSeedProducts();
    }

    private void approveSeedProducts() {
        Set<String> seedProductNames = Set.of(
                "iPhone 15 Pro Max",
                "MacBook Pro M3 14 inch",
                "Tivi Sony Bravia 4K 55 inch",
                "Tủ lạnh Samsung Inverter 400L",
                "Máy lọc không khí Sharp KC-G40EV-W",
                "Samsung Galaxy S24 Ultra",
                "Tai nghe Sony WH-1000XM5",
                "Sạc nhanh Anker Nano 65W",
                "Áo thun nam cotton basic",
                "Váy midi nữ dáng suông",
                "Kệ sách IKEA Billy 5 tầng",
                "Đèn bàn LED chống cận"
        );

        productRepository.findAll().stream()
                .filter(product -> seedProductNames.contains(product.getName()))
                .filter(product -> product.getApprovalStatus() != ProductApprovalStatus.APPROVED)
                .forEach(product -> {
                    product.setApprovalStatus(ProductApprovalStatus.APPROVED);
                    productRepository.save(product);
                });
    }

    private void seedProductIfMissing(Brand brand,
                                      String name,
                                      String description,
                                      Category category,
                                      String imageUrl,
                                      BigDecimal price,
                                      int stock) {
        Product product = Product.builder()
                .name(name)
                .description(description)
                .category(category)
                .brand(brand)
                .active(true)
                .approvalStatus(ProductApprovalStatus.APPROVED)
                .build();
        product.generateSlug();

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
