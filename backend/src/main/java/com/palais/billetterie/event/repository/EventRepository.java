package com.palais.billetterie.event.repository;

import com.palais.billetterie.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, UUID> {
	List<Event> findByStartDateTimeBetween(Instant from, Instant to);
}