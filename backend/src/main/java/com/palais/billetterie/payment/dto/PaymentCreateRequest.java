package com.palais.billetterie.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class PaymentCreateRequest {
    @NotNull(message = "Commande requise")
    private UUID orderId;
    @NotNull(message = "Montant requis")
    @Positive(message = "Le montant doit être > 0")
    private Double amount;
    private String provider; // optionnel, ex: STRIPE

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
}
