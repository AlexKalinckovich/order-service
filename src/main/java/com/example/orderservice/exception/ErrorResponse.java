package com.example.orderservice.exception;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String path,
        Object details
) {}
