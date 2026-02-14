package com.palais.billetterie.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DashboardSummaryDto {
    private double revenueTotal;
    private long paymentsSuccessCount;
    private long ordersPaidCount;
    private long ticketsUsedCount;
}
