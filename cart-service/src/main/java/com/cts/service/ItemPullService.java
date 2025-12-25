package com.cts.service;

import com.cts.client.ItemServiceClient;
import com.cts.dtos.ItemDto;
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
public class ItemPullService {
    private final ItemServiceClient itemServiceClient;

    @CircuitBreaker(name = "item-service", fallbackMethod = "getItemFallback")
    @Retry(name = "retry-service")
    public ItemDto getItemDetails(Long itemId) {
        log.info("Calling Item Service for itemId: {}", itemId);
        return itemServiceClient.getItemById(itemId);
    }

    // Fallback Method
    public ItemDto getItemFallback(Long itemId, Throwable th) {
        log.error("Item Service call failed for itemId: {}. Error: {}", itemId, th.getMessage());

        if (th instanceof FeignException.NotFound) {
            throw new ResourceNotFoundException("Item", "id", itemId);
        }
        throw new ServiceUnavailableException("Item Service is currently unavailable. Please try again later.");
    }
}
