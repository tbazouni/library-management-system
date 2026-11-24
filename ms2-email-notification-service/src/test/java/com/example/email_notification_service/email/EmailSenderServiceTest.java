package com.example.email_notification_service.email;

import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailSenderServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);

    @Test
    void sendsRequestRecipientAndMessageThroughSpringMailSender() {
        EmailSenderService service = new EmailSenderService(mailSender, "library@example.com");

        service.send(new EmailRequest("reader@example.com", "Book Clean Code borrowed successfully"));

        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertEquals("library@example.com", captor.getValue().getFrom());
        assertEquals("reader@example.com", captor.getValue().getTo()[0]);
        assertEquals("Book Clean Code borrowed successfully", captor.getValue().getText());
    }

    @Test
    void wrapsMailProviderFailureForServiceUnavailableResponse() {
        EmailSenderService service = new EmailSenderService(mailSender, "library@example.com");
        MailSendException failure = new MailSendException("SMTP unavailable");
        doThrow(failure).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(
                EmailDeliveryException.class,
                () -> service.send(new EmailRequest("reader@example.com", "borrowed"))
        );
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
