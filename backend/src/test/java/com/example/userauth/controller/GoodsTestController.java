package com.example.userauth.controller;

import com.example.userauth.entity.Category;
import com.example.userauth.entity.Goods;
import com.example.userauth.service.CategoryService;
import com.example.userauth.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品管理API测试控制器
 */
@RestController
@RequestMapping("/api/test/goods")
public class GoodsTestController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 测试商品管理功能
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testGoodsManagement() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 测试分类功能
            Map<String, Object> categoryResult = testCategoryManagement();
            result.put("categoryTest", categoryResult);

            // 2. 测试商品功能
            Map<String, Object> goodsResult = testGoodsManagement((Long) categoryResult.get("categoryId"));
            result.put("goodsTest", goodsResult);

            result.put("success", true);
            result.put("message", "商品管理功能测试成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "测试失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    private Map<String, Object> testCategoryManagement() {
        Map<String, Object> result = new HashMap<>();

        // 创建分类
        Category electronics = new Category();
        electronics.setName("电子产品");
        electronics.setDescription("各种电子设备和配件");
        electronics.setSortOrder(1);
        electronics.setStatus(1);

        Category savedCategory = categoryService.addCategory(electronics);
        result.put("categoryId", savedCategory.getId());
        result.put("categoryName", savedCategory.getName());
        result.put("message", "创建分类成功");

        return result;
    }

    private Map<String, Object> testGoodsManagement(Long categoryId) {
        Map<String, Object> result = new HashMap<>();

        // 创建商品
        Goods phone = new Goods();
        phone.setName("智能手机");
        phone.setCategoryId(categoryId);
        phone.setPrice(new BigDecimal("2999.99"));
        phone.setStock(100);
        phone.setDescription("高性能智能手机");
        phone.setImage("phone.jpg");
        phone.setStatus(1);

        Goods savedGoods = goodsService.addGoods(phone);
        result.put("goodsId", savedGoods.getId());
        result.put("goodsName", savedGoods.getName());
        result.put("goodsPrice", savedGoods.getPrice());
        result.put("message", "创建商品成功");

        // 查询商品
        Goods foundGoods = goodsService.getGoodsById(savedGoods.getId());
        result.put("foundGoodsName", foundGoods.getName());
        result.put("queryMessage", "查询商品成功");

        // 更新商品
        foundGoods.setPrice(new BigDecimal("2799.99"));
        goodsService.updateGoods(foundGoods.getId(), foundGoods);
        result.put("updateMessage", "更新商品价格成功");

        // 查询分类下的商品
        List<Goods> goodsList = goodsService.getGoodsByCategoryId(categoryId, null).getContent();
        result.put("goodsCount", goodsList.size());
        result.put("listMessage", "分类下的商品数量: " + goodsList.size());

        return result;
    }

    /**
     * 清理测试数据
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupTestData() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 查找测试分类
            List<Category> categories = categoryService.getAllCategories();
            for (Category category : categories) {
                if ("电子产品".equals(category.getName())) {
                    // 删除分类下的所有商品
                    List<Goods> goodsList = goodsService.getGoodsByCategoryId(category.getId(), null).getContent();
                    for (Goods goods : goodsList) {
                        goodsService.deleteGoods(goods.getId());
                    }
                    
                    // 删除分类
                    categoryService.deleteCategory(category.getId());
                }
            }

            result.put("success", true);
            result.put("message", "测试数据清理成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "清理失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}