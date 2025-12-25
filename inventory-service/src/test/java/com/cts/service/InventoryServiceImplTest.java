package com.cts.service;

import com.cts.dtos.InventoryDto;
import com.cts.entities.Inventory;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ItemValidationService itemValidationService;

    @Mock
    private AuditService auditService;

    @Mock
    private NotificationSenderService notificationSenderService;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory inventory1;
    private InventoryDto inventoryDto1;

    @BeforeEach
    void init() {
        inventory1 = new Inventory(1L, 101L, 50, "A1-North", LocalDateTime.now());
        inventoryDto1 = new InventoryDto(1L, 101L, 50, "A1-North", inventory1.getLastUpdated());
    }

    @Test
    void addInventory() {
        doNothing().when(itemValidationService).validateItemId(anyLong());
        when(modelMapper.map(any(InventoryDto.class), eq(Inventory.class))).thenReturn(inventory1);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory1);
        when(modelMapper.map(any(Inventory.class), eq(InventoryDto.class))).thenReturn(inventoryDto1);
        doNothing().when(auditService).logEvent(anyString(), anyLong(), anyString());

        InventoryDto createdInventory = inventoryService.addInventory(inventoryDto1);

        assertNotNull(createdInventory);
        assertEquals(101L, createdInventory.getItemId());
        verify(inventoryRepository).save(any(Inventory.class));
        verify(itemValidationService).validateItemId(101L);
        verify(auditService).logEvent(eq("CREATE"), eq(1L), anyString());
    }

    @Test
    void getInventoryById() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory1));
        when(modelMapper.map(any(Inventory.class), eq(InventoryDto.class))).thenReturn(inventoryDto1);

        var result = inventoryService.getInventoryById(1L);

        assertNotNull(result);
        assertEquals(50, result.getQuantity());
        verify(inventoryRepository).findById(1L);
    }

    @Test
    void getInventoryById_NotFound() {
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.getInventoryById(99L));
        verify(inventoryRepository).findById(99L);      //verify that the service tried to find the item
    }

    @Test
    void getAllInventory() {
        when(inventoryRepository.findAll()).thenReturn(List.of(inventory1));
        when(modelMapper.map(any(Inventory.class), eq(InventoryDto.class))).thenReturn(inventoryDto1);

        var results = inventoryService.getAllInventory();

        assertEquals(1, results.size());
        verify(inventoryRepository).findAll();
    }

    @Test
    void updateInventory() {
        doNothing().when(itemValidationService).validateItemId(anyLong());
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory1));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory1);
        when(modelMapper.map(any(Inventory.class), eq(InventoryDto.class))).thenReturn(inventoryDto1);
        doNothing().when(auditService).logEvent(anyString(), anyLong(), anyString());

        InventoryDto updatedInventory = inventoryService.updateInventory(1L, inventoryDto1);

        assertNotNull(updatedInventory);
        assertEquals("A1-North", updatedInventory.getWarehouseLocation());
        verify(inventoryRepository).findById(1L);
        verify(inventoryRepository).save(any(Inventory.class));
        verify(itemValidationService).validateItemId(101L);
        verify(auditService).logEvent(eq("UPDATE"), eq(1L), anyString());
    }

    @Test
    void updateInventoryByItemId() {
        // Setting up mock behavior
        doNothing().when(itemValidationService).validateItemId(anyLong());
        when(inventoryRepository.findByItemId(101L)).thenReturn(Optional.of(inventory1));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory1);
        when(modelMapper.map(any(Inventory.class), eq(InventoryDto.class))).thenReturn(inventoryDto1);
        doNothing().when(auditService).logEvent(anyString(), anyLong(), anyString());


        InventoryDto updatedInventory = inventoryService.updateInventoryByItemId(101L, inventoryDto1);

        assertNotNull(updatedInventory);
        assertEquals(101L, updatedInventory.getItemId());
        verify(inventoryRepository).findByItemId(101L);
        verify(inventoryRepository).save(any(Inventory.class));
        verify(itemValidationService).validateItemId(101L);
        verify(auditService).logEvent(eq("UPDATE"), eq(1L), anyString());
    }

    @Test
    void updateInventory_LowStock() {
        inventory1.setQuantity(5);
        inventoryDto1.setQuantity(5);
        doNothing().when(itemValidationService).validateItemId(anyLong());
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory1));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory1);
        when(modelMapper.map(any(Inventory.class), eq(InventoryDto.class))).thenReturn(inventoryDto1);
        doNothing().when(auditService).logEvent(anyString(), anyLong(), anyString());
        doNothing().when(notificationSenderService).sendLowStockNotification(anyLong());

        inventoryService.updateInventory(1L, inventoryDto1);

        verify(notificationSenderService).sendLowStockNotification(101L);
    }

    @Test
    void deleteInventory() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory1));
        doNothing().when(inventoryRepository).delete(any(Inventory.class));
        doNothing().when(auditService).logEvent(anyString(), anyLong(), anyString());

        var result = inventoryService.deleteInventory(1L);

        assertEquals("Inventory with ID 1 deleted successfully.", result);
        verify(inventoryRepository).findById(1L);    // Ensure findById was called
        verify(inventoryRepository).delete(inventory1);     // Ensure delete was called with inventory1
        verify(auditService).logEvent(eq("DELETE"), eq(1L), anyString());
    }

    @Test
    void deleteInventoryByItemId() {
        // Setting up the mock behavior
        when(inventoryRepository.findByItemId(101L)).thenReturn(Optional.of(inventory1));    // Mocking repository to find the inventory item
        doNothing().when(inventoryRepository).deleteByItemId(anyLong());    // Mocking repository's delete method to do nothing
        doNothing().when(auditService).logEvent(anyString(), anyLong(), anyString());    // Mocking audit service to do nothing

        String result = inventoryService.deleteInventoryByItemId(101L);

        // Checks results and verify mock interactions
        assertEquals("Inventory with Item ID 101 deleted successfully.", result);
        verify(inventoryRepository).findByItemId(101L);
        verify(inventoryRepository).deleteByItemId(101L);
        verify(auditService).logEvent(eq("DELETE"), eq(1L), anyString());
    }

    @Test
    void deleteInventoryByItemId_NotFound() {
        when(inventoryRepository.findByItemId(99L)).thenReturn(Optional.empty());    // Mocking repository to return an empty Optional

        // Verify that a ResourceNotFoundException is thrown
        assertThrows(ResourceNotFoundException.class, () -> {
            inventoryService.deleteInventoryByItemId(99L);
        });

        verify(inventoryRepository, never()).deleteByItemId(anyLong()); // Verify that the delete method was never called
    }
}