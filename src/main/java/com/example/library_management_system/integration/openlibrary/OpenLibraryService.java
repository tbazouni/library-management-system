package com.example.library_management_system.integration.openlibrary;

import com.example.library_management_system.integration.dto.OpenLibraryBookDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenLibraryService {

    private final RestClient openLibraryRestClient;

    public Optional<String> findAuthorNameByIsbn(String isbn) {

        try {

            String isbnKey = "ISBN:" + isbn;

            Map<String, OpenLibraryBookDto> response =
                    openLibraryRestClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/api/books")
                                    .queryParam("bibkeys", isbnKey)
                                    .queryParam("format", "json")
                                    .queryParam("jscmd", "data")
                                    .build())
                            .retrieve()
                            .body(
                                    new ParameterizedTypeReference<
                                            Map<String, OpenLibraryBookDto>
                                            >() {
                                    }
                            );

            if (response == null || response.isEmpty()) {

                log.warn(
                        "No book information found in Open Library for ISBN: {}",
                        isbn
                );

                return Optional.empty();
            }

            OpenLibraryBookDto book = response.get(isbnKey);

            if (book == null
                    || book.getAuthors() == null
                    || book.getAuthors().isEmpty()) {

                log.warn(
                        "No author information found for ISBN: {}",
                        isbn
                );

                return Optional.empty();
            }

            return book.getAuthors()
                    .stream()
                    .map(author -> author.getName())
                    .filter(name -> name != null && !name.isBlank())
                    .findFirst();

        } catch (RestClientException exception) {

            log.error(
                    "Failed to communicate with Open Library for ISBN: {}",
                    isbn,
                    exception
            );

            return Optional.empty();
        }
    }
}