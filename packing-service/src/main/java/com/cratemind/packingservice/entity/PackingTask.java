package com.cratemind.packingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name="packing_tasks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackingTask {
    @Id
    private UUID order_id;
    @Enumerated(EnumType.STRING)
    private PackingStatus status;
    private String failure_reason;
}
