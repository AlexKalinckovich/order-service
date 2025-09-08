package com.example.orderservice.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class Operation {
    private OperationType type;
    private Long quantity;

    public static Operation add(Long qty) { return of(OperationType.ADD, qty); }
    public static Operation update(Long qty) { return of(OperationType.UPDATE, qty); }
    public static Operation remove() { return of(OperationType.REMOVE, null); }
}