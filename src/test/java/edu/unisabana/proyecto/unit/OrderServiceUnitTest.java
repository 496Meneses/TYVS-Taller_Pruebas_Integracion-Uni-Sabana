package edu.unisabana.proyecto.unit;

import edu.unisabana.proyecto.application.port.out.BookRepositoryPort;
import edu.unisabana.proyecto.application.port.out.OrderRepositoryPort;
import edu.unisabana.proyecto.application.usecase.OrderService;
import edu.unisabana.proyecto.domain.model.Book;
import edu.unisabana.proyecto.domain.model.Order;
import edu.unisabana.proyecto.domain.model.OrderResult;
import edu.unisabana.proyecto.domain.model.OrderStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias puras de OrderService: cada prueba verifica una
 * regla de negocio de forma aislada, sin infraestructura real.
 * Patron Arrange-Act-Assert (AAA).
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceUnitTest {

    @Mock
    private OrderRepositoryPort orderRepo;

    @Mock
    private BookRepositoryPort bookRepo;

    private OrderService orderService;

    @Before
    public void setUp() {
        orderService = new OrderService(orderRepo, bookRepo);
    }

    // --- placeOrder: validaciones de entrada ---

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenCustomerNameIsNull() {
        // Act
        orderService.placeOrder(1, null, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenCustomerNameIsBlank() {
        // Act
        orderService.placeOrder(1, "   ", 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenQuantityIsZero() {
        // Act
        orderService.placeOrder(1, "Cliente", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenQuantityIsNegative() {
        // Act
        orderService.placeOrder(1, "Cliente", -3);
    }

    // --- placeOrder: reglas de negocio ---

    @Test
    public void shouldNotQueryOrderRepoWhenBookDoesNotExist() {
        // Arrange
        when(bookRepo.findById(anyInt())).thenReturn(Optional.empty());

        // Act
        OrderResult result = orderService.placeOrder(1, "Cliente", 1);

        // Assert
        assertEquals(OrderResult.BOOK_NOT_FOUND, result);
        verifyNoInteractions(orderRepo);
    }

    @Test
    public void shouldReduceExactQuantityFromStockOnSuccess() {
        // Arrange
        Book book = new Book(1, "Test", "Author", 10.0, 8, true);
        when(bookRepo.findById(1)).thenReturn(Optional.of(book));
        when(orderRepo.save(any(Order.class))).thenReturn(1);

        // Act
        orderService.placeOrder(1, "Cliente", 3);

        // Assert — stock reducido exactamente en 3 (8 - 3 = 5)
        verify(bookRepo, times(1)).updateStock(1, 5);
    }

    @Test
    public void shouldReturnOutOfStockWhenRequestedQuantityEqualsStockPlusOne() {
        // Arrange — stock=2, se pide 3
        Book book = new Book(2, "Test", "Author", 10.0, 2, true);
        when(bookRepo.findById(2)).thenReturn(Optional.of(book));

        // Act
        OrderResult result = orderService.placeOrder(2, "Cliente", 3);

        // Assert
        assertEquals(OrderResult.OUT_OF_STOCK, result);
    }

    @Test
    public void shouldAllowOrderWhenQuantityEqualsStock() {
        // Arrange — stock=5, se pide exactamente 5
        Book book = new Book(3, "Test", "Author", 10.0, 5, true);
        when(bookRepo.findById(3)).thenReturn(Optional.of(book));
        when(orderRepo.save(any(Order.class))).thenReturn(1);

        // Act
        OrderResult result = orderService.placeOrder(3, "Cliente", 5);

        // Assert
        assertEquals(OrderResult.CREATED, result);
    }

    // --- confirmOrder ---

    @Test
    public void shouldCallUpdateStatusToConfirmedWhenOrderIsPending() {
        // Arrange
        Order pending = new Order(1, 1, "Cliente", 1, OrderStatus.PENDING);
        when(orderRepo.findById(1)).thenReturn(Optional.of(pending));

        // Act
        OrderResult result = orderService.confirmOrder(1);

        // Assert
        assertEquals(OrderResult.CONFIRMED, result);
        verify(orderRepo, times(1)).updateStatus(1, OrderStatus.CONFIRMED);
    }

    @Test
    public void shouldNotCallUpdateStatusWhenOrderIsNotFound() {
        // Arrange
        when(orderRepo.findById(anyInt())).thenReturn(Optional.empty());

        // Act
        orderService.confirmOrder(999);

        // Assert
        verify(orderRepo, never()).updateStatus(anyInt(), any(OrderStatus.class));
    }

    // --- cancelOrder ---

    @Test
    public void shouldCallUpdateStatusToCancelledWhenOrderIsConfirmed() {
        // Arrange — un pedido confirmado también puede cancelarse
        Order confirmed = new Order(2, 1, "Cliente", 1, OrderStatus.CONFIRMED);
        when(orderRepo.findById(2)).thenReturn(Optional.of(confirmed));

        // Act
        OrderResult result = orderService.cancelOrder(2);

        // Assert
        assertEquals(OrderResult.CANCELLED, result);
        verify(orderRepo, times(1)).updateStatus(2, OrderStatus.CANCELLED);
    }

    @Test
    public void shouldReturnNotFoundWhenCancellingNonExistentOrder() {
        // Arrange
        when(orderRepo.findById(anyInt())).thenReturn(Optional.empty());

        // Act
        OrderResult result = orderService.cancelOrder(500);

        // Assert
        assertEquals(OrderResult.NOT_FOUND, result);
        verify(orderRepo, never()).updateStatus(anyInt(), any(OrderStatus.class));
    }

    // --- getOrder ---

    @Test
    public void shouldDelegateGetOrderToRepository() {
        // Arrange
        Order order = new Order(3, 1, "Laura", 2, OrderStatus.PENDING);
        when(orderRepo.findById(3)).thenReturn(Optional.of(order));

        // Act
        Optional<Order> result = orderService.getOrder(3);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Laura", result.get().getCustomerName());
        verify(orderRepo, times(1)).findById(3);
    }
}
