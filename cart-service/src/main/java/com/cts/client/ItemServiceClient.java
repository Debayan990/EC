package com.cts.client;

import com.cts.dtos.ItemDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "api-gateway")
public interface ItemServiceClient {
    @GetMapping("/api/items/{id}")
    ItemDto getItemById(@PathVariable("id") Long id);
}
