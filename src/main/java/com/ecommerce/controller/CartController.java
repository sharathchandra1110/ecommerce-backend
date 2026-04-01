package com.ecommerce.controller;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.model.Cart;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart() {
        Cart cart = cartService.getCart();
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addItemToCart(@Valid @RequestBody CartItemRequest request) {
        Cart cart = cartService.addItemToCart(request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<Cart> updateCartItem(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        Cart cart = cartService.updateCartItem(productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse> removeItemFromCart(@PathVariable Long productId) {
        cartService.removeItemFromCart(productId);
        return ResponseEntity.ok(new ApiResponse(true, "Item removed from cart"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(new ApiResponse(true, "Cart cleared successfully"));
    }
}