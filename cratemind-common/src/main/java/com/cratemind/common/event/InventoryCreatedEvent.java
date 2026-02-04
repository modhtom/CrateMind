package com.cratemind.common.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InventoryCreatedEvent (UUID message_id, String status, Instant created_at, List<OrderItem>items) {
}
