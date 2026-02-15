package com.palais.billetterie.order.controller;

import com.palais.billetterie.common.exceptions.BadRequestException;
import com.palais.billetterie.event.domain.Event;
import com.palais.billetterie.event.repository.EventRepository;
import com.palais.billetterie.order.domain.Order;
import com.palais.billetterie.order.domain.OrderStatus;
import com.palais.billetterie.order.repository.OrderRepository;
import com.palais.billetterie.user.domain.User;
import com.palais.billetterie.user.repository.UserRepository;
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
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Object evIdObj = body.get("eventId");
        Object qtyObj = body.getOrDefault("quantity", 1);
        if (evIdObj == null) throw new BadRequestException("eventId requis");

        UUID eventId = UUID.fromString(String.valueOf(evIdObj));
        int quantity;
        try {
            quantity = Integer.parseInt(String.valueOf(qtyObj));
        } catch (Exception e) {
            throw new BadRequestException("quantity invalide");
        }
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
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        orderRepository.save(order);

        return ResponseEntity.ok(Map.of(
                "orderId", order.getId(),
                "status", order.getStatus(),
                "amount", order.getAmount(),
                "quantity", quantity
        ));
    }

    @GetMapping
    public ResponseEntity<List<Order>> listForUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new BadRequestException("Utilisateur non authentifié");
        User user = userRepository.findByEmail(auth.getName()).orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));
        List<Order> orders = orderRepository.findAllByUser_Id(user.getId());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable("id") UUID id) {
        Order order = orderRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(order);
    }
}