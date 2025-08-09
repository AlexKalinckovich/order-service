package com.example.orderservice.mapper.order;

import com.example.orderservice.dto.order.OrderCreateDto;
import com.example.orderservice.dto.order.OrderResponseDto;
import com.example.orderservice.dto.order.OrderUpdateDto;
import com.example.orderservice.dto.orderItem.OrderItemResponseDto;
import com.example.orderservice.mapper.orderItem.OrderItemMapper;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring",
        uses = OrderItemMapper.class,
        imports = {LocalDateTime.class, OrderStatus.class})
public interface OrderMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "orderItems", ignore = true),
            @Mapping(target = "status", ignore = true, qualifiedByName = "mapOrderStatus"),
            @Mapping(target = "orderDate", ignore = true, qualifiedByName = "mapOrderDate")
    })
    Order toEntity(final OrderCreateDto orderCreateDto);

    @Mapping(target = "orderItems", source = "orderItems")
    OrderResponseDto toResponseDto(Order order);

    List<OrderResponseDto> toResponseDtoList(final List<Order> orders);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "orderItems", ignore = true)
    void updateFromDto(final OrderUpdateDto orderUpdateDto, @MappingTarget final Order order);

    List<OrderItemResponseDto> mapOrderItems(List<OrderItem> items);

    @Named("mapOrderStatus")
    default OrderStatus mapOrderStatus(final OrderStatus status) {
        return Objects.requireNonNullElse(status, OrderStatus.CREATED);
    }

    @Named("mapOrderDate")
    default LocalDateTime mapOrderDate(final LocalDateTime orderDate) {
        return Objects.requireNonNullElseGet(orderDate, LocalDateTime::now);
    }
}
