package com.cratemind.common.event;

import java.util.UUID;

public record PackCreatedEvent(UUID message_id, int totalCrates, long totalWeight) {
}
