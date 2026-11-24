package com.michael.luke.java.microservices.repository;

import com.michael.luke.java.microservices.entity.Domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByAccountNumber(Long accountNumber);

    Optional<Account> findByAccountNumber(Long accountNumber);
}
