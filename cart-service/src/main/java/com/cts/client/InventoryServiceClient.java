package com.cts.client;

import com.cts.dtos.InventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "api-gateway")
public interface InventoryServiceClient {

    @GetMapping("/api/inventory/item/{itemId}")
    InventoryDto getInventoryByItemId(@PathVariable("itemId") Long itemId);
}