
package com.example.library_management_system.borrowing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowingRepository
        extends JpaRepository<BorrowingTransaction, Long> {
}
