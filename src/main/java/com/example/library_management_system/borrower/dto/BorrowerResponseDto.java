
package com.example.library_management_system.borrower.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BorrowerResponseDto {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
}
