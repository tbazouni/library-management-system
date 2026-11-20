
package com.example.library_management_system.borrowing.dto;

import com.example.library_management_system.borrowing.BorrowingStatus;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BorrowingResponseDto {

    private Long id;

    private Long bookId;
    private String bookTitle;

    private Long borrowerId;
    private String borrowerName;

    private LocalDate borrowDate;
    private LocalDate returnDate;

    private BorrowingStatus status;
}
