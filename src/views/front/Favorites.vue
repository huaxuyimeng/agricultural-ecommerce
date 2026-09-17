/**
 * 收藏夹页面
 * 文件路径: src/views/front/Favorites.vue
 * 功能描述: 用户收藏管理，Tab切换商品收藏/资讯收藏，商品收藏卡片展示（图片、名称、描述、价格、新品/热销标签），
 *           支持收藏/取消收藏、加入购物车、点击跳转商品详情，资讯收藏支持取消收藏和阅读
 * 关联文件:
 * - src/api/index.js: 提供收藏数据接口
 * - src/views/front/ProductDetail.vue: 商品详情页面
 * - src/views/front/NewsDetail.vue: 资讯详情页面
 */
<template>
  <div class="favorites-page">
    <div class="page-header">
      <h2><i class="el-icon-star-on"></i> 我的收藏</h2>
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="商品收藏" name="products" />
        <el-tab-pane label="资讯收藏" name="news" />
      </el-tabs>
    </div>

    <!-- 商品收藏 -->
    <div v-if="activeTab === 'products'" class="favorites-content">
      <div v-if="products.length > 0" class="favorites-grid">
        <div v-for="product in products" :key="product.id" class="favorite-card">
          <div class="card-image" @click="goProduct(product)">
            <img :src="getImage(product)" class="card-img" />
            <div v-if="product.isNew" class="card-badge new">新品</div>
            <div v-else-if="product.isHot" class="card-badge hot">热销</div>
          </div>
          <div class="card-body">
            <h4 class="card-title" @click="goProduct(product)">{{ product.name }}</h4>
            <p class="card-desc">{{ product.description || product.desc }}</p>
            <div class="card-footer">
              <span class="card-price">¥{{ formatPrice(product.price) }}</span>
              <div class="card-actions">
                <el-button type="text" size="small" @click="handleRemove(product, 'product')">
                  <i class="el-icon-delete"></i>
                </el-button>
                <el-button type="primary" size="small" @click="handleAddToCart(product)">
                  <i class="el-icon-shopping-cart-2"></i> 加购
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon"><i class="el-icon-star-off"></i></div>
        <h3>暂无收藏商品</h3>
        <p>快去发现心仪的商品吧</p>
        <el-button type="primary" @click="goProducts">去逛逛</el-button>
      </div>
    </div>

    <!-- 资讯收藏 -->
    <div v-if="activeTab === 'news'" class="favorites-content">
      <div v-if="newsList.length > 0" class="news-list">
        <div v-for="item in newsList" :key="item.id" class="news-card">
          <img :src="getImage(item)" class="news-image" @click="goNews(item)" />
          <div class="news-content">
            <h4 class="news-title" @click="goNews(item)">{{ item.title }}</h4>
            <p class="news-desc">{{ item.description || item.content }}</p>
            <div class="news-footer">
              <span class="news-time">{{ formatDateTime(item.createTime || item.createAt) }}</span>
              <div class="news-actions">
                <el-button type="text" size="small" @click="goNews(item)">查看</el-button>
                <el-button type="text" size="small" @click="handleRemove(item, 'news')">取消</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon"><i class="el-icon-star-off"></i></div>
        <h3>暂无收藏资讯</h3>
        <p>快去阅读感兴趣的资讯吧</p>
        <el-button type="primary" @click="goNewsList">去看看</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from '@vue/composition-api'
import { getUserFavorites, removeFavorite as apiRemoveFavorite, addToCart as addToCartApi } from '@/api'
import { formatPrice, formatDateTime, getFullImageUrl, notifyFavoritesChanged, notifyCartChanged, isLoggedIn } from '@/utils/common'

export default {
  name: 'Favorites',
  setup() {
    const activeTab = ref('products')
    const products = ref([])
    const newsList = ref([])
    const loading = ref(false)

    // 跳转商品详情
    const goProduct = (product) => {
      window.location.href = `/front/product/${product.id}`
    }

    // 跳转商品列表
    const goProducts = () => {
      window.location.href = '/front/products'
    }

    // 跳转资讯详情
    const goNews = (item) => {
      window.location.href = `/front/news/${item.id}`
    }

    // 跳转资讯列表
    const goNewsList = () => {
      window.location.href = '/front/news'
    }

    // 加载收藏
    const loadFavorites = async () => {
      loading.value = true
      try {
        const res = await getUserFavorites()
        const favorites = (res && res.data) ? res.data : (Array.isArray(res) ? res : [])
        
        // 处理商品收藏 - 兼容不同数据格式
        const productFavorites = favorites.filter(f => {
          if (f.type === 'product') return true
          if (!f.type && f.product) return true
          if (!f.type && !f.news && f.id) return true
          return false
        })
        products.value = productFavorites.map(f => f.product || f).filter(p => p && p.id)
        
        // 处理资讯收藏 - 兼容不同数据格式
        const newsFavorites = favorites.filter(f => {
          if (f.type === 'news') return true
          if (!f.type && f.news) return true
          return false
        })
        newsList.value = newsFavorites.map(f => f.news || f).filter(n => n && n.id)
      } catch (error) {
        console.error('加载收藏失败:', error)
        products.value = []
        newsList.value = []
      } finally {
        loading.value = false
      }
    }

    // 获取图片
    const getImage = (item) => {
      return getFullImageUrl(item.image || item.img || item.cover, null, item.id)
    }

    // 移除收藏
    const handleRemove = async (item, type) => {
      try {
        await apiRemoveFavorite(item.id, type === 'news')
        notifyFavoritesChanged()
        loadFavorites()
      } catch {}
    }

    // 加入购物车
    const handleAddToCart = async (product) => {
      try {
        await addToCartApi({ productId: product.id, quantity: 1 })
        notifyCartChanged()
      } catch {}
    }

    // 切换标签
    const handleTabClick = () => {}

    onMounted(() => {
      if (!isLoggedIn()) {
        window.location.href = '/login'
        return
      }
      loadFavorites()
      window.addEventListener('xm-favorites-changed', loadFavorites)
    })

    onUnmounted(() => {
      window.removeEventListener('xm-favorites-changed', loadFavorites)
    })

    return {
      activeTab,
      products,
      newsList,
      loading,
      loadFavorites,
      getImage,
      goProduct,
      goProducts,
      goNews,
      goNewsList,
      handleRemove,
      handleAddToCart,
      handleTabClick,
      formatPrice,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.favorites-page { max-width: 1200px; margin: 20px auto; padding: 20px; }

.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0 0 16px; font-size: 20px; color: #303133; display: flex; align-items: center; gap: 8px; }
.page-header h2 i { color: #ff9800; }

.favorites-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px; }
.favorite-card { background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); transition: all 0.3s; }
.favorite-card:hover { transform: translateY(-4px); box-shadow: 0 8px 20px rgba(0,0,0,0.12); }

.card-image { position: relative; height: 160px; cursor: pointer; overflow: hidden; }
.card-image .card-img { width: 100%; height: 100%; object-fit: cover; }
.card-badge { position: absolute; top: 8px; left: 8px; padding: 2px 8px; border-radius: 4px; font-size: 12px; color: white; }
.card-badge.new { background: linear-gradient(135deg, #667eea, #764ba2); }
.card-badge.hot { background: linear-gradient(135deg, #f093fb, #f5576c); }

.card-body { padding: 12px; }
.card-title { margin: 0 0 8px; font-size: 14px; color: #333; cursor: pointer; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-title:hover { color: #ff9800; }
.card-desc { margin: 0 0 12px; font-size: 12px; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.card-price { font-size: 18px; font-weight: bold; color: #ff9800; }
.card-actions { display: flex; gap: 8px; align-items: center; }

.news-list { display: flex; flex-direction: column; gap: 12px; }
.news-card { display: flex; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.news-image { width: 160px; height: 120px; flex-shrink: 0; object-fit: cover; }
.news-content { flex: 1; padding: 16px; display: flex; flex-direction: column; }
.news-title { margin: 0 0 8px; font-size: 16px; color: #333; cursor: pointer; }
.news-title:hover { color: #ff9800; }
.news-desc { flex: 1; margin: 0 0 12px; font-size: 13px; color: #999; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; }
.news-footer { display: flex; justify-content: space-between; align-items: center; }
.news-time { font-size: 12px; color: #999; }
.news-actions { display: flex; gap: 8px; }

.empty-state { text-align: center; padding: 60px 20px; background: white; border-radius: 8px; }
.empty-icon { font-size: 64px; color: #ddd; margin-bottom: 16px; }
.empty-state h3 { margin: 0 0 8px; color: #666; }
.empty-state p { margin: 0 0 20px; color: #999; }
</style>
