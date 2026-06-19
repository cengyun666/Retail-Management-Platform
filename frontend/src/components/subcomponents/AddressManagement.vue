<template>
  <div class="address-container">
    <div class="address-header-section">
      <h2>收货地址管理</h2>
      <p class="address-subtitle">管理您的收货地址信息</p>
    </div>
    
    <el-card class="address-card" v-loading="loading" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-button type="primary" size="default" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            添加新地址
          </el-button>
        </div>
      </template>
      
      <!-- 地址列表 -->
      <el-table 
        :data="addressList" 
        style="width: 100%"
        stripe
        border
        :header-cell-style="{backgroundColor: '#fafafa', fontWeight: 'bold'}"
        :row-style="{transition: 'all 0.3s'}"
        :row-class-name="(row) => row.isDefault ? 'default-address-row' : ''"
      >
        <el-table-column prop="name" label="收货人" width="120" align="center" />
        <el-table-column prop="phone" label="手机号" width="150" align="center" />
        <el-table-column prop="province" label="省份" width="100" align="center" />
        <el-table-column prop="city" label="城市" width="100" align="center" />
        <el-table-column prop="district" label="区县" width="100" align="center" />
        <el-table-column prop="detail" label="详细地址" min-width="250" align="left" />
        <el-table-column prop="isDefault" label="是否默认" width="120" align="center">
          <template #default="scope">
            <el-tag type="success" effect="dark" v-if="scope.row.isDefault">默认地址</el-tag>
            <el-tag type="info" effect="plain" v-else>普通地址</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right" align="center">
          <template #default="scope">
            <div style="display: flex; gap: 5px; justify-content: center;">
              <el-button 
                type="primary" 
                size="small" 
                @click="showEditDialog(scope.row)"
                :icon="Edit"
              >
                编辑
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="deleteAddress(scope.row.id)"
                :icon="Delete"
              >
                删除
              </el-button>
              <el-button 
                v-if="!scope.row.isDefault" 
                size="small" 
                @click="setDefaultAddress(scope.row.id)"
                :icon="Star"
                type="warning"
              >
                设为默认
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 添加/编辑地址对话框 -->
      <el-dialog
        :title="dialogTitle"
        v-model="dialogVisible"
        width="550px"
        @close="resetForm"
        center
        :close-on-click-modal="false"
      >
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top" size="default">
          <el-form-item label="收货人" prop="name" label-width="80px">
            <el-input v-model="form.name" placeholder="请输入收货人姓名" clearable />
          </el-form-item>
          <el-form-item label="手机号" prop="phone" label-width="80px">
            <el-input v-model="form.phone" placeholder="请输入手机号" clearable />
          </el-form-item>
          <el-row :gutter="15">
            <el-col :span="8">
              <el-form-item label="省份" prop="province" label-width="80px">
                <el-select v-model="form.province" placeholder="请选择省份" @change="handleProvinceChange">
                  <el-option 
                    v-for="province in provinces" 
                    :key="province.id" 
                    :label="province.name" 
                    :value="province.name" 
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="城市" prop="city" label-width="80px">
                <el-select v-model="form.city" placeholder="请选择城市" @change="handleCityChange">
                  <el-option 
                    v-for="city in cities" 
                    :key="city.id" 
                    :label="city.name" 
                    :value="city.name" 
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="区县" prop="district" label-width="80px">
                <el-select v-model="form.district" placeholder="请选择区县">
                  <el-option 
                    v-for="district in districts" 
                    :key="district.id" 
                    :label="district.name" 
                    :value="district.name" 
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="详细地址" prop="detail" label-width="80px">
            <el-input 
              v-model="form.detail" 
              type="textarea" 
              :rows="4" 
              placeholder="请输入详细地址（如街道、门牌号等）" 
              resize="vertical"
            />
          </el-form-item>
          <el-form-item label-width="80px">
            <el-checkbox v-model="form.isDefault" size="large">设为默认地址</el-checkbox>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false" size="default">取消</el-button>
            <el-button type="primary" @click="saveAddress" size="default">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { Plus, Edit, Delete, Star } from '@element-plus/icons-vue'
import { getUserAddresses, addAddress, updateAddress, deleteAddress as deleteAddressAPI, setDefaultAddress as setDefaultAddressAPI } from '../../api/addresses'
import addressData from '../../utils/addressData.json'

const addressList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('添加地址')
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  id: '',
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

const rules = reactive({
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
})

// 从后端API获取地址列表
const fetchAddresses = async () => {
  try {
    loading.value = true
    const response = await getUserAddresses()
    if (response.code === 200) {
      addressList.value = response.data || []
    } else {
      ElMessage.error(response.message || '获取地址列表失败')
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
    ElMessage.error('获取地址列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchAddresses()
})



// 地址选择相关数据
const provinces = ref(addressData.provinces)
const cities = ref([])
const districts = ref([])

// 处理省份变化
const handleProvinceChange = (provinceName) => {
  const province = provinces.value.find(p => p.name === provinceName)
  if (province) {
    cities.value = province.cities
    districts.value = []
    form.city = ''
    form.district = ''
  }
}

// 处理城市变化
const handleCityChange = (cityName) => {
  const province = provinces.value.find(p => p.name === form.province)
  if (province) {
    const city = province.cities.find(c => c.name === cityName)
    if (city) {
      districts.value = city.districts
      form.district = ''
    }
  }
}

// 显示编辑对话框
const showEditDialog = (row) => {
  dialogTitle.value = '编辑地址'
  Object.assign(form, row)
  
  // 初始化下拉框数据
  if (row.province) {
    const province = provinces.value.find(p => p.name === row.province)
    if (province) {
      cities.value = province.cities
      if (row.city) {
        const city = province.cities.find(c => c.name === row.city)
        if (city) {
          districts.value = city.districts
        }
      }
    }
  }
  
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.id = ''
  // 从地址列表中获取默认地址信息，自动填充到新地址表单
  const defaultAddress = addressList.value.find(addr => addr.isDefault)
  if (defaultAddress) {
    form.name = defaultAddress.name
    form.phone = defaultAddress.phone
    form.province = defaultAddress.province
    form.city = defaultAddress.city
    form.district = defaultAddress.district
    
    // 初始化下拉框数据
    const province = provinces.value.find(p => p.name === defaultAddress.province)
    if (province) {
      cities.value = province.cities
      if (defaultAddress.city) {
        const city = province.cities.find(c => c.name === defaultAddress.city)
        if (city) {
          districts.value = city.districts
        }
      }
    }
  } else {
    form.name = ''
    form.phone = ''
    form.province = ''
    form.city = ''
    form.district = ''
    cities.value = []
    districts.value = []
  }
  form.detail = ''
  form.isDefault = false
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 保存地址
const saveAddress = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    let response
    if (form.id) {
      // 编辑现有地址
      response = await updateAddress(form.id, form)
    } else {
      // 添加新地址
      response = await addAddress(form)
    }
    
    if (response.code === 200) {
      ElMessage.success(form.id ? '地址编辑成功' : '地址添加成功')
      // 重新获取地址列表
      await fetchAddresses()
      dialogVisible.value = false
      resetForm()
    } else {
      ElMessage.error(response.message || '保存地址失败')
    }
  } catch (error) {
    console.error('保存地址失败:', error)
    ElMessage.error('保存地址失败')
  }
}

// 删除地址
const deleteAddress = async (id) => {
  try {
    const response = await deleteAddressAPI(id)
    if (response.code === 200) {
      ElMessage.success('地址删除成功')
      // 重新获取地址列表
      await fetchAddresses()
    } else {
      ElMessage.error(response.message || '删除地址失败')
    }
  } catch (error) {
    console.error('删除地址失败:', error)
    ElMessage.error('删除地址失败')
  }
}

// 设置默认地址
const setDefaultAddress = async (id) => {
  try {
    const response = await setDefaultAddressAPI(id)
    if (response.code === 200) {
      ElMessage.success('默认地址设置成功')
      // 重新获取地址列表
      await fetchAddresses()
    } else {
      ElMessage.error(response.message || '设置默认地址失败')
    }
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置默认地址失败')
  }
}
</script>

<style scoped>
.address-container {
  padding: 30px 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.address-header-section {
  max-width: 1200px;
  margin: 0 auto 20px;
  text-align: center;
}

.address-header-section h2 {
  color: #303133;
  margin-bottom: 8px;
  font-size: 28px;
  font-weight: 600;
}

.address-subtitle {
  color: #606266;
  font-size: 14px;
  margin: 0;
}

.address-card {
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 16px 20px;
  background-color: #fff;
}

.default-address-row {
  background-color: #f0f9ff !important;
  border-left: 4px solid #409eff;
}

.default-address-row:hover {
  background-color: #e6f7ff !important;
}

/* 表格行悬停效果 */
.el-table__row:hover {
  background-color: #fafafa !important;
}

/* 操作按钮样式优化 */
:deep(.el-button--small) {
  margin-right: 4px;
  border-radius: 4px;
  transition: all 0.3s;
}

:deep(.el-button--small:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .address-container {
    padding: 15px 10px;
  }
  
  .address-card {
    margin: 0;
    border-radius: 0;
  }
  
  .el-table {
    font-size: 12px;
  }
  
  .el-table-column {
    padding: 0 5px;
  }
}
</style>