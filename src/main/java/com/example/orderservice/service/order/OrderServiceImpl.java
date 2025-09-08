package com.example.orderservice.service.order;

import com.example.orderservice.dto.event.PaymentStatus;
import com.example.orderservice.dto.order.Operation;
import com.example.orderservice.dto.order.OperationType;
import com.example.orderservice.dto.order.OrderCreateDto;
import com.example.orderservice.dto.order.OrderResponseDto;
import com.example.orderservice.dto.order.OrderUpdateDto;
import com.example.orderservice.dto.order.OrderUpdateNormalizer;
import com.example.orderservice.exception.order.OrderNotFoundException;
import com.example.orderservice.kafka.OrderEventPublisher;
import com.example.orderservice.mapper.order.OrderMapper;
import com.example.orderservice.mapper.orderItem.OrderItemMapper;
import com.example.orderservice.model.Item;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.item.ItemRepository;
import com.example.orderservice.repository.order.OrderRepository;
import com.example.orderservice.validators.order.OrderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Qualifier("orderServiceImpl")
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    private final OrderEventPublisher orderEventPublisher;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    private final OrderValidator orderValidator;

    @Transactional
    @Override
    public OrderResponseDto createOrder(final OrderCreateDto orderCreateDto) {
        orderValidator.validateCreateDto(orderCreateDto);

        final Order createOrder = orderMapper.toEntity(orderCreateDto);

        final List<OrderItem> orderItems = orderItemMapper.toEntityList(orderCreateDto.getOrderItems());

        final List<Long> itemIds = orderItems.stream()
                .map(oi -> oi.getItem().getId())
                .distinct()
                .toList();
        final List<Item> items = itemRepository.findExistingItems(itemIds);

        final Map<Long, Item> itemById = items.stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (final OrderItem oi : orderItems) {
            oi.setOrder(createOrder);

            final Item orderedItem = itemById.get(oi.getItem().getId());
            totalPrice = totalPrice.add(
                    orderedItem.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity()))
            );
            oi.setItem(orderedItem);
        }
        createOrder.setOrderTotal(totalPrice);
        createOrder.getOrderItems().addAll(orderItems);

        final Order saved = orderRepository.save(createOrder);

        orderEventPublisher.publishOrderCreated(saved,totalPrice);
        return orderMapper.toResponseDto(saved);
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

        final Map<Long, Operation> ops = OrderUpdateNormalizer.normalize(orderUpdateDto);

        final List<OrderItem> items = order.getOrderItems();

        final Map<Long, OrderItem> existing = items.stream()
                .collect(Collectors.toMap(oi -> oi.getItem().getId(), oi -> oi));

        for (final Map.Entry<Long, Operation> entry : ops.entrySet()) {
            final Long itemId = entry.getKey();
            final Operation op = entry.getValue();
            final OperationType type = op.getType();
            switch (type) {
                case REMOVE -> {
                    items.removeIf((final OrderItem oi) -> oi.getItem().getId().equals(itemId));
                }
                case UPDATE, ADD -> {
                    handleModifying(existing, itemId, type, op, order, items);
                }
            }
        }
        final BigDecimal updatedTotal = calculateNewTotal(order.getOrderItems());
        if(!updatedTotal.equals(order.getOrderTotal())) {
            order.setOrderTotal(updatedTotal);
        }
        orderRepository.saveAndFlush(order);
        return orderMapper.toResponseDto(order);
    }

    private void handleModifying(final Map<Long, OrderItem> existing,
                                 final long itemId,
                                 final OperationType operationType,
                                 final Operation op,
                                 final Order order,
                                 final List<OrderItem> items) {
        final OrderItem oi = existing.get(itemId);
        if (oi != null) {
            long updatedQuantity = op.getQuantity();
            if(operationType == OperationType.ADD){
                updatedQuantity += oi.getQuantity();
            }
            oi.setQuantity(updatedQuantity);
        } else {
            final Item item = itemRepository.getReferenceById(itemId);
            final OrderItem newItem = OrderItem.builder()
                    .order(order)
                    .item(item)
                    .quantity(op.getQuantity())
                    .build();
            items.add(newItem);
            existing.put(itemId, newItem);
        }
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

    @Override
    public BigDecimal getOrderTotalById(final Long orderId) {
        final Order order = orderValidator.checkOrderToExistence(orderId);
        return order.getOrderTotal();
    }

    @Override
    @Transactional
    public void updateOrderStatus(final Long orderId, final PaymentStatus paymentStatus) {
        final Order order = orderValidator.checkOrderToExistence(orderId);
        final OrderStatus status = paymentStatus == PaymentStatus.SUCCESS ?
                OrderStatus.PAID :
                OrderStatus.UNPAID;
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Override
    public List<OrderResponseDto> getAllOrdersByUserId(final Long userId) {
        orderValidator.validateUserExistence(userId);
        final List<Order> userOrders = orderRepository.findOrdersByUserId(userId);
        return orderMapper.toResponseDtoList(userOrders);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean isOrderExistsById(final Long orderId) {
        return orderRepository.existsById(orderId);
    }

    private BigDecimal calculateNewTotal(final List<OrderItem> items){
        BigDecimal total = BigDecimal.ZERO;
        for(final OrderItem oi : items){
            final BigDecimal price = oi.getItem().getPrice();
            final long quantity = oi.getQuantity();
            total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
        }
        return total;
    }
}
