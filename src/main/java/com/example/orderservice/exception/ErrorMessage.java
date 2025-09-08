package com.example.orderservice.exception;

import lombok.Getter;

@Getter
public enum ErrorMessage {
    RESOURCE_NOT_FOUND("resource_not_found"),
    ORDER_NOT_FOUND("order_not_found"),
    ITEM_NOT_FOUND("item_not_found"),
    VALIDATION_ERROR("validation_error"),
    ERROR_KEY("error_key"),
    SERVER_ERROR("server_error"),
    DATABASE_ERROR("database_error"),
    DATABASE_CONSTRAINT_VIOLATION("database_constraint_violation"),
    CONCURRENCY_ERROR("concurrency_error"),
    NETWORK_ERROR("network_error"),
    DOWNSTREAM_SERVICE_ERROR("downstream_service_error"),
    INVALID_REQUEST("invalid_request"),
    AUTHENTICATION_ERROR("authentication_error"),
    AUTHORIZATION_ERROR("authorization_error"),
    SERVICE_UNAVAILABLE("service_unavailable"),
    PAYMENT_ERROR("payment_error"),
    INVENTORY_ERROR("inventory_error"),
    SHIPPING_ERROR("shipping_error");

    private final String key;

    ErrorMessage(final String key) {
        this.key = key;
    }
}