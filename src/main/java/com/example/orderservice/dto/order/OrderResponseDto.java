package com.example.orderservice.dto.order;

import com.example.orderservice.dto.orderItem.OrderItemResponseDto;
import com.example.orderservice.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long id;

    private Long userId;

    private OrderStatus status;

    private LocalDateTime orderDate;

    private List<OrderItemResponseDto> orderItems;

}
