package com.tuan.ecommerce.modules.order.application;

import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import com.tuan.ecommerce.modules.cart.domain.Cart;
import com.tuan.ecommerce.modules.cart.domain.CartItem;
import com.tuan.ecommerce.modules.cart.infrastructure.persistence.CartRepository;
import com.tuan.ecommerce.modules.inventory.application.InventoryService;
import com.tuan.ecommerce.modules.order.application.dto.CheckoutRequest;
import com.tuan.ecommerce.modules.order.domain.Order;
import com.tuan.ecommerce.modules.order.domain.OrderItem;
import com.tuan.ecommerce.modules.order.infrastructure.mapper.OrderMapper;
import com.tuan.ecommerce.modules.order.infrastructure.persistence.OrderRepository;
import com.tuan.ecommerce.modules.order.infrastructure.persistence.OrderStatusHistoryRepository;
import com.tuan.ecommerce.modules.payment.application.PaymentService;
import com.tuan.ecommerce.modules.product.domain.Product;
import com.tuan.ecommerce.modules.product.domain.ProductSKU;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductSkuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductSkuRepository skuRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private OrderStatusHistoryRepository statusHistoryRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private ProductSKU sku;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Product product = new Product();
        product.setName("Test Product");

        sku = new ProductSKU();
        sku.setId(1L);
        sku.setProduct(product);
        sku.setPrice(BigDecimal.valueOf(100));
        sku.setSkuCode("SKU123");
        sku.setTierIndex("0");

        cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());
        
        CartItem cartItem = new CartItem();
        cartItem.setSku(sku);
        cartItem.setQuantity(2);
        cart.getItems().add(cartItem);
    }

    @Test
    void checkout_ShouldSetProductNameInOrderItem() {
        // Arrange
        CheckoutRequest request = new CheckoutRequest();
        request.setRecipientName("Test Receiver");
        request.setShippingAddress("Address");
        request.setPhoneNumber("123456789");

        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.checkout(user.getEmail(), request);

        // Assert
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertFalse(savedOrder.getItems().isEmpty());
        OrderItem orderItem = savedOrder.getItems().get(0);
        
        assertNotNull(orderItem.getProductName(), "Product name should not be null");
        assertEquals("Test Product", orderItem.getProductName());
        assertEquals("SKU123", orderItem.getSkuCode());
        assertEquals("0", orderItem.getTierIndex());
    }
}
