package com.ecommerce.service;

import com.ecommerce.dto.request.OrderRequest;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
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
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

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
                .items(new ArrayList<>())
                .build();

        testCartItem = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .build();

        testCart.getItems().add(testCartItem);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        OrderRequest request = new OrderRequest();
        request.setShippingAddress("123 Test St");
        request.setPaymentMethod("Credit Card");

        Order savedOrder = Order.builder()
                .id(1L)
                .user(testUser)
                .orderNumber("ORD-12345")
                .totalAmount(new BigDecimal("199.98"))
                .build();

        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("ORD-12345", result.getOrderNumber());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testCreateOrder_EmptyCart() {
        // Arrange
        OrderRequest request = new OrderRequest();
        request.setShippingAddress("123 Test St");

        Cart emptyCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .items(new ArrayList<>())
                .build();

        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(emptyCart));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.createOrder(request));
    }

    @Test
    void testCancelOrder_Success() {
        // Arrange
        Order testOrder = Order.builder()
                .id(1L)
                .user(testUser)
                .status(Order.OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .order(testOrder)
                .product(testProduct)
                .quantity(2)
                .build();

        testOrder.getItems().add(orderItem);

        when(orderRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testOrder));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        orderService.cancelOrder(1L);

        // Assert
        assertEquals(Order.OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCancelOrder_InvalidStatus() {
        // Arrange
        Order testOrder = Order.builder()
                .id(1L)
                .user(testUser)
                .status(Order.OrderStatus.SHIPPED) // Cannot cancel shipped orders
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(1L));
    }
}