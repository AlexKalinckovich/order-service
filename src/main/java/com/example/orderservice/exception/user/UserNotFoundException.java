package com.example.orderservice.exception.user;

import jakarta.persistence.EntityNotFoundException;

public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException(final Long userId) {
        super("User with id " + userId + " not found");
    }
}
