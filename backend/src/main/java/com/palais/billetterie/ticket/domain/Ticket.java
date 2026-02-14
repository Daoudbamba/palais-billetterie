package com.palais.billetterie.ticket.domain;

import com.palais.billetterie.user.domain.User;
import com.palais.billetterie.event.domain.Event;
import com.palais.billetterie.order.domain.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "tickets",
    indexes = {
        @Index(name = "idx_tickets_status_createdat", columnList = "status, createdAt"),
        @Index(name = "idx_tickets_createdat", columnList = "createdAt"),
        @Index(name = "idx_tickets_event_id", columnList = "event_id"),
        @Index(name = "idx_tickets_order_id", columnList = "order_id"),
        @Index(name = "idx_tickets_user_id", columnList = "user_id")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Event event;

    @ManyToOne(optional = false)
    private Order order;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}