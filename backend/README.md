# 农产品直供平台后端

Spring Boot 2.7 + MyBatis-Plus + MySQL + JWT

## 项目结构

```
vuehouduan/
├── pom.xml
├── sql/
│   └── init.sql           # 数据库初始化脚本
└── src/main/
    ├── java/com/example/vuehouduan/
    │   ├── VuehouduanApplication.java    # 启动类
    │   ├── common/                      # 公共组件
    │   │   ├── Result.java              # 统一响应格式
    │   │   ├── PageResult.java          # 分页响应
    │   │   ├── LoginRequest.java        # 登录请求
    │   │   ├── RegisterRequest.java     # 注册请求
    │   │   └── GlobalExceptionHandler.java
    │   ├── config/                      # 配置类
    │   │   ├── CorsConfig.java          # 跨域配置
    │   │   ├── MybatisPlusConfig.java   # MP配置
    │   │   ├── SwaggerConfig.java       # Swagger文档
    │   │   └── WebMvcConfig.java        # Web配置
    │   ├── controller/                  # 控制器层
    │   ├── service/                     # 服务层
    │   ├── mapper/                      # 数据访问层
    │   ├── entity/                       # 实体类
    │   ├── interceptor/                 # JWT拦截器
    │   └── util/                        # 工具类
    └── resources/
        └── application.yml               # 应用配置
```

## 快速开始

### 1. 初始化数据库

```sql
mysql -u root -p
source sql/init.sql;
```

或手动执行 `sql/init.sql` 中的 SQL 语句。

### 2. 修改配置文件

编辑 `src/main/resources/application.yml`，确认数据库连接信息：
- 数据库：`agricultural_ecommerce`
- 用户名：`root`
- 密码：`123456@MySQL.txt`
- 端口：`3306`

### 3. 启动项目

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或打包后运行
mvn clean package -DskipTests
java -jar target/vuehouduan-1.0.0.jar
```

### 4. 访问接口

- 后端地址：http://localhost:9090
- Swagger文档：http://localhost:9090/doc.html

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| qhj | 123456 | 普通用户 |
| farmer1 | 123456 | 商家 |
| business1 | 123456 | 商家 |

## API 接口

### 认证
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/logout` - 用户登出

### 用户
- `GET /api/users` - 获取用户列表
- `GET /api/users/{id}` - 获取用户详情
- `PUT /api/users/{id}` - 更新用户信息
- `DELETE /api/users/{id}` - 删除用户

### 商品
- `GET /api/products` - 获取商品列表
- `GET /api/products/{id}` - 获取商品详情
- `POST /api/products` - 创建商品
- `PUT /api/products/{id}` - 更新商品
- `DELETE /api/products/{id}` - 删除商品
- `GET /api/products/hot` - 获取热门商品
- `GET /api/products/new` - 获取新品
- `GET /api/products/recommended` - 获取推荐商品

### 订单
- `POST /api/orders` - 创建订单
- `GET /api/orders` - 获取订单列表
- `GET /api/orders/{id}` - 获取订单详情
- `PUT /api/orders/{id}/pay` - 支付订单
- `PUT /api/orders/{id}/cancel` - 取消订单
- `PUT /api/orders/{id}/confirm` - 确认收货

### 购物车
- `GET /api/cart` - 获取购物车
- `POST /api/cart` - 添加到购物车
- `PUT /api/cart/{id}` - 更新数量
- `DELETE /api/cart/{id}` - 删除商品
- `DELETE /api/cart/clear` - 清空购物车

### 新闻
- `GET /api/news` - 获取新闻列表
- `GET /api/news/{id}` - 获取新闻详情
- `POST /api/news` - 创建新闻
- `PUT /api/news/{id}` - 更新新闻
- `DELETE /api/news/{id}` - 删除新闻

### 收藏
- `GET /api/favorites` - 获取收藏列表
- `POST /api/favorites` - 添加收藏
- `DELETE /api/favorites/{id}` - 取消收藏

### 评价
- `GET /api/products/{id}/reviews` - 获取商品评价
- `POST /api/orders/{orderId}/reviews` - 添加评价

### 登录历史
- `GET /api/login-history` - 获取登录历史
- `POST /api/login-history` - 记录登录
- `PUT /api/login-history/{id}/logout` - 记录登出

### 系统管理
- `GET /api/admin/system/status` - 服务器状态
- `GET /api/admin/system/performance` - 性能指标
- `GET /api/admin/logs` - 日志列表
- `POST /api/admin/backup` - 数据备份
- `GET /api/admin/backup` - 备份列表
