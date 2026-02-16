package com.palais.billetterie.event.repository;

import com.palais.billetterie.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByStartDateTimeBetween(Instant from, Instant to);

    @Query("select e from Event e where e.createdAt between :from and :to")
    List<Event> findCreatedBetween(@Param("from") Instant from, @Param("to") Instant to);
}