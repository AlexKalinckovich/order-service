package com.example.orderservice.dto.orderItem;

import com.example.orderservice.dto.item.ItemResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDto {

    private Long id;

    private ItemResponseDto itemDto;

    private Long quantity;
}
