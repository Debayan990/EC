package com.cts.service;

import com.cts.dtos.InventoryDto;

import java.util.List;

public interface InventoryService {
    InventoryDto addInventory(InventoryDto inventoryDto);
    InventoryDto getInventoryById(Long id);
    InventoryDto getInventoryByItemId(Long itemId);
    List<InventoryDto> getAllInventory();
    InventoryDto updateInventory(Long id, InventoryDto inventoryDto);
    InventoryDto updateInventoryByItemId(Long itemId, InventoryDto inventoryDto);
    String deleteInventory(Long id);
    String deleteInventoryByItemId(Long itemId);


}
