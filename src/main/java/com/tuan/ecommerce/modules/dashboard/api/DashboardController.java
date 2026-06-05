package com.tuan.ecommerce.modules.dashboard.api;

import com.tuan.ecommerce.modules.dashboard.application.DashboardService;
import com.tuan.ecommerce.modules.dashboard.application.dto.DashboardSummaryResponse;
import com.tuan.ecommerce.modules.dashboard.application.dto.RevenuePointResponse;
import com.tuan.ecommerce.modules.dashboard.application.dto.TopProductResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<RevenuePointResponse>> getRevenueByDay() {
        return ResponseEntity.ok(dashboardService.getRevenueByDay());
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<TopProductResponse>> getTopProducts(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getTopProducts(limit));
    }
}
