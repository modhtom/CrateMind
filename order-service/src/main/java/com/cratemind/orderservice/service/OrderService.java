package com.cratemind.orderservice.service;

import com.cratemind.common.event.OrderCreatedEvent;
import com.cratemind.orderservice.DTO.OrderRequest;
import com.cratemind.orderservice.entity.Order;
import com.cratemind.orderservice.entity.OrderItem;
import com.cratemind.orderservice.entity.OrderStatus;
import com.cratemind.orderservice.repository.OrderRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository repo;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderService(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public UUID createOrder(OrderRequest req){
        Order savedOrder = mapToOrder(req);
        repo.save(savedOrder);
        try {
            OrderCreatedEvent event = mapToOrderEvent(savedOrder);
            kafkaTemplate.send("order.created", event.orderId().toString(), event);
            log.info("Event published for Order: {}", savedOrder.getId());
        }catch (Exception e){
            log.error("FAILED to publish event for Order: {}. Reason: {}", savedOrder.getId(), e.getMessage());
        }
        return savedOrder.getId();
    }

    @Transactional
    protected Order mapToOrder(OrderRequest req){
        Order order = new Order();
        order.setCustomerId(req.getCustomerId());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());
        List<OrderItem> entityItems = req.getItems().stream().map(i->{
            OrderItem item = new OrderItem();
            item.setProductId(i.getProductId());
            item.setQuantity(i.getQuantity());
            return item;
        }).collect(Collectors.toList());
        order.setItems(entityItems);
        return order;
    }

    private OrderCreatedEvent mapToOrderEvent(Order order){
        List<com.cratemind.common.event.OrderItem> eventItems = order.getItems().stream()
                .map(item -> new com.cratemind.common.event.OrderItem(item.getProductId(), item.getQuantity()))
                .toList();

        return new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                eventItems
        );
    }
}
