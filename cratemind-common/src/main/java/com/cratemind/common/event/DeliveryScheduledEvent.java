package com.cratemind.common.event;

import java.time.Instant;
import java.util.UUID;

public record DeliveryScheduledEvent (UUID orderId, String truckId, Instant deliverySlot)
{

}
