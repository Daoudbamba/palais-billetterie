package com.palais.billetterie.refund.repository;

import com.palais.billetterie.refund.domain.Refund;
import com.palais.billetterie.refund.domain.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
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

	// Somme des montants remboursés (champ amount) avec un statut donné pour un paiement
	@Query("select coalesce(sum(r.amount), 0) from Refund r where r.payment.id = :paymentId and r.status = :status")
	Double sumAmountByPaymentIdAndStatus(@Param("paymentId") UUID paymentId, @Param("status") RefundStatus status);

	// Nombre de remboursements pour un paiement dans une liste de statuts (ex: PENDING ou SUCCESS)
	long countByPaymentIdAndStatusIn(UUID paymentId, Collection<RefundStatus> statuses);
}