
package com.example.library_management_system.borrower;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowerRepository
        extends JpaRepository<Borrower, Long> {

    boolean existsByEmail(String email);
}
