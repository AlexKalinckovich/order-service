package com.example.orderservice.service.item;

import com.example.orderservice.dto.item.ItemCreateDto;
import com.example.orderservice.dto.item.ItemResponseDto;
import com.example.orderservice.dto.item.ItemUpdateDto;
import com.example.orderservice.exception.item.ItemNotFoundException;
import com.example.orderservice.mapper.item.ItemMapper;
import com.example.orderservice.model.Item;
import com.example.orderservice.repository.item.ItemRepository;
import com.example.orderservice.validators.Item.ItemValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final ItemValidator itemValidator;

    @Transactional
    @Override
    public ItemResponseDto createItem(final ItemCreateDto createDto) {
        itemValidator.validateCreateDto(createDto);

        final Item newItem = itemMapper.toEntity(createDto);

        final Item savedItem = itemRepository.save(newItem);

        return itemMapper.toResponseDto(savedItem);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemResponseDto> getAllItemsByIds(final List<Long> itemIds) {
        final List<Item> existingItems = itemValidator.getExistsItems(itemIds);
        return itemMapper.toResponseDtoList(existingItems);
    }

    @Transactional(readOnly = true)
    @Override
    public ItemResponseDto getItemById(final Long orderId) {
        final Item item = itemValidator.checkItemToExistence(orderId);
        return itemMapper.toResponseDto(item);
    }

    @Transactional
    @Override
    public ItemResponseDto updateItem(final ItemUpdateDto updateDto) {
        itemValidator.validateUpdateDto(updateDto);

        final Item item = itemRepository.findById(updateDto.getId())
                .orElseThrow(() -> new ItemNotFoundException(updateDto.getId()));

        itemMapper.updateFromCommon(updateDto, item);

        return itemMapper.toResponseDto(item);
    }

    @Transactional
    @Override
    public ItemResponseDto deleteItem(final Long orderId) {
        final Item item = itemValidator.checkItemToExistence(orderId);
        itemRepository.delete(item);
        return itemMapper.toResponseDto(item);
    }
}
