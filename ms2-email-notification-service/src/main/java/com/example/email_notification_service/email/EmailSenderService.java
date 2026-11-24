package com.example.email_notification_service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailSenderService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:no-reply@example.com}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void send(EmailRequest request) {
        log.info("Email sending started");

        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(fromAddress);
        email.setTo(request.email());
        email.setSubject("Library borrowing notification");
        email.setText(request.message());

        try {
            mailSender.send(email);
            log.info("Email sent successfully");
        }
        catch (MailException exception) {
            log.error("Email delivery failed", exception);
            throw new EmailDeliveryException("Email delivery is temporarily unavailable", exception);
        }
    }
}
