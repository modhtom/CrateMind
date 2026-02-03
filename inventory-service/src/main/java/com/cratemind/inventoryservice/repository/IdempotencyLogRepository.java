package com.cratemind.inventoryservice.repository;

import com.cratemind.inventoryservice.entity.IdempotencyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdempotencyLogRepository extends JpaRepository<IdempotencyLog, UUID> {
}
