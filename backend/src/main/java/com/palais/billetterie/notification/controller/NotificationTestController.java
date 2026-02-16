package com.palais.billetterie.notification.controller;

import com.palais.billetterie.notification.service.EmailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationTestController {

    private final EmailService emailService;

    public NotificationTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/test-email")
    public String sendTestEmail(@RequestParam("to") String to) {
        emailService.send(to, "Test Email", "Votre plateforme de billetterie fonctionne !");
        return "Email envoyé";
    }
}
