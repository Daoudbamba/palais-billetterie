package com.palais.billetterie.refund.service;

import com.palais.billetterie.payment.domain.Payment;
import com.palais.billetterie.payment.domain.PaymentStatus;
import com.palais.billetterie.payment.repository.PaymentRepository;
import com.palais.billetterie.refund.domain.Refund;
import com.palais.billetterie.refund.domain.RefundStatus;
import com.palais.billetterie.refund.repository.RefundRepository;
import com.palais.billetterie.order.domain.Order;
import com.palais.billetterie.order.domain.OrderStatus;
import com.palais.billetterie.order.repository.OrderRepository;
import com.palais.billetterie.ticket.domain.TicketStatus;
import com.palais.billetterie.ticket.repository.TicketRepository;
import com.palais.billetterie.common.exceptions.BadRequestException;
import com.palais.billetterie.payment.stripe.StripeService;
import com.palais.billetterie.notification.service.EmailService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final StripeService stripeService;
    private final EmailService emailService;

    public RefundService(RefundRepository refundRepository,
                         PaymentRepository paymentRepository,
                         OrderRepository orderRepository,
                         TicketRepository ticketRepository,
                         StripeService stripeService,
                         EmailService emailService) {
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.stripeService = stripeService;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public List<Refund> getAll() {
        return refundRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Refund getById(UUID id) {
        if (id == null) throw new BadRequestException("Identifiant de remboursement manquant");
        return refundRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("Remboursement introuvable"));
    }

    @Transactional
    public Refund requestRefund(UUID paymentId, Double amount) {
        if (paymentId == null) throw new BadRequestException("Paiement requis");
        if (amount == null || amount <= 0) throw new BadRequestException("Le montant doit être supérieur à 0");

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BadRequestException("Paiement introuvable"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Seuls les paiements réussis peuvent être remboursés");
        }

        Refund refund = Refund.builder()
                .payment(payment)
                .amount(amount)
                .status(RefundStatus.PENDING)
                .provider("STRIPE")
                .createdAt(Instant.now())
                .build();

        refund = refundRepository.save(refund);

        // Call Stripe to create the refund and mark success
        try {
            String providerRefundId = stripeService.createRefund(payment.getId(), Math.round(amount * 100));
            return markRefundSuccess(refund.getId(), providerRefundId);
        } catch (Exception e) {
            throw new BadRequestException("Échec du remboursement Stripe: " + e.getMessage());
        }
    }

    @Transactional
    public Refund markRefundSuccess(UUID refundId, String providerRefundId) {
        Refund refund = getById(refundId);
        refund.setStatus(RefundStatus.SUCCESS);
        refund.setProviderRefundId(providerRefundId);
        refundRepository.save(refund);

        Payment payment = refund.getPayment();
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        ticketRepository.findAll().stream()
                .filter(t -> t.getOrder().getId().equals(order.getId()))
                .forEach(t -> {
                    t.setStatus(TicketStatus.CANCELLED);
                    ticketRepository.save(t);
                });

        // Email confirmation de remboursement
        try {
            String subject = "Remboursement effectué";
            String body = "Bonjour " + order.getUser().getName() + ",\n\n" +
                    "Votre remboursement de " + refund.getAmount() + "€ a été traité.\n" +
                    "Commande : " + order.getId() + "\n" +
                    "Merci et à bientôt !";
            emailService.send(order.getUser().getEmail(), subject, body);
        } catch (Exception ex) { /* ne bloque pas le flux */ }

        return refund;
    }

    @Transactional
    public Refund markRefundFailed(UUID refundId) {
        Refund refund = getById(refundId);
        refund.setStatus(RefundStatus.FAILED);
        return refundRepository.save(refund);
    }
}