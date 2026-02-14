package com.palais.billetterie.scan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ScanRequest {
    @NotBlank(message = "Code ticket requis")
    public String code;

    // Champs optionnels pour QR signé
    // Horodatage Unix (ms) pour limiter la fenêtre temporelle
    public Long ts;

    // Nonce anti-rejeu
    @Size(max = 128, message = "Nonce trop long")
    public String nonce;

    // Signature HMAC hex de la concaténation (code|eventId|ts|nonce)
    @Size(max = 256, message = "Signature trop longue")
    public String sig;
}
