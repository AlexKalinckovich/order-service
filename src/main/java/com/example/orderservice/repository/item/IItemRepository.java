package com.example.orderservice.repository.item;


import com.example.orderservice.model.Item;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE i.id IN :ids")
    List<Item> findExistingItems(@Param("ids") List<Long> ids);

    @Query("SELECT i.id from Item i where i.id in :ids")
    List<Long> findExistingItemsIds(@Param("ids") List<Long> ids);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long excludeId);

    @EntityGraph(attributePaths = "orderItems")
    List<Item> findByIdIn(List<Long> ids);
}
