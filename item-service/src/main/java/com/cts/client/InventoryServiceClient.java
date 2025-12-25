package com.cts.client;

import com.cts.dtos.InventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service", url = "${api.gateway.url}")
public interface InventoryServiceClient {

    @PostMapping("/api/inventory")
    InventoryDto addInventory(@RequestBody InventoryDto inventoryDto);

    @DeleteMapping("/api/inventory/item/{itemId}")
    void deleteInventoryByItemId(@PathVariable("itemId") Long itemId);
}