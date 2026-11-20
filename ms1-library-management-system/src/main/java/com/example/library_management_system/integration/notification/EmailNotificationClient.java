package com.example.library_management_system.integration.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "email-notification-service",
        url = "${email.service.url:http://localhost:8081}"
)
public interface EmailNotificationClient {

    @PostMapping("/send-email")
    void sendEmail(@RequestBody EmailNotificationRequest request);
}
