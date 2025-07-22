package com.example.orderservice.dto.orderItem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemCreateDto implements OrderItemCommon{

    @NotNull
    @Range(min = 1)
    private Long itemId;

    @NotNull
    @Positive
    @Min(1)
    private Long quantity;

}
