package com.cts.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDto {
    private Long id;
    private Long itemId;
    private Integer quantity;
    private String warehouseLocation;
    private LocalDateTime lastUpdated;
}