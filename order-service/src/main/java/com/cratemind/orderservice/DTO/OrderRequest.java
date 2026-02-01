package com.cratemind.orderservice.DTO;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String customerId;
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        private String productId;
        private int quantity;
    }
}