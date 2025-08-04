package com.example.orderservice.config;

import com.example.orderservice.model.Item;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.item.ItemRepository;
import com.example.orderservice.repository.order.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

@Profile("dev")
@Configuration
public class DatabaseInitializer {

    @Bean
    public CommandLineRunner seedDatabase(
            ItemRepository itemRepo,
            OrderRepository orderRepo
    ) {
        return args -> {
            if (itemRepo.count() == 0) {
                final Item premium = itemRepo.save(Item.builder()
                        .name("Premium Widget")
                        .price(new BigDecimal("19.99"))
                        .build());

                final Item standard = itemRepo.save(Item.builder()
                        .name("Standard Widget")
                        .price(new BigDecimal("9.99"))
                        .build());

                final Order order = orderRepo.save(Order.builder()
                        .userId(1001L)
                        .status(OrderStatus.CREATED)
                        .orderDate(LocalDateTime.now())
                        .build());

                final OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .item(premium)
                        .quantity(2L)
                        .build();

                order.setOrderItems(Collections.singletonList(orderItem));
                orderRepo.save(order);
            }
        };
    }
}
