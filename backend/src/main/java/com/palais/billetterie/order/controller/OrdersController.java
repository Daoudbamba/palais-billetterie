package com.palais.billetterie.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        // stub order creation
        String orderId = UUID.randomUUID().toString();
        return ResponseEntity.ok(Map.of(
            "orderId", orderId,
            "status", "PENDING_PAYMENT",
            "payload", body
        ));
    }
}