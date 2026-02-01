package com.cratemind.orderservice.controller;

import com.cratemind.orderservice.DTO.OrderRequest;
import com.cratemind.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService service;

    @PostMapping
    public ResponseEntity<UUID> createOrder(@RequestBody OrderRequest req){
        return new ResponseEntity<>(service.createOrder(req), HttpStatusCode.valueOf(201));
    }

}
