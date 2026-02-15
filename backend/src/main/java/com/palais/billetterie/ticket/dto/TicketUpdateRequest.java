package com.palais.billetterie.ticket.dto;

import com.palais.billetterie.ticket.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

public class TicketUpdateRequest {
    @NotNull(message = "Statut requis")
    private TicketStatus status;

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
}
