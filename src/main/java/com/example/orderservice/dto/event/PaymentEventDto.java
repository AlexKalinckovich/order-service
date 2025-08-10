package com.example.orderservice.dto.event;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PaymentEventDto(
        String paymentId,
        Long orderId,
        LocalDateTime date,
        PaymentStatus paymentStatus
) { }
