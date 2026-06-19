package com.example.userauth.repository;

import com.example.userauth.entity.Goods;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商品仓库接口
 */
@Repository
public interface GoodsRepository extends JpaRepository<Goods, Long> {
    
    /**
     * 根据分类ID查找商品
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<Goods> findByCategoryIdAndIsDeletedFalse(Long categoryId);
    
    /**
     * 根据分类ID分页查找商品
     * @param categoryId 分类ID
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    Page<Goods> findByCategoryIdAndIsDeletedFalse(Long categoryId, Pageable pageable);
    
    /**
     * 根据商品名称模糊查询
     * @param name 商品名称
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    Page<Goods> findByNameContainingAndIsDeletedFalse(String name, Pageable pageable);
    
    /**
     * 根据状态查找商品
     * @param status 状态
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    Page<Goods> findByStatusAndIsDeletedFalse(Integer status, Pageable pageable);
    
    /**
     * 根据分类ID和状态查找商品
     * @param categoryId 分类ID
     * @param status 状态
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    Page<Goods> findByCategoryIdAndStatusAndIsDeletedFalse(Long categoryId, Integer status, Pageable pageable);
    
    /**
     * 根据分类ID和商品名称模糊查询
     * @param categoryId 分类ID
     * @param name 商品名称
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    Page<Goods> findByCategoryIdAndNameContainingAndIsDeletedFalse(Long categoryId, String name, Pageable pageable);
    
    /**
     * 查找所有未删除的商品
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    Page<Goods> findByIsDeletedFalse(Pageable pageable);
    
    /**
     * 根据分类ID查找商品数量
     * @param categoryId 分类ID
     * @return 商品数量
     */
    @Query("SELECT COUNT(g) FROM Goods g WHERE g.categoryId = :categoryId AND g.isDeleted = false")
    Long countByCategoryIdAndIsDeletedFalse(@Param("categoryId") Long categoryId);
    
    /**
     * 原子性扣减库存
     * @param id 商品ID
     * @param quantity 扣减数量
     * @return 更新的行数
     */
    @Modifying
    @Query("UPDATE Goods g SET g.stock = g.stock - :quantity WHERE g.id = :id AND g.stock >= :quantity AND g.isDeleted = false")
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    /**
     * 原子性增加库存
     * @param id 商品ID
     * @param quantity 增加数量
     * @return 更新的行数
     */
    @Modifying
    @Query("UPDATE Goods g SET g.stock = g.stock + :quantity WHERE g.id = :id AND g.isDeleted = false")
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    /**
     * 根据名称查找商品
     * @param name 商品名称
     * @return 商品
     */
    Goods findByNameAndIsDeletedFalse(String name);
    
    /**
     * 根据ID查找未删除的商品
     * @param id 商品ID
     * @return 商品
     */
    Optional<Goods> findByIdAndIsDeletedFalse(Long id);
    
    /**
     * 多条件筛选商品
     * @param name 商品名称（可为空）
     * @param categoryId 分类ID（可为空）
     * @param status 状态（可为空）
     * @param pageable 分页参数
     * @return 商品分页结果
     */
    @Query("SELECT g FROM Goods g WHERE " +
           "(:name IS NULL OR g.name LIKE %:name%) AND " +
           "(:categoryId IS NULL OR g.categoryId = :categoryId) AND " +
           "(:status IS NULL OR g.status = :status) AND " +
           "g.isDeleted = false")
    Page<Goods> findWithFilters(@Param("name") String name, 
                               @Param("categoryId") Long categoryId, 
                               @Param("status") Integer status, 
                               Pageable pageable);
}