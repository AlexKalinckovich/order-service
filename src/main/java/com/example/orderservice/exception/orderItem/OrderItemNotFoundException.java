package com.example.orderservice.exception.orderItem;

import jakarta.persistence.EntityNotFoundException;

public class OrderItemNotFoundException extends EntityNotFoundException {
    public OrderItemNotFoundException(final Long id) {
        super("OrderItem with id " + id + " not found");
    }
}
