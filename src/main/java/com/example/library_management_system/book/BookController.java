
package com.example.library_management_system.book;

import com.example.library_management_system.book.dto.BookRequestDto;
import com.example.library_management_system.book.dto.BookResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponseDto createBook(
            @Valid @RequestBody BookRequestDto request) {

        return bookService.createBook(request);
    }

    @GetMapping
    public List<BookResponseDto> getAllBooks() {

        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    public BookResponseDto getBookById(@PathVariable Long id) {

        return bookService.getBookById(id);
    }

    @PutMapping("/{id}")
    public BookResponseDto updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequestDto request) {

        return bookService.updateBook(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {

        bookService.deleteBook(id);
    }

    @GetMapping("/search/title")
    public List<BookResponseDto> searchByTitle(
            @RequestParam String title) {

        return bookService.searchByTitle(title);
    }

    @GetMapping("/search/category")
    public List<BookResponseDto> searchByCategory(
            @RequestParam BookCategory category) {

        return bookService.searchByCategory(category);
    }

    @GetMapping("/search/author/{authorId}")
    public List<BookResponseDto> searchByAuthor(
            @PathVariable Long authorId) {

        return bookService.getBooksByAuthorId(authorId);
    }
}
