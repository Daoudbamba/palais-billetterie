package com.palais.billetterie.order.dto;

import com.palais.billetterie.order.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderUpdateRequest {
    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être supérieur à 0")
    private Double amount;

    @NotNull(message = "Le statut est obligatoire")
    private OrderStatus status;
}
