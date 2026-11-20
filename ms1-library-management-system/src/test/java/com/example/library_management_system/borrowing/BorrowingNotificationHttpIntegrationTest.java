package com.example.library_management_system.borrowing;

import com.example.library_management_system.author.Author;
import com.example.library_management_system.author.AuthorRepository;
import com.example.library_management_system.book.Book;
import com.example.library_management_system.book.BookCategory;
import com.example.library_management_system.book.BookRepository;
import com.example.library_management_system.borrower.Borrower;
import com.example.library_management_system.borrower.BorrowerRepository;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BorrowingNotificationHttpIntegrationTest {

    private static final AtomicReference<String> notificationBody = new AtomicReference<>();
    private static final HttpServer notificationServer = startNotificationServer();

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configureNotificationService(DynamicPropertyRegistry properties) {
        properties.add(
                "email.service.url",
                () -> "http://127.0.0.1:" + notificationServer.getAddress().getPort()
        );
    }

    @AfterAll
    static void stopNotificationServer() {
        notificationServer.stop(0);
    }

    @Test
    void successfulBorrowCallsNotificationServiceOverHttp() throws Exception {
        Author author = new Author();
        author.setName("Part Three Integration Author");
        authorRepository.saveAndFlush(author);

        Book book = new Book();
        book.setTitle("Part Three Integration Book");
        book.setIsbn("part-three-integration-" + System.nanoTime());
        book.setCategory(BookCategory.FICTION);
        book.setAuthor(author);
        book.setAvailable(true);
        bookRepository.saveAndFlush(book);

        Borrower borrower = new Borrower();
        borrower.setName("Part Three Integration Borrower");
        borrower.setEmail("part-three-" + System.nanoTime() + "@example.com");
        borrower.setPhoneNumber("+96170000000");
        borrowerRepository.saveAndFlush(borrower);

        String body = "{\"borrowerId\":" + borrower.getId() + "}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:" + port + "/api/borrowings/books/" + book.getId()
                ))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(201, response.statusCode());
        String notification = notificationBody.get();
        assertTrue(notification.contains(borrower.getEmail()));
        assertTrue(notification.contains("Book Part Three Integration Book borrowed successfully"));
    }

    private static HttpServer startNotificationServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/send-email", exchange -> {
                notificationBody.set(new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                ));
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
            });
            server.start();
            return server;
        }
        catch (IOException exception) {
            throw new IllegalStateException("Unable to start notification HTTP test server", exception);
        }
    }
}
