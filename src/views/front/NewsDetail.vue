/**
 * 资讯详情页面
 * 文件路径: src/views/front/NewsDetail.vue
 * 功能描述: 展示单篇农业资讯完整内容，面包屑导航（首页→资讯中心→分类），文章头部含分类标签和关键词标签，
 *           文章正文富文内展示、发布时间/作者/浏览量等元信息，相关推荐文章列表，分享按钮和返回操作
 * 关联文件:
 * - src/api/index.js: 提供资讯详情和推荐文章接口
 * - src/views/front/News.vue: 资讯列表页面
 */
<template>
  <div class="news-detail-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/front/news' }">资讯中心</el-breadcrumb-item>
        <el-breadcrumb-item v-if="article">{{ getCategoryText(article.category) }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- 文章详情 -->
    <div v-else-if="article" class="news-detail-container">
      <!-- 主内容区 -->
      <main class="article-main">
        <article class="article-card">
          <!-- 文章头部 -->
          <header class="article-header">
            <div class="article-tags">
              <el-tag type="success" size="small">{{ getCategoryText(article.category) }}</el-tag>
              <el-tag v-for="tag in parseTags(article.tags)" :key="tag" type="warning" size="small">{{ tag }}</el-tag>
            </div>
            <h1 class="article-title">{{ article.title }}</h1>
            <p class="article-description">{{ article.description || article.excerpt }}</p>
            
            <div class="article-meta">
              <div class="meta-item" v-if="article.author">
                <i class="el-icon-user-solid"></i>
                <span>{{ article.author }}</span>
              </div>
              <div class="meta-item">
                <i class="el-icon-calendar"></i>
                <span>{{ formatDate(article.createTime) }}</span>
              </div>
              <div class="meta-item" v-if="article.source">
                <i class="el-icon-office-building"></i>
                <span>{{ article.source }}</span>
              </div>
              <div class="meta-item">
                <i class="el-icon-view"></i>
                <span>{{ article.views || 0 }} 阅读</span>
              </div>
            </div>
          </header>

          <!-- 文章封面图 -->
          <div v-if="article.image" class="article-cover">
            <el-image
              :src="getImageUrl(article.image)"
              :alt="article.title"
              fit="cover"
              class="cover-image"
              :preview-src-list="[getImageUrl(article.image)]"
              :lazy="true"
            >
              <template v-slot:error>
                <div class="image-placeholder">
                  <i class="el-icon-picture"></i>
                </div>
              </template>
            </el-image>
          </div>

          <!-- 文章正文 -->
          <div class="article-content" v-html="article.content"></div>

          <!-- 文章底部操作 -->
          <footer class="article-footer">
            <div class="footer-left">
              <el-button
                :type="isLiked ? 'danger' : 'default'"
                :icon="isLiked ? 'el-icon-star-on' : 'el-icon-star-off'"
                @click="handleLike"
                :loading="likeLoading"
              >
                点赞 ({{ article.likes || 0 }})
              </el-button>
              <el-button icon="el-icon-share" @click="shareArticle">
                分享 ({{ article.shares || 0 }})
              </el-button>
            </div>
            <el-button type="primary" icon="el-icon-arrow-left" @click="$router.push('/front/news')">
              返回列表
            </el-button>
          </footer>
        </article>

        <!-- 上下篇导航 -->
        <div class="article-nav">
          <div class="nav-item prev" v-if="prevArticle" @click="goToArticle(prevArticle.id)">
            <div class="nav-label"><i class="el-icon-arrow-left"></i> 上一篇</div>
            <div class="nav-title">{{ prevArticle.title }}</div>
          </div>
          <div class="nav-item next" v-if="nextArticle" @click="goToArticle(nextArticle.id)">
            <div class="nav-label">下一篇 <i class="el-icon-arrow-right"></i></div>
            <div class="nav-title">{{ nextArticle.title }}</div>
          </div>
        </div>
      </main>

      <!-- 侧边栏 -->
      <aside class="article-sidebar">
        <!-- 相关推荐 -->
        <div class="sidebar-card">
          <h3 class="sidebar-title">
            <i class="el-icon-reading"></i>
            相关推荐
          </h3>
          <div class="related-list">
            <div 
              v-for="item in relatedArticles" 
              :key="item.id"
              class="related-item"
              @click="goToArticle(item.id)"
            >
              <div class="related-image">
                <img :src="getImageUrl(item.image)" :alt="item.title" @error="handleImageError" />
              </div>
              <div class="related-content">
                <div class="related-category">{{ getCategoryText(item.category) }}</div>
                <div class="related-title">{{ item.title }}</div>
                <div class="related-meta">
                  <span><i class="el-icon-view"></i> {{ item.views || 0 }}</span>
                  <span><i class="el-icon-time"></i> {{ formatDate(item.createTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 浏览历史 -->
        <div class="sidebar-card">
          <h3 class="sidebar-title">
            <i class="el-icon-time"></i>
            浏览历史
          </h3>
          <div class="history-list">
            <div 
              v-for="item in browseHistory" 
              :key="item.id"
              class="history-item"
              @click="goToArticle(item.id)"
            >
              <div class="history-title">{{ item.title }}</div>
              <div class="history-time">{{ formatRelativeTime(item.viewTime) }}</div>
            </div>
            <div v-if="browseHistory.length === 0" class="history-empty">
              暂无浏览记录
            </div>
          </div>
        </div>

        <!-- 快速链接 -->
        <div class="sidebar-card">
          <h3 class="sidebar-title">
            <i class="el-icon-link"></i>
            快速链接
          </h3>
          <div class="quick-links">
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

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">
        <i class="el-icon-document-delete"></i>
      </div>
      <h3>未找到文章</h3>
      <p>抱歉，没有找到对应的资讯信息</p>
      <el-button type="primary" @click="$router.push('/front/news')">
        返回列表
      </el-button>
    </div>
  </div>
</template>

<script>
import { getNewsPage, getNewsById, likeNews, shareNews } from '@/api'

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

export default {
  name: 'NewsDetail',
  data() {
    return {
      article: null,
      loading: false,
      isLiked: false,
      likeLoading: false,
      allArticles: [],
      prevArticle: null,
      nextArticle: null,
      browseHistory: []
    }
  },
  computed: {
    relatedArticles() {
      if (!this.article) return []
      return this.allArticles
        .filter(item => item.id !== this.article.id && item.category === this.article.category)
        .slice(0, 4)
    }
  },
  created() {
    this.loadAllArticles()
    this.loadArticle()
    this.loadBrowseHistory()
  },
  watch: {
    '$route.params.id': function() {
      this.loadArticle()
      this.loadBrowseHistory()
    }
  },
  methods: {
    getImageUrl(path) {
      if (!path) return '/imgs/foods/1.png'
      if (path.startsWith('http') || path.startsWith('data:')) return path
      if (path.startsWith('upload') || /^\d{8}\//.test(path)) {
        const baseUrl = (process.env.VUE_APP_BASEURL || '').replace('/api', '') || 'http://localhost:9090'
        return `${baseUrl}/upload/${path.replace(/^.*upload\//, '')}`
      }
      return path.startsWith('/') ? path : '/' + path
    },

    handleImageError(e) {
      e.target.src = '/imgs/foods/1.png'
    },

    formatDate(date) {
      if (!date) return '-'
      const d = new Date(date)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },

    formatRelativeTime(timeStr) {
      if (!timeStr) return ''
      const now = new Date()
      const time = new Date(timeStr)
      const diff = now - time
      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(diff / 3600000)
      const days = Math.floor(diff / 86400000)
      
      if (minutes < 1) return '刚刚'
      if (minutes < 60) return `${minutes}分钟前`
      if (hours < 24) return `${hours}小时前`
      if (days < 7) return `${days}天前`
      return this.formatDate(timeStr)
    },

    getCategoryText(category) {
      return categoryMap[category] || category || '其他'
    },

    parseTags(tags) {
      if (!tags) return []
      if (Array.isArray(tags)) return tags.slice(0, 3)
      return tags.split(',').map(t => t.trim()).filter(Boolean).slice(0, 3)
    },

    async loadAllArticles() {
      try {
        const res = await getNewsPage({ page: 1, pageSize: 100 })
        const result = res?.data || res
        this.allArticles = result.list || result.records || []
      } catch (e) {}
    },

    async loadArticle() {
      const id = this.$route.params.id
      if (!id) {
        this.article = null
        return
      }

      this.loading = true
      try {
        const res = await getNewsById(id)
        this.article = res.data || res
        
        if (this.article?.title) {
          document.title = `${this.article.title} - 资讯详情`
          this.updateBrowseHistory()
          this.updatePrevNext()
        }
      } catch (error) {
        console.error('加载文章失败:', error)
        this.article = null
      } finally {
        this.loading = false
      }
    },

    updateBrowseHistory() {
      if (!this.article) return
      const history = JSON.parse(localStorage.getItem('xm-news-history') || '[]')
      const filtered = history.filter(h => h.id !== this.article.id)
      filtered.unshift({
        id: this.article.id,
        title: this.article.title,
        image: this.article.image,
        viewTime: new Date().toISOString()
      })
      localStorage.setItem('xm-news-history', JSON.stringify(filtered.slice(0, 10)))
    },

    loadBrowseHistory() {
      this.browseHistory = JSON.parse(localStorage.getItem('xm-news-history') || '[]').slice(0, 5)
    },

    updatePrevNext() {
      if (!this.article || this.allArticles.length === 0) {
        this.prevArticle = null
        this.nextArticle = null
        return
      }
      
      const sorted = [...this.allArticles].sort((a, b) => 
        new Date(b.createTime || 0) - new Date(a.createTime || 0)
      )
      const index = sorted.findIndex(item => item.id === this.article.id)
      
      this.prevArticle = index > 0 ? sorted[index - 1] : null
      this.nextArticle = index < sorted.length - 1 ? sorted[index + 1] : null
    },

    goToArticle(id) {
      this.$router.push(`/front/news/${id}`)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },

    async handleLike() {
      if (!this.article?.id || this.likeLoading) return
      
      this.likeLoading = true
      try {
        await likeNews(this.article.id)
        this.isLiked = true
        this.article.likes = (this.article.likes || 0) + 1
        this.$message.success('点赞成功')
      } catch (error) {
        this.$message.error('点赞失败')
      } finally {
        this.likeLoading = false
      }
    },

    shareArticle() {
      const url = window.location.href
      const title = this.article?.title || '资讯分享'

      if (navigator.share) {
        navigator.share({ title, url })
          .then(() => this.incrementShare())
          .catch(() => this.copyLink(url))
      } else {
        this.copyLink(url)
      }
    },

    copyLink(url) {
      navigator.clipboard.writeText(url)
        .then(() => {
          this.$message.success('链接已复制到剪贴板')
          this.incrementShare()
        })
        .catch(() => this.$message.warning('复制失败，请手动复制'))
    },

    async incrementShare() {
      if (!this.article?.id) return
      try {
        await shareNews(this.article.id)
        this.article.shares = (this.article.shares || 0) + 1
      } catch (e) {}
    }
  }
}
</script>

<style scoped>
.news-detail-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 200px);
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.breadcrumb {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.loading-state {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.news-detail-container {
  display: flex;
  gap: 24px;
}

/* 主内容区 */
.article-main { flex: 1; min-width: 0; }

.article-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.article-header {
  padding: 30px;
  border-bottom: 1px solid #f0f0f0;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.article-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 12px;
  line-height: 1.4;
}

.article-description {
  font-size: 16px;
  color: #666;
  margin: 0 0 20px;
  line-height: 1.7;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  color: #999;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-item i {
  color: #4caf50;
}

.article-cover {
  width: 100%;
  max-height: 450px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  display: block;
}

.image-placeholder {
  width: 100%;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #ccc;
  font-size: 48px;
}

.article-content {
  padding: 30px;
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}

.article-content ::v-deep h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 28px 0 14px;
  padding-left: 14px;
  border-left: 4px solid #4caf50;
}

.article-content ::v-deep h3 {
  font-size: 18px;
  font-weight: 600;
  color: #4caf50;
  margin: 24px 0 12px;
}

.article-content ::v-deep p {
  margin: 14px 0;
  text-indent: 2em;
}

.article-content ::v-deep ul,
.article-content ::v-deep ol {
  margin: 14px 0;
  padding-left: 28px;
}

.article-content ::v-deep li {
  margin: 8px 0;
}

.article-content ::v-deep blockquote {
  margin: 20px 0;
  padding: 14px 20px;
  background: #f8f9fa;
  border-left: 4px solid #4caf50;
  border-radius: 0 6px 6px 0;
  color: #666;
}

.article-content ::v-deep img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 16px 0;
}

.article-content ::v-deep a {
  color: #4caf50;
  text-decoration: none;
  border-bottom: 1px solid transparent;
}

.article-content ::v-deep a:hover {
  border-bottom-color: #4caf50;
}

.article-footer {
  padding: 20px 30px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
}

.footer-left {
  display: flex;
  gap: 12px;
}

/* 上下篇导航 */
.article-nav {
  display: flex;
  gap: 16px;
  margin-top: 20px;
}

.nav-item {
  flex: 1;
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.nav-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.nav-item.prev { border-left: 3px solid #4caf50; }
.nav-item.next { border-right: 3px solid #4caf50; text-align: right; }

.nav-label {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item.next .nav-label {
  justify-content: flex-end;
}

.nav-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 侧边栏 */
.article-sidebar {
  width: 340px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #4caf50;
}

.sidebar-title i {
  color: #4caf50;
}

/* 相关推荐 */
.related-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.related-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s;
}

.related-item:hover {
  background: #f5f7fa;
}

.related-image {
  width: 80px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.related-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-content { flex: 1; min-width: 0; }

.related-category {
  font-size: 11px;
  color: #4caf50;
  margin-bottom: 4px;
}

.related-title {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.related-meta {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: #999;
}

.related-meta span {
  display: flex;
  align-items: center;
  gap: 2px;
}

/* 浏览历史 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.history-item:hover {
  background: #e8f5e9;
}

.history-title {
  font-size: 13px;
  color: #333;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.history-time {
  font-size: 11px;
  color: #999;
}

.history-empty {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 13px;
}

/* 快速链接 */
.quick-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quick-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  color: #333;
  text-decoration: none;
  font-size: 13px;
  transition: all 0.2s;
}

.quick-link:hover {
  background: #e8f5e9;
  color: #4caf50;
}

.quick-link i {
  color: #4caf50;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
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

/* 响应式 */
@media (max-width: 1200px) {
  .news-detail-container {
    flex-direction: column;
  }
  
  .article-sidebar {
    width: 100%;
    flex-direction: row;
    flex-wrap: wrap;
  }
  
  .sidebar-card {
    flex: 1;
    min-width: 280px;
  }
}

@media (max-width: 768px) {
  .article-title {
    font-size: 22px;
  }
  
  .article-header,
  .article-content,
  .article-footer {
    padding: 20px;
  }
  
  .article-nav {
    flex-direction: column;
  }
  
  .article-footer {
    flex-direction: column;
    gap: 12px;
  }
  
  .footer-left {
    width: 100%;
    justify-content: center;
  }
}
</style>
