package com.tuan.ecommerce.modules.order.infrastructure.mapper;

import com.tuan.ecommerce.modules.order.application.dto.OrderItemResponse;
import com.tuan.ecommerce.modules.order.application.dto.OrderResponse;
import com.tuan.ecommerce.modules.order.domain.Order;
import com.tuan.ecommerce.modules.order.domain.OrderItem;
import com.tuan.ecommerce.modules.payment.domain.Payment;
import com.tuan.ecommerce.modules.payment.infrastructure.persistence.PaymentRepository;
import com.tuan.ecommerce.modules.product.domain.ProductImage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private final PaymentRepository paymentRepository;

    public OrderMapper(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .recipientName(order.getRecipientName())
                .shippingAddress(order.getShippingAddress())
                .phoneNumber(order.getPhoneNumber())
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentStatus(payment != null ? payment.getStatus() : null)
                .paymentProviderRef(payment != null ? payment.getProviderRef() : null)
                .paymentPaidAt(payment != null ? payment.getPaidAt() : null)
                .paymentExpiresAt(payment != null ? payment.getExpiresAt() : null)
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getSku().getProduct().getId())
                .skuId(item.getSku().getId())
                .productName(item.getProductName())
                .imageUrl(getMainImageUrl(item))
                .skuCode(item.getSkuCode())
                .tierIndex(item.getTierIndex())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(subtotal)
                .build();
    }

    private String getMainImageUrl(OrderItem item) {
        List<ProductImage> images = item.getSku().getProduct().getImages();
        if (images == null || images.isEmpty()) {
            return null;
        }

        return images.stream()
                .filter(ProductImage::isMain)
                .findFirst()
                .orElse(images.get(0))
                .getUrl();
    }

    public List<OrderResponse> toResponseList(List<Order> orders) {
        return orders.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
