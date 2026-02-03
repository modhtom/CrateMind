package com.cratemind.inventoryservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="idempotency_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyLog {
    @Id
    private UUID message_id;
    private Instant created_at;
    private InventoryStatus status;
}
