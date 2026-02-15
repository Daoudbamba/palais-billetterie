package com.palais.billetterie.payment.repository;

import com.palais.billetterie.payment.domain.Payment;
import com.palais.billetterie.payment.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = :status and p.createdAt between :from and :to and (:eventId is null or p.order.event.id = :eventId)")
    Double sumAmountByStatusBetweenAndEvent(
	    @Param("status") PaymentStatus status,
	    @Param("from") Instant from,
	    @Param("to") Instant to,
	    @Param("eventId") UUID eventId
    );

    @Query("select count(p) from Payment p where p.status = :status and p.createdAt between :from and :to and (:eventId is null or p.order.event.id = :eventId)")
    long countByStatusBetweenAndEvent(
	    @Param("status") PaymentStatus status,
	    @Param("from") Instant from,
	    @Param("to") Instant to,
	    @Param("eventId") UUID eventId
    );

    @Query("select FUNCTION('date_trunc','day', p.createdAt) as day, coalesce(sum(p.amount), 0) as total from Payment p where p.status = :status and p.createdAt between :from and :to and (:eventId is null or p.order.event.id = :eventId) group by FUNCTION('date_trunc','day', p.createdAt) order by day")
    List<Object[]> sumPerDayByStatusBetweenAndEvent(
	    @Param("status") PaymentStatus status,
	    @Param("from") Instant from,
	    @Param("to") Instant to,
	    @Param("eventId") UUID eventId
    );

    @Query("select p.order.event.id, p.order.event.title, coalesce(sum(p.amount), 0) from Payment p where p.status = :status and p.createdAt between :from and :to group by p.order.event.id, p.order.event.title order by coalesce(sum(p.amount), 0) desc")
    List<Object[]> sumByEventBetween(
	    @Param("status") PaymentStatus status,
	    @Param("from") Instant from,
	    @Param("to") Instant to
    );
}