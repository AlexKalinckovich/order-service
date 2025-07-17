package com.example.orderservice.exception.item;

import jakarta.persistence.EntityNotFoundException;

public class ItemNotFoundException extends EntityNotFoundException {
    public ItemNotFoundException(final Long id) {
        super("Item not found: " + id);
    }

    public ItemNotFoundException(final String message) {
        super(message);
    }

}
