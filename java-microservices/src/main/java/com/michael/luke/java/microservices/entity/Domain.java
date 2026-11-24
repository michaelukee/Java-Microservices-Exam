package com.michael.luke.java.microservices.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Table for Customer and Account. One Customer may have more than one Account.
 */
public final class Domain {

    private Domain() {
    
    }

    @Entity
    @Table(name = "customers", indexes = {
            @Index(name = "idx_customer_email", columnList = "customer_email")
    })
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString(exclude = "accounts")
    @EqualsAndHashCode(of = "id")
    public static class Customer {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "customer_name", nullable = false, length = 50)
        private String customerName;

        @Column(name = "customer_mobile", nullable = false, length = 20)
        private String customerMobile;

        @Column(name = "customer_email", nullable = false, length = 50)
        private String customerEmail;

        @Column(name = "address1", nullable = false, length = 100)
        private String address1;

        @Column(name = "address2", length = 100)
        private String address2;

        @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @Builder.Default
        private List<Account> accounts = new ArrayList<>();
    }

    @Entity
    @Table(name = "accounts", indexes = {
            @Index(name = "idx_account_number", columnList = "account_number", unique = true),
            @Index(name = "idx_account_customer_id", columnList = "customer_id")
    })
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString(exclude = "customer")
    @EqualsAndHashCode(of = "id")
    public static class Account {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "account_number", nullable = false, unique = true)
        private Long accountNumber;

        @Enumerated(EnumType.STRING)
        @Column(name = "account_type", nullable = false, length = 10)
        private AccountType accountType;

        @Column(name = "balance", nullable = false, precision = 15, scale = 2)
        @Builder.Default
        private BigDecimal balance = BigDecimal.ZERO;

        @Column(name = "created_date", nullable = false)
        private LocalDateTime createdDate;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "customer_id", nullable = false)
        private Customer customer;
    }

    /** Account type as defined by the API contract: S = Savings, C = Checking. */
    public enum AccountType {

        SAVINGS("S"),
        CHECKING("C");

        private final String code;

        AccountType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public String toJson() {
            return code;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static AccountType fromCode(String code) {
            if (code == null) {
                return null;
            }
            for (AccountType type : values()) {
                if (type.code.equalsIgnoreCase(code.trim())) {
                    return type;
                }
            }
            throw new IllegalArgumentException(
                    "Account type is invalid. Allowed values: S (Savings), C (Checking)");
        }
    }
}
