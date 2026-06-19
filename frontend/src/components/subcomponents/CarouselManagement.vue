<template>
  <div class="carousel-management">
    <div class="page-header">
      <h2 class="page-title">轮播图管理</h2>
      <p class="page-description">管理系统首页轮播图，可以添加、编辑、删除和排序轮播图</p>
    </div>

    <div class="carousel-controls">
      <el-button type="primary" @click="showAddDialog">
        <i class="el-icon-plus"></i>
        添加轮播图
      </el-button>
      <el-button @click="refreshCarousel">
        <i class="el-icon-refresh"></i>
        刷新列表
      </el-button>
    </div>

    <div class="carousel-list">
      <el-table :data="carouselList" border style="width: 100%">
        <el-table-column label="排序" width="120" align="center">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.sort"
              :min="1"
              :max="100"
              size="small"
              @change="(value) => updateSort(scope.$index, value)"
              style="width: 100px;"
            />
          </template>
        </el-table-column>
        <el-table-column label="预览图" width="200" align="center">
          <template #default="scope">
            <el-image
              :src="scope.row.imageUrl"
              fit="cover"
              style="width: 160px; height: 80px"
              :preview-src-list="[scope.row.imageUrl]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
              {{ scope.row.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="mini" @click="editCarousel(scope.$index, scope.row)">
                编辑
              </el-button>
              <el-button
                size="mini"
                :type="scope.row.status === 'active' ? 'warning' : 'success'"
                @click="toggleStatus(scope.$index, scope.row)"
              >
                {{ scope.row.status === 'active' ? '禁用' : '启用' }}
              </el-button>
              <el-button
                size="mini"
                type="danger"
                @click="deleteCarousel(scope.$index, scope.row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加/编辑轮播图对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
      @close="resetForm"
    >
      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
        >
        <el-form-item label="图片地址" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="请输入图片地址">
            <template #append>
              <el-button @click="showImageUploader = true">选择图片</el-button>
            </template>
          </el-input>
          <div v-if="form.imageUrl" class="image-preview">
            <el-image
              :src="form.imageUrl"
              fit="cover"
              style="width: 200px; height: 100px"
              :preview-src-list="[form.imageUrl]"
            />
          </div>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="form.sort"
            :min="1"
            :max="100"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveCarousel">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 图片上传对话框 -->
    <el-dialog
      v-model="showImageUploader"
      title="选择图片"
      width="500px"
    >
      <ImageUploader v-model="tempImageUrl" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showImageUploader = false">取消</el-button>
          <el-button type="primary" @click="confirmImage">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUploader from './ImageUploader.vue'

export default {
  name: 'CarouselManagement',
  components: {
    ImageUploader
  },
  setup() {
    // 轮播图列表
    const carouselList = ref([])
    
    // 对话框相关
    const dialogVisible = ref(false)
    const showImageUploader = ref(false)
    const tempImageUrl = ref('')
    const dialogTitle = computed(() => {
      return isEdit.value ? '编辑轮播图' : '添加轮播图'
    })
    
    // 表单引用
    const formRef = ref(null)
    const carouselForm = ref(null)
    
    // 是否为编辑模式
    const isEdit = ref(false)
    const editIndex = ref(-1)
    
    // 表单数据
    const form = reactive({
      id: null,
      imageUrl: '',
      sort: 1,
      status: 'active'
    })
    
    // 表单验证规则
    const rules = {
      imageUrl: [
        { required: true, message: '请输入图片地址', trigger: 'blur' }
      ],
      sort: [
        { required: true, message: '请输入排序值', trigger: 'blur' }
      ]
    }
    
    // 初始化轮播图数据
    const initCarouselData = () => {
      // 从本地存储获取轮播图数据，如果没有则使用默认数据
      const savedData = localStorage.getItem('carouselData')
      if (savedData) {
        try {
          carouselList.value = JSON.parse(savedData)
        } catch (e) {
          console.error('解析轮播图数据失败', e)
          useDefaultData()
        }
      } else {
        useDefaultData()
      }
    }
    
    // 使用默认数据
    const useDefaultData = () => {
      carouselList.value = [
        {
          id: 1,
          imageUrl: 'https://picsum.photos/seed/carousel1/800/400.jpg',
          sort: 1,
          status: 'active'
        },
        {
          id: 2,
          imageUrl: 'https://picsum.photos/seed/carousel2/800/400.jpg',
          sort: 2,
          status: 'active'
        },
        {
          id: 3,
          imageUrl: 'https://picsum.photos/seed/carousel3/800/400.jpg',
          sort: 3,
          status: 'active'
        }
      ]
      saveCarouselData()
    }
    
    // 保存轮播图数据到本地存储
    const saveCarouselData = () => {
      localStorage.setItem('carouselData', JSON.stringify(carouselList.value))
      // 触发自定义事件，通知轮播图组件更新
      window.dispatchEvent(new CustomEvent('carouselDataUpdated'))
    }
    
    // 显示添加对话框
    const showAddDialog = () => {
      isEdit.value = false
      editIndex.value = -1
      resetForm()
      dialogVisible.value = true
    }
    
    // 编辑轮播图
    const editCarousel = (index, row) => {
      isEdit.value = true
      editIndex.value = index
      
      // 填充表单数据
      Object.keys(form).forEach(key => {
        form[key] = row[key]
      })
      
      dialogVisible.value = true
    }
    
    // 保存轮播图
    const saveCarousel = () => {
      if (!formRef.value) {
        return
      }
      
      formRef.value.validate((valid) => {
        if (valid) {
          if (isEdit.value) {
            // 编辑模式
            const index = carouselList.value.findIndex(item => item.id === form.id)
            if (index !== -1) {
              carouselList.value[index] = {
                ...form
              }
            }
            ElMessage.success('轮播图更新成功')
          } else {
            // 添加模式
            const newCarousel = {
              id: Date.now(),
              ...form
            }
            carouselList.value.push(newCarousel)
            ElMessage.success('轮播图添加成功')
          }
          
          // 按排序值重新排序
          carouselList.value.sort((a, b) => a.sort - b.sort)
          
          // 保存数据
          saveCarouselData()
          
          // 关闭对话框
          dialogVisible.value = false
        }
      })
    }
    
    // 删除轮播图
    const deleteCarousel = (index, row) => {
      ElMessageBox.confirm('确定要删除这个轮播图吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        carouselList.value.splice(index, 1)
        saveCarouselData()
        ElMessage.success('删除成功')
      }).catch(() => {
        // 用户取消删除
      })
    }
    
    // 切换状态
    const toggleStatus = (index, row) => {
      row.status = row.status === 'active' ? 'inactive' : 'active'
      saveCarouselData()
      ElMessage.success(`已${row.status === 'active' ? '启用' : '禁用'}轮播图`)
    }
    
    // 更新排序
    const updateSort = (index, newSort) => {
      // 确保newSort是数字类型
      newSort = Number(newSort)
      
      // 更新当前轮播图的排序值
      carouselList.value[index].sort = newSort
      
      // 检查是否有重复的排序值
      const sortValues = carouselList.value.map(item => Number(item.sort))
      const duplicates = sortValues.filter((value, idx) => sortValues.indexOf(value) !== idx)
      
      if (duplicates.length > 0) {
        // 如果有重复值，重新分配唯一排序值
        carouselList.value.sort((a, b) => Number(a.sort) - Number(b.sort))
        
        // 重新分配唯一排序值
        carouselList.value.forEach((item, idx) => {
          item.sort = idx + 1
        })
        
        // 保存数据
        saveCarouselData()
        
        // 显示成功消息
        ElMessage.success('检测到重复排序值，已自动重新分配唯一排序值')
      } else {
        // 如果没有重复值，直接按排序值排序
        carouselList.value.sort((a, b) => Number(a.sort) - Number(b.sort))
        
        // 保存数据
        saveCarouselData()
        
        // 显示成功消息
        ElMessage.success('排序更新成功')
      }
      
      // 强制更新视图
      setTimeout(() => {
        // 触发响应式更新
        carouselList.value = [...carouselList.value]
      }, 100)
    }
    
    // 刷新轮播图列表
    const refreshCarousel = () => {
      initCarouselData()
      ElMessage.success('列表已刷新')
    }
    
    // 重置表单
    const resetForm = () => {
      if (formRef.value) {
        formRef.value.resetFields()
      }
      
      // 重置表单数据
      Object.keys(form).forEach(key => {
        if (key === 'id') {
          form[key] = null
        } else if (key === 'sort') {
          form[key] = 1
        } else if (key === 'status') {
          form[key] = 'active'
        } else {
          form[key] = ''
        }
      })
    }
    
    // 选择图片
    const chooseImage = () => {
      showImageUploader.value = true
      tempImageUrl.value = form.imageUrl
    }
    
    // 确认选择图片
    const confirmImage = () => {
      form.imageUrl = tempImageUrl.value
      showImageUploader.value = false
      ElMessage.success('图片已选择')
    }
    
    onMounted(() => {
      initCarouselData()
    })
    
    return {
      carouselList,
      dialogVisible,
      showImageUploader,
      tempImageUrl,
      dialogTitle,
      formRef,
      carouselForm,
      form,
      rules,
      isEdit,
      editIndex,
      showAddDialog,
      editCarousel,
      saveCarousel,
      deleteCarousel,
      toggleStatus,
      updateSort,
      refreshCarousel,
      resetForm,
      chooseImage,
      confirmImage
    }
  }
}
</script>

<style scoped>
.carousel-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.carousel-controls {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.carousel-list {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.image-preview {
  margin-top: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.action-buttons {
  display: flex;
  gap: 5px;
  justify-content: center;
}
</style>