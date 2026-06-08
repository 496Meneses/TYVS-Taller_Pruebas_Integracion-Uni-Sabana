package edu.unisabana.proyecto.delivery.rest;

import edu.unisabana.proyecto.application.usecase.OrderService;
import edu.unisabana.proyecto.domain.model.Order;
import edu.unisabana.proyecto.domain.model.OrderResult;
import edu.unisabana.proyecto.domain.model.rq.OrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest request) {
        try {
            OrderResult result = orderService.placeOrder(
                request.getBookId(), request.getCustomerName(), request.getQuantity()
            );
            if (result == OrderResult.CREATED) {
                return ResponseEntity.ok("CREATED");
            } else if (result == OrderResult.BOOK_NOT_FOUND) {
                return ResponseEntity.badRequest().body("BOOK_NOT_FOUND");
            } else {
                return ResponseEntity.badRequest().body("OUT_OF_STOCK");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable int id) {
        return orderService.getOrder(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<String> confirmOrder(@PathVariable int id) {
        OrderResult result = orderService.confirmOrder(id);
        if (result == OrderResult.CONFIRMED) {
            return ResponseEntity.ok("CONFIRMED");
        } else if (result == OrderResult.NOT_FOUND) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.badRequest().body("INVALID_STATUS");
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable int id) {
        OrderResult result = orderService.cancelOrder(id);
        if (result == OrderResult.CANCELLED) {
            return ResponseEntity.ok("CANCELLED");
        } else if (result == OrderResult.NOT_FOUND) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.badRequest().body("INVALID_STATUS");
        }
    }
}
