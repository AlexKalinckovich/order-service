package com.example.orderservice.dto.event;

public record PaymentEventDto(Long orderId, PaymentStatus paymentStatus){ }
