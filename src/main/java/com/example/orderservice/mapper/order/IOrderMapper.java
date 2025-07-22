package com.example.orderservice.mapper.order;

import com.example.orderservice.dto.order.OrderCreateDto;
import com.example.orderservice.dto.order.OrderResponseDto;
import com.example.orderservice.dto.order.OrderUpdateDto;
import com.example.orderservice.dto.orderItem.OrderItemResponseDto;
import com.example.orderservice.mapper.orderItem.IOrderItemMapper;
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
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        uses = IOrderItemMapper.class,
        imports = {LocalDateTime.class, OrderStatus.class})
public interface IOrderMapper {

    IOrderItemMapper orderItemMapper = Mappers.getMapper(IOrderItemMapper.class);


    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "orderItems", ignore = true),
            @Mapping(target = "status", expression = "java(OrderStatus.CREATED)"),
            @Mapping(target = "orderDate", expression = "java(LocalDateTime.now())")
    })
    Order toEntity(final OrderCreateDto orderCreateDto);

    @Mapping(target = "orderItems", source = "orderItems", qualifiedByName = "mapOrderItems")
    OrderResponseDto toResponseDto(Order order);

    List<OrderResponseDto> toResponseDtoList(final List<Order> orders);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "orderItems", ignore = true)
    void updateFromDto(final OrderUpdateDto orderUpdateDto, @MappingTarget final Order order);

    @Named("mapOrderItems")
    default List<OrderItemResponseDto> mapOrderItems(final List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(orderItemMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}
