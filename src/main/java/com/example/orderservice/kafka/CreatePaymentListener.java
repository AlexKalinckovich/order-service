package com.example.orderservice.kafka;

import com.example.orderservice.dto.event.PaymentEventDto;
import com.example.orderservice.service.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CreatePaymentListener {

    private final OrderService orderService;

    @Autowired
    public CreatePaymentListener(final OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "create-payment", groupId = "order-service-group")
    public void handlePaymentEvent(final PaymentEventDto evt) {
        orderService.updateOrderStatus(evt.orderId(), evt.paymentStatus());
    }
}
