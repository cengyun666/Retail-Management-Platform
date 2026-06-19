package com.example.userauth.controller;

import com.example.userauth.entity.Goods;
import com.example.userauth.service.GoodsService;
import com.example.userauth.dto.ApiResponse;
import com.example.userauth.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GoodsController.class)
@Import({SecurityConfig.class})
public class GoodsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoodsService goodsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Goods testGoods;

    @BeforeEach
    void setUp() {
        testGoods = new Goods();
        testGoods.setId(1L);
        testGoods.setName("测试商品");
        testGoods.setDescription("测试商品描述");
        testGoods.setPrice(new BigDecimal("99.99"));
        testGoods.setStock(100);
        testGoods.setCategoryId(1L);
        testGoods.setStatus(1);
    }

    @Test
    void testGetAllGoods() throws Exception {
        List<Goods> goodsList = Arrays.asList(testGoods);
        Page<Goods> goodsPage = new PageImpl<>(goodsList, PageRequest.of(0, 10), 1);
        
        when(goodsService.getAllGoods(any(Pageable.class))).thenReturn(goodsPage);

        mockMvc.perform(get("/api/goods"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("测试商品"));
    }

    @Test
    void testGetGoodsById() throws Exception {
        when(goodsService.getGoodsById(1L)).thenReturn(testGoods);

        mockMvc.perform(get("/api/goods/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试商品"));
    }

    @Test
    void testAddGoods() throws Exception {
        when(goodsService.addGoods(any(Goods.class))).thenReturn(testGoods);

        mockMvc.perform(post("/api/goods")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGoods)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试商品"));
    }

    @Test
    void testUpdateGoods() throws Exception {
        when(goodsService.updateGoods(eq(1L), any(Goods.class))).thenReturn(testGoods);

        mockMvc.perform(put("/api/goods/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGoods)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试商品"));
    }

    @Test
    void testDeleteGoods() throws Exception {
        mockMvc.perform(delete("/api/goods/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data").value("删除成功"));
    }

    @Test
    void testGetGoodsByCategoryId() throws Exception {
        List<Goods> goodsList = Arrays.asList(testGoods);
        Page<Goods> goodsPage = new PageImpl<>(goodsList, PageRequest.of(0, 10), 1);
        
        when(goodsService.getGoodsByCategoryId(eq(1L), any(Pageable.class))).thenReturn(goodsPage);

        mockMvc.perform(get("/api/goods/category/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("测试商品"));
    }

    @Test
    void testSearchGoodsByName() throws Exception {
        List<Goods> goodsList = Arrays.asList(testGoods);
        Page<Goods> goodsPage = new PageImpl<>(goodsList, PageRequest.of(0, 10), 1);
        
        when(goodsService.searchGoodsByName(eq("测试商品"), any(Pageable.class))).thenReturn(goodsPage);

        mockMvc.perform(get("/api/goods/search?name=测试商品"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("测试商品"));
    }

    @Test
    void testGetGoodsByStatus() throws Exception {
        List<Goods> goodsList = Arrays.asList(testGoods);
        Page<Goods> goodsPage = new PageImpl<>(goodsList, PageRequest.of(0, 10), 1);
        
        when(goodsService.getGoodsByStatus(eq(1), any(Pageable.class))).thenReturn(goodsPage);

        mockMvc.perform(get("/api/goods/status/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("测试商品"));
    }

    @Test
    void testUpdateStock() throws Exception {
        mockMvc.perform(put("/api/goods/1/stock")
                .param("stock", "50"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data").value("库存更新成功"));
    }

    @Test
    void testIncreaseStock() throws Exception {
        mockMvc.perform(put("/api/goods/1/stock/increase")
                .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data").value("库存增加成功"));
    }

    @Test
    void testDecreaseStock() throws Exception {
        mockMvc.perform(put("/api/goods/1/stock/decrease")
                .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data").value("库存减少成功"));
    }
}