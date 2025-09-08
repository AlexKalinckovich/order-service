package com.example.orderservice.service.order;

import com.example.orderservice.dto.event.PaymentStatus;
import com.example.orderservice.dto.order.OrderCreateDto;
import com.example.orderservice.dto.order.OrderResponseDto;
import com.example.orderservice.dto.order.OrderUpdateDto;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(final OrderCreateDto orderCreateDto);

    List<OrderResponseDto> getAllOrdersByIds(List<Long> orderIds);

    OrderResponseDto getOrderById(final Long orderId);

    OrderResponseDto updateOrder(final OrderUpdateDto orderUpdateDto);

    OrderResponseDto deleteOrder(final Long orderId);

    BigDecimal getOrderTotalById(final Long orderId);

    void updateOrderStatus(Long orderId, PaymentStatus paymentStatus);

    List<OrderResponseDto> getAllOrdersByUserId(Long userId);

    boolean isOrderExistsById(Long orderId);
}
