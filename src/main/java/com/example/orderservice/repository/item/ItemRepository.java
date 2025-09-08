package com.example.orderservice.repository.item;


import com.example.orderservice.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE i.id IN :ids")
    List<Item> findExistingItems(@Param("ids") List<Long> ids);

    @Query("SELECT i.id from Item i where i.id in :ids")
    List<Long> findExistingItemsIds(@Param("ids") Set<Long> ids);

    boolean existsByName(String name);

}
