<template>
  <div class="category-management">
    <div class="header">
      <h2>商品分类管理</h2>
      <el-button type="primary" @click="showAddDialog">添加分类</el-button>
    </div>

    <div class="search-area">
      <el-input v-model="searchName" placeholder="分类名称" style="width: 200px;" clearable></el-input>
      <el-button type="primary" @click="searchCategories">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="categoryList" style="width: 100%" border>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="name" label="分类名称" width="200"></el-table-column>
      <el-table-column prop="description" label="描述"></el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="editCategory(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteCategory(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total">
    </el-pagination>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="categoryForm" :rules="rules" ref="categoryFormRef" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称"></el-input>
        </el-form-item>
        <el-form-item label="父分类" prop="parentId">
          <el-select v-model="categoryForm.parentId" placeholder="选择父分类（可选）">
            <el-option label="顶级分类" :value="null"></el-option>
            <el-option
              v-for="category in topLevelCategories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="categoryForm.description" type="textarea" placeholder="请输入分类描述"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getCategories, 
  getCategoryById, 
  addCategory, 
  updateCategory, 
  deleteCategory as deleteCategoryApi,
  getTopLevelCategories, 
  hasChildrenCategories
} from '../api/category'

// 响应式数据
const categoryList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchName = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('添加分类')
const categoryFormRef = ref(null)
const topLevelCategories = ref([]) // 顶级分类列表

// 分类表单
const categoryForm = reactive({
  id: null,
  name: '',
  description: '',
  parentId: null
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const params = {
      page: currentPage.value - 1, // Spring Data JPA的页码从0开始
      size: pageSize.value,
      name: searchName.value
    }
    
    const response = await getCategories(params)
    // 处理分页数据
    if (response.data && response.data.content) {
      categoryList.value = response.data.content
      total.value = response.data.totalElements
    } else {
      // 兼容非分页数据
      categoryList.value = response.data || []
      total.value = response.data ? response.data.length : 0
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

// 搜索分类
const searchCategories = () => {
  currentPage.value = 1
  fetchCategories()
}

// 重置搜索
const resetSearch = () => {
  searchName.value = ''
  currentPage.value = 1
  fetchCategories()
}

// 显示添加对话框
const showAddDialog = () => {
  dialogTitle.value = '添加分类'
  dialogVisible.value = true
  resetForm()
}

// 编辑分类
const editCategory = async (row) => {
  dialogTitle.value = '编辑分类'
  dialogVisible.value = true
  
  try {
    const response = await getCategoryById(row.id)
    const category = response.data
    
    categoryForm.id = category.id
    categoryForm.name = category.name
    categoryForm.description = category.description
    categoryForm.parentId = category.parent ? category.parent.id : null
  } catch (error) {
    console.error('获取分类详情失败:', error)
    ElMessage.error('获取分类详情失败')
  }
}

// 删除分类
const deleteCategory = async (id) => {
  try {
    // 检查是否有子分类
    const hasChildrenResponse = await hasChildrenCategories(id)
    if (hasChildrenResponse.data) {
      ElMessage.warning('该分类下存在子分类，无法删除')
      return
    }
    
    await ElMessageBox.confirm('确定要删除该分类吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteCategoryApi(id)
    ElMessage.success('删除成功')
    fetchCategories()
    fetchTopLevelCategories() // 更新顶级分类列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除分类失败')
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!categoryFormRef.value) return
  
  try {
    await categoryFormRef.value.validate()
    
    // 构建提交数据
    const submitData = {
      ...categoryForm
    }
    
    // 处理父分类
    if (submitData.parentId === null) {
      delete submitData.parentId
    }
    
    if (categoryForm.id) {
      // 更新分类
      await updateCategory(categoryForm.id, submitData)
      ElMessage.success('更新成功')
    } else {
      // 添加分类
      await addCategory(submitData)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    fetchCategories()
    fetchTopLevelCategories() // 更新顶级分类列表
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  }
}

// 重置表单
const resetForm = () => {
  categoryForm.id = null
  categoryForm.name = ''
  categoryForm.description = ''
  categoryForm.parentId = null
  
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchCategories()
}

// 当前页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchCategories()
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

// 获取顶级分类列表
const fetchTopLevelCategories = async () => {
  try {
    const response = await getTopLevelCategories()
    topLevelCategories.value = response.data
  } catch (error) {
    console.error('获取顶级分类失败:', error)
    ElMessage.error('获取顶级分类失败')
  }
}

// 生命周期钩子
onMounted(() => {
  fetchCategories()
  fetchTopLevelCategories()
})
</script>

<style scoped>
.category-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-area {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.el-pagination {
  margin-top: 20px;
  text-align: right;
}
</style>