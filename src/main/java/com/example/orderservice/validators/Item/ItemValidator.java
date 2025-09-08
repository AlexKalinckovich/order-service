package com.example.orderservice.validators.Item;

import com.example.orderservice.dto.item.ItemCreateDto;
import com.example.orderservice.dto.item.ItemUpdateDto;
import com.example.orderservice.exception.item.ItemNotFoundException;
import com.example.orderservice.model.Item;
import com.example.orderservice.repository.item.ItemRepository;
import com.example.orderservice.validators.Validator;
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
public class ItemValidator implements Validator<ItemCreateDto, ItemUpdateDto> {

    private final ItemRepository itemRepository;

    @Override
    public void validateCreateDto(final ItemCreateDto createDto) {
        final String itemName = createDto.getName();

        checkNameUniques(itemName);

    }

    @Override
    public void validateUpdateDto(final ItemUpdateDto updateDto) {
        final String itemName = updateDto.getName();

        checkNameUniques(itemName);

    }

    private void checkNameUniques(final String itemName){
        boolean exists = itemRepository.existsByName(itemName);
        if(exists) {
            throw new ValidationException("Item with name " + itemName + " already exists");
        }

    }

    public List<Item> getExistsItems(final List<Long> itemIds){
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Item> items = itemRepository.findExistingItems(itemIds);

        if (items.size() != itemIds.size()) {
            final Set<Long> foundIds = items.stream()
                    .map(Item::getId)
                    .collect(Collectors.toSet());

            final List<Long> missingIds = itemIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            throw new ItemNotFoundException("Missing items: " + missingIds);
        }
        return items;
    }

    public List<Long> checkItemsToExistByIds(final Set<Long> itemIds){
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyList();
        }
        final List<Long> existingItemsIds = itemRepository.findExistingItemsIds(itemIds);
        if (existingItemsIds.size() != itemIds.size()) {
            final List<Long> missingIds = itemIds.stream()
                    .filter(id -> !existingItemsIds.contains(id))
                    .toList();
            throw new ItemNotFoundException("Missing existingItemsIds: " + missingIds);
        }
        return existingItemsIds;
    }

    public Item checkItemToExistence(final Long id){
        final Optional<Item> item = itemRepository.findById(id);
        if(item.isEmpty()) {
            throw new ItemNotFoundException("Item with id " + id + " does not exist");
        }

        return item.get();
    }

}
