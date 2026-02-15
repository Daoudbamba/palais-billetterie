package com.palais.billetterie.order.repository;

import com.palais.billetterie.order.domain.Order;
import com.palais.billetterie.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByUser_Id(UUID userId);

    @Query("select count(o) from Order o where o.createdAt between :from and :to and (:eventId is null or o.event.id = :eventId)")
    long countBetweenAndEvent(
	    @Param("from") Instant from,
	    @Param("to") Instant to,
	    @Param("eventId") UUID eventId
    );

    @Query("select count(o) from Order o where o.status = :status and o.createdAt between :from and :to and (:eventId is null or o.event.id = :eventId)")
    long countByStatusBetweenAndEvent(
	    @Param("status") OrderStatus status,
	    @Param("from") Instant from,
	    @Param("to") Instant to,
	    @Param("eventId") UUID eventId
    );

    @Query("select FUNCTION('date_trunc','day', o.createdAt) as day, count(o) as cnt from Order o where o.status = :status and o.createdAt between :from and :to and (:eventId is null or o.event.id = :eventId) group by FUNCTION('date_trunc','day', o.createdAt) order by day")
    List<Object[]> countPerDayByStatusBetweenAndEvent(
	    @Param("status") OrderStatus status,
	    @Param("from") Instant from,
	    @Param("to") Instant to,
	    @Param("eventId") UUID eventId
    );
}