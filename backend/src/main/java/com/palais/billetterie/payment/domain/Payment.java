package com.palais.billetterie.payment.domain;

import com.palais.billetterie.order.domain.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payments_status_createdat", columnList = "status, createdAt"),
        @Index(name = "idx_payments_createdat", columnList = "createdAt"),
        @Index(name = "idx_payments_order_id", columnList = "order_id")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Order order;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String provider; // ex: STRIPE

    private String providerPaymentId; // ex: Stripe PaymentIntent ID

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
 