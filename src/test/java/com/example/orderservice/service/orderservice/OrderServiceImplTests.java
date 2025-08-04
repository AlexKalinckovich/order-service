package com.example.orderservice.service.orderservice;

import com.example.orderservice.client.UserServiceClient;
import com.example.orderservice.dto.order.ItemAddedType;
import com.example.orderservice.dto.order.OrderCreateDto;
import com.example.orderservice.dto.order.OrderResponseDto;
import com.example.orderservice.dto.order.OrderUpdateDto;
import com.example.orderservice.dto.orderItem.OrderItemCreateDto;
import com.example.orderservice.dto.orderItem.OrderItemUpdateDto;
import com.example.orderservice.exception.order.OrderNotFoundException;
import com.example.orderservice.exception.orderItem.OrderItemNotFoundException;
import com.example.orderservice.exception.user.UserNotFoundException;
import com.example.orderservice.mapper.order.OrderMapper;
import com.example.orderservice.model.Item;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.item.ItemRepository;
import com.example.orderservice.repository.order.OrderRepository;
import com.example.orderservice.service.order.OrderServiceImpl;
import com.example.orderservice.model.Order;
import com.example.orderservice.validators.order.OrderValidator;
import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest(properties = "user.service.url=http://localhost:8080")
@EnableWireMock({
        @ConfigureWireMock(name = "user-service", port = 8080)
})
class OrderServiceImplTests {

    @InjectWireMock("user-service")
    private WireMockServer wireMockServer;

    @Autowired
    private OrderMapper orderMapper;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private OrderValidator orderValidator;

    @Autowired
    private OrderServiceImpl orderServiceImpl;

    private OrderCreateDto createSampleOrderCreateDto() {
        return OrderCreateDto.builder()
                .userId(USER_ID)
                .status(OrderStatus.CREATED)
                .orderItems(List.of(
                        OrderItemCreateDto.builder().itemId(ITEM_ID_1).quantity(2L).build(),
                        OrderItemCreateDto.builder().itemId(ITEM_ID_2).quantity(1L).build()
                ))
                .build();
    }

    private OrderUpdateDto createSampleOrderUpdateDto(Long orderId) {
        return OrderUpdateDto.builder()
                .id(orderId)
                .userId(USER_ID)
                .status(OrderStatus.PROCESSING)
                .itemAddedType(ItemAddedType.APPEND)
                .orderItemsToAdd(List.of(
                        OrderItemCreateDto.builder().itemId(ITEM_ID_3).quantity(3L).build()
                ))
                .build();
    }

    private Order createSampleOrderEntity(final Long id) {
        return Order.builder()
                .id(id)
                .userId(USER_ID)
                .status(OrderStatus.CREATED)
                .orderDate(LocalDateTime.now())
                .orderItems(new ArrayList<>())
                .build();
    }

    private Item createSampleItemEntity(final Long id) {
        return Item.builder()
                .id(id)
                .name("Item " + id)
                .price(BigDecimal.valueOf(id * 10))
                .build();
    }

    private OrderResponseDto createSampleOrderResponseDto(final Long id) {
        return OrderResponseDto.builder()
                .id(id)
                .userId(USER_ID)
                .status(OrderStatus.CREATED)
                .orderDate(LocalDateTime.now())
                .build();
    }

    protected final Long ORDER_ID = 1L;
    protected final Long USER_ID = 1L;
    protected final Long ITEM_ID_1 = 101L;
    protected final Long ITEM_ID_2 = 102L;
    protected final Long ITEM_ID_3 = 103L;
    protected final OrderCreateDto SAMPLE_CREATE_DTO = createSampleOrderCreateDto();
    protected final OrderUpdateDto SAMPLE_UPDATE_DTO = createSampleOrderUpdateDto(ORDER_ID);
    protected final Order SAMPLE_ORDER = createSampleOrderEntity(ORDER_ID);
    protected final OrderResponseDto SAMPLE_RESPONSE_DTO = createSampleOrderResponseDto(ORDER_ID);
    protected final Item SAMPLE_ITEM_1 = createSampleItemEntity(ITEM_ID_1);
    protected final Item SAMPLE_ITEM_2 = createSampleItemEntity(ITEM_ID_2);
    protected final Item SAMPLE_ITEM_3 = createSampleItemEntity(ITEM_ID_3);

    @Value("${user.service.url}")
    private String userServiceUrl;

    @BeforeEach
    void setUp() {
        when(itemRepository.getReferenceById(ITEM_ID_1)).thenReturn(SAMPLE_ITEM_1);
        when(itemRepository.getReferenceById(ITEM_ID_2)).thenReturn(SAMPLE_ITEM_2);
        when(itemRepository.getReferenceById(ITEM_ID_3)).thenReturn(SAMPLE_ITEM_3);

        when(orderValidator.checkOrderToExistence(ORDER_ID)).thenReturn(SAMPLE_ORDER);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }


    @Test
    void whenCreatingOrderWithInvalidUserId_thenThrowsUserNotFoundException() {
        final long invalidUserId = SAMPLE_CREATE_DTO.getUserId();
        final String testUrl = "/user/" + invalidUserId;
        wireMockServer.stubFor(get(urlEqualTo(testUrl))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"User not found\"}")));

        final UserServiceClient client = new UserServiceClient(userServiceUrl);
        assertThrows(UserNotFoundException.class, () -> client.validateUserExists(invalidUserId));
    }


    @Test
    @Transactional
    void createOrder_ValidInput_ShouldCreateAndReturnOrder() {
        // Given
        final OrderCreateDto createDto = OrderCreateDto.builder()
                .userId(USER_ID)
                .status(OrderStatus.CREATED)
                .orderItems(List.of(
                        OrderItemCreateDto.builder().itemId(ITEM_ID_1).quantity(2L).build(),
                        OrderItemCreateDto.builder().itemId(ITEM_ID_2).quantity(1L).build()
                ))
                .build();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(ORDER_ID); // Simulate ID assignment
            return savedOrder;
        });

        // When
        OrderResponseDto result = orderServiceImpl.createOrder(createDto);

        // Then
        assertNotNull(result);
        assertEquals(ORDER_ID, result.getId());
        assertEquals(USER_ID, result.getUserId());
        assertEquals(OrderStatus.CREATED, result.getStatus());

        final ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        final Order savedOrder = orderCaptor.getValue();

        assertEquals(2, savedOrder.getOrderItems().size());
        assertTrue(savedOrder.getOrderItems().stream()
                .anyMatch(item -> item.getItem().getId().equals(ITEM_ID_1) &&
                        item.getQuantity() == 2L));
        assertTrue(savedOrder.getOrderItems().stream()
                .anyMatch(item -> item.getItem().getId().equals(ITEM_ID_2) &&
                        item.getQuantity() == 1L));
    }

    @Test
    @Transactional
    void createOrder_UserDoesNotExist_ShouldThrowException() {
        // Given
        final Long invalidUserId = 999L;
        final OrderCreateDto createDto = OrderCreateDto.builder()
                .userId(invalidUserId)
                .status(OrderStatus.CREATED)
                .orderItems(List.of(
                        OrderItemCreateDto.builder().itemId(ITEM_ID_1).quantity(2L).build()
                ))
                .build();

        doThrow(new UserNotFoundException(invalidUserId))
                .when(orderValidator).validateCreateDto(createDto);

        // When & Then
        assertThrows(UserNotFoundException.class, () ->
                orderServiceImpl.createOrder(createDto));
    }

    @Test
    @Transactional
    void createOrder_UserServiceUnavailable_ShouldThrowException() {
        // Given
        final OrderCreateDto createDto = createSampleOrderCreateDto();

        doThrow(new WebClientResponseException("Service unavailable", 503, "Service Unavailable",
                null, null, null))
                .when(orderValidator).validateCreateDto(createDto);

        // When & Then
        assertThrows(WebClientResponseException.class, () ->
                orderServiceImpl.createOrder(createDto));
    }

    @Test
    void getAllOrdersByIds_ValidIds_ShouldReturnOrders() {
        // Given
        final List<Long> orderIds = List.of(1L, 2L, 3L);

        final Order order1 = createSampleOrderEntity(1L);
        final Order order2 = createSampleOrderEntity(2L);
        final Order order3 = createSampleOrderEntity(3L);

        when(orderValidator.checkOrdersToExistence(orderIds))
                .thenReturn(List.of(order1, order2, order3));

        // When
        final List<OrderResponseDto> result = orderServiceImpl.getAllOrdersByIds(orderIds);

        // Then
        assertEquals(3, result.size());
        assertThat(result.stream().map(OrderResponseDto::getId).collect(Collectors.toList()))
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    void getOrderById_ExistingId_ShouldReturnOrder() {
        // Given
        final Long orderId = 5L;
        final Order expectedOrder = createSampleOrderEntity(orderId);
        final OrderResponseDto expectedDto = orderMapper.toResponseDto(expectedOrder);

        when(orderValidator.checkOrderToExistence(orderId)).thenReturn(expectedOrder);

        // When
        final OrderResponseDto result = orderServiceImpl.getOrderById(orderId);

        // Then
        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
        assertEquals(expectedDto.getUserId(), result.getUserId());
        assertEquals(expectedDto.getStatus(), result.getStatus());
        assertEquals(expectedDto.getOrderDate(), result.getOrderDate());
    }

    @Test
    void getOrderById_NonExistingId_ShouldThrowException() {
        // Given
        Long invalidId = 999L;
        when(orderValidator.checkOrderToExistence(invalidId))
                .thenThrow(new OrderNotFoundException(invalidId));

        // When & Then
        assertThrows(OrderNotFoundException.class, () ->
                orderServiceImpl.getOrderById(invalidId));
    }

    @Test
    @Transactional
    void createOrder_WithPastDate_ShouldUseProvidedDate() {
        // Given
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        OrderCreateDto createDto = OrderCreateDto.builder()
                .userId(USER_ID)
                .status(OrderStatus.CREATED)
                .orderDate(pastDate)
                .orderItems(List.of())
                .build();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(ORDER_ID);
            return savedOrder;
        });

        // When
        OrderResponseDto result = orderServiceImpl.createOrder(createDto);

        // Then
        assertEquals(pastDate, result.getOrderDate());
    }

    @Test
    @Transactional
    void createOrder_WithoutDate_ShouldUseCurrentDate() {
        // Given
        OrderCreateDto createDto = OrderCreateDto.builder()
                .userId(USER_ID)
                .status(OrderStatus.CREATED)
                .orderItems(List.of())
                .build();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(ORDER_ID);
            return savedOrder;
        });

        // When
        OrderResponseDto result = orderServiceImpl.createOrder(createDto);

        // Then
        assertNotNull(result.getOrderDate());
        assertTrue(result.getOrderDate().isBefore(LocalDateTime.now().plusSeconds(1)) &&
                result.getOrderDate().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    @Transactional
    void updateOrder_AppendItems_ShouldAddNewItems() {
        // Given
        Order existingOrder = createSampleOrderEntity(ORDER_ID);
        existingOrder.setOrderItems(new ArrayList<>(List.of(
                createOrderItem(1L, SAMPLE_ITEM_1, 2L)
        )));

        OrderUpdateDto updateDto = OrderUpdateDto.builder()
                .id(ORDER_ID)
                .status(OrderStatus.PROCESSING)
                .itemAddedType(ItemAddedType.APPEND)
                .orderItemsToAdd(List.of(
                        OrderItemCreateDto.builder().itemId(ITEM_ID_2).quantity(3L).build()
                ))
                .build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));

        // When
        orderServiceImpl.updateOrder(updateDto);

        // Then
        assertEquals(2, existingOrder.getOrderItems().size());
        assertTrue(existingOrder.getOrderItems().stream()
                .anyMatch(item -> item.getItem().getId().equals(ITEM_ID_1) && item.getQuantity() == 2L));
        assertTrue(existingOrder.getOrderItems().stream()
                .anyMatch(item -> item.getItem().getId().equals(ITEM_ID_2) && item.getQuantity() == 3L));
        verify(orderRepository).saveAndFlush(existingOrder);
    }

    @Test
    @Transactional
    void updateOrder_ReplaceItems_ShouldClearAndAddNewItems() {
        // Given
        final Order existingOrder = createSampleOrderEntity(ORDER_ID);
        existingOrder.setOrderItems(new ArrayList<>(List.of(
                createOrderItem(1L, SAMPLE_ITEM_1, 2L),
                createOrderItem(2L, SAMPLE_ITEM_2, 1L)
        )));

        final OrderUpdateDto updateDto = OrderUpdateDto.builder()
                .id(ORDER_ID)
                .status(OrderStatus.PROCESSING)
                .itemAddedType(ItemAddedType.REPLACE)
                .orderItemsToAdd(List.of(
                        OrderItemCreateDto.builder().itemId(ITEM_ID_3).quantity(4L).build()
                ))
                .build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));

        // When
        orderServiceImpl.updateOrder(updateDto);

        // Then
        assertEquals(1, existingOrder.getOrderItems().size());
        assertEquals(ITEM_ID_3, existingOrder.getOrderItems().getFirst().getItem().getId());
        assertEquals(4L, existingOrder.getOrderItems().getFirst().getQuantity());
    }

    @Test
    @Transactional
    void updateOrder_UpdateItems_ShouldModifyExistingItems() {
        // Given
        final Order existingOrder = createSampleOrderEntity(ORDER_ID);
        existingOrder.setOrderItems(new ArrayList<>(List.of(
                createOrderItem(1L, SAMPLE_ITEM_1, 2L),
                createOrderItem(2L, SAMPLE_ITEM_2, 1L)
        )));

        final OrderUpdateDto updateDto = OrderUpdateDto.builder()
                .id(ORDER_ID)
                .status(OrderStatus.PROCESSING)
                .itemAddedType(ItemAddedType.UPDATE)
                .orderItemsToUpdate(List.of(
                        OrderItemUpdateDto.builder().id(1L).quantity(5L).build(),
                        OrderItemUpdateDto.builder().id(2L).quantity(3L).build()
                ))
                .build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));

        // When
        orderServiceImpl.updateOrder(updateDto);

        // Then
        final Map<Long, Long> quantities = existingOrder
                .getOrderItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, OrderItem::getQuantity));

        assertEquals(5L, quantities.get(1L));
        assertEquals(3L, quantities.get(2L));
    }

    @Test
    @Transactional
    void updateOrder_NotUpdateItems_ShouldOnlyUpdateOrderFields() {
        // Given
        final Order existingOrder = createSampleOrderEntity(ORDER_ID);
        existingOrder.setOrderItems(new ArrayList<>(List.of(
                createOrderItem(1L, SAMPLE_ITEM_1, 2L)
        )));

        final OrderUpdateDto updateDto = OrderUpdateDto.builder()
                .id(ORDER_ID)
                .userId(USER_ID + 1)
                .status(OrderStatus.COMPLETED)
                .itemAddedType(ItemAddedType.NOT_UPDATED)
                .build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));

        // When
        orderServiceImpl.updateOrder(updateDto);

        // Then
        assertEquals(OrderStatus.COMPLETED, existingOrder.getStatus());
        assertEquals(USER_ID + 1, existingOrder.getUserId());
        assertEquals(1, existingOrder.getOrderItems().size()); // Items unchanged
    }

    @Test
    @Transactional
    void updateOrder_InvalidItemUpdate_ShouldThrowException() {
        // Given
        final Order existingOrder = createSampleOrderEntity(ORDER_ID);
        existingOrder.setOrderItems(new ArrayList<>());

        final OrderUpdateDto updateDto = OrderUpdateDto.builder()
                .id(ORDER_ID)
                .itemAddedType(ItemAddedType.UPDATE)
                .orderItemsToUpdate(List.of(
                        OrderItemUpdateDto.builder().id(999L).quantity(5L).build()
                ))
                .build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));

        // When & Then
        assertThrows(OrderItemNotFoundException.class, () ->
                orderServiceImpl.updateOrder(updateDto));
    }

    @Test
    @Transactional
    void deleteOrder_ExistingOrder_ShouldDeleteAndReturnResponse() {
        // Given
        final Order existingOrder = createSampleOrderEntity(ORDER_ID);
        existingOrder.setOrderItems(List.of(
                createOrderItem(1L, SAMPLE_ITEM_1, 2L)
        ));

        when(orderRepository.findWithOrderItemsById(ORDER_ID)).thenReturn(Optional.of(existingOrder));
        orderMapper.toResponseDto(existingOrder);

        // When
        final OrderResponseDto result = orderServiceImpl.deleteOrder(ORDER_ID);

        // Then
        assertNotNull(result);
        assertEquals(ORDER_ID, result.getId());
        verify(orderRepository).delete(existingOrder);
    }

    @Test
    @Transactional
    void deleteOrder_NonExistingOrder_ShouldThrowException() {
        // Given
        final Long invalidId = 999L;
        when(orderRepository.findWithOrderItemsById(invalidId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class, () ->
                orderServiceImpl.deleteOrder(invalidId));
    }

    @Test
    @Transactional
    void updateOrder_UpdateWithNullItems_ShouldHandleGracefully() {
        // Given
        final Order existingOrder = createSampleOrderEntity(ORDER_ID);
        final OrderUpdateDto updateDto = OrderUpdateDto.builder()
                .id(ORDER_ID)
                .itemAddedType(ItemAddedType.APPEND)
                .orderItemsToAdd(null)
                .build();

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(existingOrder));
        doThrow(new ValidationException()).when(orderValidator).validateUpdateDto(updateDto);

        // Then
        assertThrows(ValidationException.class, () -> orderServiceImpl.updateOrder(updateDto));
    }

    private OrderItem createOrderItem(final Long id, final Item item, final Long quantity) {
        return OrderItem.builder()
                .id(id)
                .item(item)
                .quantity(quantity)
                .build();
    }

}

