package edu.unisabana.proyecto.application.port.out;

import edu.unisabana.proyecto.domain.model.Book;

import java.util.Optional;

public interface BookRepositoryPort {
    void save(Book book);
    Optional<Book> findById(int id);
    boolean existsById(int id);
    void updateStock(int id, int newStock);
    void deleteAll();
}
