package com.example.email_notification_service.email;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmailNotificationHttpIntegrationTest {

    private static final SmtpTestInbox smtpInbox = new SmtpTestInbox();

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configureSmtp(DynamicPropertyRegistry properties) {
        properties.add("spring.mail.host", () -> "127.0.0.1");
        properties.add("spring.mail.port", smtpInbox::port);
        properties.add("spring.mail.username", () -> "");
        properties.add("spring.mail.password", () -> "");
        properties.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        properties.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
        properties.add("spring.mail.properties.mail.smtp.connectiontimeout", () -> "1000");
        properties.add("spring.mail.properties.mail.smtp.timeout", () -> "1000");
        properties.add("spring.mail.properties.mail.smtp.writetimeout", () -> "1000");
    }

    @AfterAll
    static void closeSmtpInbox() throws IOException {
        smtpInbox.close();
    }

    @Test
    void acceptsHttpRequestAndDeliversMessageToSmtpTestInbox() throws Exception {
        HttpResponse<String> response = postEmail(
                "reader@example.com",
                "Book Clean Code borrowed successfully"
        );

        assertEquals(200, response.statusCode());
        String deliveredEmail = smtpInbox.awaitMessage(Duration.ofSeconds(5));
        assertNotNull(deliveredEmail, "Expected MS2 to deliver mail to the test inbox");
        assertTrue(deliveredEmail.contains("reader@example.com"));
        assertTrue(deliveredEmail.contains("Book Clean Code borrowed successfully"));
    }

    @Test
    void rejectsInvalidRecipientOverHttp() throws Exception {
        HttpResponse<String> response = postEmail("not-an-email", "Validation test");

        assertEquals(400, response.statusCode());
    }

    @Test
    void rejectsBlankMessageOverHttp() throws Exception {
        HttpResponse<String> response = postEmail("reader@example.com", " ");

        assertEquals(400, response.statusCode());
    }

    private HttpResponse<String> postEmail(String email, String message) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"message\":\"" + message + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/send-email"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return HttpClient.newHttpClient().send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private static final class SmtpTestInbox implements AutoCloseable {

        private final ServerSocket serverSocket;
        private final LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
        private final Thread serverThread;

        private SmtpTestInbox() {
            try {
                serverSocket = new ServerSocket(
                        0,
                        1,
                        InetAddress.getByName("127.0.0.1")
                );
            }
            catch (IOException exception) {
                throw new IllegalStateException("Unable to start SMTP test inbox", exception);
            }

            serverThread = new Thread(this::acceptOneMessage, "smtp-test-inbox");
            serverThread.setDaemon(true);
            serverThread.start();
        }

        private int port() {
            return serverSocket.getLocalPort();
        }

        private String awaitMessage(Duration timeout) throws InterruptedException {
            return messages.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }

        private void acceptOneMessage() {
            try (Socket socket = serverSocket.accept();
                 BufferedReader input = new BufferedReader(new InputStreamReader(
                         socket.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                         socket.getOutputStream(), StandardCharsets.UTF_8))) {
                reply(output, "220 localhost test inbox ready");
                String line;
                while ((line = input.readLine()) != null) {
                    String command = line.toUpperCase();
                    if (command.startsWith("EHLO") || command.startsWith("HELO")) {
                        reply(output, "250-localhost");
                        reply(output, "250 SIZE 10000000");
                    }
                    else if (command.startsWith("MAIL FROM")
                            || command.startsWith("RCPT TO")
                            || command.startsWith("RSET")) {
                        reply(output, "250 OK");
                    }
                    else if (command.equals("DATA")) {
                        reply(output, "354 End data with <CR><LF>.<CR><LF>");
                        StringBuilder message = new StringBuilder();
                        while ((line = input.readLine()) != null && !line.equals(".")) {
                            message.append(line).append('\n');
                        }
                        messages.offer(message.toString());
                        reply(output, "250 Message accepted into test inbox");
                    }
                    else if (command.equals("QUIT")) {
                        reply(output, "221 Bye");
                        return;
                    }
                    else {
                        reply(output, "250 OK");
                    }
                }
            }
            catch (IOException exception) {
                if (!serverSocket.isClosed()) {
                    throw new IllegalStateException("SMTP test inbox failed", exception);
                }
            }
        }

        private static void reply(BufferedWriter output, String response) throws IOException {
            output.write(response);
            output.write("\r\n");
            output.flush();
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
        }
    }
}
