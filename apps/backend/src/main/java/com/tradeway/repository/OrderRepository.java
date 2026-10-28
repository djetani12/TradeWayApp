package com.tradeway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradeway.model.CustomerAccount;
import com.tradeway.model.OrderStatus;
import com.tradeway.model.TradeOrder;

public interface OrderRepository extends JpaRepository<TradeOrder, Long> {

    List<TradeOrder> findByAccountOrderByCreatedAtDesc(CustomerAccount account);

    List<TradeOrder> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    Optional<TradeOrder> findByClientOrderId(String clientOrderId);

    Optional<TradeOrder> findByIdAndAccount_AccountNumber(Long id, String accountNumber);
}
