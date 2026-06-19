<template>
  <div class="orders-container">
    <div class="header">
      <h2>{{ (userInfo.role || '').toLowerCase() === 'user' ? '我的订单' : '订单管理' }}</h2>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单编号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单编号" clearable />
        </el-form-item>
        <el-form-item label="用户名称">
          <el-input v-model="searchForm.userName" placeholder="请输入用户名称" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option v-for="item in statusList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格区域 -->
    <div class="table-container">
      <el-table :data="ordersList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="userName" label="用户名称" width="120" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="scope">
            ¥{{ scope.row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">详情</el-button>
            <el-button 
              size="small" 
              type="success"
              @click="handlePayment(scope.row)"
              v-if="!canViewAllOrders && scope.row.status === 1"
            >
              付款
            </el-button>
            <el-button 
              size="small" 
              type="warning"
              @click="handleUpdateStatus(scope.row)"
              v-if="canUpdateOrderStatus && ((canViewAllOrders && scope.row.status === 2) || (!canViewAllOrders && scope.row.status === 3))"
            >
              处理
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(scope.row)"
              v-if="canDeleteOrder(scope.row)"
            >
              删除
            </el-button>
            <el-button 
              size="small" 
              type="warning"
              @click="handleCancel(scope.row)"
              v-if="canCancelOrder(scope.row) && scope.row.status !== 4 && scope.row.status !== 5"
            >
              取消订单
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :layout="`prev, pager, next`"
          :total="pagination.total"
          :prev-text="'上一页'"
          :next-text="'下一页'"
          :pager-count="7"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        >
        </el-pagination>
        <div class="pagination-info">
          <span>总计 {{ pagination.total }} 条</span>
          <el-select v-model="pagination.pageSize" style="width: 100px; margin: 0 20px" @change="handleSizeChange">
            <el-option
              v-for="item in [10, 20, 50, 100]"
              :key="item"
              :label="`${item} 条/页`"
              :value="item"
            />
          </el-select>
        </div>
      </div>
    </div>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="60%"
      @close="handleDetailDialogClose"
    >
      <div class="order-detail">
        <div class="detail-header">
          <h3>订单信息</h3>
          <el-tag :type="getStatusType(currentOrder.status)">
            {{ getStatusText(currentOrder.status) }}
          </el-tag>
        </div>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="用户名称">{{ currentOrder.userName }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ currentOrder.totalAmount.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ getStatusText(currentOrder.status) }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" v-if="currentOrder.address">{{ currentOrder.address }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" v-else>未设置</el-descriptions-item>
          <el-descriptions-item label="订单备注">
              {{ currentOrder.remark !== undefined && currentOrder.remark !== null ? currentOrder.remark : '无' }}
            </el-descriptions-item>
        </el-descriptions>
        
        <div class="order-items">
          <h4>订单商品</h4>
          <el-table :data="currentOrder.orderItems" border style="width: 100%" size="small" v-if="currentOrder.orderItems && currentOrder.orderItems.length > 0">
            <el-table-column prop="goodsName" label="商品名称" />
            <el-table-column prop="price" label="单价" width="100">
              <template #default="scope">¥{{ scope.row.price ? scope.row.price.toFixed(2) : '0.00' }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100">
              <template #default="scope">{{ scope.row.quantity || 0 }}</template>
            </el-table-column>
            <el-table-column label="小计" width="120">
                <template #default="scope">¥{{ scope.row.subtotal ? scope.row.subtotal.toFixed(2) : '0.00' }}</template>
              </el-table-column>
          </el-table>
          <el-empty v-else description="暂无商品信息" />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            type="success" 
            @click="handleConfirmReceipt"
            v-if="currentOrder.status === 3 && !canViewAllOrders"
          >
            确认收货
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 更新状态对话框 -->
    <el-dialog
      v-model="statusDialogVisible"
      title="更新订单状态"
      width="40%"
    >
      <el-form>
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(currentOrder.status)">
            {{ getStatusText(currentOrder.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="newStatus" style="width: 100%" placeholder="请选择新状态">
            <el-option 
              v-for="item in availableStatus"
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleStatusUpdate">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, View, Edit, Delete, Check } from '@element-plus/icons-vue'
import { getOrdersList, updateOrderStatus, deleteOrder, getOrderStatusList, getOrderById, cancelOrder, confirmReceipt } from '../../api/orders'
import { hasPermission, hasAnyPermission } from '../../utils/permissions'
import { ROLES, PERMISSIONS } from '../../utils/roles'

// 获取当前用户信息
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

// 路由
const router = useRouter()

// 权限检查
const canViewAllOrders = computed(() => {
  return hasPermission(userInfo.value.role, PERMISSIONS.ORDER_READ_ALL)
})

const canUpdateOrderStatus = computed(() => {
  return hasPermission(userInfo.value.role, PERMISSIONS.ORDER_UPDATE) || 
         hasPermission(userInfo.value.role, PERMISSIONS.ORDER_PROCESS)
})

const canDeleteOrder = (order) => {
  // 检查订单状态，只有已取消(5)和已完成(4)状态的订单才能删除
  if (order.status !== 4 && order.status !== 5) {
    return false
  }
  
  // 管理员可以删除所有符合条件的订单
  if (hasPermission(userInfo.value.role, PERMISSIONS.ORDER_DELETE)) {
    return true
  }
  
  // 用户和商家只能删除自己的订单
  if (hasPermission(userInfo.value.role, PERMISSIONS.ORDER_DELETE_OWN)) {
    // 普通用户只能删除自己的订单
    if (userInfo.value.role === 'user' && order.userId === userInfo.value.id) {
      return true
    }
    
    // 商家可以删除与自己相关的订单（由后端验证）
    if (userInfo.value.role === 'merchant') {
      return true
    }
  }
  
  return false
}

const canCancelOrder = (order) => {
  // 管理员可以取消所有订单
  if (hasPermission(userInfo.value.role, PERMISSIONS.ORDER_CANCEL) && userInfo.value.role === 'admin') {
    return true
  }
  
  // 用户和商家只能取消自己的订单
  if (hasPermission(userInfo.value.role, PERMISSIONS.ORDER_CANCEL)) {
    // 普通用户只能取消自己的订单
    if (userInfo.value.role === 'user' && order.userId === userInfo.value.id) {
      return true
    }
    
    // 商家只能取消与自己相关的订单
    if (userInfo.value.role === 'merchant' && order.merchantId === userInfo.value.id) {
      return true
    }
  }
  
  return false
}

// 响应式数据
const loading = ref(false)
const ordersList = ref([])
const statusList = ref([])
const detailDialogVisible = ref(false)
const statusDialogVisible = ref(false)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  userName: '',
  status: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 当前订单
const currentOrder = ref({
  id: '',
  orderNo: '',
  userId: '',
  userName: '',
  totalAmount: 0,
  status: 1,
  createTime: '',
  payTime: null,
  deliverTime: null,
  finishTime: null,
  orderItems: []
})

// 新状态
const newStatus = ref(2)

// 计算属性 - 可用状态
const availableStatus = computed(() => {
  const currentStatus = currentOrder.value.status
  const allStatus = statusList.value
  
  // 根据当前状态和用户角色决定可用的新状态
  if (canViewAllOrders.value) {
    // 商家只能将待发货状态更新为待收货
    switch (currentStatus) {
      case 2: // 待发货
        return allStatus.filter(s => s.value === 3)
      default:
        return []
    }
  } else {
    // 普通用户的状态更新逻辑
    switch (currentStatus) {
      case 1: // 待付款
        return allStatus.filter(s => s.value === 2 || s.value === 5)
      case 3: // 待收货
        return allStatus.filter(s => s.value === 4)
      default:
        return []
    }
  }
})

// 方法
// 获取订单列表
const fetchOrdersList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    let params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    // 分别处理搜索条件
    if (searchForm.orderNo) {
      params.orderNo = searchForm.orderNo
    }
    if (searchForm.userName) {
      params.userName = searchForm.userName
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    
    // 根据用户角色设置查询参数
    if (userInfo.value.role === 'user') {
      // 普通用户只能查看自己的订单
      params.userId = userInfo.value.id
    } else if (userInfo.value.role === 'merchant') {
      // 商家只能查看与自己相关的订单
      params.merchantId = userInfo.value.id
    }
    // 管理员可以查看所有订单，不需要额外参数
    
    const res = await getOrdersList(params)
    ordersList.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取订单列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchStatusList = async () => {
  try {
    const res = await getOrderStatusList()
    statusList.value = res.data || []
  } catch (error) {
    ElMessage.error('获取状态列表失败')
    console.error(error)
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchOrdersList()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  fetchOrdersList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchOrdersList()
}

const getStatusText = (status) => {
  const statusMap = {
    1: '待付款',
    2: '待发货',
    3: '待收货',
    4: '已完成',
    5: '已取消'
  }
  return statusMap[status] || '未知状态'
}

const getStatusType = (status) => {
  const typeMap = {
    1: 'info',
    2: 'primary',
    3: 'success',
    4: 'success',
    5: 'danger'
  }
  return typeMap[status] || 'info'
}

const handleView = async (row) => {
  // 根据用户角色检查权限
  if (userInfo.value.role === 'user') {
    // 普通用户只能查看自己的订单
    if (row.userId !== userInfo.value.id) {
      ElMessage.warning('您只能查看自己的订单')
      return
    }
  }
  // 商家和管理员权限检查在获取订单详情后进行
  
  try {
    const res = await getOrderById(row.id)
    const order = res.data
    
    // 商家权限检查：检查订单中的商品是否属于当前商家
    if (userInfo.value.role === 'merchant') {
      // 如果订单没有订单项，显示提示信息
      if (!order.orderItems || order.orderItems.length === 0) {
        ElMessage.warning('该订单没有商品信息，无法查看详情')
        return
      }
      
      // 检查订单中是否有属于该商家的商品
      const hasMerchantGoods = order.orderItems.some(item => 
        item.goods && item.goods.merchantId === userInfo.value.id
      )
      
      if (!hasMerchantGoods) {
        ElMessage.warning('您只能查看与自己相关的订单')
        return
      }
    }
    
    // 添加调试信息
    console.log('完整响应数据:', res)
    console.log('订单数据:', order)
    console.log('订单备注:', order.remark)
    console.log('订单项:', order.orderItems)
    
    currentOrder.value = order
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取订单详情失败')
    console.error(error)
  }
}

// 付款处理
const handlePayment = (row) => {
  // 根据用户角色检查权限
  if (userInfo.value.role === 'user') {
    // 普通用户只能支付自己的订单
    if (row.userId !== userInfo.value.id) {
      ElMessage.warning('您只能支付自己的订单')
      return
    }
  } else if (userInfo.value.role === 'merchant') {
    // 商家不应该支付订单，只处理订单
    ElMessage.warning('商家不能支付订单')
    return
  }
  // 管理员可以支付所有订单（如果需要）
  
  // 检查订单状态是否为待付款
  if (row.status !== 1) {
    ElMessage.warning('只有待付款状态的订单才能支付')
    return
  }
  
  // 跳转到支付页面，传递订单ID和金额
  router.push({
    path: '/home/payment',
    query: {
      orderId: row.id,
      amount: row.totalAmount
    }
  })
}

const handleUpdateStatus = (row) => {
  // 前端不再进行权限检查，所有权限验证由后端处理
  // 只检查订单状态是否可操作
  
  currentOrder.value = row
  
  // 根据用户角色设置默认新状态
  if (userInfo.value.role === 'admin') {
    // 管理员可以将订单状态更新为任何有效状态
    // 这里可以根据实际需求添加更多逻辑
    switch (row.status) {
      case 1: // 待付款
        newStatus.value = 2
        break
      case 2: // 待发货
        newStatus.value = 3
        break
      case 3: // 待收货
        newStatus.value = 4
        break
      default:
        ElMessage.warning('该订单状态无法处理')
        return
    }
  } else if (userInfo.value.role === 'merchant') {
    // 商家只能将待发货状态更新为待收货
    if (row.status === 2) {
      newStatus.value = 3
    } else {
      ElMessage.warning('该订单状态无法处理')
      return
    }
  } else {
    // 普通用户的状态更新逻辑
    switch (row.status) {
      case 1: // 待付款
        newStatus.value = 2
        break
      case 3: // 待收货
        newStatus.value = 4
        break
      default:
        ElMessage.warning('该订单状态无法处理')
        return
    }
  }
  
  statusDialogVisible.value = true
}

const handleStatusUpdate = async () => {
    try {
      await updateOrderStatus(currentOrder.value.id, newStatus.value, userInfo.value.id)
      ElMessage.success('更新订单状态成功')
      statusDialogVisible.value = false
      fetchOrdersList()
      // 更新当前订单详情
      const res = await getOrderById(currentOrder.value.id)
      currentOrder.value = res.data
    } catch (error) {
      ElMessage.error('更新订单状态失败')
      console.error('Debug: handleStatusUpdate error:', error)
    }
  }

const handleDelete = (row) => {
  // 根据用户角色检查权限
  if (userInfo.value.role === 'user') {
    // 普通用户只能删除自己的订单
    if (row.userId !== userInfo.value.id) {
      ElMessage.warning('您只能删除自己的订单')
      return
    }
  }
  // 商家权限由后端验证
  // 管理员可以删除所有订单
  
  // 检查订单状态，只有已取消(5)和已完成(4)状态的订单才能删除
  if (row.status !== 4 && row.status !== 5) {
    ElMessage.warning('只有已完成或已取消的订单才能删除')
    return
  }
  
  ElMessageBox.confirm(
    `确认删除订单 ${row.orderNo} 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await deleteOrder(row.id)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          fetchOrdersList()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (error) {
        console.error('删除订单失败:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {
      // 用户取消删除
    })
}

// 取消订单
const handleCancel = (row) => {
  // 根据用户角色检查权限
  if (userInfo.value.role === 'user') {
    // 普通用户只能取消自己的订单
    if (row.userId !== userInfo.value.id) {
      ElMessage.warning('您只能取消自己的订单')
      return
    }
  } else if (userInfo.value.role === 'merchant') {
    // 商家只能取消与自己相关的订单
    if (row.merchantId !== userInfo.value.id) {
      ElMessage.warning('您只能取消与自己相关的订单')
      return
    }
  }
  // 管理员可以取消所有订单
  
  // 检查订单状态是否可以取消
  if (row.status === 4) {
    ElMessage.warning('已完成的订单不能取消')
    return
  }
  
  if (row.status === 5) {
    ElMessage.warning('订单已经是取消状态')
    return
  }
  
  ElMessageBox.confirm(
    `确认取消订单 ${row.orderNo} 吗？取消后无法恢复。`,
    '取消订单确认',
    {
      confirmButtonText: '确定取消',
      cancelButtonText: '保留订单',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await cancelOrder(row.id)
        if (res.code === 200) {
          ElMessage.success('取消订单成功')
          fetchOrdersList()
        } else {
          ElMessage.error(res.message || '取消订单失败')
        }
      } catch (error) {
        console.error('取消订单失败:', error)
        ElMessage.error('取消订单失败')
      }
    })
    .catch(() => {
      // 用户取消操作
    })
}

// 确认收货
const handleConfirmReceipt = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用确认收货API
    const res = await confirmReceipt(currentOrder.value.id)
    if (res.code === 200) {
      ElMessage.success('确认收货成功')
      // 更新订单状态
      currentOrder.value.status = 4
      // 刷新订单列表
      fetchOrdersList()
    } else {
      ElMessage.error(res.message || '确认收货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认收货失败')
      console.error(error)
    }
  }
}

const handleDetailDialogClose = () => {
  // 重置当前订单
  currentOrder.value = {
    id: '',
    orderNo: '',
    userId: '',
    userName: '',
    totalAmount: 0,
    status: 1,
    createTime: '',
    payTime: null,
    deliverTime: null,
    finishTime: null,
    orderItems: []
  }
}

// 生命周期
onMounted(() => {
  fetchOrdersList()
  fetchStatusList()
})
</script>

<style scoped>
.orders-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #333;
}

.search-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.table-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.pagination-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #606266;
}

.order-detail {
  padding: 10px 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.detail-header h3 {
  margin: 0;
}

.order-items {
  margin-top: 30px;
}

.order-items h4 {
  margin-bottom: 10px;
}
</style>