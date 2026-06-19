package com.example.userauth;

import com.example.userauth.entity.Category;
import com.example.userauth.entity.Goods;
import com.example.userauth.repository.CategoryRepository;
import com.example.userauth.repository.GoodsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品管理测试类
 */
//@SpringBootApplication
@ComponentScan("com.example.userauth")
public class GoodsManagementTest {

    public static void main(String[] args) {
        SpringApplication.run(GoodsManagementTest.class, args);
    }

    @Bean
    public CommandLineRunner testGoodsManagement(
            @Autowired CategoryRepository categoryRepository,
            @Autowired GoodsRepository goodsRepository) {
        return args -> {
            System.out.println("开始测试商品管理功能...");

            // 清理现有数据
            goodsRepository.deleteAll();
            categoryRepository.deleteAll();

            // 创建商品分类
            Category electronics = new Category();
            electronics.setName("电子产品");
            electronics.setDescription("各种电子设备和配件");
            electronics.setSortOrder(1);
            electronics.setStatus(1);
            electronics.setCreateTime(LocalDateTime.now());
            electronics.setUpdateTime(LocalDateTime.now());
            categoryRepository.save(electronics);

            Category clothing = new Category();
            clothing.setName("服装");
            clothing.setDescription("各种服装和配饰");
            clothing.setSortOrder(2);
            clothing.setStatus(1);
            clothing.setCreateTime(LocalDateTime.now());
            clothing.setUpdateTime(LocalDateTime.now());
            categoryRepository.save(clothing);

            System.out.println("创建了2个商品分类");

            // 创建商品
            Goods phone = new Goods();
            phone.setName("智能手机");
            phone.setCategoryId(electronics.getId());
            phone.setPrice(new BigDecimal("2999.99"));
            phone.setStock(100);
            phone.setDescription("高性能智能手机");
            phone.setImage("phone.jpg");
            phone.setStatus(1);
            phone.setCreateTime(LocalDateTime.now());
            phone.setUpdateTime(LocalDateTime.now());
            goodsRepository.save(phone);

            Goods laptop = new Goods();
            laptop.setName("笔记本电脑");
            laptop.setCategoryId(electronics.getId());
            laptop.setPrice(new BigDecimal("5999.99"));
            laptop.setStock(50);
            laptop.setDescription("高性能笔记本电脑");
            laptop.setImage("laptop.jpg");
            laptop.setStatus(1);
            laptop.setCreateTime(LocalDateTime.now());
            laptop.setUpdateTime(LocalDateTime.now());
            goodsRepository.save(laptop);

            Goods shirt = new Goods();
            shirt.setName("T恤");
            shirt.setCategoryId(clothing.getId());
            shirt.setPrice(new BigDecimal("99.99"));
            shirt.setStock(200);
            shirt.setDescription("纯棉T恤");
            shirt.setImage("shirt.jpg");
            shirt.setStatus(1);
            shirt.setCreateTime(LocalDateTime.now());
            shirt.setUpdateTime(LocalDateTime.now());
            goodsRepository.save(shirt);

            System.out.println("创建了3个商品");

            // 查询所有商品
            Iterable<Goods> allGoods = goodsRepository.findAll();
            int count = 0;
            for (Goods goods : allGoods) {
                count++;
                System.out.println("商品" + count + ": " + goods.getName() + ", 价格: " + goods.getPrice());
            }

            // 根据分类查询商品
            Iterable<Goods> electronicsGoods = goodsRepository.findByCategoryIdAndIsDeletedFalse(electronics.getId());
            System.out.println("电子产品分类下的商品数量: " + ((java.util.Collection<Goods>) electronicsGoods).size());

            // 根据商品名称模糊查询
            Iterable<Goods> searchResults = goodsRepository.findByNameContainingAndIsDeletedFalse("手机", null);
            System.out.println("名称包含'手机'的商品数量: " + ((java.util.Collection<Goods>) searchResults).size());

            // 更新商品信息
            Goods foundGoods = goodsRepository.findByNameAndIsDeletedFalse("智能手机");
            if (foundGoods != null) {
                foundGoods.setPrice(new BigDecimal("2799.99"));
                foundGoods.setStock(80);
                foundGoods.setUpdateTime(LocalDateTime.now());
                goodsRepository.save(foundGoods);
                System.out.println("更新了商品信息");
            }

            System.out.println("商品管理功能测试完成！");
        };
    }
}