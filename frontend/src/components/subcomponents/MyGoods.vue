<template>
  <div class="goods-container">
    <div class="header">
      <h2>{{ isUser(userInfo?.role) ? '商品' : '商品管理' }}</h2>
      <div class="actions">
        <el-button 
          type="primary" 
          @click="handleAdd"
          v-if="canAddGoods"
        >
          添加商品
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable style="width: 200px;">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <!-- 状态筛选只对商家和管理员显示 -->
        <el-form-item label="状态" v-if="!isUser(userInfo?.role)">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="上架" :value="0" />
            <el-option label="下架" :value="1" />
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
      <el-table :data="goodsList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="scope">
            {{ getCategoryName(scope.row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120">
          <template #default="scope">
            ¥{{ scope.row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="status" label="状态" width="100" v-if="!isUser(userInfo?.role)">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button 
              size="small" 
              type="success"
              @click="handleAddToCart(scope.row)"
              v-if="userInfo && isUser(userInfo?.role) && scope.row.status === 0 && scope.row.stock > 0"
            >
              加入购物车
            </el-button>
            <el-button 
              size="small" 
              @click="handleEdit(scope.row)"
              v-if="canEditGoods"
            >
              编辑
            </el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === 0 ? 'warning' : 'success'"
              @click="handleToggleStatus(scope.row)"
              v-if="canChangeGoodsStatus"
            >
              {{ scope.row.status === 0 ? '下架' : '上架' }}
            </el-button>
            <el-button 
              size="small" 
              type="danger"
              @click="handleDelete(scope.row)"
              v-if="canDeleteGoods"
            >
              删除
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :visible="dialogVisible"
      :title="dialogTitle"
      width="50%"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <!-- 删除库存预警阈值字段 -->
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="商品图片" prop="image">
          <!-- 图片预览（所有角色都可以查看） -->
          <div v-if="form.image" class="image-preview-container">
            <img 
              :src="getFullImageUrl(form.image)" 
              class="avatar" 
              v-show="canEditGoods"
              style="cursor: pointer;"
              title="点击更换图片"
            />
            <input 
              v-if="canEditGoods"
              type="file" 
              accept="image/*" 
              class="image-upload-input"
              @change="handleFileInputChange"
            />
          </div>
          <img 
            v-if="form.image && !canEditGoods" 
            :src="getFullImageUrl(form.image)" 
            class="avatar" 
          />
          <!-- 图片上传组件（只有商家和管理员可以看到） -->
          <el-upload
            ref="uploadRef"
            v-if="canEditGoods"
            class="avatar-uploader"
            action=""
            :auto-upload="false"
            :on-change="handleImageChange"
            :show-file-list="false"
            :file-list="imageFileList"
            accept="image/*"
          >
            <el-icon v-if="!form.image" class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">上架</el-radio>
            <el-radio :label="1">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getGoodsList, 
  getCategories, 
  addGoods, 
  updateGoods, 
  deleteGoods, 
  updateGoodsStatus 
} from '../../api/goods'
import { addToCart } from '../../api/cart'
import { getDefaultAddress } from '../../api/addresses'
import { hasPermission, isUser } from '../../utils/permissions'

// 获取当前用户信息
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{"role":"user"}')) // 从localStorage加载用户信息，如果没有则使用默认角色
try {
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo)
  }
} catch (error) {
  console.error('获取用户信息失败:', error)
  userInfo.value = { role: 'user' } // 出错时设置默认角色
}

// 响应式数据
const loading = ref(false)
const goodsList = ref([])
const categories = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const isEdit = ref(false)

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

// 搜索表单
const searchForm = reactive({
  name: '',
  category: '',
  status: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 表单数据
const form = reactive({
  id: null,
  name: '',
  category: '',
  price: 0,
  stock: 0,
  // 删除库存预警阈值字段
  description: '',
  image: '',
  status: 0
})
// 图片上传相关
const imageFileList = ref([])
const selectedImageFile = ref(null)
const uploadRef = ref(null)

// 监听库存变化，自动更新商品状态
watch(
  () => form.stock,
  (newStock, oldStock) => {
    // 如果库存从0变为正数，自动设置状态为上架
    if (oldStock === 0 && newStock > 0) {
      form.status = 0 // 0表示上架状态
    }
    // 如果库存从正数变为0，自动设置状态为下架
    if (oldStock > 0 && newStock === 0) {
      form.status = 1 // 1表示下架状态
    }
  }
)

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'change' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'change' }
  ],
  stock: [
    { required: true, message: '请输入商品库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存不能小于0', trigger: 'change' }
  ],
  // 删除库存预警阈值验证规则
}

// 计算属性
// 格式化时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  
  try {
    // 处理后端返回的时间格式
    const date = new Date(dateTime)
    if (isNaN(date.getTime())) {
      return '-'
    }
    
    // 格式化为 YYYY-MM-DD HH:mm:ss
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (error) {
    console.error('时间格式化错误:', error)
    return '-'
  }
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  // 确保categories.value是数组
  if (!Array.isArray(categories.value)) {
    console.error('categories.value is not an array:', categories.value)
    return '未知分类'
  }
  
  const category = categories.value.find(item => item.id === categoryId)
  return category ? category.name : '未知分类'
}

// 计算属性
// 检查当前用户是否有商品编辑权限
const canEditGoods = computed(() => {
  return userInfo.value && hasPermission(userInfo.value.role, 'goods:update')
})

// 检查当前用户是否有商品删除权限
const canDeleteGoods = computed(() => {
  return userInfo.value && hasPermission(userInfo.value.role, 'goods:delete')
})

// 检查当前用户是否有商品添加权限
const canAddGoods = computed(() => {
  return userInfo.value && hasPermission(userInfo.value.role, 'goods:create')
})

// 检查当前用户是否有商品状态修改权限
const canChangeGoodsStatus = computed(() => {
  return userInfo.value && hasPermission(userInfo.value.role, 'goods:publish')
})

// 方法
const fetchGoodsList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1, // 后端分页从0开始，前端从1开始，需要减1
      pageSize: pagination.pageSize,
      name: searchForm.name,
      category: searchForm.category,
      // 普通用户只能查看上架商品，自动设置status=0（0表示上架状态，1表示下架状态）
    status: isUser(userInfo?.value?.role) ? 0 : searchForm.status
    }
    const res = await getGoodsList(params)
    // 修正数据映射，后端返回的是Spring Data JPA的Page对象
    goodsList.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } catch (error) {
    ElMessage.error('获取商品列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    console.log('分类API响应:', res)
    // 确保API返回成功
    if (res.code === 200) {
      // 确保返回的数据是数组
      if (Array.isArray(res.data)) {
        console.log('设置分类数据:', res.data)
        console.log('分类数据详情:', JSON.stringify(res.data))
        categories.value = res.data
      } else if (res.data && Array.isArray(res.data.content)) {
        // 处理可能的分页响应
        console.log('设置分类数据(分页):', res.data.content)
        console.log('分类数据详情(分页):', JSON.stringify(res.data.content))
        categories.value = res.data.content
      } else {
        console.warn('分类数据格式异常:', res.data)
        categories.value = []
      }
    } else {
      console.error('分类API返回错误:', res)
      ElMessage.error(res.message || '获取分类列表失败')
      categories.value = []
    }
  } catch (error) {
    ElMessage.error('获取分类列表失败')
    console.error(error)
    categories.value = [] // 确保在错误情况下categories是数组
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchGoodsList()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  fetchGoodsList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchGoodsList()
}

// 监听页码变化，当用户在输入框中修改页码时触发
const handlePageInput = (val) => {
  // 确保页码在有效范围内
  const maxPage = Math.ceil(pagination.total / pagination.pageSize)
  if (val < 1) {
    pagination.currentPage = 1
  } else if (val > maxPage) {
    pagination.currentPage = maxPage
  } else {
    pagination.currentPage = val
  }
  fetchGoodsList()
}

const router = useRouter()

const handleDetail = (row) => {
  router.push(`/home/goods/${row.id}`)
}

const handleAddToCart = async (row) => {
  if (!userInfo.value || !isUser(userInfo.value.role)) {
    ElMessage.warning('只有普通用户可以添加商品到购物车')
    return
  }
  
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

    // 构建购物车项
    const cartItem = {
      goodsId: row.id,
      goodsName: row.name,
      price: row.price,
      quantity: 1,
      image: getFullImageUrl(row.image),
      addressId: addressInfo?.id,
      addressDetails: addressInfo?.address || ''
    }
    
    // 调用添加到购物车API
    const res = await addToCart(cartItem)
    if (res.code === 200) {
      ElMessage.success(res.message || '商品已添加到购物车')
    } else {
      ElMessage.error(res.message || '添加到购物车失败')
    }
  } catch (error) {
    console.error('添加到购物车失败:', error)
    ElMessage.error('添加到购物车失败')
  }
}

const handleAdd = () => {
  if (!canAddGoods.value) {
    ElMessage.warning('您没有添加商品的权限')
    return
  }
  
  isEdit.value = false
  dialogTitle.value = '添加商品'
  dialogVisible.value = true
  resetForm()
}

const handleEdit = (row) => {
  if (!canEditGoods.value) {
    ElMessage.warning('您没有编辑商品的权限')
    return
  }
  
  isEdit.value = true
  dialogTitle.value = '编辑商品'
  dialogVisible.value = true
  Object.keys(form).forEach(key => {
    // 处理分类字段的特殊映射，row中是categoryId，form中是category
    if (key === 'category' && row.categoryId !== undefined && row.categoryId !== null) {
      form[key] = row.categoryId
    } else {
      form[key] = row[key]
    }
  })
  
  // 重置图片文件列表，但保留当前图片的预览
  imageFileList.value = []
  selectedImageFile.value = null
}

const handleDelete = (row) => {
  if (!canDeleteGoods.value) {
    ElMessage.warning('您没有删除商品的权限')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除商品 "${row.name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await deleteGoods(row.id)
      ElMessage.success('删除成功')
      fetchGoodsList()
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }).catch(() => {
    // 用户取消删除
  })
}

const handleToggleStatus = (row) => {
  if (!canChangeGoodsStatus.value) {
    ElMessage.warning('您没有修改商品状态的权限')
    return
  }
  
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '上架' : '下架'
  
  ElMessageBox.confirm(
    `确定要${statusText}商品 "${row.name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await updateGoodsStatus(row.id, newStatus)
      ElMessage.success(`${statusText}成功`)
      fetchGoodsList()
    } catch (error) {
      ElMessage.error(`${statusText}失败`)
      console.error(error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const formData = new FormData()
        formData.append('name', form.name)
        formData.append('categoryId', form.category)
        formData.append('price', form.price)
        formData.append('stock', form.stock)
        formData.append('description', form.description)
        formData.append('status', form.status)
        
        // 如果有选择图片文件，则添加到FormData中
        console.log('Selected image file:', selectedImageFile.value)
        if (selectedImageFile.value && selectedImageFile.value.raw) {
          console.log('Adding image to FormData:', selectedImageFile.value.raw)
          formData.append('image', selectedImageFile.value.raw)
        }
        
        if (isEdit.value) {
          await updateGoods(form.id, formData)
          ElMessage.success('更新成功')
        } else {
          await addGoods(formData)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        fetchGoodsList()
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
        console.error('Submit error:', error)
      }
    }
  })
}

// 图片上传处理
const handleImageChange = (file) => {
  // 权限检查
  if (!canEditGoods.value) {
    ElMessage.warning('您没有上传商品图片的权限')
    return
  }
  
  selectedImageFile.value = file
  imageFileList.value = [file]
  
  // 预览图片
  if (file.raw) {
    const reader = new FileReader()
    reader.onload = (e) => {
      form.image = e.target.result
    }
    reader.readAsDataURL(file.raw)
  }
}

// 处理隐藏文件输入框的文件选择
const handleFileInputChange = (event) => {
  // 权限检查
  if (!canEditGoods.value) {
    ElMessage.warning('您没有更换商品图片的权限')
    return
  }
  
  const file = event.target.files[0]
  if (file) {
    // 创建一个模拟的El Upload文件对象
    const mockFile = {
      raw: file,
      name: file.name,
      size: file.size,
      type: file.type
    }
    
    // 调用原有的图片处理函数
    handleImageChange(mockFile)
    
    // 清空文件输入框，以便下次可以选择同一个文件
    event.target.value = ''
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
  imageFileList.value = []
  selectedImageFile.value = null
}

const resetForm = () => {
  Object.keys(form).forEach(key => {
    if (key === 'status') {
      form[key] = 0 // 0表示上架状态，1表示下架状态
    } else if (key === 'price' || key === 'stock') {
      form[key] = 0
    } else {
      form[key] = ''
    }
  })
  imageFileList.value = []
  selectedImageFile.value = null
}

// 生命周期
onMounted(() => {
  fetchGoodsList()
  fetchCategories()
})
</script>

<style scoped>
.goods-container {
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

/* 图片上传样式 */
.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 178px;
  height: 178px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar {
  width: 178px;
  height: 178px;
  object-fit: cover;
  display: block;
}
/* 图片预览容器样式 */
.image-preview-container {
  position: relative;
  display: inline-block;
}

/* 隐藏的文件上传输入框 */
.image-upload-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}
</style>