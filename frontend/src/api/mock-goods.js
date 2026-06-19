// 模拟商品数据
const mockGoods = [
  {
    id: 1,
    name: '苹果手机',
    category: 1,
    price: 5999,
    stock: 100,
    description: '最新款苹果手机，性能卓越',
    image: 'https://example.com/iphone.jpg',
    status: 1,
    createTime: '2023-01-01 10:00:00'
  },
  {
    id: 2,
    name: '华为笔记本',
    category: 2,
    price: 6999,
    stock: 50,
    description: '高性能笔记本，办公首选',
    image: 'https://example.com/laptop.jpg',
    status: 1,
    createTime: '2023-01-02 10:00:00'
  },
  {
    id: 3,
    name: '小米电视',
    category: 3,
    price: 2999,
    stock: 30,
    description: '4K高清智能电视',
    image: 'https://example.com/tv.jpg',
    status: 0,
    createTime: '2023-01-03 10:00:00'
  },
  {
    id: 4,
    name: '华为手机',
    category: 1,
    price: 4999,
    stock: 80,
    description: '华为旗舰手机，拍照出色',
    image: 'https://example.com/huawei-phone.jpg',
    status: 1,
    createTime: '2023-01-04 10:00:00'
  },
  {
    id: 5,
    name: '联想笔记本',
    category: 2,
    price: 5999,
    stock: 40,
    description: '性价比高，适合学生使用',
    image: 'https://example.com/lenovo-laptop.jpg',
    status: 1,
    createTime: '2023-01-05 10:00:00'
  },
  {
    id: 6,
    name: '索尼电视',
    category: 3,
    price: 7999,
    stock: 20,
    description: '高端画质，观影体验佳',
    image: 'https://example.com/sony-tv.jpg',
    status: 1,
    createTime: '2023-01-06 10:00:00'
  },
  {
    id: 7,
    name: '小米手机',
    category: 1,
    price: 3999,
    stock: 120,
    description: '性价比高，功能齐全',
    image: 'https://example.com/xiaomi-phone.jpg',
    status: 1,
    createTime: '2023-01-07 10:00:00'
  },
  {
    id: 8,
    name: '戴尔笔记本',
    category: 2,
    price: 6499,
    stock: 35,
    description: '商务办公首选，稳定可靠',
    image: 'https://example.com/dell-laptop.jpg',
    status: 1,
    createTime: '2023-01-08 10:00:00'
  },
  {
    id: 9,
    name: '三星电视',
    category: 3,
    price: 5999,
    stock: 25,
    description: '曲面屏设计，视觉效果震撼',
    image: 'https://example.com/samsung-tv.jpg',
    status: 0,
    createTime: '2023-01-09 10:00:00'
  },
  {
    id: 10,
    name: 'OPPO手机',
    category: 1,
    price: 3499,
    stock: 90,
    description: '拍照功能强大，外观精美',
    image: 'https://example.com/oppo-phone.jpg',
    status: 1,
    createTime: '2023-01-10 10:00:00'
  },
  {
    id: 11,
    name: 'HP笔记本',
    category: 2,
    price: 5299,
    stock: 45,
    description: '轻薄便携，续航持久',
    image: 'https://example.com/hp-laptop.jpg',
    status: 1,
    createTime: '2023-01-11 10:00:00'
  },
  {
    id: 12,
    name: 'TCL电视',
    category: 3,
    price: 3299,
    stock: 50,
    description: '经济实惠，画质清晰',
    image: 'https://example.com/tcl-tv.jpg',
    status: 1,
    createTime: '2023-01-12 10:00:00'
  },
  {
    id: 13,
    name: 'vivo手机',
    category: 1,
    price: 3699,
    stock: 75,
    description: '游戏性能强劲，充电速度快',
    image: 'https://example.com/vivo-phone.jpg',
    status: 1,
    createTime: '2023-01-13 10:00:00'
  },
  {
    id: 14,
    name: '华硕笔记本',
    category: 2,
    price: 5799,
    stock: 30,
    description: '游戏本配置，散热良好',
    image: 'https://example.com/asus-laptop.jpg',
    status: 1,
    createTime: '2023-01-14 10:00:00'
  },
  {
    id: 15,
    name: '创维电视',
    category: 3,
    price: 3799,
    stock: 40,
    description: '智能系统，内容丰富',
    image: 'https://example.com/skyworth-tv.jpg',
    status: 1,
    createTime: '2023-01-15 10:00:00'
  }
]

// 模拟分类数据
const mockCategories = [
  { id: 1, name: '手机数码' },
  { id: 2, name: '电脑办公' },
  { id: 3, name: '家用电器' }
]

// 从localStorage加载商品数据
const loadGoodsFromStorage = () => {
  try {
    const storedGoods = localStorage.getItem('mockGoods')
    if (storedGoods) {
      return JSON.parse(storedGoods)
    }
  } catch (error) {
    console.error('加载商品数据失败:', error)
  }
  return mockGoods
}

// 保存商品数据到localStorage
const saveGoodsToStorage = (goods) => {
  try {
    localStorage.setItem('mockGoods', JSON.stringify(goods))
  } catch (error) {
    console.error('保存商品数据失败:', error)
  }
}

// 模拟API延迟
const delay = (ms = 300) => new Promise(resolve => setTimeout(resolve, ms))

// 获取商品列表
export const getGoodsList = async (params) => {
  await delay()
  
  // 从localStorage加载商品数据
  let goods = loadGoodsFromStorage()
  
  // 模拟筛选逻辑
  let filteredGoods = [...goods]
  
  if (params.name) {
    filteredGoods = filteredGoods.filter(item => 
      item.name.toLowerCase().includes(params.name.toLowerCase())
    )
  }
  
  if (params.category) {
    filteredGoods = filteredGoods.filter(item => item.category === params.category)
  }
  
  if (params.status !== undefined && params.status !== '') {
    filteredGoods = filteredGoods.filter(item => item.status === params.status)
  }
  
  // 模拟分页逻辑
  const pageSize = params.pageSize || 10
  const currentPage = params.page || 1
  const total = filteredGoods.length
  const start = (currentPage - 1) * pageSize
  const end = start + pageSize
  const list = filteredGoods.slice(start, end)
  
  return {
    code: 200,
    data: {
      list,
      total,
      pageSize,
      currentPage
    },
    message: '获取商品列表成功'
  }
}

// 根据ID获取商品详情
export const getGoodsById = async (id) => {
  await delay()
  console.log('查找商品ID:', id, '类型:', typeof id)
  
  // 从localStorage加载商品数据
  const goods = loadGoodsFromStorage()
  
  // 确保id是数字类型进行比较
  const goodsId = parseInt(id)
  const item = goods.find(item => item.id === goodsId)
  
  if (!item) {
    return {
      code: 404,
      message: '商品不存在'
    }
  }
  
  return {
    code: 200,
    data: item,
    message: '获取商品详情成功'
  }
}

// 添加商品
export const addGoods = async (data) => {
  await delay()
  
  // 从localStorage加载商品数据
  const goods = loadGoodsFromStorage()
  
  // 获取当前最大ID值，确保ID唯一性
  const maxId = goods.length > 0 ? Math.max(...goods.map(item => item.id)) : 0
  
  const newGoods = {
    ...data,
    id: maxId + 1,
    createTime: new Date().toLocaleString('zh-CN')
  }
  
  goods.push(newGoods)
  
  // 保存更新后的商品数据
  saveGoodsToStorage(goods)
  
  return {
    code: 200,
    data: newGoods,
    message: '添加商品成功'
  }
}

// 更新商品信息
export const updateGoods = async (id, data) => {
  await delay()
  
  // 从localStorage加载商品数据
  const goods = loadGoodsFromStorage()
  
  const index = goods.findIndex(item => item.id === parseInt(id))
  
  if (index === -1) {
    return {
      code: 404,
      message: '商品不存在'
    }
  }
  
  goods[index] = {
    ...goods[index],
    ...data
  }
  
  // 保存更新后的商品数据
  saveGoodsToStorage(goods)
  
  return {
    code: 200,
    data: goods[index],
    message: '更新商品成功'
  }
}

// 删除商品
export const deleteGoods = async (id) => {
  await delay()
  
  // 从localStorage加载商品数据
  const goods = loadGoodsFromStorage()
  
  const index = goods.findIndex(item => item.id === parseInt(id))
  
  if (index === -1) {
    return {
      code: 404,
      message: '商品不存在'
    }
  }
  
  goods.splice(index, 1)
  
  // 保存更新后的商品数据
  saveGoodsToStorage(goods)
  
  return {
    code: 200,
    message: '删除商品成功'
  }
}

// 更新商品状态
export const updateGoodsStatus = async (id, status) => {
  await delay()
  
  // 从localStorage加载商品数据
  const goods = loadGoodsFromStorage()
  
  const index = goods.findIndex(item => item.id === parseInt(id))
  
  if (index === -1) {
    return {
      code: 404,
      message: '商品不存在'
    }
  }
  
  goods[index].status = status
  
  // 保存更新后的商品数据
  saveGoodsToStorage(goods)
  
  return {
    code: 200,
    data: goods[index],
    message: '更新商品状态成功'
  }
}

// 获取商品分类列表
export const getCategories = async () => {
  await delay()
  
  return {
    code: 200,
    data: mockCategories,
    message: '获取分类列表成功'
  }
}