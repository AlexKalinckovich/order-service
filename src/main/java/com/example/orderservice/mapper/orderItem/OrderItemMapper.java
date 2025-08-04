package com.example.orderservice.mapper.orderItem;

import com.example.orderservice.dto.item.ItemResponseDto;
import com.example.orderservice.dto.orderItem.OrderItemCreateDto;
import com.example.orderservice.dto.orderItem.OrderItemResponseDto;
import com.example.orderservice.dto.orderItem.OrderItemUpdateDto;
import com.example.orderservice.mapper.item.ItemMapper;
import com.example.orderservice.model.Item;
import com.example.orderservice.model.OrderItem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface OrderItemMapper {

    @Mapping(source = "item", target = "itemDto", qualifiedByName = "mapItem")
    OrderItemResponseDto toResponseDto(OrderItem orderItem);

    @Named("mapItem")
    ItemResponseDto toItemResponseDto(Item orderItem);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "order", ignore = true),
            @Mapping(target = "item", source = "itemId", qualifiedByName = "mapItemId"),
    })
    OrderItem toEntity(OrderItemCreateDto createDto);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "order", ignore = true),
            @Mapping(target = "item", source = "itemId", qualifiedByName = "mapItemId"),
    })
    List<OrderItem> toEntityList(List<OrderItemCreateDto> createDtos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "order", ignore = true),
            @Mapping(target = "item", source = "itemId", qualifiedByName = "mapItemId"),
    })
    void updateFromDto(OrderItemUpdateDto updateDto, @MappingTarget OrderItem entity);
}
