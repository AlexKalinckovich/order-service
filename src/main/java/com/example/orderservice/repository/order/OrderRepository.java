package com.example.orderservice.repository.order;

import com.example.orderservice.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "orderItems")
    @Query(
            "SELECT o FROM Order o WHERE o.id = :id"
    )
    Optional<Order> findWithOrderItemsById(@Param("id") final Long id);

}
