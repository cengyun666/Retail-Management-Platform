<template>
  <div class="home-carousel">
    <el-carousel :interval="4000" type="card" height="400px" :autoplay="true">
      <el-carousel-item v-for="(item, index) in activeCarouselList" :key="index">
        <div class="carousel-container">
          <img :src="item.imageUrl" alt="轮播图" class="carousel-image">
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'

export default {
  name: 'HomeCarousel',
  setup() {
    // 轮播图列表
    const carouselList = ref([])
    
    // 获取启用的轮播图列表
    const activeCarouselList = computed(() => {
      return carouselList.value
        .filter(item => item.status === 'active')
        .sort((a, b) => a.sort - b.sort)
    })
    
    // 初始化轮播图数据
    const initCarouselData = () => {
      // 从本地存储获取轮播图数据
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
      localStorage.setItem('carouselData', JSON.stringify(carouselList.value))
    }
    
    // 监听本地存储变化
    const handleStorageChange = (event) => {
      if (event.key === 'carouselData') {
        initCarouselData()
      }
    }
    
    // 监听自定义事件
    const handleCarouselUpdate = () => {
      initCarouselData()
    }
    
    onMounted(() => {
      initCarouselData()
      // 监听storage事件
      window.addEventListener('storage', handleStorageChange)
      // 监听自定义事件
      window.addEventListener('carouselDataUpdated', handleCarouselUpdate)
    })
    
    onUnmounted(() => {
      // 移除storage事件监听
      window.removeEventListener('storage', handleStorageChange)
      // 移除自定义事件监听
      window.removeEventListener('carouselDataUpdated', handleCarouselUpdate)
    })
    
    return {
      activeCarouselList
    }
  }
}
</script>

<style scoped>
.home-carousel {
  margin-bottom: 30px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.carousel-container {
  display: block;
  width: 100%;
  height: 100%;
  position: relative;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-carousel {
    height: 300px;
  }
}
</style>