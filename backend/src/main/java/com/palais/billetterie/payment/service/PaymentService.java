package com.palais.billetterie.payment.service;

import com.palais.billetterie.payment.domain.Payment;
import com.palais.billetterie.payment.domain.PaymentStatus;
import com.palais.billetterie.payment.repository.PaymentRepository;
import com.palais.billetterie.order.domain.Order;
import com.palais.billetterie.order.repository.OrderRepository;
import com.palais.billetterie.common.exceptions.BadRequestException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository repository, OrderRepository orderRepository) {
        this.repository = repository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<Payment> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Payment getById(UUID id) {
        if (id == null) throw new BadRequestException("Identifiant de paiement manquant");
        return repository.findById(id)
            .orElseThrow(() -> new BadRequestException("Paiement introuvable"));
    }

    @Transactional
        public Payment create(UUID orderId, Double amount, String provider) {
        if (orderId == null) throw new BadRequestException("Commande requise");
        if (amount == null || amount <= 0) throw new BadRequestException("Montant invalide, doit être > 0");

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BadRequestException("Commande introuvable"));

        Payment payment = Payment.builder()
            .order(order)
            .amount(amount)
            .status(PaymentStatus.PENDING)
            .provider(provider == null || provider.isBlank() ? "STRIPE" : provider)
            .createdAt(Instant.now())
            .build();

        Objects.requireNonNull(payment, "payment must not be null");
        return repository.save(payment);
    }

    @Transactional
    public Payment updateStatus(UUID paymentId, PaymentStatus status, String providerPaymentId) {
        if (paymentId == null) throw new BadRequestException("Identifiant de paiement manquant");
        if (status == null) throw new BadRequestException("Statut requis");
        Payment payment = repository.findById(paymentId)
            .orElseThrow(() -> new BadRequestException("Paiement introuvable"));
        payment.setStatus(status);
        payment.setProviderPaymentId(providerPaymentId);
        Objects.requireNonNull(payment, "payment must not be null");
        return repository.save(payment);
    }

        @Transactional
        public void delete(UUID id) {
        if (id == null) throw new BadRequestException("Identifiant de paiement manquant");
        Payment payment = repository.findById(id)
            .orElseThrow(() -> new BadRequestException("Paiement introuvable"));
        Objects.requireNonNull(payment, "payment must not be null");
        repository.delete(payment);
        }
}