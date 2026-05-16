# Anime Mall 动漫商城系统 - 前后端完整功能链路文档

## 一、项目核心需求提炼

### 1.1 用户端核心需求

| 需求模块 | 详细功能点 | 用户价值 |
|---------|-----------|---------|
| **用户认证** | 注册、登录、验证码、Token认证 | 保障账户安全，实现个性化服务 |
| **商品浏览** | 分类筛选、IP筛选、关键词搜索、价格区间、排序 | 快速找到目标商品 |
| **商品详情** | 图片展示、价格库存、评价展示、收藏、加入购物车 | 全面了解商品，做出购买决策 |
| **购物车** | 添加/删除/修改数量、批量选中、实时价格计算 | 高效管理购物清单 |
| **订单流程** | 创建订单、选择地址、支付、取消、确认收货 | 完成购买闭环 |
| **售后服务** | 申请售后、查看售后进度 | 保障售后权益 |
| **个人中心** | 资料管理、收货地址管理、订单查询、收藏管理 | 便捷的账户管理 |

### 1.2 管理端核心需求

| 需求模块 | 详细功能点 | 管理价值 |
|---------|-----------|---------|
| **商品管理** | 商品增删改查、上下架、价格库存调整 | 商品运营管理 |
| **分类管理** | 分类增删改查、层级结构 | 商品组织管理 |
| **订单管理** | 订单列表、状态更新、发货操作 | 订单履约管理 |
| **售后管理** | 售后申请处理、退款操作 | 售后问题处理 |
| **用户管理** | 用户列表、状态管理 | 用户运营 |
| **数据统计** | 销售数据可视化、图表展示 | 数据驱动决策 |
| **推荐配置** | 推荐规则参数调整 | 个性化推荐优化 |

---

## 二、前后端对应关系详解

### 2.1 认证模块【登录/注册】

#### 后端设计

**1. 接口设计**

| 接口名称 | HTTP方法 | 路径 | 请求参数 | 返回数据 | 权限 |
|---------|----------|------|----------|----------|------|
| 发送验证码 | POST | /api/auth/send-code | identifier | 验证码字符串 | 公开 |
| 用户注册 | POST | /api/auth/register | username, identifier, code, password, nickname等 | 无 | 公开 |
| 用户登录 | POST | /api/auth/login | identifier, password | {token: "jwt字符串"} | 公开 |

**2. 后端核心代码**

```java
// ApiAuthController.java - 认证控制器
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {
    private final AuthService authService;
    
    // POST /api/auth/send-code
    @PostMapping("/send-code")
    public Result<String> sendCode(@Valid @RequestBody SendCodeRequest request) {
        String code = authService.sendVerificationCode(request.getIdentifier());
        return Result.success(code);  // 返回验证码用于开发调试
    }
    
    // POST /api/auth/register
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(
            request.getUsername(),
            request.getIdentifier(), 
            request.getCode(), 
            request.getPassword(),
            request.getNickname(),
            request.getGender(),
            request.getBirthday()
        );
        return Result.success();
    }
    
    // POST /api/auth/login
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getIdentifier(), request.getPassword());
        return Result.success(new LoginResponse(token));
    }
}
```

**3. 数据结构设计**

```java
// LoginRequest.java - 登录请求DTO
@Data
public class LoginRequest {
    @NotBlank(message = "账号不能为空")
    private String identifier;  // 手机号/邮箱/用户名
    
    @NotBlank(message = "密码不能为空")
    private String password;
}

// LoginResponse.java - 登录响应DTO
@Data
public class LoginResponse {
    private String token;
    
    public LoginResponse(String token) {
        this.token = token;
    }
}

// Result.java - 统一响应格式
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;     // 200=成功, 500=失败
    private String message;   // 响应消息
    private T data;           // 响应数据
    
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }
    
    public static Result<Void> fail(String message) {
        return new Result<>(500, message, null);
    }
}
```

**4. 安全认证设计**

```java
// JwtUtil.java - JWT工具类
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration-minutes}")
    private long expirationMinutes;
    
    // 生成Token：包含用户ID和角色
    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMinutes * 60_000);
        
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)  // 区分USER和ADMIN
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }
    
    // 解析Token：获取用户信息
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

// SecurityConfig.java - 安全配置
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()  // 公开接口
                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // 管理员接口
                .anyRequest().authenticated()  // 其他需要认证
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

#### 前端实现

**1. API层封装**

```javascript
// frontend/src/api/auth.js
import request from '@/utils/request'

// 发送验证码
export const sendCodeApi = (identifier) => request.post('/auth/send-code', { identifier })

// 用户注册
export const registerApi = (data) => request.post('/auth/register', data)

// 用户登录
export const loginApi = (data) => request.post('/auth/login', data)
```

**2. 网络请求封装**

```javascript
// frontend/src/utils/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动添加Token
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  // 登录、注册、验证码接口不需要Token
  if (config.url.includes('/auth/') || config.url === '/banners') {
    delete config.headers.Authorization
    return config
  }
  if (token) {
    config.headers.Authorization = `Bearer ${token}`  // 添加JWT Token
  }
  return config
})

// 响应拦截器：统一处理错误
request.interceptors.response.use(
  (res) => {
    const payload = res.data
    if (payload?.code !== 200) {
      ElMessage.error(payload?.message || '请求失败')
      return Promise.reject(payload)
    }
    return payload.data  // 返回data部分
  },
  (err) => {
    if (err?.response?.status === 401 || err?.response?.status === 403) {
      ElMessage.warning('未登录，请先登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (location.pathname !== '/login') {
        setTimeout(() => { location.href = '/login' }, 1500)
      }
    }
    return Promise.reject(err)
  }
)

export default request
```

**3. 状态管理**

```javascript
// frontend/src/stores/user.js
import { defineStore } from 'pinia'
import { loginApi } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || '{}'),
    role: localStorage.getItem('role') || ''
  }),
  
  getters: {
    isLogin: (state) => !!state.token,
    isAdmin: (state) => state.role === 'ADMIN'
  },
  
  actions: {
    setAuth(token, user) {
      this.token = token
      this.user = user
      this.role = user.role
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
      localStorage.setItem('role', user.role)
    },
    
    logout() {
      this.token = ''
      this.user = {}
      this.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('role')
    }
  }
})
```

**4. 登录页面实现**

```vue
<!-- frontend/src/views/Login.vue -->
<template>
  <div class="login-page">
    <div class="login-box">
      <!-- Tab切换：用户登录 / 管理员登录 -->
      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="用户登录" name="user">
          <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
            <!-- 账号输入框 -->
            <el-form-item prop="identifier">
              <el-input 
                v-model="form.identifier" 
                placeholder="手机号/邮箱/用户名" 
                size="large"
                prefix-icon="User"
              />
            </el-form-item>
            <!-- 密码输入框 -->
            <el-form-item prop="password">
              <el-input 
                v-model="form.password" 
                type="password" 
                show-password 
                placeholder="密码" 
                size="large"
                prefix-icon="Lock"
              />
            </el-form-item>
            <!-- 登录按钮 -->
            <el-button type="primary" size="large" class="login-btn" @click="submit('USER')">
              登录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="管理员登录" name="admin">
          <!-- 管理员登录表单... -->
          <el-button type="primary" size="large" class="login-btn admin-btn" @click="submit('ADMIN')">
            管理员登录
          </el-button>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('user')
const formRef = ref()
const form = ref({ identifier: '', password: '' })

// 表单验证规则
const rules = {
  identifier: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 提交登录
const submit = async (role) => {
  if (role === 'USER') {
    await formRef.value.validate()
    const data = await loginApi(form.value)
    userStore.setAuth(data.token, { role: 'USER' })
    ElMessage.success('登录成功')
    router.push('/')
  } else {
    // 管理员登录逻辑...
    userStore.setAuth(data.token, { role: 'ADMIN' })
    router.push('/admin/products')
  }
}
</script>
```

#### 界面效果展示

```
┌─────────────────────────────────────────────┐
│          动漫商城 - 登录页面                  │
├─────────────────────────────────────────────┤
│                                             │
│              🏮 动漫商城                     │
│          正品周边，品质保证                   │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │ [用户登录]  [管理员登录]              │    │
│  ├─────────────────────────────────────┤    │
│  │  👤 [请输入账号                    ] │    │
│  │  🔒 [请输入密码                    ] │    │
│  │                                     │    │
│  │  ╭─────────────────────────────╮   │    │
│  │  │         登 录              │   │    │
│  │  ╰─────────────────────────────╯   │    │
│  │                                     │    │
│  │     还没有账号？立即注册              │    │
│  └─────────────────────────────────────┘    │
│                                             │
│     ☁️        ⭐         🐱                │
└─────────────────────────────────────────────┘
```

**界面效果说明：**
- 左侧Tab切换：支持普通用户和管理员两种登录模式
- 渐变背景色：#d4e5ef 到 #b8d4e3
- 卡片毛玻璃效果：背景透明度和模糊效果
- 浮动动画装饰：云朵、星星、猫耳emoji
- 输入框聚焦效果：粉色边框高亮
- 登录按钮：珊瑚色渐变 (#FF6B6B → #FF8E53)
- 登录成功：根据角色跳转不同页面（用户→首页，管理员→后台）

---

### 2.2 商品模块【列表/详情】

#### 后端设计

**1. 接口设计**

| 接口名称 | HTTP方法 | 路径 | 查询参数 | 返回数据 |
|---------|----------|------|----------|----------|
| 商品列表 | GET | /api/products | categoryId, animeIpId, keyword, minPrice, maxPrice, sortBy, order, page, size | Page<Product> |
| 动漫IP列表 | GET | /api/products/anime-ips | 无 | List<AnimeIp> |
| 商品详情 | GET | /api/products/{id} | 无 | ProductDetailVO |

**2. 后端核心代码**

```java
// ApiProductsController.java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ApiProductsController {
    private final ProductService productService;
    private final ProductImageService productImageService;
    private final AnimeIpService animeIpService;
    private final CharacterEntityService characterEntityService;
    private final ReviewService reviewService;
    
    // GET /api/products - 商品列表查询
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
    ) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);  // 只查上架商品
        
        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        
        // IP筛选
        if (animeIpId != null) {
            wrapper.eq(Product::getAnimeIpId, animeIpId);
        }
        
        // 关键词搜索
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or().like(Product::getTags, keyword)
                    .or().like(Product::getDescription, keyword));
        }
        
        // 价格区间
        if (minPrice != null) {
            wrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Product::getPrice, maxPrice);
        }
        
        // 排序
        boolean asc = "asc".equalsIgnoreCase(order);
        if ("price".equalsIgnoreCase(sortBy)) {
            if (asc) wrapper.orderByAsc(Product::getPrice);
            else wrapper.orderByDesc(Product::getPrice);
        } else if ("sales".equalsIgnoreCase(sortBy)) {
            if (asc) wrapper.orderByAsc(Product::getSales);
            else wrapper.orderByDesc(Product::getSales);
        } else {
            if (asc) wrapper.orderByAsc(Product::getCreateTime);
            else wrapper.orderByDesc(Product::getCreateTime);
        }
        
        return Result.success(productService.page(new Page<>(page, size), wrapper));
    }
    
    // GET /api/products/anime-ips
    @GetMapping("/anime-ips")
    public Result<List<AnimeIp>> listAnimeIps() {
        return Result.success(animeIpService.list(
            new LambdaQueryWrapper<AnimeIp>().eq(AnimeIp::getStatus, 1)
        ));
    }
    
    // GET /api/products/{id}
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable("id") Long id) {
        Product product = productService.getOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id)
                .eq(Product::getStatus, 1)
        );
        if (product == null) {
            return Result.fail("product not found");
        }
        
        // 查询商品图片
        List<ProductImage> images = productImageService.list(
            new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, id)
                .orderByAsc(ProductImage::getSortOrder)
        );
        
        // 查询动漫IP
        AnimeIp animeIp = product.getAnimeIpId() == null ? null :
                animeIpService.getById(product.getAnimeIpId());
        
        // 查询关联角色
        List<CharacterEntity> characters = parseCharacterIds(product.getCharacterIds()).isEmpty()
                ? Collections.emptyList()
                : characterEntityService.list(
                    new LambdaQueryWrapper<CharacterEntity>()
                        .in(CharacterEntity::getId, parseCharacterIds(product.getCharacterIds()))
        );
        
        // 查询最新评价
        List<Review> latestReviews = reviewService.list(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getProductId, id)
                        .orderByDesc(Review::getCreateTime)
                        .last("limit 5")
        );
        
        // 组装VO
        ProductDetailVO vo = new ProductDetailVO();
        vo.setProduct(product);
        vo.setImages(images);
        vo.setAnimeIp(animeIp);
        vo.setCharacters(characters);
        vo.setLatestReviews(latestReviews);
        
        return Result.success(vo);
    }
    
    // 解析角色ID字符串
    private List<Long> parseCharacterIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}
```

**3. 数据模型设计**

```java
// Product.java - 商品实体
@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;                 // 商品名称
    private String description;           // 商品描述
    private BigDecimal price;            // 现价
    private BigDecimal originalPrice;    // 原价
    private Integer stock;                // 库存
    private Integer sales;               // 销量
    private String mainImage;            // 主图URL
    private Long categoryId;             // 分类ID
    private Long animeIpId;              // 动漫IP ID
    private String characterIds;          // 角色ID（逗号分隔）
    private String tags;                 // 标签（逗号分隔）
    private Integer status;              // 状态：0下架，1上架
    private Integer isHot;               // 是否热销：0否，1是
    private Integer isNew;               // 是否新品：0否，1是
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic                      // 逻辑删除
    private Integer deleted;
}

// ProductDetailVO.java - 商品详情视图对象
@Data
public class ProductDetailVO {
    private Product product;                    // 商品信息
    private List<ProductImage> images;         // 图片列表
    private AnimeIp animeIp;                   // 动漫IP
    private List<CharacterEntity> characters; // 角色列表
    private List<Review> latestReviews;        // 最新评价
}
```

#### 前端实现

**1. API层封装**

```javascript
// frontend/src/api/product.js
import request from '@/utils/request'

// 获取分类树
export const getCategoriesApi = () => request.get('/categories/tree')

// 获取商品列表
export const getProductsApi = (params) => request.get('/products', { params })

// 获取商品详情
export const getProductDetailApi = (id) => request.get(`/products/${id}`)

// 获取动漫IP列表
export const getAnimeIpsApi = () => request.get('/products/anime-ips')
```

**2. 商品列表页实现**

```vue
<!-- frontend/src/views/Products.vue -->
<template>
  <div class="products-page">
    <div class="products-wrapper">
      <!-- 左侧边栏：分类和IP筛选 -->
      <div class="sidebar">
        <div class="category-card sidebar-card">
          <div class="category-header">
            <span class="category-icon">📂</span>
            <h4>商品分类</h4>
          </div>
          <div class="category-list">
            <div 
              class="category-item" 
              :class="{ active: !query.categoryId }"
              @click="onCategory('')"
            >
              全部分类
            </div>
            <div 
              v-for="c in categories" 
              :key="c.id" 
              class="category-item"
              :class="{ active: query.categoryId === String(c.id) }"
              @click="onCategory(String(c.id))"
            >
              {{ c.name }}
            </div>
          </div>
        </div>
        
        <div class="category-card sidebar-card">
          <div class="category-header">
            <span class="category-icon">🎭</span>
            <h4>动漫IP</h4>
          </div>
          <div class="category-list">
            <div 
              class="category-item" 
              :class="{ active: !query.animeIpId }"
              @click="onAnimeIp('')"
            >
              全部IP
            </div>
            <div 
              v-for="ip in animeIps" 
              :key="ip.id" 
              class="category-item"
              :class="{ active: query.animeIpId === String(ip.id) }"
              @click="onAnimeIp(String(ip.id))"
            >
              {{ ip.name }}
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧主内容区 -->
      <div class="main-content">
        <!-- 筛选栏 -->
        <div class="filter-bar">
          <div class="filter-left">
            <!-- 关键词搜索 -->
            <el-input 
              v-model="query.keyword" 
              placeholder="搜索商品" 
              clearable
              class="search-input"
              @keyup.enter="load"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <!-- 价格区间 -->
            <div class="price-filter">
              <el-input-number v-model="query.minPrice" :min="0" placeholder="最低价" />
              <span>-</span>
              <el-input-number v-model="query.maxPrice" :min="0" placeholder="最高价" />
            </div>
          </div>
          
          <div class="filter-right">
            <!-- 排序选择 -->
            <el-select v-model="query.sortBy" @change="load">
              <el-option label="综合排序" value="createTime" />
              <el-option label="价格排序" value="price" />
              <el-option label="销量排序" value="sales" />
            </el-select>
            <el-button type="primary" @click="load">搜索</el-button>
          </div>
        </div>

        <!-- 已选筛选标签 -->
        <div class="selected-tags" v-if="query.categoryId || query.animeIpId">
          <span>已选筛选：</span>
          <span class="tag-item category" v-if="query.categoryId">
            {{ getCategoryName(query.categoryId) }}
            <span @click="onCategory('')">×</span>
          </span>
          <span class="tag-item anime-ip" v-if="query.animeIpId">
            {{ getAnimeIpName(query.animeIpId) }}
            <span @click="onAnimeIp('')">×</span>
          </span>
        </div>

        <!-- 商品列表 -->
        <div class="product-list" v-loading="loading">
          <div v-if="products.length === 0" class="empty-state">
            <el-empty description="暂无商品" />
          </div>
          
          <div v-else class="product-grid">
            <div 
              v-for="p in products" 
              :key="p.id" 
              class="product-card"
              @click="$router.push(`/products/${p.id}`)"
            >
              <div class="product-img-wrap">
                <img :src="p.mainImage" :alt="p.name" />
                <!-- 标签 -->
                <div class="product-tag hot" v-if="p.isHot">热销</div>
                <div class="product-tag new" v-if="p.isNew">新品</div>
              </div>
              <div class="product-info">
                <h4>{{ p.name }}</h4>
                <div class="product-price">
                  <span class="price-current">¥{{ p.price }}</span>
                  <span class="price-original" v-if="p.originalPrice > p.price">
                    ¥{{ p.originalPrice }}
                  </span>
                </div>
                <div class="product-meta">
                  <span>已售 {{ p.sales || 0 }}</span>
                  <span>库存 {{ p.stock || 0 }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <el-pagination
            v-if="total > 0"
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :total="total"
            :page-sizes="[12, 24, 48]"
            layout="total, sizes, prev, pager, next"
            @size-change="load"
            @current-change="load"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getCategoriesApi, getProductsApi, getAnimeIpsApi } from '@/api/product'

const categories = ref([])
const animeIps = ref([])
const products = ref([])
const total = ref(0)
const loading = ref(false)

const query = ref({ 
  categoryId: '', 
  animeIpId: '', 
  keyword: '', 
  minPrice: null, 
  maxPrice: null, 
  sortBy: 'createTime', 
  order: 'desc', 
  page: 1, 
  size: 12 
})

// 加载数据
const load = async () => {
  loading.value = true
  try {
    const params = { ...query.value }
    if (!params.animeIpId) delete params.animeIpId
    if (!params.categoryId) delete params.categoryId
    const res = await getProductsApi(params)
    products.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

// 分类筛选
const onCategory = (id) => {
  query.value.categoryId = id || ''
  query.value.page = 1
  load()
}

// IP筛选
const onAnimeIp = (id) => {
  query.value.animeIpId = id || ''
  query.value.page = 1
  load()
}

onMounted(async () => {
  categories.value = await getCategoriesApi()
  animeIps.value = await getAnimeIpsApi()
  load()
})
</script>
```

**3. 商品详情页实现**

```vue
<!-- frontend/src/views/ProductDetail.vue -->
<template>
  <div class="product-detail" v-if="detail.product">
    <div class="detail-main">
      <!-- 左侧图片区 -->
      <div class="gallery-section">
        <div class="main-image">
          <img :src="currentImage" :alt="detail.product.name" />
        </div>
        <div class="thumbnail-list" v-if="detail.images?.length > 1">
          <div 
            v-for="img in detail.images" 
            :key="img.id"
            class="thumbnail-item"
            :class="{ active: currentImage === img.imageUrl }"
            @click="currentImage = img.imageUrl"
          >
            <img :src="img.imageUrl" />
          </div>
        </div>
      </div>

      <!-- 右侧信息区 -->
      <div class="info-section">
        <h1 class="product-title">{{ detail.product.name }}</h1>
        
        <!-- 价格展示 -->
        <div class="price-box">
          <span class="price-label">价格</span>
          <span class="price-value">¥{{ detail.product.price }}</span>
          <span class="original-price" v-if="detail.product.originalPrice > detail.product.price">
            ¥{{ detail.product.originalPrice }}
          </span>
        </div>

        <!-- 基础信息 -->
        <div class="info-row">
          <span class="info-label">销量</span>
          <span>{{ detail.product.sales || 0 }} 件</span>
        </div>
        <div class="info-row">
          <span class="info-label">库存</span>
          <span>{{ detail.product.stock }} 件</span>
        </div>

        <!-- 标签 -->
        <div class="info-row" v-if="detail.product.tags">
          <span class="info-label">标签</span>
          <el-tag v-for="tag in detail.product.tags.split(',')" :key="tag">
            {{ tag }}
          </el-tag>
        </div>

        <!-- 数量选择 -->
        <div class="quantity-row">
          <span class="info-label">数量</span>
          <el-input-number v-model="qty" :min="1" :max="detail.product.stock" />
          <span>件（库存{{ detail.product.stock }}件）</span>
        </div>

        <!-- 操作按钮 -->
        <div class="action-row">
          <el-button type="primary" size="large" @click="addCart">
            <el-icon><ShoppingCart /></el-icon>
            加入购物车
          </el-button>
          <el-button size="large" @click="favorite">
            <el-icon><Star /></el-icon>
            收藏
          </el-button>
        </div>
      </div>
    </div>

    <!-- Tab区域 -->
    <div class="detail-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="商品详情" name="detail">
          <div class="detail-content">
            <p>{{ detail.product.description || '暂无详情' }}</p>
          </div>
        </el-tab-pane>
        <el-tab-pane label="用户评价" name="reviews">
          <div class="reviews-content">
            <div v-for="review in detail.latestReviews" :key="review.id" class="review-item">
              <div class="review-header">
                <el-rate :model-value="review.rating" disabled show-score />
                <span>{{ formatTime(review.createTime) }}</span>
              </div>
              <div class="review-content" v-if="review.content">
                {{ review.content }}
              </div>
              <div class="review-images" v-if="review.images">
                <img v-for="img in parseReviewImages(review.images)" :key="img" :src="img" />
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Star } from '@element-plus/icons-vue'
import { getProductDetailApi } from '@/api/product'
import { addCartApi } from '@/api/cart'
import { addFavoriteApi } from '@/api/user'

const route = useRoute()
const detail = ref({ product: null, images: [] })
const qty = ref(1)
const currentImage = ref('')
const activeTab = ref('detail')

onMounted(async () => {
  detail.value = await getProductDetailApi(route.params.id)
  if (detail.value.images?.length > 0) {
    currentImage.value = detail.value.images[0].imageUrl
  }
})

// 加入购物车
const addCart = async () => {
  try {
    await addCartApi({ productId: detail.value.product.id, quantity: qty.value })
    ElMessage.success('已加入购物车')
  } catch (error) {
    // 错误已在拦截器处理
  }
}

// 收藏
const favorite = async () => {
  try {
    await addFavoriteApi(detail.value.product.id)
    ElMessage.success('收藏成功')
  } catch (error) {
    // 错误已在拦截器处理
  }
}
</script>
```

#### 界面效果展示

**商品列表页：**
```
┌─────────────────────────────────────────────────────────────────┐
│  📂 商品分类              🎭 动漫IP                    │
│  ├─ 全部分类              ├─ 全部IP                    │
│  ├─ 手办模型              ├─ 鬼灭之刃                  │
│  ├─ 服装配饰    ◀──选中    ├─ 进击的巨人                │
│  ├─ 文具用品              ├─ 咒术回战                  │
│  └─ 数码配件              └─ 间谍过家家                │
├─────────────────────────────────────────────────────────────────┤
│  🔍 [搜索商品...]    [0] - [999] 价格筛选    [综合排序 ▼] [搜索]│
│                                                                  │
│  已选筛选：📂 服装配饰 ✕   🎭 鬼灭之刃 ✕                      │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │   热销    │ │          │ │          │ │          │           │
│  │  [图片]   │ │  [图片]   │ │  [图片]   │ │  [图片]   │           │
│  │          │ │          │ │          │ │          │           │
│  │ 名称...  │ │ 名称...  │ │ 名称...  │ │ 名称...  │           │
│  │ ¥199  ¥299│ │ ¥89  ¥129│ │ ¥159     │ │ ¥299     │           │
│  │ 已售 128  │ │ 已售 56   │ │ 已售 89   │ │ 已售 234  │           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
│                                                                  │
│                    [< 1 2 3 4 5 >]                              │
└─────────────────────────────────────────────────────────────────┘
```

**商品详情页：**
```
┌─────────────────────────────────────────────────────────────────┐
│  ┌──────────────────┐                                          │
│  │                  │    鬼灭之刃 蝴蝶忍 cosplay 服装             │
│  │    [大图展示]      │    ━━━━━━━━━━━━━━━━━━━━━                 │
│  │                  │                                          │
│  │                  │    价格    ¥599    ¥799                   │
│  │                  │    ──────────────────────                 │
│  └──────────────────┘    销量    128 件                         │
│  ┌────┐┌────┐┌────┐     库存    56 件                           │
│  │图1 ││图2 ││图3 │     标签    鬼灭之刃 cosplay 限定           │
│  └────┘└────┘└────┘     ──────────────────────                 │
│                           数量    [-][1][+]  件（库存56件）     │
│                           ──────────────────────               │
│                           ╭─────────────────────╮              │
│                           │    🛒 加入购物车     │              │
│                           ╰─────────────────────╯              │
│                           ╭─────────╮╭─────────╮              │
│                           │  ⭐ 收藏 │              │
│                           ╰─────────╯              │
├─────────────────────────────────────────────────────────────────┤
│  [商品详情]  [用户评价]                                          │
│  ━━━━━━━━━━━━━━━━━━━━━━━━                                        │
│  鬼灭之刃 蝴蝶忍 cosplay服装，采用高品质面料制作...               │
│  [详情图片1]  [详情图片2]  [详情图片3]                           │
├─────────────────────────────────────────────────────────────────┤
│  用户评价                                                         │
│  ⭐⭐⭐⭐⭐  2024-01-15 19:30                                  │
│  质量很好，尺码标准，物流很快...                                  │
│  [晒图1] [晒图2]                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

### 2.3 购物车模块【添加/管理/结算】

#### 后端设计

**1. 接口设计**

| 接口名称 | HTTP方法 | 路径 | 请求参数 | 返回数据 |
|---------|----------|------|----------|----------|
| 购物车列表 | GET | /api/cart | 无 | List<CartVO> |
| 添加购物车 | POST | /api/cart/add | productId, quantity | 无 |
| 更新购物车 | PUT | /api/cart/update | id, quantity, selected | 无 |
| 选中状态 | POST | /api/cart/selected | id, selected | 无 |
| 删除商品 | DELETE | /api/cart/remove | id | 无 |
| 清空购物车 | DELETE | /api/cart/clear | 无 | 无 |

#### 前端实现

**1. API层封装**

```javascript
// frontend/src/api/cart.js
import request from '@/utils/request'

export const addCartApi = (data) => request.post('/cart/add', data)
export const getCartApi = () => request.get('/cart')
export const updateCartApi = (data) => request.put('/cart/update', data)
export const removeCartApi = (id) => request.delete('/cart/remove', { params: { id } })
export const clearCartApi = () => request.delete('/cart/clear')
export const selectedCartApi = (data) => request.post('/cart/selected', data)
```

**2. 购物车页面实现**

```vue
<!-- frontend/src/views/Cart.vue -->
<template>
  <div class="cart-page">
    <h2>我的购物车</h2>
    
    <div v-if="!list.length" class="empty-wrap">
      <div class="empty-icon">🛒</div>
      <p>购物车还是空的，快去挑选心仪的商品吧</p>
      <el-button type="primary" @click="$router.push('/products')">去购物</el-button>
    </div>
    
    <div v-else class="cart-content">
      <!-- 表头 -->
      <div class="cart-header">
        <div class="col-check">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
        </div>
        <div class="col-product">商品信息</div>
        <div class="col-price">单价</div>
        <div class="col-quantity">数量</div>
        <div class="col-total">小计</div>
        <div class="col-action">操作</div>
      </div>
      
      <!-- 商品列表 -->
      <div class="cart-list">
        <div v-for="item in list" :key="item.id" class="cart-item">
          <div class="col-check">
            <el-checkbox v-model="item.selected" @change="handleSelectedChange(item)" />
          </div>
          <div class="col-product">
            <div class="product-info" @click="$router.push(`/products/${item.productId}`)">
              <img :src="item.imageUrl" class="product-img" />
              <div class="product-name">{{ item.name }}</div>
            </div>
          </div>
          <div class="col-price">¥{{ item.price }}</div>
          <div class="col-quantity">
            <div class="quantity-box">
              <button @click="item.quantity > 1 && updateQty(item, -1)">-</button>
              <input type="text" :value="item.quantity" @change="updateQtyInput(item, $event)" />
              <button @click="updateQty(item, 1)">+</button>
            </div>
          </div>
          <div class="col-total">¥{{ item.totalPrice }}</div>
          <div class="col-action">
            <span @click="remove(item.id)">删除</span>
          </div>
        </div>
      </div>
      
      <!-- 底部结算栏 -->
      <div class="cart-footer">
        <div class="footer-left">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <span @click="clear">清空购物车</span>
        </div>
        <div class="footer-right">
          <div class="summary">
            <span>已选择 <em>{{ selectedCount }}</em> 件商品</span>
            <div class="total-box">
              <span>合计：</span>
              <span class="total-amount">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
          </div>
          <el-button 
            type="primary" 
            size="large" 
            :disabled="selectedCount === 0"
            @click="goToCheckout"
          >
            去结算
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  clearCartApi, getCartApi, removeCartApi, 
  updateCartApi, selectedCartApi 
} from '@/api/cart'

const router = useRouter()
const list = ref([])

// 加载购物车
const load = async () => {
  list.value = await getCartApi()
}

// 更新数量
const updateQty = async (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty < 1) return
  try {
    await updateCartApi({ id: item.id, quantity: newQty, selected: item.selected })
    item.quantity = newQty
    item.totalPrice = (item.price || 0) * newQty
  } catch {
    ElMessage.error('更新失败')
  }
}

// 删除商品
const remove = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？')
    await removeCartApi(id)
    await load()
    ElMessage.success('删除成功')
  } catch {}
}

// 全选/取消全选
const handleSelectAll = async (value) => {
  list.value.forEach(item => {
    item.selected = value
    selectedCartApi({ id: item.id, selected: value ? 1 : 0 })
  })
}

// 跳转结算
const goToCheckout = () => {
  const selectedIds = list.value
    .filter(item => item.selected)
    .map(item => item.id)
    .join(',')
  router.push(`/checkout?cartIds=${selectedIds}`)
}

// 计算属性
const selectedCount = computed(() => 
  list.value.filter(item => item.selected).length
)

const totalPrice = computed(() => 
  list.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + (item.totalPrice || 0), 0)
)

onMounted(load)
</script>
```

#### 界面效果展示

```
┌─────────────────────────────────────────────────────────────────┐
│  我的购物车                                                       │
├─────────────────────────────────────────────────────────────────┤
│  ☐ 全选    商品信息              单价      数量      小计    操作 │
├─────────────────────────────────────────────────────────────────┤
│  ☑        [图] 商品名称1          ¥199    [-][3][+]  ¥597    删除│
│  ☑        [图] 商品名称2          ¥89     [-][1][+]  ¥89     删除│
│  ☐        [图] 商品名称3          ¥159    [-][2][+]  ¥318    删除│
├─────────────────────────────────────────────────────────────────┤
│  ☐ 全选                      清空购物车                            │
├─────────────────────────────────────────────────────────────────┤
│  已选择 2 件商品                                                  │
│  合计：¥686                           ╭─────────────────────╮   │
│                                        │      去 结 算        │   │
│                                        ╰─────────────────────╯   │
└─────────────────────────────────────────────────────────────────┘
```

**交互效果说明：**
- 点击商品图片/名称 → 跳转到商品详情页
- 点击 +/- 按钮 → 实时更新数量和价格
- 点击选中框 → 实时计算选中数量和总价
- 点击"删除" → 弹出确认框，确认后删除
- 点击"清空购物车" → 弹出确认框，确认后清空
- 点击"去结算" → 跳转到结算页面，传递选中的购物车ID

---

### 2.4 订单模块【创建/支付/管理】

#### 后端设计

**1. 接口设计**

| 接口名称 | HTTP方法 | 路径 | 请求参数 | 返回数据 |
|---------|----------|------|----------|----------|
| 创建订单 | POST | /api/orders | addressId, payType, remark | OrderVO |
| 订单列表 | GET | /api/orders | status, page, size | Page<OrderVO> |
| 订单详情 | GET | /api/orders/{id} | 无 | OrderVO |
| 取消订单 | DELETE | /api/orders/{id} | 无 | 无 |
| 支付订单 | POST | /api/orders/{id}/pay | 无 | 无 |
| 确认收货 | PUT | /api/orders/{id}/receive | 无 | 无 |

**2. 后端核心代码**

```java
// OrderBizServiceImpl.java - 订单业务实现
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
    public OrderVO createFromSelectedCart(Long userId, CreateOrderRequest request) {
        // 1. 获取选中的购物车项
        List<Cart> carts = cartService.list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, 1));
        if (carts.isEmpty()) {
            throw new BusinessException("no selected cart items");
        }
        
        // 2. 计算总价并扣减库存
        BigDecimal total = BigDecimal.ZERO;
        for (Cart c : carts) {
            Product p = productService.getById(c.getProductId());
            if (p == null || p.getStatus() == 0) {
                throw new BusinessException("product unavailable: " + c.getProductId());
            }
            // 扣减库存
            int ok = productMapper.deductStock(p.getId(), c.getQuantity());
            if (ok == 0) {
                throw new BusinessException("stock not enough: " + p.getName());
            }
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())));
        }
        
        // 3. 创建订单
        Order order = new Order();
        order.setOrderNo("AN" + System.currentTimeMillis() + userId);  // 订单号
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setPayAmount(total);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayType(request.getPayType() != null ? request.getPayType() : 1);
        order.setStatus(1);  // 待支付
        order.setAddressId(request.getAddressId());
        order.setRemark(request.getRemark());
        orderService.save(order);
        
        // 4. 创建订单项
        for (Cart c : carts) {
            Product p = productService.getById(c.getProductId());
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(p.getId());
            item.setProductName(p.getName());
            item.setProductImage(p.getMainImage());
            item.setPrice(p.getPrice());
            item.setQuantity(c.getQuantity());
            item.setTotalAmount(p.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())));
            orderItemService.save(item);
        }
        
        // 5. 清空已选购物车
        cartService.remove(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, 1));
        
        // 6. 返回订单信息
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setItems(orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())));
        
        return orderVO;
    }
    
    // 支付订单
    @Override
    public void pay(Long userId, Long orderId) {
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException("order not found");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("only unpaid order can be paid");
        }
        order.setStatus(2);  // 已支付
        order.setPaymentTime(LocalDateTime.now());
        orderService.updateById(order);
    }
    
    // 确认收货
    @Override
    public void receive(Long userId, Long orderId) {
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException(404, "order not found");
        }
        if (order.getStatus() != 3) {
            throw new BusinessException("only shipped order can be received");
        }
        order.setStatus(4);  // 已完成
        order.setReceiveTime(LocalDateTime.now());
        orderService.updateById(order);
    }
}
```

#### 前端实现

**1. API层封装**

```javascript
// frontend/src/api/order.js
import request from '@/utils/request'

export const createOrderApi = (data) => request.post('/orders', data)
export const payOrderApi = (id) => request.post(`/orders/${id}/pay`)
export const getOrdersApi = (params) => request.get('/orders', { params })
export const getOrderDetailApi = (id) => request.get(`/orders/${id}`)
export const cancelOrderApi = (id) => request.put(`/orders/${id}/cancel`)
export const receiveOrderApi = (id) => request.put(`/orders/${id}/receive`)
```

**2. 订单页面实现**

```vue
<!-- frontend/src/views/Orders.vue -->
<template>
  <div class="orders-page">
    <h2>我的订单</h2>
    
    <!-- 状态Tab -->
    <div class="tabs-nav">
      <div :class="{ active: activeStatus === '' }" @click="changeStatus('')">全部订单</div>
      <div :class="{ active: activeStatus === '1' }" @click="changeStatus('1')">待付款</div>
      <div :class="{ active: activeStatus === '2' }" @click="changeStatus('2')">待发货</div>
      <div :class="{ active: activeStatus === '3' }" @click="changeStatus('3')">待收货</div>
      <div :class="{ active: activeStatus === '4' }" @click="changeStatus('4')">已完成</div>
      <div :class="{ active: activeStatus === 'after_sale' }" @click="changeStatus('after_sale')">售后</div>
    </div>

    <!-- 订单列表 -->
    <div class="orders-list">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <span>{{ order.orderNo }}</span>
          <span>{{ formatTime(order.createTime) }}</span>
          <span class="status-text" :class="'status-' + order.status">
            {{ getStatusText(order.status) }}
          </span>
        </div>
        
        <div class="order-body">
          <div v-for="item in order.items" :key="item.id" class="product-row">
            <img :src="item.productImage" class="product-img" />
            <div class="product-name">{{ item.productName }}</div>
            <div class="product-price">¥{{ item.price }}</div>
            <div class="product-qty">×{{ item.quantity }}</div>
          </div>
          
          <div class="order-summary">
            <div>商品总额 ¥{{ order.totalAmount }}</div>
            <div>运费 ¥{{ order.freightAmount }}</div>
            <div class="total">实付金额 ¥{{ order.payAmount }}</div>
          </div>
        </div>
        
        <div class="order-footer">
          <div>
            <span>支付方式: {{ getPayTypeText(order.payType) }}</span>
          </div>
          <div class="actions">
            <!-- 根据状态显示不同按钮 -->
            <el-button v-if="order.status === 1" type="primary" @click="handlePay(order)">立即支付</el-button>
            <el-button v-if="order.status === 1" @click="handleCancel(order)">取消订单</el-button>
            <el-button v-if="order.status === 3" type="success" @click="handleReceive(order)">确认收货</el-button>
            <el-button v-if="order.status === 4" @click="handleAfterSale(order)">申请售后</el-button>
            <el-button v-if="order.status === 4" type="primary" @click="handleReview(order)">商品评价</el-button>
            <el-button @click="handleDetail(order)">查看详情</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 支付弹窗 -->
    <el-dialog v-model="payDialogVisible" title="订单支付" width="450px">
      <div class="pay-amount">
        支付金额: ¥{{ currentOrder?.payAmount }}
      </div>
      <div class="pay-methods">
        <div 
          v-for="method in [{id:1,name:'支付宝'},{id:2,name:'微信支付'},{id:3,name:'余额支付'}]"
          :key="method.id"
          :class="{ active: payForm.payType === method.id }"
          @click="payForm.payType = method.id"
        >
          {{ method.name }}
        </div>
      </div>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getOrdersApi, payOrderApi, cancelOrderApi, receiveOrderApi 
} from '@/api/order'

const activeStatus = ref('')
const orders = ref([])
const payDialogVisible = ref(false)
const currentOrder = ref(null)
const payForm = ref({ payType: 1 })

// 加载订单
const loadOrders = async () => {
  const result = await getOrdersApi({
    status: activeStatus.value || undefined,
    page: 1,
    size: 10
  })
  orders.value = result.records || []
}

// 状态文本
const getStatusText = (status) => {
  const map = { 0:'已取消', 1:'待付款', 2:'待发货', 3:'待收货', 4:'已完成' }
  return map[status] || '未知'
}

const getPayTypeText = (payType) => {
  const map = { 1:'支付宝', 2:'微信支付', 3:'余额支付' }
  return map[payType] || '未选择'
}

// 支付
const handlePay = (order) => {
  currentOrder.value = order
  payDialogVisible.value = true
}

const confirmPay = async () => {
  try {
    await ElMessageBox.confirm('确认支付该订单？')
    await payOrderApi(currentOrder.value.id)
    ElMessage.success('支付成功')
    payDialogVisible.value = false
    loadOrders()
  } catch {}
}

// 取消
const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？')
    await cancelOrderApi(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch {}
}

// 确认收货
const handleReceive = async (order) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？')
    await receiveOrderApi(order.id)
    ElMessage.success('确认收货成功')
    loadOrders()
  } catch {}
}

onMounted(loadOrders)
</script>
```

#### 界面效果展示

```
┌─────────────────────────────────────────────────────────────────┐
│  我的订单                                                       │
├─────────────────────────────────────────────────────────────────┤
│  [全部订单]  [待付款]  [待发货]  [待收货]  [已完成]  [售后]      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ AN1705320000001      2024-01-15 10:30      🔴 待付款      │  │
│  ├───────────────────────────────────────────────────────────┤  │
│  │ [图] 商品1  ¥199    ×1                                    │  │
│  │ [图] 商品2  ¥89     ×2    │ 商品总额 ¥377                  │  │
│  │                            │ 运费 ¥0                       │  │
│  │                            │ 实付金额 ¥377                 │  │
│  ├───────────────────────────────────────────────────────────┤  │
│  │ 支付方式: 支付宝                    [立即支付] [取消订单]   │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ AN1705310000002      2024-01-14 15:20      🟢 已完成      │  │
│  ├───────────────────────────────────────────────────────────┤  │
│  │ [图] 商品3  ¥599    ×1    │ 商品总额 ¥599                  │  │
│  │                            │ 实付金额 ¥599                 │  │
│  ├───────────────────────────────────────────────────────────┤  │
│  │ 支付方式: 微信支付                [申请售后] [商品评价]     │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐
│           订单支付                   │
├─────────────────────────────────────┤
│                                     │
│         支付金额 ¥377               │
│                                     │
│  ┌───────────────────────────────┐  │
│  │  ● 支付宝                      │  │
│  ├───────────────────────────────┤  │
│  │    微信支付                    │  │
│  ├───────────────────────────────┤  │
│  │    余额支付                    │  │
│  └───────────────────────────────┘  │
│                                     │
│         [取消]    [确认支付]         │
└─────────────────────────────────────┘
```

**订单状态流转：**
```
    ┌─────────────────────────────────────────────────────────────┐
    │                                                             │
    │    待支付(1) ──[支付]──→ 已支付(2) ──[发货]──→ 已发货(3)   │
    │        │                          │                │       │
    │        │                          │                │       │
    │    [取消]                      [退款]            [收货]     │
    │        │                          │                │       │
    │        ▼                          ▼                ▼       │
    │    已取消(0)                   退款中(5) ──[处理]──→ 已退款(6)
    │                                                             │
    │                              已收货(4) ─────────────────┘    │
    │                                                             │
    └─────────────────────────────────────────────────────────────┘
```

---

### 2.5 后台管理模块【商品管理】

#### 后端设计

**1. 接口设计**

| 接口名称 | HTTP方法 | 路径 | 请求参数 | 返回数据 |
|---------|----------|------|----------|----------|
| 商品列表 | GET | /api/admin/products | page, size | Page<Product> |
| 创建商品 | POST | /api/admin/products | Product | 无 |
| 更新商品 | PUT | /api/admin/products/{id} | Product | 无 |
| 删除商品 | DELETE | /api/admin/products/{id} | 无 | 无 |
| 更新状态 | PUT | /api/admin/products/{id}/status | status | 无 |

#### 前端实现

**1. API层封装**

```javascript
// frontend/src/api/admin.js
import request from '@/utils/request'

export const adminProductsApi = (params) => request.get('/admin/products', { params })
export const adminCreateProductApi = (data) => request.post('/admin/products', data)
export const adminUpdateProductApi = (data) => request.put(`/admin/products/${data.id}`, data)
export const adminDeleteProductApi = (id) => request.delete(`/admin/products/${id}`)
```

**2. 后台商品管理页面**

```vue
<!-- frontend/src/views/admin/AdminProducts.vue -->
<template>
  <div class="admin-page">
    <div class="page-header">
      <h3>商品管理</h3>
      <el-button type="primary" @click="open()">+ 新增商品</el-button>
    </div>

    <!-- 商品列表表格 -->
    <div class="table-wrap">
      <el-table :data="list.records || []" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="mainImage" label="图片" width="80">
          <template #default="{ row }">
            <img :src="row.mainImage" class="product-img" v-if="row.mainImage" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="180" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80">
          <template #default="{ row }">
            <span :class="{ 'low-stock': row.stock < 10 }">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <span :class="row.status === 1 ? 'on' : 'off'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <span class="action-btn" @click="open(row)">编辑</span>
            <span :class="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </span>
            <span class="danger" @click="remove(row.id)">删除</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="list.total || 0"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="load"
        @current-change="load"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="售价" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-cascader
                v-model="form.categoryId"
                :options="categoryTree"
                :props="{ value:'id', label:'name', checkStrictly:true }"
                placeholder="请选择分类"
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="商品主图" prop="mainImage">
          <el-input v-model="form.mainImage" placeholder="请输入图片URL" />
          <img v-if="form.mainImage" :src="form.mainImage" class="image-preview" />
        </el-form-item>
        
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="是否热销">
              <el-switch v-model="form.isHot" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否新品">
              <el-switch v-model="form.isNew" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  adminProductsApi, adminCreateProductApi, 
  adminUpdateProductApi, adminDeleteProductApi 
} from '@/api/admin'

const list = ref({})
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const dialogVisible = ref(false)
const formRef = ref()

const defaultForm = {
  id: null, name: '', description: '', price: 0, 
  originalPrice: 0, stock: 0, mainImage: '', categoryId: null,
  tags: '', isHot: 0, isNew: 0, status: 1
}
const form = ref({ ...defaultForm })

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

// 加载数据
const load = async () => {
  loading.value = true
  try {
    list.value = await adminProductsApi({ page: page.value, size: size.value })
  } finally {
    loading.value = false
  }
}

// 打开弹窗
const open = (row) => {
  form.value = row ? { ...row } : { ...defaultForm }
  dialogVisible.value = true
}

// 提交
const submit = async () => {
  try {
    await formRef.value.validate()
    if (form.value.id) {
      await adminUpdateProductApi(form.value)
      ElMessage.success('更新成功')
    } else {
      await adminCreateProductApi(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    load()
  } catch {
    ElMessage.error('操作失败')
  }
}

// 切换状态
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.put(`/admin/products/${row.id}/status`, null, { params: { status: newStatus } })
    row.status = newStatus
    ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
  } catch {}
}

// 删除
const remove = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？')
    await adminDeleteProductApi(id)
    ElMessage.success('删除成功')
    load()
  } catch {}
}

onMounted(load)
</script>
```

#### 界面效果展示

```
┌─────────────────────────────────────────────────────────────────┐
│  商品管理                          [+ 新增商品]                   │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────┬─────┬────────────┬──────┬──────┬──────┬──────┬────────┐│
│  │ ID  │图片 │ 商品名称   │ 价格 │ 库存 │ 销量 │ 状态 │ 操作   ││
│  ├─────┼─────┼────────────┼──────┼──────┼──────┼──────┼────────┤│
│  │ 1   │ [图]│ 手办模型1  │ ¥199 │  56  │ 128  │ 🟢   │编辑下架││
│  │ 2   │ [图]│ 服装配饰2  │  89  │  23  │  56  │ 🔴   │编辑上架││
│  │ 3   │ [图]│ 文具用品3  │ 159  │   5  │  89  │ 🟢   │编辑下架││
│  └─────┴─────┴────────────┴──────┴──────┴──────┴──────┴────────┘│
│                                                                 │
│                    [< 1 2 3 4 5 >]  共 128 条                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │                    编辑商品                               │  │
│  ├─────────────────────────────────────────────────────────┤  │
│  │  商品名称   [鬼灭之刃 蝴蝶忍 cosplay 服装                ] │  │
│  │  ─────────────────────────────────────────────────────  │  │
│  │  售价       [199        ]   原价   [299        ]         │  │
│  │  ─────────────────────────────────────────────────────  │  │
│  │  库存       [56         ]   分类   [服装配饰 ▼]          │  │
│  │  ─────────────────────────────────────────────────────  │  │
│  │  动漫IP     [鬼灭之刃 ▼]                                │  │
│  │  ─────────────────────────────────────────────────────  │  │
│  │  商品主图   [https://example.com/image.jpg     ]        │  │
│  │             [预览图]                                      │  │
│  │  ─────────────────────────────────────────────────────  │  │
│  │  标签       [鬼灭之刃,cosplay,限定]                     │  │
│  │  ─────────────────────────────────────────────────────  │  │
│  │  是否热销   [开关]    是否新品  [开关]    状态  [开关]   │  │
│  │                                                         │  │
│  │                      [取消]    [确定]                   │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 三、完整功能链路说明

### 3.1 用户购物流程链路

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│  【浏览商品】                                                            │
│  ┌──────────────┐                                                       │
│  │   首页       │                                                       │
│  │  /          │                                                       │
│  └──────┬───────┘                                                       │
│         │                                                                │
│         ▼                                                                │
│  ┌──────────────┐                                                       │
│  │  商品列表页   │                                                       │
│  │ /products   │◄──────────────────────┐                              │
│  └──────┬───────┘                       │                              │
│         │                                │                              │
│         ├───────────────────────────────┤                              │
│         │                                │                              │
│         ▼                                ▼                              │
│  ┌──────────────┐              ┌──────────────┐                       │
│  │ 分类筛选     │              │ 关键词搜索   │                       │
│  │ categoryId  │              │ keyword     │                       │
│  └──────┬───────┘              └──────┬───────┘                       │
│         │                                │                              │
│         └───────────────────────────────┼──────────────────────────┐   │
│                                         │                          │   │
│                                         ▼                          │   │
│                                  ┌──────────────┐                  │   │
│                                  │  商品详情页   │                  │   │
│                                  │ /products/1 │──────────────────┘   │
│                                  └──────┬───────┘                      │
│                                         │                              │
│                    ┌────────────────────┼────────────────────┐      │
│                    │                    │                    │      │
│                    ▼                    ▼                    ▼      │
│             ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│             │  加入购物车   │     │ 收藏商品      │     │ 查看评价      │
│             │ POST /cart   │     │ POST /fav    │     │ GET /reviews │
│             └──────┬───────┘     └──────┬───────┘     └──────────────┘
│                    │                    │
│                    ▼                    │
│             ┌──────────────┐            │
│             │  购物车页面   │            │
│             │  /cart       │            │
│             └──────┬───────┘            │
│                    │                    │
│                    ▼                    │
│             ┌──────────────┐            │
│             │  结算页面     │            │
│             │ /checkout    │────────────┘
│             └──────┬───────┘
│                    │
│                    ▼
│             ┌──────────────┐
│             │  提交订单     │
│             │ POST /orders │
│             └──────┬───────┘
│                    │
│                    ▼
│             ┌──────────────┐
│             │  支付页面     │
│             │ /payment/1   │
│             └──────┬───────┘
│                    │
│                    ▼
│             ┌──────────────┐
│             │  支付成功    │
│             │ POST /pay    │
│             └──────┬───────┘
│                    │
│                    ▼
│             ┌──────────────┐
│             │  订单列表     │
│             │  /orders     │
│             └──────────────┘
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3.2 管理员商品管理链路

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│  【管理员登录】                                                          │
│  ┌──────────────┐                                                       │
│  │  登录页      │                                                       │
│  │ /login      │                                                       │
│  │ (Tab:管理员) │                                                       │
│  └──────┬───────┘                                                       │
│         │                                                                │
│         ▼                                                                │
│  ┌──────────────┐                                                       │
│  │ JWT认证     │                                                       │
│  │ role=ADMIN  │                                                       │
│  └──────┬───────┘                                                       │
│         │                                                                │
│         ▼                                                                │
│  ┌──────────────┐                                                       │
│  │ 后台首页      │                                                       │
│  │ /admin       │                                                       │
│  └──────┬───────┘                                                       │
│         │                                                                │
│         ▼                                                                │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    后台管理菜单                                    │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │   │
│  │  │ 商品管理  │ │ 分类管理  │ │ 订单管理  │ │ 用户管理  │           │   │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘           │   │
│  │       │            │            │            │                  │   │
│  │       ▼            ▼            ▼            ▼                  │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │   │
│  │  │ 商品列表  │ │ 分类列表  │ │ 订单列表  │ │ 用户列表  │           │   │
│  │  │ GET /adm │ │ GET /adm │ │ GET /adm │ │ GET /adm │           │   │
│  │  │ /products│ │ /cats    │ │ /orders │ │ /users   │           │   │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘           │   │
│  │       │            │            │            │                  │   │
│  │       └────────────┴────────────┴────────────┘                  │   │
│  │                           │                                       │   │
│  │                           ▼                                       │   │
│  │  ┌──────────────────────────────────────────────────────────┐   │   │
│  │  │              CRUD 操作                                    │   │   │
│  │  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐        │   │   │
│  │  │  │  新增   │ │  编辑   │ │  删除   │ │ 状态切换│        │   │   │
│  │  │  │ POST   │ │ PUT    │ │ DELETE │ │ PUT    │        │   │   │
│  │  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘        │   │   │
│  │  └──────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 四、关键技术实现总结

### 4.1 JWT认证机制

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│  登录流程：                                                       │
│                                                                 │
│  1. 用户提交 credentials                                          │
│         │                                                        │
│         ▼                                                        │
│  2. 后端验证用户名密码                                            │
│         │                                                        │
│         ▼                                                        │
│  3. 生成JWT Token                                                │
│     { userId: 1, role: "USER", exp: 24h }                       │
│         │                                                        │
│         ▼                                                        │
│  4. 返回Token给前端                                               │
│     { token: "eyJhbGciOiJIUzI1NiJ9..." }                       │
│         │                                                        │
│         ▼                                                        │
│  5. 前端存储Token                                                 │
│     localStorage.setItem('token', token)                        │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  请求认证流程：                                                    │
│                                                                 │
│  前端请求 ──► 请求拦截器 ──► 添加Header                          │
│               Authorization: Bearer xxx                          │
│                        │                                         │
│                        ▼                                         │
│               后端JwtAuthenticationFilter                        │
│                        │                                         │
│                        ▼                                         │
│               验证Token有效 ──► 放行 ──► Controller              │
│                        │                                         │
│                        ▼                                         │
│               Token无效/过期 ──► 401响应 ──► 前端跳转登录        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 数据一致性保证

```java
// 订单创建使用事务保证数据一致性
@Override
@Transactional  // 事务注解
public OrderVO createFromSelectedCart(Long userId, CreateOrderRequest request) {
    // 1. 验证购物车项
    // 2. 扣减库存（原子操作）
    int ok = productMapper.deductStock(p.getId(), c.getQuantity());
    if (ok == 0) {
        throw new BusinessException("库存不足");  // 抛异常则回滚
    }
    
    // 3. 创建订单
    orderService.save(order);
    
    // 4. 创建订单项
    orderItemService.save(item);
    
    // 5. 清空已选购物车
    cartService.remove(...);
    
    // 整个方法成功才提交事务，任何一步失败都会回滚
}
```

### 4.3 前端路由守卫

```javascript
// frontend/src/router/index.js
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from) => {
  const userStore = useUserStore()
  
  // 需要登录的页面
  if (to.meta?.auth && !userStore.isLogin) {
    return '/login'  // 未登录跳转登录页
  }
  
  // 需要管理员权限的页面
  if (to.meta?.admin && userStore.role !== 'ADMIN') {
    return '/login?tab=admin'  // 非管理员跳转登录页并打开管理员Tab
  }
  
  return true  // 放行
})

export default router
```

---

## 五、数据库表关系图

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    user     │       │    cart     │       │   address   │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │──┐    │ id (PK)     │       │ id (PK)     │
│ username    │  │    │ user_id(FK) │──────►│ user_id(FK) │──►user
│ password    │  └───►│ product_id  │       │ receiver    │
│ phone       │       │ quantity    │       │ phone       │
│ avatar      │       │ selected    │       │ address     │
└─────────────┘       └─────────────┘       └─────────────┘
                            │
                            ▼
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   product   │       │    order    │       │ order_item  │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │◄──────│ id (PK)     │──┐    │ id (PK)     │
│ name        │       │ order_no    │  │    │ order_id(FK)│──►order
│ price       │       │ user_id(FK) │──┼───►│ product_id  │──►product
│ stock       │       │ total_amount│  │    │ price       │
│ category_id │──────│ status      │  │    │ quantity    │
│ anime_ip_id │──────│ address_id  │──┘    └─────────────┘
└─────────────┘       └─────────────┘
       │
       ▼
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  category   │       │   review    │       │  favorite   │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │       │ id (PK)     │       │ id (PK)     │
│ name        │       │ user_id(FK) │──►user│ user_id(FK) │──►user
│ parent_id   │       │ product_id  │──►product│ product_id │──►product
│ sort_order  │       │ rating      │       └─────────────┘
└─────────────┘       │ content     │
                      └─────────────┘

┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   anime_ip  │       │ after_sale  │       │    banner   │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │       │ id (PK)     │       │ id (PK)     │
│ name        │       │ user_id(FK) │──►user│ title       │
│ description │       │ order_id(FK)│──►order│ image       │
│ logo        │       │ type        │       │ link        │
└─────────────┘       │ status      │       │ sort_order  │
                      └─────────────┘       └─────────────┘
```

---

*文档更新时间: 2026-05-15*
