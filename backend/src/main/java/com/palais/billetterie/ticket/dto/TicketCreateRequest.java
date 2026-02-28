package com.palais.billetterie.ticket.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class TicketCreateRequest {
    @NotNull(message = "Utilisateur requis")
    private UUID userId;
    @NotNull(message = "Événement requis")
    private UUID eventId;
    @NotNull(message = "Commande requise")
    private UUID orderId;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
}
