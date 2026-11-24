package com.michael.luke.java.microservices.dto;

import com.michael.luke.java.microservices.entity.Domain.AccountType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public final class RequestAndResponse {

    private RequestAndResponse() {
    
    }

    /** Validation Messages for POST /api/v1/account. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateAccountRequest {

        @NotBlank(message = "Customer name is required field")
        private String customerName;

        @NotBlank(message = "Customer mobile is required field")
        private String customerMobile;

        @NotBlank(message = "Email is required field")
        @Email(message = "Email is invalid")
        @Size(max = 50, message = "Email must not exceed 50 characters")
        private String customerEmail;

        @NotBlank(message = "Address1 is required field")
        private String address1;

        private String address2;

        @NotNull(message = "Account type is required field")
        private AccountType accountType;
    }

    /** Success response (HTTP 201). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateAccountResponse {

        @JsonProperty("customerNumber")
        private Long customerNumber;

        private int transactionStatusCode;

        private String transactionStatusDescription;
    }

    /** Error response for failed requests (HTTP 400). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {

        private int transactionStatusCode;

        private String transactionStatusDescription;
    }

    /** Account Summary Details. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountSummary {

        @JsonProperty("accountNumber")
        private Long accountNumber;

        private String accountType;

        private BigDecimal availableBalance;
    }

    /** Success response (HTTP 302). */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerAccountResponse {

        @JsonProperty("customerNumber")
        private Long customerNumber;

        private String customerName;

        private String customerMobile;

        private String customerEmail;

        private String address1;

        private String address2;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<AccountSummary> savings;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<AccountSummary> checking;

        private int transactionStatusCode;

        private String transactionStatusDescription;
    }
}
