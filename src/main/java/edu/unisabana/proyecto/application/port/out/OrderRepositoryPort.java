package edu.unisabana.proyecto.application.port.out;

import edu.unisabana.proyecto.domain.model.Order;
import edu.unisabana.proyecto.domain.model.OrderStatus;

import java.util.Optional;

public interface OrderRepositoryPort {
    int save(Order order);
    Optional<Order> findById(int id);
    void updateStatus(int id, OrderStatus status);
    void deleteAll();
}
