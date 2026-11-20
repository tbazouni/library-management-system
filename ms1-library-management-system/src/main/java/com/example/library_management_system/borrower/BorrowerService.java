
package com.example.library_management_system.borrower;

import com.example.library_management_system.borrower.dto.BorrowerRequestDto;
import com.example.library_management_system.borrower.dto.BorrowerResponseDto;
import com.example.library_management_system.exception.BusinessException;
import com.example.library_management_system.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;

    public BorrowerResponseDto createBorrower(
            BorrowerRequestDto request) {

        if (borrowerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "A borrower with this email already exists"
            );
        }

        Borrower borrower = new Borrower();

        borrower.setName(request.getName());
        borrower.setEmail(request.getEmail());
        borrower.setPhoneNumber(request.getPhoneNumber());

        Borrower savedBorrower = borrowerRepository.save(borrower);

        log.info(
                "Borrower created successfully with ID: {}",
                savedBorrower.getId()
        );

        return mapToResponse(savedBorrower);
    }

    @Transactional(readOnly = true)
    public List<BorrowerResponseDto> getAllBorrowers() {

        return borrowerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BorrowerResponseDto getBorrowerById(Long id) {

        Borrower borrower = findBorrowerById(id);

        return mapToResponse(borrower);
    }

    public BorrowerResponseDto updateBorrower(
            Long id,
            BorrowerRequestDto request) {

        Borrower borrower = findBorrowerById(id);

        if (!borrower.getEmail().equals(request.getEmail())
                && borrowerRepository.existsByEmail(request.getEmail())) {

            throw new BusinessException(
                    "A borrower with this email already exists"
            );
        }

        borrower.setName(request.getName());
        borrower.setEmail(request.getEmail());
        borrower.setPhoneNumber(request.getPhoneNumber());

        Borrower updatedBorrower = borrowerRepository.save(borrower);

        log.info("Borrower updated successfully with ID: {}", id);

        return mapToResponse(updatedBorrower);
    }

    public void deleteBorrower(Long id) {

        Borrower borrower = findBorrowerById(id);

        borrowerRepository.delete(borrower);

        log.info("Borrower deleted successfully with ID: {}", id);
    }

    private Borrower findBorrowerById(Long id) {

        return borrowerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Borrower not found with ID: " + id
                        )
                );
    }

    private BorrowerResponseDto mapToResponse(Borrower borrower) {

        BorrowerResponseDto response = new BorrowerResponseDto();

        response.setId(borrower.getId());
        response.setName(borrower.getName());
        response.setEmail(borrower.getEmail());
        response.setPhoneNumber(borrower.getPhoneNumber());

        return response;
    }
}
