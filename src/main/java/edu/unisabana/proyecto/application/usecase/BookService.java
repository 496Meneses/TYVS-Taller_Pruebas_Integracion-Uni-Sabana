package edu.unisabana.proyecto.application.usecase;

import edu.unisabana.proyecto.application.port.out.BookRepositoryPort;
import edu.unisabana.proyecto.domain.model.Book;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookService {

    private final BookRepositoryPort bookRepo;

    public BookService(BookRepositoryPort bookRepo) {
        this.bookRepo = bookRepo;
    }

    public void addBook(Book book) {
        if (book == null || book.getTitle() == null || book.getTitle().isBlank()) {
            throw new IllegalArgumentException("El título del libro es obligatorio");
        }
        if (bookRepo.existsById(book.getId())) {
            throw new IllegalArgumentException("Ya existe un libro con id " + book.getId());
        }
        bookRepo.save(book);
    }

    public Optional<Book> getBook(int id) {
        return bookRepo.findById(id);
    }

    public void updateStock(int id, int newStock) {
        if (!bookRepo.existsById(id)) {
            throw new IllegalArgumentException("Libro no encontrado: " + id);
        }
        if (newStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        bookRepo.updateStock(id, newStock);
    }
}
