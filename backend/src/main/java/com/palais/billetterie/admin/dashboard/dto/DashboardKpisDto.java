package com.palais.billetterie.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DashboardKpisDto {
    private double revenueTotal;

    private long ordersCreatedCount;
    private long ordersPaidCount;
    private double paidConversionRate; // ordersPaid / ordersCreated

    private long ticketsCreatedCount;
    private long ticketsUsedCount;
    private double ticketsUsageRate; // ticketsUsed / ticketsCreated

    private long paymentsSuccessCount;
    private long refundsSuccessCount;
    private double refundsAmountTotal;
    private double refundsRate; // refundsSuccess / ordersPaid
}
