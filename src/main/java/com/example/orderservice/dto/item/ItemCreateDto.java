package com.example.orderservice.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.intellij.lang.annotations.RegExp;
import org.springframework.format.annotation.NumberFormat;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemCreateDto implements ItemCommon {

    @NotBlank
    @Size(min = 2, max = 30)
    @RegExp(prefix = "[a-z]+.*")
    private String name;

    @NotNull
    @NumberFormat(style = NumberFormat.Style.CURRENCY)
    @Range(min = 1, max = 100_000)
    private BigDecimal price;

}
