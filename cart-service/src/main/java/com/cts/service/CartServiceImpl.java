package com.cts.service;

import com.cts.client.ItemServiceClient;
import com.cts.dtos.CartDto;
import com.cts.dtos.InventoryDto;
import com.cts.dtos.ItemDto;
import com.cts.entities.Cart;
import com.cts.entities.CartItem;
import com.cts.exception.BadRequestException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ItemPullService itemPullService;           // For Name & Price
    private final InventoryStockPullService inventoryStockPullService; // For Stock Validation
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CartDto addItemToCart(String username, Long itemId, int requestedQuantity) {

        // Fetch Item Details
        ItemDto itemDto = itemPullService.getItemDetails(itemId);

        // Validate Stock
        InventoryDto inventoryDto = inventoryStockPullService.getInventoryStock(itemId);

        // Check availability
        if (inventoryDto.getQuantity() < requestedQuantity) {
            throw new BadRequestException("Out of Stock! Available quantity: " + inventoryDto.getQuantity());
        }

        // 3. Get or Create Cart
        Cart cart = cartRepository.findByUsername(username)
                .orElse(new Cart());

        if (cart.getUsername() == null) {
            cart.setUsername(username);
            cart.setItems(new ArrayList<>());
        }

        // Add/Update Item in Cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getItemId().equals(itemId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();

            // Check if (existing quantity + new quantity) > stock
            int totalRequested = item.getQuantity() + requestedQuantity;
            if (inventoryDto.getQuantity() < totalRequested) {
                throw new BadRequestException("Cannot add more. Total in cart would exceed stock. Available: " + inventoryDto.getQuantity());
            }

            item.setQuantity(totalRequested);
            item.setPrice(itemDto.getPrice());
            item.setSubTotal(itemDto.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        } else {
            CartItem newItem = new CartItem();
            newItem.setItemId(itemId);
            newItem.setItemName(itemDto.getName());
            newItem.setQuantity(requestedQuantity);
            newItem.setPrice(itemDto.getPrice());
            newItem.setSubTotal(itemDto.getPrice().multiply(BigDecimal.valueOf(requestedQuantity)));
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        // Update Total
        BigDecimal total = cart.getItems().stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);

        return modelMapper.map(cartRepository.save(cart), CartDto.class);
    }

    @Override
    public CartDto getCartByUsername(String username) {
        Cart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "username", username));
        return modelMapper.map(cart, CartDto.class);
    }

    @Override
    @Transactional
    public CartDto removeItemFromCart(String username, Long itemId) {
        Cart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "username", username));

        // Remove item logic
        boolean removed = cart.getItems().removeIf(item -> item.getItemId().equals(itemId));

        if (!removed) {
            throw new ResourceNotFoundException("Item in Cart", "id", itemId);
        }

        // Recalculate Total
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            total = total.add(item.getSubTotal());
        }
        cart.setTotalPrice(total);

        Cart savedCart = cartRepository.save(cart);
        return modelMapper.map(savedCart, CartDto.class);
    }

    @Override
    @Transactional
    public String clearCart(String username) {
        Cart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "username", username));

        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);

        cartRepository.save(cart);
        return "Cart cleared successfully";
    }
}