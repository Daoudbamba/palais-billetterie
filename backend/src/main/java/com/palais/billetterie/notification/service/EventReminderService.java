package com.palais.billetterie.notification.service;

import com.palais.billetterie.event.domain.Event;
import com.palais.billetterie.event.repository.EventRepository;
import com.palais.billetterie.ticket.domain.Ticket;
import com.palais.billetterie.ticket.domain.TicketStatus;
import com.palais.billetterie.ticket.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EventReminderService {

    private static final Logger log = LoggerFactory.getLogger(EventReminderService.class);

    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final EmailService emailService;

    @Value("${app.notification.reminderHours:24}")
    private int reminderHours;

    public EventReminderService(EventRepository eventRepository,
                                TicketRepository ticketRepository,
                                EmailService emailService) {
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.emailService = emailService;
    }

    // Run every hour at minute 0
    @Scheduled(cron = "0 0 * * * *")
    public void sendUpcomingEventReminders() {
        Instant now = Instant.now();
        Instant windowEnd = now.plus(Duration.ofHours(reminderHours));

        List<Event> upcoming = eventRepository.findByStartDateTimeBetween(now, windowEnd);
        if (upcoming.isEmpty()) {
            log.debug("No upcoming events within {} hours", reminderHours);
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

        for (Event event : upcoming) {
            List<Ticket> tickets = ticketRepository.findByEvent_IdAndStatus(event.getId(), TicketStatus.VALID);
            if (tickets.isEmpty()) continue;

            String subject = "Rappel événement : " + event.getTitle();
            String when = fmt.format(event.getStartDateTime());
            for (Ticket t : tickets) {
                String email = t.getUser().getEmail();
                String body = "Bonjour " + t.getUser().getName() + ",\n\n" +
                        "Votre événement \"" + event.getTitle() + "\" démarre le " + when + ".\n" +
                        "Lieu : " + (event.getVenue() == null ? "(à préciser)" : event.getVenue()) + "\n" +
                        "Code ticket : " + t.getCode() + "\n\n" +
                        "À très vite !";
                try {
                    emailService.send(email, subject, body);
                } catch (Exception ex) {
                    log.warn("Failed to send reminder to {} for event {}: {}", email, event.getId(), ex.getMessage());
                }
            }
        }
    }
}
