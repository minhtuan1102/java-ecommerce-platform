package com.tuan.ecommerce.modules.order.application;

import com.tuan.ecommerce.modules.auth.domain.Role;
import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import com.tuan.ecommerce.modules.cart.infrastructure.persistence.CartRepository;
import com.tuan.ecommerce.modules.order.domain.Order;
import com.tuan.ecommerce.modules.order.domain.OrderStatus;
import com.tuan.ecommerce.modules.order.infrastructure.mapper.OrderMapper;
import com.tuan.ecommerce.modules.order.infrastructure.persistence.OrderRepository;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductSkuRepository;
import com.tuan.ecommerce.modules.shop.domain.Shop;
import com.tuan.ecommerce.modules.shop.infrastructure.persistence.ShopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        CartRepository cartRepository = mock(CartRepository.class);
        userRepository = mock(UserRepository.class);
        ProductSkuRepository skuRepository = mock(ProductSkuRepository.class);
        ShopRepository shopRepository = mock(ShopRepository.class);

        orderService = new OrderService(
                orderRepository,
                cartRepository,
                userRepository,
                skuRepository,
                shopRepository,
                new OrderMapper()
        );

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void updateOrderStatus_shouldAllowSellerValidTransition() {
        User owner = buildUser(1L, "seller@mail.com", "ROLE_SELLER");
        Order order = buildOrder(owner, OrderStatus.PENDING);

        when(orderRepository.findById(10L)).thenReturn(java.util.Optional.of(order));
        when(userRepository.findByEmailIgnoreCase("seller@mail.com")).thenReturn(java.util.Optional.of(owner));

        var response = orderService.updateOrderStatus(10L, OrderStatus.PROCESSING, "seller@mail.com");

        assertEquals(OrderStatus.PROCESSING, response.getStatus());
    }

    @Test
    void updateOrderStatus_shouldRejectInvalidTransition() {
        User owner = buildUser(1L, "seller@mail.com", "ROLE_SELLER");
        Order order = buildOrder(owner, OrderStatus.PENDING);

        when(orderRepository.findById(11L)).thenReturn(java.util.Optional.of(order));
        when(userRepository.findByEmailIgnoreCase("seller@mail.com")).thenReturn(java.util.Optional.of(owner));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> orderService.updateOrderStatus(11L, OrderStatus.SHIPPED, "seller@mail.com")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateOrderStatus_shouldAllowAdminToUpdateOtherShopOrder() {
        User owner = buildUser(1L, "seller@mail.com", "ROLE_SELLER");
        User admin = buildUser(2L, "admin@mail.com", "ROLE_ADMIN");
        Order order = buildOrder(owner, OrderStatus.SHIPPED);

        when(orderRepository.findById(12L)).thenReturn(java.util.Optional.of(order));
        when(userRepository.findByEmailIgnoreCase("admin@mail.com")).thenReturn(java.util.Optional.of(admin));

        var response = orderService.updateOrderStatus(12L, OrderStatus.DELIVERED, "admin@mail.com");

        assertEquals(OrderStatus.DELIVERED, response.getStatus());
    }

    private User buildUser(Long id, String email, String roleName) {
        return User.builder()
                .id(id)
                .email(email)
                .username(email)
                .password("secret")
                .roles(Set.of(Role.builder().name(roleName).build()))
                .build();
    }

    private Order buildOrder(User owner, OrderStatus status) {
        Shop shop = Shop.builder()
                .id(5L)
                .name("Seller Shop")
                .owner(owner)
                .build();

        User buyer = User.builder()
                .id(99L)
                .email("buyer@mail.com")
                .username("buyer")
                .password("secret")
                .build();

        return Order.builder()
                .id(1L)
                .shop(shop)
                .user(buyer)
                .status(status)
                .totalAmount(BigDecimal.TEN)
                .build();
    }
}

