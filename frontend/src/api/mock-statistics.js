// 真实统计数据API，从后端获取数据
import { getUsersList } from './users.js'
import { getGoodsList } from './goods.js'
import { getOrdersList } from './orders.js'
import { getCategories } from './category.js'

/**
 * 获取统计数据
 * @returns {Promise} 统计数据
 */
export function getStatistics() {
  return new Promise(async (resolve) => {
    try {
      // 获取用户数据
      const usersRes = await getUsersList()
      const users = usersRes.data || []
      const userStats = {
        total: users.length,
        admin: users.filter(user => user.role === 'ADMIN').length,
        merchant: users.filter(user => user.role === 'MERCHANT').length,
        normal: users.filter(user => user.role === 'USER').length
      }

      // 获取商品数据
      const goodsRes = await getGoodsList({ page: 0, pageSize: 1000 })
      const goods = goodsRes.data?.content || []
      const goodsStats = {
        total: goods.length,
        onSale: goods.filter(item => item.status === 1).length,
        offSale: goods.filter(item => item.status === 0).length
      }

      // 获取订单数据
      const ordersRes = await getOrdersList({ page: 1, pageSize: 1000 })
      const orders = ordersRes.data?.list || []
      const orderStats = {
        total: orders.length,
        pending: orders.filter(order => order.status === 1).length,
        processing: orders.filter(order => order.status === 2).length,
        shipping: orders.filter(order => order.status === 3).length,
        completed: orders.filter(order => order.status === 4).length,
        cancelled: orders.filter(order => order.status === 5).length,
        revenue: orders
          .filter(order => order.status === 4)
          .reduce((sum, order) => sum + parseFloat(order.totalAmount || 0), 0),
        monthRevenue: orders
          .filter(order => {
            try {
              const orderDate = new Date(order.createTime)
              const now = new Date()
              return order.status === 4 && 
                     orderDate.getMonth() === now.getMonth() && 
                     orderDate.getFullYear() === now.getFullYear()
            } catch (e) {
              return false
            }
          })
          .reduce((sum, order) => sum + parseFloat(order.totalAmount || 0), 0)
      }

      // 订单状态数据（用于饼图）
      const orderStatusData = [
        { name: '待付款', value: orderStats.pending, color: '#E6A23C' },
        { name: '待发货', value: orderStats.processing, color: '#409EFF' },
        { name: '待收货', value: orderStats.shipping, color: '#67C23A' },
        { name: '已完成', value: orderStats.completed, color: '#67C23A' },
        { name: '已取消', value: orderStats.cancelled, color: '#F56C6C' }
      ]

      // 用户角色数据（用于饼图）
      const userRoleData = [
        { name: '管理员', value: userStats.admin, color: '#F56C6C' },
        { name: '商家', value: userStats.merchant, color: '#409EFF' },
        { name: '普通用户', value: userStats.normal, color: '#67C23A' }
      ]

      // 最近订单数据
      const recentOrders = orders
        .sort((a, b) => {
          try {
            return new Date(b.createTime) - new Date(a.createTime)
          } catch (e) {
            return 0
          }
        })
        .slice(0, 10)

      resolve({
        userStats,
        goodsStats,
        orderStats,
        orderStatusData,
        userRoleData,
        recentOrders
      })
    } catch (error) {
      console.error('获取统计数据失败:', error)
      resolve({
        userStats: { total: 0, admin: 0, merchant: 0, normal: 0 },
        goodsStats: { total: 0, onSale: 0, offSale: 0 },
        orderStats: { total: 0, pending: 0, processing: 0, shipping: 0, completed: 0, cancelled: 0, revenue: 0, monthRevenue: 0 },
        orderStatusData: [],
        userRoleData: [],
        recentOrders: []
      })
    }
  })
}

/**
 * 获取月度订单数据
 * @returns {Promise} 月度订单数据
 */
export function getMonthlyOrderData() {
  return new Promise(async (resolve) => {
    try {
      const ordersRes = await getOrdersList({ pageSize: 1000 })
      const orders = ordersRes.data?.list || []
      const monthlyData = []
      
      // 生成最近6个月的数据
      const months = ['一月', '二月', '三月', '四月', '五月', '六月']
      const now = new Date()
      
      for (let i = 5; i >= 0; i--) {
        const monthDate = new Date(now.getFullYear(), now.getMonth() - i, 1)
        const monthOrders = orders.filter(order => {
          try {
            const orderDate = new Date(order.createTime)
            return orderDate.getMonth() === monthDate.getMonth() && 
                   orderDate.getFullYear() === monthDate.getFullYear()
          } catch (e) {
            return false
          }
        })
        
        monthlyData.push({
          month: months[i] || `${monthDate.getMonth() + 1}月`,
          count: monthOrders.length,
          revenue: monthOrders
            .filter(order => order.status === 4)
            .reduce((sum, order) => sum + parseFloat(order.totalAmount || 0), 0)
        })
      }
      
      resolve(monthlyData)
    } catch (error) {
      console.error('获取月度订单数据失败:', error)
      resolve([])
    }
  })
}

/**
 * 获取商品类别销售数据
 * @returns {Promise} 商品类别销售数据
 */
export function getCategoryOrderData() {
  return new Promise(async (resolve) => {
    try {
      const ordersRes = await getOrdersList({ pageSize: 1000 })
      const goodsRes = await getGoodsList({ pageSize: 1000 })
      const categoriesRes = await getCategories()
      
      const orders = ordersRes.data?.list || []
      const goods = goodsRes.data?.list || []
      const categories = categoriesRes.data?.list || []
      
      const categoryData = {}
      
      // 初始化所有类别
      categories.forEach(category => {
        categoryData[category.name] = {
          count: 0,
          revenue: 0
        }
      })
      
      // 统计每个类别的销售数量和收入
      orders.forEach(order => {
        if (order.orderItems && Array.isArray(order.orderItems)) {
          order.orderItems.forEach(item => {
            const good = goods.find(g => g.id === parseInt(item.goodsId))
            if (good) {
              const category = categories.find(c => c.id === good.category)
              const categoryName = category ? category.name : '其他'
              
              if (!categoryData[categoryName]) {
                categoryData[categoryName] = {
                  count: 0,
                  revenue: 0
                }
              }
              
              categoryData[categoryName].count += item.quantity || 1
              categoryData[categoryName].revenue += parseFloat(item.price || 0) * (item.quantity || 1)
            }
          })
        }
      })
      
      // 转换为数组格式
      const result = Object.keys(categoryData).map(category => ({
        category,
        count: categoryData[category].count,
        revenue: categoryData[category].revenue
      }))
      
      resolve(result)
    } catch (error) {
      console.error('获取商品类别销售数据失败:', error)
      resolve([])
    }
  })
}