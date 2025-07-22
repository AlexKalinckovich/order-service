package com.example.orderservice.service.exception;

import com.example.orderservice.exception.ErrorMessage;
import com.example.orderservice.exception.ErrorResponse;
import com.example.orderservice.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExceptionResponseService {

    private final MessageService messageService;

    public ResponseEntity<ErrorResponse> buildWebRequestErrorResponse(final Exception ex,
                                                                      final WebRequest request,
                                                                      final HttpStatus status,
                                                                      final String message){
        final ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                message,
                request.getDescription(false),
                Map.of(messageService.getMessage(ErrorMessage.ERROR_KEY), ex.getMessage())
        );

        return new ResponseEntity<>(errorResponse, status);
    }

}
