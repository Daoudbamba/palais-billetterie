package com.palais.billetterie.scan.repository;

import com.palais.billetterie.scan.domain.ScanLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScanLogRepository extends JpaRepository<ScanLog, UUID> {}
