package com.example.userauth;

import com.example.userauth.entity.Goods;
import com.example.userauth.entity.User;
import com.example.userauth.repository.GoodsRepository;
import com.example.userauth.repository.UserRepository;
import com.example.userauth.service.GoodsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class StockZeroAutoOfflineTest {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private UserRepository userRepository;

    private Goods testGoods;

    @BeforeEach
    public void setUp() {
        // 输出所有商品信息，以便调试
        System.out.println("\n=== setUp() - All goods in database: ===");
        List<Goods> allGoods = goodsRepository.findAll();
        for (Goods g : allGoods) {
            System.out.println("  ID: " + g.getId() + ", Name: " + g.getName() + ", Stock: " + g.getStock() + ", Status: " + g.getStatus() + ", IsDeleted: " + g.getIsDeleted());
        }
        
        // 找到一个可用的商品，如果没有找到，就创建一个
        Goods goods = allGoods.stream()
                .filter(g -> g.getStock() > 0 && g.getStatus() == 0 && !g.getIsDeleted()) // 确保库存>0且状态为上架且未删除
                .findFirst()
                .orElse(null);
        
        if (goods == null) {
            System.out.println("No suitable goods found, creating a new one");
            // 如果没有找到，创建一个新商品
            // 找到一个可用的用户作为商家
            User merchant = userRepository.findByUsername("admin");
            if (merchant == null) {
                merchant = userRepository.findAll().get(0); // 使用第一个用户
            }
            
            goods = new Goods();
            goods.setName("测试商品");
            goods.setPrice(java.math.BigDecimal.valueOf(100.00));
            goods.setStock(5); // 初始库存为5
            goods.setStatus(0); // 初始状态为上架（0）
            goods.setMerchantId(merchant.getId()); // 使用找到的商家用户ID
            goods.setLowStockThreshold(10);
            goods.setIsDeleted(false);
            
            // 先不设置categoryId，看看是否能保存成功
            testGoods = goodsRepository.save(goods);
            System.out.println("Created new goods: " + testGoods);
        } else {
            System.out.println("Found existing goods: " + goods);
            // 如果找到了现有的商品，直接使用它
            // 重置商品状态为上架且库存为10，以便测试
            goods.setStock(10);
            goods.setStatus(0);
            goods.setIsDeleted(false);
            testGoods = goodsRepository.save(goods);
            System.out.println("Reset goods: " + testGoods);
        }
        
        System.out.println("最终测试商品 - ID: " + testGoods.getId() + ", 库存: " + testGoods.getStock() + ", 状态: " + testGoods.getStatus() + ", IsDeleted: " + testGoods.getIsDeleted());
    }

    @Test
    public void testStockZeroAutoOffline() {
        // 输出测试前的商品信息
        System.out.println("Test testStockZeroAutoOffline - Before decreaseStock:");
        Goods initialGoods = goodsRepository.findById(testGoods.getId()).orElseThrow();
        System.out.println("  Goods ID: " + testGoods.getId());
        System.out.println("  Goods Stock: " + initialGoods.getStock());
        System.out.println("  Goods Status: " + initialGoods.getStatus());
        
        int initialStock = initialGoods.getStock();
        
        // 确保初始库存大于0且状态为上架
        assertEquals(0, initialGoods.getStatus()); // 初始状态为上架
        
        // 使用service层的方法进行库存扣减
        try {
            goodsService.decreaseStock(testGoods.getId(), initialStock);
            System.out.println("  Service decreaseStock executed successfully");
        } catch (Exception e) {
            System.out.println("  Service decreaseStock failed: " + e.getMessage());
            e.printStackTrace();
        }

        // 重新获取商品信息并输出
        Goods updatedGoods = goodsRepository.findById(testGoods.getId()).orElseThrow();
        System.out.println("  Goods Stock after all operations: " + updatedGoods.getStock());
        System.out.println("  Goods Status after all operations: " + updatedGoods.getStatus());
        
        // 验证库存为零后，商品状态是否自动变为下架
        assertEquals(0, updatedGoods.getStock()); // 库存应为零
        assertEquals(1, updatedGoods.getStatus()); // 状态应自动变为下架（1）
    }

    @Test
    public void testStockNotZeroNoAutoOffline() {
        // 获取初始库存
        Goods initialGoods = goodsRepository.findById(testGoods.getId()).orElseThrow();
        int initialStock = initialGoods.getStock();
        
        // 确保初始库存大于0且状态为上架
        assertEquals(0, initialGoods.getStatus()); // 初始状态为上架
        
        // 减少库存的数量（确保减少后库存不为零）
        int decreaseQuantity = Math.min(initialStock - 1, 3); // 减少3个或直到剩下1个库存
        
        // 减少库存但不为零
        goodsService.decreaseStock(testGoods.getId(), decreaseQuantity);

        // 验证库存不为零，商品状态仍为上架
        Goods updatedGoods = goodsRepository.findById(testGoods.getId()).orElseThrow();
        assertEquals(initialStock - decreaseQuantity, updatedGoods.getStock()); // 库存应为初始库存减decreaseQuantity
        assertEquals(0, updatedGoods.getStatus()); // 状态仍为上架（0）
    }
}