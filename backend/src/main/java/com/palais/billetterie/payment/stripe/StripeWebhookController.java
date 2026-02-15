package com.palais.billetterie.payment.stripe;

import com.palais.billetterie.order.domain.OrderStatus;
import com.palais.billetterie.order.repository.OrderRepository;
import com.palais.billetterie.payment.domain.PaymentStatus;
import com.palais.billetterie.payment.service.PaymentService;
import com.palais.billetterie.ticket.service.TicketService;
import com.palais.billetterie.notification.service.EmailService;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Objects;

@RestController
@RequestMapping("/api/payments/stripe")
public class StripeWebhookController {

    private final PaymentService paymentService;
    private final TicketService ticketService;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    private final String webhookSecret;
    private final boolean skipVerify;

    public StripeWebhookController(PaymentService paymentService,
                                   TicketService ticketService,
                                   OrderRepository orderRepository,
                                   EmailService emailService,
                                   @Value("${app.stripe.webhook-secret}") String webhookSecret,
                                   @Value("${app.stripe.skip-verify:false}") boolean skipVerify) {
        this.paymentService = paymentService;
        this.ticketService = ticketService;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
        this.webhookSecret = webhookSecret;
        this.skipVerify = skipVerify;
    }

    @PostMapping("/webhook")
    @Transactional
    public ResponseEntity<String> handle(@RequestBody String payload,
                                         @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        try {
            if (skipVerify) {
                // DEV mode: parse JSON and perform actions without Stripe verification
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(payload);
                String type = root.path("type").asText();
                JsonNode obj = root.path("data").path("object");
                String paymentIntentId = obj.path("id").asText("pi_dev");
                String paymentIdStr = obj.path("metadata").path("paymentId").asText();
                String orderIdStr = obj.path("metadata").path("orderId").asText();

                if ("payment_intent.succeeded".equals(type)) {
                    UUID paymentId = UUID.fromString(paymentIdStr);
                    UUID orderId = UUID.fromString(orderIdStr);
                    Objects.requireNonNull(orderId, "orderId is required");

                    paymentService.updateStatus(paymentId, PaymentStatus.SUCCESS, paymentIntentId);

                    // Mark order as PAID and proceed with ticket + email
                    orderRepository.findById(orderId).ifPresent(order -> {
                        order.setStatus(OrderStatus.PAID);
                        orderRepository.save(order);
                        ticketService.create(order.getUser().getId(), order.getEvent().getId(), order.getId());
                        try {
                            emailService.send(order.getUser().getEmail(),
                                    "Paiement réussi",
                                    "Bonjour " + order.getUser().getName() + ",\n\n" +
                                            "Votre commande est confirmée pour l'événement \"" + order.getEvent().getTitle() + "\".\n" +
                                            "Merci pour votre achat !");
                        } catch (Exception ex) { /* ignore in dev */ }
                    });
                } else if ("payment_intent.payment_failed".equals(type)) {
                    UUID paymentId = UUID.fromString(paymentIdStr);
                    paymentService.updateStatus(paymentId, PaymentStatus.FAILED, paymentIntentId);
                    try {
                        UUID orderId = UUID.fromString(orderIdStr);
                        Objects.requireNonNull(orderId, "orderId is required");
                        orderRepository.findById(orderId).ifPresent(order -> {
                            emailService.send(order.getUser().getEmail(),
                                    "Échec de paiement",
                                    "Bonjour " + order.getUser().getName() + ",\n\n" +
                                            "Votre paiement a échoué pour l'événement \"" + order.getEvent().getTitle() + "\".\n" +
                                            "Merci de réessayer ou de vérifier vos informations.");
                        });
                    } catch (Exception ex) { /* ignore in dev */ }
                }

                return ResponseEntity.ok("OK");
            } else {
                Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
                if ("payment_intent.succeeded".equals(event.getType())) {
                    PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                            .getObject().orElseThrow();

                    UUID paymentId = UUID.fromString(intent.getMetadata().get("paymentId"));
                    UUID orderId = UUID.fromString(intent.getMetadata().get("orderId"));
                    Objects.requireNonNull(orderId, "orderId is required");

                    paymentService.updateStatus(paymentId, PaymentStatus.SUCCESS, intent.getId());

                    // Mark order as PAID
                    orderRepository.findById(orderId).ifPresent(order -> {
                        order.setStatus(OrderStatus.PAID);
                        orderRepository.save(order);
                        // Create ticket for the paid order
                        ticketService.create(order.getUser().getId(), order.getEvent().getId(), order.getId());

                        // Email confirmation de paiement
                        try {
                            emailService.send(order.getUser().getEmail(),
                                    "Paiement réussi",
                                    "Bonjour " + order.getUser().getName() + ",\n\n" +
                                            "Votre commande est confirmée pour l'événement \"" + order.getEvent().getTitle() + "\".\n" +
                                            "Merci pour votre achat !");
                        } catch (Exception ex) { /* ne bloque pas le flux */ }
                    });
                } else if ("payment_intent.payment_failed".equals(event.getType())) {
                    PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                            .getObject().orElseThrow();

                    UUID paymentId = UUID.fromString(intent.getMetadata().get("paymentId"));
                    paymentService.updateStatus(paymentId, PaymentStatus.FAILED, intent.getId());

                    // Email alerte échec de paiement
                    try {
                        UUID orderId = UUID.fromString(intent.getMetadata().get("orderId"));
                        Objects.requireNonNull(orderId, "orderId is required");
                        orderRepository.findById(orderId).ifPresent(order -> {
                            emailService.send(order.getUser().getEmail(),
                                    "Échec de paiement",
                                    "Bonjour " + order.getUser().getName() + ",\n\n" +
                                            "Votre paiement a échoué pour l'événement \"" + order.getEvent().getTitle() + "\".\n" +
                                            "Merci de réessayer ou de vérifier vos informations.");
                        });
                    } catch (Exception ex) { /* ne bloque pas le flux */ }
                }

                return ResponseEntity.ok("OK");
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook error: " + e.getMessage());
        }
    }
}