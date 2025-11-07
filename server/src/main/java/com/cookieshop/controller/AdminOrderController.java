package com.cookieshop.controller;

import com.cookieshop.dto.OrderDTO;
import com.cookieshop.entity.User;
import com.cookieshop.service.OrderService;
import com.cookieshop.util.AdminActionLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AdminOrderController {

    private final OrderService orderService;
    private final AdminActionLogger adminLogger;

    @Autowired
    public AdminOrderController(OrderService orderService, AdminActionLogger adminLogger) {
        this.orderService = orderService;
        this.adminLogger = adminLogger;
    }

    @GetMapping
    public ResponseEntity<?> listAllOrders(HttpServletRequest request) {
        User admin = adminLogger.verifyAdmin(request);
        List<OrderDTO> orders = orderService.listAllOrders();
        adminLogger.log(admin, "Viewed all orders (" + orders.size() + ")");
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id, HttpServletRequest request) {
        User admin = adminLogger.verifyAdmin(request);
        try {
            OrderDTO dto = orderService.getOrderById(id);
            adminLogger.log(admin, "Viewed order #" + id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody Map<String, Object> updates, HttpServletRequest request) {
        User admin = adminLogger.verifyAdmin(request);
        try {
            OrderDTO updated = orderService.adminUpdateOrder(id, updates);
            adminLogger.log(admin, "Updated order #" + id + " with fields " + updates.keySet());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to update order: " + ex.getMessage());
        }
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        User admin = adminLogger.verifyAdmin(request);
        String status = body.get("status");
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing 'status' in request body");
        }
        try {
            OrderDTO dto = orderService.changeStatus(id, status.trim());
            adminLogger.log(admin, "Changed status of order #" + id + " to " + status);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to change status: " + ex.getMessage());
        }
    }
}
