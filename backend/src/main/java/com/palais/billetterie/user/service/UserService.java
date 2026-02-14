package com.palais.billetterie.user.service;

import com.palais.billetterie.user.domain.Role;
import com.palais.billetterie.user.domain.User;
import com.palais.billetterie.user.repository.UserRepository;
import com.palais.billetterie.common.exceptions.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<User> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public User getById(UUID id) {
        UUID nonNullId = Objects.requireNonNull(id, "id ne doit pas être nul");
        return repository.findById(nonNullId)
            .orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));
    }

    @Transactional
    public User create(User user) {
        user.setCreatedAt(Instant.now());
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        return repository.save(user);
    }

    @Transactional
    public User update(UUID id, User patch) {
        UUID nonNullId = Objects.requireNonNull(id, "id ne doit pas être nul");
        User existing = repository.findById(nonNullId)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));
        existing.setName(patch.getName());
        existing.setEmail(patch.getEmail());
        existing.setPhone(patch.getPhone());
        existing.setRole(patch.getRole() == null ? Role.USER : patch.getRole());
        return repository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        UUID nonNullId = Objects.requireNonNull(id, "id ne doit pas être nul");
        if (!repository.existsById(nonNullId)) {
            throw new BadRequestException("Utilisateur introuvable");
        }
        repository.deleteById(nonNullId);
    }
}