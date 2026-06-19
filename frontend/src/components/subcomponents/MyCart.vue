<template>
  <div class="cart-container">
    <el-card v-loading="loading" class="cart-card">
      <template #header>
        <div class="card-header">
          <h2>购物车</h2>
          <el-button 
            type="danger" 
            plain 
            size="small" 
            @click="handleClearCart"
            :disabled="cartItems.length === 0"
          >
            清空购物车
          </el-button>
        </div>
      </template>

      <div v-if="cartItems.length === 0" class="empty-cart">
        <el-empty description="购物车是空的">
          <el-button type="primary" @click="goToGoods">去购物</el-button>
        </el-empty>
      </div>

      <div v-else class="cart-content">
        <!-- 购物车列表 -->
        <div class="cart-items">
          <div class="cart-header">
            <el-checkbox 
              v-model="selectAll" 
              @change="handleSelectAll"
              :indeterminate="isIndeterminate"
            >
              全选
            </el-checkbox>
            <span class="header-item">商品信息</span>
            <span class="header-item">单价</span>
            <span class="header-item">数量</span>
            <span class="header-item">小计</span>
            <span class="header-item">操作</span>
          </div>

          <div class="cart-list">
            <div 
              v-for="item in cartItems" 
              :key="item.id" 
              class="cart-item"
              :class="{ 'selected': item.selected }"
            >
              <el-checkbox 
                v-model="item.selected" 
                @change="updateItemSelection(item)"
              />
              
              <div class="item-info">
                <img 
  :src="getFullImageUrl(item.image)" 
  :alt="item.goodsName" 
  class="item-image" 
  @error="$event.target.src='https://picsum.photos/seed/error/80/80'"
/>
                <div class="item-details">
                  <div class="item-name">{{ item.goodsName }}</div>
                  <div class="item-stock" v-if="item.stock < item.quantity">
                    <el-alert
                      title="库存不足"
                      type="warning"
                      description="当前商品库存不足，最大可购买数量为 {{ item.stock }}"
                      :closable="false"
                      size="small"
                    />
                  </div>
                </div>
              </div>
              
              <div class="item-price">¥{{ item.price.toFixed(2) }}</div>
              
              <div class="item-quantity">
                <el-input-number 
                  v-model="item.quantity" 
                  :min="1" 
                  :max="item.stock || 99"
                  :key="`quantity_${item.id}_${item.stock}`" 
                  @change="updateItemQuantity(item)"
                  size="small"
                />
              </div>
              
              <div class="item-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
              
              <div class="item-actions">
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleRemoveItem(item)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 购物车总结 -->
        <div class="cart-summary">
          <div class="summary-info">
            <div class="summary-item">
              <span>已选择 {{ selectedCount }} 件商品</span>
            </div>
            <div class="summary-item total">
              <span>总计：</span>
              <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
          </div>
          
          <div class="summary-actions">
            <el-button @click="goToGoods">继续购物</el-button>
            <el-button 
              type="primary" 
              size="large"
              @click="handleCheckout"
              :disabled="selectedCount === 0"
            >
              结算 ({{ selectedCount }})
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCartItems, updateCartItem, removeFromCart, clearCart } from '../../api/mock-cart'
import { getGoodsById } from '../../api/goods'

// 图片URL处理方法
const getFullImageUrl = (url) => {
  if (!url) return 'https://picsum.photos/seed/default/80/80'; // 默认图片
  
  // 已经是完整URL的情况
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('blob:') || url.startsWith('data:image/')) {
    return url;
  }
  
  // 确保URL以/api开头，以便Vite代理能正确转发请求
  if (!url.startsWith('/api')) {
    if (url.startsWith('/')) {
      return `/api${url}`;
    } else {
      return `/api/${url}`;
    }
  }
  
  return url;
}

const router = useRouter()
const loading = ref(false)
const cartItems = ref([])

// 计算属性
const selectAll = computed({
  get() {
    return cartItems.value.length > 0 && cartItems.value.every(item => item.selected)
  },
  set(value) {
    cartItems.value.forEach(item => {
      item.selected = value
    })
  }
})

const isIndeterminate = computed(() => {
  const selectedItems = cartItems.value.filter(item => item.selected)
  return selectedItems.length > 0 && selectedItems.length < cartItems.value.length
})

const selectedCount = computed(() => {
  return cartItems.value.filter(item => item.selected).length
})

const totalPrice = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((total, item) => total + (item.price * item.quantity), 0)
})

// 方法
const fetchCartItems = async () => {
  loading.value = true
  try {
    const res = await getCartItems()
    cartItems.value = res.data.list || []
    
    // 获取每个商品的库存信息
    for (const item of cartItems.value) {
      try {
        const goodsRes = await getGoodsById(item.goodsId)
        item.stock = goodsRes.data.stock || 99 // 如果没有库存信息，默认设置为99
        console.log(`商品 ${item.goodsName} 的库存信息:`, goodsRes.data.stock)
      } catch (error) {
        console.error(`获取商品 ${item.id} 的库存信息失败:`, error)
        item.stock = 99 // 默认设置为99
      }
      
      // 确保数量不超过库存
      if (item.quantity > item.stock) {
        item.quantity = item.stock
        console.log(`商品 ${item.goodsName} 的数量已调整为库存上限:`, item.stock)
      }
    }
  } catch (error) {
    ElMessage.error('获取购物车失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const updateItemQuantity = async (item) => {
  try {
    // 确保数量不超过库存
    if (item.quantity > item.stock) {
      item.quantity = item.stock
      ElMessage.warning(`该商品库存不足，最大可购买数量为 ${item.stock}`)
    }
    
    await updateCartItem(item.id, { quantity: item.quantity })
    // 更新小计
    item.totalPrice = item.price * item.quantity
  } catch (error) {
    ElMessage.error('更新商品数量失败')
    console.error(error)
    // 恢复原数量
    fetchCartItems()
  }
}

// 监听数量变化，确保不超过库存
watch(() => cartItems.value, (newItems) => {
  newItems.forEach(item => {
    if (item.quantity > item.stock) {
      item.quantity = item.stock
    }
  })
}, { deep: true })

const updateItemSelection = async (item) => {
  try {
    await updateCartItem(item.id, { selected: item.selected })
  } catch (error) {
    ElMessage.error('更新选择状态失败')
    console.error(error)
    // 恢复原状态
    fetchCartItems()
  }
}

const handleSelectAll = (value) => {
  cartItems.value.forEach(item => {
    item.selected = value
  })
  
  // 批量更新选择状态
  const updatePromises = cartItems.value.map(item => 
    updateCartItem(item.id, { selected: value })
  )
  
  Promise.all(updatePromises).catch(error => {
    ElMessage.error('更新选择状态失败')
    console.error(error)
    fetchCartItems()
  })
}

const handleRemoveItem = (item) => {
  ElMessageBox.confirm(
    `确定要删除 ${item.goodsName} 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await removeFromCart(item.id)
        ElMessage.success('删除成功')
        fetchCartItems()
      } catch (error) {
        ElMessage.error('删除失败')
        console.error(error)
      }
    })
    .catch(() => {
      // 用户取消删除
    })
}

const handleClearCart = () => {
  ElMessageBox.confirm(
    '确定要清空购物车吗？',
    '清空确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await clearCart()
        ElMessage.success('清空购物车成功')
        fetchCartItems()
      } catch (error) {
        ElMessage.error('清空购物车失败')
        console.error(error)
      }
    })
    .catch(() => {
      // 用户取消清空
    })
}

const handleCheckout = () => {
  // 获取选中的商品
  const selectedItems = cartItems.value.filter(item => item.selected)
  
  if (selectedItems.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  
  // 将选中的商品存储到本地，用于结算页面
  localStorage.setItem('checkoutItems', JSON.stringify(selectedItems))
  
  // 跳转到结算页面
  router.push('/home/checkout')
}

const goToGoods = () => {
  router.push('/home/goods')
}

// 生命周期
onMounted(() => {
  fetchCartItems()
})
</script>

<style scoped>
.cart-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.cart-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #333;
}

.empty-cart {
  padding: 40px 0;
  text-align: center;
}

.cart-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cart-items {
  background-color: #fff;
  border-radius: 4px;
  overflow: hidden;
}

.cart-header {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
  color: #606266;
}

.cart-header .header-item {
  flex: 1;
  text-align: center;
}

.cart-header .header-item:first-of-type {
  flex: 3;
  text-align: left;
  padding-left: 20px;
}

.cart-list {
  max-height: 400px;
  overflow-y: auto;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  transition: background-color 0.3s;
}

.cart-item:hover {
  background-color: #f8f9fa;
}

.cart-item.selected {
  background-color: #f0f9ff;
}

.item-info {
  flex: 3;
  display: flex;
  align-items: center;
  gap: 15px;
  padding-left: 20px;
}

.item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.item-name {
  font-size: 14px;
  color: #303133;
}

.item-price, .item-quantity, .item-total, .item-actions {
  flex: 1;
  text-align: center;
}

.item-price, .item-total {
  font-weight: 600;
  color: #f56c6c;
}

.cart-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.summary-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-item {
  font-size: 14px;
  color: #606266;
}

.summary-item.total {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.total-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: 700;
}

.summary-actions {
  display: flex;
  gap: 15px;
}
</style>