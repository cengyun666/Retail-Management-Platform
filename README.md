# 🏬 商超管理系统 (Retail Management Platform)

一个基于 **Vue 3 + Spring Boot** 的全栈零售管理平台，支持多角色（管理员、商家、普通用户）权限管理，涵盖商品管理、订单处理、用户管理、数据统计等核心业务功能。

---

## ✨ 功能概览

### 🔐 用户认证与权限
- 用户注册 / 登录（BCrypt 密码加密）
- 基于角色的访问控制（**RBAC**），三种角色：
  - **管理员 (Admin)** — 全部权限，可管理用户、商品、订单、分类、轮播图，查看数据大屏
  - **商家 (Merchant)** — 管理自己的商品、分类，处理订单
  - **普通用户 (User)** — 浏览商品、加入购物车、下单购买

### 📦 商品管理
- 商品 CRUD（创建、查看、修改、删除/下架）
- 商品图片上传（支持多图）
- 商品分类管理（多级分类）
- 库存自动管理（库存归零自动下架）

### 🛒 购物与订单
- 购物车功能
- 订单创建、支付、处理、取消
- 收货地址管理
- 订单状态追踪

### 📊 数据大屏 (Dashboard)
- ECharts 可视化图表
- 销售统计、用户统计
- 实时数据概览

### 🎠 运营管理
- 首页轮播图管理
- 商品分类管理
- 用户信息管理

---

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| **前端框架** | Vue 3 (Composition API) |
| **构建工具** | Vite 4 |
| **UI 组件库** | Element Plus |
| **状态管理** | Vue Router 4 |
| **HTTP 客户端** | Axios |
| **图表库** | ECharts 5 |
| **CSS 预处理** | Less |
| **后端框架** | Spring Boot 2.7 |
| **安全框架** | Spring Security |
| **ORM** | Spring Data JPA + JDBC |
| **数据库** | MySQL 8.0 |
| **API 文档** | Swagger 2 |
| **构建工具** | Maven |

---

## 📁 项目结构

```
Retail-Management-Platform/
├── frontend/                    # 前端项目 (Vue 3 + Vite)
│   ├── src/
│   │   ├── api/                 # API 接口封装
│   │   ├── assets/              # 静态资源
│   │   ├── components/          # 公共组件
│   │   │   └── subcomponents/   # 业务子组件
│   │   ├── utils/               # 工具函数 & 权限配置
│   │   ├── views/               # 页面视图
│   │   ├── router.js            # 路由配置
│   │   ├── main.js              # 入口文件
│   │   └── style.css            # 全局样式
│   ├── vite.config.js           # Vite 配置
│   └── package.json
│
├── backend/                     # 后端项目 (Spring Boot)
│   ├── src/main/java/com/example/userauth/
│   │   ├── config/              # 配置类 (Security, CORS, Swagger)
│   │   ├── controller/          # REST 控制器
│   │   ├── dto/                 # 数据传输对象
│   │   ├── entity/              # JPA 实体
│   │   ├── enums/               # 枚举类
│   │   ├── exception/           # 全局异常处理
│   │   ├── interceptor/         # 认证拦截器
│   │   ├── repository/          # 数据访问层
│   │   ├── service/             # 业务逻辑接口
│   │   │   └── impl/            # 业务逻辑实现
│   │   └── util/                # 工具类
│   ├── src/main/resources/
│   │   ├── application.properties  # 应用配置
│   │   ├── schema.sql              # 数据库架构
│   │   └── data.sql                # 初始数据
│   ├── docs/API.md              # API 文档
│   └── pom.xml
│
└── README.md
```

---

## 🚀 快速开始

### 环境要求

- **JDK** 11+
- **Node.js** 16+
- **MySQL** 8.0+
- **Maven** 3.6+

### 1. 克隆项目

```bash
git clone https://github.com/cengyun666/Retail-Management-Platform.git
cd Retail-Management-Platform
```

### 2. 配置数据库

1. 创建 MySQL 数据库：
```sql
CREATE DATABASE user_auth_db DEFAULT CHARACTER SET utf8mb4;
```

2. 修改 `backend/src/main/resources/application.properties` 中的数据库连接信息：
```properties
spring.datasource.username=你的数据库用户名
spring.datasource.password=你的数据库密码
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动后访问：
- API 服务：`http://localhost:8080/api`
- Swagger 文档：`http://localhost:8080/swagger-ui.html`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问：`http://localhost:5173`

### 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | `admin` | `123456` | 拥有全部权限 |
| 商家 | `merchant001` | `123456` | 可管理商品和订单 |
| 普通用户 | `user001` | `123456` | 可浏览和购买商品 |

---

## 🔌 API 概览

| 模块 | 端点 | 方法 | 说明 |
|------|------|------|------|
| 用户 | `/api/users/register` | POST | 用户注册 |
| 用户 | `/api/users/login` | POST | 用户登录 |
| 用户 | `/api/users/{username}` | GET | 获取用户信息 |
| 商品 | `/api/goods` | GET/POST | 商品列表 / 新增 |
| 商品 | `/api/goods/{id}` | GET/PUT/DELETE | 商品详情 / 修改 / 删除 |
| 分类 | `/api/categories` | GET/POST | 分类列表 / 新增 |
| 订单 | `/api/orders` | GET/POST | 订单列表 / 创建 |
| 订单 | `/api/orders/{id}` | GET/PUT | 订单详情 / 更新 |
| 支付 | `/api/payments` | POST | 创建支付 |
| 地址 | `/api/addresses` | GET/POST | 地址列表 / 新增 |
| 统计 | `/api/statistics` | GET | 统计数据 |

详细 API 文档请参考：[backend/docs/API.md](backend/docs/API.md)

---

## 🎨 功能截图

### 角色与权限体系
```
┌──────────┐    ┌──────────┐    ┌──────────┐
│  管理员   │    │   商家    │    │ 普通用户  │
│  Admin   │    │ Merchant │    │   User   │
├──────────┤    ├──────────┤    ├──────────┤
│ 全部权限  │    │ 商品管理  │    │ 浏览商品  │
│ 用户管理  │    │ 分类管理  │    │ 购物车   │
│ 商品管理  │    │ 订单处理  │    │ 下单购买  │
│ 订单管理  │    │ 库存管理  │    │ 查看订单  │
│ 数据大屏  │    │          │    │ 地址管理  │
│ 轮播图管理 │    │          │    │          │
│ 分类管理  │    │          │    │          │
└──────────┘    └──────────┘    └──────────┘
```

---

## 🏗 架构设计

- **前后端分离**：Vue 3 SPA + Spring Boot RESTful API
- **Token 认证**：基于 Bearer Token 的无状态认证
- **权限控制**：前端路由守卫 + 后端拦截器双重校验
- **全局异常处理**：统一 API 响应格式 `{ code, message, data }`
- **文件上传**：支持商品图片上传至本地存储

---

## 📝 课程设计说明

本项目为 **Web 前端框架技术课程设计**，重点展示：

1. **Vue 3 组件化开发**：合理拆分公共组件与业务组件
2. **Vue Router 权限路由**：基于角色的动态路由与导航守卫
3. **Element Plus 实战**：表单、表格、对话框、上传等组件综合应用
4. **ECharts 数据可视化**：图表配置与动态数据绑定
5. **前后端联调**：Axios 封装、请求拦截、代理配置
6. **Spring Boot 后端**：RESTful API 设计、JPA 数据持久化、Spring Security 安全控制

---

## 📄 License

本项目仅用于课程学习与展示。
