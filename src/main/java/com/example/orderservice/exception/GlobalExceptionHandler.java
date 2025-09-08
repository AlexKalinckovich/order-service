package com.example.orderservice.exception;

import com.example.orderservice.exception.item.ItemNotFoundException;
import com.example.orderservice.exception.order.OrderNotFoundException;
import com.example.orderservice.exception.response.ErrorResponse;
import com.example.orderservice.exception.response.ExceptionResponseService;
import com.example.orderservice.exception.response.ValidationErrorDetails;
import com.example.orderservice.exception.user.UserNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.AuthenticationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import org.apache.kafka.common.KafkaException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.LinkedHashMap;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ExceptionResponseService exceptionResponseService;

    /* =========================
       ORDER DOMAIN
       ========================= */

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.NOT_FOUND, ErrorMessage.ORDER_NOT_FOUND
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.BAD_REQUEST, ErrorMessage.VALIDATION_ERROR
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.BAD_REQUEST, ErrorMessage.INVALID_REQUEST
        );
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleItemNotFound(ItemNotFoundException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.NOT_FOUND, ErrorMessage.ITEM_NOT_FOUND
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.NOT_FOUND, ErrorMessage.RESOURCE_NOT_FOUND
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.NOT_FOUND, ErrorMessage.RESOURCE_NOT_FOUND
        );
    }

    /* =========================
       VALIDATION
       ========================= */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        final Map<String, String> fieldErrorsMap = new LinkedHashMap<>();
        for (final FieldError fe : ex.getBindingResult().getFieldErrors()) {
            final String defaultMessage = fe.getDefaultMessage() == null ? "" : fe.getDefaultMessage();
            fieldErrorsMap.putIfAbsent(fe.getField(), defaultMessage);
        }

        ValidationErrorDetails details = new ValidationErrorDetails(fieldErrorsMap);

        ErrorResponse response = new ErrorResponse(
                java.time.Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorMessage.VALIDATION_ERROR.name(),
                "Request validation failed. Please check the field errors for details.",
                request.getDescription(false),
                details
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        final Map<String, String> fieldErrorsMap = new LinkedHashMap<>();
        for (final ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            final String defaultMessage = cv.getMessage() == null ? "" : cv.getMessage();
            fieldErrorsMap.putIfAbsent(cv.getPropertyPath().toString(), defaultMessage);
        }

        final ValidationErrorDetails details = new ValidationErrorDetails(fieldErrorsMap);

        final ErrorResponse response = new ErrorResponse(
                java.time.Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorMessage.VALIDATION_ERROR.name(),
                "Request validation failed. Please check the constraint violations for details.",
                request.getDescription(false),
                details
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /* =========================
       DOWNSTREAM SERVICE ERRORS
       ========================= */

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponse(WebClientResponseException ex, WebRequest request) {
        HttpStatus remoteStatus = HttpStatus.valueOf(ex.getStatusCode().value());

        if (remoteStatus == HttpStatus.NOT_FOUND) {
            return exceptionResponseService.buildErrorResponse(
                    ex, request, HttpStatus.NOT_FOUND, ErrorMessage.RESOURCE_NOT_FOUND
            );
        } else if (remoteStatus.is4xxClientError()) {
            return exceptionResponseService.buildErrorResponse(
                    ex, request, HttpStatus.BAD_REQUEST, ErrorMessage.DOWNSTREAM_SERVICE_ERROR
            );
        } else {
            return exceptionResponseService.buildErrorResponse(
                    ex, request, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.DOWNSTREAM_SERVICE_ERROR
            );
        }
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ErrorResponse> handleWebClientRequest(WebClientRequestException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.SERVICE_UNAVAILABLE, ErrorMessage.NETWORK_ERROR
        );
    }

    /* =========================
       DATABASE ERRORS
       ========================= */

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.CONFLICT, ErrorMessage.DATABASE_CONSTRAINT_VIOLATION
        );
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.CONFLICT, ErrorMessage.CONCURRENCY_ERROR
        );
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(DataAccessException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.SERVICE_UNAVAILABLE, ErrorMessage.DATABASE_ERROR
        );
    }

    /* =========================
       MESSAGE BROKER ERRORS
       ========================= */

    @ExceptionHandler({KafkaException.class})
    public ResponseEntity<ErrorResponse> handleKafkaException(KafkaException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.SERVICE_UNAVAILABLE, ErrorMessage.SERVICE_UNAVAILABLE
        );
    }

    /* =========================
       AUTHENTICATION & AUTHORIZATION
       ========================= */

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.UNAUTHORIZED, ErrorMessage.AUTHENTICATION_ERROR
        );
    }

    /* =========================
       JSON / MESSAGE CONVERSION
       ========================= */

    @ExceptionHandler({HttpMessageNotReadableException.class, JsonProcessingException.class})
    public ResponseEntity<ErrorResponse> handleJsonParsing(Exception ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.BAD_REQUEST, ErrorMessage.INVALID_REQUEST
        );
    }

    /* =========================
       SERVLET ERRORS
       ========================= */

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ErrorResponse> handleServlet(ServletException ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.BAD_REQUEST, ErrorMessage.INVALID_REQUEST
        );
    }

    /* =========================
       FALLBACK
       ========================= */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        return exceptionResponseService.buildErrorResponse(
                ex, request, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.SERVER_ERROR
        );
    }
}

