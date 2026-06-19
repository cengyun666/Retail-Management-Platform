<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h1>数据大屏</h1>
      <div class="time">{{ currentTime }}</div>
    </div>
    <!-- 主要内容区域 -->
    
    <div class="dashboard-content">
      <!-- 统计卡片区域 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="user-icon"></div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.userCount }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="goods-icon"></div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.goodsCount }}</div>
            <div class="stat-label">商品总数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="order-icon"></div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.orderCount }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="sales-icon"></div>
          <div class="stat-info">
            <div class="stat-value">¥{{ statistics.totalSales }}</div>
            <div class="stat-label">总销售额</div>
          </div>
        </div>
      </div>
      
      <!-- 图表区域 -->
      <div class="charts-section">
        <!-- 月度订单图表 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>月度订单与销售收入趋势</h3>
          </div>
          <div class="chart-content">
            <e-charts 
              :option="monthlyOrderOption" 
              autoresize 
              style="width: 100%; height: 300px;"
            />
          </div>
        </div>
        
        <!-- 商品类别销售图表 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>商品类别销售分布</h3>
          </div>
          <div class="chart-content">
            <e-charts 
              :option="categoryOrderOption" 
              autoresize 
              style="width: 100%; height: 300px;"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { createComponent } from 'echarts-for-vue'
import * as echarts from 'echarts'
import request from '../utils/request'

// 创建ECharts组件
const ECharts = createComponent({ echarts, h })

// 当前时间
const currentTime = ref('')

// 统计数据
const statistics = ref({
  userCount: 0,
  goodsCount: 0,
  orderCount: 0,
  totalSales: 0
})

// 月度订单数据
const monthlyOrderData = ref([])

// 商品类别销售数据
const categoryOrderData = ref([])

// 更新当前时间
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    console.log('开始获取统计数据')
    const response = await request.get('/statistics/dashboard')
    console.log('API响应:', response)
    if (response && response.data) {
      console.log('响应数据:', response.data)
      // response.data包含了statistics、monthlyOrderTrend和categoryOrderData
      statistics.value = response.data.statistics || statistics.value
      monthlyOrderData.value = response.data.monthlyOrderTrend || []
      categoryOrderData.value = response.data.categoryOrderData || []
      
      // 更新图表
      updateMonthlyOrderChart()
      updateCategoryOrderChart()
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
    // 使用默认数据
    statistics.value = {
      userCount: 1000,
      goodsCount: 500,
      orderCount: 2000,
      totalSales: 150000
    }
    monthlyOrderData.value = [
      { month: '1月', orderCount: 120, sales: 12000 },
      { month: '2月', orderCount: 150, sales: 15000 },
      { month: '3月', orderCount: 180, sales: 18000 },
      { month: '4月', orderCount: 200, sales: 20000 },
      { month: '5月', orderCount: 220, sales: 22000 },
      { month: '6月', orderCount: 250, sales: 25000 }
    ]
    categoryOrderData.value = [
      { category: '食品', sales: 1048 },
      { category: '饮料', sales: 735 },
      { category: '日用品', sales: 580 },
      { category: '文具', sales: 484 },
      { category: '其他', sales: 300 }
    ]
    updateMonthlyOrderChart()
    updateCategoryOrderChart()
  }
}

// 月度订单图表配置
const monthlyOrderOption = ref({})

// 更新月度订单图表
const updateMonthlyOrderChart = () => {
  const months = monthlyOrderData.value.map(item => item.month)
  const orderCounts = monthlyOrderData.value.map(item => item.orderCount)
  const sales = monthlyOrderData.value.map(item => item.sales)
  
  monthlyOrderOption.value = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      data: ['订单数量', '销售收入'],
      top: 'bottom'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        boundaryGap: false,
        data: months
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '订单数量',
        axisLabel: {
          formatter: '{value}'
        }
      },
      {
        type: 'value',
        name: '销售收入',
        axisLabel: {
          formatter: '¥{value}'
        }
      }
    ],
    series: [
      {
        name: '订单数量',
        type: 'line',
        smooth: true,
        data: orderCounts
      },
      {
        name: '销售收入',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: sales
      }
    ]
  }
}

// 商品类别销售图表配置
const categoryOrderOption = ref({})

// 更新商品类别销售图表
const updateCategoryOrderChart = () => {
  const categories = categoryOrderData.value.map(item => item.category)
  const sales = categoryOrderData.value.map(item => item.sales)
  
  categoryOrderOption.value = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: categories
    },
    series: [
      {
        name: '商品类别销售',
        type: 'pie',
        radius: '50%',
        center: ['60%', '50%'],
        data: categoryOrderData.value.map(item => ({
          name: item.category,
          value: item.sales
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
}

onMounted(() => {
  // 更新时间
  updateTime()
  setInterval(updateTime, 1000)
  
  // 获取数据
  fetchStatistics()
  
  // 添加定时刷新，每5秒更新一次数据
  setInterval(fetchStatistics, 2000)
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
}

.dashboard-header h1 {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.time {
  font-size: 16px;
  color: #606266;
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #409eff;
}

.goods-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #67c23a;
}

.order-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #e6a23c;
}

.sales-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f56c6c;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
}

.chart-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 16px;
  color: #303133;
  margin: 0;
}

.chart-content {
  height: 300px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .charts-section {
    grid-template-columns: 1fr;
  }
  
  .chart-card {
    padding: 15px;
  }
  
  .chart-content {
    height: 250px;
  }
}
</style>