package com.example.userauth.service;

import com.example.userauth.entity.Goods;
import com.example.userauth.entity.Category;
import com.example.userauth.repository.GoodsRepository;
import com.example.userauth.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 商品服务类
 */
@Service
@Transactional
public class GoodsService {
    
    @Autowired
    private GoodsRepository goodsRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * 添加商品
     * @param goods 商品信息
     * @return 添加后的商品
     */
    public Goods addGoods(Goods goods) {
        // 验证分类是否存在
        Optional<Category> category = categoryRepository.findByIdAndIsDeletedFalse(goods.getCategoryId());
        if (!category.isPresent()) {
            throw new RuntimeException("商品分类不存在");
        }
        
        // 检查商品名称是否已存在
        Goods existingGoods = goodsRepository.findByNameAndIsDeletedFalse(goods.getName());
        if (existingGoods != null) {
            throw new RuntimeException("商品名称已存在");
        }
        
        goods.setIsDeleted(false);
        return goodsRepository.save(goods);
    }
    
    /**
     * 更新商品
     * @param id 商品ID
     * @param goods 商品信息
     * @return 更新后的商品
     */
    public Goods updateGoods(Long id, Goods goods) {
        Optional<Goods> existingGoods = goodsRepository.findByIdAndIsDeletedFalse(id);
        if (!existingGoods.isPresent()) {
            throw new RuntimeException("商品不存在");
        }
        
        // 验证分类是否存在
        Optional<Category> category = categoryRepository.findByIdAndIsDeletedFalse(goods.getCategoryId());
        if (!category.isPresent()) {
            throw new RuntimeException("商品分类不存在");
        }
        
        // 检查商品名称是否已存在（排除当前商品）
        Goods goodsWithSameName = goodsRepository.findByNameAndIsDeletedFalse(goods.getName());
        if (goodsWithSameName != null && !goodsWithSameName.getId().equals(id)) {
            throw new RuntimeException("商品名称已存在");
        }
        
        Goods goodsToUpdate = existingGoods.get();
        goodsToUpdate.setName(goods.getName());
        goodsToUpdate.setCategoryId(goods.getCategoryId());
        goodsToUpdate.setPrice(goods.getPrice());
        // 保存原始库存值用于比较
        int oldStock = goodsToUpdate.getStock();
        goodsToUpdate.setStock(goods.getStock());
        goodsToUpdate.setLowStockThreshold(goods.getLowStockThreshold());
        goodsToUpdate.setDescription(goods.getDescription());
        goodsToUpdate.setImage(goods.getImage());
        
        // 库存状态逻辑：
        // 1. 如果新库存为0，自动下架
        // 2. 如果新库存从0增加到正数，自动上架
        // 3. 其他情况保持原有状态
        if (goods.getStock() == 0) {
            goodsToUpdate.setStatus(1); // 1表示下架状态
        } else if (oldStock == 0 && goods.getStock() > 0) {
            goodsToUpdate.setStatus(0); // 0表示上架状态
        }
        
        return goodsRepository.save(goodsToUpdate);
    }
    
    /**
     * 删除商品（逻辑删除）
     * @param id 商品ID
     */
    public void deleteGoods(Long id) {
        Optional<Goods> goods = goodsRepository.findByIdAndIsDeletedFalse(id);
        if (!goods.isPresent()) {
            throw new RuntimeException("商品不存在");
        }
        
        Goods goodsToDelete = goods.get();
        goodsToDelete.setIsDeleted(true);
        goodsRepository.save(goodsToDelete);
    }
    
    /**
     * 根据ID获取商品
     * @param id 商品ID
     * @return 商品信息
     */
    public Goods getGoodsById(Long id) {
        Optional<Goods> goods = goodsRepository.findByIdAndIsDeletedFalse(id);
        if (!goods.isPresent()) {
            throw new RuntimeException("商品不存在");
        }
        return goods.get();
    }
    
    /**
     * 获取所有商品（分页）
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    public Page<Goods> getAllGoods(Pageable pageable) {
        return goodsRepository.findByIsDeletedFalse(pageable);
    }
    
    /**
     * 根据分类ID获取商品（分页）
     * @param categoryId 分类ID
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    public Page<Goods> getGoodsByCategoryId(Long categoryId, Pageable pageable) {
        return goodsRepository.findByCategoryIdAndIsDeletedFalse(categoryId, pageable);
    }
    
    /**
     * 根据商品名称搜索商品（分页）
     * @param name 商品名称
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    public Page<Goods> searchGoodsByName(String name, Pageable pageable) {
        return goodsRepository.findByNameContainingAndIsDeletedFalse(name, pageable);
    }
    
    /**
     * 根据状态获取商品（分页）
     * @param status 状态
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    public Page<Goods> getGoodsByStatus(Integer status, Pageable pageable) {
        return goodsRepository.findByStatusAndIsDeletedFalse(status, pageable);
    }
    
    /**
     * 根据分类ID和商品名称搜索商品（分页）
     * @param categoryId 分类ID
     * @param name 商品名称
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    public Page<Goods> searchGoodsByCategoryAndName(Long categoryId, String name, Pageable pageable) {
        return goodsRepository.findByCategoryIdAndNameContainingAndIsDeletedFalse(categoryId, name, pageable);
    }
    
    /**
     * 更新商品库存
     * @param id 商品ID
     * @param stock 新库存
     */
    public void updateStock(Long id, Integer stock) {
        Optional<Goods> goods = goodsRepository.findByIdAndIsDeletedFalse(id);
        if (!goods.isPresent()) {
            throw new RuntimeException("商品不存在");
        }
        
        Goods goodsToUpdate = goods.get();
        // 保存原始库存值用于比较
        int oldStock = goodsToUpdate.getStock();
        goodsToUpdate.setStock(stock);
        
        // 库存状态逻辑：
        // 1. 如果新库存为0，自动下架
        // 2. 如果新库存从0增加到正数，自动上架
        if (stock == 0) {
            goodsToUpdate.setStatus(1); // 1表示下架状态
        } else if (oldStock == 0 && stock > 0) {
            goodsToUpdate.setStatus(0); // 0表示上架状态
        }
        
        goodsRepository.save(goodsToUpdate);
    }
    
    /**
     * 增加商品库存（使用原子性操作）
     * @param id 商品ID
     * @param quantity 增加数量
     */
    @Transactional
    public void increaseStock(Long id, Integer quantity) {
        // 获取当前商品信息
        Optional<Goods> goodsOptional = goodsRepository.findByIdAndIsDeletedFalse(id);
        if (!goodsOptional.isPresent()) {
            throw new RuntimeException("商品不存在");
        }
        Goods goods = goodsOptional.get();
        
        // 保存原始库存值
        int oldStock = goods.getStock();
        
        // 增加库存
        int affectedRows = goodsRepository.increaseStock(id, quantity);
        if (affectedRows == 0) {
            throw new RuntimeException("商品不存在或增加库存失败");
        }
        
        // 重新获取商品信息以更新内存中的对象
        Optional<Goods> updatedGoodsOptional = goodsRepository.findByIdAndIsDeletedFalse(id);
        if (updatedGoodsOptional.isPresent()) {
            goods = updatedGoodsOptional.get();
        }
        
        // 如果库存从0增加到正数，自动上架商品
        if (oldStock == 0 && quantity > 0) {
            updateGoodsStatus(id, 0); // 0表示上架状态
        }
    }
    
    /**
     * 减少商品库存（使用原子性操作）
     * @param id 商品ID
     * @param quantity 减少数量
     */
    @Transactional
    public void decreaseStock(Long id, Integer quantity) {
        int affectedRows = goodsRepository.decreaseStock(id, quantity);
        if (affectedRows == 0) {
            throw new RuntimeException("库存不足或商品不存在");
        }
        
        // 检查库存是否为零，如果是则自动下架商品
        Optional<Goods> goods = goodsRepository.findByIdAndIsDeletedFalse(id);
        if (goods.isPresent() && goods.get().getStock() == 0) {
            updateGoodsStatus(id, 1); // 1表示下架状态
        }
    }
    
    /**
     * 多条件筛选商品
     * @param name 商品名称（可为空）
     * @param categoryId 分类ID（可为空）
     * @param status 状态（可为空）
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    public Page<Goods> findWithFilters(String name, Long categoryId, Integer status, Pageable pageable) {
        // 处理空字符串参数
        if (name != null && name.trim().isEmpty()) {
            name = null;
        }
        
        return goodsRepository.findWithFilters(name, categoryId, status, pageable);
    }
    
    /**
     * 更新商品状态
     * @param id 商品ID
     * @param status 新状态（0：上架，1：下架）
     */
    public void updateGoodsStatus(Long id, Integer status) {
        Optional<Goods> goods = goodsRepository.findByIdAndIsDeletedFalse(id);
        if (!goods.isPresent()) {
            throw new RuntimeException("商品不存在");
        }
        
        Goods goodsToUpdate = goods.get();
        goodsToUpdate.setStatus(status);
        goodsRepository.save(goodsToUpdate);
    }
}