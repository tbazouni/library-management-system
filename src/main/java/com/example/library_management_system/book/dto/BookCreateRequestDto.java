package com.example.library_management_system.book.dto;

import com.example.library_management_system.book.BookCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCreateRequestDto {

    @NotBlank(message = "Book title is required")
    private String title;

    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotNull(message = "Book category is required")
    private BookCategory category;
}