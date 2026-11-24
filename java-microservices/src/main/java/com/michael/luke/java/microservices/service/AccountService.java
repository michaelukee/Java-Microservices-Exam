package com.michael.luke.java.microservices.service;

import com.michael.luke.java.microservices.dto.RequestAndResponse.AccountSummary;
import com.michael.luke.java.microservices.dto.RequestAndResponse.CreateAccountRequest;
import com.michael.luke.java.microservices.dto.RequestAndResponse.CustomerAccountResponse;
import com.michael.luke.java.microservices.entity.Domain.Account;
import com.michael.luke.java.microservices.entity.Domain.AccountType;
import com.michael.luke.java.microservices.entity.Domain.Customer;
import com.michael.luke.java.microservices.repository.AccountRepository;
import com.michael.luke.java.microservices.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final long ACCOUNT_NUMBER_LOWER_BOUND = 67_000_000L;
    private static final long ACCOUNT_NUMBER_RANGE = 1_000_000L;
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final Random random = new Random();

    /**
     * Creates a new customer together with its account.
     *
     * @return the generated 8-digit account number to be returned to the client.
     */
    @Transactional
    public long createCustomerAccount(CreateAccountRequest request) {
        Customer customer = Customer.builder()
                .customerName(request.getCustomerName())
                .customerMobile(request.getCustomerMobile())
                .customerEmail(request.getCustomerEmail())
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .build();

        Account account = Account.builder()
                .accountNumber(generateUniqueAccountNumber())
                .accountType(request.getAccountType())
                .createdDate(LocalDateTime.now())
                .customer(customer)
                .build();

        customer.setAccounts(Collections.singletonList(account));
        customerRepository.save(customer);

        return account.getAccountNumber();
    }

    /**
     * Looks up a customer by one of their account numbers and returns the
     * customer's details together with their accounts of the same type as the
     * looked-up account (savings or checking only).
     *
     * @throws NoSuchElementException if no account has the given number.
     */
    @Transactional(readOnly = true)
    public CustomerAccountResponse getCustomerAccount(long accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NoSuchElementException("Customer not found"));
        Customer customer = account.getCustomer();
        AccountType accountType = account.getAccountType();

        CustomerAccountResponse response = new CustomerAccountResponse();
        response.setCustomerNumber(accountNumber);
        response.setCustomerName(customer.getCustomerName());
        response.setCustomerMobile(customer.getCustomerMobile());
        response.setCustomerEmail(customer.getCustomerEmail());
        response.setAddress1(customer.getAddress1());
        response.setAddress2(customer.getAddress2());
        if (accountType == AccountType.SAVINGS) {
            response.setSavings(toAccountSummaries(customer, accountType));
        } else {
            response.setChecking(toAccountSummaries(customer, accountType));
        }
        return response;
    }

    private java.util.List<AccountSummary> toAccountSummaries(Customer customer, AccountType accountType) {
        return customer.getAccounts().stream()
                .filter(acc -> acc.getAccountType() == accountType)
                .map(acc -> new AccountSummary(acc.getAccountNumber(), capitalize(accountType), acc.getBalance()))
                .collect(Collectors.toList());
    }

    private String capitalize(AccountType accountType) {
        String name = accountType.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private long generateUniqueAccountNumber() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            long candidate = ACCOUNT_NUMBER_LOWER_BOUND + (long) (random.nextDouble() * ACCOUNT_NUMBER_RANGE);
            if (!accountRepository.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique account number, please retry");
    }
}
