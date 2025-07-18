package com.example.orderservice.controller.order;

import com.example.orderservice.dto.order.OrderCreateDto;
import com.example.orderservice.dto.order.OrderResponseDto;
import com.example.orderservice.dto.order.OrderUpdateDto;
import com.example.orderservice.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@RequiredArgsConstructor
@RequestMapping("/order")
@Validated
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponseDto> create(@Valid @RequestBody final OrderCreateDto createDto){
        final OrderResponseDto createdOrder = orderService.createOrder(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PutMapping("/update")
    public ResponseEntity<OrderResponseDto> update(@Valid @RequestBody final OrderUpdateDto updateDto){
        final OrderResponseDto updatedOrder = orderService.updateOrder(updateDto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponseDto> delete(@PathVariable final Long id){
        final OrderResponseDto deletedOrder = orderService.deleteOrder(id);
        return ResponseEntity.status(HttpStatus.FOUND).body(deletedOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> get(@PathVariable final Long id){
        final OrderResponseDto order = orderService.getOrderById(id);
        return ResponseEntity.status(HttpStatus.FOUND).body(order);
    }

    @GetMapping("/list/{ids}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByIds(final @PathVariable List<Long> ids){
        final List<OrderResponseDto> orders = orderService.getAllOrdersByIds(ids);
        return ResponseEntity.status(HttpStatus.FOUND).body(orders);
    }
}
