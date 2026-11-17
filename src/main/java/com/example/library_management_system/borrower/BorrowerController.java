
package com.example.library_management_system.borrower;

import com.example.library_management_system.borrower.dto.BorrowerRequestDto;
import com.example.library_management_system.borrower.dto.BorrowerResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowers")
@RequiredArgsConstructor
public class BorrowerController {

    private final BorrowerService borrowerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowerResponseDto createBorrower(
            @Valid @RequestBody BorrowerRequestDto request) {

        return borrowerService.createBorrower(request);
    }

    @GetMapping
    public List<BorrowerResponseDto> getAllBorrowers() {
        return borrowerService.getAllBorrowers();
    }

    @GetMapping("/{id}")
    public BorrowerResponseDto getBorrowerById(@PathVariable Long id) {
        return borrowerService.getBorrowerById(id);
    }

    @PutMapping("/{id}")
    public BorrowerResponseDto updateBorrower(@PathVariable Long id,
            @Valid @RequestBody BorrowerRequestDto request) {

        return borrowerService.updateBorrower(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBorrower(@PathVariable Long id) {

        borrowerService.deleteBorrower(id);
    }
}
