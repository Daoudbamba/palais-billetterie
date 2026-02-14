package com.palais.billetterie.payment.stripe;

import com.palais.billetterie.payment.domain.Payment;
import com.palais.billetterie.payment.domain.PaymentStatus;
import com.palais.billetterie.payment.repository.PaymentRepository;
import com.palais.billetterie.order.domain.Order;
import com.palais.billetterie.order.repository.OrderRepository;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.palais.billetterie.common.exceptions.BadRequestException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;

@Service
public class StripeService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

        private final boolean fakeMode;

        public StripeService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
                        @Value("${app.stripe.secret}") String stripeSecret,
                        @Value("${app.stripe.fake:false}") boolean fakeMode
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
                this.fakeMode = fakeMode || stripeSecret == null || stripeSecret.isBlank() || "sk_test_xxx".equals(stripeSecret);
                if (!this.fakeMode) {
                        Stripe.apiKey = stripeSecret;
                }
    }

        @Transactional
                public Map<String, Object> createPaymentIntent(UUID orderId) throws Exception {
                Objects.requireNonNull(orderId, "orderId is required");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getAmount())
                .status(PaymentStatus.PENDING)
                .provider("STRIPE")
                .createdAt(Instant.now())
                .build();

        Objects.requireNonNull(payment, "payment must not be null");
        paymentRepository.save(payment);

        String providerPaymentId;
        String clientSecret;
        if (fakeMode) {
            providerPaymentId = "pi_fake_" + UUID.randomUUID();
            clientSecret = providerPaymentId + "_secret_" + UUID.randomUUID();
        } else {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (order.getAmount() * 100)) // en centimes
                    .setCurrency("eur")
                    .putMetadata("paymentId", payment.getId().toString())
                    .putMetadata("orderId", orderId.toString())
                    .build();

            PaymentIntent intent;
            try {
                intent = PaymentIntent.create(params);
            } catch (Exception e) {
                throw new BadRequestException("Échec de création PaymentIntent: " + e.getMessage());
            }
            providerPaymentId = intent.getId();
            clientSecret = intent.getClientSecret();
        }

        payment.setProviderPaymentId(providerPaymentId);
        Objects.requireNonNull(payment, "payment must not be null");
        paymentRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("clientSecret", clientSecret);
        response.put("paymentId", payment.getId());

                return response;
        }

        /**
         * Create a refund in Stripe for the given payment.
         * @param paymentId Payment id in our database
         * @param amountCents amount in cents (optional for full refund)
         * @return provider refund id
         */
        @Transactional
        public String createRefund(UUID paymentId, Long amountCents) throws Exception {
                Objects.requireNonNull(paymentId, "paymentId is required");
                Payment payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new RuntimeException("Payment not found"));

                if (payment.getProviderPaymentId() == null || payment.getProviderPaymentId().isBlank()) {
                        throw new RuntimeException("Provider PaymentIntent manquant");
                }

                RefundCreateParams.Builder builder = RefundCreateParams.builder()
                                .setPaymentIntent(payment.getProviderPaymentId());

                if (amountCents != null && amountCents > 0) {
                        builder.setAmount(amountCents);
                }

                try {
                        Refund stripeRefund = Refund.create(builder.build());
                        return stripeRefund.getId();
                } catch (Exception e) {
                        throw new RuntimeException("Stripe refund error: " + e.getMessage());
                }
        }
}