package com.cts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cts.entities.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByCategoryIgnoreCase(String category);
}
