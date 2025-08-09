package com.example.orderservice.dto.event;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderEventDto(
        Long orderId,
        Long userId,
        BigDecimal amount
) { }
