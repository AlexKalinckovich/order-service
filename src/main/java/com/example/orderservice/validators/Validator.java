package com.example.orderservice.validators;

public interface Validator<C, U> {

    void validateCreateDto(C createDto);

    void validateUpdateDto(U updateDto);

}
