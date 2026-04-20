package com.tuan.ecommerce.modules.order.application;

import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import com.tuan.ecommerce.modules.cart.domain.Cart;
import com.tuan.ecommerce.modules.cart.domain.CartItem;
import com.tuan.ecommerce.modules.cart.infrastructure.persistence.CartRepository;
import com.tuan.ecommerce.modules.order.application.dto.OrderResponse;
import com.tuan.ecommerce.modules.order.domain.Order;
import com.tuan.ecommerce.modules.order.domain.OrderItem;
import com.tuan.ecommerce.modules.order.infrastructure.mapper.OrderMapper;
import com.tuan.ecommerce.modules.order.infrastructure.persistence.OrderRepository;
import com.tuan.ecommerce.modules.product.domain.ProductSKU;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductSkuRepository;
import com.tuan.ecommerce.modules.shop.domain.Shop;
import com.tuan.ecommerce.modules.shop.infrastructure.persistence.ShopRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductSkuRepository skuRepository;
    private final ShopRepository shopRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, UserRepository userRepository, ProductSkuRepository skuRepository, ShopRepository shopRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.skuRepository = skuRepository;
        this.shopRepository = shopRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public List<OrderResponse> checkout(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found or empty"));

        if (cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        // Group cart items by Shop ID
        Map<Long, List<CartItem>> itemsByShop = cart.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getSku().getProduct().getShop().getId()));

        List<Order> createdOrders = new ArrayList<>();

        // Create an order for each shop
        for (Map.Entry<Long, List<CartItem>> entry : itemsByShop.entrySet()) {
            List<CartItem> shopItems = entry.getValue();
            
            Order order = new Order();
            order.setUser(user);
            order.setShop(shopItems.get(0).getSku().getProduct().getShop());
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (CartItem cartItem : shopItems) {
                // Áp dụng Pessimistic Locking để đảm bảo an toàn khi trừ kho (không bị Race Condition)
                ProductSKU sku = skuRepository.findByIdWithLock(cartItem.getSku().getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU not found"));

                if (sku.getStock() < cartItem.getQuantity()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product " + sku.getProduct().getName() + " (" + sku.getTierIndex() + ") is out of stock");
                }
                
                // Reduce stock
                sku.setStock(sku.getStock() - cartItem.getQuantity());
                skuRepository.save(sku);

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .sku(sku)
                        .quantity(cartItem.getQuantity())
                        .price(sku.getPrice()) // Snapshot price at time of order
                        .build();
                        
                order.getItems().add(orderItem);
                totalAmount = totalAmount.add(sku.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            }
            
            order.setTotalAmount(totalAmount);
            createdOrders.add(orderRepository.save(order));
        }

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return orderMapper.toResponseList(createdOrders);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return orderMapper.toResponseList(orderRepository.findByUserId(user.getId()));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getShopOrders(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        Shop shop = shopRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found"));
                
        return orderMapper.toResponseList(orderRepository.findByShopId(shop.getId()));
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, com.tuan.ecommerce.modules.order.domain.OrderStatus status, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isAdmin = user.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
        boolean isShopOwner = order.getShop().getOwner().getId().equals(user.getId());
        if (!isAdmin && !isShopOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to update this order");
        }

        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    private void validateStatusTransition(com.tuan.ecommerce.modules.order.domain.OrderStatus currentStatus,
                                          com.tuan.ecommerce.modules.order.domain.OrderStatus requestedStatus) {
        if (currentStatus == requestedStatus) {
            return;
        }

        Set<com.tuan.ecommerce.modules.order.domain.OrderStatus> nextStatuses = switch (currentStatus) {
            case PENDING -> Set.of(com.tuan.ecommerce.modules.order.domain.OrderStatus.PROCESSING,
                    com.tuan.ecommerce.modules.order.domain.OrderStatus.CANCELLED);
            case PROCESSING -> Set.of(com.tuan.ecommerce.modules.order.domain.OrderStatus.SHIPPED,
                    com.tuan.ecommerce.modules.order.domain.OrderStatus.CANCELLED);
            case SHIPPED -> Set.of(com.tuan.ecommerce.modules.order.domain.OrderStatus.DELIVERED);
            case DELIVERED, CANCELLED -> Set.of();
        };

        if (!nextStatuses.contains(requestedStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid order status transition: " + currentStatus + " -> " + requestedStatus
            );
        }
    }
}
