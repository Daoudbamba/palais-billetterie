package com.palais.billetterie.refund.domain;

import com.palais.billetterie.payment.domain.Payment;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "refunds",
    indexes = {
        @Index(name = "idx_refunds_status_createdat", columnList = "status, createdAt"),
        @Index(name = "idx_refunds_createdat", columnList = "createdAt"),
        @Index(name = "idx_refunds_payment_id", columnList = "payment_id")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Refund {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Payment payment;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    private String provider; // STRIPE

    private String providerRefundId; // id du refund Stripe

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}