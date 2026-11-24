package com.example.library_management_system.borrowing;

import com.example.library_management_system.book.Book;
import com.example.library_management_system.book.BookRepository;
import com.example.library_management_system.borrower.Borrower;
import com.example.library_management_system.borrower.BorrowerRepository;
import com.example.library_management_system.borrowing.dto.BorrowingRequestDto;
import com.example.library_management_system.exception.BusinessException;
import com.example.library_management_system.integration.notification.EmailNotificationClient;
import com.example.library_management_system.integration.notification.EmailNotificationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BorrowingServiceNotificationTest {

    private final BorrowingRepository borrowingRepository = mock(BorrowingRepository.class);
    private final BookRepository bookRepository = mock(BookRepository.class);
    private final BorrowerRepository borrowerRepository = mock(BorrowerRepository.class);
    private final EmailNotificationClient emailNotificationClient = mock(EmailNotificationClient.class);

    private BorrowingService borrowingService;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        borrowingService = new BorrowingService(
                borrowingRepository,
                bookRepository,
                borrowerRepository,
                emailNotificationClient
        );
        ReflectionTestUtils.setField(borrowingService, "transactionLimit", 5);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clear();
    }

    @Test
    void sendsBorrowerEmailAndBookTitleAfterTransactionCommit() {
        Book book = new Book();
        book.setId(10L);
        book.setTitle("Clean Code");
        book.setAvailable(true);

        Borrower borrower = new Borrower();
        borrower.setId(20L);
        borrower.setEmail("reader@example.com");

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(20L)).thenReturn(Optional.of(borrower));
        when(borrowingRepository.countByBorrowerIdAndStatus(20L, BorrowingStatus.BORROWED))
                .thenReturn(0L);
        when(borrowingRepository.save(any(BorrowingTransaction.class)))
                .thenAnswer(invocation -> {
                    BorrowingTransaction transaction = invocation.getArgument(0);
                    transaction.setId(30L);
                    return transaction;
                });

        BorrowingRequestDto request = new BorrowingRequestDto();
        request.setBorrowerId(20L);
        borrowingService.borrowBook(10L, request);

        verify(emailNotificationClient, never()).sendEmail(any());
        for (TransactionSynchronization synchronization :
                TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(
                EmailNotificationRequest.class
        );
        verify(emailNotificationClient).sendEmail(requestCaptor.capture());
        assertEquals("reader@example.com", requestCaptor.getValue().email());
        assertEquals(
                "Book Clean Code borrowed successfully",
                requestCaptor.getValue().message()
        );
    }

    @Test
    void doesNotScheduleNotificationWhenBookCannotBeBorrowed() {
        Book book = new Book();
        book.setId(10L);
        book.setTitle("Clean Code");
        book.setAvailable(false);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        BorrowingRequestDto request = new BorrowingRequestDto();
        request.setBorrowerId(20L);

        assertThrows(
                BusinessException.class,
                () -> borrowingService.borrowBook(10L, request)
        );
        verify(emailNotificationClient, never()).sendEmail(any());
        assertTrue(
                TransactionSynchronizationManager.getSynchronizations().isEmpty()
        );
    }

    @Test
    void keepsCommittedBorrowSuccessfulWhenNotificationServiceIsUnavailable() {
        Book book = new Book();
        book.setId(10L);
        book.setTitle("Clean Code");
        book.setAvailable(true);

        Borrower borrower = new Borrower();
        borrower.setId(20L);
        borrower.setEmail("reader@example.com");

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(20L)).thenReturn(Optional.of(borrower));
        when(borrowingRepository.countByBorrowerIdAndStatus(20L, BorrowingStatus.BORROWED))
                .thenReturn(0L);
        when(borrowingRepository.save(any(BorrowingTransaction.class)))
                .thenAnswer(invocation -> {
                    BorrowingTransaction transaction = invocation.getArgument(0);
                    transaction.setId(30L);
                    return transaction;
                });
        doThrow(new RuntimeException("MS2 is unavailable"))
                .when(emailNotificationClient)
                .sendEmail(any());

        BorrowingRequestDto request = new BorrowingRequestDto();
        request.setBorrowerId(20L);
        borrowingService.borrowBook(10L, request);

        assertDoesNotThrow(() -> TransactionSynchronizationManager
                .getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit));
        verify(emailNotificationClient).sendEmail(any());
    }
}
