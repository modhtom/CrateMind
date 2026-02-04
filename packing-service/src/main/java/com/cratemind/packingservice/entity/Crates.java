package com.cratemind.packingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name="crates")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Crates {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;
    @Column(name = "order_id")
    private UUID orderId;
    @Enumerated(EnumType.STRING)
    private CrateType type;
    private int weight;
}
