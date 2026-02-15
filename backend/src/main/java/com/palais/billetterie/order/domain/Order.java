package com.palais.billetterie.order.domain;

import com.palais.billetterie.user.domain.User;
import com.palais.billetterie.event.domain.Event;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_orders_status_createdat", columnList = "status, createdAt"),
        @Index(name = "idx_orders_createdat", columnList = "createdAt"),
        @Index(name = "idx_orders_event_id", columnList = "event_id"),
        @Index(name = "idx_orders_user_id", columnList = "user_id")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Event event;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
 