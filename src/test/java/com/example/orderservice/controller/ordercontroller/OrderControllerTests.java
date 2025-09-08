package com.example.orderservice.controller.ordercontroller;

import com.example.orderservice.config.MessageConfig;
import com.example.orderservice.controller.order.OrderController;
import com.example.orderservice.dto.item.ItemResponseDto;
import com.example.orderservice.dto.order.OrderCreateDto;
import com.example.orderservice.dto.order.OrderResponseDto;
import com.example.orderservice.dto.order.OrderUpdateDto;
import com.example.orderservice.dto.orderItem.OrderItemCreateDto;
import com.example.orderservice.dto.orderItem.OrderItemResponseDto;
import com.example.orderservice.dto.orderItem.OrderItemUpdateDto;
import com.example.orderservice.exception.GlobalExceptionHandler;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.exception.response.ExceptionResponseService;
import com.example.orderservice.service.message.MessageService;
import com.example.orderservice.service.order.OrderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc
@Import({MessageService.class, ExceptionResponseService.class, MessageConfig.class, GlobalExceptionHandler.class})
public class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderServiceImpl orderServiceImpl;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final Long USER_ID = 10L;
    private static final Long STANDARD_ID = 1L;
    private static final Long STANDARD_ITEM_ID = 101L;
    private static final Long STANDARD_QUANTITY = 2L;
    
    private OrderCreateDto createDto;
    private OrderUpdateDto updateDto;
    private OrderResponseDto responseDto;

    @BeforeEach
    void setup() {
        final ItemResponseDto itemDto1 = ItemResponseDto.builder()
                .id(STANDARD_ID)
                .name("Test Item 1")
                .price(BigDecimal.valueOf(100))
                .build();

        final ItemResponseDto itemDto2 = ItemResponseDto.builder()
                .id(STANDARD_ID + 1L)
                .name("Test Item 2")
                .price(BigDecimal.valueOf(50))
                .build();

        final OrderItemCreateDto itemCreate1 = OrderItemCreateDto.builder()
                .itemId(STANDARD_ID)
                .quantity(STANDARD_QUANTITY)
                .build();

        final OrderItemCreateDto itemCreate2 = OrderItemCreateDto.builder()
                .itemId(STANDARD_ID + 1L)
                .quantity(3L)
                .build();

        final OrderItemResponseDto itemResponse1 = OrderItemResponseDto.builder()
                .id(STANDARD_ITEM_ID)
                .itemDto(itemDto1)
                .quantity(STANDARD_QUANTITY)
                .build();

        final OrderItemResponseDto itemResponse2 = OrderItemResponseDto.builder()
                .id(STANDARD_ITEM_ID + 1L)
                .itemDto(itemDto2)
                .quantity(STANDARD_QUANTITY + 1L)
                .build();

        final OrderItemUpdateDto updateItem = OrderItemUpdateDto.builder()
                .id(STANDARD_ITEM_ID)
                .orderId(STANDARD_ID)
                .itemId(STANDARD_ID)
                .quantity(STANDARD_QUANTITY + 3L)
                .build();

        createDto = OrderCreateDto.builder()
                .userId(USER_ID)
                .status(OrderStatus.CREATED)
                .orderDate(LocalDateTime.now().minusDays(1))
                .orderItems(List.of(itemCreate1, itemCreate2))
                .build();

        updateDto = OrderUpdateDto.builder()
                .id(STANDARD_ID)
                .userId(USER_ID)
                .status(OrderStatus.PROCESSING)
                .orderDate(LocalDateTime.now().minusDays(2))
                .itemsToAdd(List.of(itemCreate2))
                .itemsToUpdate(List.of(updateItem))
                .build();

        responseDto = OrderResponseDto.builder()
                .id(STANDARD_ID)
                .userId(USER_ID)
                .status(OrderStatus.CREATED)
                .orderDate(LocalDateTime.now().minusDays(1))
                .orderItems(List.of(itemResponse1, itemResponse2))
                .build();
    }

    @Test
    void shouldCreateOrder() throws Exception {
        when(orderServiceImpl.createOrder(any()))
                .thenReturn(responseDto);

        mockMvc.perform(post("/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.userId").value(responseDto.getUserId()))
                .andExpect(jsonPath("$.orderItems.length()").value(2));
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        when(orderServiceImpl.updateOrder(any()))
                .thenReturn(responseDto);

        mockMvc.perform(put("/order/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(responseDto.getStatus().name()));
    }

    @Test
    @DisplayName("Create Order - Success")
    void testCreateOrder() throws Exception {

        when(orderServiceImpl.createOrder(any(OrderCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.id").value(STANDARD_ID))
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(2));
    }

    @Test
    @DisplayName("Get Order By Id - Success")
    void testGetOrderById() throws Exception {

        when(orderServiceImpl.getOrderById(STANDARD_ID)).thenReturn(responseDto);

        mockMvc.perform(get("/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(STANDARD_ID))
                .andExpect(jsonPath("$.userId").value(USER_ID));
    }

    @Test
    @DisplayName("Update Order - Replace Items")
    void testUpdateOrderReplaceItems() throws Exception {

        responseDto.setStatus(OrderStatus.PROCESSING);

        when(orderServiceImpl.updateOrder(any(OrderUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/order/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }


    @Test
    @DisplayName("Create Order - Bad Request When Missing Fields")
    void testCreateOrderValidationFail() throws Exception {
        OrderCreateDto invalidDto = OrderCreateDto.builder()
                .userId(null)
                .status(null)
                .orderDate(null)
                .orderItems(List.of())
                .build();

        mockMvc.perform(post("/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get Order By Id - Not Found")
    void testGetOrderByIdNotFound() throws Exception {
        when(orderServiceImpl.getOrderById(99L)).thenThrow(new RuntimeException("Order not found"));

        mockMvc.perform(get("/order/99"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.details.message").value("Order not found"));
    }

    @Test
    @DisplayName("Update Order - Append Items")
    void testUpdateOrderAppendItems() throws Exception {
        OrderUpdateDto updateDto = OrderUpdateDto.builder()
                .id(STANDARD_ID)
                .userId(STANDARD_ID)
                .status(OrderStatus.CREATED)
                .orderDate(LocalDateTime.now().minusDays(1))
                .itemsToAdd(List.of(OrderItemCreateDto.builder().itemId(123L).quantity(STANDARD_ID).build()))
                .build();


        final ItemResponseDto itemResponseDto = ItemResponseDto
                .builder()
                .id(123L)
                .name("Item 2")
                .price(BigDecimal.valueOf(20))
                .build();

        final ArrayList<OrderItemResponseDto> orderItems = new ArrayList<>(responseDto.getOrderItems());
        orderItems.addLast(
                OrderItemResponseDto.builder()
                .id(STANDARD_ID + 1L)
                .itemDto(itemResponseDto)
                .quantity(STANDARD_ID)
                .build()
        );
        responseDto.setOrderItems(orderItems);

        when(orderServiceImpl.updateOrder(any(OrderUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/order/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItems.length()").value(3));
    }

    @Test
    @DisplayName("Delete Order - Not Found")
    void testDeleteOrderNotFound() throws Exception {
        mockMvc.perform(delete("/order/999"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Create Order - Invalid Item Quantity")
    void testCreateOrderInvalidItemQuantity() throws Exception {

        createDto.getOrderItems().getFirst().setQuantity(null);
        mockMvc.perform(post("/order/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

}

