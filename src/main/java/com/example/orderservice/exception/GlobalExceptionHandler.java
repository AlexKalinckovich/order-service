package com.example.orderservice.exception;

import com.example.orderservice.exception.item.ItemNotFoundException;
import com.example.orderservice.exception.order.OrderNotFoundException;
import com.example.orderservice.service.exception.ExceptionResponseService;
import com.example.orderservice.service.message.MessageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageService messageService;
    private final ExceptionResponseService exceptionResponseService;


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
                                                                  final @NotNull HttpHeaders headers,
                                                                  final @NotNull HttpStatusCode status,
                                                                  final WebRequest request) {

        final Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    final String fieldName = ((FieldError) error).getField();
                    final String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        final ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                messageService.getMessage(ErrorMessage.VALIDATION_ERROR),
                request.getDescription(false),
                errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            OrderNotFoundException.class,
            ItemNotFoundException.class,
            EntityNotFoundException.class,
    })
    private ResponseEntity<ErrorResponse> entityNotFoundHandler(final Exception ex, final WebRequest request) {
        return exceptionResponseService.buildWebRequestErrorResponse(
                ex,request,HttpStatus.NOT_FOUND,
                messageService.getMessage(ErrorMessage.RESOURCE_NOT_FOUND));
    }

    @ExceptionHandler({
         ValidationException.class,
    })
    private ResponseEntity<ErrorResponse> validationExceptionHandler(final Exception ex, final WebRequest request) {
        return exceptionResponseService.buildWebRequestErrorResponse(
                ex,request,HttpStatus.BAD_REQUEST,
                messageService.getMessage(ErrorMessage.VALIDATION_ERROR)
        );
    }

    @ExceptionHandler({
            Exception.class,
    })
    private ResponseEntity<ErrorResponse> globalExceptionHandler(final Exception ex, final WebRequest request) {
        return exceptionResponseService.buildWebRequestErrorResponse(
                ex, request, HttpStatus.INTERNAL_SERVER_ERROR,
                messageService.getMessage(ErrorMessage.SERVER_ERROR)
        );
    }
}
