package com.example.userauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.example.userauth.repository.CategoryRepository;
import com.example.userauth.repository.GoodsRepository;
import com.example.userauth.entity.Category;
import com.example.userauth.entity.Goods;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

// @Component
@Order(1) // 确保这个组件在其他组件之后执行
public class DatabaseDataChecker implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private GoodsRepository goodsRepository;

    @Override
    public void run(String... args) throws Exception {
        // 只在特定参数下运行，避免每次启动都执行
        boolean shouldRun = false;
        for (String arg : args) {
            if ("checkdb".equals(arg)) {
                shouldRun = true;
                break;
            }
        }
        
        if (shouldRun) {
            System.out.println("=== 检查数据库分类和商品数据 ===");
            
            // 检查分类数据
            System.out.println("\n--- 分类数据 ---");
            Iterable<Category> categories = categoryRepository.findAll();
            int categoryCount = 0;
            for (Category category : categories) {
                System.out.println("分类ID: " + category.getId() + 
                                 ", 名称: " + category.getName() + 
                                 ", 描述: " + category.getDescription() + 
                                 ", 状态: " + category.getStatus() + 
                                 ", 排序: " + category.getSortOrder());
                categoryCount++;
            }
            System.out.println("总分类数: " + categoryCount);
            
            // 检查商品数据
            System.out.println("\n--- 商品数据 ---");
            Page<Goods> goodsPage = goodsRepository.findByIsDeletedFalse(PageRequest.of(0, 100));
            int goodsCount = 0;
            for (Goods goods : goodsPage.getContent()) {
                System.out.println("商品ID: " + goods.getId() + 
                                 ", 名称: " + goods.getName() + 
                                 ", 价格: " + goods.getPrice() + 
                                 ", 库存: " + goods.getStock() + 
                                 ", 分类ID: " + goods.getCategoryId() + 
                                 ", 状态: " + goods.getStatus());
                goodsCount++;
            }
            System.out.println("总商品数: " + goodsCount);
            
            System.out.println("\n=== 数据库检查完成 ===");
            
            // 检查完成后退出应用
            System.exit(0);
        }
    }
}