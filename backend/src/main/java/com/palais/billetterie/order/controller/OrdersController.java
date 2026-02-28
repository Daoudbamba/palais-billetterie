package com.palais.billetterie.order.controller;

import com.palais.billetterie.common.exceptions.BadRequestException;
import com.palais.billetterie.event.domain.Event;
import com.palais.billetterie.event.repository.EventRepository;
import com.palais.billetterie.order.domain.Order;
import com.palais.billetterie.order.domain.OrderStatus;
import com.palais.billetterie.order.dto.CreateOrderRequest;
import com.palais.billetterie.order.dto.OrderResponse;
import com.palais.billetterie.order.dto.UpdateOrderRequest;
import com.palais.billetterie.order.repository.OrderRepository;
import com.palais.billetterie.user.domain.User;
import com.palais.billetterie.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public OrdersController(OrderRepository orderRepository,
                            UserRepository userRepository,
                            EventRepository eventRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        UUID eventId = request.getEventId();
        if (eventId == null) throw new BadRequestException("eventId requis");
        int quantity = request.getQuantity() == null ? 1 : request.getQuantity();
        if (quantity < 1) throw new BadRequestException("quantity doit être >= 1");

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new BadRequestException("Événement introuvable"));
        if (event.getCapacity() != null && quantity > event.getCapacity()) {
            throw new BadRequestException("Quantité dépasse la capacité de l'événement");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new BadRequestException("Utilisateur non authentifié");
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));

        double unitPrice = 10.0; // mode dev: prix unitaire fixe
        double amount = unitPrice * quantity;

        Order order = Order.builder()
                .user(user)
                .event(event)
                .amount(amount)
            .quantity(quantity)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        Order saved = orderRepository.save(order);

        return ResponseEntity.ok(OrderResponse.from(saved));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> listForUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new BadRequestException("Utilisateur non authentifié");
        User user = userRepository.findByEmail(auth.getName()).orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));
        List<Order> orders = orderRepository.findAllByUser_Id(user.getId());
        List<OrderResponse> resp = orders.stream().map(OrderResponse::from).toList();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> get(@PathVariable("id") UUID id) {
        Order order = orderRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable("id") UUID id,
                                                @Valid @RequestBody UpdateOrderRequest request) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new BadRequestException("Commande introuvable"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Seules les commandes en attente peuvent être modifiées");
        }

        int quantity = request.getQuantity() == null ? order.getQuantity() == null ? 1 : order.getQuantity() : request.getQuantity();
        if (quantity < 1) throw new BadRequestException("quantity doit être >= 1");

        Event event = order.getEvent();
        if (event.getCapacity() != null && quantity > event.getCapacity()) {
            throw new BadRequestException("Quantité dépasse la capacité de l'événement");
        }

        double unitPrice = 10.0; // mode dev
        order.setQuantity(quantity);
        order.setAmount(unitPrice * quantity);
        orderRepository.save(order);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable("id") UUID id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new BadRequestException("Commande introuvable"));
        if (order.getStatus() == OrderStatus.PAID) {
            throw new BadRequestException("Impossible d'annuler une commande payée");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return ResponseEntity.ok(OrderResponse.from(order));
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return ResponseEntity.ok(OrderResponse.from(order));
    }
}