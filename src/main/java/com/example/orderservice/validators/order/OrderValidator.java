package com.example.orderservice.validators.order;

import com.example.orderservice.client.UserServiceClient;
import com.example.orderservice.dto.order.ItemAddedType;
import com.example.orderservice.dto.order.OrderCreateDto;
import com.example.orderservice.dto.order.OrderUpdateDto;
import com.example.orderservice.dto.orderItem.OrderItemCommon;
import com.example.orderservice.dto.orderItem.OrderItemCreateDto;
import com.example.orderservice.exception.item.ItemNotFoundException;
import com.example.orderservice.exception.order.OrderNotFoundException;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.order.IOrderRepository;
import com.example.orderservice.validators.IValidator;
import com.example.orderservice.validators.Item.ItemValidator;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderValidator implements IValidator<OrderCreateDto, OrderUpdateDto> {

    private final ItemValidator itemValidator;
    private final IOrderRepository orderRepository;

    private final UserServiceClient client;

    @Override
    public void validateCreateDto(final OrderCreateDto createDto) {
        final List<Long> itemIds = createDto.getOrderItems()
                .stream()
                .map(OrderItemCreateDto::getItemId)
                .toList();
        if(itemIds.isEmpty()) {
            throw new ValidationException("itemIds is null or empty");
        }

        client.validateUserExists(createDto.getUserId());
        itemValidator.checkItemsToExistByIds(itemIds);
    }

    @Override
    public void validateUpdateDto(final OrderUpdateDto updateDto) {
        final ItemAddedType addedType = updateDto.getItemAddedType();

        if(addedType == ItemAddedType.NOT_UPDATED){
            return;
        }

        final Long userId = updateDto.getUserId();
        if(userId != null){
            client.validateUserExists(userId);
        }

        final List<? extends OrderItemCommon> orderItems = getOrderItems(updateDto, addedType);

        validateOrderItems(orderItems, addedType);

        final List<Long> quantities = orderItems.stream()
                .map(OrderItemCommon::getQuantity)
                .collect(Collectors.toList());
        final List<Long> itemIds = orderItems.stream()
                .map(OrderItemCommon::getItemId)
                .collect(Collectors.toList());

        checkQuantityToNegativeOrZeroValues(quantities);

        itemValidator.checkItemsToExistByIds(itemIds);
    }

    public List<Order> checkOrdersToExistence(final List<Long> orderIds){
        if(orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Order> orders = orderRepository.findAllById(orderIds);

        if (orders.size() != orderIds.size()) {
            final Set<Long> foundIds = orders.stream()
                    .map(Order::getId)
                    .collect(Collectors.toSet());

            final List<Long> missingIds = orderIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            throw new ItemNotFoundException("Missing items: " + missingIds);
        }
        return orders;
    }

    public Order checkOrderToExistence(final Long orderId){
        if(orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("orderId is null or empty");
        }

        final Optional<Order> order = orderRepository.findById(orderId);
        if(order.isEmpty()){
            throw new OrderNotFoundException(orderId);
        }

        return order.get();
    }


    private void validateOrderItems(final List<? extends OrderItemCommon> items,
                                    final ItemAddedType addedType) {
        if (items == null || items.isEmpty()) {
            final String message = (addedType == ItemAddedType.APPEND || addedType == ItemAddedType.REPLACE)
                    ? "createOrderItem is null or empty"
                    : "updateOrderItems is null or empty";
            throw new ValidationException(message);
        }
    }

    private List<? extends OrderItemCommon> getOrderItems(final OrderUpdateDto updateDto,
                                                          final ItemAddedType addedType) {
        List<? extends OrderItemCommon> result;

        if (addedType == ItemAddedType.APPEND || addedType == ItemAddedType.REPLACE) {
            result = updateDto.getOrderItemsToAdd();
        } else {
            result = updateDto.getOrderItemsToUpdate();
        }

        return result;
    }

    private void checkQuantityToNegativeOrZeroValues(final List<Long> quantities){
        final List<Long> negativeQuantities = quantities.stream().filter((q) -> q < 1).toList();
        if(!negativeQuantities.isEmpty()) {
            throw new ValidationException("Cannot handle negative quantity: " + negativeQuantities);
        }
    }


}
