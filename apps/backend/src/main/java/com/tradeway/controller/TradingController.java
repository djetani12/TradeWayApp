package com.tradeway.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tradeway.dto.PositionResponse;
import com.tradeway.dto.TradeOrderRequest;
import com.tradeway.dto.TradeOrderResponse;
import com.tradeway.model.CustomerAccount;
import com.tradeway.model.OrderSide;
import com.tradeway.model.TradeOrder;
import com.tradeway.model.TradePosition;
import com.tradeway.model.UserAccount;
import com.tradeway.repository.CustomerAccountRepository;
import com.tradeway.repository.UserAccountRepository;
import com.tradeway.service.TradingService;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/trading")
public class TradingController {

    private final TradingService tradingService;
    private final UserAccountRepository userAccountRepository;
    private final CustomerAccountRepository customerAccountRepository;

    public TradingController(TradingService tradingService,
                            UserAccountRepository userAccountRepository,
                            CustomerAccountRepository customerAccountRepository) {
        this.tradingService = tradingService;
        this.userAccountRepository = userAccountRepository;
        this.customerAccountRepository = customerAccountRepository;
    }

    @PostMapping("/orders")
    public ResponseEntity<TradeOrderResponse> placeOrder(@Valid @RequestBody TradeOrderRequest request,
                                                        Authentication authentication) {
        CustomerAccount account = currentAccount(authentication);
        OrderSide side = OrderSide.valueOf(request.side().trim().toUpperCase());
        TradeOrder order = tradingService.placeOrder(account.getAccountNumber(), request.symbol(), side, request.quantity());
        return ResponseEntity.ok(TradeOrderResponse.from(order));
    }

    @GetMapping("/orders")
    public List<TradeOrderResponse> getOrders(Authentication authentication) {
        CustomerAccount account = currentAccount(authentication);
        return tradingService.getOrdersForAccount(account.getAccountNumber())
            .stream()
            .map(TradeOrderResponse::from)
            .toList();
    }

    @GetMapping("/portfolio")
    public List<PositionResponse> getPortfolio(Authentication authentication) {
        CustomerAccount account = currentAccount(authentication);
        return tradingService.getPortfolio(account.getAccountNumber())
            .stream()
            .map(position -> new PositionResponse(position.getInstrument().getSymbol(), position.getQuantity(), position.getAverageCost()))
            .toList();
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<TradeOrderResponse> cancelOrder(@PathVariable Long id, Authentication authentication) {
        CustomerAccount account = currentAccount(authentication);
        TradeOrder order = tradingService.cancelOrder(id, account.getAccountNumber());
        return ResponseEntity.ok(TradeOrderResponse.from(order));
    }

    private CustomerAccount currentAccount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication required");
        }

        UserAccount user = userAccountRepository.findByUsernameIgnoreCase(authentication.getName())
            .or(() -> userAccountRepository.findByEmailIgnoreCase(authentication.getName()))
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));

        return customerAccountRepository.findByUserAccount(user)
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Trading account not found"));
    }
}
