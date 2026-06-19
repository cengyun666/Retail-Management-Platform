# 用户认证系统 API 文档

## 概述

本文档描述了用户认证系统的RESTful API接口，提供用户注册、登录等功能。系统基于Spring Boot框架开发，使用H2内存数据库存储数据。

## 基础信息

- **基础URL**: `http://localhost:8080/api`
- **内容类型**: `application/json`
- **字符编码**: `UTF-8`

## 通用响应格式

所有API响应都遵循统一的格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

- `code`: 状态码，200表示成功，其他值表示错误
- `message`: 响应消息
- `data`: 响应数据，具体结构根据API而定

## 错误响应格式

当请求失败时，返回以下格式：

```json
{
  "code": 400,
  "message": "错误描述",
  "data": {}
}
```

## API接口

### 1. 用户注册

**接口描述**: 创建新用户账号

**请求信息**:
- **URL**: `/users/register`
- **方法**: `POST`
- **内容类型**: `application/json`

**请求参数**:
```json
{
  "username": "string",      // 用户名，必填，3-20个字符
  "password": "string",      // 密码，必填，6-20个字符
  "name": "string",          // 姓名，必填，2-20个字符
  "email": "string",         // 邮箱，必填，格式需正确
  "phone": "string",         // 手机号，必填，11位数字
  "address": "string"        // 地址，必填，5-100个字符
}
```

**成功响应**:
```json
{
  "code": 201,
  "message": "注册成功",
  "data": {
    "id": "1",
    "username": "newuser",
    "name": "新用户",
    "email": "newuser@example.com",
    "phone": "13900139000",
    "address": "新用户地址",
    "role": "user",
    "status": "enabled",
    "createTime": "2023-01-01 12:00:00"
  }
}
```

**错误响应**:
- 400: 参数校验失败
- 400: 用户名已存在
- 400: 邮箱已被使用

### 2. 用户登录

**接口描述**: 用户身份验证

**请求信息**:
- **URL**: `/users/login`
- **方法**: `POST`
- **内容类型**: `application/json`

**请求参数**:
```json
{
  "username": "string",      // 用户名，必填
  "password": "string"       // 密码，必填
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "Bearer uuid-timestamp",
    "userInfo": {
      "id": "1",
      "username": "testuser",
      "name": "测试用户",
      "email": "test@example.com",
      "phone": "13800138000",
      "address": "测试地址",
      "role": "user",
      "status": "enabled",
      "createTime": "2023-01-01 12:00:00"
    }
  }
}
```

**错误响应**:
- 400: 参数校验失败
- 400: 用户名或密码错误
- 400: 账号已被禁用，请联系管理员

### 3. 获取用户信息

**接口描述**: 根据用户名获取用户信息

**请求信息**:
- **URL**: `/users/{username}`
- **方法**: `GET`

**路径参数**:
- `username`: 用户名

**成功响应**:
```json
{
  "code": 200,
  "message": "获取用户信息成功",
  "data": {
    "id": "1",
    "username": "testuser",
    "name": "测试用户",
    "email": "test@example.com",
    "phone": "13800138000",
    "address": "测试地址",
    "role": "user",
    "status": "enabled",
    "createTime": "2023-01-01 12:00:00"
  }
}
```

**错误响应**:
- 400: 用户不存在

### 4. 根据ID获取用户信息

**接口描述**: 根据用户ID获取用户信息

**请求信息**:
- **URL**: `/users/id/{id}`
- **方法**: `GET`

**路径参数**:
- `id`: 用户ID

**成功响应**:
```json
{
  "code": 200,
  "message": "获取用户信息成功",
  "data": {
    "id": "1",
    "username": "testuser",
    "name": "测试用户",
    "email": "test@example.com",
    "phone": "13800138000",
    "address": "测试地址",
    "role": "user",
    "status": "enabled",
    "createTime": "2023-01-01 12:00:00"
  }
}
```

**错误响应**:
- 400: 用户不存在

## 示例请求

### 注册新用户

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123",
    "name": "新用户",
    "email": "newuser@example.com",
    "phone": "13900139000",
    "address": "新用户地址"
  }'
```

### 用户登录

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123"
  }'
```

### 获取用户信息

```bash
curl -X GET http://localhost:8080/api/users/newuser
```

## 注意事项

1. 所有密码在存储时都使用BCrypt算法加密
2. 用户名和邮箱在系统中必须唯一
3. 默认情况下，新注册用户的角色为"user"
4. 系统预置了以下测试用户：
   - 用户名: admin, 密码: 123456, 角色: admin
   - 用户名: merchant001, 密码: 123456, 角色: merchant
   - 用户名: user001, 密码: 123456, 角色: user
   - 用户名: user002, 密码: 123456, 角色: user

## 在线API文档

启动应用后，可以通过以下URL访问在线API文档：
- Swagger UI: http://localhost:8080/swagger-ui.html
- API文档JSON: http://localhost:8080/v2/api-docs

## H2数据库控制台

启动应用后，可以通过以下URL访问H2数据库控制台：
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:userauth
- 用户名: sa
- 密码: (留空)