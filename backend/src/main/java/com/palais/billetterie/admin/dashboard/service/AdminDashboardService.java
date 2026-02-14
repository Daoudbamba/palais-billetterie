package com.palais.billetterie.admin.dashboard.service;

import com.palais.billetterie.admin.dashboard.dto.DashboardSummaryDto;
import com.palais.billetterie.admin.dashboard.dto.DashboardTimeSeriesDto;
import com.palais.billetterie.admin.dashboard.dto.TopEventStatDto;
import com.palais.billetterie.order.domain.OrderStatus;
import com.palais.billetterie.order.repository.OrderRepository;
import com.palais.billetterie.payment.domain.PaymentStatus;
import com.palais.billetterie.payment.repository.PaymentRepository;
import com.palais.billetterie.ticket.domain.TicketStatus;
import com.palais.billetterie.ticket.repository.TicketRepository;
import com.palais.billetterie.refund.domain.RefundStatus;
import com.palais.billetterie.refund.repository.RefundRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class AdminDashboardService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final RefundRepository refundRepository;
    private static final long CACHE_TTL_MILLIS = 60_000L;
    private static class CacheItem<T> { final long ts; final T val; CacheItem(long ts, T val){ this.ts=ts; this.val=val; } }
    private final ConcurrentHashMap<String, CacheItem<DashboardSummaryDto>> summaryCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CacheItem<List<DashboardTimeSeriesDto>>> tsCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CacheItem<List<TopEventStatDto>>> topEventsCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CacheItem<com.palais.billetterie.admin.dashboard.dto.DashboardKpisDto>> kpisCache = new ConcurrentHashMap<>();

    private <T> T getOrCompute(ConcurrentHashMap<String, CacheItem<T>> cache, String key, Supplier<T> supplier) {
        CacheItem<T> item = cache.get(key);
        long now = System.currentTimeMillis();
        if (item != null && (now - item.ts) < CACHE_TTL_MILLIS) {
            return item.val;
        }
        T val = supplier.get();
        cache.put(key, new CacheItem<>(now, val));
        return val;
    }

    private String keyOf(Instant from, Instant to, UUID eventId, String extra) {
        long f = from != null ? from.toEpochMilli() : -1L;
        long t = to != null ? to.toEpochMilli() : -1L;
        String e = eventId != null ? eventId.toString() : "-";
        return extra + "|f=" + f + "|t=" + t + "|e=" + e;
    }

    public AdminDashboardService(PaymentRepository paymentRepository,
                                 OrderRepository orderRepository,
                                 TicketRepository ticketRepository,
                                 RefundRepository refundRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.refundRepository = refundRepository;
    }

    public DashboardSummaryDto getSummary(Instant from, Instant to, UUID eventId) {
        String key = keyOf(from, to, eventId, "summary");
        return getOrCompute(summaryCache, key, () -> {
            // Defaults: last 30 days if nulls
            Instant toEff = to != null ? to : Instant.now();
            Instant fromEff = from != null ? from : toEff.minus(30, ChronoUnit.DAYS);

            Double revenue = paymentRepository.sumAmountByStatusBetweenAndEvent(PaymentStatus.SUCCESS, fromEff, toEff, eventId);
            if (revenue == null) revenue = 0.0;

            long paymentsSuccess = paymentRepository.countByStatusBetweenAndEvent(PaymentStatus.SUCCESS, fromEff, toEff, eventId);
            long ordersPaid = orderRepository.countByStatusBetweenAndEvent(OrderStatus.PAID, fromEff, toEff, eventId);
            long ticketsUsed = ticketRepository.countByStatusBetweenAndEvent(TicketStatus.USED, fromEff, toEff, eventId);

            return DashboardSummaryDto.builder()
                    .revenueTotal(revenue)
                    .paymentsSuccessCount(paymentsSuccess)
                    .ordersPaidCount(ordersPaid)
                    .ticketsUsedCount(ticketsUsed)
                    .build();
        });
    }

    public com.palais.billetterie.admin.dashboard.dto.DashboardKpisDto getKpis(Instant from, Instant to, UUID eventId) {
        String key = keyOf(from, to, eventId, "kpis");
        return getOrCompute(kpisCache, key, () -> {
            Instant toEff = to != null ? to : Instant.now();
            Instant fromEff = from != null ? from : toEff.minus(30, ChronoUnit.DAYS);

            Double revenue = paymentRepository.sumAmountByStatusBetweenAndEvent(PaymentStatus.SUCCESS, fromEff, toEff, eventId);
            if (revenue == null) revenue = 0.0;

            long ordersCreated = orderRepository.countBetweenAndEvent(fromEff, toEff, eventId);
            long ordersPaid = orderRepository.countByStatusBetweenAndEvent(OrderStatus.PAID, fromEff, toEff, eventId);
            double paidConversion = (ordersCreated > 0) ? ((double) ordersPaid / (double) ordersCreated) : 0.0;

            long ticketsCreated = ticketRepository.countBetweenAndEvent(fromEff, toEff, eventId);
            long ticketsUsed = ticketRepository.countByStatusBetweenAndEvent(TicketStatus.USED, fromEff, toEff, eventId);
            double ticketsUsage = (ticketsCreated > 0) ? ((double) ticketsUsed / (double) ticketsCreated) : 0.0;

            long paymentsSuccess = paymentRepository.countByStatusBetweenAndEvent(PaymentStatus.SUCCESS, fromEff, toEff, eventId);
            long refundsSuccess = refundRepository.countByStatusBetweenAndEvent(RefundStatus.SUCCESS, fromEff, toEff, eventId);
            Double refundsAmount = refundRepository.sumAmountByStatusBetweenAndEvent(RefundStatus.SUCCESS, fromEff, toEff, eventId);
            if (refundsAmount == null) refundsAmount = 0.0;
            double refundsRate = (ordersPaid > 0) ? ((double) refundsSuccess / (double) ordersPaid) : 0.0;

            return com.palais.billetterie.admin.dashboard.dto.DashboardKpisDto.builder()
                    .revenueTotal(revenue)
                    .ordersCreatedCount(ordersCreated)
                    .ordersPaidCount(ordersPaid)
                    .paidConversionRate(paidConversion)
                    .ticketsCreatedCount(ticketsCreated)
                    .ticketsUsedCount(ticketsUsed)
                    .ticketsUsageRate(ticketsUsage)
                    .paymentsSuccessCount(paymentsSuccess)
                    .refundsSuccessCount(refundsSuccess)
                    .refundsAmountTotal(refundsAmount)
                    .refundsRate(refundsRate)
                    .build();
        });
    }

    public List<DashboardTimeSeriesDto> getTimeSeries(Instant from, Instant to, UUID eventId, String granularity) {
        String key = keyOf(from, to, eventId, "ts|g=" + (granularity == null ? "day" : granularity));
        return getOrCompute(tsCache, key, () -> {
        Instant toEff = to != null ? to : Instant.now();
        Instant fromEff = from != null ? from : toEff.minus(30, ChronoUnit.DAYS);

        List<Object[]> revSeries = paymentRepository.sumPerDayByStatusBetweenAndEvent(PaymentStatus.SUCCESS, fromEff, toEff, eventId);
        List<Object[]> usedSeries = ticketRepository.countPerDayByStatusBetweenAndEvent(TicketStatus.USED, fromEff, toEff, eventId);
        List<Object[]> paidSeries = orderRepository.countPerDayByStatusBetweenAndEvent(OrderStatus.PAID, fromEff, toEff, eventId);

        Map<Instant, Double> revenueByDay = new HashMap<>();
        for (Object[] row : revSeries) {
            Instant day = ((java.sql.Timestamp) row[0]).toInstant();
            Double total = ((Number) row[1]).doubleValue();
            revenueByDay.put(day, total);
        }

        Map<Instant, Long> usedByDay = new HashMap<>();
        for (Object[] row : usedSeries) {
            Instant day = ((java.sql.Timestamp) row[0]).toInstant();
            Long cnt = ((Number) row[1]).longValue();
            usedByDay.put(day, cnt);
        }

        Map<Instant, Long> ordersPaidByDay = new HashMap<>();
        for (Object[] row : paidSeries) {
            Instant day = ((java.sql.Timestamp) row[0]).toInstant();
            Long cnt = ((Number) row[1]).longValue();
            ordersPaidByDay.put(day, cnt);
        }

        List<Instant> days = new ArrayList<>(revenueByDay.keySet());
        for (Instant d : usedByDay.keySet()) { if (!days.contains(d)) days.add(d); }
        for (Instant d : ordersPaidByDay.keySet()) { if (!days.contains(d)) days.add(d); }
        days.sort(Comparator.naturalOrder());

        // Build daily series
        List<DashboardTimeSeriesDto> daily = days.stream().map(d -> DashboardTimeSeriesDto.builder()
                        .day(d)
                        .revenueTotal(revenueByDay.getOrDefault(d, 0.0))
                        .ticketsUsedCount(usedByDay.getOrDefault(d, 0L))
                        .ordersPaidCount(ordersPaidByDay.getOrDefault(d, 0L))
                        .build())
                .collect(Collectors.toList());

        // Aggregate if granularity != day
        if (granularity == null || granularity.equalsIgnoreCase("day")) {
            return daily;
        }

        Map<Instant, DashboardTimeSeriesDto> agg = new HashMap<>();
        java.time.ZoneId zone = java.time.ZoneOffset.UTC;
        for (DashboardTimeSeriesDto row : daily) {
            java.time.LocalDate date = row.getDay().atZone(zone).toLocalDate();
            Instant bucketKey;
            if (granularity.equalsIgnoreCase("week")) {
                java.time.LocalDate monday = date.with(java.time.DayOfWeek.MONDAY);
                bucketKey = monday.atStartOfDay(zone).toInstant();
            } else if (granularity.equalsIgnoreCase("month")) {
                java.time.LocalDate first = date.withDayOfMonth(1);
                bucketKey = first.atStartOfDay(zone).toInstant();
            } else {
                bucketKey = row.getDay(); // fallback to day
            }
            DashboardTimeSeriesDto cur = agg.get(bucketKey);
            if (cur == null) {
                agg.put(bucketKey, DashboardTimeSeriesDto.builder()
                        .day(bucketKey)
                        .revenueTotal(row.getRevenueTotal())
                        .ticketsUsedCount(row.getTicketsUsedCount())
                        .ordersPaidCount(row.getOrdersPaidCount())
                        .build());
            } else {
                cur.setRevenueTotal(cur.getRevenueTotal() + row.getRevenueTotal());
                cur.setTicketsUsedCount(cur.getTicketsUsedCount() + row.getTicketsUsedCount());
                cur.setOrdersPaidCount(cur.getOrdersPaidCount() + row.getOrdersPaidCount());
            }
        }
        return agg.values().stream()
            .sorted(Comparator.comparing(DashboardTimeSeriesDto::getDay))
            .collect(Collectors.toList());
        });
        }

    public List<TopEventStatDto> getTopEvents(Instant from, Instant to, int limit, String sortBy, java.util.Set<UUID> filterIds) {
        String ids = (filterIds == null || filterIds.isEmpty()) ? "-" : filterIds.stream().map(UUID::toString).sorted().collect(Collectors.joining(","));
        String key = keyOf(from, to, null, "top|lim=" + limit + "|sort=" + sortBy + "|ids=" + ids);
        return getOrCompute(topEventsCache, key, () -> {
        Instant toEff = to != null ? to : Instant.now();
        Instant fromEff = from != null ? from : toEff.minus(30, ChronoUnit.DAYS);

        List<Object[]> revenueRows = paymentRepository.sumByEventBetween(PaymentStatus.SUCCESS, fromEff, toEff);
        Map<UUID, TopEventStatDto> map = new HashMap<>();
        for (Object[] r : revenueRows) {
            UUID eventId = (UUID) r[0];
            String title = (String) r[1];
            double total = ((Number) r[2]).doubleValue();
            map.put(eventId, TopEventStatDto.builder().eventId(eventId).eventTitle(title).revenueTotal(total).ticketsUsedCount(0L).build());
        }

        List<Object[]> usedRows = ticketRepository.countByEventBetween(TicketStatus.USED, fromEff, toEff);
        for (Object[] r : usedRows) {
            UUID eventId = (UUID) r[0];
            String title = (String) r[1];
            long used = ((Number) r[2]).longValue();
            TopEventStatDto dto = map.get(eventId);
            if (dto == null) {
                dto = TopEventStatDto.builder().eventId(eventId).eventTitle(title).revenueTotal(0.0).ticketsUsedCount(used).build();
                map.put(eventId, dto);
            } else {
                dto.setTicketsUsedCount(used);
            }
        }

        java.util.stream.Stream<TopEventStatDto> stream = map.values().stream();
        if (filterIds != null && !filterIds.isEmpty()) {
            stream = stream.filter(dto -> filterIds.contains(dto.getEventId()));
        }
        return stream
                .sorted((a, b) -> {
                    if ("tickets".equalsIgnoreCase(sortBy)) {
                        int cmp = Long.compare(b.getTicketsUsedCount(), a.getTicketsUsedCount());
                        if (cmp != 0) return cmp;
                        return Double.compare(b.getRevenueTotal(), a.getRevenueTotal());
                    } else { // default revenue
                        int cmp = Double.compare(b.getRevenueTotal(), a.getRevenueTotal());
                        if (cmp != 0) return cmp;
                        return Long.compare(b.getTicketsUsedCount(), a.getTicketsUsedCount());
                    }
                })
                .limit(limit)
                .collect(Collectors.toList());
        });
    }
}
