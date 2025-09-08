package com.example.orderservice.service.item;

import com.example.orderservice.dto.item.ItemCreateDto;
import com.example.orderservice.dto.item.ItemResponseDto;
import com.example.orderservice.dto.item.ItemUpdateDto;

import java.util.List;

public interface ItemService {

    ItemResponseDto createItem(final ItemCreateDto orderCreateDto);

    List<ItemResponseDto> getAllItemsByIds(List<Long> orderIds);

    ItemResponseDto getItemById(final Long orderId);

    ItemResponseDto updateItem(final ItemUpdateDto orderUpdateDto);

    ItemResponseDto deleteItem(final Long orderId);

    List<ItemResponseDto> getAllItems();
}
