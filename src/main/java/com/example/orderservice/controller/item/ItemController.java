package com.example.orderservice.controller.item;

import com.example.orderservice.dto.item.ItemCreateDto;
import com.example.orderservice.dto.item.ItemResponseDto;
import com.example.orderservice.dto.item.ItemUpdateDto;
import com.example.orderservice.service.item.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping("/create")
    public ResponseEntity<ItemResponseDto> create(@Valid @RequestBody final ItemCreateDto itemCreateDto) {
        final ItemResponseDto itemResponseDto = itemService.createItem(itemCreateDto);
        return ResponseEntity.ok(itemResponseDto);
    }

    @PutMapping("/update")
    public ResponseEntity<ItemResponseDto> update(@Valid @RequestBody final ItemUpdateDto itemUpdateDto) {
        final ItemResponseDto itemResponseDto = itemService.updateItem(itemUpdateDto);
        return ResponseEntity.ok(itemResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ItemResponseDto> delete(@Valid @PathVariable final Long id) {
        final ItemResponseDto itemResponseDto = itemService.deleteItem(id);
        return ResponseEntity.ok(itemResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> get(@Valid @PathVariable final Long id) {
        final ItemResponseDto itemResponseDto = itemService.getItemById(id);
        return ResponseEntity.ok(itemResponseDto);
    }

    @GetMapping("/list/{ids}")
    public ResponseEntity<List<ItemResponseDto>> getItemsByIds(@PathVariable final List<Long> ids) {
        final List<ItemResponseDto> items = itemService.getAllItemsByIds(ids);
        return ResponseEntity.ok(items);
    }

}
