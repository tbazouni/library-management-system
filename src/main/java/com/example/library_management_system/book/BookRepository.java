
package com.example.library_management_system.book;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByCategory(BookCategory category);

    List<Book> findByAuthorId(Long authorId);

    boolean existsByIsbn(String isbn);
}
