package com.example.userauth;

import com.example.userauth.entity.OrderItem;
import com.example.userauth.repository.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class OrderItemTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    public void testFindByOrderId() {
        Long orderId = 10L;
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        System.out.println("Order " + orderId + " has " + orderItems.size() + " active items:");
        for (OrderItem item : orderItems) {
            System.out.println("Active Item: " + item.getId() + ", GoodsId: " + item.getGoodsId() + ", GoodsName: " + item.getGoodsName() + ", IsDeleted: " + item.getIsDeleted());
        }
        
        // 查询所有订单项，包括已删除的
        List<OrderItem> allOrderItems = orderItemRepository.findAllByOrderId(orderId);
        System.out.println("Order " + orderId + " has " + allOrderItems.size() + " total items (including deleted):");
        for (OrderItem item : allOrderItems) {
            System.out.println("Total Item: " + item.getId() + ", GoodsId: " + item.getGoodsId() + ", GoodsName: " + item.getGoodsName() + ", IsDeleted: " + item.getIsDeleted());
        }
    }
}