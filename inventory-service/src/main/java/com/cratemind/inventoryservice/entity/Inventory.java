package com.cratemind.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "inventory_db")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;
    private String productId;
    private int quantity;
}
