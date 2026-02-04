package com.cratemind.inventoryservice.service;

import com.cratemind.common.event.InventoryCreatedEvent;
import com.cratemind.common.event.OrderCreatedEvent;
import com.cratemind.common.event.OrderItem;
import com.cratemind.inventoryservice.entity.Inventory;
import com.cratemind.inventoryservice.entity.InventoryStatus;
import com.cratemind.inventoryservice.entity.IdempotencyLog;
import com.cratemind.inventoryservice.repository.IdempotencyLogRepository;
import com.cratemind.inventoryservice.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class InventoryService {
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private IdempotencyLogRepository idempotencyLogRepository;

    private final KafkaTemplate<String, InventoryCreatedEvent> kafkaTemplate;

    public InventoryService(KafkaTemplate<String, InventoryCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order.created", groupId = "inventory-group")
    @Transactional
    public void updateInv(OrderCreatedEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String messageId) {
        log.info("Received Event: {} with Message ID: {}", event, messageId);
        UUID msgId = (messageId != null) ? UUID.fromString(messageId) : event.orderId();

        if (idempotencyLogRepository.existsById(msgId)) {
            log.info("Message {} already processed. Skipping.", msgId);
            return;
        }

        try {
            boolean reservationSuccess = true;
            for (OrderItem item : event.items()) {
                Inventory product = inventoryRepository.findByProductId(item.productId());
                if (product != null && product.getQuantity() >= item.quantity()) {
                    product.setQuantity(product.getQuantity() - item.quantity());
                    product.setReservedQty(product.getReservedQty() + item.quantity());
                    inventoryRepository.save(product);
                } else {
                    log.error("Insufficient stock for product: {}", item.productId());
                    reservationSuccess = false;
                    break;
                }
            }

            IdempotencyLog logEntry = new IdempotencyLog();
            logEntry.setMessage_id(msgId);
            logEntry.setCreated_at(Instant.now());
            if (reservationSuccess) {
                logEntry.setStatus(InventoryStatus.PENDING);
                InventoryCreatedEvent invEvent = mapToOrderEvent(logEntry,event.items());
                kafkaTemplate.send("inventory.reserved", logEntry.getMessage_id().toString(), invEvent);
            } else {
                logEntry.setStatus(InventoryStatus.FAILED);
                InventoryCreatedEvent invEvent = mapToOrderEvent(logEntry,event.items());
                kafkaTemplate.send("inventory.released", logEntry.getMessage_id().toString(), invEvent);
                throw new RuntimeException("Inventory reservation failed due to insufficient stock");
            }

            idempotencyLogRepository.save(logEntry);
            log.info("Successfully processed message {}", msgId);

        } catch (Exception e) {
            log.error("Error processing inventory update for message {}", msgId, e);
            throw e;
        }
    }

    private InventoryCreatedEvent mapToOrderEvent(IdempotencyLog inv, List<OrderItem> items){
        return new InventoryCreatedEvent(
                inv.getMessage_id(),
                inv.getStatus().toString(),
                inv.getCreated_at(),
                items
        );
    }
}