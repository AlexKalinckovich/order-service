package com.example.orderservice.exception.response;

import java.util.Map;

public record ValidationErrorDetails(Map<String, String> fieldErrors) implements ErrorDetails {
}
