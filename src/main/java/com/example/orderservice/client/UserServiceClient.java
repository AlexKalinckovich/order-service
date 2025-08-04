package com.example.orderservice.client;

import com.example.orderservice.exception.user.UserNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {
    private final WebClient client;

    public UserServiceClient(@Value("${USER_SERVICE_URL}") String baseUrl) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void validateUserExists(final Long userId) {
        client.get()
                .uri("/user/{id}", userId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        Mono.error(new UserNotFoundException(userId)))
                .toBodilessEntity()
                .block();
    }
}
