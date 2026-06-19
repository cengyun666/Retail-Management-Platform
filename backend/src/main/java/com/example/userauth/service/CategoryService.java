package com.example.userauth.service;

import com.example.userauth.entity.Category;
import com.example.userauth.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 商品分类服务类
 */
@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * 添加商品分类
     * @param category 分类信息
     * @return 添加后的分类
     */
    public Category addCategory(Category category) {
        // 检查分类名称是否已存在（考虑父分类）
        if (existsByNameWithParent(category.getName(), category.getParent() != null ? category.getParent().getId() : null)) {
            throw new RuntimeException("分类名称已存在");
        }
        
        category.setIsDeleted(false);
        return categoryRepository.save(category);
    }
    
    /**
     * 更新商品分类
     * @param id 分类ID
     * @param category 分类信息
     * @return 更新后的分类
     */
    public Category updateCategory(Long id, Category category) {
        Optional<Category> existingCategory = categoryRepository.findByIdAndIsDeletedFalse(id);
        if (!existingCategory.isPresent()) {
            throw new RuntimeException("分类不存在");
        }
        
        Category categoryToUpdate = existingCategory.get();
        Long parentId = category.getParent() != null ? category.getParent().getId() : null;
        
        // 检查分类名称是否已存在（排除当前分类，考虑父分类）
        if (existsByNameAndNotId(category.getName(), parentId, id)) {
            throw new RuntimeException("分类名称已存在");
        }
        
        categoryToUpdate.setName(category.getName());
        categoryToUpdate.setDescription(category.getDescription());
        categoryToUpdate.setSortOrder(category.getSortOrder());
        categoryToUpdate.setStatus(category.getStatus());
        categoryToUpdate.setParent(category.getParent());
        
        return categoryRepository.save(categoryToUpdate);
    }
    
    /**
     * 删除商品分类（逻辑删除）
     * @param id 分类ID
     */
    public void deleteCategory(Long id) {
        Optional<Category> category = categoryRepository.findByIdAndIsDeletedFalse(id);
        if (!category.isPresent()) {
            throw new RuntimeException("分类不存在");
        }
        
        // 检查是否有子分类
        if (hasChildren(id)) {
            throw new RuntimeException("该分类下有子分类，无法删除");
        }
        
        Category categoryToDelete = category.get();
        categoryToDelete.setIsDeleted(true);
        categoryRepository.save(categoryToDelete);
    }
    
    /**
     * 根据ID获取商品分类
     * @param id 分类ID
     * @return 分类信息
     */
    public Category getCategoryById(Long id) {
        Optional<Category> category = categoryRepository.findByIdAndIsDeletedFalse(id);
        if (!category.isPresent()) {
            throw new RuntimeException("分类不存在");
        }
        return category.get();
    }
    
    /**
     * 分页获取商品分类
     * @param pageable 分页参数
     * @param name 分类名称（可选）
     * @param status 状态（可选）
     * @return 分类分页数据
     */
    public Page<Category> getCategories(Pageable pageable, String name, Integer status) {
        if (name != null && !name.isEmpty() && status != null) {
            return categoryRepository.findByNameContainingIgnoreCaseAndStatusAndIsDeletedFalse(name, status, pageable);
        } else if (name != null && !name.isEmpty()) {
            return categoryRepository.findByNameContainingIgnoreCaseAndIsDeletedFalse(name, pageable);
        } else if (status != null) {
            return categoryRepository.findByStatusAndIsDeletedFalse(status, pageable);
        } else {
            return categoryRepository.findByIsDeletedFalseOrderBySortOrderAsc(pageable);
        }
    }
    
    /**
     * 获取所有商品分类
     * @return 分类列表
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findByIsDeletedFalseOrderBySortOrderAsc();
    }
    
    /**
     * 根据状态获取商品分类
     * @param status 状态
     * @return 分类列表
     */
    public List<Category> getCategoriesByStatus(Integer status) {
        return categoryRepository.findByStatusAndIsDeletedFalse(status);
    }
    
    /**
     * 根据名称获取商品分类
     * @param name 分类名称
     * @return 分类信息
     */
    public Category getCategoryByName(String name) {
        Optional<Category> category = categoryRepository.findByNameAndIsDeletedFalse(name);
        if (!category.isPresent()) {
            throw new RuntimeException("分类不存在");
        }
        return category.get();
    }
    
    /**
     * 检查分类名称是否存在
     * @param name 分类名称
     * @param id 排除的分类ID（用于更新时检查）
     * @return 是否存在
     */
    public boolean existsByName(String name, Long id) {
        return categoryRepository.existsByNameAndIsDeletedFalse(name, id);
    }
    
    /**
     * 统计分类数量
     * @return 分类数量
     */
    public Long countCategories() {
        return categoryRepository.countByIsDeletedFalse();
    }
    
    /**
     * 获取所有顶级分类
     * @return 顶级分类列表
     */
    public List<Category> getAllTopLevelCategories() {
        return categoryRepository.findByParentIdIsNullAndIsDeletedFalseOrderBySortOrderAsc();
    }
    
    /**
     * 获取指定父分类的子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    public List<Category> getCategoriesByParentId(Long parentId) {
        return categoryRepository.findByParentIdAndIsDeletedFalseOrderBySortOrderAsc(parentId);
    }
    
    /**
     * 统计顶级分类数量
     * @return 顶级分类数量
     */
    public Long countTopLevelCategories() {
        return categoryRepository.countByParentIdIsNullAndIsDeletedFalse();
    }
    
    /**
     * 检查分类是否有子分类
     * @param id 分类ID
     * @return 是否有子分类
     */
    public Boolean hasChildren(Long id) {
        return categoryRepository.existsByParentIdAndIsDeletedFalse(id);
    }
    
    /**
     * 检查分类名称是否存在（考虑父分类，支持同一父分类下名称唯一）
     * @param name 分类名称
     * @param parentId 父分类ID
     * @return 是否存在
     */
    public Boolean existsByNameWithParent(String name, Long parentId) {
        return categoryRepository.existsByNameAndParentIdAndIsDeletedFalse(name, parentId);
    }
    
    /**
     * 检查分类名称是否存在（更新时使用，排除当前分类）
     * @param name 分类名称
     * @param parentId 父分类ID
     * @param id 当前分类ID
     * @return 是否存在
     */
    public Boolean existsByNameAndNotId(String name, Long parentId, Long id) {
        return categoryRepository.existsByNameAndParentIdAndIsDeletedFalseAndIdNot(name, parentId, id);
    }
    
    /**
     * 获取完整的分类树结构
     * @return 分类树
     */
    public List<Category> getCategoryTree() {
        List<Category> topLevelCategories = getAllTopLevelCategories();
        for (Category category : topLevelCategories) {
            loadChildren(category);
        }
        return topLevelCategories;
    }
    
    /**
     * 递归加载子分类
     * @param category 分类
     */
    private void loadChildren(Category category) {
        List<Category> children = getCategoriesByParentId(category.getId());
        category.setChildren(children);
        for (Category child : children) {
            loadChildren(child);
        }
    }
}