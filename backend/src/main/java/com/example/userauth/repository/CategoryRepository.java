package com.example.userauth.repository;

import com.example.userauth.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 商品分类仓库接口
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * 根据名称查找分类
     * @param name 分类名称
     * @return 分类
     */
    Optional<Category> findByNameAndIsDeletedFalse(String name);
    
    /**
     * 查找所有未删除的分类
     * @return 分类列表
     */
    List<Category> findByIsDeletedFalse();
    
    /**
     * 根据状态查找分类
     * @param status 状态
     * @return 分类列表
     */
    List<Category> findByStatusAndIsDeletedFalse(Integer status);
    
    /**
     * 根据排序顺序查找分类
     * @return 分类列表
     */
    List<Category> findByIsDeletedFalseOrderBySortOrderAsc();
    
    /**
     * 查找顶级分类（父分类ID为空）
     * @return 顶级分类列表
     */
    List<Category> findByParentIdIsNullAndIsDeletedFalseOrderBySortOrderAsc();
    
    /**
     * 根据父分类ID查找子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> findByParentIdAndIsDeletedFalseOrderBySortOrderAsc(Long parentId);
    
    /**
     * 统计顶级分类数量
     * @return 顶级分类数量
     */
    Long countByParentIdIsNullAndIsDeletedFalse();
    
    /**
     * 检查分类是否有子分类
     * @param parentId 父分类ID
     * @return 是否有子分类
     */
    boolean existsByParentIdAndIsDeletedFalse(Long parentId);
    
    /**
     * 检查分类名称是否存在（考虑父分类）
     * @param name 分类名称
     * @param parentId 父分类ID
     * @return 是否存在
     */
    boolean existsByNameAndParentIdAndIsDeletedFalse(String name, Long parentId);
    
    /**
     * 检查分类名称是否存在（更新时使用，排除当前分类）
     * @param name 分类名称
     * @param parentId 父分类ID
     * @param id 排除的分类ID
     * @return 是否存在
     */
    boolean existsByNameAndParentIdAndIsDeletedFalseAndIdNot(String name, Long parentId, Long id);
    
    /**
     * 根据排序顺序查找分类（分页）
     * @param pageable 分页参数
     * @return 分类分页数据
     */
    Page<Category> findByIsDeletedFalseOrderBySortOrderAsc(Pageable pageable);
    
    /**
     * 根据状态查找分类（分页）
     * @param status 状态
     * @param pageable 分页参数
     * @return 分类分页数据
     */
    Page<Category> findByStatusAndIsDeletedFalse(Integer status, Pageable pageable);
    
    /**
     * 根据名称模糊查询分类（分页）
     * @param name 分类名称
     * @param pageable 分页参数
     * @return 分类分页数据
     */
    Page<Category> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);
    
    /**
     * 根据名称查找分类（考虑父分类，支持同一父分类下名称唯一）
     * @param name 分类名称
     * @param parentId 父分类ID
     * @return 分类
     */
    Optional<Category> findByNameAndParentIdAndIsDeletedFalse(String name, Long parentId);
    
    /**
     * 根据名称和状态模糊查询分类（分页）
     * @param name 分类名称
     * @param status 状态
     * @param pageable 分页参数
     * @return 分类分页数据
     */
    Page<Category> findByNameContainingIgnoreCaseAndStatusAndIsDeletedFalse(String name, Integer status, Pageable pageable);
    
    /**
     * 根据ID查找未删除的分类
     * @param id 分类ID
     * @return 分类
     */
    Optional<Category> findByIdAndIsDeletedFalse(Long id);
    
    /**
     * 检查分类名称是否存在
     * @param name 分类名称
     * @param id 排除的分类ID（用于更新时检查）
     * @return 是否存在
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.name = :name AND c.isDeleted = false AND (:id IS NULL OR c.id != :id)")
    boolean existsByNameAndIsDeletedFalse(@Param("name") String name, @Param("id") Long id);
    
    /**
     * 统计未删除的分类数量
     * @return 分类数量
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.isDeleted = false")
    Long countByIsDeletedFalse();
}