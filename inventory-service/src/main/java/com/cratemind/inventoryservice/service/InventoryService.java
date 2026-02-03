package com.cratemind.inventoryservice.service;

import com.cratemind.common.event.OrderCreatedEvent;
import com.cratemind.common.event.OrderItem;
import com.cratemind.inventoryservice.entity.Inventory;
import com.cratemind.inventoryservice.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class InventoryService {
    @Autowired
    private InventoryRepository repo;

    @KafkaListener(topics = "order.created", groupId = "inventory-group")
    public void updateInv(OrderCreatedEvent event){
        log.info("Received Event: {}", event);
        for(OrderItem item:event.items()){
            Inventory product = repo.findByProductId(item.productId());
            if(product != null && (product.getQuantity()>0)) {
                int updatedQuantity = product.getQuantity() - item.quantity();
                if(updatedQuantity>=0){
                    product.setQuantity(updatedQuantity);
                    repo.save(product);
                }else{
                    log.error("Unavailable Items in Inventory.");
                }
            }else{
                log.error("Unavailable product in Inventory.");
            }
        }
    }
}