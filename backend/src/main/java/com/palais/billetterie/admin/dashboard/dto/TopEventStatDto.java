package com.palais.billetterie.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class TopEventStatDto {
    private UUID eventId;
    private String eventTitle;
    private double revenueTotal;
    private long ticketsUsedCount;
}
