package com.palais.billetterie.refund.controller;

import com.palais.billetterie.refund.domain.Refund;
import com.palais.billetterie.refund.dto.RefundCreateRequest;
import com.palais.billetterie.refund.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService service;

    public RefundController(RefundService service) {
        this.service = service;
    }

    @GetMapping
    public List<Refund> list() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Refund get(@PathVariable("id") UUID id) {
        return service.getById(id);
    }

    @PostMapping
    public Refund request(@RequestBody @Valid RefundCreateRequest req) {
        return service.requestRefund(req.getPaymentId(), req.getAmount());
    }

    @PostMapping("/{id}/success")
    public Refund markSuccess(@PathVariable("id") UUID id,
                              @RequestParam String providerRefundId) {
        return service.markRefundSuccess(id, providerRefundId);
    }

    @PostMapping("/{id}/failed")
    public Refund markFailed(@PathVariable("id") UUID id) {
        return service.markRefundFailed(id);
    }
}