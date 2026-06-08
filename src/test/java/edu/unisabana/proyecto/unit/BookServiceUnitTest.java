package edu.unisabana.proyecto.unit;

import edu.unisabana.proyecto.application.port.out.BookRepositoryPort;
import edu.unisabana.proyecto.application.usecase.BookService;
import edu.unisabana.proyecto.domain.model.Book;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias puras de BookService: cada prueba verifica un
 * comportamiento aislado sin base de datos ni contexto Spring.
 * Patron Arrange-Act-Assert (AAA).
 */
@RunWith(MockitoJUnitRunner.class)
public class BookServiceUnitTest {

    @Mock
    private BookRepositoryPort bookRepo;

    private BookService bookService;

    @Before
    public void setUp() {
        bookService = new BookService(bookRepo);
    }

    // --- addBook ---

    @Test
    public void shouldCallSaveWhenBookDataIsValid() {
        // Arrange
        Book book = new Book(1, "Clean Code", "Robert C. Martin", 45.0, 10, true);
        when(bookRepo.existsById(1)).thenReturn(false);

        // Act
        bookService.addBook(book);

        // Assert
        verify(bookRepo, times(1)).save(book);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenTitleIsNull() {
        // Arrange
        Book book = new Book(1, null, "Author", 20.0, 5, true);

        // Act
        bookService.addBook(book);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenTitleIsBlank() {
        // Arrange
        Book book = new Book(2, "   ", "Author", 20.0, 5, true);

        // Act
        bookService.addBook(book);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenBookIdAlreadyExists() {
        // Arrange
        Book book = new Book(3, "Effective Java", "Joshua Bloch", 50.0, 3, true);
        when(bookRepo.existsById(3)).thenReturn(true);

        // Act
        bookService.addBook(book);
    }

    @Test
    public void shouldNotCallSaveWhenBookAlreadyExists() {
        // Arrange
        Book book = new Book(4, "Refactoring", "Fowler", 40.0, 2, true);
        when(bookRepo.existsById(4)).thenReturn(true);

        // Act
        try {
            bookService.addBook(book);
        } catch (IllegalArgumentException ignored) { }

        // Assert
        verify(bookRepo, never()).save(any(Book.class));
    }

    // --- getBook ---

    @Test
    public void shouldReturnBookFromRepositoryWhenExists() {
        // Arrange
        Book expected = new Book(5, "TDD by Example", "Kent Beck", 35.0, 4, true);
        when(bookRepo.findById(5)).thenReturn(Optional.of(expected));

        // Act
        Optional<Book> result = bookService.getBook(5);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("TDD by Example", result.get().getTitle());
        verify(bookRepo, times(1)).findById(5);
    }

    @Test
    public void shouldReturnEmptyOptionalWhenBookNotFound() {
        // Arrange
        when(bookRepo.findById(99)).thenReturn(Optional.empty());

        // Act
        Optional<Book> result = bookService.getBook(99);

        // Assert
        assertFalse(result.isPresent());
    }

    // --- updateStock ---

    @Test
    public void shouldCallUpdateStockWhenBookExistsAndStockIsValid() {
        // Arrange
        when(bookRepo.existsById(6)).thenReturn(true);

        // Act
        bookService.updateStock(6, 15);

        // Assert
        verify(bookRepo, times(1)).updateStock(6, 15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenUpdatingStockOfNonExistentBook() {
        // Arrange
        when(bookRepo.existsById(7)).thenReturn(false);

        // Act
        bookService.updateStock(7, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenNewStockIsNegative() {
        // Arrange
        when(bookRepo.existsById(8)).thenReturn(true);

        // Act
        bookService.updateStock(8, -1);
    }

    @Test
    public void shouldNotCallUpdateStockWhenValidationFails() {
        // Arrange
        when(bookRepo.existsById(9)).thenReturn(false);

        // Act
        try {
            bookService.updateStock(9, 5);
        } catch (IllegalArgumentException ignored) { }

        // Assert
        verify(bookRepo, never()).updateStock(anyInt(), anyInt());
    }
}
