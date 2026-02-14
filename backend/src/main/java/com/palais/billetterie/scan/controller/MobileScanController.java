package com.palais.billetterie.scan.controller;

import com.palais.billetterie.scan.dto.ScanRequest;
import com.palais.billetterie.scan.dto.ScanResponse;
import com.palais.billetterie.scan.service.ScanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scan")
public class MobileScanController {

    private final ScanService scanService;

    public MobileScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping("/verify")
    public ResponseEntity<ScanResponse> verify(@Valid @RequestBody ScanRequest req) {
        return ResponseEntity.ok(scanService.verify(req));
    }

    @PostMapping("/use")
    public ResponseEntity<ScanResponse> use(@Valid @RequestBody ScanRequest req) {
        return ResponseEntity.ok(scanService.use(req));
    }
}
