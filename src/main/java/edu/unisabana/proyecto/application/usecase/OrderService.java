package edu.unisabana.proyecto.application.usecase;

import edu.unisabana.proyecto.application.port.out.BookRepositoryPort;
import edu.unisabana.proyecto.application.port.out.OrderRepositoryPort;
import edu.unisabana.proyecto.domain.model.Book;
import edu.unisabana.proyecto.domain.model.Order;
import edu.unisabana.proyecto.domain.model.OrderResult;
import edu.unisabana.proyecto.domain.model.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepositoryPort orderRepo;
    private final BookRepositoryPort bookRepo;

    public OrderService(OrderRepositoryPort orderRepo, BookRepositoryPort bookRepo) {
        this.orderRepo = orderRepo;
        this.bookRepo = bookRepo;
    }

    public OrderResult placeOrder(int bookId, String customerName, int quantity) {
        if (customerName == null || customerName.isBlank() || quantity <= 0) {
            throw new IllegalArgumentException("Datos del pedido inválidos");
        }
        Optional<Book> bookOpt = bookRepo.findById(bookId);
        if (bookOpt.isEmpty()) {
            return OrderResult.BOOK_NOT_FOUND;
        }
        Book book = bookOpt.get();
        if (!book.isAvailable() || book.getStock() < quantity) {
            return OrderResult.OUT_OF_STOCK;
        }
        Order order = new Order(0, bookId, customerName, quantity, OrderStatus.PENDING);
        orderRepo.save(order);
        bookRepo.updateStock(bookId, book.getStock() - quantity);
        return OrderResult.CREATED;
    }

    public OrderResult confirmOrder(int orderId) {
        Optional<Order> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty()) {
            return OrderResult.NOT_FOUND;
        }
        if (orderOpt.get().getStatus() != OrderStatus.PENDING) {
            return OrderResult.INVALID_STATUS;
        }
        orderRepo.updateStatus(orderId, OrderStatus.CONFIRMED);
        return OrderResult.CONFIRMED;
    }

    public OrderResult cancelOrder(int orderId) {
        Optional<Order> orderOpt = orderRepo.findById(orderId);
        if (orderOpt.isEmpty()) {
            return OrderResult.NOT_FOUND;
        }
        if (orderOpt.get().getStatus() == OrderStatus.CANCELLED) {
            return OrderResult.INVALID_STATUS;
        }
        orderRepo.updateStatus(orderId, OrderStatus.CANCELLED);
        return OrderResult.CANCELLED;
    }

    public Optional<Order> getOrder(int orderId) {
        return orderRepo.findById(orderId);
    }
}
