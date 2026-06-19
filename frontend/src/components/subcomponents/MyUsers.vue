<template>
    <div class="users-container">
        <!-- 页面标题 -->
        <h2 class="page-title">用户管理</h2>
        
        <!-- 操作栏 -->
        <div class="toolbar">
            <!-- 搜索框 -->
            <div class="search-box">
                <input 
                    type="text" 
                    v-model="searchQuery" 
                    placeholder="搜索用户姓名..." 
                    class="search-input"
                    @input="handleSearch"
                />
                <button class="search-btn" @click="handleSearch">🔍</button>
            </div>
        </div>
        
        <!-- 统计信息 -->
        <div class="stats-info">
            <span>共 {{ userlist.length }} 位用户</span>
            <span v-if="searchQuery" class="search-result">
                搜索结果：{{ filteredUsers.length }} 位用户
            </span>
        </div>
        
        <!-- 用户列表 -->
        <div class="table-wrapper">
            <table class="users-table">
                <thead>
                    <tr class="table-header">
                        <th>编号</th>
                        <th>姓名</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <template v-if="paginatedUsers.length > 0">
                        <tr 
                            v-for="item in paginatedUsers" 
                            :key="item.id"
                            class="table-row"
                            :class="{ 'row-hover': true }"
                        >
                            <td>{{ item.id }}</td>
                            <td class="user-name">{{ item.name }}</td>
                            <td class="actions">
                                <router-link 
                                    :to="'/home/users/' + item.id"
                                    class="btn btn-link"
                                    @click="showUserDetail(item)"
                                >
                                    详情
                                </router-link>
                                <button 
                                    class="btn btn-danger"
                                    @click="showDeleteConfirm(item)"
                                    :title="`删除用户 ${item.name}`"
                                >
                                    删除
                                </button>
                            </td>
                        </tr>
                    </template>
                    <tr v-else class="empty-row">
                        <td colspan="4" class="empty-message">
                            <div class="empty-content">
                                <div class="empty-icon">📋</div>
                                <p>{{ searchQuery ? '没有找到匹配的用户' : '暂无用户数据' }}</p>
                                <button 
                                    v-if="searchQuery" 
                                    class="btn btn-default" 
                                    @click="clearSearch"
                                >
                                    清除搜索
                                </button>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        
        <!-- 分页 -->
        <div v-if="filteredUsers.length > pageSize" class="pagination">
            <button 
                class="btn btn-default"
                :disabled="currentPage === 1"
                @click="currentPage--"
            >
                上一页
            </button>
            <span class="page-info">
                第 {{ currentPage }} / {{ totalPages }} 页
            </span>
            <button 
                class="btn btn-default"
                :disabled="currentPage === totalPages"
                @click="currentPage++"
            >
                下一页
            </button>
            <select v-model="pageSize" class="page-size-select" @change="resetPage">
                <option :value="5">5条/页</option>
                <option :value="10">10条/页</option>
                <option :value="20">20条/页</option>
            </select>
        </div>
        
        <!-- 删除确认对话框 -->
        <div v-if="showDeleteDialog" class="dialog-overlay" @click.self="cancelDelete">
            <div class="dialog-content animate-fade-in">
                <div class="dialog-header">
                    <h3>确认删除</h3>
                    <button class="close-btn" @click="cancelDelete">×</button>
                </div>
                <div class="dialog-body">
                    <p class="warning-text">
                        确定要删除用户 <strong>{{ deletingUser?.name }}</strong> 吗？
                        <br>
                        <small>此操作不可撤销。</small>
                    </p>
                </div>
                <div class="dialog-footer">
                    <button class="btn btn-default" @click="cancelDelete">取消</button>
                    <button class="btn btn-danger" @click="confirmDelete" :disabled="isDeleting">
                        {{ isDeleting ? '删除中...' : '确认删除' }}
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsersList, deleteUser } from '../../api/users'

const router = useRouter()

// 用户列表数据
const userlist = ref([])
const loading = ref(false)

// 状态管理
const showDeleteDialog = ref(false)
const deletingUser = ref(null)
const searchQuery = ref('')
const formErrors = ref({})
const isDeleting = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 获取用户列表
const fetchUsers = async () => {
    try {
        loading.value = true
        const response = await getUsersList()
        
        if (response.code === 200) {
            userlist.value = response.data.list
        } else {
            ElMessage.error(response.message || '获取用户列表失败')
        }
    } catch (error) {
        console.error('获取用户列表失败:', error)
        ElMessage.error('获取用户列表失败')
    } finally {
        loading.value = false
    }
}

// 计算属性 - 过滤后的用户列表
const filteredUsers = computed(() => {
    if (!searchQuery.value.trim()) {
        return userlist.value
    }
    const query = searchQuery.value.toLowerCase().trim()
    return userlist.value.filter(user => 
        user.name.toLowerCase().includes(query)
    )
})

// 计算属性 - 分页后的用户列表
const paginatedUsers = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredUsers.value.slice(start, end)
})

// 计算属性 - 总页数
const totalPages = computed(() => {
    return Math.ceil(filteredUsers.value.length / pageSize.value)
})

// 搜索处理
const handleSearch = () => {
    resetPage() // 搜索时重置到第一页
}

// 清除搜索
const clearSearch = () => {
    searchQuery.value = ''
    resetPage()
}

// 重置页码
const resetPage = () => {
    currentPage.value = 1
}

// 显示用户详情
const showUserDetail = (user) => {
    router.push(`/home/users/${user.id}`)
}

// 编辑用户
const editUser = (user) => {
    router.push(`/home/users/edit/${user.id}`)
}

// 显示删除确认
const showDeleteConfirm = (user) => {
    deletingUser.value = user
    showDeleteDialog.value = true
}

// 取消删除
const cancelDelete = () => {
    showDeleteDialog.value = false
    deletingUser.value = null
}

// 确认删除
const confirmDelete = async () => {
    if (!deletingUser.value) return
    
    try {
        await ElMessageBox.confirm(
            `确定要删除用户 ${deletingUser.value.name} 吗？`,
            '确认删除',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )
        
        isDeleting.value = true
        const response = await deleteUser(deletingUser.value.id)
        
        if (response.code === 200) {
            ElMessage.success('用户删除成功')
            // 重新获取用户列表
            await fetchUsers()
            // 重置页码（如果删除后当前页没有数据）
            if (paginatedUsers.value.length === 0 && currentPage.value > 1) {
                currentPage.value--
            }
        } else {
            ElMessage.error(response.message || '删除用户失败')
        }
    } catch (error) {
        if (error !== 'cancel') {
            console.error('删除用户失败:', error)
            ElMessage.error('删除用户失败')
        }
    } finally {
        isDeleting.value = false
        cancelDelete()
    }
}

// 组件挂载时获取用户列表
onMounted(() => {
    fetchUsers()
})
</script>

<style scoped>
/* 全局动画 */
@keyframes slideIn {
    from {
        transform: translateX(100%);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}

@keyframes slideOut {
    from {
        transform: translateX(0);
        opacity: 1;
    }
    to {
        transform: translateX(100%);
        opacity: 0;
    }
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: scale(0.95);
    }
    to {
        opacity: 1;
        transform: scale(1);
    }
}

.users-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.page-title {
    margin-bottom: 24px;
    color: #333;
    font-size: 28px;
    font-weight: 600;
}

/* 工具栏 */
.toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    gap: 16px;
}

.search-box {
    display: flex;
    align-items: center;
    flex: 1;
    max-width: 400px;
}

.search-input {
    flex: 1;
    padding: 8px 12px;
    border: 1px solid #d9d9d9;
    border-radius: 4px 0 0 4px;
    font-size: 14px;
    transition: all 0.3s;
}

.search-input:focus {
    outline: none;
    border-color: #40a9ff;
    box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.search-btn {
    padding: 8px 16px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 0 4px 4px 0;
    cursor: pointer;
    font-size: 16px;
    transition: background-color 0.3s;
}

.search-btn:hover {
    background-color: #0056b3;
}

/* 统计信息 */
.stats-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    color: #666;
    font-size: 14px;
}

.search-result {
    color: #007bff;
    font-weight: 500;
}

/* 按钮样式 */
.btn {
    padding: 8px 16px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.3s ease;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
}

.btn:hover:not(:disabled) {
    opacity: 0.9;
    transform: translateY(-1px);
}

.btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    transform: none;
}

.btn-primary {
    background-color: #007bff;
    color: white;
}

.btn-primary:hover:not(:disabled) {
    background-color: #0056b3;
}

.btn-default {
    background-color: #f0f0f0;
    color: #333;
    border: 1px solid #d9d9d9;
}

.btn-default:hover:not(:disabled) {
    background-color: #e6e6e6;
    border-color: #bfbfbf;
}

.btn-danger {
    background-color: #dc3545;
    color: white;
}

.btn-danger:hover:not(:disabled) {
    background-color: #c82333;
}

.btn-link {
    background: none;
    color: #007bff;
    text-decoration: none;
    padding: 4px 8px;
}

.btn-link:hover {
    background-color: rgba(0, 123, 255, 0.1);
    text-decoration: underline;
}

/* 表格样式 */
.table-wrapper {
    overflow-x: auto;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.users-table {
    width: 100%;
    border-collapse: collapse;
    background-color: white;
}

.table-header {
    background-color: #f8f9fa;
    border-bottom: 2px solid #e9ecef;
}

.users-table th {
    padding: 12px 16px;
    text-align: left;
    font-weight: 600;
    color: #333;
    white-space: nowrap;
}

.table-row {
    border-bottom: 1px solid #f0f0f0;
    transition: background-color 0.2s;
}

.table-row:last-child {
    border-bottom: none;
}

.row-hover:hover {
    background-color: #f8f9fa;
}

.users-table td {
    padding: 12px 16px;
    color: #666;
}

.user-name {
    font-weight: 500;
    color: #333;
}

/* 操作列 */
.actions {
    display: flex;
    gap: 8px;
    align-items: center;
}

/* 空状态 */
.empty-row td {
    padding: 60px 20px;
    text-align: center;
}

.empty-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    color: #999;
}

.empty-icon {
    font-size: 48px;
    opacity: 0.5;
}

.empty-content p {
    margin: 0;
    font-size: 16px;
}

/* 分页 */
.pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 16px;
    margin-top: 24px;
    padding: 16px;
    background-color: #f8f9fa;
    border-radius: 6px;
}

.page-info {
    color: #666;
    font-size: 14px;
    min-width: 100px;
    text-align: center;
}

.page-size-select {
    padding: 6px 10px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    font-size: 14px;
    background-color: white;
}

/* 对话框样式 */
.dialog-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.dialog-content {
    background-color: white;
    border-radius: 8px;
    width: 400px;
    max-width: 90vw;
    max-height: 90vh;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.animate-fade-in {
    animation: fadeIn 0.3s ease-out;
}

.dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
}

.dialog-header h3 {
    margin: 0;
    font-size: 18px;
    color: #333;
}

.close-btn {
    background: none;
    border: none;
    font-size: 24px;
    color: #999;
    cursor: pointer;
    padding: 0;
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
    transition: all 0.2s;
}

.close-btn:hover {
    background-color: #f0f0f0;
    color: #333;
}

.dialog-body {
    padding: 20px;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 16px 20px;
    border-top: 1px solid #f0f0f0;
    background-color: #fafafa;
}

/* 表单样式 */
.form-group {
    margin-bottom: 20px;
}

.form-group label {
    display: block;
    margin-bottom: 6px;
    color: #333;
    font-weight: 500;
    font-size: 14px;
}

.form-input,
.form-select {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    font-size: 14px;
    transition: all 0.3s;
    box-sizing: border-box;
}

.form-input:focus,
.form-select:focus {
    outline: none;
    border-color: #40a9ff;
    box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.error-message {
    color: #f5222d;
    font-size: 12px;
    margin-top: 4px;
}

/* 警告文本 */
.warning-text {
    color: #faad14;
    line-height: 1.6;
}

.warning-text small {
    color: #999;
    font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .toolbar {
        flex-direction: column;
        align-items: stretch;
    }
    
    .search-box {
        max-width: none;
    }
    
    .stats-info {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
    }
    
    .pagination {
        flex-wrap: wrap;
    }
    
    .users-table {
        font-size: 14px;
    }
    
    .users-table th,
    .users-table td {
        padding: 8px 12px;
    }
    
    .actions {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
    }
}
</style>