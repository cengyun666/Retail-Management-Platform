package com.example.userauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.userauth.repository.CategoryRepository;
import com.example.userauth.repository.GoodsRepository;
import com.example.userauth.entity.Category;
import com.example.userauth.entity.Goods;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private GoodsRepository goodsRepository;

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        try {
            Iterable<Category> categories = categoryRepository.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/goods")
    public ResponseEntity<?> getGoods() {
        try {
            Page<Goods> goodsPage = goodsRepository.findByIsDeletedFalse(PageRequest.of(0, 100));
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", goodsPage.getContent());
            response.put("total", goodsPage.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}