package edu.unisabana.proyecto.system;

import edu.unisabana.proyecto.application.port.out.BookRepositoryPort;
import edu.unisabana.proyecto.domain.model.Book;
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
 * Pruebas de sistema HTTP sobre BookController: validan el ciclo
 * completo de solicitud-respuesta via REST, usando MockMvc con el
 * contexto Spring Boot completo arrancado en memoria.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BookControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepositoryPort bookRepo;

    @Before
    public void cleanDatabase() {
        bookRepo.deleteAll();
    }

    // --- Prueba 1: POST /books con datos válidos retorna 200 CREATED ---
    @Test
    public void shouldAddBookAndReturn200() throws Exception {
        // Arrange
        String bookJson = "{\"id\":1,\"title\":\"Clean Code\",\"author\":\"Robert C. Martin\",\"price\":45.0,\"stock\":10,\"available\":true}";

        // Act & Assert
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson))
            .andExpect(status().isOk())
            .andExpect(content().string("CREATED"));
    }

    // --- Prueba 2: GET /books/{id} retorna el libro existente ---
    @Test
    public void shouldReturnBookWhenItExists() throws Exception {
        // Arrange
        bookRepo.save(new Book(2, "Effective Java", "Joshua Bloch", 50.0, 5, true));

        // Act & Assert
        mockMvc.perform(get("/books/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Effective Java"))
            .andExpect(jsonPath("$.author").value("Joshua Bloch"))
            .andExpect(jsonPath("$.stock").value(5));
    }

    // --- Prueba 3: GET /books/{id} para libro inexistente retorna 404 ---
    @Test
    public void shouldReturn404WhenBookDoesNotExist() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/books/9999"))
            .andExpect(status().isNotFound());
    }

    // --- Prueba 4: POST /books con titulo nulo retorna 400 ---
    @Test
    public void shouldReturn400WhenTitleIsNull() throws Exception {
        // Arrange
        String invalidJson = "{\"id\":3,\"title\":null,\"author\":\"Autor\",\"price\":20.0,\"stock\":5,\"available\":true}";

        // Act & Assert
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    // --- Prueba 5: PUT /books/{id}/stock actualiza el stock correctamente ---
    @Test
    public void shouldUpdateStockAndReturn200() throws Exception {
        // Arrange
        bookRepo.save(new Book(4, "Design Patterns", "GoF", 55.0, 8, true));

        // Act & Assert
        mockMvc.perform(put("/books/4/stock")
                .param("quantity", "3"))
            .andExpect(status().isOk())
            .andExpect(content().string("UPDATED"));
    }

    // --- Prueba 6: POST /books con ID duplicado retorna 400 ---
    @Test
    public void shouldReturn400WhenBookIdAlreadyExists() throws Exception {
        // Arrange
        bookRepo.save(new Book(5, "Refactoring", "Fowler", 40.0, 3, true));
        String duplicateJson = "{\"id\":5,\"title\":\"Otro Libro\",\"author\":\"Otro Autor\",\"price\":25.0,\"stock\":2,\"available\":true}";

        // Act & Assert
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateJson))
            .andExpect(status().isBadRequest());
    }
}
