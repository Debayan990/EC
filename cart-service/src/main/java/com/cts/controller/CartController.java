package com.cts.controller;

import com.cts.dtos.CartDto;
import com.cts.dtos.SuccessDto;
import com.cts.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartDto> addToCart(
            @RequestHeader("X-User-Name") String username,
            @RequestParam Long itemId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.addItemToCart(username, itemId, quantity));
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(@RequestHeader("X-User-Name") String username) {
        return ResponseEntity.ok(cartService.getCartByUsername(username));
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<CartDto> removeItem(
            @RequestHeader("X-User-Name") String username,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(username, itemId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<SuccessDto> clearCart(@RequestHeader("X-User-Name") String username) {
        SuccessDto result =new SuccessDto(cartService.clearCart(username));
        return ResponseEntity.ok(result);
    }
}