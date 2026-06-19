<script setup>
import MyHeader from './subcomponents/MyHeader.vue'
import MyAside from './subcomponents/MyAside.vue'
import HomeCarousel from './subcomponents/HomeCarousel.vue'
</script>

<template>
  <div class="home-container">
    <!-- 头部区域 -->
    <header class="home-header">
      <MyHeader />
    </header>
    
    <!-- 主体区域 -->
    <div class="home-main">
      <!-- 侧边栏 -->
      <aside class="home-aside">
        <MyAside />
      </aside>
      
      <!-- 主内容区域 -->
      <main class="home-content">
        <!-- 轮播图展示 -->
        <HomeCarousel />
        
        <!-- 路由内容区域 -->
        <div class="router-content">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.home-header {
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.home-main {
  flex: 1;
  display: flex;
  min-height: calc(100vh - 60px);
}

.home-aside {
  width: 240px;
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.home-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #f8fafc;
}

.router-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
  min-height: 400px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-main {
    flex-direction: column;
  }
  
  .home-aside {
    width: 100%;
    position: sticky;
    top: 60px;
    z-index: 90;
  }
  
  .home-content {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .home-content {
    padding: 12px;
  }
}

/* 滚动条样式 */
.home-content::-webkit-scrollbar {
  width: 6px;
}

.home-content::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.home-content::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.home-content::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>