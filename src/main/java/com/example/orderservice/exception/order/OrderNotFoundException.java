package com.example.orderservice.exception.order;

import jakarta.persistence.EntityNotFoundException;

public class OrderNotFoundException extends EntityNotFoundException {
    public OrderNotFoundException(final Long id) {
        super("Order with id " + id + " not found");
    }
}
