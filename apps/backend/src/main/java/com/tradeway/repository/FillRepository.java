package com.tradeway.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradeway.model.CustomerAccount;
import com.tradeway.model.TradeFill;

public interface FillRepository extends JpaRepository<TradeFill, Long> {

    List<TradeFill> findByAccountOrderByCreatedAtDesc(CustomerAccount account);
}
