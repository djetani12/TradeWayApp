package com.tradeway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradeway.model.CustomerAccount;
import com.tradeway.model.UserAccount;

public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {

    Optional<CustomerAccount> findByAccountNumber(String accountNumber);

    List<CustomerAccount> findByUserAccount(UserAccount userAccount);
}
