package edu.unisabana.proyecto.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private int id;
    private int bookId;
    private String customerName;
    private int quantity;
    private OrderStatus status;
}
