package com.palais.billetterie.order.service;

import com.palais.billetterie.order.domain.Order;
import com.palais.billetterie.order.domain.OrderStatus;
import com.palais.billetterie.order.repository.OrderRepository;
import com.palais.billetterie.user.domain.User;
import com.palais.billetterie.user.repository.UserRepository;
import com.palais.billetterie.event.domain.Event;
import com.palais.billetterie.event.repository.EventRepository;
import com.palais.billetterie.common.exceptions.BadRequestException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public OrderService(OrderRepository repository,
                        UserRepository userRepository,
                        EventRepository eventRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<Order> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> getAllForUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));
        return repository.findAllByUser_Id(user.getId());
    }

    @Transactional(readOnly = true)
    public Order getById(UUID id) {
        UUID nonNullId = Objects.requireNonNull(id, "id ne doit pas être nul");
        return repository.findById(nonNullId)
                .orElseThrow(() -> new BadRequestException("Commande introuvable"));
    }

    @Transactional(readOnly = true)
    public Order getByIdForUserEmail(String email, UUID id) {
        UUID nonNullId = Objects.requireNonNull(id, "id ne doit pas être nul");
        Order order = repository.findById(nonNullId)
                .orElseThrow(() -> new BadRequestException("Commande introuvable"));
        if (!order.getUser().getEmail().equals(email)) {
            throw new BadRequestException("Accès refusé à cette commande");
        }
        return order;
    }

    @Transactional
        public Order create(UUID userId, UUID eventId, Double amount) {
        UUID nonNullUserId = Objects.requireNonNull(userId, "userId ne doit pas être nul");
        UUID nonNullEventId = Objects.requireNonNull(eventId, "eventId ne doit pas être nul");
        if (amount == null || amount <= 0) {
            throw new BadRequestException("Le montant doit être supérieur à 0");
        }

        User user = userRepository.findById(nonNullUserId)
            .orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));

        Event event = eventRepository.findById(nonNullEventId)
            .orElseThrow(() -> new BadRequestException("Événement introuvable"));

        Order order = Order.builder()
                .user(user)
                .event(event)
                .amount(amount)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        return repository.save(Objects.requireNonNull(order));
    }

    @Transactional
    public Order update(UUID id, Double amount, OrderStatus status) {
        UUID nonNullId = Objects.requireNonNull(id, "id ne doit pas être nul");
        if (amount == null || amount <= 0) {
            throw new BadRequestException("Le montant doit être supérieur à 0");
        }
        if (status == null) {
            throw new BadRequestException("Le statut est obligatoire");
        }

        Order existing = repository.findById(nonNullId)
                .orElseThrow(() -> new BadRequestException("Commande introuvable"));
        existing.setAmount(amount);
        existing.setStatus(status);
        return repository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        UUID nonNullId = Objects.requireNonNull(id, "id ne doit pas être nul");
        if (!repository.existsById(nonNullId)) {
            throw new BadRequestException("Commande introuvable");
        }
        repository.deleteById(nonNullId);
    }
}