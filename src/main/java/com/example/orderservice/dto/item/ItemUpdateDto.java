package com.example.orderservice.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.intellij.lang.annotations.RegExp;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemUpdateDto implements ItemCommon{

    private Long id;

    @NotBlank
    @RegExp(prefix = "[a-z]+.*")
    private String name;

    @NotNull
    @NumberFormat(style = NumberFormat.Style.CURRENCY)
    private BigDecimal price;

}
