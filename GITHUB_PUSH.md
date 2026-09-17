# 🚀 完整 GitHub 上传操作手册

本手册带你一步步把"绿源农鲜"农产品电商平台上传到 GitHub。

---

## 一、GitHub 仓库命名建议

你的项目核心关键词：**农产品 + 电商 + 前后端分离**

### ✅ 推荐命名（按优先级排序）

| 序号 | 仓库名 | 优点 | URL 示例 |
| --- | --- | --- | --- |
| 1 | `agricultural-ecommerce` | 直白英文，国际通用 ⭐推荐 | `github.com/xxx/agricultural-ecommerce` |
| 2 | `farm-product-mall` | "农产品商场"的精准翻译 | `github.com/xxx/farm-product-mall` |
| 3 | `fresh-farm-platform` | 强调"新鲜农产品" + "平台" | `github.com/xxx/fresh-farm-platform` |
| 4 | `green-farm-mall` | 呼应你的品牌"绿源农鲜" | `github.com/xxx/green-farm-mall` |
| 5 | `vue-springboot-mall` | 直接体现技术栈 | `github.com/xxx/vue-springboot-mall` |
| 6 | `lvyuan-ecommerce` | 中文拼音+电商 | `github.com/xxx/lvyuan-ecommerce` |

### 📝 推荐写法

**仓库名（Repository name）**：`agricultural-ecommerce`

**描述（Description，≤ 100 字符）**：
```
🌾 农产品电商平台 - Vue 2 + Spring Boot 2 + MyBatis-Plus + JWT
基于前后端分离架构，支持用户/商家/管理员三端
```

**Topics（标签，最多 20 个）**：
```
vue, spring-boot, mybatis-plus, mysql, jwt, ecommerce,
agriculture, element-ui, axios, redis, java
```

---

## 二、完整上传步骤（推荐：HTTPS + Token 方式）

### 📍 第 1 步：创建 GitHub 仓库

1. 打开 https://github.com/new
2. 填写：
   - **Owner**：选你的账号
   - **Repository name**：`agricultural-ecommerce`（或上面你选的名字）
   - **Description**：粘贴上面的描述
   - **Public / Private**：自选（Public 别人能看到，建议公开让同学学习）
   - ⚠️ **全部取消勾选**：
     - ☐ Add a README file
     - ☐ Add .gitignore
     - ☐ Choose a license
   - 点击绿色按钮 **Create repository**
3. 创建后 GitHub 会跳到一个 "Quick setup" 页面，先**放着别关**，需要里面的 URL

---

### 📍 第 2 步：生成 Personal Access Token（推送凭证）

GitHub 已不支持密码推送，必须用 Token：

1. 打开 https://github.com/settings/tokens
2. 点 **Generate new token** → **Generate new token (classic)**
3. 填写：
   - **Note**：填写 `vue-agricultural-ecommerce`（备注这个 token 干什么用）
   - **Expiration**：选 `30 days` 或 `No expiration`（推荐 90 天）
   - **Scopes（权限）**：勾选 ✅ **repo**（完整仓库权限）
4. 点 **Generate token**
5. **⚠️ 立刻复制生成的 token**（形如 `ghp_xxxxxxxxxxxxxxxxxxxx`，只显示一次！）
6. 临时保存到记事本，待会儿要用

---

### 📍 第 3 步：在 PowerShell 中执行推送命令

打开 PowerShell，按顺序执行（**把 `YOUR_USERNAME` 替换成你的 GitHub 用户名**）：

```powershell
# 1. 进入项目目录
cd "D:\课程代码\智慧农产品商场"

# 2. 确认 git 已配置好（之前我已经配过，这里再确认一下）
git config user.email
git config user.name

# 3. 关联远程仓库（替换成你的 URL）
git remote add origin https://github.com/YOUR_USERNAME/agricultural-ecommerce.git

# 4. 验证远程仓库
git remote -v

# 5. 推送到 main 分支（首次推送加 -u 关联上游）
git push -u origin main
```

**首次推送时会弹窗：**
- Username：填你的 GitHub 用户名
- Password：**粘贴第 2 步生成的 token**（不是你的 GitHub 密码！）

推送成功后你会看到类似：

```
Enumerating objects: 395, done.
Counting objects: 100% (395/395), done.
Writing objects: 100% (395/395), 196.00 KiB | 19.60 MiB/s, done.
Total 395 (delta 0), reused 0 (delta 0)
To https://github.com/YOUR_USERNAME/agricultural-ecommerce.git
 * [new branch]      main -> main
Branch 'main' set up to track remote branch 'main' from 'origin'.
```

---

### 📍 第 4 步：验证上传成功

1. 刷新 GitHub 仓库页面（`https://github.com/YOUR_USERNAME/agricultural-ecommerce`）
2. 你应该能看到：
   - 371 个文件
   - README.md 自动渲染在主页
   - 文件树包含 `backend/` 和 `src/`

---

## 三、（可选）切换 SSH 方式推送

如果不想每次输 Token，可以配置 SSH：

```powershell
# 1. 检查是否已有 SSH key
type $env:USERPROFILE\.ssh\id_ed25519.pub

# 2. 如果上面命令报错"No such file"，生成一个新的
ssh-keygen -t ed25519 -C "your_email@example.com"
# 一路回车（不设密码就空着）

# 3. 复制公钥内容
Get-Content $env:USERPROFILE\.ssh\id_ed25519.pub | Set-Clipboard

# 4. 打开 https://github.com/settings/keys → New SSH key
#    - Title: "My PC"
#    - Key: 粘贴刚才复制的公钥
#    - 点击 Add SSH key

# 5. 把远程 URL 改成 SSH 形式
cd "D:\课程代码\智慧农产品商场"
git remote set-url origin git@github.com:YOUR_USERNAME/agricultural-ecommerce.git

# 6. 验证连通
ssh -T git@github.com
# 第一次会提示 "Are you sure you want to continue connecting"，输入 yes

# 7. 以后推送就直接 git push 即可
git push
```

---

## 四、上传后的完善工作

### 1️⃣ 在仓库页面设置信息

1. 进仓库 → 点 **⚙️ Settings**
2. **General** → **Topics**：粘贴上面的 11 个标签
3. **General** → **Description**：粘贴上面的项目描述
4. **General** → **Website**：填你的演示地址（如果有）

### 2️⃣ 在 README 顶部加项目截图

在仓库主页点 **Add file** → **Upload files**，上传几张截图到 `docs/screenshots/` 目录，然后修改 `README.md` 顶部：

```markdown
<p align="center">
  <img src="docs/screenshots/home.png" width="600" alt="前台首页"/>
  <br/>
  <img src="docs/screenshots/manager.png" width="600" alt="后台管理"/>
</p>
```

**截图建议至少这几张：**
- 前台首页（轮播图 + 商品列表）
- 商品详情页
- 购物车结算
- 商家管理后台
- 管理员仪表盘（数据统计图表）

### 3️⃣ 创建第一个 Release

```powershell
cd "D:\课程代码\智慧农产品商场"
git tag v1.0.0
git push origin v1.0.0
```

然后去 GitHub → **Releases** → **Draft a new release**：
- Tag version: `v1.0.0`
- Release title: `v1.0.0 - 首次发布`
- Description 写一段话介绍主要功能

### 4️⃣ 添加项目徽章（README 顶部）

修改 README.md 最顶部：

```markdown
<p align="center">
  <img src="https://img.shields.io/badge/Vue-2.6-brightgreen" alt="Vue"/>
  <img src="https://img.shields.io/badge/Spring_Boot-2.7-blue" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-orange" alt="MySQL"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License"/>
</p>
```

效果：

![Vue](https://img.shields.io/badge/Vue-2.6-brightgreen)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7-blue)

---

## 五、可能遇到的问题

### ❌ 问题 1：`Authentication failed`

**原因**：密码错误或 Token 无效

**解决**：
- 确认输入的是 **Token** 不是 GitHub 密码
- Token 是否过期
- Token 是否勾选了 `repo` 权限

### ❌ 问题 2：`remote origin already exists`

**原因**：之前已经添加过远程仓库

**解决**：
```powershell
git remote remove origin
git remote add origin https://github.com/YOUR_USERNAME/agricultural-ecommerce.git
```

### ❌ 问题 3：`failed to push some refs`

**原因**：远程仓库已有内容（你勾选了 Add README）

**解决**：
```powershell
# 强制推送（会覆盖远程内容）
git push -u origin main --force
```

或者：
```powershell
# 先拉取再合并
git pull origin main --allow-unrelated-histories
git push -u origin main
```

### ❌ 问题 4：推送时报 `Could not resolve host`

**原因**：网络问题或 DNS 污染

**解决**：
```powershell
# 检查是否能访问 GitHub
ping github.com

# 如果 ping 不通，配置代理或 hosts
# 临时方案：使用 Gitee 中转后再推 GitHub
```

---

## 六、上传后的演示视频/部署建议

如果想让人一眼看到效果，可以：

### 方案 A：录个 GIF / 短视频
- 用 [ScreenToGif](https://www.screentogif.com/) 录 30 秒核心流程
- 放到 README 里，效果最好

### 方案 B：部署到云服务器
- 后端：买个 1 核 2G 云服务器，装 Java + MySQL + Redis，跑 `java -jar`
- 前端：`npm run build`，把 `dist/` 上传到服务器 Nginx
- 数据库：用阿里云 RDS 或自建 MySQL

### 方案 C：纯前端演示（不用后端）
- 前端可以改造成纯静态 demo，Mock 数据
- 上传后用 GitHub Pages 托管
- 改 `vue.config.js` 的 `publicPath: './'`

---

## 七、命名规范小贴士

### 文件命名建议（如果以后要重构）
```
src/views/
├── front/                    # 用户端 (frontend)
│   ├── Home.vue
│   ├── Cart.vue
│   ├── Checkout.vue
│   └── OrderDetail.vue
├── merchant/                 # 商家端 (merchant portal)
│   ├── Dashboard.vue
│   ├── ProductList.vue
│   └── OrderManage.vue
└── admin/                    # 管理端 (admin panel)
    ├── Home.vue
    ├── UserManage.vue
    └── ProductReview.vue
```

### 提交信息规范（如果以后要多次提交）
```bash
# 格式：<type>(<scope>): <subject>
git commit -m "feat(user): 新增用户头像上传功能"
git commit -m "fix(order): 修复订单取消时库存未恢复的bug"
git commit -m "docs(readme): 更新项目截图"
git commit -m "refactor(auth): 重构 JWT 认证逻辑"
git commit -m "style(css): 统一按钮样式"
```

Type 类型：
- `feat` 新功能
- `fix` 修复
- `docs` 文档
- `style` 格式（不影响代码运行）
- `refactor` 重构
- `perf` 性能优化
- `test` 测试
- `chore` 构建/工具

---

## 八、整个流程时间估算

| 步骤 | 预计耗时 |
| --- | --- |
| 创建 GitHub 仓库 | 2 分钟 |
| 生成 Personal Access Token | 2 分钟 |
| 第一次推送代码 | 2 分钟 |
| 添加截图、改 README | 10-30 分钟 |
| 创建 Release | 2 分钟 |
| **合计** | **20-40 分钟** |

---

## 九、上传后能给同学/学弟学妹看什么

仓库地址可以这样宣传：

> 🎓 **农产品电商平台完整源码（前后端分离）**
>
> 🌐 仓库地址：https://github.com/YOUR_USERNAME/agricultural-ecommerce
>
> ✨ 亮点：
> - 完整的三端架构（用户/商家/管理）
> - JWT + BCrypt 安全方案
> - 19 个核心模块（商品/订单/优惠券/售后/物流...）
> - 详细注释 + 系统架构图 + Swagger 接口文档
> - 一键启动：导入 SQL → `mvn spring-boot:run` → `npm run serve`

---

## 🎉 完成 Checklist

推送成功后，逐项核对：

- [ ] 仓库已创建并设为 Public
- [ ] 371 个文件全部推送成功
- [ ] README.md 在仓库首页正常显示
- [ ] 仓库 Topics 已设置（vue、spring-boot 等）
- [ ] 仓库 Description 已设置
- [ ] （可选）上传了项目截图
- [ ] （可选）创建了 v1.0.0 Release
- [ ] （可选）把仓库链接发给同学/老师

完成后你就拥有了一个完整的、专业的、有安全保障的 GitHub 项目仓库 🎊
