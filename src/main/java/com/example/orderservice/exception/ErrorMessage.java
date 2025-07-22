package com.example.orderservice.exception;

import lombok.Getter;

@Getter
public enum ErrorMessage {
    RESOURCE_NOT_FOUND("resource_not_found"),
    ORDER_NOT_FOUND("order_not_found"),
    ITEM_NOT_FOUND("item_not_found"),
    VALIDATION_ERROR("validation_error"),
    ERROR_KEY("error_key"),
    SERVER_ERROR("server_error");

    private final String key;

    ErrorMessage(final String key) {
        this.key = key;
    }

}
