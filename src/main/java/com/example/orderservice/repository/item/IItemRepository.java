package com.example.orderservice.repository.item;


import com.example.orderservice.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IItemRepository extends JpaRepository<Item, Long> {

    @Query(
            "select i.id from Item i where i.id in :ids"
    )
    List<Item> findExistingIds(@Param("ids") final List<Long> ids);


    @Query(
            "select EXISTS(i) from Item i where i.name = :name"
    )
    boolean existsByName(@Param("name") final String name);


}
