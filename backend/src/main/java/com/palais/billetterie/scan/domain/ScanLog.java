package com.palais.billetterie.scan.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scan_logs",
        indexes = {
                @Index(name = "idx_scan_logs_event_id", columnList = "event_id"),
                @Index(name = "idx_scan_logs_created_at", columnList = "created_at")
        })
public class ScanLog {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "code", length = 128, nullable = false)
    private String code;

    @Column(name = "controller_id")
    private UUID controllerId;

    @Column(name = "controller_email", length = 255)
    private String controllerEmail;

    @Column(name = "event_id")
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 32, nullable = false)
    private ScanResult result;

    @Column(name = "message", length = 512)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ScanLog() {}

    public ScanLog(String code, UUID controllerId, String controllerEmail, UUID eventId, ScanResult result, String message, Instant createdAt) {
        this.code = code;
        this.controllerId = controllerId;
        this.controllerEmail = controllerEmail;
        this.eventId = eventId;
        this.result = result;
        this.message = message;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public UUID getControllerId() { return controllerId; }
    public String getControllerEmail() { return controllerEmail; }
    public UUID getEventId() { return eventId; }
    public ScanResult getResult() { return result; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
}
