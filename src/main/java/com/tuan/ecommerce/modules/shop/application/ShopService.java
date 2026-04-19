package com.tuan.ecommerce.modules.shop.application;

import com.tuan.ecommerce.modules.auth.domain.Role;
import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.role.RoleRepository;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import com.tuan.ecommerce.modules.shop.application.dto.CreateShopRequest;
import com.tuan.ecommerce.modules.shop.application.dto.ShopResponse;
import com.tuan.ecommerce.modules.shop.domain.Shop;
import com.tuan.ecommerce.modules.shop.infrastructure.mapper.ShopMapper;
import com.tuan.ecommerce.modules.shop.infrastructure.persistence.ShopRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ShopMapper shopMapper;

    public ShopService(ShopRepository shopRepository, UserRepository userRepository, RoleRepository roleRepository, ShopMapper shopMapper) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.shopMapper = shopMapper;
    }

    @Transactional
    public ShopResponse createShop(CreateShopRequest request, String ownerEmail) {
        User owner = userRepository.findByEmailIgnoreCase(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (shopRepository.findByOwnerId(owner.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have a shop");
        }

        if (shopRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Shop name already exists");
        }

        Shop shop = shopMapper.toEntity(request);
        shop.setOwner(owner);

        // Add ROLE_SELLER to user if not present
        boolean hasSellerRole = owner.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_SELLER"));
        
        if (!hasSellerRole) {
            Role sellerRole = roleRepository.findByName("ROLE_SELLER")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ROLE_SELLER not found"));
            owner.getRoles().add(sellerRole);
            userRepository.save(owner);
        }

        Shop savedShop = shopRepository.save(shop);
        return shopMapper.toResponse(savedShop);
    }

    @Transactional(readOnly = true)
    public ShopResponse getMyShop(String ownerEmail) {
        User owner = userRepository.findByEmailIgnoreCase(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Shop shop = shopRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found"));

        return shopMapper.toResponse(shop);
    }

    @Transactional(readOnly = true)
    public List<ShopResponse> getAllShops() {
        List<Shop> shops = shopRepository.findAll();
        return shopMapper.toResponseList(shops);
    }
}

