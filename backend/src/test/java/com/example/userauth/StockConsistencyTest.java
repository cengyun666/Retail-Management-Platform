package com.example.userauth;

import com.example.userauth.entity.Goods;
import com.example.userauth.entity.Order;
import com.example.userauth.entity.OrderItem;
import com.example.userauth.entity.User;
import com.example.userauth.repository.GoodsRepository;
import com.example.userauth.repository.OrderRepository;
import com.example.userauth.repository.UserRepository;
import com.example.userauth.service.GoodsService;
import com.example.userauth.service.OrderService;
import com.example.userauth.dto.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class StockConsistencyTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    private Long testGoodsId;
    private Long testUserId;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        // 1. 找到一个可用的用户
        User user = userRepository.findByUsername("testuser");
        if (user == null) {
            user = userRepository.findAll().get(0); // 使用第一个用户
        }
        testUserId = user.getId();

        // 2. 找到一个库存大于0的商品
        List<Goods> goodsList = goodsRepository.findAll();
        Goods goods = goodsList.stream()
                .filter(g -> g.getStock() > 5)
                .findFirst()
                .orElse(null);
        if (goods == null) {
            // 如果没有找到，创建一个新商品
            goods = new Goods();
            goods.setName("测试商品");
            goods.setPrice(new BigDecimal("100.00"));
            goods.setStock(10);
            goods.setDescription("测试用商品");
            goods.setMerchantId(1L);
            goods.setIsDeleted(false);
            goodsRepository.save(goods);
        }
        testGoodsId = goods.getId();
        System.out.println("测试商品ID: " + testGoodsId + ", 初始库存: " + goods.getStock());
    }

    @Test
    void testOrderCancelStockRecovery() {
        // 1. 获取商品初始库存
        Goods initialGoods = goodsRepository.findById(testGoodsId).orElseThrow();
        int initialStock = initialGoods.getStock();

        // 2. 创建一个测试订单
        Order order = new Order();
        order.setUserId(testUserId);
        order.setAddress("测试地址");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setPayAmount(new BigDecimal("100.00"));
        order.setStatus(1); // 待付款状态
        order.setRemark("测试订单");

        // 创建订单项
        List<OrderItem> items = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setGoodsId(testGoodsId);
        item.setQuantity(1);
        item.setPrice(new BigDecimal("100.00"));
        item.setGoodsName(initialGoods.getName());
        item.setOrder(order); // 设置订单关联
        items.add(item);

        order.setOrderItems(items);

        // 创建订单（这会自动扣减库存）
        Order savedOrder = orderService.createOrder(order);
        assertNotNull(savedOrder);
        assertNotNull(savedOrder.getId());

        // 3. 验证库存是否正确扣减
        Goods goodsAfterOrder = goodsRepository.findById(testGoodsId).orElseThrow();
        assertEquals(initialStock - 1, goodsAfterOrder.getStock(), "创建订单后库存未正确扣减");
        System.out.println("创建订单后库存: " + goodsAfterOrder.getStock());

        // 4. 取消订单
        // 创建一个简单的UserInfo对象
        UserInfo userInfo = new UserInfo();
        userInfo.setId(testUserId);
        userInfo.setRole("user"); // 添加用户角色
        orderService.cancelOrder(savedOrder.getId(), userInfo);

        // 5. 验证库存是否正确恢复
        Goods goodsAfterCancel = goodsRepository.findById(testGoodsId).orElseThrow();
        assertEquals(initialStock, goodsAfterCancel.getStock(), "取消订单后库存未正确恢复");
        System.out.println("取消订单后库存: " + goodsAfterCancel.getStock());

        System.out.println("✅ 订单取消后库存恢复测试通过！");
    }

    @Test
    void testConcurrentStockUpdate() {
        // 测试并发情况下的库存更新
        // 1. 获取商品初始库存
        Goods initialGoods = goodsRepository.findById(testGoodsId).orElseThrow();
        int initialStock = initialGoods.getStock();
        System.out.println("并发测试初始库存: " + initialStock);

        // 2. 模拟并发增加库存
        final int increaseAmount = 5;
        final int threadCount = 10;

        // 使用CountDownLatch来等待所有线程完成
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount);
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    goodsService.increaseStock(testGoodsId, increaseAmount);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executor.shutdown();

        // 3. 验证最终库存是否正确
        Goods finalGoods = goodsRepository.findById(testGoodsId).orElseThrow();
        int expectedStock = initialStock + (increaseAmount * threadCount);
        assertEquals(expectedStock, finalGoods.getStock(), "并发增加库存后最终库存不正确");
        System.out.println("并发增加库存后最终库存: " + finalGoods.getStock() + ", 预期: " + expectedStock);

        System.out.println("✅ 并发库存更新测试通过！");
    }
}