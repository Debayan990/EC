package com.cts.controllers;

import com.cts.dtos.ItemDto;
import com.cts.dtos.ItemInputDto;
import com.cts.dtos.SuccessDto;
import com.cts.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@Valid @RequestBody ItemInputDto itemDto) {
        return new ResponseEntity<>(itemService.createItem(itemDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    //Uses namedParameter: api/items/search/category?category=Computer
    @GetMapping("/search/category")
    public ResponseEntity<List<ItemDto>> searchByCategory(@RequestParam String category) {
        return ResponseEntity.ok(itemService.findItemsByCategory(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemDto> updateItem(@PathVariable Long id, @Valid @RequestBody ItemDto itemDto) {
        return ResponseEntity.ok(itemService.updateItem(id, itemDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessDto> deleteItem(@PathVariable Long id) {
        SuccessDto result =new SuccessDto(itemService.deleteItem(id));
        return ResponseEntity.ok(result);
    }
}
