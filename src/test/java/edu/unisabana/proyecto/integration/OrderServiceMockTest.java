package edu.unisabana.proyecto.integration;

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
 * Pruebas con dobles de prueba (Mockito): verifican la logica de negocio de
 * OrderService de forma aislada, sin base de datos ni infraestructura real.
 * Patron Arrange-Act-Assert (AAA).
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceMockTest {

    @Mock
    private OrderRepositoryPort orderRepo;

    @Mock
    private BookRepositoryPort bookRepo;

    private OrderService orderService;

    @Before
    public void setUp() {
        orderService = new OrderService(orderRepo, bookRepo);
    }

    // --- Prueba 1: libro inexistente no consulta el repositorio de pedidos ---
    @Test
    public void shouldReturnBookNotFoundWithoutSavingOrder() {
        // Arrange
        when(bookRepo.findById(99)).thenReturn(Optional.empty());

        // Act
        OrderResult result = orderService.placeOrder(99, "Cliente", 1);

        // Assert
        assertEquals(OrderResult.BOOK_NOT_FOUND, result);
        verify(orderRepo, never()).save(any(Order.class));
    }

    // --- Prueba 2: stock insuficiente no genera pedido ---
    @Test
    public void shouldReturnOutOfStockAndNotSaveWhenStockInsufficient() {
        // Arrange
        Book book = new Book(1, "Test Book", "Author", 20.0, 2, true);
        when(bookRepo.findById(1)).thenReturn(Optional.of(book));

        // Act
        OrderResult result = orderService.placeOrder(1, "Cliente", 10);

        // Assert
        assertEquals(OrderResult.OUT_OF_STOCK, result);
        verify(orderRepo, never()).save(any(Order.class));
    }

    // --- Prueba 3: libro no disponible (stock=0) no genera pedido ---
    @Test
    public void shouldReturnOutOfStockWhenBookNotAvailable() {
        // Arrange
        Book book = new Book(2, "Unavailable Book", "Author", 30.0, 5, false);
        when(bookRepo.findById(2)).thenReturn(Optional.of(book));

        // Act
        OrderResult result = orderService.placeOrder(2, "Cliente", 1);

        // Assert
        assertEquals(OrderResult.OUT_OF_STOCK, result);
        verify(orderRepo, never()).save(any(Order.class));
    }

    // --- Prueba 4: pedido válido guarda en repositorio y reduce stock ---
    @Test
    public void shouldSaveOrderAndUpdateStockWhenDataIsValid() {
        // Arrange
        Book book = new Book(3, "Available Book", "Author", 25.0, 5, true);
        when(bookRepo.findById(3)).thenReturn(Optional.of(book));
        when(orderRepo.save(any(Order.class))).thenReturn(1);

        // Act
        OrderResult result = orderService.placeOrder(3, "Cliente Válido", 2);

        // Assert
        assertEquals(OrderResult.CREATED, result);
        verify(orderRepo, times(1)).save(any(Order.class));
        verify(bookRepo, times(1)).updateStock(3, 3);
    }

    // --- Prueba 5: confirmar pedido inexistente devuelve NOT_FOUND ---
    @Test
    public void shouldReturnNotFoundWhenConfirmingNonExistentOrder() {
        // Arrange
        when(orderRepo.findById(999)).thenReturn(Optional.empty());

        // Act
        OrderResult result = orderService.confirmOrder(999);

        // Assert
        assertEquals(OrderResult.NOT_FOUND, result);
        verify(orderRepo, never()).updateStatus(anyInt(), any(OrderStatus.class));
    }

    // --- Prueba 6: confirmar pedido no-pendiente devuelve INVALID_STATUS ---
    @Test
    public void shouldReturnInvalidStatusWhenOrderIsNotPending() {
        // Arrange
        Order confirmedOrder = new Order(1, 1, "Cliente", 1, OrderStatus.CONFIRMED);
        when(orderRepo.findById(1)).thenReturn(Optional.of(confirmedOrder));

        // Act
        OrderResult result = orderService.confirmOrder(1);

        // Assert
        assertEquals(OrderResult.INVALID_STATUS, result);
        verify(orderRepo, never()).updateStatus(anyInt(), any(OrderStatus.class));
    }

    // --- Prueba 7: cancelar pedido ya cancelado devuelve INVALID_STATUS ---
    @Test
    public void shouldReturnInvalidStatusWhenCancellingAlreadyCancelledOrder() {
        // Arrange
        Order cancelledOrder = new Order(2, 1, "Cliente", 1, OrderStatus.CANCELLED);
        when(orderRepo.findById(2)).thenReturn(Optional.of(cancelledOrder));

        // Act
        OrderResult result = orderService.cancelOrder(2);

        // Assert
        assertEquals(OrderResult.INVALID_STATUS, result);
        verify(orderRepo, never()).updateStatus(anyInt(), any(OrderStatus.class));
    }

    // --- Prueba 8: cancelar pedido pendiente llama a updateStatus ---
    @Test
    public void shouldUpdateStatusToCancelledWhenOrderIsPending() {
        // Arrange
        Order pendingOrder = new Order(3, 1, "Cliente", 1, OrderStatus.PENDING);
        when(orderRepo.findById(3)).thenReturn(Optional.of(pendingOrder));

        // Act
        OrderResult result = orderService.cancelOrder(3);

        // Assert
        assertEquals(OrderResult.CANCELLED, result);
        verify(orderRepo, times(1)).updateStatus(3, OrderStatus.CANCELLED);
    }
}
