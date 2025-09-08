package com.example.orderservice.dto.order;

import com.example.orderservice.dto.orderItem.OrderItemCreateDto;
import com.example.orderservice.dto.orderItem.OrderItemUpdateDto;

import java.util.HashMap;
import java.util.Map;

public class OrderUpdateNormalizer {

    public static Map<Long, Operation> normalize(final OrderUpdateDto dto) {
        final Map<Long, Operation> ops = new HashMap<>();

        if (dto.getIdsToRemove() != null) {
            for (final Long id : dto.getIdsToRemove()) {
                ops.put(id, Operation.remove());
            }
        }

        if (dto.getItemsToUpdate() != null) {
            for (final OrderItemUpdateDto upd : dto.getItemsToUpdate()) {
                if(!ops.containsKey(upd.getItemId())) {
                    ops.put(upd.getItemId(), Operation.update(upd.getQuantity()));
                }
            }
        }

        if (dto.getItemsToAdd() != null) {
            for (final OrderItemCreateDto add : dto.getItemsToAdd()) {
                final Long id = add.getItemId();
                final Operation existing = ops.get(id);

                if (existing != null && existing.getType() == OperationType.REMOVE) {
                    ops.put(id, Operation.update(add.getQuantity()));
                } else {
                    ops.put(id, Operation.add(add.getQuantity()));
                }
            }
        }

        return ops;
    }
}

