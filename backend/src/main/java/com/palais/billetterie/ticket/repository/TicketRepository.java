package com.palais.billetterie.ticket.repository;

import com.palais.billetterie.ticket.domain.Ticket;
import com.palais.billetterie.ticket.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByEvent_IdAndStatus(UUID eventId, TicketStatus status);
    Optional<Ticket> findByCode(String code);

    @Query("select count(t) from Ticket t where t.status = :status and t.createdAt between :from and :to and (:eventId is null or t.event.id = :eventId)")
    long countByStatusBetweenAndEvent(
            @Param("status") TicketStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("eventId") UUID eventId
    );

    @Query("select count(t) from Ticket t where t.createdAt between :from and :to and (:eventId is null or t.event.id = :eventId)")
    long countBetweenAndEvent(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("eventId") UUID eventId
    );

    @Query("select FUNCTION('date_trunc','day', t.createdAt) as day, count(t) as cnt from Ticket t where t.status = :status and t.createdAt between :from and :to and (:eventId is null or t.event.id = :eventId) group by FUNCTION('date_trunc','day', t.createdAt) order by day")
    List<Object[]> countPerDayByStatusBetweenAndEvent(
            @Param("status") TicketStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("eventId") UUID eventId
    );

    @Query("select t.event.id, t.event.title, count(t) from Ticket t where t.status = :status and t.createdAt between :from and :to group by t.event.id, t.event.title order by count(t) desc")
    List<Object[]> countByEventBetween(
            @Param("status") TicketStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
 