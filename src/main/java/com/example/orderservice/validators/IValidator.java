package com.example.orderservice.validators;

public interface IValidator<C, U> {

    void validateCreateDto(C createDto);

    void validateUpdateDto(U updateDto);

}
