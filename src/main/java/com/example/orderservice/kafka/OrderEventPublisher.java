package com.example.orderservice.kafka;

import com.example.orderservice.dto.event.OrderEventDto;
import com.example.orderservice.model.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderEventPublisher {
    private final KafkaTemplate<Long, OrderEventDto> kafka;

    public OrderEventPublisher(KafkaTemplate<Long, OrderEventDto> kafka) {
        this.kafka = kafka;
    }

    public void publishOrderCreated(final Order order,
                                    final BigDecimal amount) {
        final OrderEventDto evt = OrderEventDto.builder()
                .userId(order.getUserId())
                .amount(amount)
                .orderId(order.getId())
                .build();
        kafka.send("create-order", order.getId(), evt);
    }
}
