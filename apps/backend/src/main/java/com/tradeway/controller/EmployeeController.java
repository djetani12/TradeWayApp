package com.tradeway.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tradeway.dto.EmployeeDashboardResponse;
import com.tradeway.dto.EmployeeUserResponse;
import com.tradeway.model.AccountStatus;
import com.tradeway.model.UserAccount;
import com.tradeway.model.UserRole;
import com.tradeway.repository.UserAccountRepository;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api")
public class EmployeeController {

    private final UserAccountRepository userAccountRepository;

    public EmployeeController(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/employee/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public EmployeeDashboardResponse dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }

        return new EmployeeDashboardResponse(
            "TradeWay operations",
            "Live portfolio and execution monitor",
            List.of(
                new EmployeeDashboardResponse.Kpi("Total trades", "1,284"),
                new EmployeeDashboardResponse.Kpi("Buy volume", "$420.8K"),
                new EmployeeDashboardResponse.Kpi("Sell volume", "$395.6K"),
                new EmployeeDashboardResponse.Kpi("Active users", "632")
            ),
            List.of(
                new EmployeeDashboardResponse.ChartPoint("2026-01-01", 220),
                new EmployeeDashboardResponse.ChartPoint("2026-01-02", 280),
                new EmployeeDashboardResponse.ChartPoint("2026-01-03", 260),
                new EmployeeDashboardResponse.ChartPoint("2026-01-04", 310)
            ),
            List.of(
                new EmployeeDashboardResponse.StatusBreakdown("FILLED", 74),
                new EmployeeDashboardResponse.StatusBreakdown("PENDING", 18),
                new EmployeeDashboardResponse.StatusBreakdown("REJECTED", 8)
            )
        );
    }

    @GetMapping("/employee/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EmployeeUserResponse> users() {
        return userAccountRepository.findAll().stream()
            .map(EmployeeUserResponse::from)
            .toList();
    }

    @PostMapping("/employee/users/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> suspendUser(@PathVariable Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        user.setStatus(AccountStatus.SUSPENDED);
        userAccountRepository.save(user);
        return ResponseEntity.ok(Map.of("status", "suspended", "username", user.getUsername()));
    }

    @PostMapping("/employee/users/{userId}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> reactivateUser(@PathVariable Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        user.setStatus(AccountStatus.ACTIVE);
        userAccountRepository.save(user);
        return ResponseEntity.ok(Map.of("status", "active", "username", user.getUsername()));
    }

    @GetMapping("/employee/system-health")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public Map<String, Object> systemHealth() {
        return Map.of(
            "status", "HEALTHY",
            "database", "PRIMARY_OK",
            "reportingDatabase", "REPORTING_OK",
            "outboxLagSeconds", 2,
            "failedOrders", 3,
            "uptime", "99.97%"
        );
    }

    @GetMapping("/employee/reports")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public Map<String, Object> reports(@RequestParam(required = false) String from,
                                      @RequestParam(required = false) String to) {
        return Map.of(
            "from", from != null ? from : "2026-01-01",
            "to", to != null ? to : "2026-01-31",
            "volume", Map.of("buy", 420800.0, "sell", 395600.0),
            "popularInstruments", List.of("AAPL", "MSFT", "NVDA"),
            "sectorMix", Map.of("Technology", 58, "Healthcare", 18, "Financials", 12, "Bond", 12)
        );
    }
}
