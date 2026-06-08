package edu.unisabana.proyecto.domain.model.rq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookRequest {
    private int id;
    private String title;
    private String author;
    private double price;
    private int stock;
    private boolean available;
}
