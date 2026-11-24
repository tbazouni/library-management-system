
package com.example.library_management_system.borrowing.dto;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BorrowingRequestDto {

    @NotNull(message = "Borrower ID is required")
    private Long borrowerId;
}
