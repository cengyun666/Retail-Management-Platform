<template>
  <div class="checkout-container">
    <el-card v-loading="loading" class="checkout-card">
      <template #header>
        <div class="card-header">
          <h2>确认订单</h2>
        </div>
      </template>

      <div v-if="!checkoutItems || checkoutItems.length === 0" class="empty-checkout">
        <el-empty description="没有要结算的商品">
          <el-button type="primary" @click="goToCart">返回购物车</el-button>
        </el-empty>
      </div>

      <div v-else class="checkout-content">
        <!-- 收货地址 -->
        <div class="address-section">
          <h3>收货地址</h3>
          <div class="address-list">
            <div 
              v-for="address in addresses" 
              :key="address.id"
              class="address-item"
              :class="{ 'selected': selectedAddressId === address.id }"
              @click="selectAddress(address.id)"
            >
              <div class="address-info">
                <div class="address-header">
                  <span class="name">{{ address.name }}</span>
                  <span class="phone">{{ address.phone }}</span>
                  <el-tag v-if="address.isDefault" type="success" size="small">默认</el-tag>
                </div>
                <div class="address-detail">
                  {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail }}
                </div>
              </div>
              <div class="address-actions">
                <el-button 
                  type="text" 
                  size="small" 
                  @click.stop="editAddress(address)"
                >
                  编辑
                </el-button>
                <el-button 
                  type="text" 
                  size="small" 
                  @click.stop="confirmDeleteAddress(address)"
                  danger
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
            
            <div class="add-address" @click="showAddAddressDialog = true">
              <el-icon><Plus /></el-icon>
              <span>添加新地址</span>
            </div>
          </div>
        </div>

        <!-- 商品列表 -->
        <div class="goods-section">
          <h3>商品信息</h3>
          <div class="goods-list">
            <div v-for="item in checkoutItems" :key="item.id" class="goods-item">
              <img 
  :src="getFullImageUrl(item.image)" 
  :alt="item.goodsName" 
  class="goods-image" 
  @error="$event.target.src='https://picsum.photos/seed/error/80/80'"
/>
              <div class="goods-info">
                <div class="goods-name">{{ item.goodsName }}</div>
                <div class="goods-price">¥{{ item.price.toFixed(2) }}</div>
              </div>
              <div class="goods-quantity">× {{ item.quantity }}</div>
              <div class="goods-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            </div>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="payment-section">
          <h3>支付方式</h3>
          <el-radio-group v-model="selectedPaymentMethod">
            <el-radio label="alipay">
              <div class="payment-method">
                <span class="payment-icon alipay-icon">支付宝</span>
                <span>支付宝</span>
              </div>
            </el-radio>
            <el-radio label="wechat">
              <div class="payment-method">
                <span class="payment-icon wechat-icon">微信</span>
                <span>微信支付</span>
              </div>
            </el-radio>
            <el-radio label="unionpay">
              <div class="payment-method">
                <span class="payment-icon unionpay-icon">银联</span>
                <span>银联支付</span>
              </div>
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 订单备注 -->
        <div class="note-section">
          <h3>订单备注</h3>
          <el-input
            v-model="orderNote"
            type="textarea"
            :rows="3"
            placeholder="请输入订单备注（选填）"
            maxlength="200"
            show-word-limit
          />
        </div>

        <!-- 订单总结 -->
        <div class="order-summary">
          <div class="summary-row">
            <span>商品总额：</span>
            <span>¥{{ goodsTotal.toFixed(2) }}</span>
          </div>
          <div class="summary-row total">
            <span>应付总额：</span>
            <span class="total-price">¥{{ orderTotal.toFixed(2) }}</span>
          </div>
        </div>

        <!-- 提交订单 -->
        <div class="submit-section">
          <el-button @click="goToCart">返回购物车</el-button>
          <el-button 
            type="primary" 
            size="large"
            @click="submitOrder"
            :disabled="!selectedAddressId || !selectedPaymentMethod"
          >
            提交订单
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 添加/编辑地址对话框 -->
    <el-dialog
      v-model="showAddAddressDialog"
      :title="editingAddress ? '编辑地址' : '添加地址'"
      width="500px"
    >
      <el-form
        ref="addressFormRef"
        :model="addressForm"
        :rules="addressRules"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="name">
          <el-input v-model="addressForm.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-select v-model="addressForm.province" placeholder="请选择省份" @change="handleProvinceChange">
            <el-option 
              v-for="province in provinces" 
              :key="province.id" 
              :label="province.name" 
              :value="province.name" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-select v-model="addressForm.city" placeholder="请选择城市" @change="handleCityChange">
            <el-option 
              v-for="city in cities" 
              :key="city.id" 
              :label="city.name" 
              :value="city.name" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-select v-model="addressForm.district" placeholder="请选择区县">
            <el-option 
              v-for="district in districts" 
              :key="district.id" 
              :label="district.name" 
              :value="district.name" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input
            v-model="addressForm.detail"
            type="textarea"
            :rows="2"
            placeholder="请输入详细地址"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddAddressDialog = false">取消</el-button>
          <el-button type="primary" @click="saveAddress">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { createOrder } from '../../api/orders'
import { getGoodsById } from '../../api/goods'
import { getUserAddresses, addAddress, updateAddress, deleteAddress as deleteAddressAPI, setDefaultAddress } from '../../api/addresses'
import { updateCartItem } from '../../api/cart'
import addressData from '../../utils/addressData.json'

// 图片URL处理方法
const getFullImageUrl = (url) => {
  if (!url) return '';
  
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
const checkoutItems = ref([])
const selectedAddressId = ref(null)
const selectedPaymentMethod = ref('alipay')
const orderNote = ref('')

// 地址相关
const addresses = ref([])
const showAddAddressDialog = ref(false)
const editingAddress = ref(null)
const addressFormRef = ref(null)

// 地址下拉框数据
const provinces = ref(addressData.provinces)
const cities = ref([])
const districts = ref([])

const addressForm = ref({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

const addressRules = {
  name: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  province: [
    { required: true, message: '请输入省份', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入城市', trigger: 'blur' }
  ],
  district: [
    { required: true, message: '请输入区县', trigger: 'blur' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 计算属性
const goodsTotal = computed(() => {
  return checkoutItems.value.reduce((total, item) => total + (item.price * item.quantity), 0)
})

const orderTotal = computed(() => {
  return goodsTotal.value
})

// 方法
const initCheckoutData = () => {
  // 从本地存储获取结算商品
  const savedItems = localStorage.getItem('checkoutItems')
  if (savedItems) {
    checkoutItems.value = JSON.parse(savedItems)
  } else {
    checkoutItems.value = []
  }
  
  // 获取用户地址
  fetchUserAddresses()
}

const fetchUserAddresses = async () => {
  try {
    loading.value = true
    const res = await getUserAddresses()
    addresses.value = res.data || []
    
    // 设置默认选中地址
    if (addresses.value.length > 0) {
      const defaultAddress = addresses.value.find(addr => addr.isDefault)
      selectedAddressId.value = defaultAddress ? defaultAddress.id : addresses.value[0].id
    } else {
      selectedAddressId.value = null
    }
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '获取地址失败'
    ElMessage.error(errorMsg)
    console.error('获取地址失败:', error)
    // 如果获取失败，使用空数组
    addresses.value = []
  } finally {
    loading.value = false
  }}


const selectAddress = async (addressId) => {
  selectedAddressId.value = addressId
  
  // 获取选中的地址详情
  const selectedAddress = addresses.value.find(addr => addr.id === addressId)
  if (selectedAddress) {
    // 同步更新购物车中所有商品的地址信息
    try {
      for (const item of checkoutItems.value) {
        await updateCartItem(item.id, {
          addressId: addressId,
          addressDetails: {
            name: selectedAddress.name,
            phone: selectedAddress.phone,
            province: selectedAddress.province,
            city: selectedAddress.city,
            district: selectedAddress.district,
            detail: selectedAddress.detail,
            isDefault: selectedAddress.isDefault
          }
        })
      }
      ElMessage.success('收货地址已同步到购物车')
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.message || '地址同步失败'
      console.error('同步地址到购物车失败:', errorMsg)
    }
  }
}

const editAddress = (address) => {
  editingAddress.value = address
  addressForm.value = { ...address }
  
  // 初始化下拉框数据
  if (address.province) {
    const province = provinces.value.find(p => p.name === address.province)
    if (province) {
      cities.value = province.cities
      if (address.city) {
        const city = province.cities.find(c => c.name === address.city)
        if (city) {
          districts.value = city.districts
        }
      }
    }
  }
  
  showAddAddressDialog.value = true
}

const confirmDeleteAddress = (address) => {
  ElMessageBox.confirm(`确定要删除${address.name}的地址吗？`, '删除地址', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    handleDeleteAddress(address.id)
  }).catch(() => {
    // 取消删除
  })
}

const handleDeleteAddress = async (addressId) => {
  try {
    await deleteAddressAPI(addressId)
    ElMessage.success('地址删除成功')
    // 重新获取地址列表
    await fetchUserAddresses()
    // 如果删除的是当前选中的地址，重新选择地址
    if (selectedAddressId.value === addressId) {
      selectedAddressId.value = addresses.value.length > 0 ? addresses.value[0].id : null
    }
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message || '地址删除失败'
    ElMessage.error(errorMsg)
    console.error('删除地址失败:', error)
  }
}

const saveAddress = async () => {
  addressFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let res
        if (editingAddress.value) {
          // 编辑地址
          res = await updateAddress(editingAddress.value.id, addressForm.value)
        } else {
          // 添加新地址
          res = await addAddress(addressForm.value)
        }
        
        ElMessage.success(editingAddress.value ? '地址更新成功' : '地址添加成功')
        
        // 重置表单
        addressForm.value = {
          name: '',
          phone: '',
          province: '',
          city: '',
          district: '',
          detail: '',
          isDefault: false
        }
        editingAddress.value = null
        showAddAddressDialog.value = false
        // 重置下拉框数据
        cities.value = []
        districts.value = []
        
        // 重新获取地址列表
        await fetchUserAddresses()
      } catch (error) {
        const errorMsg = error.response?.data?.message || error.message || '地址保存失败'
        ElMessage.error(errorMsg)
        console.error('保存地址失败:', error)
      }
    }
  })
}

const submitOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  if (!selectedPaymentMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }
  
  try {
    loading.value = true
    
    // 获取选中的地址
    const selectedAddress = addresses.value.find(addr => addr.id === selectedAddressId.value)
    
    // 获取当前用户信息
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) {
      ElMessage.error('请先登录后再提交订单')
      loading.value = false
      router.push('/login')
      return
    }
    
    const currentUser = JSON.parse(userInfoStr)
    
    // 确保使用正确的用户ID和用户名
    const userId = currentUser.id || currentUser.userId
    const userName = currentUser.username || currentUser.name
    
    if (!userId || !userName) {
      ElMessage.error('用户信息不完整，请重新登录')
      loading.value = false
      router.push('/login')
      return
    }
    
    // 重新获取商品的最新价格，确保前后端数据一致
    const updatedCheckoutItems = []
    let totalPrice = 0
    
    for (const item of checkoutItems.value) {
      try {
        const goodsDetail = await getGoodsById(item.goodsId)
        const updatedItem = {
          ...item,
          price: goodsDetail.data.price || item.price // 使用后端返回的最新价格
        }
        updatedCheckoutItems.push(updatedItem)
        totalPrice += updatedItem.price * updatedItem.quantity
      } catch (error) {
        console.error('获取商品详情失败:', error)
        // 如果获取失败，使用原来的价格
        updatedCheckoutItems.push(item)
        totalPrice += item.price * item.quantity
      }
    }
    
    // 构建订单数据，符合API期望的结构
    const orderData = {
      userId: userId,
      totalAmount: totalPrice,
      address: `${selectedAddress.province}${selectedAddress.city}${selectedAddress.district}${selectedAddress.detail}` || '未设置详细地址', // 添加完整的地址信息
      remark: orderNote.value, // 添加订单备注
      goods: updatedCheckoutItems.map(item => ({
        id: item.goodsId,
        name: item.goodsName || item.name,
        price: item.price,
        quantity: item.quantity,
        image: item.image // 添加商品图片信息
      }))
    }
    
    // 提交订单
    const res = await createOrder(orderData)
    
    ElMessage.success('订单提交成功')
    
    // 清空本地存储的结算商品
    localStorage.removeItem('checkoutItems')
    
    // 跳转到支付页面，传递订单ID和金额
    console.log('API返回结果:', res)
    console.log('订单ID:', res.data.id)
    console.log('订单金额:', orderTotal.value)
    
    router.push({
      path: '/home/payment',
      query: {
        orderId: res.data.id,
        amount: orderTotal.value
      }
    })
    console.log('跳转路由参数:', { orderId: res.data.id, amount: orderTotal.value })
  } catch (error) {
    // 显示更详细的错误信息
    const errorMsg = error.response?.data?.message || error.message || '订单提交失败'
    ElMessage.error(errorMsg)
    console.error('订单提交失败:', error)
  } finally {
    loading.value = false
  }
}

const goToCart = () => {
  router.push('/home/cart')
}

// 生命周期
onMounted(() => {
  initCheckoutData()
})
// 处理省份变化
const handleProvinceChange = (provinceName) => {
  const province = provinces.value.find(p => p.name === provinceName)
  if (province) {
    cities.value = province.cities
    districts.value = []
    addressForm.value.city = ''
    addressForm.value.district = ''
  }
}

// 处理城市变化
const handleCityChange = (cityName) => {
  const province = provinces.value.find(p => p.name === addressForm.value.province)
  if (province) {
    const city = province.cities.find(c => c.name === cityName)
    if (city) {
      districts.value = city.districts
      addressForm.value.district = ''
    }
  }
}
</script>

<style scoped>
.checkout-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.checkout-card {
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

.empty-checkout {
  padding: 40px 0;
  text-align: center;
}

.checkout-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.address-section, .goods-section, .payment-section, .note-section {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
}

.address-section h3, .goods-section h3, .payment-section h3, .note-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #303133;
}

.address-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.address-item {
  position: relative;
  width: calc(50% - 8px);
  padding: 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #409eff;
}

.address-item.selected {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.address-info {
  margin-bottom: 10px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.name {
  font-weight: 600;
  color: #303133;
}

.phone {
  color: #606266;
}

.address-detail {
  color: #606266;
  font-size: 14px;
}

.address-actions {
  position: absolute;
  top: 10px;
  right: 10px;
}

.add-address {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: calc(50% - 8px);
  height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  color: #909399;
}

.add-address:hover {
  border-color: #409eff;
  color: #409eff;
}

.add-address .el-icon {
  margin-bottom: 5px;
  font-size: 24px;
}

.goods-list {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.goods-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.goods-item:last-child {
  border-bottom: none;
}

.goods-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  margin-right: 15px;
}

.goods-info {
  flex: 1;
}

.goods-name {
  margin-bottom: 5px;
  color: #303133;
}

.goods-price {
  color: #f56c6c;
  font-weight: 600;
}

.goods-quantity {
  width: 80px;
  text-align: center;
  color: #606266;
}

.goods-total {
  width: 100px;
  text-align: right;
  color: #f56c6c;
  font-weight: 600;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 10px;
}

.payment-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  color: white;
}

.alipay-icon {
  background-color: #1677ff;
}

.wechat-icon {
  background-color: #07c160;
}

.unionpay-icon {
  background-color: #e02020;
}

.order-summary {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  border: 1px solid #ebeef5;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #606266;
}

.summary-row:last-child {
  margin-bottom: 0;
}

.summary-row.discount {
  color: #67c23a;
}

.summary-row.total {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  border-top: 1px dashed #ebeef5;
  padding-top: 10px;
}

.total-price {
  color: #f56c6c;
}

.submit-section {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

@media (max-width: 768px) {
  .address-item, .add-address {
    width: 100%;
  }
  
  .goods-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .goods-image {
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .goods-info {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .goods-quantity, .goods-total {
    width: 100%;
    text-align: left;
  }
}
</style>