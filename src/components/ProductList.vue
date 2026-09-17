<template>
  <div class="product-list">
    <div v-if="title" class="list-header">
      <h3>{{ title }}</h3>
      <slot name="extra"></slot>
    </div>
    
    <div class="products">
      <div
        v-for="(item, index) in products"
        :key="`${item.id}-${index}`"
        class="product-item"
        @click="$emit('view-detail', item.id)"
      >
        <div class="product-image">
          <img :src="getImage(item)" class="product-img" />
          <div v-if="item.isNew" class="badge badge-new">新品</div>
          <div v-else-if="item.isHot" class="badge badge-hot">热销</div>
          <div v-else-if="item.discount" class="badge badge-discount">折扣</div>
        </div>
        
        <div class="product-content">
          <h4 class="product-name">{{ item.name }}</h4>
          <p class="product-desc">{{ item.description || item.desc }}</p>
          
          <div class="product-meta">
            <span v-if="item.category" class="category-tag">{{ getCategoryLabel(item.category) }}</span>
            <span v-if="item.merchantName" class="merchant">{{ item.merchantName }}</span>
          </div>
          
          <div class="product-footer">
            <div class="price-info">
              <span class="current-price">¥{{ formatPrice(item.price) }}</span>
              <span v-if="item.originalPrice && item.originalPrice > item.price" class="original-price">
                ¥{{ formatPrice(item.originalPrice) }}
              </span>
            </div>
            <div class="product-actions">
              <el-button size="mini" type="text" @click.stop="handleToggleFavorite(item)" class="favorite-btn">
                <i :class="isFavorited(item.id) ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
              </el-button>
              <el-button size="mini" type="primary" @click.stop="handleAddToCart(item)" class="add-btn">
                <i class="el-icon-shopping-cart-2"></i>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="products.length === 0" class="empty-list">
      <i class="el-icon-goods"></i>
      <p>暂无商品</p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from '@vue/composition-api'
import { getUserFavorites, addFavorite, removeFavorite } from '@/api'
import { addToCart as addToCartApi } from '@/data/cart'
import { formatPrice, getFullImageUrl, getCategoryInfo, notifyCartChanged, notifyFavoritesChanged, isLoggedIn } from '@/utils/common'

export default {
  name: 'ProductList',
  props: {
    products: { type: Array, required: true, default: () => [] },
    title: { type: String, default: '' }
  },
  emits: ['view-detail', 'add-cart', 'toggle-favorite'],
  setup(props, { emit }) {
    const favorites = ref([])

    // 加载收藏状态
    const loadFavorites = async () => {
      if (!isLoggedIn()) {
        favorites.value = []
        return
      }
      try {
        const res = await getUserFavorites()
        favorites.value = res.data || []
      } catch {
        favorites.value = []
      }
    }

    // 获取图片
    const getImage = (item) => {
      const img = item.image || item.img
      return getFullImageUrl(img, null, item.id)
    }

    // 获取分类标签
    const getCategoryLabel = (category) => {
      return getCategoryInfo(category).text
    }

    // 是否已收藏
    const isFavorited = (id) => {
      return favorites.value.some(f => Number(f.targetId || f.target_id) === Number(id))
    }

    // 切换收藏
    const handleToggleFavorite = async (item) => {
      if (!isLoggedIn()) {
        return
      }
      try {
        if (isFavorited(item.id)) {
          await removeFavorite(item.id)
        } else {
          await addFavorite({ productId: item.id })
        }
        notifyFavoritesChanged()
        await loadFavorites()
      } catch {}
    }

    // 加入购物车
    const handleAddToCart = async (item) => {
      if ((item.stock || 0) === 0) {
        return
      }
      try {
        await addToCartApi({ productId: item.id, quantity: 1 })
        notifyCartChanged()
        emit('add-cart', item)
      } catch {}
    }

    onMounted(() => {
      loadFavorites()
      window.addEventListener('xm-favorites-changed', loadFavorites)
    })

    onUnmounted(() => {
      window.removeEventListener('xm-favorites-changed', loadFavorites)
    })

    return {
      getImage,
      getCategoryLabel,
      isFavorited,
      handleToggleFavorite,
      handleAddToCart,
      formatPrice
    }
  }
}
</script>

<style scoped>
.product-list { margin: 20px 0; }
.list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.list-header h3 { margin: 0; font-size: 18px; color: #333; }

.products { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }

.product-item { background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.06); transition: all 0.3s; cursor: pointer; display: flex; flex-direction: column; }
.product-item:hover { transform: translateY(-4px); box-shadow: 0 8px 20px rgba(0,0,0,0.1); }

.product-image { position: relative; height: 160px; background: #f5f5f5; }
.product-image .product-img { width: 100%; height: 100%; object-fit: cover; }

.image-error { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 32px; color: #ccc; }

.badge { position: absolute; top: 8px; left: 8px; padding: 2px 8px; border-radius: 4px; font-size: 12px; color: white; }
.badge-new { background: linear-gradient(135deg, #667eea, #764ba2); }
.badge-hot { background: linear-gradient(135deg, #f093fb, #f5576c); }
.badge-discount { background: linear-gradient(135deg, #ff9800, #ff5722); }

.product-content { padding: 12px; flex: 1; display: flex; flex-direction: column; }

.product-name { margin: 0 0 6px; font-size: 14px; font-weight: 600; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.product-desc { margin: 0 0 8px; font-size: 12px; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }

.product-meta { display: flex; gap: 8px; margin-bottom: 8px; flex-wrap: wrap; }
.category-tag { background: #f0f9ff; color: #409eff; padding: 2px 6px; border-radius: 4px; font-size: 11px; }
.merchant { color: #909399; font-size: 11px; }

.product-footer { display: flex; justify-content: space-between; align-items: center; margin-top: auto; }

.price-info { display: flex; flex-direction: column; }
.current-price { font-size: 18px; font-weight: bold; color: #ff6b6b; }
.original-price { font-size: 12px; color: #999; text-decoration: line-through; }

.product-actions { display: flex; gap: 6px; }
.favorite-btn { padding: 4px 6px; color: #999; }
.favorite-btn:hover { color: #ff9800; }
.add-btn { padding: 4px 8px; }

.empty-list { text-align: center; padding: 40px; color: #999; }
.empty-list i { font-size: 48px; color: #ddd; }
</style>
