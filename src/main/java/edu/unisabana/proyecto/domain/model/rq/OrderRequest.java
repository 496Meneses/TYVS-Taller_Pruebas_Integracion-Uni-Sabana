package edu.unisabana.proyecto.domain.model.rq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private int bookId;
    private String customerName;
    private int quantity;
}
