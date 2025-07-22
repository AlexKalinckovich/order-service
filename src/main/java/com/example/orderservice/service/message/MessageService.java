package com.example.orderservice.service.message;


import com.example.orderservice.exception.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageSource messageSource;

    public String getMessage(final ErrorMessage errorMessage) {
        return messageSource.getMessage(errorMessage.getKey(), null, Locale.ENGLISH);
    }
}
