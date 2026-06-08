package edu.unisabana.proyecto.delivery.rest;

import edu.unisabana.proyecto.application.usecase.BookService;
import edu.unisabana.proyecto.domain.model.Book;
import edu.unisabana.proyecto.domain.model.rq.BookRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<String> addBook(@RequestBody BookRequest request) {
        Book book = new Book(
            request.getId(), request.getTitle(), request.getAuthor(),
            request.getPrice(), request.getStock(), request.isAvailable()
        );
        try {
            bookService.addBook(book);
            return ResponseEntity.ok("CREATED");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable int id) {
        return bookService.getBook(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<String> updateStock(@PathVariable int id, @RequestParam int quantity) {
        try {
            bookService.updateStock(id, quantity);
            return ResponseEntity.ok("UPDATED");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
