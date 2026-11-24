package com.michael.luke.java.microservices.controller;

import com.michael.luke.java.microservices.dto.RequestAndResponse.CreateAccountRequest;
import com.michael.luke.java.microservices.dto.RequestAndResponse.CreateAccountResponse;
import com.michael.luke.java.microservices.dto.RequestAndResponse.CustomerAccountResponse;
import com.michael.luke.java.microservices.dto.RequestAndResponse.ErrorResponse;
import com.michael.luke.java.microservices.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {

    private static final String SUCCESS_MESSAGE = "Customer account created";
    private static final String FOUND_MESSAGE = "Customer Account found";

    private final AccountService accountService;

    @PostMapping("/account")
    public ResponseEntity<CreateAccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        long customerNumber = accountService.createCustomerAccount(request);
        CreateAccountResponse response = new CreateAccountResponse(
                customerNumber, HttpStatus.CREATED.value(), SUCCESS_MESSAGE);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/account/{customerNumber}")
    public ResponseEntity<CustomerAccountResponse> getAccount(@PathVariable long customerNumber) {
        CustomerAccountResponse response = accountService.getCustomerAccount(customerNumber);
        response.setTransactionStatusCode(HttpStatus.FOUND.value());
        response.setTransactionStatusDescription(FOUND_MESSAGE);
        return ResponseEntity.status(HttpStatus.FOUND).body(response);
    }

    /**
     * Error Handling
     */
    @RestControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
            String message = ex.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(FieldError::getDefaultMessage)
                    .orElse("Invalid request payload");
            return buildResponse(HttpStatus.BAD_REQUEST, message);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
            String message = extractEnumValidationMessage(ex).orElse("Malformed request body");
            return buildResponse(HttpStatus.BAD_REQUEST, message);
        }

        @ExceptionHandler(NoSuchElementException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex) {
            return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
            return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception ex) {
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }

        private Optional<String> extractEnumValidationMessage(HttpMessageNotReadableException ex) {
            Throwable current = ex.getCause();
            while (current != null) {
                if (current instanceof IllegalArgumentException) {
                    return Optional.of(current.getMessage());
                }
                current = current.getCause();
            }
            return Optional.empty();
        }

        private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
            return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
        }
    }
}
