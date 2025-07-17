package com.example.orderservice.dto.order;

import com.example.orderservice.dto.orderItem.OrderItemCreateDto;
import com.example.orderservice.dto.orderItem.OrderItemUpdateDto;
import com.example.orderservice.model.OrderStatus;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderUpdateDto {

    @NotNull
    @Range(min = 0)
    private Long id;

    @Range(min = 0)
    private Long userId;

    private OrderStatus status;

    @Past
    private LocalDateTime orderDate;

    @NotNull
    private ItemAddedType itemAddedType;

    private List<OrderItemCreateDto> orderItemsToAdd;

    private List<OrderItemUpdateDto> orderItemsToUpdate;
}
