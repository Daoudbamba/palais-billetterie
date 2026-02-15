package com.palais.billetterie.payment.dto;

import com.palais.billetterie.payment.domain.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public class PaymentUpdateRequest {
    @NotNull(message = "Statut requis")
    private PaymentStatus status;
    private String providerPaymentId;

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getProviderPaymentId() { return providerPaymentId; }
    public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }
}
