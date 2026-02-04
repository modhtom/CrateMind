package com.cratemind.deliveryservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity()
@Table(name="deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;
    private UUID order_id;
    private Instant delivery_slot;
    private String truck_id;
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
}