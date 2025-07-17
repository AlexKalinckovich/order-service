package com.example.orderservice.mapper.item;

import com.example.orderservice.dto.item.ItemCommon;
import com.example.orderservice.dto.item.ItemCreateDto;
import com.example.orderservice.dto.item.ItemResponseDto;
import com.example.orderservice.model.Item;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IItemMapper {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "orderItems", ignore = true)
    })
    Item toEntity(final ItemCreateDto createDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "orderItems", ignore = true)
    })
    void updateFromCommon(final ItemCommon source, @MappingTarget final Item target);

    ItemResponseDto toResponseDto(final Item item);

    List<ItemResponseDto> toResponseDtoList(final List<Item> items);

    @Named("mapItemId")
    default Item mapItemId(final Long itemId) {
        Item result = null;
        if(itemId != null){
            result = Item.builder()
                    .id(itemId)
                    .build();
        }
        return result;
    }
}
