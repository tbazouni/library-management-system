
package com.example.library_management_system.author;

import com.example.library_management_system.author.dto.AuthorRequestDto;
import com.example.library_management_system.author.dto.AuthorResponseDto;
import com.example.library_management_system.book.BookService;
import com.example.library_management_system.book.dto.BookResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;
    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponseDto createAuthor(@Valid @RequestBody AuthorRequestDto request) {
        return authorService.createAuthor(request);
    }

    @GetMapping
    public List<AuthorResponseDto> getAllAuthors() {
        return authorService.getAllAuthors();
    }

    @GetMapping("/{id}")
    public AuthorResponseDto getAuthorById(@PathVariable Long id) {
        return authorService.getAuthorById(id);
    }

    @PutMapping("/{id}")
    public AuthorResponseDto updateAuthor(
            @PathVariable Long id,
            @Valid @RequestBody AuthorRequestDto request) {

        return authorService.updateAuthor(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
    }

    @GetMapping("/{id}/books")
    public List<BookResponseDto> getBooksByAuthor(@PathVariable Long id) {
        return bookService.getBooksByAuthorId(id);
    }

}
