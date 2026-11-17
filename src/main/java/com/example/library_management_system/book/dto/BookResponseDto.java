
package com.example.library_management_system.book.dto;

import com.example.library_management_system.book.BookCategory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookResponseDto {

    private Long id;
    private String title;
    private String isbn;
    private BookCategory category;
    private Long authorId;
    private String authorName;
    private boolean available;
}
