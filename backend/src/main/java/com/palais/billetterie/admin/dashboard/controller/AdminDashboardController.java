package com.palais.billetterie.admin.dashboard.controller;

import com.palais.billetterie.admin.dashboard.dto.DashboardSummaryDto;
import com.palais.billetterie.admin.dashboard.dto.DashboardTimeSeriesDto;
import com.palais.billetterie.admin.dashboard.dto.TopEventStatDto;
import com.palais.billetterie.admin.dashboard.dto.DashboardKpisDto;
import com.palais.billetterie.admin.dashboard.service.AdminDashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService service;

    public AdminDashboardController(AdminDashboardService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    public DashboardSummaryDto summary(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "eventId", required = false) UUID eventId
    ) {
        return service.getSummary(from, to, eventId);
    }

    @GetMapping("/timeseries")
    public java.util.List<DashboardTimeSeriesDto> timeSeries(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "eventId", required = false) UUID eventId,
            @RequestParam(value = "granularity", required = false, defaultValue = "day") String granularity
    ) {
        return service.getTimeSeries(from, to, eventId, granularity);
    }

    @GetMapping("/top-events")
    public java.util.List<TopEventStatDto> topEvents(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit,
            @RequestParam(value = "sortBy", required = false, defaultValue = "revenue") String sortBy,
            @RequestParam(value = "eventIds", required = false) String eventIdsCsv
    ) {
        java.util.Set<java.util.UUID> filterIds = new java.util.HashSet<>();
        if (eventIdsCsv != null && !eventIdsCsv.isBlank()) {
            for (String part : eventIdsCsv.split(",")) {
                String s = part.trim();
                if (!s.isEmpty()) {
                    try { filterIds.add(java.util.UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
                }
            }
        }
        return service.getTopEvents(from, to, limit, sortBy, filterIds);
    }

    @GetMapping("/kpis")
    public DashboardKpisDto kpis(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "eventId", required = false) UUID eventId
    ) {
        return service.getKpis(from, to, eventId);
    }
}
