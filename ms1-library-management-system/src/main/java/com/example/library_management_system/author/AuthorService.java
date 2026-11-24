
package com.example.library_management_system.author;

import com.example.library_management_system.author.dto.AuthorRequestDto;
import com.example.library_management_system.author.dto.AuthorResponseDto;
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
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorResponseDto createAuthor(AuthorRequestDto request) {

        Author author = new Author();

        author.setName(request.getName());
        author.setBiography(request.getBiography());

        Author savedAuthor = authorRepository.save(author);

        log.info(
                "Author created successfully with ID: {}",
                savedAuthor.getId()
        );

        return mapToResponse(savedAuthor);
    }

    @Transactional(readOnly = true)
    public List<AuthorResponseDto> getAllAuthors() {

        return authorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuthorResponseDto getAuthorById(Long id) {

        Author author = findAuthorById(id);

        return mapToResponse(author);
    }

    public AuthorResponseDto updateAuthor(
            Long id,
            AuthorRequestDto request) {

        Author author = findAuthorById(id);

        author.setName(request.getName());
        author.setBiography(request.getBiography());

        Author updatedAuthor = authorRepository.save(author);

        log.info("Author updated successfully with ID: {}", id);

        return mapToResponse(updatedAuthor);
    }

    public void deleteAuthor(Long id) {

        Author author = findAuthorById(id);

        authorRepository.delete(author);

        log.info("Author deleted successfully with ID: {}", id);
    }

    private Author findAuthorById(Long id) {

        return authorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Author not found with ID: " + id
                        )
                );
    }

    private AuthorResponseDto mapToResponse(Author author) {

        AuthorResponseDto response = new AuthorResponseDto();

        response.setId(author.getId());
        response.setName(author.getName());
        response.setBiography(author.getBiography());

        return response;
    }
}
