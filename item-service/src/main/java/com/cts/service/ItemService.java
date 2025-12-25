package com.cts.service;

import com.cts.dtos.ItemDto;
import com.cts.dtos.ItemInputDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemInputDto itemDto);
    ItemDto getItemById(Long id);
    List<ItemDto> getAllItems();
    List<ItemDto> findItemsByCategory(String category);
    ItemDto updateItem(Long id, ItemDto itemDto);
    String deleteItem(Long id);
}
