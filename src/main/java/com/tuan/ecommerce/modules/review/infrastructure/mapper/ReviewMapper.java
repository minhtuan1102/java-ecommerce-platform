package com.tuan.ecommerce.modules.review.infrastructure.mapper;

import com.tuan.ecommerce.modules.review.application.dto.ReviewResponse;
import com.tuan.ecommerce.modules.review.domain.Review;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getUsername())
                .orderItemId(review.getOrderItem().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    public List<ReviewResponse> toResponseList(List<Review> reviews) {
        return reviews.stream().map(this::toResponse).toList();
    }
}
