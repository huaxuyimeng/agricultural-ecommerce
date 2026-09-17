/**
 * 资讯中心页面
 * 文件路径: src/views/front/News.vue
 * 功能描述: 农业资讯列表展示，资讯总数/本月更新统计卡片并支持点击筛选，
 *           分类筛选（政策/技术/市场/气候/价格波动/病虫害防治/全部）、关键词搜索、
 *           资讯卡片列表（封面图、标题、摘要、分类标签、浏览数、发布时间），分页查看
 * 关联文件:
 * - src/api/index.js: 提供资讯数据接口
 * - src/views/front/NewsDetail.vue: 资讯详情页面
 */
<template>
  <div class="news-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">资讯中心</h1>
          <p class="page-subtitle">获取最新农业资讯，掌握产业发展动态</p>
        </div>
        
        <div class="stats-cards">
          <div class="stat-card" @click="handleStatClick('total')">
            <div class="stat-icon total">
              <i class="el-icon-s-data"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ totalNews }}</div>
              <div class="stat-label">资讯总数</div>
            </div>
          </div>
          
          <div class="stat-card" @click="handleStatClick('recent')">
            <div class="stat-icon recent">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ recentNewsCount }}</div>
              <div class="stat-label">本月更新</div>
            </div>
          </div>
          
          <div class="stat-card" @click="handleStatClick('views')">
            <div class="stat-icon hot">
              <i class="el-icon-s-opportunity"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ totalViews }}</div>
              <div class="stat-label">累计阅读</div>
            </div>
          </div>
          
          <div class="stat-card" @click="handleStatClick('category')">
            <div class="stat-icon tags">
              <i class="el-icon-collection-tag"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ categoryOptions.length }}</div>
              <div class="stat-label">分类数量</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 搜索和筛选 -->
      <div class="search-filter">
        <div class="search-wrapper">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索资讯内容..."
            class="search-input"
            clearable
            @keyup.enter="handleSearch"
            @clear="clearSearch"
          >
            <template v-slot:prefix>
              <i class="el-icon-search"></i>
            </template>
          </el-input>
          <el-button type="warning" @click="handleSearch" class="search-btn">
            搜索
          </el-button>
          <el-button @click="refreshNews" :loading="loading" class="refresh-btn" title="刷新">
            <i class="el-icon-refresh"></i>
          </el-button>
        </div>
        
        <!-- 分类筛选 -->
        <div class="category-filter">
          <div class="filter-title">分类筛选：</div>
          <div class="filter-options">
            <el-button
              size="small"
              :type="activeCategory === '' ? 'success' : ''"
              :plain="activeCategory !== ''"
              @click="setCategory('')"
            >
              全部
            </el-button>
            <el-button
              v-for="option in categoryOptions"
              :key="option.value"
              size="small"
              :type="activeCategory === option.value ? 'success' : ''"
              :plain="activeCategory !== option.value"
              @click="setCategory(option.value)"
            >
              {{ option.label }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="content-wrapper">
      <!-- 主内容 -->
      <div class="main-content">
        <!-- 加载状态 -->
        <div v-if="loading && newsList.length === 0" class="loading-state">
          <el-skeleton :rows="8" animated />
        </div>
        
        <!-- 咨询列表 -->
        <div v-else class="news-container">
          <!-- 搜索结果提示 -->
          <div v-if="searchKeyword || activeCategory" class="filter-tip">
            <span v-if="searchKeyword">搜索关键词: "{{ searchKeyword }}"</span>
            <span v-if="searchKeyword && activeCategory">，</span>
            <span v-if="activeCategory">分类: {{ getCategoryText(activeCategory) }}</span>
            <span class="result-count">，共找到 {{ filteredNews.length }} 条结果</span>
            <el-button type="text" size="small" @click="clearAllFilters">清空筛选</el-button>
          </div>

          <div v-if="newsList.length > 0" class="news-grid">
            <!-- 主推资讯 -->
            <div v-if="featuredNews.length > 0 && !searchKeyword && !activeCategory" class="featured-section">
              <div class="section-header">
                <div class="section-title">
                  <i class="el-icon-star-on"></i>
                  <span>热门推荐</span>
                </div>
                <el-tag type="warning" size="small">基于阅读量</el-tag>
              </div>
              <div class="featured-grid">
                <div 
                  v-for="item in featuredNews" 
                  :key="item.id" 
                  class="featured-card"
                  @click="viewNewsDetail(item)"
                >
                  <div class="featured-image">
                    <img :src="getImageUrl(item.image)" :alt="item.title" @error="handleImageError" />
                    <div class="featured-badge">推荐</div>
                    <div class="featured-views">
                      <i class="el-icon-view"></i>
                      {{ item.views || 0 }}
                    </div>
                  </div>
                  <div class="featured-content">
                    <div class="featured-tag">{{ getCategoryText(item.category) }}</div>
                    <h3 class="featured-title">{{ item.title }}</h3>
                    <p class="featured-desc">{{ item.description || item.excerpt }}</p>
                    <div class="featured-meta">
                      <span class="meta-item">
                        <i class="el-icon-date"></i>
                        {{ formatDate(item.createTime) }}
                      </span>
                      <span class="meta-item">
                        <i class="el-icon-user"></i>
                        {{ item.author || '官方' }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 普通资讯列表 -->
            <div class="normal-section">
              <div class="section-header">
                <div class="section-title">
                  <i class="el-icon-document"></i>
                  <span>{{ searchKeyword || activeCategory ? '搜索结果' : '最新资讯' }}</span>
                </div>
                <div class="sort-options">
                  <span class="sort-label">排序：</span>
                  <el-radio-group v-model="sortBy" size="small">
                    <el-radio-button label="date">按时间</el-radio-button>
                    <el-radio-button label="views">按热度</el-radio-button>
                  </el-radio-group>
                </div>
              </div>
              
              <div class="news-list">
                <div 
                  v-for="(item, index) in paginatedNews" 
                  :key="item.id" 
                  class="news-card"
                  @click="viewNewsDetail(item)"
                >
                  <div class="news-card-left">
                    <div class="news-index">{{ formatIndex(index + 1 + (currentPage - 1) * pageSize) }}</div>
                    <div class="news-image">
                      <img :src="getImageUrl(item.image)" :alt="item.title" @error="handleImageError" />
                    </div>
                  </div>
                  
                  <div class="news-card-middle">
                    <h3 class="news-title">{{ item.title }}</h3>
                    <p class="news-desc">{{ item.description || item.excerpt }}</p>
                    
                    <div class="news-meta">
                      <div class="meta-left">
                        <div class="news-date">
                          <i class="el-icon-date"></i>
                          {{ formatDate(item.createTime) }}
                        </div>
                        <div class="news-source">
                          <i class="el-icon-office-building"></i>
                          {{ item.source || '官方发布' }}
                        </div>
                      </div>
                      <div class="meta-right">
                        <div class="news-views">
                          <i class="el-icon-view"></i>
                          {{ item.views || 0 }}
                        </div>
                        <div class="news-likes">
                          <i class="el-icon-like"></i>
                          {{ item.likes || 0 }}
                        </div>
                      </div>
                    </div>
                    
                    <div class="news-tags">
                      <el-tag v-if="item.category" type="info" size="small">
                        {{ getCategoryText(item.category) }}
                      </el-tag>
                      <el-tag v-for="tag in parseTags(item.tags)" :key="tag" type="warning" size="small" class="tag-chip">
                        {{ tag }}
                      </el-tag>
                    </div>
                  </div>
                  
                  <div class="news-card-right">
                    <el-button type="primary" size="small" class="read-btn" @click.stop="viewNewsDetail(item)">
                      阅读全文
                      <i class="el-icon-arrow-right"></i>
                    </el-button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div v-if="totalPages > 1" class="pagination-section">
              <el-pagination
                :current-page="currentPage"
                :page-size="pageSize"
                :total="filteredNews.length"
                :page-sizes="[6, 12, 24]"
                layout="total, sizes, prev, pager, next"
                background
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-else class="empty-state">
            <div class="empty-icon">
              <i class="el-icon-document-delete"></i>
            </div>
            <h3>未找到相关资讯</h3>
            <p>抱歉，没有找到符合搜索条件的资讯内容</p>
            <el-button type="warning" @click="clearAllFilters">
              <i class="el-icon-refresh"></i>
              清空筛选条件
            </el-button>
          </div>
        </div>
      </div>

      <!-- 侧边栏 -->
      <aside class="sidebar">
        <!-- 热门资讯 -->
        <div class="sidebar-section hot-news">
          <div class="sidebar-header">
            <h3 class="sidebar-title">
              <i class="el-icon-s-opportunity"></i>
              热门资讯
            </h3>
            <el-tag type="danger" size="small" class="hot-tag">TOP</el-tag>
          </div>
          <div class="hot-list">
            <div 
              v-for="(item, index) in hotNews" 
              :key="item.id"
              class="hot-item"
              @click="viewNewsDetail(item)"
            >
              <div class="hot-rank" :class="{ 'top-1': index === 0, 'top-2': index === 1, 'top-3': index === 2 }">
                {{ index + 1 }}
              </div>
              <div class="hot-content">
                <h4 class="hot-title">{{ item.title }}</h4>
                <div class="hot-meta">
                  <span><i class="el-icon-view"></i> {{ item.views || 0 }}</span>
                  <span><i class="el-icon-time"></i> {{ formatDate(item.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分类导航 -->
        <div class="sidebar-section category-nav">
          <h3 class="sidebar-title">
            <i class="el-icon-menu"></i>
            资讯分类
          </h3>
          <div class="category-grid">
            <div 
              v-for="cat in categoryOptions" 
              :key="cat.value"
              class="category-item"
              :class="{ active: activeCategory === cat.value }"
              @click="setCategory(cat.value)"
            >
              <i :class="getCategoryIcon(cat.value)"></i>
              <span>{{ cat.label }}</span>
              <el-tag type="info" size="mini">{{ getCategoryCount(cat.value) }}</el-tag>
            </div>
          </div>
        </div>

        <!-- 资讯统计 -->
        <div class="sidebar-section stats">
          <h3 class="sidebar-title">
            <i class="el-icon-s-data"></i>
            数据统计
          </h3>
          <div class="stats-list">
            <div class="stat-item">
              <span class="stat-label">资讯总数</span>
              <span class="stat-value">{{ total }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">累计阅读</span>
              <span class="stat-value">{{ totalViews }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">本月更新</span>
              <span class="stat-value">{{ recentNewsCount }}</span>
            </div>
          </div>
        </div>

        <!-- 快速链接 -->
        <div class="sidebar-section quick-links">
          <h3 class="sidebar-title">
            <i class="el-icon-link"></i>
            快速链接
          </h3>
          <div class="link-list">
            <router-link to="/front" class="quick-link">
              <i class="el-icon-s-home"></i> 返回首页
            </router-link>
            <router-link to="/front/products" class="quick-link">
              <i class="el-icon-s-goods"></i> 商品中心
            </router-link>
            <router-link to="/front/cart" class="quick-link">
              <i class="el-icon-shopping-cart-2"></i> 购物车
            </router-link>
          </div>
        </div>
      </aside>
    </div>

    <Footer />
  </div>
</template>

<script>
import Footer from '@/components/Footer'
import { getNewsPage } from '@/api'

const categoryMap = {
  policy: '政策解读',
  technology: '技术培训',
  farming: '农事指南',
  ecommerce: '电商发展',
  finance: '农村金融',
  safety: '质量安全',
  tourism: '乡村旅游',
  marketing: '品牌建设'
}

const categoryIcons = {
  policy: 'el-icon-document',
  technology: 'el-icon-magic-stick',
  farming: 'el-icon-crop',
  ecommerce: 'el-icon-shopping-cart-2',
  finance: 'el-icon-coin',
  safety: 'el-icon-shield',
  tourism: 'el-icon-tickets',
  marketing: 'el-icon-bangzhu'
}

export default {
  name: 'News',
  components: { Footer },
  data() {
    return {
      newsList: [],
      loading: false,
      searchKeyword: '',
      activeCategory: '',
      sortBy: 'date',
      currentPage: 1,
      pageSize: 8,
      total: 0,
      categoryOptions: Object.entries(categoryMap).map(([value, label]) => ({ value, label }))
    }
  },
  computed: {
    totalNews() {
      return this.total
    },
    totalViews() {
      return this.newsList.reduce((sum, item) => sum + (item.views || 0), 0)
    },
    recentNewsCount() {
      const now = new Date()
      const currentMonth = now.getMonth()
      const currentYear = now.getFullYear()
      return this.newsList.filter(item => {
        if (!item.createTime) return false
        const date = new Date(item.createTime)
        return date.getMonth() === currentMonth && date.getFullYear() === currentYear
      }).length
    },
    filteredNews() {
      let filtered = [...this.newsList]
      
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(item => 
          (item.title && item.title.toLowerCase().includes(keyword)) ||
          (item.description && item.description.toLowerCase().includes(keyword)) ||
          (item.excerpt && item.excerpt.toLowerCase().includes(keyword)) ||
          (item.content && item.content.toLowerCase().includes(keyword)) ||
          (item.tags && item.tags.toLowerCase().includes(keyword)) ||
          (item.author && item.author.toLowerCase().includes(keyword))
        )
      }
      
      if (this.activeCategory) {
        filtered = filtered.filter(item => item.category === this.activeCategory)
      }
      
      if (this.sortBy === 'date') {
        filtered.sort((a, b) => new Date(b.createTime || 0) - new Date(a.createTime || 0))
      } else {
        filtered.sort((a, b) => (b.views || 0) - (a.views || 0))
      }
      
      return filtered
    },
    totalPages() {
      return Math.ceil(this.filteredNews.length / this.pageSize)
    },
    paginatedNews() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredNews.slice(start, end)
    },
    featuredNews() {
      return [...this.newsList]
        .sort((a, b) => (b.views || 0) - (a.views || 0))
        .slice(0, 2)
    },
    hotNews() {
      return [...this.newsList]
        .sort((a, b) => (b.views || 0) - (a.views || 0))
        .slice(0, 5)
    }
  },
  watch: {
    sortBy() {
      this.currentPage = 1
    }
  },
  created() {
    this.loadNews()
  },
  methods: {
    getImageUrl(path) {
      if (!path) return '/imgs/foods/1.png'
      if (path.startsWith('http') || path.startsWith('data:')) return path
      // 处理上传路径
      const baseUrl = (process.env.VUE_APP_BASEURL || '').replace('/api', '') || 'http://localhost:9090'
      if (path.startsWith('upload')) {
        return `${baseUrl}/upload/${path.replace(/^.*upload\//, '')}`
      }
      if (/^\d{8}\//.test(path)) {
        return `${baseUrl}/upload/${path}`
      }
      return path.startsWith('/') ? path : '/' + path
    },
    
    handleImageError(e) {
      e.target.src = '/imgs/foods/1.png'
    },
    
    async loadNews() {
      this.loading = true
      try {
        const res = await getNewsPage({ page: 1, pageSize: 50 })
        let result = res
        // 处理响应格式
        if (res?.data) result = res.data
        if (res?.data?.data) result = res.data.data
        
        const list = result?.list || result?.records || []
        this.newsList = Array.isArray(list) ? list : []
        this.total = result?.total || this.newsList.length
      } catch (error) {
        console.error('加载新闻失败:', error)
        this.newsList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    
    async refreshNews() {
      await this.loadNews()
      this.$message.success('刷新成功')
    },
    
    parseTags(tags) {
      if (!tags) return []
      if (Array.isArray(tags)) return tags.slice(0, 2)
      return tags.split(',').map(t => t.trim()).filter(Boolean).slice(0, 2)
    },
    
    getCategoryText(category) {
      return categoryMap[category] || category || '其他'
    },
    
    getCategoryIcon(category) {
      return categoryIcons[category] || 'el-icon-folder'
    },
    
    getCategoryCount(category) {
      return this.newsList.filter(item => item.category === category).length
    },
    
    formatIndex(index) {
      return index < 10 ? '0' + index : index.toString()
    },
    
    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    
    viewNewsDetail(item) {
      // 保存浏览历史
      const history = JSON.parse(localStorage.getItem('xm-news-history') || '[]')
      const newEntry = {
        id: item.id,
        title: item.title,
        image: item.image,
        viewTime: new Date().toISOString()
      }
      const filteredHistory = history.filter(h => h.id !== item.id)
      filteredHistory.unshift(newEntry)
      localStorage.setItem('xm-news-history', JSON.stringify(filteredHistory.slice(0, 10)))
      
      // 广播更新事件
      window.dispatchEvent(new CustomEvent('xm-news-updated'))
      
      this.$router.push(`/front/news/${item.id}`)
    },
    
    handleSearch() {
      this.currentPage = 1
      if (this.searchKeyword) {
        this.$message.success(`搜索到 ${this.filteredNews.length} 条结果`)
      }
    },
    
    clearSearch() {
      this.searchKeyword = ''
      this.currentPage = 1
    },
    
    setCategory(value) {
      this.activeCategory = this.activeCategory === value ? '' : value
      this.currentPage = 1
    },
    
    clearAllFilters() {
      this.searchKeyword = ''
      this.activeCategory = ''
      this.currentPage = 1
    },
    
    clearFilters() {
      this.clearAllFilters()
    },
    
    handleStatClick(type) {
      if (type === 'recent') {
        this.sortBy = 'date'
      } else if (type === 'views') {
        this.sortBy = 'views'
      }
    },
    
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
    },
    
    handleCurrentChange(val) {
      this.currentPage = val
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  }
}
</script>

<style scoped>
.news-list-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.page-header {
  background: linear-gradient(135deg, #fff 0%, #f8faf8 100%);
  border-radius: 12px;
  padding: 24px 30px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px;
}

.page-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid #e8e8e8;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.15);
  border-color: #4caf50;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
}

.stat-icon.total { background: linear-gradient(135deg, #4caf50, #8bc34a); }
.stat-icon.recent { background: linear-gradient(135deg, #2196f3, #64b5f6); }
.stat-icon.hot { background: linear-gradient(135deg, #ff9800, #ffb74d); }
.stat-icon.tags { background: linear-gradient(135deg, #9c27b0, #ba68c8); }

.stat-info { flex: 1; }
.stat-number { font-size: 24px; font-weight: 700; color: #1a1a1a; }
.stat-label { font-size: 13px; color: #666; margin-top: 2px; }

.search-filter { margin-top: 20px; }
.search-wrapper { display: flex; gap: 10px; align-items: center; }
.search-input { flex: 1; }
.search-btn { background: linear-gradient(135deg, #4caf50, #2e7d32); border: none; }
.refresh-btn { padding: 0 12px; height: 40px; }

.category-filter { margin-top: 16px; display: flex; align-items: center; gap: 12px; }
.filter-title { font-size: 14px; font-weight: 600; color: #333; white-space: nowrap; }
.filter-options { display: flex; flex-wrap: wrap; gap: 8px; }

.content-wrapper { display: flex; gap: 20px; }

.main-content { flex: 1; min-width: 0; }
.news-container { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }

.filter-tip {
  background: #f0f9ff;
  border: 1px solid #91d5ff;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #1890ff;
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-count { flex: 1; }

.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 600; color: #333; }
.section-title i { color: #4caf50; font-size: 18px; }

.featured-section { margin-bottom: 24px; }
.featured-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }

.featured-card {
  background: #fafafa;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
  cursor: pointer;
  transition: all 0.3s;
}

.featured-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #4caf50;
}

.featured-image { position: relative; height: 180px; overflow: hidden; }
.featured-image img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.featured-card:hover .featured-image img { transform: scale(1.05); }
.featured-badge { position: absolute; top: 12px; left: 12px; background: linear-gradient(135deg, #ff9800, #ff5722); color: #fff; padding: 4px 12px; border-radius: 4px; font-size: 12px; font-weight: 600; }
.featured-views { position: absolute; bottom: 12px; right: 12px; background: rgba(0,0,0,0.6); color: #fff; padding: 4px 10px; border-radius: 4px; font-size: 12px; display: flex; align-items: center; gap: 4px; }
.featured-content { padding: 16px; }
.featured-tag { display: inline-block; background: rgba(76, 175, 80, 0.1); color: #4caf50; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 600; margin-bottom: 8px; }
.featured-title { font-size: 16px; font-weight: 600; color: #1a1a1a; margin: 0 0 8px; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.featured-desc { font-size: 13px; color: #666; margin: 0 0 12px; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.featured-meta { display: flex; gap: 16px; font-size: 12px; color: #999; }
.meta-item { display: flex; align-items: center; gap: 4px; }
.meta-item i { color: #4caf50; }

.normal-section { margin-bottom: 20px; }
.sort-options { display: flex; align-items: center; gap: 8px; }
.sort-label { font-size: 13px; color: #666; }


.news-list { display: flex; flex-direction: column; gap: 12px; }

.news-card {
  display: flex;
  background: #fafafa;
  border-radius: 10px;
  padding: 16px;
  border: 1px solid #e8e8e8;
  cursor: pointer;
  transition: all 0.3s;
  gap: 16px;
}

.news-card:hover {
  border-color: #4caf50;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.1);
  background: #fff;
}

.news-card-left { display: flex; align-items: center; gap: 12px; }
.news-index { width: 36px; height: 36px; background: linear-gradient(135deg, #4caf50, #8bc34a); color: #fff; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; }
.news-image { width: 100px; height: 70px; border-radius: 8px; overflow: hidden; }
.news-image img { width: 100%; height: 100%; object-fit: cover; }

.news-card-middle { flex: 1; min-width: 0; }
.news-title { font-size: 15px; font-weight: 600; color: #1a1a1a; margin: 0 0 6px; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.news-desc { font-size: 13px; color: #666; margin: 0 0 10px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.news-meta { display: flex; justify-content: space-between; margin-bottom: 8px; }
.meta-left, .meta-right { display: flex; gap: 12px; font-size: 12px; color: #999; }
.news-date, .news-source, .news-views, .news-likes { display: flex; align-items: center; gap: 4px; }
.news-date i, .news-source i, .news-views i, .news-likes i { color: #4caf50; }

.news-tags { display: flex; gap: 6px; flex-wrap: wrap; }
.tag-chip { font-size: 11px; }

.news-card-right { display: flex; align-items: center; }
.read-btn { background: linear-gradient(135deg, #4caf50, #2e7d32); border: none; white-space: nowrap; }

.pagination-section { display: flex; justify-content: center; padding-top: 20px; border-top: 1px solid #f0f0f0; }

.empty-state { text-align: center; padding: 60px 20px; }
.empty-icon { font-size: 64px; color: #ddd; margin-bottom: 16px; }
.empty-state h3 { margin: 0 0 8px; color: #333; }
.empty-state p { color: #999; margin: 0 0 16px; }

/* 侧边栏 */
.sidebar { width: 300px; flex-shrink: 0; display: flex; flex-direction: column; gap: 16px; }

.sidebar-section { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.sidebar-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.sidebar-title { display: flex; align-items: center; gap: 8px; font-size: 15px; font-weight: 600; color: #333; margin: 0 0 16px; }
.sidebar-title i { color: #4caf50; }
.sidebar-section > .sidebar-title { margin-bottom: 16px; }

.hot-tag { font-size: 10px; }

.hot-list { display: flex; flex-direction: column; gap: 12px; }
.hot-item { display: flex; align-items: center; gap: 12px; cursor: pointer; padding: 8px; border-radius: 8px; transition: all 0.2s; }
.hot-item:hover { background: #f5f7fa; }
.hot-rank { width: 24px; height: 24px; background: #e8e8e8; color: #666; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 12px; flex-shrink: 0; }
.hot-rank.top-1 { background: linear-gradient(135deg, #ffd700, #ffb800); color: #fff; }
.hot-rank.top-2 { background: linear-gradient(135deg, #c0c0c0, #a8a8a8); color: #fff; }
.hot-rank.top-3 { background: linear-gradient(135deg, #cd7f32, #b87333); color: #fff; }
.hot-content { flex: 1; min-width: 0; }
.hot-title { font-size: 13px; font-weight: 500; color: #333; margin: 0 0 4px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.hot-meta { display: flex; gap: 12px; font-size: 11px; color: #999; }
.hot-meta span { display: flex; align-items: center; gap: 2px; }

.category-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
.category-item { display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 12px 8px; background: #f5f7fa; border-radius: 8px; cursor: pointer; transition: all 0.2s; border: 2px solid transparent; }
.category-item:hover { background: #e8f5e9; border-color: #4caf50; }
.category-item.active { background: #e8f5e9; border-color: #4caf50; }
.category-item i { font-size: 20px; color: #4caf50; }
.category-item span { font-size: 12px; color: #333; }

.stats-list { display: flex; flex-direction: column; gap: 12px; }
.stat-item { display: flex; justify-content: space-between; padding: 10px 12px; background: #f5f7fa; border-radius: 6px; }
.stat-item .stat-label { font-size: 13px; color: #666; }
.stat-item .stat-value { font-size: 14px; font-weight: 600; color: #4caf50; }

.link-list { display: flex; flex-direction: column; gap: 8px; }
.quick-link { display: flex; align-items: center; gap: 8px; padding: 10px 12px; background: #f5f7fa; border-radius: 6px; color: #333; text-decoration: none; font-size: 13px; transition: all 0.2s; }
.quick-link:hover { background: #e8f5e9; color: #4caf50; }
.quick-link i { color: #4caf50; }

@media (max-width: 1200px) {
  .content-wrapper { flex-direction: column; }
  .sidebar { width: 100%; flex-direction: row; flex-wrap: wrap; }
  .sidebar-section { flex: 1; min-width: 280px; }
}

@media (max-width: 768px) {
  .stats-cards { grid-template-columns: repeat(2, 1fr); }
  .featured-grid { grid-template-columns: 1fr; }
  .news-card { flex-direction: column; }
  .news-card-right { justify-content: center; }
}
</style>
