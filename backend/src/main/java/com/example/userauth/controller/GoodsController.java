package com.example.userauth.controller;

import com.example.userauth.entity.Goods;
import com.example.userauth.service.GoodsService;
import com.example.userauth.dto.ApiResponse;
import com.example.userauth.service.UserService;
import com.example.userauth.dto.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {
    
    @Autowired
    private GoodsService goodsService;
    
    @Autowired
    private UserService userService;
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;
    
    /**
     * 图片上传接口
     * @param file 上传的图片文件
     * @return 图片URL
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 确保上传目录存在
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
            File destination = new File(uploadDirectory, fileName);

            // 保存文件
            file.transferTo(destination);

            // 返回图片访问URL
            String imageUrl = "/uploads/" + fileName;
            return ResponseEntity.ok(ApiResponse.success(imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("图片上传失败: " + e.getMessage()));
        }
    }

    /**
     * 添加商品
     * @param name 商品名称
     * @param price 商品价格
     * @param stock 商品库存
     * @param description 商品描述
     * @param categoryId 商品分类ID
     * @param status 商品状态
     * @param image 商品图片
     * @param token 认证token
     * @return 添加结果
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Goods>> addGoods(
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam Integer stock,
            @RequestParam(required = false) String description,
            @RequestParam Long categoryId,
            @RequestParam(required = false, defaultValue = "0") Integer status,
            @RequestParam(required = false) MultipartFile image,
            @RequestHeader(value = "Authorization", required = true) String token) {
        try {
            // 解析token获取当前用户信息
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            UserInfo currentUser = userService.getUserInfoByToken(token);
            
            // 创建商品对象
            Goods goods = new Goods();
            goods.setName(name);
            goods.setPrice(price);
            goods.setStock(stock);
            goods.setDescription(description);
            goods.setCategoryId(categoryId);
            goods.setStatus(status);
            goods.setMerchantId(currentUser.getId()); // 设置商家ID为当前用户ID
            
            // 处理图片上传
            if (image != null && !image.isEmpty()) {
                // 确保上传目录存在
                File uploadDirectory = new File(uploadDir);
                if (!uploadDirectory.exists()) {
                    uploadDirectory.mkdirs();
                }

                // 生成唯一文件名
                String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(image.getOriginalFilename());
                File destination = new File(uploadDirectory, fileName);

                // 保存文件
                image.transferTo(destination);

                // 设置图片URL
                String imageUrl = "/uploads/" + fileName;
                goods.setImage(imageUrl);
            }
            
            Goods savedGoods = goodsService.addGoods(goods);
            return ResponseEntity.ok(ApiResponse.success(savedGoods));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 更新商品
     * @param id 商品ID
     * @param name 商品名称
     * @param price 商品价格
     * @param stock 商品库存
     * @param description 商品描述
     * @param categoryId 商品分类ID
     * @param status 商品状态
     * @param image 商品图片
     * @param token 认证token
     * @return 更新结果
     */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Goods>> updateGoods(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam BigDecimal price,
            @RequestParam Integer stock,
            @RequestParam(required = false) String description,
            @RequestParam Long categoryId,
            @RequestParam(required = false, defaultValue = "0") Integer status,
            @RequestParam(required = false) MultipartFile image,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 获取现有商品信息
            Goods existingGoods = goodsService.getGoodsById(id);
            if (existingGoods == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("商品不存在"));
            }
            
            // 更新商品基本信息（所有角色都可以修改的字段）
            existingGoods.setName(name);
            existingGoods.setCategoryId(categoryId);
            existingGoods.setPrice(price);
            existingGoods.setStock(stock);
            existingGoods.setDescription(description);
            existingGoods.setStatus(status);
            
            // 处理图片 - 只有商家和管理员可以编辑图片
            if (image != null && !image.isEmpty()) {
                // 验证用户权限
                UserInfo currentUser = null;
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    currentUser = userService.getUserInfoByToken(token);
                }
                
                // 检查用户角色
                String userRole = currentUser != null ? currentUser.getRole().toLowerCase() : "";
                if (!"admin".equals(userRole) && !"merchant".equals(userRole)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("普通用户没有权限编辑商品图片"));
                }
                
                // 确保上传目录存在
                File uploadDirectory = new File(uploadDir);
                if (!uploadDirectory.exists()) {
                    uploadDirectory.mkdirs();
                }

                // 生成唯一文件名
                String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(image.getOriginalFilename());
                File destination = new File(uploadDirectory, fileName);

                // 保存文件
                image.transferTo(destination);

                // 设置图片URL
                String imageUrl = "/uploads/" + fileName;
                existingGoods.setImage(imageUrl);
            }
            
            Goods updatedGoods = goodsService.updateGoods(id, existingGoods);
            return ResponseEntity.ok(ApiResponse.success(updatedGoods));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("图片处理失败: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 删除商品
     * @param id 商品ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteGoods(@PathVariable Long id) {
        try {
            goodsService.deleteGoods(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取商品
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Goods>> getGoodsById(@PathVariable Long id) {
        try {
            Goods goods = goodsService.getGoodsById(id);
            return ResponseEntity.ok(ApiResponse.success(goods));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取所有商品（分页）
     * @param page 页码
     * @param size 每页数量
     * @return 商品分页结果
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Goods>>> getAllGoods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status
    ) {
        try {
            // 如果有任何筛选条件，使用筛选方法
            if ((name != null && !name.trim().isEmpty()) || 
                (category != null && !category.trim().isEmpty()) || 
                (status != null && !status.trim().isEmpty())) {
                
                // 转换字符串参数为适当的类型
                Long categoryId = null;
                Integer statusValue = null;
                
                try {
                    if (category != null && !category.trim().isEmpty()) {
                        categoryId = Long.parseLong(category);
                    }
                } catch (NumberFormatException e) {
                    // 忽略无效的分类ID
                }
                
                try {
                    if (status != null && !status.trim().isEmpty()) {
                        statusValue = Integer.parseInt(status);
                    }
                } catch (NumberFormatException e) {
                    // 忽略无效的状态值
                }
                
                Page<Goods> goods = goodsService.findWithFilters(name, categoryId, statusValue, PageRequest.of(page, pageSize));
                return ResponseEntity.ok(ApiResponse.success(goods));
            }
            
            // 否则返回所有商品
            Page<Goods> goods = goodsService.getAllGoods(PageRequest.of(page, pageSize));
            return ResponseEntity.ok(ApiResponse.success(goods));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 根据分类ID获取商品（分页）
     * @param categoryId 分类ID
     * @param page 页码
     * @param size 每页大小
     * @param sort 排序字段
     * @param order 排序方向
     * @return 商品列表
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<Goods>>> getGoodsByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String order) {
        try {
            Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
            Page<Goods> goodsPage = goodsService.getGoodsByCategoryId(categoryId, pageable);
            return ResponseEntity.ok(ApiResponse.success(goodsPage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 根据商品名称搜索商品（分页）
     * @param name 商品名称
     * @param page 页码
     * @param size 每页大小
     * @param sort 排序字段
     * @param order 排序方向
     * @return 商品列表
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<Goods>>> searchGoodsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String order) {
        try {
            Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
            Page<Goods> goodsPage = goodsService.searchGoodsByName(name, pageable);
            return ResponseEntity.ok(ApiResponse.success(goodsPage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 根据分类ID和商品名称搜索商品（分页）
     * @param categoryId 分类ID
     * @param name 商品名称
     * @param page 页码
     * @param size 每页大小
     * @param sort 排序字段
     * @param order 排序方向
     * @return 商品列表
     */
    @GetMapping("/category/{categoryId}/search")
    public ResponseEntity<ApiResponse<Page<Goods>>> searchGoodsByCategoryAndName(
            @PathVariable Long categoryId,
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String order) {
        try {
            Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
            Page<Goods> goodsPage = goodsService.searchGoodsByCategoryAndName(categoryId, name, pageable);
            return ResponseEntity.ok(ApiResponse.success(goodsPage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 根据状态获取商品（分页）
     * @param status 状态
     * @param page 页码
     * @param size 每页大小
     * @param sort 排序字段
     * @param order 排序方向
     * @return 商品列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<Goods>>> getGoodsByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String order) {
        try {
            Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
            Page<Goods> goodsPage = goodsService.getGoodsByStatus(status, pageable);
            return ResponseEntity.ok(ApiResponse.success(goodsPage));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 更新商品库存
     * @param id 商品ID
     * @param stock 新库存
     * @return 更新结果
     */
    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<String>> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        try {
            goodsService.updateStock(id, stock);
            return ResponseEntity.ok(ApiResponse.success("库存更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 增加商品库存
     * @param id 商品ID
     * @param quantity 增加数量
     * @return 更新结果
     */
    @PutMapping("/{id}/stock/increase")
    public ResponseEntity<ApiResponse<String>> increaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            goodsService.increaseStock(id, quantity);
            return ResponseEntity.ok(ApiResponse.success("库存增加成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 减少商品库存
     * @param id 商品ID
     * @param quantity 减少数量
     * @return 更新结果
     */
    @PutMapping("/{id}/stock/decrease")
    public ResponseEntity<ApiResponse<String>> decreaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            goodsService.decreaseStock(id, quantity);
            return ResponseEntity.ok(ApiResponse.success("库存减少成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 更新商品状态
     * @param id 商品ID
     * @param status 新状态（0：上架，1：下架）
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateGoodsStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            goodsService.updateGoodsStatus(id, status);
            String statusText = status == 0 ? "上架" : "下架";
            return ResponseEntity.ok(ApiResponse.success("商品" + statusText + "成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}