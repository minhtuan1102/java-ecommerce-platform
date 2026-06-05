package com.tuan.ecommerce.modules.review.application;

import com.tuan.ecommerce.modules.auth.domain.User;
import com.tuan.ecommerce.modules.auth.infrastructure.persistence.user.UserRepository;
import com.tuan.ecommerce.modules.order.domain.OrderItem;
import com.tuan.ecommerce.modules.order.domain.OrderStatus;
import com.tuan.ecommerce.modules.order.infrastructure.persistence.OrderItemJpaRepository;
import com.tuan.ecommerce.modules.product.domain.Product;
import com.tuan.ecommerce.modules.product.infrastructure.persistence.ProductRepository;
import com.tuan.ecommerce.modules.review.application.dto.CreateReviewRequest;
import com.tuan.ecommerce.modules.review.application.dto.ReviewResponse;
import com.tuan.ecommerce.modules.review.domain.Review;
import com.tuan.ecommerce.modules.review.infrastructure.mapper.ReviewMapper;
import com.tuan.ecommerce.modules.review.infrastructure.persistence.ReviewJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewJpaRepository reviewRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;

    public ReviewService(ReviewJpaRepository reviewRepository,
                         OrderItemJpaRepository orderItemRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository,
                         ReviewMapper reviewMapper) {
        this.reviewRepository = reviewRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.reviewMapper = reviewMapper;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getProductReviews(Long productId) {
        return reviewMapper.toResponseList(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    @Transactional
    public ReviewResponse createReview(Long productId, CreateReviewRequest request, String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (!orderItem.getOrder().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot review another customer's order item");
        }

        if (orderItem.getOrder().getStatus() != OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only delivered order items can be reviewed");
        }

        if (!orderItem.getSku().getProduct().getId().equals(productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order item does not belong to this product");
        }

        if (reviewRepository.findByOrderItemId(orderItem.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This order item has already been reviewed");
        }

        Review review = Review.builder()
                .product(product)
                .customer(user)
                .orderItem(orderItem)
                .rating(request.getRating())
                .comment(request.getComment() != null ? request.getComment().trim() : null)
                .build();

        Review savedReview = reviewRepository.save(review);
        refreshProductRating(product);
        return reviewMapper.toResponse(savedReview);
    }

    private void refreshProductRating(Product product) {
        long reviewCount = reviewRepository.countByProductId(product.getId());
        Double averageRating = reviewRepository.averageRatingByProductId(product.getId());

        product.setReviewCount(Math.toIntExact(reviewCount));
        product.setAverageRating(averageRating == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP));
        productRepository.save(product);
    }
}
