package com.cts.service;

import com.cts.client.InventoryServiceClient;
import com.cts.dtos.InventoryDto;
import com.cts.exception.ResourceNotFoundException;
import com.cts.exception.ServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryStockPullService {

    private final InventoryServiceClient inventoryServiceClient;

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "getInventoryFallback")
    @Retry(name = "retry-service")
    public InventoryDto getInventoryStock(Long itemId) {
        log.info("Checking stock for itemId: {}", itemId);
        return inventoryServiceClient.getInventoryByItemId(itemId);
    }

    // Fallback Method: Must return InventoryDto
    public InventoryDto getInventoryFallback(Long itemId, Throwable th) {
        log.error("Inventory Service call failed for itemId: {}. Error: {}", itemId, th.getMessage());

        if (th instanceof FeignException.NotFound) {
            throw new ResourceNotFoundException("Inventory", "itemId", itemId);
        }

        throw new ServiceUnavailableException("Inventory Service is currently unavailable. Please try again later.");
    }
}