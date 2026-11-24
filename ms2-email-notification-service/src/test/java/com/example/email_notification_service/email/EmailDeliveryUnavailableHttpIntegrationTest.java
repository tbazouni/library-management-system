package com.example.email_notification_service.email;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmailDeliveryUnavailableHttpIntegrationTest {

    private static final int CLOSED_SMTP_PORT = reserveThenReleasePort();

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configureUnavailableSmtp(DynamicPropertyRegistry properties) {
        properties.add("spring.mail.host", () -> "127.0.0.1");
        properties.add("spring.mail.port", () -> CLOSED_SMTP_PORT);
        properties.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        properties.add("spring.mail.properties.mail.smtp.connectiontimeout", () -> "500");
        properties.add("spring.mail.properties.mail.smtp.timeout", () -> "500");
        properties.add("spring.mail.properties.mail.smtp.writetimeout", () -> "500");
    }

    @Test
    void returnsServiceUnavailableWhenSmtpCannotBeReached() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/send-email"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"email\":\"reader@example.com\",\"message\":\"Borrowed\"}"
                ))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(503, response.statusCode());
        assertTrue(response.body().contains("Email delivery is temporarily unavailable"));
    }

    private static int reserveThenReleasePort() {
        try (ServerSocket socket = new ServerSocket(
                0,
                1,
                InetAddress.getByName("127.0.0.1")
        )) {
            return socket.getLocalPort();
        }
        catch (IOException exception) {
            throw new IllegalStateException("Unable to reserve an unused SMTP port", exception);
        }
    }
}
