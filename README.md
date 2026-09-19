# 绿源农鲜 - 智慧农产品电商平台

> 一套基于前后端分离架构的农产品电商系统，支持普通用户、商家、管理员三类角色，覆盖商品浏览、订单交易、售后服务、营销活动、系统监控等完整业务链路。
项目文档请看https://huaxuyimeng.github.io/agricultural-ecommerce/

## ✨ 项目特色

- 🌾 **三端协同**：用户端、商家端、管理端三种角色，一套后端统一支撑
- 🛒 **完整业务**：商品、订单、购物车、收藏、优惠券、售后、物流跟踪、登录历史、新闻公告等
- 🔐 **安全可靠**：JWT 无状态认证 + BCrypt 密码加密 + 细粒度 RBAC 权限控制
- 📊 **可视化**：ECharts 数据统计、报表中心、服务器状态、性能监控
- 🧰 **工程化**：Swagger/Knife4j 接口文档、统一响应、全局异常处理、操作日志
- 🐛 **课程项目**：注释完整、文档详尽，适合作为前后端分离教学案例

## 🧱 技术栈

### 前端
| 技术 | 版本 | 说明 |
| --- | --- | --- |
| Vue | 2.6.14 | 渐进式前端框架 |
| Vue Router | 3.5 | 路由 + 权限守卫 |
| Element UI | 2.15 | 后台组件库 |
| Axios | 1.5 | HTTP 客户端 |
| ECharts | 5.4 | 数据可视化 |
| WangEditor | 4.7 | 富文本编辑器（公告/新闻） |
| @vue/composition-api | 1.7 | Composition API 支持 |

### 后端
| 技术 | 版本 | 说明 |
| --- | --- | --- |
| Spring Boot | 2.7.18 | Web 应用框架 |
| MyBatis-Plus | 3.5.3 | ORM + 分页 + 逻辑删除 |
| MySQL | 8.0 | 数据库 |
| Redis | - | 缓存 |
| JWT (jjwt) | 0.9.1 | 无状态认证 |
| BCrypt | - | 密码加密（Spring Security） |
| Knife4j | 2.0.9 | Swagger 增强 |
| Hutool | 5.8 | Java 工具集 |

## 📁 项目结构

```
vuehouduan/
├── backend/                     # Spring Boot 后端
│   ├── src/main/java/com/example/vuehouduan/
│   │   ├── common/              # 通用类（Result、PageResult、异常）
│   │   ├── config/              # 配置类（CORS、Swagger、MyBatis-Plus、WebMvc）
│   │   ├── controller/          # 控制器层（19 个）
│   │   ├── service/             # 服务层
│   │   ├── service/impl/        # 服务实现
│   │   ├── mapper/              # 数据访问层
│   │   ├── entity/              # 数据库实体
│   │   ├── interceptor/         # JWT 拦截器
│   │   └── util/                # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml      # 主配置（默认）
│   │   ├── application-example.yml  # 配置模板（不包含敏感信息）
│   │   └── mapper/              # MyBatis XML
│   ├── sql/agricultural_ecommerce.sql  # 数据库初始化脚本（含示例数据）
│   └── pom.xml
│
├── src/                         # Vue 前端
│   ├── api/                     # 接口封装（按模块组织）
│   ├── assets/                  # 静态资源（CSS、图片）
│   ├── components/              # 公共组件
│   ├── composables/             # 组合式 API
│   ├── config/                  # 配置（菜单、权限）
│   ├── data/                    # 数据访问层（含 mock）
│   ├── router/                  # 路由 + 权限守卫
│   ├── utils/                   # 工具类
│   ├── views/                   # 页面
│   │   ├── front/               # 用户端 + 商家端
│   │   └── manager/             # 管理端
│   ├── App.vue
│   └── main.js
│
├── public/                      # 公共静态资源
├── dist/                        # 构建产物（.gitignore）
├── node_modules/                # 依赖（.gitignore）
│
├── .env.development             # 开发环境变量
├── .env.production              # 生产环境变量
├── .gitignore
├── babel.config.js
├── jsconfig.json
├── vue.config.js                # Vue CLI 配置（含代理）
├── package.json
├── package-lock.json
└── README.md
```

## 🚀 快速开始

### 环境要求

- **JDK 11+**
- **Maven 3.6+**
- **Node.js 16+** & npm
- **MySQL 8.0+**
- **Redis**（可选，不依赖也能跑）

### 一、初始化数据库

```bash
mysql -u root -p
source backend/sql/agricultural_ecommerce.sql;
```

或者使用 Navicat / DataGrip 直接执行 SQL 文件。

### 二、启动后端

```bash
cd backend

# 方式 A：直接用 Maven 跑
mvn spring-boot:run

# 方式 B：打包后运行
mvn clean package -DskipTests
java -jar target/vuehouduan-1.0.0.jar
```

后端启动后访问：
- API 地址：http://localhost:9090
- Swagger 文档：http://localhost:9090/doc.html

### 三、启动前端

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run serve
```

前端启动后访问：http://localhost:8080

## 🔑 测试账号

| 用户名 | 密码 | 角色 | 备注 |
| --- | --- | --- | --- |
| `admin` | `123456` | 管理员 | 后台管理 |
| `qhj` | `123456` | 普通用户 | 购物体验 |
| `farmer1` | `123456` | 商家 | 发布商品、订单管理 |

> 🔐 **安全提示**：项目源码已对密码进行 BCrypt 加密。新注册/导入的用户密码会自动哈希；
> 老用户的明文密码会在首次成功登录时自动升级为 BCrypt。

## 🗺️ 系统架构

![系统架构图](system_architecture.md)（同目录下 Mermaid 图）

完整架构说明见 [`system_architecture.md`](./system_architecture.md) — 使用 Mermaid 描述了
"前端 → Controller → Service → Mapper → Database"的完整调用链与中间件依赖。

## 📡 API 接口概览

### 认证
| Method | URL | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/logout` | 用户登出 |
| POST | `/api/auth/refresh` | 刷新 Token |

### 商品
| Method | URL | 说明 |
| --- | --- | --- |
| GET | `/api/products` | 商品分页列表（支持分类、关键词、筛选、排序） |
| GET | `/api/products/{id}` | 商品详情（自动增加浏览量） |
| GET | `/api/products/hot` | 热门商品 |
| GET | `/api/products/new` | 新品 |
| GET | `/api/products/recommended` | 推荐商品 |
| GET | `/api/products/search` | 搜索商品 |
| POST | `/api/products` | 商家发布商品（待审核） |
| PUT | `/api/products/{id}/approve` | 管理员审核通过 |
| PUT | `/api/products/{id}/reject` | 管理员驳回 |

### 订单
| Method | URL | 说明 |
| --- | --- | --- |
| POST | `/api/orders` | 创建订单（含优惠券处理） |
| GET | `/api/orders` | 订单列表（按角色自动过滤） |
| GET | `/api/orders/{id}` | 订单详情 |
| GET | `/api/orders/stats` | 各状态订单数 |
| PUT | `/api/orders/{id}/pay` | 支付订单 |
| PUT | `/api/orders/{id}/cancel` | 取消订单 |
| PUT | `/api/orders/{id}/confirm` | 确认收货 |
| PUT | `/api/orders/{id}/address` | 修改收货地址 |
| PUT | `/api/orders/{id}/coupon` | 更换订单优惠券 |

### 购物车 / 收藏 / 地址
- `GET/POST/PUT/DELETE /api/cart` — 购物车 CRUD
- `GET/POST /api/favorites` — 收藏
- `GET/POST/PUT/DELETE /api/addresses` — 收货地址

### 售后 / 物流
- `POST /api/after-sales` — 创建售后申请（退货退款 / 仅退款 / 换货）
- `PUT /api/after-sales/{id}/approve|reject|complete` — 商家处理
- `GET /api/delivery/tracking/{orderId}` — 查询物流

### 营销 / 优惠券
- `GET /api/coupon/list` — 优惠券列表
- `POST /api/coupon/receive/{id}` — 领取
- `GET /api/user-coupon/checkout` — 结算时可用券

### 管理后台
- 用户、商家、公告、新闻、评价、登录历史、操作日志、数据备份、
- 服务器状态、性能监控、系统设置、报表中心、数据统计 等 19 个模块

## 🔐 安全机制

1. **JWT 无状态认证**：登录后签发 token，前端 `Authorization: Bearer xxx`
2. **BCrypt 密码加密**：所有用户密码均经 `BCryptPasswordEncoder` 哈希后入库
3. **RBAC 权限控制**：前端路由守卫 + 后端拦截器，按角色（USER/MERCHANT/ADMIN）分配接口
4. **统一异常处理**：`GlobalExceptionHandler` 兜底所有未捕获异常，统一返回 `Result` JSON
5. **CORS 白名单**：仅放行明确的前端源，不再 `*` 放行

### ⚠️ 生产部署前必读

如果要把本项目部署到生产环境，请务必修改以下项：

- `application.yml` 中：
  - `DB_PASSWORD` — MySQL 密码（建议改成强密码）
  - `JWT_SECRET` — 至少 64 位随机字符串
- `CorsConfig.java` 中 `ALLOWED_ORIGINS` — 加入你前端的真实域名
- 删除/修改 `agricultural_ecommerce.sql` 中的示例账号密码
- 启用 HTTPS，关闭 HTTP 端口

可通过环境变量注入敏感配置：

```bash
export DB_PASSWORD="your-strong-password"
export JWT_SECRET="$(openssl rand -hex 64)"
```

## 🛠️ 二次开发

### 新增一个模块的标准流程（以"积分"为例）

1. **数据库**：建 `points` 表，字段参考 `entities/`
2. **后端**
   - `entity/Points.java`
   - `mapper/PointsMapper.java` + `resources/mapper/PointsMapper.xml`
   - `service/PointsService.java` + `service/impl/PointsServiceImpl.java`
   - `controller/PointsController.java`（挂 `@RequestMapping("/api/points")`）
3. **前端**
   - `api/index.js` 新增 `points` 接口方法
   - `views/` 新增页面组件
   - `router/index.js` 注册路由
   - 菜单按角色挂入 `Manager.vue` / `Front.vue`

### 调试技巧

- 后端日志：`com.example.vuehouduan: debug`（application.yml 已开启）
- SQL 日志：MyBatis-Plus `log-impl: StdOutImpl` 会打印完整 SQL
- 接口联调：直接访问 `/doc.html`（Knife4j）在线测试

## 📦 构建部署

### 前端打包

```bash
npm run build
# 产物在 dist/ 目录，部署到 Nginx 即可
```

Nginx 反向代理示例：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /var/www/vuehouduan/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:9090/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /imgs/ {
        proxy_pass http://127.0.0.1:9090/imgs/;
    }

    location /upload/ {
        proxy_pass http://127.0.0.1:9090/upload/;
    }
}
```

### 后端打包

```bash
cd backend
mvn clean package -DskipTests
# 产物在 backend/target/vuehouduan-1.0.0.jar
```

## ❓ 常见问题

**Q1: 启动后端报 "Communications link failure"？**
A: 检查 MySQL 是否启动、端口是否正确、`agricultural_ecommerce` 数据库是否已创建并导入 SQL。

**Q2: 前端跨域报错？**
A: `vue.config.js` 中 `proxy.target` 指向后端 9090；`CorsConfig` 已放行 `localhost:8080`。

**Q3: 登录提示"密码错误"但密码是对的？**
A: 检查数据库 `users` 表 `password` 字段是否是 BCrypt 哈希（以 `$2a$` 开头）。
SQL 中的示例账号已使用明文 `123456`，首次登录会自动升级为 BCrypt。

**Q4: 上传 GitHub 后如何处理上传的头像图片？**
A: 项目使用本地文件系统存储。生产环境建议改为对象存储（OSS/COS）。

## 📜 License

本项目仅用于学习和教学演示，请勿直接用于商业场景。
