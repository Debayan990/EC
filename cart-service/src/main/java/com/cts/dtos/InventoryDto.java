package com.cts.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InventoryDto {
    private Long id;
    private Long itemId;
    private Integer quantity;
    private String warehouseLocation;
}