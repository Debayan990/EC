package com.cts.service;

import com.cts.client.InventoryServiceClient;
import com.cts.dtos.InventoryDto;
import com.cts.dtos.ItemDto;
import com.cts.dtos.ItemInputDto;
import com.cts.entities.Item;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ModelMapper modelMapper;
    private final AuditService auditService;    // Inject the AuditService
    private final InventoryServiceClient inventoryServiceClient; // Inject the InventoryService

    @Override
    public ItemDto createItem(ItemInputDto itemDto) {
        Item item = modelMapper.map(itemDto, Item.class);
        item.setCreatedAt(LocalDateTime.now());
        Item savedItem = itemRepository.save(item);

        if (itemDto.getQuantity() != null && itemDto.getWarehouseLocation() != null) {
            try {
                // Map details to InventoryDto
                InventoryDto inventoryDto = new InventoryDto();
                inventoryDto.setItemId(savedItem.getId());
                inventoryDto.setQuantity(itemDto.getQuantity());
                inventoryDto.setWarehouseLocation(itemDto.getWarehouseLocation());

                // Call Inventory Service
                inventoryServiceClient.addInventory(inventoryDto);

            } catch (Exception e) {
                log.warn("Failed to add inventory details for item ID: {}. Error: {}", savedItem.getId(), e.getMessage());
            }
        }

        // Log in Audit
        auditService.logEvent("CREATE", savedItem.getId(), "Item created: " + savedItem.getName());

        return modelMapper.map(savedItem, ItemDto.class);
    }

    @Override
    public ItemDto getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        return modelMapper.map(item, ItemDto.class);
    }

    @Override
    public List<ItemDto> getAllItems() {
        var items = itemRepository.findAll();
        return items.stream()
                .map(item -> modelMapper.map(item, ItemDto.class))
                .toList();
    }

    @Override
    public List<ItemDto> findItemsByCategory(String category) {
        return itemRepository.findByCategoryIgnoreCase(category).stream()
                .map(product -> modelMapper.map(product, ItemDto.class))
                .toList();
    }

    @Override
    public ItemDto updateItem(Long id, ItemDto itemDto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setCategory(itemDto.getCategory());
        item.setPrice(itemDto.getPrice());

        Item updatedItem = itemRepository.save(item);

        // Log in Audit
        auditService.logEvent("UPDATE", updatedItem.getId(), "Item updated: " + updatedItem.getName());

        return modelMapper.map(updatedItem, ItemDto.class);
    }

    @Override
    public String deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));
        itemRepository.delete(item);

        // Log in Audit
        auditService.logEvent("DELETE", id, "Item with ID " + id + " deleted.");

        // Delete the corresponding inventory of that item id
        try{
            inventoryServiceClient.deleteInventoryByItemId(id);

        } catch (Exception e) {
            log.warn("Failed to delete inventory for item ID: {}. Error: {}", id, e.getMessage());
        }

        return "Item with ID " + id + " deleted successfully.";
    }
}
