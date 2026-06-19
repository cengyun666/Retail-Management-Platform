package com.example.userauth.controller;

import com.example.userauth.entity.Category;
import com.example.userauth.service.CategoryService;
import com.example.userauth.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 商品分类控制器
 */
@RestController
@RequestMapping("/categories")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * 添加商品分类
     * @param category 分类信息
     * @return 添加结果
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> addCategory(@Valid @RequestBody Category category) {
        try {
            Category savedCategory = categoryService.addCategory(category);
            return ResponseEntity.ok(ApiResponse.success(savedCategory));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 更新商品分类
     * @param id 分类ID
     * @param category 分类信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable Long id, @Valid @RequestBody Category category) {
        try {
            Category updatedCategory = categoryService.updateCategory(id, category);
            return ResponseEntity.ok(ApiResponse.success(updatedCategory));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 删除商品分类
     * @param id 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取所有商品分类
     * @param page 页码
     * @param size 每页大小
     * @param name 分类名称（可选）
     * @param status 状态（可选）
     * @return 分类列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Category>>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Category> categories = categoryService.getCategories(pageable, name, status);
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取所有分类列表（不分页）
     * @return 分类列表
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategoriesList() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取所有顶级分类
     * @return 顶级分类列表
     */
    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<Category>>> getTopLevelCategories() {
        try {
            List<Category> topLevelCategories = categoryService.getAllTopLevelCategories();
            return ResponseEntity.ok(ApiResponse.success(topLevelCategories));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 根据父分类ID获取子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @GetMapping("/children/{parentId}")
    public ResponseEntity<ApiResponse<List<Category>>> getChildrenCategories(@PathVariable Long parentId) {
        try {
            List<Category> childrenCategories = categoryService.getCategoriesByParentId(parentId);
            return ResponseEntity.ok(ApiResponse.success(childrenCategories));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取完整的分类树结构
     * @return 分类树
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<Category>>> getCategoryTree() {
        try {
            List<Category> categoryTree = categoryService.getCategoryTree();
            return ResponseEntity.ok(ApiResponse.success(categoryTree));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 检查分类是否有子分类
     * @param id 分类ID
     * @return 是否有子分类
     */
    @GetMapping("/has-children/{id}")
    public ResponseEntity<ApiResponse<Boolean>> hasChildren(@PathVariable Long id) {
        try {
            Boolean hasChildren = categoryService.hasChildren(id);
            return ResponseEntity.ok(ApiResponse.success(hasChildren));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取商品分类
     * @param id 分类ID
     * @return 分类信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long id) {
        try {
            Category category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(ApiResponse.success(category));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 根据状态获取商品分类
     * @param status 状态
     * @return 分类列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Category>>> getCategoriesByStatus(@PathVariable Integer status) {
        try {
            List<Category> categories = categoryService.getCategoriesByStatus(status);
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 根据名称获取商品分类
     * @param name 分类名称
     * @return 分类信息
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<Category>> getCategoryByName(@PathVariable String name) {
        try {
            Category category = categoryService.getCategoryByName(name);
            return ResponseEntity.ok(ApiResponse.success(category));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 检查分类名称是否存在
     * @param name 分类名称
     * @param id 排除的分类ID（可选，用于更新时检查）
     * @return 检查结果
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> existsByName(
            @RequestParam String name, 
            @RequestParam(required = false) Long id) {
        try {
            boolean exists = categoryService.existsByName(name, id);
            return ResponseEntity.ok(ApiResponse.success(exists));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 统计分类数量
     * @return 分类数量
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countCategories() {
        try {
            Long count = categoryService.countCategories();
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
}