package com.example.orderservice.service.order;

import com.example.orderservice.dto.order.OrderCreateDto;
import com.example.orderservice.dto.order.OrderResponseDto;
import com.example.orderservice.dto.order.OrderUpdateDto;

import java.util.List;

public interface IOrderService {

    OrderResponseDto createOrder(final OrderCreateDto orderCreateDto);

    List<OrderResponseDto> getAllOrdersByIds(List<Long> orderIds);

    OrderResponseDto getOrderById(final Long orderId);

    OrderResponseDto updateOrder(final OrderUpdateDto orderUpdateDto);

    OrderResponseDto deleteOrder(final Long orderId);

}
