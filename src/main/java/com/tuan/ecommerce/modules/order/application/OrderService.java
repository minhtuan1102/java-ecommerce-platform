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
import com.tuan.ecommerce.modules.product.domain.Product;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductRepository;
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
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository, ShopRepository shopRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
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
                .collect(Collectors.groupingBy(item -> item.getProduct().getShop().getId()));

        List<Order> createdOrders = new ArrayList<>();

        // Create an order for each shop
        for (Map.Entry<Long, List<CartItem>> entry : itemsByShop.entrySet()) {
            List<CartItem> shopItems = entry.getValue();
            
            Order order = new Order();
            order.setUser(user);
            order.setShop(shopItems.get(0).getProduct().getShop());
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (CartItem cartItem : shopItems) {
                Product product = cartItem.getProduct();
                if (product.getStock() < cartItem.getQuantity()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product " + product.getName() + " is out of stock");
                }
                
                // Reduce stock
                product.setStock(product.getStock() - cartItem.getQuantity());
                productRepository.save(product);

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .quantity(cartItem.getQuantity())
                        .price(product.getPrice()) // Snapshot price at time of order
                        .build();
                        
                order.getItems().add(orderItem);
                totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
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
}
