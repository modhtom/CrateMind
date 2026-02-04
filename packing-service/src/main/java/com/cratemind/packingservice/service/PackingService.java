package com.cratemind.packingservice.service;

import com.cratemind.common.event.InventoryCreatedEvent;
import com.cratemind.common.event.OrderItem;
import com.cratemind.common.event.PackCreatedEvent;
import com.cratemind.packingservice.entity.CrateType;
import com.cratemind.packingservice.entity.Crates;
import com.cratemind.packingservice.entity.PackingStatus;
import com.cratemind.packingservice.entity.PackingTask;
import com.cratemind.packingservice.repository.CrateRepository;
import com.cratemind.packingservice.repository.PackingTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class PackingService {
    private final PackingTaskRepository packRepo;
    private final CrateRepository crateRepo;
    private final KafkaTemplate<String, PackCreatedEvent> kafkaTemplate;
    public PackingService(PackingTaskRepository packRepo, CrateRepository crateRepo, KafkaTemplate<String, PackCreatedEvent> kafkaTemplate) {
        this.packRepo = packRepo;
        this.crateRepo = crateRepo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "inventory.reserved", groupId = "packing-group")
    public void pack(InventoryCreatedEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String messageId) {
        log.info("Received Event: {} with Message ID: {}", event, messageId);
        UUID msgId = (messageId != null) ? UUID.fromString(messageId) : event.message_id();

        if(event.status().equalsIgnoreCase("FAILED")){
            assert messageId != null;
            PackCreatedEvent packEvent = new PackCreatedEvent(UUID.fromString(messageId),0, 0);
            kafkaTemplate.send("order.failed_packing",packEvent);
            return;
        }
        List<OrderItem> items = event.items();
        PackingTask packTask = new PackingTask(msgId, PackingStatus.PENDING,"");
        packRepo.save(packTask);
        List<Crates> crates = packItems(msgId, items);
        crateRepo.saveAll(crates);
        packTask.setStatus(PackingStatus.COMPLETED);
        packRepo.save(packTask);
        PackCreatedEvent packEvent = new PackCreatedEvent(UUID.fromString(messageId),crates.size(), crates.stream().mapToInt(Crates::getWeight).sum());
        kafkaTemplate.send("order.packed",packEvent);
        log.info("Order {} with message Id {} packed into {} crates.", msgId,messageId,crates.size());
    }

    private static final int CRATE_CAPACITY = 5000; // grams
    private List<Crates> packItems(UUID orderId, List<OrderItem> items) {
        Map<String, Integer> productWeights = Map.of(
                "apple", 150,
                "monitor", 2000,
                "laptop", 1800,
                "keyboard", 700
        );

        List<Integer> weights = new ArrayList<>();
        for (OrderItem item : items) {
            int weight = productWeights.getOrDefault(item.productId(), 500);
            for (int i = 0; i < item.quantity(); i++) {
                weights.add(weight);
            }
        }

        weights.sort(Comparator.reverseOrder());

        List<Crates> crates = new ArrayList<>();
        for (int weight : weights) {
            boolean placed = false;
            for (Crates crate : crates) {
                if (crate.getWeight()+weight <= CRATE_CAPACITY) {
                    crate.setWeight(crate.getWeight()+weight);
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                Crates newCrate = new Crates();
                newCrate.setType(CrateType.LARGE);
                newCrate.setOrderId(orderId);
                newCrate.setWeight(weight);
                crates.add(newCrate);
            }
        }

        return crates;
    }
}
