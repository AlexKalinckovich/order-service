package com.example.orderservice.validators.order;

import com.example.orderservice.client.UserServiceClient;
import com.example.orderservice.dto.order.OrderCreateDto;
import com.example.orderservice.dto.order.OrderUpdateDto;
import com.example.orderservice.dto.orderItem.OrderItemCommon;
import com.example.orderservice.dto.orderItem.OrderItemCreateDto;
import com.example.orderservice.dto.orderItem.OrderItemUpdateDto;
import com.example.orderservice.exception.item.ItemNotFoundException;
import com.example.orderservice.exception.order.OrderNotFoundException;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.order.OrderRepository;
import com.example.orderservice.validators.Validator;
import com.example.orderservice.validators.Item.ItemValidator;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderValidator implements Validator<OrderCreateDto, OrderUpdateDto> {

    private final ItemValidator itemValidator;
    private final OrderRepository orderRepository;

    private final UserServiceClient client;

    @Override
    public void validateCreateDto(final OrderCreateDto createDto) {
        final Set<Long> itemIds = createDto.getOrderItems()
                .stream()
                .map(OrderItemCreateDto::getItemId)
                .collect(Collectors.toSet());
        if(itemIds.isEmpty()) {
            throw new ValidationException("itemIds is null or empty");
        }

        validateUserExistence(createDto.getUserId());
        itemValidator.checkItemsToExistByIds(itemIds);
    }

    @Override
    public void validateUpdateDto(final OrderUpdateDto updateDto) {
        final Set<Long> toRemove = safeSet(updateDto.getIdsToRemove());
        final List<OrderItemUpdateDto> toUpdate = safeList(updateDto.getItemsToUpdate());
        final List<OrderItemCreateDto> toAdd = safeList(updateDto.getItemsToAdd());

        final Set<Long> allIds = new HashSet<>();
        allIds.addAll(toRemove);
        allIds.addAll(toUpdate.stream().map(OrderItemUpdateDto::getItemId).toList());
        allIds.addAll(toAdd.stream().map(OrderItemCreateDto::getItemId).toList());

        if (!allIds.isEmpty()) {
            itemValidator.checkItemsToExistByIds(allIds);
        }

    }

    private Set<Long> safeSet(final Set<Long> input) {
        return input != null ? new HashSet<>(input) : new HashSet<>();
    }

    private <T> List<T> safeList(final List<T> input) {
        return input != null ? new ArrayList<>(input) : new ArrayList<>();
    }


    public void validateUserExistence(final Long userId) {
        client.validateUserExists(userId);
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

}
