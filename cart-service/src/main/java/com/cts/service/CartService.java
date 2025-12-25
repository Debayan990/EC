package com.cts.service;

import com.cts.dtos.CartDto;

public interface CartService {
    CartDto addItemToCart(String username, Long itemId, int quantity);
    CartDto getCartByUsername(String username);
    CartDto removeItemFromCart(String username, Long itemId);
    String clearCart(String username);
}