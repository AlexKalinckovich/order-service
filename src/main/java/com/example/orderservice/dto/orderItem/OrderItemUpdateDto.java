package com.example.orderservice.dto.orderItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemUpdateDto implements OrderItemCommon{
    private Long id;
    private Long orderId;
    private Long itemId;
    private Long quantity;
}
