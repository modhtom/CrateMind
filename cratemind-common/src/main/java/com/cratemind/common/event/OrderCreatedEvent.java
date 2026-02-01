package com.cratemind.common.event;

import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(UUID orderId, String customerId, List<OrderItem> items) {
}
