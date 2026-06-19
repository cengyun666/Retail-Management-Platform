<template>
  <div class="category-container">
    <div class="header">
      <h2>商品分类管理</h2>
      <el-button type="primary" @click="handleAdd" v-if="canAddCategory">
        <el-icon><Plus /></el-icon>
        添加分类
      </el-button>
    </div>

    <div class="search-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="分类名称">
          <el-input v-model="searchForm.name" placeholder="请输入分类名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <el-table :data="categoryList" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleEdit(scope.row)"
              v-if="canEditCategory"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(scope.row)"
              v-if="canDeleteCategory"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
        <div class="pagination-info">
          <span>共 {{ pagination.total }} 条记录</span>
          <span>当前第 {{ pagination.currentPage }} 页</span>
        </div>
      </div>
    </div>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="500px"
      @close="handleDialogClose"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            placeholder="请输入分类描述"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getCategories, 
  addCategory, 
  updateCategory, 
  deleteCategory
} from '../../api/category'
import { hasPermission } from '../../utils/permissions'

// 获取当前用户信息
const userInfo = ref({ role: 'user' }) // 设置默认角色，防止undefined错误
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
const categoryList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const isEdit = ref(false)

// 搜索表单
const searchForm = reactive({
  name: ''
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
  description: '',
  sortOrder: 0
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
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

// 检查当前用户是否有分类添加权限
const canAddCategory = computed(() => {
  return userInfo.value && hasPermission(userInfo.value.role, 'category:create')
})

// 检查当前用户是否有分类编辑权限
const canEditCategory = computed(() => {
  return userInfo.value && hasPermission(userInfo.value.role, 'category:update')
})

// 检查当前用户是否有分类删除权限
const canDeleteCategory = computed(() => {
  return userInfo.value && hasPermission(userInfo.value.role, 'category:delete')
})

// 方法
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1, // 后端分页从0开始，前端从1开始，需要减1
      pageSize: pagination.pageSize,
      name: searchForm.name
    }
    const res = await getCategories(params)
    // 修正数据映射，后端返回的是Spring Data JPA的Page对象
    categoryList.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } catch (error) {
    ElMessage.error('获取分类列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchCategoryList()
}

const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  fetchCategoryList()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchCategoryList()
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
  fetchCategoryList()
}

const handleAdd = () => {
  if (!canAddCategory.value) {
    ElMessage.warning('您没有添加分类的权限')
    return
  }
  
  isEdit.value = false
  dialogTitle.value = '添加分类'
  dialogVisible.value = true
  resetForm()
}

const handleEdit = (row) => {
  if (!canEditCategory.value) {
    ElMessage.warning('您没有编辑分类的权限')
    return
  }
  
  isEdit.value = true
  dialogTitle.value = '编辑分类'
  dialogVisible.value = true
  // 复制行数据到表单
  form.id = row.id
  form.name = row.name
  form.description = row.description
  form.sortOrder = row.sortOrder
}



const handleDelete = (row) => {
  if (!canDeleteCategory.value) {
    ElMessage.warning('您没有删除分类的权限')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除分类 "${row.name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await deleteCategory(row.id)
      ElMessage.success('删除成功')
      fetchCategoryList()
    } catch (error) {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }).catch(() => {
    // 用户取消删除
  })
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 构建提交数据
        const submitData = { ...form }
        
        if (isEdit.value) {
          await updateCategory(form.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await addCategory(submitData)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        fetchCategoryList()
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
        console.error(error)
      }
    }
  })
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

const resetForm = () => {
  Object.keys(form).forEach(key => {
    if (key === 'sortOrder') {
      form[key] = 0
    } else {
      form[key] = ''
    }
  })
}

// 生命周期
onMounted(() => {
  fetchCategoryList()
})
</script>

<style scoped>
.category-container {
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
</style>