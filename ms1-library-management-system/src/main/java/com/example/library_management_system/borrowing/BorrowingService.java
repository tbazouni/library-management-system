
package com.example.library_management_system.borrowing;

import com.example.library_management_system.book.Book;
import com.example.library_management_system.book.BookRepository;
import com.example.library_management_system.borrower.Borrower;
import com.example.library_management_system.borrower.BorrowerRepository;
import com.example.library_management_system.borrowing.dto.BorrowingRequestDto;
import com.example.library_management_system.borrowing.dto.BorrowingResponseDto;
import com.example.library_management_system.exception.BusinessException;
import com.example.library_management_system.exception.ResourceNotFoundException;
import com.example.library_management_system.integration.notification.EmailNotificationClient;
import com.example.library_management_system.integration.notification.EmailNotificationRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final BorrowerRepository borrowerRepository;
    private final EmailNotificationClient emailNotificationClient;

    // Maximum number of active borrowing transactions
    // The value is read from application.properties
    @Value("${borrower.transaction.limit}")
    private int transactionLimit;

    // Get all borrowing transactions
    @Transactional(readOnly = true)
    public List<BorrowingResponseDto> getAllBorrowings() {

        return borrowingRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get a borrowing transaction by ID
    @Transactional(readOnly = true)
    public BorrowingResponseDto getBorrowingById(Long id) {

        BorrowingTransaction transaction =
                borrowingRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Borrowing transaction not found with ID: "
                                                + id
                                )
                        );

        return mapToResponse(transaction);
    }

    // Borrow a book
    public BorrowingResponseDto borrowBook(
            Long bookId,
            BorrowingRequestDto request) {

        // 1. Find the requested book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with ID: " + bookId
                        )
                );

        // 2. Check whether the book is available
        if (!book.isAvailable()) {
            throw new BusinessException(
                    "This book is not available for borrowing"
            );
        }

        // 3. Find the borrower
        Borrower borrower = borrowerRepository
                .findById(request.getBorrowerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Borrower not found with ID: "
                                        + request.getBorrowerId()
                        )
                );

        // 4. Count the borrower's active borrowing transactions
        long currentTransactions =
                borrowingRepository.countByBorrowerIdAndStatus(
                        borrower.getId(),
                        BorrowingStatus.BORROWED
                );

        // 5. Check whether the borrower reached the maximum limit
        if (currentTransactions >= transactionLimit) {

            throw new BusinessException(
                    "Borrower has reached the maximum borrowing limit of "
                            + transactionLimit + " books"
            );
        }

        // 6. Create a new borrowing transaction
        BorrowingTransaction transaction =
                new BorrowingTransaction();

        transaction.setBook(book);
        transaction.setBorrower(borrower);
        transaction.setBorrowDate(LocalDate.now());
        transaction.setReturnDate(null);
        transaction.setStatus(BorrowingStatus.BORROWED);

        // 7. Mark the book as unavailable
        book.setAvailable(false);

        bookRepository.save(book);

        // 8. Save the borrowing transaction
        BorrowingTransaction savedTransaction =
                borrowingRepository.save(transaction);

        log.info(
                "Book ID {} borrowed successfully by borrower ID {}",
                bookId,
                borrower.getId()
        );

        EmailNotificationRequest notification = new EmailNotificationRequest(
                borrower.getEmail(),
                "Book " + book.getTitle() + " borrowed successfully"
        );

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            emailNotificationClient.sendEmail(notification);
                        }
                        catch (RuntimeException exception) {
                            log.error(
                                    "Borrowing transaction {} was saved, but its email notification could not be delivered",
                                    savedTransaction.getId(),
                                    exception
                            );
                        }
                    }
                }
        );

        return mapToResponse(savedTransaction);
    }

    // Return a borrowed book
    public BorrowingResponseDto returnBook(Long transactionId) {

        // 1. Find the borrowing transaction
        BorrowingTransaction transaction =
                borrowingRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Borrowing transaction not found with ID: "
                                                + transactionId
                                )
                        );

        // 2. Check whether the book was already returned
        if (transaction.getStatus() == BorrowingStatus.RETURNED) {

            throw new BusinessException(
                    "This book has already been returned"
            );
        }

        // 3. Update the borrowing transaction
        transaction.setStatus(BorrowingStatus.RETURNED);
        transaction.setReturnDate(LocalDate.now());

        // 4. Make the book available again
        Book book = transaction.getBook();

        book.setAvailable(true);

        bookRepository.save(book);

        // 5. Save the updated transaction
        BorrowingTransaction updatedTransaction =
                borrowingRepository.save(transaction);

        log.info(
                "Borrowing transaction ID {} returned successfully",
                transactionId
        );

        return mapToResponse(updatedTransaction);
    }

    // Convert BorrowingTransaction entity into BorrowingResponseDto
    private BorrowingResponseDto mapToResponse(
            BorrowingTransaction transaction) {

        BorrowingResponseDto response =
                new BorrowingResponseDto();

        response.setId(transaction.getId());

        response.setBookId(transaction.getBook().getId());
        response.setBookTitle(transaction.getBook().getTitle());

        response.setBorrowerId(transaction.getBorrower().getId());
        response.setBorrowerName(
                transaction.getBorrower().getName()
        );

        response.setBorrowDate(transaction.getBorrowDate());
        response.setReturnDate(transaction.getReturnDate());

        response.setStatus(transaction.getStatus());

        return response;
    }
}
