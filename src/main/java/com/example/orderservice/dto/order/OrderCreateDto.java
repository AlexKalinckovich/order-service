package com.example.orderservice.dto.order;

import com.example.orderservice.dto.orderItem.OrderItemCreateDto;
import com.example.orderservice.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateDto {

    @Range(min = 0)
    private Long userId;

    @NotNull
    private OrderStatus status;

    @Past
    private LocalDateTime orderDate;

    @NotNull
    @Valid
    private List<OrderItemCreateDto> orderItems;
}
