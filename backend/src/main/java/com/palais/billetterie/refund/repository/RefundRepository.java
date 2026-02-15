package com.palais.billetterie.refund.repository;

import com.palais.billetterie.refund.domain.Refund;
import com.palais.billetterie.refund.domain.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface RefundRepository extends JpaRepository<Refund, UUID> {

    @Query("select count(r) from Refund r where r.status = :status and r.createdAt between :from and :to and (:eventId is null or r.payment.order.event.id = :eventId)")
    long countByStatusBetweenAndEvent(
	    @Param("status") RefundStatus status,
	    @Param("from") Instant from,
	    @Param("to") Instant to,
	    @Param("eventId") UUID eventId
    );

    @Query("select coalesce(sum(r.amount), 0) from Refund r where r.status = :status and r.createdAt between :from and :to and (:eventId is null or r.payment.order.event.id = :eventId)")
    Double sumAmountByStatusBetweenAndEvent(
	    @Param("status") RefundStatus status,
	    @Param("from") Instant from,
	    @Param("to") Instant to,
	    @Param("eventId") UUID eventId
    );
}