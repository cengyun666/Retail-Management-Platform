package com.example.userauth;

import com.example.userauth.entity.OrderItem;
import com.example.userauth.repository.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class DebugOrderItemTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    public void testFindOrderItemsById() {
        Long orderId = 12L;
        System.out.println("正在查询订单ID为" + orderId + "的订单项...");
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);
        System.out.println("查询结果：");
        System.out.println("订单项数量：" + orderItems.size());
        for (OrderItem item : orderItems) {
            System.out.println("订单项ID：" + item.getId());
            System.out.println("商品ID：" + item.getGoodsId());
            System.out.println("商品名称：" + item.getGoodsName());
            System.out.println("价格：" + item.getPrice());
            System.out.println("数量：" + item.getQuantity());
            System.out.println("=================================");
        }
    }
}