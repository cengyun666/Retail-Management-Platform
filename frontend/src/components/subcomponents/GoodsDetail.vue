<template>
  <div class="goods-detail">
    <el-card v-loading="loading" class="detail-card">
      <template #header>
        <div class="card-header">
          <h2>{{ goods.name || '加载中...' }}</h2>
          <el-tag v-if="goods.status !== undefined" :type="goods.status === 0 ? 'success' : 'danger'">
            {{ goods.status === 0 ? '上架' : '下架' }}
          </el-tag>
        </div>
      </template>

      <div v-if="Object.keys(goods).length > 0" class="goods-info">
        <div class="goods-image">
          <img 
  :src="getFullImageUrl(goods.image)" 
  :alt="goods.name" 
  @error="$event.target.src='https://picsum.photos/seed/error/80/80'"
/>
        </div>
        <div class="goods-details">
          <div class="detail-item">
            <label>商品分类：</label>
            <span>{{ getCategoryName(goods.categoryId) }}</span>
          </div>
          <div class="detail-item">
            <label>商品价格：</label>
            <span class="price">¥{{ goods.price?.toFixed(2) }}</span>
          </div>
          <div class="detail-item">
            <label>库存数量：</label>
            <span>{{ goods.stock }}</span>
          </div>
          <div class="detail-item">
            <label>创建时间：</label>
            <span>{{ goods.createTime }}</span>
          </div>
          <div class="detail-item description">
            <label>商品描述：</label>
            <p>{{ goods.description }}</p>
          </div>
        </div>
      </div>

      <div v-if="Object.keys(goods).length > 0" class="order-section">
        <el-form :model="orderForm" class="order-form">
          <el-form-item label="购买数量" v-if="isUser(userInfo.role)">
            <el-input-number
              v-model="orderForm.quantity"
              :min="1"
              :max="goods.stock"
              :disabled="goods.status === 1 || goods.stock === 0"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              @click="handleOrder"
              v-if="isUser(userInfo.role)"
              :disabled="goods.status === 1 || goods.stock === 0"
            >
              立即下单
            </el-button>
            <el-button
              type="success"
              size="large"
              @click="handleAddToCart"
              v-if="isUser(userInfo.role)"
              :disabled="goods.status === 1 || goods.stock === 0"
            >
              加入购物车
            </el-button>

            <el-button
              size="large"
              @click="router.back()"
            >
              返回列表
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGoodsById, getCategories } from '../../api/goods'
import { createOrder } from '../../api/orders'
import { addToCart } from '../../api/cart'
import { getDefaultAddress } from '../../api/addresses'
import { hasPermission, isUser } from '../../utils/permissions'
import { PERMISSIONS } from '../../utils/roles'

const route = useRoute()
const router = useRouter()
const goodsId = ref(route.params.id)
const goods = ref({})
const categories = ref([])
const loading = ref(false)
const orderForm = ref({
  quantity: 1,
  address: ''
})

// 获取当前用户信息
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))



const fetchGoodsDetail = async () => {
  loading.value = true
  try {
    console.log('路由参数id:', goodsId.value, '类型:', typeof goodsId.value)
    const response = await getGoodsById(goodsId.value)
    console.log('API返回结果:', response)
    if (response.code === 200) {
      goods.value = response.data
      console.log('商品数据:', goods.value)
    } else {
      ElMessage.error(response.message)
      router.push('/home/goods')
    }
  } catch (error) {
    ElMessage.error('获取商品详情失败')
    console.error('错误信息:', error)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const response = await getCategories()
    if (response.code === 200) {
      categories.value = response.data
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const getCategoryName = (categoryId) => {
  const category = categories.value.find(cat => cat.id === categoryId)
  return category ? category.name : '未知分类'
}

const getFullImageUrl = (imagePath) => {
  if (!imagePath) return ''
  // 如果已经是完整的URL，则直接返回
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://') || imagePath.startsWith('blob:') || imagePath.startsWith('data:image/')) {
    return imagePath
  }
  // 否则，确保URL以/api开头
  if (!imagePath.startsWith('/api')) {
    if (imagePath.startsWith('/')) {
      return `/api${imagePath}`
    } else {
      return `/api/${imagePath}`
    }
  }
  return imagePath
}

const handleOrder = () => {
  // 将商品信息保存到localStorage，供结算页面使用
  const checkoutItem = {
    goodsId: goods.value.id,
    goodsName: goods.value.name,
    price: goods.value.price,
    quantity: orderForm.value.quantity,
    image: getFullImageUrl(goods.value.image)
  }
  
  // 保存到localStorage
  localStorage.setItem('checkoutItems', JSON.stringify([checkoutItem]))
  
  // 跳转到结算页面
  router.push('/home/checkout')
}

const handleAddToCart = async () => {
  try {
    // 获取用户默认地址
    let addressInfo = null
    try {
      const addressResponse = await getDefaultAddress()
      if (addressResponse.code === 200) {
        addressInfo = addressResponse.data
      }
    } catch (addressError) {
      console.log('未找到默认地址:', addressError)
    }

    const cartItem = {
      goodsId: goods.value.id,
      goodsName: goods.value.name,
      price: goods.value.price,
      quantity: orderForm.value.quantity,
      image: getFullImageUrl(goods.value.image),
      addressId: addressInfo?.id,
      addressDetails: addressInfo?.address || ''
    }

    const response = await addToCart(cartItem)
    if (response.code === 200) {
      ElMessage.success('商品已加入购物车')
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    ElMessage.error('加入购物车失败')
    console.error(error)
  }
}



onMounted(() => {
  fetchGoodsDetail()
  fetchCategories()
})
</script>

<style scoped>
.goods-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.detail-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.goods-info {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
}

.goods-image {
  flex: 1;
  max-width: 500px;
}

.goods-image img {
  width: 100%;
  height: auto;
  border-radius: 8px;
}

.goods-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.detail-item label {
  font-weight: 600;
  min-width: 100px;
  color: #606266;
}

.detail-item .price {
  font-size: 28px;
  font-weight: 600;
  color: #f56c6c;
}

.detail-item.description {
  flex-direction: column;
  align-items: flex-start;
}

.detail-item.description p {
  margin: 0;
  line-height: 1.6;
  color: #606266;
}

.order-section {
  margin-top: 40px;
  padding-top: 40px;
  border-top: 1px solid #ebeef5;
}

.order-form {
  max-width: 600px;
}

.order-form .el-form-item {
  margin-bottom: 24px;
}
</style>