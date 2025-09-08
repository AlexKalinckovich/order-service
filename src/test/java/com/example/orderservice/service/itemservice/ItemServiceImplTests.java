package com.example.orderservice.service.itemservice;

import com.example.orderservice.dto.item.ItemCreateDto;
import com.example.orderservice.dto.item.ItemResponseDto;
import com.example.orderservice.dto.item.ItemUpdateDto;
import com.example.orderservice.exception.item.ItemNotFoundException;
import com.example.orderservice.mapper.item.ItemMapper;
import com.example.orderservice.model.Item;
import com.example.orderservice.repository.item.ItemRepository;
import com.example.orderservice.service.item.ItemServiceImpl;
import com.example.orderservice.util.AbstractContainerBaseTest;
import com.example.orderservice.validators.Item.ItemValidator;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ItemServiceImplTests extends AbstractContainerBaseTest {

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private ItemValidator itemValidator;

    @Autowired
    private ItemServiceImpl itemServiceImpl;

    protected static final Long ITEM_ID = 1L;
    protected static final Long NON_EXISTENT_ITEM_ID = 999L;
    protected static final String ITEM_NAME = "Test Item";
    protected static final String UPDATED_ITEM_NAME = "Updated Test Item";
    protected static final BigDecimal ITEM_PRICE = BigDecimal.valueOf(19.99);
    protected static final BigDecimal UPDATED_ITEM_PRICE = BigDecimal.valueOf(29.99);

    protected Item testItem;
    protected ItemResponseDto testItemResponseDto;
    protected ItemCreateDto testItemCreateDto;
    protected ItemUpdateDto testItemUpdateDto;

    @BeforeEach
    void setUp() {
        testItem = Item.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .price(ITEM_PRICE)
                .build();

        testItemResponseDto = ItemResponseDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME)
                .price(ITEM_PRICE)
                .build();

        testItemCreateDto = ItemCreateDto.builder()
                .name(ITEM_NAME)
                .price(ITEM_PRICE)
                .build();

        testItemUpdateDto = ItemUpdateDto.builder()
                .id(ITEM_ID)
                .name(UPDATED_ITEM_NAME)
                .price(UPDATED_ITEM_PRICE)
                .build();

        doNothing().when(itemValidator).validateCreateDto(any(ItemCreateDto.class));
        doNothing().when(itemValidator).validateUpdateDto(any(ItemUpdateDto.class));
        when(itemValidator.checkItemToExistence(ITEM_ID)).thenReturn(testItem);
        when(itemValidator.getExistsItems(anyList())).thenReturn(List.of(testItem));

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            if (item.getId() == null) {
                item.setId(ITEM_ID); // Simulate ID generation
            }
            return item;
        });

        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(testItem));
        when(itemRepository.findById(NON_EXISTENT_ITEM_ID)).thenReturn(Optional.empty());
    }

    @Test
    @Transactional
    void createItem_ValidInput_ShouldCreateAndReturnItem() {
        // Given
        final ItemCreateDto createDto = createSampleItemCreateDto();

        // When
        final ItemResponseDto result = itemServiceImpl.createItem(createDto);

        // Then
        assertNotNull(result);
        assertEquals(ITEM_ID, result.getId());
        assertEquals(ITEM_NAME, result.getName());
        assertEquals(ITEM_PRICE, result.getPrice());

        verify(itemValidator).validateCreateDto(createDto);

        final ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());

        final Item savedItem = itemCaptor.getValue();
        assertEquals(ITEM_NAME, savedItem.getName());
        assertEquals(ITEM_PRICE, savedItem.getPrice());
    }

    @Test
    void getAllItemsByIds_ValidIds_ShouldReturnItems() {
        // Given
        final List<Long> itemIds = List.of(1L, 2L, 3L);

        final Item item1 = Item.builder().id(1L).name("Item 1").price(BigDecimal.TEN).build();
        final Item item2 = Item.builder().id(2L).name("Item 2").price(BigDecimal.valueOf(20)).build();
        final Item item3 = Item.builder().id(3L).name("Item 3").price(BigDecimal.valueOf(30)).build();

        when(itemValidator.getExistsItems(itemIds))
                .thenReturn(List.of(item1, item2, item3));

        // When
        final List<ItemResponseDto> result = itemServiceImpl.getAllItemsByIds(itemIds);

        // Then
        assertEquals(3, result.size());
        assertThat(result.stream().map(ItemResponseDto::getId).collect(Collectors.toList()))
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    void getItemById_ExistingId_ShouldReturnItem() {
        // Given
        final Item expectedItem = createSampleItemEntity();
        when(itemValidator.checkItemToExistence(ITEM_ID)).thenReturn(expectedItem);

        // When
        final ItemResponseDto result = itemServiceImpl.getItemById(ITEM_ID);

        // Then
        assertNotNull(result);
        assertEquals(ITEM_ID, result.getId());
        assertEquals(ITEM_NAME, result.getName());
        assertEquals(ITEM_PRICE, result.getPrice());
    }

    @Test
    void getItemById_NonExistingId_ShouldThrowException() {
        // Given
        final Long invalidId = 999L;
        when(itemValidator.checkItemToExistence(invalidId))
                .thenThrow(new ItemNotFoundException(invalidId));

        // When & Then
        assertThrows(ItemNotFoundException.class, () ->
                itemServiceImpl.getItemById(invalidId));
    }

    @Test
    @Transactional
    void updateItem_ValidInput_ShouldUpdateAndReturnItem() {
        // Given
        final String updatedName = "Updated Item";
        final BigDecimal updatedPrice = BigDecimal.valueOf(29.99);

        final ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .id(ITEM_ID)
                .name(updatedName)
                .price(updatedPrice)
                .build();

        final Item existingItem = createSampleItemEntity();

        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(existingItem));

        // When
        ItemResponseDto result = itemServiceImpl.updateItem(updateDto);

        // Then
        assertNotNull(result);
        assertEquals(ITEM_ID, result.getId());
        assertEquals(updatedName, result.getName());
        assertEquals(updatedPrice, result.getPrice());

        // Verify entity was updated
        assertEquals(updatedName, existingItem.getName());
        assertEquals(updatedPrice, existingItem.getPrice());
    }

    @Test
    @Transactional
    void updateItem_PartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Given
        final String updatedName = "Partial Update Item";

        final ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .id(ITEM_ID)
                .name(updatedName)
                // Price not provided
                .build();

        final Item existingItem = createSampleItemEntity();
        final BigDecimal originalPrice = existingItem.getPrice();

        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(existingItem));

        // When
        final ItemResponseDto result = itemServiceImpl.updateItem(updateDto);

        // Then
        assertEquals(updatedName, result.getName());
        assertEquals(originalPrice, result.getPrice());

        assertEquals(updatedName, existingItem.getName());
        assertEquals(originalPrice, existingItem.getPrice());
    }

    @Test
    @Transactional
    void deleteItem_ExistingItem_ShouldDeleteAndReturnResponse() {
        // Given
        final Item existingItem = createSampleItemEntity();
        when(itemValidator.checkItemToExistence(ITEM_ID)).thenReturn(existingItem);

        // When
        final ItemResponseDto result = itemServiceImpl.deleteItem(ITEM_ID);

        // Then
        assertNotNull(result);
        assertEquals(ITEM_ID, result.getId());
        verify(itemRepository).delete(existingItem);
    }

    @Test
    @Transactional
    void deleteItem_NonExistingItem_ShouldThrowException() {
        // Given
        final Long invalidId = 999L;
        when(itemValidator.checkItemToExistence(invalidId))
                .thenThrow(new ItemNotFoundException(invalidId));

        // When & Then
        assertThrows(ItemNotFoundException.class, () ->
                itemServiceImpl.deleteItem(invalidId));
        verify(itemRepository, never()).delete(any());
    }

    @Test
    void createItem_InvalidInput_ShouldCallValidator() {
        // Given
        final ItemCreateDto invalidDto = ItemCreateDto.builder()
                .name("") // Invalid name
                .price(BigDecimal.ZERO) // Invalid price
                .build();

        doThrow(new ValidationException("Validation failed"))
                .when(itemValidator).validateCreateDto(invalidDto);

        // When & Then
        assertThrows(ValidationException.class, () ->
                itemServiceImpl.createItem(invalidDto));
    }

    @Test
    void updateItem_InvalidInput_ShouldCallValidator() {
        // Given
        final ItemUpdateDto invalidDto = ItemUpdateDto.builder()
                .id(null) // Invalid ID
                .name("Valid Name")
                .price(BigDecimal.TEN)
                .build();

        doThrow(new ValidationException("Validation failed"))
                .when(itemValidator).validateUpdateDto(invalidDto);

        // When & Then
        assertThrows(ValidationException.class, () ->
                itemServiceImpl.updateItem(invalidDto));
    }


    private Item createSampleItemEntity() {
        final long id = 1L;
        return Item.builder()
                .id(id)
                .name(ITEM_NAME)
                .price(ITEM_PRICE)
                .build();
    }

    private ItemCreateDto createSampleItemCreateDto() {
        return ItemCreateDto.builder()
                .name(ITEM_NAME)
                .price(ITEM_PRICE)
                .build();
    }
}
