package com.cratemind.deliveryservice.service;

import com.cratemind.common.event.DeliveryScheduledEvent;
import com.cratemind.common.event.PackCreatedEvent;
import com.cratemind.deliveryservice.entity.Delivery;
import com.cratemind.deliveryservice.entity.DeliveryStatus;
import com.cratemind.deliveryservice.repository.DeliveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
public class DeliveryService {
    private final DeliveryRepository repo;
    private final KafkaTemplate<String, DeliveryScheduledEvent> kafkaTemplate;
    public DeliveryService(DeliveryRepository repo, KafkaTemplate<String, DeliveryScheduledEvent> kafkaTemplate) {
        this.repo = repo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order.packed", groupId = "delivery-group")
    private void createDeliver(PackCreatedEvent event) {
        UUID msgId = event.message_id();
        final String truck_id = "1";
        final Instant slot = Instant.now().plusSeconds(7200);

        log.info("Received Event: {} with Message ID: {}", event, msgId);

        Delivery delivery = new Delivery();
        delivery.setTruck_id(truck_id);
        delivery.setDelivery_slot(slot);
        delivery.setOrder_id(msgId);
        delivery.setStatus(DeliveryStatus.SCHEDULED);
        repo.save(delivery);

        DeliveryScheduledEvent deliveryEvent = new DeliveryScheduledEvent(msgId,truck_id,slot);

        kafkaTemplate.send("delivery.scheduled",deliveryEvent);
        log.info("Order {} scheduled for delivery on Truck {}",delivery.getId(),truck_id);
    }

}
