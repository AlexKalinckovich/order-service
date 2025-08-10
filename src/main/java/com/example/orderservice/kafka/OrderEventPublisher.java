package com.example.orderservice.kafka;

import com.example.orderservice.dto.event.OrderEventDto;
import com.example.orderservice.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderEventPublisher {
    private final KafkaTemplate<Long, String> kafka;
    private final ObjectMapper objectMapper;

    public OrderEventPublisher(final KafkaTemplate<Long, String> kafka,
                               final ObjectMapper objectMapper) {
        this.kafka = kafka;
        this.objectMapper = objectMapper;
    }

    public void publishOrderCreated(final Order order,
                                    final BigDecimal amount) {
        final OrderEventDto evt = OrderEventDto.builder()
                .userId(order.getUserId())
                .amount(amount)
                .orderId(order.getId())
                .date(order.getOrderDate())
                .build();
        try{
            final String orderCreated = objectMapper.writeValueAsString(evt);
            kafka.send("create-order", order.getId(), orderCreated);
        }catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }
}