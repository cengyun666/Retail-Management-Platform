// 模拟用户数据
const mockUsers = [
    { 
        id: '1', 
        username: 'admin', 
        password: '123456',
        name: '管理员', 
        email: 'admin@example.com',
        phone: '13800138000',
        address: '北京市朝阳区',
        role: 'admin', // 管理员角色
        status: 1, // 1-正常 0-禁用
        createTime: '2023-01-01 10:00:00',
        isDeleted: false
    },
    { 
        id: '2', 
        username: 'merchant001', 
        password: '123456',
        name: '商家小明', 
        email: 'merchant@example.com',
        phone: '13800138001',
        address: '上海市浦东新区',
        role: 'merchant', // 商家角色
        status: 1,
        createTime: '2023-01-02 10:00:00',
        isDeleted: false
    },
    { 
        id: '3', 
        username: 'user001', 
        password: '123456',
        name: '普通用户小红', 
        email: 'user@example.com',
        phone: '13800138002',
        address: '广州市天河区',
        role: 'user', // 普通用户角色
        status: 1,
        createTime: '2023-01-03 10:00:00',
        isDeleted: false
    },
    { 
        id: '4', 
        username: 'user002', 
        password: '123456',
        name: '普通用户小兰', 
        email: 'xiaolan@example.com',
        phone: '13800138003',
        address: '深圳市南山区',
        role: 'user', // 普通用户角色
        status: 1,
        createTime: '2023-01-04 10:00:00',
        isDeleted: false
    }
]

// 模拟API延迟
const delay = (ms = 300) => new Promise(resolve => setTimeout(resolve, ms))

// 获取用户列表
export async function getUsersList(params = {}) {
    await delay()
    
    let result = mockUsers.filter(user => !user.isDeleted)
    
    // 搜索过滤
    if (params.search) {
        const query = params.search.toLowerCase()
        result = result.filter(user => 
            user.name.toLowerCase().includes(query) ||
            user.username.toLowerCase().includes(query) ||
            user.email.toLowerCase().includes(query)
        )
    }
    
    // 分页处理
    const page = parseInt(params.page) || 1
    const pageSize = parseInt(params.pageSize) || 10
    const total = result.length
    const start = (page - 1) * pageSize
    const end = start + pageSize
    const list = result.slice(start, end)
    
    return {
        code: 200,
        message: '获取用户列表成功',
        data: {
            list,
            total,
            page,
            pageSize
        }
    }
}

// 根据ID获取用户详情
export async function getUserById(id) {
    await delay()
    
    const user = mockUsers.find(item => item.id === id && !item.isDeleted)
    
    if (!user) {
        return {
            code: 404,
            message: '用户不存在',
            data: null
        }
    }
    
    // 不返回密码
    const { password, ...userInfo } = user
    
    return {
        code: 200,
        message: '获取用户详情成功',
        data: userInfo
    }
}

// 根据用户名获取用户信息
export async function getUserByUsername(username) {
    await delay()
    
    const user = mockUsers.find(item => item.username === username && !item.isDeleted)
    
    if (!user) {
        return {
            code: 404,
            message: '用户不存在',
            data: null
        }
    }
    
    return {
        code: 200,
        message: '获取用户信息成功',
        data: user
    }
}

// 用户注册
export async function registerUser(userData) {
    await delay()
    
    // 检查用户名是否已存在
    const existingUser = mockUsers.find(user => user.username === userData.username && !user.isDeleted)
    if (existingUser) {
        return {
            code: 400,
            message: '用户名已存在',
            data: null
        }
    }
    
    // 检查邮箱是否已存在
    const existingEmail = mockUsers.find(user => user.email === userData.email && !user.isDeleted)
    if (existingEmail) {
        return {
            code: 400,
            message: '邮箱已被使用',
            data: null
        }
    }
    
    // 创建新用户
    const newUser = {
        id: String(Math.max(...mockUsers.map(u => parseInt(u.id)), 0) + 1),
        username: userData.username,
        password: userData.password, // 实际应用中应该加密
        name: userData.name,
        email: userData.email,
        phone: userData.phone || '',
        address: userData.address || '',
        role: userData.role || 'user', // 默认为普通用户
        status: 1, // 默认为正常状态
        createTime: new Date().toLocaleString('zh-CN'),
        isDeleted: false
    }
    
    mockUsers.push(newUser)
    
    // 不返回密码
    const { password, ...userInfo } = newUser
    
    return {
        code: 200,
        message: '注册成功',
        data: userInfo
    }
}

// 更新用户信息（仅允许修改账号封禁状态）
export async function updateUser(id, userData) {
    await delay()
    
    const userIndex = mockUsers.findIndex(user => user.id === id && !user.isDeleted)
    
    if (userIndex === -1) {
        return {
            code: 404,
            message: '用户不存在',
            data: null
        }
    }
    
    // 只允许修改status字段（账号封禁状态）
    const allowedFields = ['status']
    const updates = {}
    
    for (const field of allowedFields) {
        if (userData.hasOwnProperty(field)) {
            updates[field] = userData[field]
        }
    }
    
    // 如果没有提供允许修改的字段，返回错误
    if (Object.keys(updates).length === 0) {
        return {
            code: 400,
            message: '只能修改账号封禁状态',
            data: null
        }
    }
    
    // 更新用户信息
    Object.assign(mockUsers[userIndex], updates)
    
    // 不返回密码
    const { password, ...userInfo } = mockUsers[userIndex]
    
    return {
        code: 200,
        message: '更新用户信息成功',
        data: userInfo
    }
}

// 删除用户（逻辑删除）
export async function deleteUser(id) {
    await delay()
    
    const userIndex = mockUsers.findIndex(user => user.id === id)
    
    if (userIndex === -1) {
        return {
            code: 404,
            message: '用户不存在',
            data: null
        }
    }
    
    // 逻辑删除：设置isDeleted为true，而不是从数组中移除
    mockUsers[userIndex].isDeleted = true
    
    return {
        code: 200,
        message: '删除用户成功',
        data: null
    }
}

// 获取角色列表
export async function getRolesList() {
    await delay()
    
    const roles = [
        { 
            id: 'admin', 
            name: '管理员', 
            description: '系统管理员，拥有所有权限',
            permissions: [
                'user:read', 'user:create', 'user:update', 'user:delete',
                'goods:read', 'goods:create', 'goods:update', 'goods:delete', 'goods:publish',
                'order:read', 'order:create', 'order:update', 'order:delete', 'order:process',
                'system:settings', 'system:logs', 'role:manage'
            ]
        },
        { 
            id: 'merchant', 
            name: '商家', 
            description: '商家，可以管理商品和处理订单',
            permissions: [
                'goods:read', 'goods:create', 'goods:update', 'goods:publish',
                'order:read', 'order:process'
            ]
        },
        { 
            id: 'user', 
            name: '普通用户', 
            description: '普通用户，可以浏览商品和下单',
            permissions: [
                'goods:read', 'order:create', 'order:read'
            ]
        }
    ]
    
    return {
        code: 200,
        message: '获取角色列表成功',
        data: roles
    }
}

// 更新用户角色
export async function updateUserRole(userId, roleId) {
    await delay()
    
    const userIndex = mockUsers.findIndex(user => user.id === userId)
    
    if (userIndex === -1) {
        return {
            code: 404,
            message: '用户不存在',
            data: null
        }
    }
    
    // 更新用户角色
    mockUsers[userIndex].role = roleId
    
    // 不返回密码
    const { password, ...userInfo } = mockUsers[userIndex]
    
    return {
        code: 200,
        message: '更新用户角色成功',
        data: userInfo
    }
}

// 更新用户信息
export async function updateUserInfo(userId, userData) {
    await delay()
    
    // 查找用户
    const userIndex = mockUsers.findIndex(user => user.id === String(userId) && !user.isDeleted)
    
    if (userIndex === -1) {
        return {
            code: 404,
            message: '用户不存在',
            data: null
        }
    }
    
    // 允许修改的字段
    const allowedFields = ['name', 'email', 'phone', 'address']
    const updates = {}
    
    for (const field of allowedFields) {
        if (userData.hasOwnProperty(field)) {
            updates[field] = userData[field]
        }
    }
    
    // 更新用户信息
    Object.assign(mockUsers[userIndex], updates)
    
    // 不返回密码
    const { password, ...userInfo } = mockUsers[userIndex]
    
    // 更新localStorage中的用户信息
    const updatedUserInfo = {
        ...currentUser,
        ...userInfo
    }
    localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))
    
    return {
        code: 200,
        message: '更新用户信息成功',
        data: userInfo
    }
}
export async function loginUser(username, password) {
    await delay()
    
    const user = mockUsers.find(u => u.username === username && u.password === password && !u.isDeleted)
    
    if (!user) {
        return {
            code: 401,
            message: '用户名或密码错误',
            data: null
        }
    }
    
    // 检查用户状态
    if (user.status === 0) {
        return {
            code: 403,
            message: '账号已被禁用，请联系管理员',
            data: null
        }
    }
    
    // 不返回密码
    const { password: _, ...userInfo } = user
    
    return {
        code: 200,
        message: '登录成功',
        data: {
            userInfo
        }
    }
}