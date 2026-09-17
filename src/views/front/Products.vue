/**
 * 商品中心页面（前台）
 * 文件路径: src/views/front/Products.vue
 * 功能描述: 前台商品浏览与搜索，分类导航栏（全部/蔬菜/水果/粮油/畜牧/水产等分类筛选），
 *           关键词搜索（支持回车搜索），商品卡片网格展示（图片、名称、价格、销量/浏览统计），
 *           分页查看更多，点击跳转商品详情
 * 关联文件:
 * - src/api/index.js: 提供商品列表和分类接口
 * - src/views/front/ProductDetail.vue: 商品详情页面
 */
<template>
  <div class="products-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">商品中心</h1>
        <p class="page-subtitle">精选优质农产品，源自田间，鲜达餐桌</p>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品..."
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
        >
          <template v-slot:prefix>
            <i class="el-icon-search"></i>
          </template>
          <template v-slot:append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <div class="category-nav">
      <div 
        class="category-item"
        :class="{ active: category === '' }"
        @click="setCategory('')"
      >
        <i class="el-icon-menu"></i>
        <span>全部</span>
      </div>
      <div 
        v-for="cat in categoryList" 
        :key="cat.value"
        class="category-item"
        :class="{ active: category === cat.value }"
        @click="setCategory(cat.value)"
      >
        <i :class="cat.icon"></i>
        <span>{{ cat.label }}</span>
      </div>
    </div>

    <div class="filter-bar">
      <div class="sort-options">
        <span class="sort-label">排序：</span>
        <el-radio-group v-model="sortBy" size="small">
          <el-radio-button label="default">默认</el-radio-button>
          <el-radio-button label="sales">销量</el-radio-button>
          <el-radio-button label="price_asc">价格↑</el-radio-button>
          <el-radio-button label="price_desc">价格↓</el-radio-button>
          <el-radio-button label="new">新品</el-radio-button>
        </el-radio-group>
      </div>
      <div class="result-count">
        共找到 <span class="count">{{ total }}</span> 件商品
      </div>
    </div>

    <div v-if="loading" class="loading-grid">
      <div v-for="i in 8" :key="i" class="loading-card">
        <el-skeleton :rows="3" animated />
      </div>
    </div>

    <div v-else-if="products.length > 0" class="products-grid">
      <div 
        v-for="product in products" 
        :key="product.id" 
        class="product-card"
        @click="viewDetail(product.id)"
      >
        <div class="product-image">
          <img :src="getImage(product)" @error="handleImageError" :alt="product.name" />
          <div v-if="product.isHot" class="hot-badge">热销</div>
          <div v-if="product.isNew" class="new-badge">新品</div>
        </div>
        <div class="product-info">
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-desc">{{ product.description || '' }}</p>
          <div class="product-meta">
            <span class="product-price">¥{{ formatPrice(product.price) }}</span>
            <span class="product-sales">已售 {{ product.sales || 0 }}</span>
          </div>
          <div class="product-stock">
            <span v-if="(product.stock || 0) > 10" class="stock-enough">有货</span>
            <span v-else-if="(product.stock || 0) > 0" class="stock-low">仅剩 {{ product.stock }}</span>
            <span v-else class="stock-none">缺货</span>
          </div>
        </div>
        <div class="product-actions">
          <el-button type="primary" size="small" @click.stop="handleAddToCart(product)">
            <i class="el-icon-shopping-cart-2"></i> 加入购物车
          </el-button>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon"><i class="el-icon-s-goods"></i></div>
      <h3>暂无商品</h3>
      <p>抱歉，没有找到符合条件的商品</p>
      <el-button type="primary" @click="resetFilter">重置筛选</el-button>
    </div>

    <div v-if="total > pageSize" class="pagination-wrapper">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        @current-change="handlePageChange"
      />
    </div>

    <Footer />
  </div>
</template>

<script>
import Footer from '@/components/Footer'
import { addToCart, getProductPage } from '@/api'

const categoryList = [
  { value: 'agricultural', label: '农产品', icon: 'el-icon-s-marketing' },
  { value: 'fruit', label: '水果', icon: 'el-icon-s-goods' },
  { value: 'livestock', label: '畜禽产品', icon: 'el-icon-s-custom' },
  { value: 'grain', label: '粮油', icon: 'el-icon-s-order' },
  { value: 'processed', label: '加工产品', icon: 'el-icon-s-goods' },
  { value: 'other', label: '其他', icon: 'el-icon-goods' }
]

export default {
  name: 'ProductsPage',
  components: { Footer },
  data() {
    return {
      products: [],
      loading: false,
      category: '',
      searchKeyword: '',
      sortBy: 'default',
      total: 0,
      currentPage: 1,
      pageSize: 12,
      categoryList
    }
  },
  watch: {
    sortBy() {
      this.currentPage = 1
      this.loadProducts()
    }
  },
  created() {
    this.loadProducts()
  },
  methods: {
    async loadProducts() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize,
          category: this.category || undefined,
          keyword: this.searchKeyword || undefined
        }

        if (this.sortBy === 'sales') {
          params.sortBy = 'sales'
          params.sortOrder = 'desc'
        } else if (this.sortBy === 'price_asc') {
          params.sortBy = 'price'
          params.sortOrder = 'asc'
        } else if (this.sortBy === 'price_desc') {
          params.sortBy = 'price'
          params.sortOrder = 'desc'
        } else if (this.sortBy === 'new') {
          params.sortBy = 'createTime'
          params.sortOrder = 'desc'
        }

        const res = await getProductPage(params)
        const data = res?.data || res
        this.products = data?.list || data?.records || []
        this.total = data?.total || 0
      } catch (error) {
        this.$message.error('加载商品失败')
        this.products = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    setCategory(value) {
      this.category = value
      this.currentPage = 1
      this.loadProducts()
    },

    handleSearch() {
      this.currentPage = 1
      this.loadProducts()
    },

    resetFilter() {
      this.category = ''
      this.searchKeyword = ''
      this.sortBy = 'default'
      this.currentPage = 1
      this.loadProducts()
    },

    handlePageChange(page) {
      this.currentPage = page
      this.loadProducts()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },

    getImage(product) {
      const img = product.image || product.img
      if (!img) return '/imgs/foods/1.png'
      if (img.startsWith('http') || img.startsWith('data:')) return img
      if (img.startsWith('upload') || /^\d{8}\//.test(img)) {
        const baseUrl = (process.env.VUE_APP_BASEURL || '').replace('/api', '') || 'http://localhost:9090'
        return `${baseUrl}/upload/${img.replace(/^.*upload\//, '')}`
      }
      return img.startsWith('/') ? img : '/' + img
    },

    handleImageError(e) {
      e.target.src = '/imgs/foods/1.png'
    },

    viewDetail(id) {
      this.$router.push(`/front/product/${id}`)
    },

    async handleAddToCart(product) {
      if (!product || (product.stock || 0) <= 0) {
        this.$message.error('该商品暂时缺货')
        return
      }
      
      const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
      if (!user.id) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      
      try {
        await addToCart({ productId: product.id, count: 1 })
        this.$message.success(`已添加 "${product.name}" 到购物车`)
        window.dispatchEvent(new Event('xm-cart-changed'))
      } catch (err) {
        this.$message.error(err?.message || '加入购物车失败，请重试')
      }
    },

    formatPrice(price) {
      return typeof price === 'number' ? price.toFixed(2) : '0.00'
    }
  }
}
</script>

<style scoped>
.products-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40px;
}

.page-header {
  background: linear-gradient(135deg, #fff 0%, #f8faf8 100%);
  padding: 24px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e8e8e8;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px;
  background: linear-gradient(135deg, #4caf50 0%, #2e7d32 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 12px;
}

.search-input {
  width: 300px;
}

.category-nav {
  display: flex;
  gap: 8px;
  padding: 16px 30px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  overflow-x: auto;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f5f7fa;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  border: 2px solid transparent;
}

.category-item:hover {
  background: #e8f5e9;
  border-color: #4caf50;
}

.category-item.active {
  background: #4caf50;
  color: #fff;
}

.category-item i {
  font-size: 16px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 30px;
  background: #fff;
  margin-bottom: 20px;
  border-radius: 0 0 12px 12px;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sort-label {
  font-size: 14px;
  color: #666;
}

.result-count {
  font-size: 14px;
  color: #666;
}

.result-count .count {
  color: #4caf50;
  font-weight: 600;
}

.loading-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 0 30px;
}

.loading-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 0 30px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #4caf50;
}

.product-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f5f5;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.hot-badge,
.new-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.hot-badge {
  background: linear-gradient(135deg, #ff9800, #ff5722);
}

.new-badge {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  min-height: 44px;
}

.product-desc {
  font-size: 13px;
  color: #666;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.product-price {
  font-size: 20px;
  font-weight: 700;
  color: #ff5722;
}

.product-sales {
  font-size: 12px;
  color: #999;
}

.product-stock {
  font-size: 12px;
}

.stock-enough { color: #67c23a; }
.stock-low { color: #e6a23c; }
.stock-none { color: #f56c6c; }

.product-actions {
  padding: 0 16px 16px;
}

.product-actions .el-button {
  width: 100%;
  background: linear-gradient(135deg, #4caf50, #2e7d32);
  border: none;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  margin: 0 30px;
  border-radius: 12px;
}

.empty-icon {
  font-size: 64px;
  color: #ddd;
  margin-bottom: 16px;
}

.empty-state h3 {
  margin: 0 0 8px;
  color: #333;
}

.empty-state p {
  margin: 0 0 20px;
  color: #999;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

@media (max-width: 1200px) {
  .products-grid,
  .loading-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .products-grid,
  .loading-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    padding: 0 12px;
  }
  
  .category-nav {
    padding: 12px;
  }
  
  .filter-bar {
    flex-direction: column;
    gap: 12px;
    padding: 12px;
  }
  
  .search-input {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .products-grid,
  .loading-grid {
    grid-template-columns: 1fr;
  }
}
</style>
