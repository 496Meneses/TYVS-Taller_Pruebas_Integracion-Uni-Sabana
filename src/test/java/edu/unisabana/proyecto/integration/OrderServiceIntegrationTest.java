package edu.unisabana.proyecto.integration;

import edu.unisabana.proyecto.application.usecase.OrderService;
import edu.unisabana.proyecto.domain.model.Book;
import edu.unisabana.proyecto.domain.model.Order;
import edu.unisabana.proyecto.domain.model.OrderResult;
import edu.unisabana.proyecto.domain.model.OrderStatus;
import edu.unisabana.proyecto.infrastructure.persistence.H2BookRepository;
import edu.unisabana.proyecto.infrastructure.persistence.H2OrderRepository;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Pruebas de integracion: validan la interaccion real entre OrderService,
 * H2BookRepository y H2OrderRepository, usando base de datos H2 en memoria.
 * Patron Arrange-Act-Assert (AAA).
 */
public class OrderServiceIntegrationTest {

    private H2BookRepository bookRepo;
    private H2OrderRepository orderRepo;
    private OrderService orderService;

    @Before
    public void setUp() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:orderservicetest;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        bookRepo = new H2BookRepository(jdbc);
        orderRepo = new H2OrderRepository(jdbc);
        orderRepo.deleteAll();
        bookRepo.deleteAll();
        orderService = new OrderService(orderRepo, bookRepo);
    }

    // --- Prueba 1: pedido exitoso reduce stock del libro ---
    @Test
    public void shouldCreateOrderAndReduceStockWhenBookAvailable() {
        // Arrange
        bookRepo.save(new Book(1, "Effective Java", "Joshua Bloch", 50.0, 10, true));

        // Act
        OrderResult result = orderService.placeOrder(1, "Carlos Meneses", 3);

        // Assert
        assertEquals(OrderResult.CREATED, result);
        Optional<Book> updatedBook = bookRepo.findById(1);
        assertTrue(updatedBook.isPresent());
        assertEquals("El stock debe reducirse en 3", 7, updatedBook.get().getStock());
    }

    // --- Prueba 2: libro inexistente devuelve BOOK_NOT_FOUND ---
    @Test
    public void shouldReturnBookNotFoundWhenBookDoesNotExist() {
        // Arrange - base de datos sin libros

        // Act
        OrderResult result = orderService.placeOrder(999, "Cliente", 1);

        // Assert
        assertEquals(OrderResult.BOOK_NOT_FOUND, result);
    }

    // --- Prueba 3: stock insuficiente devuelve OUT_OF_STOCK ---
    @Test
    public void shouldReturnOutOfStockWhenInsufficientStock() {
        // Arrange
        bookRepo.save(new Book(2, "The Pragmatic Programmer", "Hunt & Thomas", 45.0, 1, true));

        // Act
        OrderResult result = orderService.placeOrder(2, "Cliente", 5);

        // Assert
        assertEquals(OrderResult.OUT_OF_STOCK, result);
    }

    // --- Prueba 4: libro no disponible devuelve OUT_OF_STOCK ---
    @Test
    public void shouldReturnOutOfStockWhenBookNotAvailable() {
        // Arrange
        bookRepo.save(new Book(3, "Clean Architecture", "Robert C. Martin", 48.0, 0, false));

        // Act
        OrderResult result = orderService.placeOrder(3, "Cliente", 1);

        // Assert
        assertEquals(OrderResult.OUT_OF_STOCK, result);
    }

    // --- Prueba 5: confirmar pedido pendiente exitosamente ---
    @Test
    public void shouldConfirmPendingOrder() {
        // Arrange
        bookRepo.save(new Book(4, "Patterns of Enterprise App Architecture", "Fowler", 65.0, 5, true));
        int orderId = orderRepo.save(new Order(0, 4, "Ana García", 1, OrderStatus.PENDING));

        // Act
        OrderResult result = orderService.confirmOrder(orderId);

        // Assert
        assertEquals(OrderResult.CONFIRMED, result);
        Optional<Order> updatedOrder = orderRepo.findById(orderId);
        assertTrue(updatedOrder.isPresent());
        assertEquals(OrderStatus.CONFIRMED, updatedOrder.get().getStatus());
    }

    // --- Prueba 6: cancelar pedido ---
    @Test
    public void shouldCancelOrder() {
        // Arrange
        bookRepo.save(new Book(5, "Domain-Driven Design", "Evans", 60.0, 3, true));
        int orderId = orderRepo.save(new Order(0, 5, "Pedro López", 1, OrderStatus.PENDING));

        // Act
        OrderResult result = orderService.cancelOrder(orderId);

        // Assert
        assertEquals(OrderResult.CANCELLED, result);
        Optional<Order> updatedOrder = orderRepo.findById(orderId);
        assertTrue(updatedOrder.isPresent());
        assertEquals(OrderStatus.CANCELLED, updatedOrder.get().getStatus());
    }

    // --- Prueba 7: confirmar pedido inexistente devuelve NOT_FOUND ---
    @Test
    public void shouldReturnNotFoundWhenConfirmingNonExistentOrder() {
        // Act
        OrderResult result = orderService.confirmOrder(9999);

        // Assert
        assertEquals(OrderResult.NOT_FOUND, result);
    }

    // --- Prueba 8: confirmar pedido ya confirmado devuelve INVALID_STATUS ---
    @Test
    public void shouldReturnInvalidStatusWhenConfirmingAlreadyConfirmedOrder() {
        // Arrange
        int orderId = orderRepo.save(new Order(0, 1, "Test", 1, OrderStatus.CONFIRMED));

        // Act
        OrderResult result = orderService.confirmOrder(orderId);

        // Assert
        assertEquals(OrderResult.INVALID_STATUS, result);
    }

    // --- Prueba 9: cancelar pedido ya cancelado devuelve INVALID_STATUS ---
    @Test
    public void shouldReturnInvalidStatusWhenCancellingAlreadyCancelledOrder() {
        // Arrange
        int orderId = orderRepo.save(new Order(0, 1, "Test", 1, OrderStatus.CANCELLED));

        // Act
        OrderResult result = orderService.cancelOrder(orderId);

        // Assert
        assertEquals(OrderResult.INVALID_STATUS, result);
    }

    // --- Prueba 10: getOrder retorna pedido existente ---
    @Test
    public void shouldRetrieveExistingOrder() {
        // Arrange
        bookRepo.save(new Book(6, "Refactoring", "Fowler", 42.0, 4, true));
        int orderId = orderRepo.save(new Order(0, 6, "Laura Reyes", 2, OrderStatus.PENDING));

        // Act
        Optional<Order> order = orderService.getOrder(orderId);

        // Assert
        assertTrue(order.isPresent());
        assertEquals("Laura Reyes", order.get().getCustomerName());
        assertEquals(6, order.get().getBookId());
    }
}
