package com.palais.billetterie.event.service;

import com.palais.billetterie.event.domain.Event;
import com.palais.billetterie.common.exceptions.BadRequestException;
import com.palais.billetterie.event.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Event> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Event getById(UUID id) {
        UUID nonNullId = Objects.requireNonNull(id, "id ne doit pas être nul");
        return repository.findById(nonNullId)
                .orElseThrow(() -> new BadRequestException("Événement introuvable"));
    }

    @Transactional
    public Event create(Event event) {
        if (event.getCapacity() == null || event.getCapacity() < 1) {
            throw new BadRequestException("La capacité doit être supérieure à 0");
        }
        if (event.getStartDateTime() == null || event.getEndDateTime() == null
                || !event.getStartDateTime().isBefore(event.getEndDateTime())) {
            throw new BadRequestException("La date de début doit être avant la date de fin");
        }
        event.setCreatedAt(Instant.now());
        return repository.save(event);
    }

    @Transactional
    public Event update(UUID id, Event patch) {
        UUID nonNullId = Objects.requireNonNull(id, "id ne doit pas être nul");
        Event existing = repository.findById(nonNullId)
                .orElseThrow(() -> new BadRequestException("Événement introuvable"));

        if (patch.getCapacity() == null || patch.getCapacity() < 1) {
            throw new BadRequestException("La capacité doit être supérieure à 0");
        }
        if (patch.getStartDateTime() == null || patch.getEndDateTime() == null
                || !patch.getStartDateTime().isBefore(patch.getEndDateTime())) {
            throw new BadRequestException("La date de début doit être avant la date de fin");
        }

        existing.setTitle(patch.getTitle());
        existing.setDescription(patch.getDescription());
        existing.setStartDateTime(patch.getStartDateTime());
        existing.setEndDateTime(patch.getEndDateTime());
        existing.setVenue(patch.getVenue());
        existing.setCapacity(patch.getCapacity());

        return repository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        UUID nonNullId = Objects.requireNonNull(id, "id ne doit pas être nul");
        if (!repository.existsById(nonNullId)) {
            throw new BadRequestException("Événement introuvable");
        }
        repository.deleteById(nonNullId);
    }
}