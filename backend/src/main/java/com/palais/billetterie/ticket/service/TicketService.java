package com.palais.billetterie.ticket.service;

import com.palais.billetterie.ticket.domain.Ticket;
import com.palais.billetterie.ticket.domain.TicketStatus;
import com.palais.billetterie.ticket.repository.TicketRepository;
import com.palais.billetterie.common.exceptions.BadRequestException;
import com.palais.billetterie.user.domain.User;
import com.palais.billetterie.user.repository.UserRepository;
import com.palais.billetterie.event.domain.Event;
import com.palais.billetterie.event.repository.EventRepository;
import com.palais.billetterie.order.domain.Order;
import com.palais.billetterie.order.repository.OrderRepository;
import com.palais.billetterie.notification.service.EmailService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TicketService {
        private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository repository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final OrderRepository orderRepository;
        private final EmailService emailService;

        public TicketService(TicketRepository repository,
                                                 UserRepository userRepository,
                                                 EventRepository eventRepository,
                                                 OrderRepository orderRepository,
                                                 EmailService emailService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.orderRepository = orderRepository;
                this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<Ticket> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Ticket getById(UUID id) {
        if (id == null) throw new BadRequestException("Identifiant de ticket manquant");
        return repository.findById(id)
                .orElseThrow(() -> new BadRequestException("Ticket introuvable"));
    }

    @Transactional
    public Ticket create(UUID userId, UUID eventId, UUID orderId) {
        if (userId == null) throw new BadRequestException("Utilisateur requis");
        if (eventId == null) throw new BadRequestException("Événement requis");
        if (orderId == null) throw new BadRequestException("Commande requise");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BadRequestException("Événement introuvable"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException("Commande introuvable"));

        Ticket ticket = Ticket.builder()
                .user(user)
                .event(event)
                .order(order)
                .code(UUID.randomUUID().toString())
                .status(TicketStatus.VALID)
                .createdAt(Instant.now())
                .build();

                Objects.requireNonNull(ticket, "ticket must not be null");
                ticket = repository.save(ticket);

                // Envoi email de ticket
                try {
                        String subject = "Votre ticket";
                        String body = "Bonjour " + user.getName() + ",\n\n" +
                                        "Voici votre ticket pour l'événement \"" + event.getTitle() + "\".\n" +
                                        "Code ticket : " + ticket.getCode() + "\n" +
                                        "Commande : " + order.getId() + "\n\n" +
                                        "Merci et à bientôt !";
                        emailService.send(user.getEmail(), subject, body);
                } catch (Exception ex) {
                        log.warn("Échec envoi email ticket à {}: {}", user.getEmail(), ex.getMessage());
                }

                return ticket;
    }

        @Transactional
        public Ticket update(UUID id, TicketStatus status) {
                if (id == null) throw new BadRequestException("Identifiant de ticket manquant");
                if (status == null) throw new BadRequestException("Statut requis");
                Ticket existing = repository.findById(id)
                                .orElseThrow(() -> new BadRequestException("Ticket introuvable"));
                existing.setStatus(status);
                Objects.requireNonNull(existing, "ticket must not be null");
                return repository.save(existing);
        }

        @Transactional
        public void delete(UUID id) {
                if (id == null) throw new BadRequestException("Identifiant de ticket manquant");
                Ticket existing = repository.findById(id)
                                .orElseThrow(() -> new BadRequestException("Ticket introuvable"));
                Objects.requireNonNull(existing, "ticket must not be null");
                repository.delete(existing);
        }
}