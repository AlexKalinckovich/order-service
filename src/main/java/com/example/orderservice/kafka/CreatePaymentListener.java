package com.example.orderservice.kafka;

import com.example.orderservice.dto.event.PaymentEventDto;
import com.example.orderservice.service.order.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CreatePaymentListener {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CreatePaymentListener(@Qualifier("orderServiceImpl") final OrderService orderService,
                                 final ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "create-payment", groupId = "order-service-group")
    public void handlePaymentEvent(final String paymentEvt) {
        final PaymentEventDto evt;
        try {
            evt = objectMapper.readValue(paymentEvt, PaymentEventDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        orderService.updateOrderStatus(evt.orderId(), evt.paymentStatus());
    }
}
