package edu.unisabana.proyecto.integration;

import edu.unisabana.proyecto.application.usecase.BookService;
import edu.unisabana.proyecto.domain.model.Book;
import edu.unisabana.proyecto.infrastructure.persistence.H2BookRepository;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Pruebas de integracion: validan la interaccion real entre BookService y la
 * base de datos H2 en memoria, siguiendo el patron Arrange-Act-Assert (AAA).
 */
public class BookServiceIntegrationTest {

    private H2BookRepository bookRepo;
    private BookService bookService;

    @Before
    public void setUp() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:bookservicetest;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        bookRepo = new H2BookRepository(jdbc);
        bookRepo.deleteAll();
        bookService = new BookService(bookRepo);
    }

    // --- Prueba 1: registro exitoso ---
    @Test
    public void shouldAddValidBook() {
        // Arrange
        Book book = new Book(1, "Clean Code", "Robert C. Martin", 45.0, 10, true);

        // Act
        bookService.addBook(book);

        // Assert
        Optional<Book> found = bookService.getBook(1);
        assertTrue("El libro debe existir en la base de datos", found.isPresent());
        assertEquals("Clean Code", found.get().getTitle());
        assertEquals(10, found.get().getStock());
        assertTrue(found.get().isAvailable());
    }

    // --- Prueba 2: libro inexistente devuelve Optional vacío ---
    @Test
    public void shouldReturnEmptyOptionalWhenBookNotFound() {
        // Arrange - no hay libros en la BD

        // Act
        Optional<Book> result = bookService.getBook(999);

        // Assert
        assertFalse("No debe encontrarse un libro que no existe", result.isPresent());
    }

    // --- Prueba 3: rechazo de libro duplicado ---
    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectDuplicateBookId() {
        // Arrange
        Book book = new Book(5, "Refactoring", "Martin Fowler", 40.0, 5, true);
        bookService.addBook(book);

        // Act - intentar registrar el mismo ID lanza excepción
        bookService.addBook(new Book(5, "Otro título", "Autor", 20.0, 3, true));
    }

    // --- Prueba 4: rechazo de título nulo ---
    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectBookWithNullTitle() {
        // Arrange
        Book book = new Book(10, null, "Autor", 20.0, 3, true);

        // Act & Assert - titulo nulo lanza excepción
        bookService.addBook(book);
    }

    // --- Prueba 5: actualización de stock ---
    @Test
    public void shouldUpdateStockCorrectly() {
        // Arrange
        Book book = new Book(7, "Design Patterns", "GoF", 55.0, 8, true);
        bookService.addBook(book);

        // Act
        bookService.updateStock(7, 3);

        // Assert
        Optional<Book> updated = bookService.getBook(7);
        assertTrue(updated.isPresent());
        assertEquals(3, updated.get().getStock());
        assertTrue(updated.get().isAvailable());
    }

    // --- Prueba 6: stock en cero marca libro como no disponible ---
    @Test
    public void shouldMarkBookUnavailableWhenStockSetToZero() {
        // Arrange
        Book book = new Book(8, "TDD by Example", "Kent Beck", 35.0, 5, true);
        bookService.addBook(book);

        // Act
        bookService.updateStock(8, 0);

        // Assert
        Optional<Book> updated = bookService.getBook(8);
        assertTrue(updated.isPresent());
        assertEquals(0, updated.get().getStock());
        assertFalse("Con stock=0 el libro debe marcarse no disponible", updated.get().isAvailable());
    }

    // --- Prueba 7: error al actualizar stock de libro inexistente ---
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenUpdatingStockOfNonExistentBook() {
        // Act & Assert
        bookService.updateStock(999, 5);
    }
}
