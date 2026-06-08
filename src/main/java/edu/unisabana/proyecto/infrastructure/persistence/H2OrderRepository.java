package edu.unisabana.proyecto.infrastructure.persistence;

import edu.unisabana.proyecto.application.port.out.OrderRepositoryPort;
import edu.unisabana.proyecto.domain.model.Order;
import edu.unisabana.proyecto.domain.model.OrderStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Component
public class H2OrderRepository implements OrderRepositoryPort {

    private final JdbcTemplate jdbc;

    public H2OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        initSchema();
    }

    private void initSchema() {
        jdbc.execute(
            "CREATE TABLE IF NOT EXISTS orders (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "book_id INT NOT NULL, " +
            "customer_name VARCHAR(255) NOT NULL, " +
            "quantity INT NOT NULL, " +
            "status VARCHAR(50) NOT NULL)"
        );
    }

    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> new Order(
        rs.getInt("id"),
        rs.getInt("book_id"),
        rs.getString("customer_name"),
        rs.getInt("quantity"),
        OrderStatus.valueOf(rs.getString("status"))
    );

    @Override
    public int save(Order order) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO orders (book_id, customer_name, quantity, status) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, order.getBookId());
            ps.setString(2, order.getCustomerName());
            ps.setInt(3, order.getQuantity());
            ps.setString(4, order.getStatus().name());
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().intValue() : -1;
    }

    @Override
    public Optional<Order> findById(int id) {
        List<Order> results = jdbc.query(
            "SELECT * FROM orders WHERE id = ?", orderRowMapper, id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public void updateStatus(int id, OrderStatus status) {
        jdbc.update("UPDATE orders SET status = ? WHERE id = ?", status.name(), id);
    }

    @Override
    public void deleteAll() {
        jdbc.execute("TRUNCATE TABLE orders");
    }
}
