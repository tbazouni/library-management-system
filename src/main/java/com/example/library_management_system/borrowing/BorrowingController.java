
package com.example.library_management_system.borrowing;

import com.example.library_management_system.borrowing.dto.BorrowingRequestDto;
import com.example.library_management_system.borrowing.dto.BorrowingResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @GetMapping
    public List<BorrowingResponseDto> getAllBorrowings() {
        return borrowingService.getAllBorrowings();
    }

    @GetMapping("/{id}")
    public BorrowingResponseDto getBorrowingById(@PathVariable Long id) {
        return borrowingService.getBorrowingById(id);
    }

    @PostMapping("/books/{bookId}")
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowingResponseDto borrowBook(
            @PathVariable Long bookId,
            @Valid @RequestBody BorrowingRequestDto request) {

        return borrowingService.borrowBook(bookId, request);
    }

    @PutMapping("/{transactionId}/return")
    public BorrowingResponseDto returnBook(
            @PathVariable Long transactionId) {

        return borrowingService.returnBook(transactionId);
    }
}
