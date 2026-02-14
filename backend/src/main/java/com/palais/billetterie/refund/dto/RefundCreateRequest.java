package com.palais.billetterie.refund.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class RefundCreateRequest {
    @NotNull(message = "Paiement requis")
    private UUID paymentId;
    @NotNull(message = "Montant requis")
    @Positive(message = "Le montant doit être > 0")
    private Double amount;

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
