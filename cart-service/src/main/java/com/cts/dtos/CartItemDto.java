package com.cts.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CartItemDto {
    private Long id;
    private Long itemId;
    private String itemName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subTotal;
}