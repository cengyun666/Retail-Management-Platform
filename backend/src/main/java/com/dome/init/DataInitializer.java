package com.dome.init;

import com.example.userauth.entity.Category;
import com.example.userauth.entity.Goods;
import com.example.userauth.repository.CategoryRepository;
import com.example.userauth.repository.GoodsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 初始化商品分类和商品数据
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private GoodsRepository goodsRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有分类数据
        if (categoryRepository.count() > 0) {
            logger.info("商品分类数据已存在，跳过初始化");
            return;
        }
        
        logger.info("开始初始化商品分类和商品数据...");
        
        // 初始化商品分类
        List<Category> categories = initCategories();
        
        // 初始化商品
        initGoods(categories);
        
        logger.info("商品分类和商品数据初始化完成");
    }
    
    /**
     * 初始化商品分类
     */
    private List<Category> initCategories() {
        List<Category> categories = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // 1. 食品饮料
        Category foodAndBeverage = new Category();
        foodAndBeverage.setName("食品饮料");
        foodAndBeverage.setDescription("各类食品和饮料，包括零食、饮料、粮油等");
        foodAndBeverage.setStatus(1);
        foodAndBeverage.setCreateTime(now);
        foodAndBeverage.setUpdateTime(now);
        foodAndBeverage.setIsDeleted(false);
        categories.add(foodAndBeverage);
        
        // 2. 生鲜果蔬
        Category freshProduce = new Category();
        freshProduce.setName("生鲜果蔬");
        freshProduce.setDescription("新鲜水果、蔬菜、肉类、水产等");
        freshProduce.setStatus(1);
        freshProduce.setCreateTime(now);
        freshProduce.setUpdateTime(now);
        freshProduce.setIsDeleted(false);
        categories.add(freshProduce);
        
        // 3. 日用百货
        Category dailyNecessities = new Category();
        dailyNecessities.setName("日用百货");
        dailyNecessities.setDescription("日常生活用品，包括洗护用品、家居清洁等");
        dailyNecessities.setStatus(1);
        dailyNecessities.setCreateTime(now);
        dailyNecessities.setUpdateTime(now);
        dailyNecessities.setIsDeleted(false);
        categories.add(dailyNecessities);
        
        // 4. 家用电器
        Category homeAppliances = new Category();
        homeAppliances.setName("家用电器");
        homeAppliances.setDescription("各类家用电器，包括小家电、大家电等");
        homeAppliances.setStatus(1);
        homeAppliances.setCreateTime(now);
        homeAppliances.setUpdateTime(now);
        homeAppliances.setIsDeleted(false);
        categories.add(homeAppliances);
        
        // 5. 个人护理
        Category personalCare = new Category();
        personalCare.setName("个人护理");
        personalCare.setDescription("个人护理用品，包括护肤品、化妆品等");
        personalCare.setStatus(1);
        personalCare.setCreateTime(now);
        personalCare.setUpdateTime(now);
        personalCare.setIsDeleted(false);
        categories.add(personalCare);
        
        // 保存分类
        List<Category> savedCategories = categoryRepository.saveAll(categories);
        logger.info("初始化了 {} 个商品分类", savedCategories.size());
        
        return savedCategories;
    }
    
    /**
     * 初始化商品
     */
    private void initGoods(List<Category> categories) {
        List<Goods> goodsList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // 为每个分类添加商品
        for (Category category : categories) {
            switch (category.getName()) {
                case "食品饮料":
                    // 添加食品饮料类商品
                    goodsList.add(createGoods("可口可乐 330ml", "经典可口可乐，冰爽口感", category.getId(), new BigDecimal("3.50"), 100, 10, now));
                    goodsList.add(createGoods("百事可乐 330ml", "百事可乐，年轻一代的选择", category.getId(), new BigDecimal("3.50"), 100, 10, now));
                    goodsList.add(createGoods("农夫山泉 550ml", "农夫山泉有点甜", category.getId(), new BigDecimal("2.00"), 200, 20, now));
                    goodsList.add(createGoods("康师傅红烧牛肉面", "经典红烧牛肉味", category.getId(), new BigDecimal("4.50"), 150, 15, now));
                    goodsList.add(createGoods("旺旺雪饼", "香脆可口，老少皆宜", category.getId(), new BigDecimal("5.00"), 80, 8, now));
                    goodsList.add(createGoods("奥利奥原味饼干", "扭一扭，舔一舔，泡一泡", category.getId(), new BigDecimal("8.50"), 60, 6, now));
                    goodsList.add(createGoods("乐事薯片原味", "乐事薯片，停不下来的美味", category.getId(), new BigDecimal("9.90"), 70, 7, now));
                    goodsList.add(createGoods("德芙巧克力", "纵享丝滑", category.getId(), new BigDecimal("12.50"), 50, 5, now));
                    break;
                    
                case "生鲜果蔬":
                    // 添加生鲜果蔬类商品
                    goodsList.add(createGoods("红富士苹果", "新鲜红富士苹果，脆甜多汁", category.getId(), new BigDecimal("8.90"), 100, 10, now));
                    goodsList.add(createGoods("香蕉", "进口香蕉，香甜软糯", category.getId(), new BigDecimal("6.50"), 80, 8, now));
                    goodsList.add(createGoods("西红柿", "新鲜西红柿，营养丰富", category.getId(), new BigDecimal("4.50"), 120, 12, now));
                    goodsList.add(createGoods("黄瓜", "新鲜黄瓜，清脆爽口", category.getId(), new BigDecimal("3.50"), 100, 10, now));
                    goodsList.add(createGoods("土豆", "新鲜土豆，耐储存", category.getId(), new BigDecimal("2.90"), 150, 15, now));
                    goodsList.add(createGoods("猪肉五花肉", "新鲜五花肉，肥瘦相间", category.getId(), new BigDecimal("28.00"), 60, 6, now));
                    goodsList.add(createGoods("鸡胸肉", "低脂高蛋白，健康之选", category.getId(), new BigDecimal("15.80"), 80, 8, now));
                    goodsList.add(createGoods("鸡蛋", "新鲜土鸡蛋，营养丰富", category.getId(), new BigDecimal("12.00"), 100, 10, now));
                    break;
                    
                case "日用百货":
                    // 添加日用百货类商品
                    goodsList.add(createGoods("蓝月亮洗衣液", "深层洁净，持久留香", category.getId(), new BigDecimal("29.90"), 50, 5, now));
                    goodsList.add(createGoods("维达抽纸", "柔软舒适，吸水性强", category.getId(), new BigDecimal("18.50"), 100, 10, now));
                    goodsList.add(createGoods("立白洗洁精", "去油污能力强，不伤手", category.getId(), new BigDecimal("12.90"), 80, 8, now));
                    goodsList.add(createGoods("六神花露水", "清凉舒爽，驱蚊止痒", category.getId(), new BigDecimal("15.50"), 60, 6, now));
                    goodsList.add(createGoods("洁丽雅毛巾", "纯棉材质，柔软舒适", category.getId(), new BigDecimal("19.90"), 70, 7, now));
                    goodsList.add(createGoods("超能洗衣粉", "去污力强，泡沫丰富", category.getId(), new BigDecimal("16.80"), 60, 6, now));
                    goodsList.add(createGoods("威猛先生洁厕灵", "强力去污，消毒杀菌", category.getId(), new BigDecimal("22.50"), 40, 4, now));
                    goodsList.add(createGoods("妙洁垃圾袋", "加厚耐用，不易破裂", category.getId(), new BigDecimal("14.90"), 100, 10, now));
                    break;
                    
                case "家用电器":
                    // 添加家用电器类商品
                    goodsList.add(createGoods("美的电饭煲", "智能电饭煲，多功能烹饪", category.getId(), new BigDecimal("199.00"), 30, 3, now));
                    goodsList.add(createGoods("九阳豆浆机", "家用豆浆机，营养健康", category.getId(), new BigDecimal("299.00"), 20, 2, now));
                    goodsList.add(createGoods("苏泊尔电水壶", "快速烧水，安全耐用", category.getId(), new BigDecimal("89.00"), 50, 5, now));
                    goodsList.add(createGoods("飞利浦剃须刀", "电动剃须刀，舒适剃须", category.getId(), new BigDecimal("399.00"), 15, 2, now));
                    goodsList.add(createGoods("美的电风扇", "落地扇，静音节能", category.getId(), new BigDecimal("159.00"), 40, 4, now));
                    goodsList.add(createGoods("海尔微波炉", "家用微波炉，加热快速", category.getId(), new BigDecimal("499.00"), 20, 2, now));
                    goodsList.add(createGoods("美的吸尘器", "家用吸尘器，强力吸尘", category.getId(), new BigDecimal("699.00"), 10, 1, now));
                    goodsList.add(createGoods("格力电暖气", "电暖器，快速升温", category.getId(), new BigDecimal("259.00"), 30, 3, now));
                    break;
                    
                case "个人护理":
                    // 添加个人护理类商品
                    goodsList.add(createGoods("海飞丝洗发水", "去屑止痒，清爽控油", category.getId(), new BigDecimal("39.90"), 80, 8, now));
                    goodsList.add(createGoods("潘婷护发素", "修护受损发质，柔顺光泽", category.getId(), new BigDecimal("35.90"), 70, 7, now));
                    goodsList.add(createGoods("高露洁牙膏", "清新口气，洁白牙齿", category.getId(), new BigDecimal("12.50"), 100, 10, now));
                    goodsList.add(createGoods("欧莱雅面霜", "保湿滋润，改善肤质", category.getId(), new BigDecimal("89.00"), 40, 4, now));
                    goodsList.add(createGoods("妮维雅沐浴露", "清爽沐浴，持久留香", category.getId(), new BigDecimal("29.90"), 80, 8, now));
                    goodsList.add(createGoods("舒肤佳香皂", "抑菌除菌，温和洁净", category.getId(), new BigDecimal("15.50"), 100, 10, now));
                    goodsList.add(createGoods("曼秀雷敦润唇膏", "滋润保湿，防止干裂", category.getId(), new BigDecimal("19.90"), 90, 9, now));
                    goodsList.add(createGoods("强生婴儿润肤露", "温和配方，呵护宝宝肌肤", category.getId(), new BigDecimal("32.50"), 50, 5, now));
                    break;
            }
        }
        
        // 保存商品
        List<Goods> savedGoods = goodsRepository.saveAll(goodsList);
        logger.info("初始化了 {} 个商品", savedGoods.size());
    }
    
    /**
     * 创建商品对象
     */
    private Goods createGoods(String name, String description, Long categoryId, BigDecimal price, int stock, int lowStockThreshold, LocalDateTime now) {
        Goods goods = new Goods();
        goods.setName(name);
        goods.setDescription(description);
        goods.setCategoryId(categoryId);
        goods.setPrice(price);
        goods.setStock(stock);
        goods.setLowStockThreshold(lowStockThreshold);
        goods.setStatus(1); // 1-上架
        goods.setCreateTime(now);
        goods.setUpdateTime(now);
        goods.setIsDeleted(false);
        return goods;
    }
}