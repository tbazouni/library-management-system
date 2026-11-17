
package com.example.library_management_system.author.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorRequestDto {

    @NotBlank(message = "Author name is required")
    private String name;

    private String biography;
}
