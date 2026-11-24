package com.michael.luke.java.microservices.repository;

import com.michael.luke.java.microservices.entity.Domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
