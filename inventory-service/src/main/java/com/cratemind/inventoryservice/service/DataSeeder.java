package com.cratemind.inventoryservice.service;

import com.cratemind.inventoryservice.entity.Inventory;
import com.cratemind.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final InventoryRepository inventoryRepository;

    public DataSeeder(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) {
        if (inventoryRepository.count() == 0) {
            Inventory inventory = new Inventory();
            inventory.setProductId("apple");
            inventory.setQuantity(100);

            inventoryRepository.save(inventory);
        }
    }
}