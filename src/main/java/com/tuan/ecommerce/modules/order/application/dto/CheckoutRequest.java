package com.tuan.ecommerce.modules.order.application.dto;

import com.tuan.ecommerce.modules.order.domain.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutRequest {
    private Long addressId;

    @NotBlank(message = "Tên người nhận không được để trống")
    private String recipientName;

    @NotBlank(message = "Địa chỉ nhận hàng không được để trống")
    private String shippingAddress;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    private PaymentMethod paymentMethod;
}
