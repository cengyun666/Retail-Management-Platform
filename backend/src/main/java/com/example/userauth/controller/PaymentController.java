package com.example.userauth.controller;

import com.example.userauth.entity.Payment;
import com.example.userauth.service.PaymentService;
import com.example.userauth.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Payment>> createPayment(@RequestBody Map<String, Object> request) {
        try {
            Long orderId = Long.parseLong(request.get("orderId").toString());
            Long userId = Long.parseLong(request.get("userId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String paymentMethod = (String) request.get("paymentMethod");
            
            Payment payment = paymentService.createPayment(orderId, userId, amount, paymentMethod);
            return ResponseEntity.ok(ApiResponse.success(payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentStatus(@PathVariable Long id) {
        try {
            Payment payment = paymentService.getPaymentStatus(id);
            if (payment == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(ApiResponse.success(payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/callback")
    public ResponseEntity<ApiResponse<Payment>> handlePaymentCallback(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String status = (String) request.get("status");
            String transactionId = (String) request.get("transactionId");
            
            Payment payment = paymentService.handlePaymentCallback(id, status, transactionId);
            return ResponseEntity.ok(ApiResponse.success(payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByOrderId(@PathVariable Long orderId) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(ApiResponse.success(payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Payment>> cancelPayment(@PathVariable Long id) {
        try {
            Payment payment = paymentService.cancelPayment(id);
            return ResponseEntity.ok(ApiResponse.success(payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<Payment>> refundPayment(@PathVariable Long id) {
        try {
            Payment payment = paymentService.refundPayment(id);
            return ResponseEntity.ok(ApiResponse.success(payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}