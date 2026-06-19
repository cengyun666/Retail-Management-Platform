<template>
  <div class="image-uploader">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="上传图片" name="upload">
        <el-upload
          class="image-uploader-component"
          action=""
          :show-file-list="false"
          :before-upload="beforeUpload"
          :http-request="uploadImage"
        >
          <img v-if="imageUrl" :src="imageUrl" class="uploaded-image" />
          <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div v-if="imageUrl" class="image-actions">
          <el-button size="small" type="primary" @click="changeImage">更换图片</el-button>
          <el-button size="small" type="danger" @click="removeImage">删除图片</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="选择预设图片" name="preset">
        <div class="preset-images-container">
          <el-select v-model="selectedCategory" placeholder="选择分类" clearable @change="filterImages" style="margin-bottom: 15px; width: 100%;">
            <el-option label="全部分类" value="" />
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
          
          <div class="preset-images-grid">
            <div
              v-for="image in filteredImages"
              :key="image.id"
              class="preset-image-item"
              :class="{ 'selected': isSelected(image.url) }"
              @click="selectPresetImage(image)"
            >
              <el-image
                :src="image.thumbnail"
                :alt="image.name"
                fit="cover"
                class="preset-image-thumbnail"
              />
              <div class="preset-image-info">
                <div class="preset-image-name">{{ image.name }}</div>
                <div class="preset-image-category">{{ image.category }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { presetImages, getPresetImagesByCategory, getPresetImageCategories } from '@/assets/preset-images.js'

export default {
  name: 'ImageUploader',
  components: {
    Plus
  },
  props: {
    modelValue: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const imageUrl = ref(props.modelValue)
    const activeTab = ref('upload')
    const selectedCategory = ref('')
    const categories = ref([])
    const filteredImages = ref([])
    
    // 初始化分类和图片
    onMounted(() => {
      categories.value = getPresetImageCategories()
      filteredImages.value = presetImages
    })
    
    // 根据分类筛选图片
    const filterImages = () => {
      filteredImages.value = getPresetImagesByCategory(selectedCategory.value)
    }
    
    // 检查图片是否被选中
    const isSelected = (url) => {
      return imageUrl.value === url
    }
    
    // 选择预设图片
    const selectPresetImage = (image) => {
      imageUrl.value = image.url
      emit('update:modelValue', image.url)
      ElMessage.success(`已选择图片: ${image.name}`)
    }
    
    // 上传前校验
    const beforeUpload = (file) => {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG) {
        ElMessage.error('上传图片只能是 JPG/PNG/GIF 格式!')
        return false
      }
      if (!isLt2M) {
        ElMessage.error('上传图片大小不能超过 2MB!')
        return false
      }
      return true
    }
    
    // 自定义上传方法
    const uploadImage = (options) => {
      const { file } = options
      
      // 创建FileReader对象
      const reader = new FileReader()
      
      reader.onload = (e) => {
        // 将图片转换为base64
        const base64 = e.target.result
        imageUrl.value = base64
        emit('update:modelValue', base64)
        ElMessage.success('图片上传成功')
      }
      
      reader.onerror = () => {
        ElMessage.error('图片读取失败')
      }
      
      // 读取文件
      reader.readAsDataURL(file)
    }
    
    // 更换图片
    const changeImage = () => {
      // 触发文件选择
      document.querySelector('.image-uploader-component input[type="file"]').click()
    }
    
    // 删除图片
    const removeImage = () => {
      imageUrl.value = ''
      emit('update:modelValue', '')
      ElMessage.success('图片已删除')
    }
    
    return {
      imageUrl,
      activeTab,
      selectedCategory,
      categories,
      filteredImages,
      filterImages,
      isSelected,
      selectPresetImage,
      beforeUpload,
      uploadImage,
      changeImage,
      removeImage,
      Plus
    }
  }
}
</script>

<style scoped>
.image-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.image-uploader-component {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.image-uploader-component:hover {
  border-color: var(--el-color-primary);
}

.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.uploaded-image {
  width: 178px;
  height: 178px;
  object-fit: cover;
  display: block;
}

.image-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

/* 预设图片相关样式 */
.preset-images-container {
  width: 100%;
}

.preset-images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.preset-image-item {
  position: relative;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.preset-image-item:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.preset-image-item.selected {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.preset-image-thumbnail {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
}

.preset-image-info {
  padding: 8px;
  background: #fff;
}

.preset-image-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.preset-image-category {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
</style>