package com.example.orderservice.service.order;

import com.example.orderservice.dto.order.ItemAddedType;
import com.example.orderservice.dto.order.OrderCreateDto;
import com.example.orderservice.dto.order.OrderResponseDto;
import com.example.orderservice.dto.order.OrderUpdateDto;
import com.example.orderservice.dto.orderItem.OrderItemCreateDto;
import com.example.orderservice.dto.orderItem.OrderItemUpdateDto;
import com.example.orderservice.exception.order.OrderNotFoundException;
import com.example.orderservice.exception.orderItem.OrderItemNotFoundException;
import com.example.orderservice.mapper.order.OrderMapper;
import com.example.orderservice.mapper.orderItem.OrderItemMapper;
import com.example.orderservice.model.Item;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.repository.item.ItemRepository;
import com.example.orderservice.repository.order.OrderRepository;
import com.example.orderservice.validators.order.OrderValidator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;


    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    private final OrderValidator orderValidator;

    @Transactional
    @Override
    public OrderResponseDto createOrder(final OrderCreateDto orderCreateDto) {
        orderValidator.validateCreateDto(orderCreateDto);

        final Order createOrder = orderMapper.toEntity(orderCreateDto);

        final List<OrderItem> orderItems = orderItemMapper.toEntityList(orderCreateDto.getOrderItems());
        for (final OrderItem orderItem : orderItems) {
            final Item item = itemRepository.getReferenceById(orderItem.getItem().getId());
            orderItem.setOrder(createOrder);
            orderItem.setItem(item);
        }

        createOrder.getOrderItems().addAll(orderItems);

        final Order order = orderRepository.save(createOrder);
        return orderMapper.toResponseDto(order);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderResponseDto> getAllOrdersByIds(final List<Long> orderIds) {
        final List<Order> orders = orderValidator.checkOrdersToExistence(orderIds);
        return orderMapper.toResponseDtoList(orders);
    }

    @Transactional(readOnly = true)
    @Override
    public OrderResponseDto getOrderById(final Long id) {
        final Order order = orderValidator.checkOrderToExistence(id);
        return orderMapper.toResponseDto(order);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrder(final OrderUpdateDto orderUpdateDto) {
        orderValidator.validateUpdateDto(orderUpdateDto);

        final Order order = orderRepository.findById(orderUpdateDto.getId())
                .orElseThrow(() -> new OrderNotFoundException(orderUpdateDto.getId()));

        orderMapper.updateFromDto(orderUpdateDto, order);


        final ItemAddedType addedType = orderUpdateDto.getItemAddedType();
        if (addedType != ItemAddedType.NOT_UPDATED) {
            if (addedType == ItemAddedType.APPEND) {
                appendOrderItems(order, orderUpdateDto.getOrderItemsToAdd());
            } else if (addedType == ItemAddedType.REPLACE) {
                replaceOrderItems(order, orderUpdateDto.getOrderItemsToAdd());
            } else if (addedType == ItemAddedType.UPDATE) {
                updateExistingOrderItems(order, orderUpdateDto.getOrderItemsToUpdate());
            }
        }

        orderRepository.saveAndFlush(order);
        return orderMapper.toResponseDto(order);
    }


    @Override
    @Transactional
    public OrderResponseDto deleteOrder(final Long orderId) {
        final Order order = orderRepository.findWithOrderItemsById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        final OrderResponseDto response = orderMapper.toResponseDto(order);

        orderRepository.delete(order);

        return response;
    }

    private void appendOrderItems(final Order order, final List<OrderItemCreateDto> createDtos) {
        final List<OrderItem> newItems = orderItemMapper.toEntityList(createDtos);
        newItems.forEach(item -> {
            final Item originalItem = itemRepository.getReferenceById(item.getItem().getId());
            item.setOrder(order);
            item.setItem(originalItem);
            order.getOrderItems().add(item);
        });
    }

    private void replaceOrderItems(final @NotNull Order order,
                                   final List<OrderItemCreateDto> createDtos) {
        order.getOrderItems().clear();

        appendOrderItems(order, createDtos);
    }

    private void updateExistingOrderItems(final @NotNull Order order,
                                          final @NotNull List<OrderItemUpdateDto> updateDtos) {
        final Map<Long, OrderItem> existingItemsMap = order.getOrderItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, Function.identity()));

        for (final OrderItemUpdateDto dto : updateDtos) {
            final OrderItem item = existingItemsMap.get(dto.getId());
            if (item == null) {
                throw new OrderItemNotFoundException(dto.getId());
            }
            orderItemMapper.updateFromDto(dto, item);
        }
    }

}
