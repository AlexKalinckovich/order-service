package com.example.orderservice.exception.response;

sealed interface ErrorDetails permits ValidationErrorDetails, SimpleErrorDetails {
}
