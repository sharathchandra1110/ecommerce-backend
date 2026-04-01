package com.ecommerce.service;

import com.ecommerce.dto.request.CartItemRequest;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .build();

        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    void testAddItemToCart_Success() {
        // Arrange
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.addItemToCart(request);

        // Assert
        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void testAddItemToCart_InsufficientStock() {
        // Arrange
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantity(20); // More than available stock

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> cartService.addItemToCart(request));
    }

    @Test
    void testRemoveItemFromCart_Success() {
        // Arrange
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        doNothing().when(cartItemRepository).deleteByCartIdAndProductId(1L, 1L);

        // Act
        cartService.removeItemFromCart(1L);

        // Assert
        verify(cartItemRepository, times(1)).deleteByCartIdAndProductId(1L, 1L);
    }

    @Test
    void testGetCart_CreatesNewIfNotExists() {
        // Arrange
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.getCart();

        // Assert
        assertNotNull(result);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }
}