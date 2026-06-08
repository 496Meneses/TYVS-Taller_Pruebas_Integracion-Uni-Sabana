package edu.unisabana.proyecto.system;

import edu.unisabana.proyecto.application.port.out.BookRepositoryPort;
import edu.unisabana.proyecto.application.port.out.OrderRepositoryPort;
import edu.unisabana.proyecto.domain.model.Book;
import edu.unisabana.proyecto.domain.model.Order;
import edu.unisabana.proyecto.domain.model.OrderStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de sistema HTTP sobre OrderController: validan el ciclo
 * completo de solicitud-respuesta via REST sobre flujos de pedidos,
 * usando MockMvc con el contexto Spring Boot completo.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepositoryPort bookRepo;

    @Autowired
    private OrderRepositoryPort orderRepo;

    @Before
    public void cleanDatabase() {
        orderRepo.deleteAll();
        bookRepo.deleteAll();
    }

    // --- Prueba 1: POST /orders con libro disponible retorna 200 CREATED ---
    @Test
    public void shouldPlaceOrderWhenBookIsAvailable() throws Exception {
        // Arrange
        bookRepo.save(new Book(10, "Effective Java", "Joshua Bloch", 50.0, 10, true));
        String orderJson = "{\"bookId\":10,\"customerName\":\"Carlos Meneses\",\"quantity\":2}";

        // Act & Assert
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
            .andExpect(status().isOk())
            .andExpect(content().string("CREATED"));
    }

    // --- Prueba 2: POST /orders con libro inexistente retorna 400 BOOK_NOT_FOUND ---
    @Test
    public void shouldReturn400WhenBookDoesNotExist() throws Exception {
        // Arrange
        String orderJson = "{\"bookId\":9999,\"customerName\":\"Cliente\",\"quantity\":1}";

        // Act & Assert
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("BOOK_NOT_FOUND"));
    }

    // --- Prueba 3: POST /orders con stock insuficiente retorna 400 OUT_OF_STOCK ---
    @Test
    public void shouldReturn400WhenStockIsInsufficient() throws Exception {
        // Arrange
        bookRepo.save(new Book(11, "Clean Code", "Robert C. Martin", 45.0, 1, true));
        String orderJson = "{\"bookId\":11,\"customerName\":\"Cliente\",\"quantity\":50}";

        // Act & Assert
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("OUT_OF_STOCK"));
    }

    // --- Prueba 4: GET /orders/{id} retorna pedido existente ---
    @Test
    public void shouldReturnOrderWhenItExists() throws Exception {
        // Arrange
        int orderId = orderRepo.save(new Order(0, 10, "Ana García", 1, OrderStatus.PENDING));

        // Act & Assert
        mockMvc.perform(get("/orders/" + orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerName").value("Ana García"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // --- Prueba 5: GET /orders/{id} para pedido inexistente retorna 404 ---
    @Test
    public void shouldReturn404WhenOrderDoesNotExist() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders/9999"))
            .andExpect(status().isNotFound());
    }

    // --- Prueba 6: PUT /orders/{id}/confirm confirma pedido pendiente ---
    @Test
    public void shouldConfirmPendingOrder() throws Exception {
        // Arrange
        int orderId = orderRepo.save(new Order(0, 10, "Pedro López", 1, OrderStatus.PENDING));

        // Act & Assert
        mockMvc.perform(put("/orders/" + orderId + "/confirm"))
            .andExpect(status().isOk())
            .andExpect(content().string("CONFIRMED"));
    }

    // --- Prueba 7: PUT /orders/{id}/cancel cancela pedido ---
    @Test
    public void shouldCancelOrder() throws Exception {
        // Arrange
        int orderId = orderRepo.save(new Order(0, 10, "Laura Reyes", 1, OrderStatus.PENDING));

        // Act & Assert
        mockMvc.perform(put("/orders/" + orderId + "/cancel"))
            .andExpect(status().isOk())
            .andExpect(content().string("CANCELLED"));
    }

    // --- Prueba 8: PUT /orders/{id}/confirm para pedido inexistente retorna 404 ---
    @Test
    public void shouldReturn404WhenConfirmingNonExistentOrder() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/orders/9999/confirm"))
            .andExpect(status().isNotFound());
    }

    // --- Prueba 9: PUT /orders/{id}/confirm sobre pedido cancelado retorna 400 ---
    @Test
    public void shouldReturn400WhenConfirmingCancelledOrder() throws Exception {
        // Arrange
        int orderId = orderRepo.save(new Order(0, 10, "Cliente", 1, OrderStatus.CANCELLED));

        // Act & Assert
        mockMvc.perform(put("/orders/" + orderId + "/confirm"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("INVALID_STATUS"));
    }
}
