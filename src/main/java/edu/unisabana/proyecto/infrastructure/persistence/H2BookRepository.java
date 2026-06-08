package edu.unisabana.proyecto.infrastructure.persistence;

import edu.unisabana.proyecto.application.port.out.BookRepositoryPort;
import edu.unisabana.proyecto.domain.model.Book;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class H2BookRepository implements BookRepositoryPort {

    private final JdbcTemplate jdbc;

    public H2BookRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        initSchema();
    }

    private void initSchema() {
        jdbc.execute(
            "CREATE TABLE IF NOT EXISTS books (" +
            "id INT PRIMARY KEY, " +
            "title VARCHAR(255) NOT NULL, " +
            "author VARCHAR(255), " +
            "price DOUBLE, " +
            "stock INT DEFAULT 0, " +
            "available BOOLEAN DEFAULT TRUE)"
        );
    }

    private final RowMapper<Book> bookRowMapper = (rs, rowNum) -> new Book(
        rs.getInt("id"),
        rs.getString("title"),
        rs.getString("author"),
        rs.getDouble("price"),
        rs.getInt("stock"),
        rs.getBoolean("available")
    );

    @Override
    public void save(Book book) {
        jdbc.update(
            "INSERT INTO books (id, title, author, price, stock, available) VALUES (?, ?, ?, ?, ?, ?)",
            book.getId(), book.getTitle(), book.getAuthor(),
            book.getPrice(), book.getStock(), book.isAvailable()
        );
    }

    @Override
    public Optional<Book> findById(int id) {
        List<Book> results = jdbc.query(
            "SELECT * FROM books WHERE id = ?", bookRowMapper, id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsById(int id) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM books WHERE id = ?", Integer.class, id
        );
        return count != null && count > 0;
    }

    @Override
    public void updateStock(int id, int newStock) {
        jdbc.update(
            "UPDATE books SET stock = ?, available = ? WHERE id = ?",
            newStock, newStock > 0, id
        );
    }

    @Override
    public void deleteAll() {
        jdbc.execute("TRUNCATE TABLE books");
    }
}
