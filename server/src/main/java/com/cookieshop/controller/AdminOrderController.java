package com.cookieshop.controller;

import com.cookieshop.dto.OrderDTO;
import com.cookieshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<OrderDTO> listAll() {
        return orderService.listAllOrders();
    }

    @GetMapping("/{id}")
    public OrderDTO get(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            return ResponseEntity.ok(orderService.adminUpdateOrder(id, updates));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        try {
            return ResponseEntity.ok(orderService.changeStatus(id, status));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}


