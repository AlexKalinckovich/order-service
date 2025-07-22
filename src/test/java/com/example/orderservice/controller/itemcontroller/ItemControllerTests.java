package com.example.orderservice.controller.itemcontroller;

import com.example.orderservice.config.MessageConfig;
import com.example.orderservice.controller.item.ItemController;
import com.example.orderservice.dto.item.ItemCreateDto;
import com.example.orderservice.dto.item.ItemResponseDto;
import com.example.orderservice.dto.item.ItemUpdateDto;
import com.example.orderservice.exception.GlobalExceptionHandler;
import com.example.orderservice.service.exception.ExceptionResponseService;
import com.example.orderservice.service.item.ItemService;
import com.example.orderservice.service.message.MessageService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
@Import({MessageService.class, ExceptionResponseService.class, MessageConfig.class, GlobalExceptionHandler.class})
class ItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final Long ID = 1L;
    private static final String CREATE_ITEM_NAME = "itemName";
    private static final String UPDATE_ITEM_NAME = "otherName";
    private static final BigDecimal PRICE = BigDecimal.valueOf(11.00);
    private static final BigDecimal UPDATED_PRICE = BigDecimal.valueOf(20.00);

    private ItemCreateDto createDto;
    private ItemUpdateDto updateDto;
    private ItemResponseDto responseDto;

    @BeforeEach
    void setup() {
        createDto = ItemCreateDto.builder()
                .name(CREATE_ITEM_NAME)
                .price(PRICE)
                .build();

        updateDto = ItemUpdateDto.builder()
                .id(ID)
                .name(UPDATE_ITEM_NAME)
                .price(UPDATED_PRICE)
                .build();

        responseDto = ItemResponseDto.builder()
                .id(ID)
                .name(CREATE_ITEM_NAME)
                .price(PRICE)
                .build();
    }

    @Test
    @DisplayName("POST /item/create -> success")
    void testCreate_Success() throws Exception {
        when(itemService.createItem(any())).thenReturn(responseDto);

        mockMvc.perform(post("/item/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.name").value(CREATE_ITEM_NAME))
                .andExpect(jsonPath("$.price").value(PRICE.doubleValue()));
    }

    @Test
    @DisplayName("POST /item/create -> validation failure (blank name)")
    void testCreate_NameBlank() throws Exception {
        createDto.setName("");
        mockMvc.perform(post("/item/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"));
    }

    @Test
    @DisplayName("POST /item/create -> validation failure (price null)")
    void testCreate_PriceNull() throws Exception {
        createDto.setPrice(null);
        mockMvc.perform(post("/item/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"));
    }

    @Test
    @DisplayName("PUT /item/update -> success")
    void testUpdate_Success() throws Exception {
        when(itemService.updateItem(any())).thenReturn(responseDto);

        mockMvc.perform(put("/item/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.name").value(CREATE_ITEM_NAME));
    }

    @Test
    @DisplayName("PUT /item/update -> validation failure (blank name)")
    void testUpdate_NameBlank() throws Exception {
        updateDto.setName("");
        mockMvc.perform(put("/item/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"));
    }

    @Test
    @DisplayName("DELETE /item/{id} -> success")
    void testDelete_Success() throws Exception {
        when(itemService.deleteItem(ID)).thenReturn(responseDto);

        mockMvc.perform(delete("/item/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID));
    }

    @Test
    @DisplayName("DELETE /item/{id} -> not found")
    void testDelete_NotFound() throws Exception {
        when(itemService.deleteItem(ID)).thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(delete("/item/{id}", ID))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Server error"))
                .andExpect(jsonPath("$.details.Errors").value("Not found"));
    }

    @Test
    @DisplayName("GET /item/{id} -> success")
    void testGetById_Success() throws Exception {
        when(itemService.getItemById(ID)).thenReturn(responseDto);

        mockMvc.perform(get("/item/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(CREATE_ITEM_NAME));
    }

    @Test
    @DisplayName("GET /item/{id} -> not found")
    void testGetById_NotFound() throws Exception {
        when(itemService.getItemById(ID)).thenThrow(new RuntimeException("Missing"));

        mockMvc.perform(get("/item/{id}", ID))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Server error"))
                .andExpect(jsonPath("$.details.Errors").value("Missing"));
    }

    @Test
    @DisplayName("GET /item/list/{ids} -> success")
    void testGetItemsByIds() throws Exception {
        List<ItemResponseDto> items = List.of(
                responseDto,
                ItemResponseDto.builder()
                        .id(2L).name("Another").price(BigDecimal.valueOf(15)).build()
        );
        when(itemService.getAllItemsByIds(List.of(1L,2L))).thenReturn(items);

        mockMvc.perform(get("/item/list/{ids}", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /item/list/{ids} -> empty list")
    void testGetItemsByIds_Empty() throws Exception {
        when(itemService.getAllItemsByIds(List.of(3L))).thenReturn(List.of());

        mockMvc.perform(get("/item/list/{ids}", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

