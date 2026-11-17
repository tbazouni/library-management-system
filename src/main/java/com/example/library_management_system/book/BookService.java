
package com.example.library_management_system.book;

import com.example.library_management_system.author.Author;
import com.example.library_management_system.author.AuthorRepository;
import com.example.library_management_system.book.dto.BookRequestDto;
import com.example.library_management_system.book.dto.BookResponseDto;
import com.example.library_management_system.exception.BusinessException;
import com.example.library_management_system.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookResponseDto createBook(BookRequestDto request) {

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException(
                    "A book with this ISBN already exists"
            );
        }

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Author not found with ID: "
                                        + request.getAuthorId()
                        )
                );

        Book book = new Book();

        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setAuthor(author);
        book.setAvailable(true);

        Book savedBook = bookRepository.save(book);

        log.info(
                "Book created successfully with ID: {}",
                savedBook.getId()
        );

        return mapToResponse(savedBook);
    }

    @Transactional(readOnly = true)
    public List<BookResponseDto> getAllBooks() {

        return bookRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponseDto getBookById(Long id) {

        Book book = findBookById(id);

        return mapToResponse(book);
    }

    public BookResponseDto updateBook(
            Long id,
            BookRequestDto request) {

        Book book = findBookById(id);

        if (!book.getIsbn().equals(request.getIsbn())
                && bookRepository.existsByIsbn(request.getIsbn())) {

            throw new BusinessException(
                    "A book with this ISBN already exists"
            );
        }

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Author not found with ID: "
                                        + request.getAuthorId()
                        )
                );

        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setAuthor(author);

        Book updatedBook = bookRepository.save(book);

        log.info("Book updated successfully with ID: {}", id);

        return mapToResponse(updatedBook);
    }

    public void deleteBook(Long id) {

        Book book = findBookById(id);

        bookRepository.delete(book);

        log.info("Book deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public List<BookResponseDto> searchByTitle(String title) {

        return bookRepository
                .findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookResponseDto> searchByCategory(
            BookCategory category) {

        return bookRepository.findByCategory(category)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookResponseDto> getBooksByAuthorId(Long authorId) {

        if (!authorRepository.existsById(authorId)) {
            throw new ResourceNotFoundException(
                    "Author not found with ID: " + authorId
            );
        }

        return bookRepository.findByAuthorId(authorId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Book findBookById(Long id) {

        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with ID: " + id
                        )
                );
    }

    private BookResponseDto mapToResponse(Book book) {

        BookResponseDto response = new BookResponseDto();

        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setIsbn(book.getIsbn());
        response.setCategory(book.getCategory());
        response.setAuthorId(book.getAuthor().getId());
        response.setAuthorName(book.getAuthor().getName());
        response.setAvailable(book.isAvailable());

        return response;
    }
}
