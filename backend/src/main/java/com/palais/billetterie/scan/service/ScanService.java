package com.palais.billetterie.scan.service;

import com.palais.billetterie.common.exceptions.BadRequestException;
import com.palais.billetterie.scan.domain.ScanLog;
import com.palais.billetterie.scan.domain.ScanResult;
import com.palais.billetterie.scan.dto.ScanRequest;
import com.palais.billetterie.scan.dto.ScanResponse;
import com.palais.billetterie.scan.repository.ScanLogRepository;
import com.palais.billetterie.ticket.domain.Ticket;
import com.palais.billetterie.ticket.domain.TicketStatus;
import com.palais.billetterie.ticket.repository.TicketRepository;
import com.palais.billetterie.user.domain.User;
import com.palais.billetterie.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScanService {

    private final TicketRepository ticketRepository;
    private final ScanLogRepository scanLogRepository;
    private final UserRepository userRepository;

    @Value("${scan.qrSecret:changeme-secret}")
    private String qrSecret;

    private final Map<UUID, Deque<Long>> rateMap = new ConcurrentHashMap<>();
    private final Map<String, Long> nonceCache = new ConcurrentHashMap<>();
    private static final long RATE_WINDOW_MS = 30_000L;
    private static final int RATE_MAX = 20;
    private static final Duration SIG_WINDOW = Duration.ofMinutes(10);
    private static final Duration NONCE_TTL = Duration.ofMinutes(60);

    public ScanService(TicketRepository ticketRepository,
                       ScanLogRepository scanLogRepository,
                       UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.scanLogRepository = scanLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ScanResponse verify(ScanRequest req) {
        Ticket ticket = ticketRepository.findByCode(req.code)
                .orElseThrow(() -> new BadRequestException("Ticket introuvable"));

        User controller = getCurrentUser();
        enforceRateLimit(controller);

        // Vérification QR signé (optionnelle si sig présent)
        if (req.sig != null && !req.sig.isBlank()) {
            if (!verifySignature(ticket, req)) {
                logScan(ticket, controller, ScanResult.VERIFIED_FAIL, "Signature QR invalide");
                return ScanResponse.of(false, "Signature QR invalide", ticket.getStatus(), ticket.getEvent().getTitle(), ticket.getUser().getName(), ticket.getCode());
            }
        }

        String msg;
        boolean ok = false;
        if (ticket.getStatus() == TicketStatus.VALID) {
            ok = true;
            msg = "Ticket valable";
            logScan(ticket, controller, ScanResult.VERIFIED_OK, msg);
        } else if (ticket.getStatus() == TicketStatus.USED) {
            msg = "Ticket déjà utilisé";
            logScan(ticket, controller, ScanResult.VERIFIED_FAIL, msg);
        } else {
            msg = "Ticket annulé";
            logScan(ticket, controller, ScanResult.VERIFIED_FAIL, msg);
        }

        return ScanResponse.of(ok, msg, ticket.getStatus(), ticket.getEvent().getTitle(), ticket.getUser().getName(), ticket.getCode());
    }

    @Transactional
    public ScanResponse use(ScanRequest req) {
        Ticket ticket = ticketRepository.findByCode(req.code)
                .orElseThrow(() -> new BadRequestException("Ticket introuvable"));

        User controller = getCurrentUser();
        enforceRateLimit(controller);

        // Vérification QR signé (optionnelle si sig présent)
        if (req.sig != null && !req.sig.isBlank()) {
            if (!verifySignature(ticket, req)) {
                logScan(ticket, controller, ScanResult.USED_FAIL, "Signature QR invalide");
                return ScanResponse.of(false, "Signature QR invalide", ticket.getStatus(), ticket.getEvent().getTitle(), ticket.getUser().getName(), ticket.getCode());
            }
        }

        // Vérifier fenêtre de l'événement
        Instant now = Instant.now();
        if (ticket.getEvent().getStartDateTime() != null && now.isBefore(ticket.getEvent().getStartDateTime())) {
            logScan(ticket, controller, ScanResult.USED_FAIL, "L'événement n'a pas encore commencé");
            throw new BadRequestException("L'événement n'a pas encore commencé");
        }
        if (ticket.getEvent().getEndDateTime() != null && now.isAfter(ticket.getEvent().getEndDateTime())) {
            logScan(ticket, controller, ScanResult.USED_FAIL, "L'événement est terminé");
            throw new BadRequestException("L'événement est terminé");
        }

        if (ticket.getStatus() != TicketStatus.VALID) {
            String msg = ticket.getStatus() == TicketStatus.USED ? "Ticket déjà utilisé" : "Ticket annulé";
            logScan(ticket, controller, ScanResult.USED_FAIL, msg);
            return ScanResponse.of(false, msg, ticket.getStatus(), ticket.getEvent().getTitle(), ticket.getUser().getName(), ticket.getCode());
        }

        ticket.setStatus(TicketStatus.USED);
        ticketRepository.save(ticket);
        logScan(ticket, controller, ScanResult.USED_OK, "Ticket validé");
        return ScanResponse.of(true, "Ticket validé", ticket.getStatus(), ticket.getEvent().getTitle(), ticket.getUser().getName(), ticket.getCode());
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    private void enforceRateLimit(User controller) {
        if (controller == null) return;
        UUID id = controller.getId();
        long now = System.currentTimeMillis();
        Deque<Long> dq = rateMap.computeIfAbsent(id, k -> new ArrayDeque<>());
        // Purge anciens
        while (!dq.isEmpty() && now - dq.peekFirst() > RATE_WINDOW_MS) {
            dq.pollFirst();
        }
        if (dq.size() >= RATE_MAX) {
            throw new BadRequestException("Trop de scans, veuillez ralentir");
        }
        dq.addLast(now);
    }

    private boolean verifySignature(Ticket ticket, ScanRequest req) {
        if (req.ts == null || req.nonce == null || req.sig == null) return false;

        // Vérifier fenêtre temporelle
        Instant ts = Instant.ofEpochMilli(req.ts);
        Instant now = Instant.now();
        if (ts.isBefore(now.minus(SIG_WINDOW)) || ts.isAfter(now.plus(SIG_WINDOW))) {
            return false;
        }

        // Anti-rejeu via nonce TTL
        Long seenAt = nonceCache.get(req.nonce);
        if (seenAt != null && (System.currentTimeMillis() - seenAt) < NONCE_TTL.toMillis()) {
            return false;
        }

        String data = ticket.getCode() + "|" + ticket.getEvent().getId() + "|" + req.ts + "|" + req.nonce;
        String expected = hmacHex(data, qrSecret);
        boolean ok = constantTimeEquals(expected, req.sig);
        if (ok) {
            nonceCache.put(req.nonce, System.currentTimeMillis());
        }
        return ok;
    }

    private String hmacHex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        if (x.length != y.length) return false;
        int r = 0;
        for (int i = 0; i < x.length; i++) r |= x[i] ^ y[i];
        return r == 0;
    }

    private void logScan(Ticket ticket, User controller, ScanResult result, String message) {
        try {
            UUID controllerId = controller == null ? null : controller.getId();
            String email = controller == null ? null : controller.getEmail();
            UUID eventId = ticket == null || ticket.getEvent() == null ? null : ticket.getEvent().getId();
            ScanLog log = new ScanLog(ticket.getCode(), controllerId, email, eventId, result, message, Instant.now());
            scanLogRepository.save(log);
        } catch (Exception ignored) {
            // éviter d'échouer le flux principal en cas d'erreur d'audit
        }
    }
}
