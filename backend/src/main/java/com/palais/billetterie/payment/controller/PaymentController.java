package com.palais.billetterie.payment.controller;

import com.palais.billetterie.payment.domain.Payment;
import com.palais.billetterie.payment.dto.PaymentCreateRequest;
import com.palais.billetterie.payment.dto.PaymentUpdateRequest;
import com.palais.billetterie.payment.service.PaymentService;
import com.palais.billetterie.payment.stripe.StripeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.palais.billetterie.common.exceptions.BadRequestException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;
    private final StripeService stripeService;

    public PaymentController(PaymentService service, StripeService stripeService) {
        this.service = service;
        this.stripeService = stripeService;
    }

    @GetMapping
    public List<Payment> list() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Payment get(@PathVariable("id") UUID id) {
        return service.getById(id);
    }

    @PostMapping
    public Payment create(@RequestBody @Valid PaymentCreateRequest req) {
        return service.create(req.getOrderId(), req.getAmount(), req.getProvider());
    }
    
    @PostMapping("/stripe/create-intent")
    public Map<String, Object> createIntent(@RequestParam("orderId") UUID orderId) {
        try {
            return stripeService.createPaymentIntent(orderId);
        } catch (Exception e) {
            throw new BadRequestException("Création PaymentIntent échouée: " + e.getMessage());
        }
    }


    @PutMapping("/{id}")
    public Payment update(@PathVariable("id") UUID id,
                          @RequestBody @Valid PaymentUpdateRequest req) {
        return service.updateStatus(id, req.getStatus(), req.getProviderPaymentId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") UUID id) {
        service.delete(id);
    }
}