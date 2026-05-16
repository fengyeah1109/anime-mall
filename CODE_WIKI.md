# Anime Mall 动漫商城系统 Code Wiki

## 目录

1. [项目概述](#1-项目概述)
2. [整体架构](#2-整体架构)
3. [后端架构详解](#3-后端架构详解)
4. [前端架构详解](#4-前端架构详解)
5. [核心模块说明](#5-核心模块说明)
6. [关键类与API](#6-关键类与api)
7. [数据库设计](#7-数据库设计)
8. [依赖关系](#8-依赖关系)
9. [项目运行方式](#9-项目运行方式)

---

## 1. 项目概述

### 1.1 项目简介

Anime Mall 是一个功能完整的动漫周边电商系统，采用前后端分离架构，提供完整的用户购物、订单管理、售后处理等电商功能。系统支持多种动漫IP关联、个性化推荐、数据统计等特色功能。

### 1.2 技术栈

#### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.30 | 渐进式JavaScript框架 |
| Vite | 8.0.1 | 下一代前端构建工具 |
| Element Plus | 2.13.6 | 基于 Vue 3 的组件库 |
| Pinia | 3.0.4 | Vue 状态管理库 |
| Vue Router | 5.0.4 | Vue官方路由管理器 |
| Axios | 1.14.0 | HTTP请求库 |
| ECharts | 6.0.0 | 数据可视化图表库 |

#### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.2.5 | Spring框架简化配置 |
| Spring Security | - | 安全框架 |
| MyBatis Plus | 3.5.5 | MyBatis增强工具 |
| MySQL | 8.x | 关系型数据库 |
| JWT | 0.12.5 | JSON Web Token认证 |
| Lombok | - | 简化代码注解库 |

### 1.3 项目特性

- **用户系统**：支持用户注册、登录、个人资料管理、收货地址管理
- **商品系统**：商品浏览、分类筛选、搜索、详情展示
- **购物车**：添加、删除、修改数量、选中结算
- **订单系统**：订单创建、支付、取消、收货确认
- **售后系统**：售后申请与处理
- **收藏系统**：商品收藏与收藏夹管理
- **评价系统**：商品评价与晒图
- **推荐系统**：基于用户行为的个性化推荐
- **后台管理**：完整的电商后台管理功能
- **数据统计**：销售数据可视化分析

---

## 2. 整体架构

### 2.1 架构设计

项目采用经典的前后端分离架构，前端负责用户界面展示和交互，后端提供RESTful API服务。

```
┌─────────────────────────────────────────────────────┐
│                    用户浏览器                         │
└────────────────────────┬────────────────────────────┘
                         │ HTTP/HTTPS
                         ▼
┌─────────────────────────────────────────────────────┐
│                   前端 (Vue 3)                       │
│  ┌─────────────────────────────────────────────┐    │
│  │              UI 组件层                        │    │
│  │  (Element Plus + 自定义组件)                  │    │
│  └─────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────┐    │
│  │              业务逻辑层                        │    │
│  │  (Pinia Store + 组合式函数)                   │    │
│  └─────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────┐    │
│  │              路由层                           │    │
│  │  (Vue Router + 路由守卫)                     │    │
│  └─────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────┐    │
│  │              网络层                           │    │
│  │  (Axios + 请求拦截器)                        │    │
│  └─────────────────────────────────────────────┘    │
└────────────────────────┬────────────────────────────┘
                         │ RESTful API
                         ▼
┌─────────────────────────────────────────────────────┐
│                  后端 (Spring Boot)                  │
│  ┌─────────────────────────────────────────────┐    │
│  │              控制器层 (Controller)            │    │
│  │  (@RestController + API路由)                 │    │
│  └─────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────┐    │
│  │              业务逻辑层 (Service)             │    │
│  │  (业务处理 + 事务管理)                        │    │
│  └─────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────┐    │
│  │              数据访问层 (Mapper)              │    │
│  │  (MyBatis Plus + 动态SQL)                   │    │
│  └─────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────┐    │
│  │              安全层 (Security)               │    │
│  │  (JWT认证 + 权限控制)                         │    │
│  └─────────────────────────────────────────────┘    │
└────────────────────────┬────────────────────────────┘
                         │ JDBC
                         ▼
┌─────────────────────────────────────────────────────┐
│                   MySQL 数据库                        │
│  (anime_db)                                        │
└─────────────────────────────────────────────────────┘
```

### 2.2 目录结构

#### 后端目录结构

```
backend/
├── src/main/java/com/anime/
│   ├── AnimeApplication.java         # Spring Boot启动类
│   ├── common/
│   │   └── Result.java               # 统一响应结果封装
│   ├── config/
│   │   ├── DatabaseInitializer.java   # 数据库初始化配置
│   │   ├── MybatisPlusConfig.java    # MyBatis Plus配置
│   │   ├── UploadResourceConfig.java # 上传资源配置
│   │   └── WebConfig.java            # Web配置
│   ├── controller/
│   │   ├── AdminController.java       # 管理员管理控制器
│   │   ├── ApiAddressesController.java # 地址管理控制器
│   │   ├── ApiAdminAfterSalesController.java # 后台售后控制器
│   │   ├── ApiAdminBannersController.java # 后台轮播图控制器
│   │   ├── ApiAdminConfigController.java # 后台配置控制器
│   │   ├── ApiAdminController.java    # 后台通用控制器
│   │   ├── ApiAfterSaleController.java # 售后控制器
│   │   ├── ApiArticlesController.java # 文章控制器
│   │   ├── ApiAuthController.java     # 认证控制器
│   │   ├── ApiBannersController.java  # 轮播图控制器
│   │   ├── ApiCartController.java     # 购物车控制器
│   │   ├── ApiCategoriesController.java # 分类控制器
│   │   ├── ApiFavoritesController.java # 收藏控制器
│   │   ├── ApiOrdersController.java   # 订单控制器
│   │   ├── ApiProductsController.java # 商品控制器
│   │   ├── ApiRecommendController.java # 推荐控制器
│   │   ├── ApiReviewsController.java  # 评价控制器
│   │   ├── ApiUserController.java     # 用户控制器
│   │   ├── GenericCrudControllers.java # 通用CRUD控制器
│   │   ├── UploadController.java      # 文件上传控制器
│   │   └── UserController.java        # 用户管理控制器
│   ├── dto/
│   │   ├── CartAddRequest.java        # 购物车添加请求
│   │   ├── CartSelectedRequest.java    # 购物车选中请求
│   │   ├── CartUpdateRequest.java     # 购物车更新请求
│   │   ├── CreateOrderRequest.java    # 创建订单请求
│   │   ├── LoginRequest.java          # 登录请求
│   │   ├── LoginResponse.java         # 登录响应
│   │   ├── RegisterRequest.java       # 注册请求
│   │   ├── SendCodeRequest.java       # 发送验证码请求
│   │   └── UserProfileUpdateRequest.java # 用户资料更新请求
│   ├── entity/
│   │   ├── Address.java               # 收货地址实体
│   │   ├── Admin.java                 # 管理员实体
│   │   ├── AfterSale.java             # 售后实体
│   │   ├── AnimeIp.java               # 动漫IP实体
│   │   ├── Article.java               # 文章实体
│   │   ├── ArticleComment.java        # 文章评论实体
│   │   ├── Banner.java                # 轮播图实体
│   │   ├── Cart.java                  # 购物车实体
│   │   ├── CharacterEntity.java       # 角色实体
│   │   ├── Favorite.java              # 收藏实体
│   │   ├── Order.java                 # 订单实体
│   │   ├── OrderItem.java             # 订单项实体
│   │   ├── Product.java               # 商品实体
│   │   ├── ProductCategory.java       # 商品分类实体
│   │   ├── ProductImage.java          # 商品图片实体
│   │   ├── Review.java                # 评价实体
│   │   ├── SystemConfig.java          # 系统配置实体
│   │   ├── User.java                  # 用户实体
│   │   └── UserBehavior.java          # 用户行为实体
│   ├── exception/
│   │   ├── BusinessException.java     # 业务异常
│   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   ├── mapper/
│   │   └── [18个Mapper接口文件]        # MyBatis Mapper接口
│   ├── security/
│   │   ├── AuthContext.java           # 认证上下文
│   │   ├── AuthPrincipal.java         # 认证主体
│   │   ├── CurrentUserHolder.java     # 当前用户持有器
│   │   ├── JwtAuthenticationFilter.java # JWT认证过滤器
│   │   ├── JwtUtil.java               # JWT工具类
│   │   ├── SecurityConfig.java        # 安全配置
│   │   └── SecurityUtils.java         # 安全工具类
│   ├── service/
│   │   ├── impl/
│   │   │   └── [20个Service实现类]     # 业务逻辑实现
│   │   └── [20个Service接口文件]        # 业务逻辑接口
│   ├── util/
│   │   └── FileStorageService.java    # 文件存储服务
│   └── vo/
│       ├── AfterSaleVO.java           # 售后视图对象
│       ├── CartVO.java               # 购物车视图对象
│       ├── CategoryVO.java           # 分类视图对象
│       ├── FavoriteVO.java           # 收藏视图对象
│       ├── OrderVO.java               # 订单视图对象
│       └── ProductDetailVO.java       # 商品详情视图对象
├── src/main/resources/
│   └── application.yml                # 应用配置文件
├── uploads/                           # 上传文件目录
│   ├── avatars/                       # 用户头像
│   ├── reviews/                       # 评价晒图
│   └── uploads/                       # 通用上传
└── pom.xml                            # Maven配置
```

#### 前端目录结构

```
frontend/
├── public/
│   ├── favicon.svg                    # 网站图标
│   └── icons.svg                      # 图标资源
├── src/
│   ├── api/
│   │   ├── admin.js                   # 后台管理API
│   │   ├── admin/
│   │   │   ├── banners.js             # 后台轮播图API
│   │   │   └── config.js              # 后台配置API
│   │   ├── auth.js                    # 认证API
│   │   ├── cart.js                    # 购物车API
│   │   ├── common.js                  # 通用API
│   │   ├── order.js                   # 订单API
│   │   ├── product.js                 # 商品API
│   │   ├── recommend.js               # 推荐API
│   │   ├── reviews.js                 # 评价API
│   │   └── user.js                    # 用户API
│   ├── assets/
│   │   ├── hero.png                   # 静态图片
│   │   ├── vite.svg                   # Vite图标
│   │   └── vue.svg                    # Vue图标
│   ├── components/
│   │   ├── HelloWorld.vue             # 示例组件
│   │   └── ProductCard.vue            # 商品卡片组件
│   ├── router/
│   │   └── index.js                   # 路由配置
│   ├── stores/
│   │   ├── cart.js                    # 购物车状态管理
│   │   └── user.js                    # 用户状态管理
│   ├── utils/
│   │   ├── image.js                    # 图片处理工具
│   │   └── request.js                 # Axios请求封装
│   ├── views/
│   │   ├── ArticleDetail.vue          # 文章详情页
│   │   ├── Articles.vue                # 文章列表页
│   │   ├── Cart.vue                    # 购物车页面
│   │   ├── Checkout.vue                # 结算页面
│   │   ├── Favorites.vue               # 收藏页面
│   │   ├── Home.vue                    # 首页
│   │   ├── Login.vue                   # 登录页面
│   │   ├── MyReviews.vue               # 我的评价页面
│   │   ├── Orders.vue                  # 订单页面
│   │   ├── Payment.vue                 # 支付页面
│   │   ├── ProductDetail.vue           # 商品详情页
│   │   ├── Products.vue                # 商品列表页
│   │   ├── Profile.vue                 # 个人中心页面
│   │   ├── Recommend.vue               # 推荐页面
│   │   ├── Register.vue               # 注册页面
│   │   ├── admin/
│   │   │   ├── AdminAfterSales.vue     # 后台售后管理
│   │   │   ├── AdminArticles.vue       # 后台文章管理
│   │   │   ├── AdminBanners.vue         # 后台轮播图管理
│   │   │   ├── AdminCategories.vue      # 后台分类管理
│   │   │   ├── AdminOrders.vue         # 后台订单管理
│   │   │   ├── AdminProducts.vue       # 后台商品管理
│   │   │   ├── AdminStatistics.vue     # 后台数据统计
│   │   │   ├── AdminUsers.vue          # 后台用户管理
│   │   │   └── RecommendConfig.vue     # 推荐配置
│   │   └── layouts/
│   │       ├── AdminLayout.vue         # 后台布局
│   │       └── MainLayout.vue          # 主布局
│   ├── App.vue                         # 根组件
│   ├── main.js                         # 应用入口
│   └── style.css                       # 全局样式
├── .gitignore                          # Git忽略配置
├── README.md                           # 项目说明
├── index.html                          # HTML模板
├── package-lock.json                  # 依赖锁定文件
├── package.json                        # 项目配置
└── vite.config.js                      # Vite配置
```

---

## 3. 后端架构详解

### 3.1 分层架构

后端采用标准的三层架构设计，包括Controller层、Service层和Mapper层。

#### Controller层职责

- 接收HTTP请求，解析参数
- 参数验证和数据绑定
- 调用Service层处理业务
- 返回统一格式的响应结果
- 定义RESTful API接口

#### Service层职责

- 实现核心业务逻辑
- 管理事务边界
- 数据转换和处理
- 调用Mapper层进行数据访问
- 异常处理和业务规则校验

#### Mapper层职责

- 数据库CRUD操作
- 复杂SQL编写
- 数据表映射

### 3.2 包结构说明

| 包名 | 说明 | 主要类 |
|------|------|--------|
| `com.anime.common` | 公共组件 | Result |
| `com.anime.config` | 配置类 | DatabaseInitializer, MybatisPlusConfig, UploadResourceConfig, WebConfig |
| `com.anime.controller` | 控制器层 | 20+个Controller类 |
| `com.anime.dto` | 数据传输对象 | LoginRequest, RegisterRequest, CreateOrderRequest等 |
| `com.anime.entity` | 实体类 | 18个数据库表对应的实体 |
| `com.anime.exception` | 异常处理 | BusinessException, GlobalExceptionHandler |
| `com.anime.mapper` | 数据访问层 | 18个Mapper接口 |
| `com.anime.security` | 安全模块 | JwtUtil, JwtAuthenticationFilter, SecurityConfig |
| `com.anime.service` | 业务逻辑层 | 20+个Service接口和实现 |
| `com.anime.util` | 工具类 | FileStorageService |
| `com.anime.vo` | 视图对象 | OrderVO, ProductDetailVO等 |

---

## 4. 前端架构详解

### 4.1 组件化架构

前端采用Vue 3的Composition API进行组件化开发，每个页面都是独立的Vue组件。

#### 页面组件

- **展示型组件**：Home.vue, Products.vue, ProductDetail.vue
- **表单型组件**：Login.vue, Register.vue, Profile.vue
- **管理型组件**：AdminProducts.vue, AdminOrders.vue等

#### 布局组件

- **MainLayout.vue**：用户端主布局，包含头部导航和底部
- **AdminLayout.vue**：管理端主布局，包含侧边栏导航

### 4.2 状态管理

使用Pinia进行状态管理，主要Store包括：

- **user.js**：用户登录状态、用户信息、Token管理
- **cart.js**：购物车商品列表、选中状态、数量

### 4.3 路由管理

使用Vue Router进行路由管理，路由分为两大类：

1. **用户端路由** (`/`)：首页、商品列表、商品详情、购物车、订单、个人中心等
2. **管理端路由** (`/admin`)：商品管理、订单管理、用户管理、数据统计等

### 4.4 网络请求

使用Axios封装统一的请求工具：

- 请求拦截器：添加JWT Token到请求头
- 响应拦截器：统一处理错误响应
- 错误处理：401跳转登录，500显示错误提示

---

## 5. 核心模块说明

### 5.1 认证模块

#### 后端认证流程

1. 用户登录请求 → JwtAuthenticationFilter
2. 验证用户名密码 → AuthService
3. 生成JWT Token → JwtUtil
4. 返回Token给客户端
5. 后续请求携带Token → JwtAuthenticationFilter验证Token

#### 前端认证流程

1. 用户登录 → Auth API
2. 保存Token到localStorage和Pinia Store
3. 请求拦截器自动添加Token到请求头
4. 401响应 → 清除Token，跳转登录页

#### 关键类

- `JwtUtil.java`：Token生成与解析
- `JwtAuthenticationFilter.java`：JWT认证过滤器
- `SecurityConfig.java`：Spring Security配置
- `ApiAuthController.java`：认证API控制器
- `AuthService.java`：认证业务逻辑

### 5.2 商品模块

#### 功能特性

- 商品列表浏览（支持分类、IP、关键词筛选）
- 商品详情展示（图片、描述、价格、评价）
- 商品搜索（按名称、标签、描述）
- 价格区间筛选
- 多种排序方式（时间、价格、销量）

#### 关键类

- `Product.java`：商品实体
- `ApiProductsController.java`：商品API
- `ProductService.java`：商品业务逻辑
- `ProductDetailVO.java`：商品详情视图对象

### 5.3 订单模块

#### 订单状态流转

```
待支付(1) → 已支付(2) → 已发货(3) → 已收货(4) → 已完成
    ↓           ↓
  已取消(0)   退款中(5) → 已退款(6)
```

#### 关键类

- `Order.java`：订单实体
- `OrderItem.java`：订单项实体
- `OrderBizServiceImpl.java`：订单业务实现
- `ApiOrdersController.java`：订单API

### 5.4 购物车模块

#### 功能特性

- 添加商品到购物车
- 修改商品数量
- 选择/取消选中
- 删除商品
- 批量结算

#### 关键类

- `Cart.java`：购物车实体
- `CartVO.java`：购物车视图对象
- `ApiCartController.java`：购物车API

### 5.5 推荐模块

#### 推荐策略

- 基于用户浏览行为（UserBehavior）
- 基于用户收藏偏好
- 热门商品推荐
- 新品推荐
- 可配置的推荐规则

#### 关键类

- `UserBehavior.java`：用户行为实体
- `SystemConfig.java`：系统配置实体
- `RecommendService.java`：推荐服务
- `ApiRecommendController.java`：推荐API

### 5.6 后台管理模块

#### 管理功能

| 模块 | 功能 |
|------|------|
| 商品管理 | 商品增删改查、上下架、价格库存管理 |
| 分类管理 | 分类增删改查、排序 |
| 订单管理 | 订单列表、状态更新、发货操作 |
| 售后管理 | 售后申请列表、处理状态 |
| 用户管理 | 用户列表、状态管理 |
| 轮播图管理 | 轮播图增删改查、链接配置 |
| 文章管理 | 文章增删改查、富文本编辑 |
| 数据统计 | 销售数据可视化、图表展示 |
| 推荐配置 | 推荐规则配置、参数调整 |

---

## 6. 关键类与API

### 6.1 公共类

#### Result.java

统一响应结果封装类，用于规范API返回值格式。

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;    // 状态码，200表示成功，500表示失败
    private String message;  // 响应消息
    private T data;          // 响应数据
    
    // 成功响应
    public static <T> Result<T> success(T data)
    public static Result<Void> success()
    
    // 失败响应
    public static <T> Result<T> fail(String message)
}
```

#### BusinessException.java

业务异常类，用于抛出业务逻辑错误。

```java
public class BusinessException extends RuntimeException {
    private Integer code = 500;
    
    public BusinessException(String message)
    public BusinessException(Integer code, String message)
}
```

#### GlobalExceptionHandler.java

全局异常处理器，统一处理各类异常并返回统一格式。

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 处理业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException ex)
    
    // 处理参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException ex)
    
    // 处理其他异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception ex)
}
```

### 6.2 安全模块

#### JwtUtil.java

JWT Token工具类，负责Token的生成和解析。

```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration-minutes}")
    private long expirationMinutes;
    
    // 生成Token
    public String generateToken(Long userId, String role)
    
    // 解析Token
    public Claims parseToken(String token)
}
```

#### JwtAuthenticationFilter.java

JWT认证过滤器，拦截请求并验证Token。

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // 从请求头获取Token
    // 解析Token获取用户信息
    // 设置SecurityContext
}
```

#### SecurityConfig.java

Spring Security安全配置类。

```java
@Configuration
public class SecurityConfig {
    // 配置哪些路径需要认证
    // 配置JWT过滤器
    // 配置CSRF
    // 配置会话管理为无状态
}
```

### 6.3 API控制器

#### ApiAuthController.java

认证相关API接口。

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发送验证码 | POST | /api/auth/send-code | 发送注册验证码 |
| 用户注册 | POST | /api/auth/register | 用户注册 |
| 用户登录 | POST | /api/auth/login | 用户登录 |

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {
    private final AuthService authService;
    
    @PostMapping("/send-code")
    public Result<String> sendCode(@Valid @RequestBody SendCodeRequest request)
    
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request)
    
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request)
}
```

#### ApiProductsController.java

商品相关API接口。

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 商品列表 | GET | /api/products | 分页查询商品 |
| 动漫IP列表 | GET | /api/products/anime-ips | 获取动漫IP列表 |
| 商品详情 | GET | /api/products/{id} | 获取商品详情 |

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ApiProductsController {
    private final ProductService productService;
    private final ProductImageService productImageService;
    private final AnimeIpService animeIpService;
    private final CharacterEntityService characterEntityService;
    private final ReviewService reviewService;
    
    @GetMapping
    public Result<Page<Product>> list(
        @RequestParam(value = "categoryId", required = false) Long categoryId,
        @RequestParam(value = "animeIpId", required = false) Long animeIpId,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
        @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
        @RequestParam(value = "sortBy", defaultValue = "createTime") String sortBy,
        @RequestParam(value = "order", defaultValue = "desc") String order,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size
    )
    
    @GetMapping("/anime-ips")
    public Result<List<AnimeIp>> listAnimeIps()
    
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable("id") Long id)
}
```

#### ApiOrdersController.java

订单相关API接口。

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建订单 | POST | /api/orders | 从购物车创建订单 |
| 我的订单列表 | GET | /api/orders | 获取我的订单列表 |
| 订单详情 | GET | /api/orders/{id} | 获取订单详情 |
| 取消订单 | DELETE | /api/orders/{id} | 取消订单 |
| 支付订单 | POST | /api/orders/{id}/pay | 支付订单 |
| 确认收货 | PUT | /api/orders/{id}/receive | 确认收货 |

#### ApiCartController.java

购物车相关API接口。

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 购物车列表 | GET | /api/cart | 获取我的购物车 |
| 添加商品 | POST | /api/cart | 添加商品到购物车 |
| 更新数量 | PUT | /api/cart | 更新商品数量 |
| 选中商品 | PUT | /api/cart/select | 设置选中状态 |
| 删除商品 | DELETE | /api/cart/{id} | 删除购物车商品 |

#### AdminController.java

后台管理员API。

```java
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    
    @GetMapping
    public Result<List<Admin>> list()
    
    @GetMapping("/{id}")
    public Result<Admin> get(@PathVariable Long id)
    
    @PostMapping
    public Result<Void> create(@RequestBody Admin admin)
    
    @PutMapping
    public Result<Void> update(@RequestBody Admin admin)
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id)
}
```

### 6.4 业务服务

#### OrderBizServiceImpl.java

订单业务实现类，核心业务逻辑包括订单创建、支付、取消、收货等。

```java
@Service
@RequiredArgsConstructor
public class OrderBizServiceImpl implements OrderBizService {
    private final CartService cartService;
    private final ProductService productService;
    private final ProductMapper productMapper;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final UserService userService;
    private final AddressService addressService;
    
    // 创建订单（从购物车选中项）
    @Override
    @Transactional
    public OrderVO createFromSelectedCart(Long userId, CreateOrderRequest request)
    
    // 支付订单
    @Override
    public void pay(Long userId, Long orderId)
    
    // 获取我的订单列表
    @Override
    public Page<OrderVO> myOrders(Long userId, Integer status, int page, int size)
    
    // 获取订单详情
    @Override
    public OrderVO myOrderDetail(Long userId, Long orderId)
    
    // 取消订单
    @Override
    public void cancel(Long userId, Long orderId)
    
    // 确认收货
    @Override
    public void receive(Long userId, Long orderId)
}
```

### 6.5 工具类

#### FileStorageService.java

文件存储服务，负责处理文件上传。

```java
@Service
public class FileStorageService {
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    // 存储文件
    public String store(MultipartFile file, String subDir)
}
```

---

## 7. 数据库设计

### 7.1 数据库表结构

#### 核心业务表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `user` | 用户表 | id, username, password, nickname, phone, avatar, gender, birthday |
| `admin` | 管理员表 | id, username, password, nickname |
| `product` | 商品表 | id, name, description, price, originalPrice, stock, sales, mainImage, categoryId, animeIpId, characterIds, tags, status, isHot, isNew |
| `product_category` | 商品分类表 | id, name, parentId, sortOrder, status |
| `product_image` | 商品图片表 | id, productId, imageUrl, sortOrder |
| `anime_ip` | 动漫IP表 | id, name, description, logo, status |
| `character_entity` | 角色表 | id, animeIpId, name, description, image |
| `cart` | 购物车表 | id, userId, productId, quantity, selected |
| `order` | 订单表 | id, orderNo, userId, totalAmount, payAmount, status, addressId, remark |
| `order_item` | 订单项表 | id, orderId, productId, productName, price, quantity, totalAmount |
| `address` | 收货地址表 | id, userId, receiverName, phone, province, city, district, detailAddress, isDefault |
| `favorite` | 收藏表 | id, userId, productId, createTime |
| `review` | 评价表 | id, userId, productId, orderId, rating, content, images, createTime |
| `after_sale` | 售后表 | id, userId, orderId, type, reason, description, status |
| `banner` | 轮播图表 | id, title, image, link, sortOrder, status |
| `article` | 文章表 | id, title, cover, content, author, createTime |
| `article_comment` | 文章评论表 | id, articleId, userId, content, createTime |
| `user_behavior` | 用户行为表 | id, userId, productId, behaviorType, createTime |
| `system_config` | 系统配置表 | id, configKey, configValue, description |

### 7.2 实体类说明

#### Product.java

商品实体类，定义了商品的完整属性。

```java
@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;              // 商品名称
    private String description;       // 商品描述
    private BigDecimal price;          // 现价
    private BigDecimal originalPrice;  // 原价
    private Integer stock;            // 库存
    private Integer sales;            // 销量
    private String mainImage;         // 主图
    private Long categoryId;          // 分类ID
    private Long animeIpId;           // 动漫IP ID
    private String characterIds;      // 角色ID列表（逗号分隔）
    private String tags;              // 标签
    private Integer status;            // 状态：0下架，1上架
    private Integer isHot;             // 是否热销
    private Integer isNew;            // 是否新品
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    @TableLogic                      // 逻辑删除
    private Integer deleted;
}
```

#### Order.java

订单实体类，使用反引号包裹表名因为order是MySQL关键字。

```java
@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;           // 订单编号
    private Long userId;               // 用户ID
    private BigDecimal totalAmount;   // 订单总金额
    private BigDecimal payAmount;      // 实付金额
    private BigDecimal freightAmount; // 运费
    private BigDecimal discountAmount;// 优惠金额
    private Integer payType;          // 支付方式
    private Integer status;           // 订单状态
    private LocalDateTime paymentTime;// 支付时间
    private LocalDateTime deliveryTime;// 发货时间
    private LocalDateTime receiveTime;// 收货时间
    private Long addressId;            // 收货地址ID
    private String remark;            // 备注
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
```

---

## 8. 依赖关系

### 8.1 前端依赖

#### package.json

```json
{
  "dependencies": {
    "axios": "^1.14.0",           // HTTP请求库
    "echarts": "^6.0.0",          // 数据可视化
    "element-plus": "^2.13.6",    // UI组件库
    "pinia": "^3.0.4",            // 状态管理
    "vue": "^3.5.30",             // 核心框架
    "vue-router": "^5.0.4"       // 路由管理
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^6.0.5", // Vue插件
    "vite": "^8.0.1"              // 构建工具
  }
}
```

### 8.2 后端依赖

#### pom.xml

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Spring JDBC -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    
    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
    
    <!-- MyBatis Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>3.5.5</version>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.5</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

### 8.3 依赖关系图

```
前端依赖关系：
┌────────────────────────────────────────────┐
│                  Vue 3                      │
│                    │                        │
│         ┌──────────┼──────────┐            │
│         ▼          ▼          ▼            │
│    Vue Router   Element Plus    Pinia      │
│         │          │                       │
│         └──────────┼──────────┘            │
│                    ▼                        │
│                   Axios                     │
└────────────────────────────────────────────┘

后端依赖关系：
┌────────────────────────────────────────────┐
│              Spring Boot                   │
│                    │                        │
│         ┌──────────┼──────────┐            │
│         ▼          ▼          ▼            │
│   Spring MVC  Spring Security  Spring JDBC │
│                    │                        │
│                    ▼                        │
│            MyBatis Plus                    │
│                    │                        │
│                    ▼                        │
│              MySQL Driver                  │
└────────────────────────────────────────────┘
```

---

## 9. 项目运行方式

### 9.1 环境要求

#### 前端环境要求

- Node.js: >= 16.x
- npm: >= 8.x
- 现代浏览器（Chrome、Firefox、Safari、Edge）

#### 后端环境要求

- JDK: 17
- Maven: >= 3.8
- MySQL: 8.x
- 服务器内存: >= 2GB

### 9.2 数据库配置

#### 创建数据库

```sql
CREATE DATABASE anime_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 修改配置文件

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/anime_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password  # 修改为你的数据库密码
```

### 9.3 后端启动

#### 使用Maven启动

```bash
# 进入后端目录
cd backend

# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

#### 使用IDE启动

1. 使用IntelliJ IDEA或Eclipse打开后端项目
2. 等待Maven下载依赖
3. 右键点击 `AnimeApplication.java`
4. 选择 "Run 'AnimeApplication'"
5. 等待应用启动，默认端口为8081

#### 验证后端启动

访问 http://localhost:8081/api/products 检查是否返回数据。

### 9.4 前端启动

#### 安装依赖

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install
```

#### 启动开发服务器

```bash
# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

#### 前端配置

如需修改API地址，编辑 `frontend/src/utils/request.js`：

```javascript
const request = axios.create({
  baseURL: 'http://localhost:8081',  // 修改为后端地址
  timeout: 10000
})
```

### 9.5 生产部署

#### 后端部署

1. 打包应用：

```bash
cd backend
mvn clean package -DskipTests
```

2. 运行JAR包：

```bash
java -jar target/anime-backend-1.0.0.jar
```

#### 前端部署

1. 构建生产版本：

```bash
cd frontend
npm run build
```

2. 将 `dist` 目录下的文件部署到Nginx或其他Web服务器：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location / {
        root /var/www/anime-mall/dist;
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8081;
    }
    
    location /uploads {
        alias /path/to/backend/uploads;
    }
}
```

### 9.6 常用命令

#### 后端常用命令

```bash
# 清理并编译
mvn clean compile

# 运行测试
mvn test

# 打包
mvn package

# 跳过测试打包
mvn clean package -DskipTests

# 查看依赖树
mvn dependency:tree
```

#### 前端常用命令

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build

# 预览构建结果
npm run preview

# 代码检查
npm run lint

# 修复代码格式
npm run lint -- --fix
```

---

## 附录

### A. API接口列表

#### 公开接口（无需认证）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 商品列表 | GET | /api/products | 分页查询商品 |
| 动漫IP列表 | GET | /api/products/anime-ips | 获取动漫IP列表 |
| 商品详情 | GET | /api/products/{id} | 获取商品详情 |
| 分类列表 | GET | /api/categories | 获取分类列表 |
| 文章列表 | GET | /api/articles | 获取文章列表 |
| 文章详情 | GET | /api/articles/{id} | 获取文章详情 |
| 轮播图列表 | GET | /api/banners | 获取轮播图列表 |
| 推荐商品 | GET | /api/recommend | 获取推荐商品 |
| 用户登录 | POST | /api/auth/login | 用户登录 |
| 用户注册 | POST | /api/auth/register | 用户注册 |
| 发送验证码 | POST | /api/auth/send-code | 发送验证码 |
| 文件上传 | POST | /api/upload | 文件上传 |

#### 用户接口（需要认证）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 购物车列表 | GET | /api/cart | 获取购物车 |
| 添加购物车 | POST | /api/cart | 添加商品到购物车 |
| 更新购物车 | PUT | /api/cart | 更新购物车 |
| 选中购物车 | PUT | /api/cart/select | 设置选中状态 |
| 删除购物车 | DELETE | /api/cart/{id} | 删除购物车商品 |
| 创建订单 | POST | /api/orders | 创建订单 |
| 订单列表 | GET | /api/orders | 获取订单列表 |
| 订单详情 | GET | /api/orders/{id} | 获取订单详情 |
| 取消订单 | DELETE | /api/orders/{id} | 取消订单 |
| 支付订单 | POST | /api/orders/{id}/pay | 支付订单 |
| 确认收货 | PUT | /api/orders/{id}/receive | 确认收货 |
| 收藏列表 | GET | /api/favorites | 获取收藏列表 |
| 添加收藏 | POST | /api/favorites | 添加收藏 |
| 删除收藏 | DELETE | /api/favorites/{id} | 删除收藏 |
| 评价列表 | GET | /api/reviews | 获取评价列表 |
| 添加评价 | POST | /api/reviews | 添加评价 |
| 售后列表 | GET | /api/after-sales | 获取售后列表 |
| 申请售后 | POST | /api/after-sales | 申请售后 |
| 收货地址 | GET/POST/PUT/DELETE | /api/addresses | 收货地址管理 |
| 用户信息 | GET/PUT | /api/user/profile | 用户信息管理 |
| 上传头像 | POST | /api/user/avatar | 上传头像 |

#### 管理接口（需要管理员权限）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 商品管理 | GET/POST/PUT/DELETE | /api/admin/products | 商品管理 |
| 分类管理 | GET/POST/PUT/DELETE | /api/admin/categories | 分类管理 |
| 订单管理 | GET/PUT | /api/admin/orders | 订单管理 |
| 用户管理 | GET | /api/admin/users | 用户列表 |
| 售后管理 | GET/PUT | /api/admin/after-sales | 售后管理 |
| 轮播图管理 | GET/POST/PUT/DELETE | /api/admin/banners | 轮播图管理 |
| 文章管理 | GET/POST/PUT/DELETE | /api/admin/articles | 文章管理 |
| 管理员管理 | GET/POST/PUT/DELETE | /admin | 管理员管理 |
| 统计数据 | GET | /api/admin/statistics | 统计数据 |
| 推荐配置 | GET/PUT | /api/admin/config | 推荐配置 |

### B. 配置说明

#### JWT配置

```yaml
jwt:
  secret: "please-change-this-secret-to-a-long-random-string"  # JWT密钥
  expiration-minutes: 1440  # Token过期时间（分钟），默认24小时
```

#### 上传配置

```yaml
app:
  upload:
    dir: ./uploads  # 上传文件存储目录

spring:
  servlet:
    multipart:
      max-file-size: 10MB  # 单个文件最大大小
      max-request-size: 20MB  # 请求最大大小
```

### C. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 500 | 服务器内部错误 |
| 401 | 未认证（Token无效或过期） |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 400 | 请求参数错误 |

---

*文档更新时间: 2026-05-15*
