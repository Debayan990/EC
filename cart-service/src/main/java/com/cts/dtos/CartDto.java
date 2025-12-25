package com.cts.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CartDto {
    private Long id;
    private String username;
    private BigDecimal totalPrice;
    private List<CartItemDto> items;
}