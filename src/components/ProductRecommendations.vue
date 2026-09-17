/**
 * 商品推荐组件
 * 文件路径: src/components/ProductRecommendations.vue
 * 功能描述: 根据当前商品推荐相关商品
 * 关联文件:
 * - src/views/front/ProductDetail.vue: 商品详情页面
 * - src/data/productStore.js: 商品数据
 */
<template>
  <div class="product-recommendations">
    <h3>你可能还喜欢</h3>
    <div v-if="recommended.length > 0" class="rec-list">
      <div v-for="p in recommended" :key="p.id" class="rec-item" @click="go(p.id)">
        <img :src="getImageUrl(p.image || p.img)" :alt="p.name" />
        <div class="info">
          <div class="name">{{ p.name }}</div>
          <div class="price">￥{{ p.price }}</div>
        </div>
      </div>
    </div>
    <div v-else class="no-recommendations">暂无推荐商品</div>
  </div>
</template>

<script>
import { getApprovedProducts } from '@/data/productStore'

export default {
  name: 'ProductRecommendations',
  props: {
    currentProductId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      allProducts: []
    }
  },
  computed: {
    recommended() {
      if (!this.allProducts || !Array.isArray(this.allProducts) || this.allProducts.length === 0) {
        return []
      }

      const currentId = Number(this.currentProductId)
      const current = this.allProducts.find(p => Number(p.id) === currentId)

      let list = []
      if (current && current.category) {
        // 优先选择同类别的商品
        list = this.allProducts.filter(p =>
          Number(p.id) !== currentId && p.category === current.category
        )
      }

      // 如果同类商品不足，补充其他商品
      if (list.length < 4) {
        const extra = this.allProducts
          .filter(p => Number(p.id) !== currentId && !list.some(l => l.id === p.id))
          .slice(0, 4 - list.length)
        list = list.concat(extra)
      }

      return list.slice(0, 4)
    }
  },
  created() {
    this.loadProducts()
  },
  watch: {
    currentProductId() {
      // 当 productId 变化时，不需要重新加载商品列表
    }
  },
  methods: {
    async loadProducts() {
      try {
        const products = await getApprovedProducts()
        this.allProducts = Array.isArray(products) ? products : []
      } catch (error) {
        console.error('加载推荐商品失败:', error)
        this.allProducts = []
      }
    },
    getImageUrl(path) {
      if (!path || path === 'null' || path === 'undefined') {
        const index = Math.floor(Math.random() * 11) + 1
        return `/imgs/foods/${index}.png`
      }
      if (path.startsWith('http') || path.startsWith('data:')) {
        return path
      }
      if (path.startsWith('upload')) {
        return `${process.env.VUE_APP_BASEURL ? process.env.VUE_APP_BASEURL.replace('/api', '') : ''}/upload/${path.replace('upload/', '')}`
      }
      if (path.startsWith('imgs') || path.startsWith('/imgs')) {
        return path.startsWith('/') ? path : '/' + path
      }
      // 默认商品图片
      const index = Math.floor(Math.random() * 11) + 1
      return `/imgs/foods/${index}.png`
    },
    go(id) { this.$router.push(`/front/product/${id}`) }
  }
}
</script>

<style scoped>
.product-recommendations { margin-top: 24px; padding: 16px; border-top: 1px solid #f0f0f0 }
.rec-list { display:flex; gap:12px; margin-top:12px; flex-wrap: wrap; }
.rec-item { width:200px; cursor:pointer; background:#fff; border-radius:8px; overflow:hidden; box-shadow:0 1px 6px rgba(0,0,0,0.03); transition: all 0.3s; }
.rec-item:hover { transform: translateY(-4px); box-shadow: 0 8px 20px rgba(0,0,0,0.12); }
.rec-item img { width:100%; height:120px; object-fit:cover }
.rec-item .info { padding:12px }
.rec-item .name { font-weight:600; margin-bottom:6px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rec-item .price { color:#e67e22; font-weight: 600; }
.no-recommendations { text-align: center; color: #999; padding: 20px; font-size: 14px; }

@media (max-width: 768px) {
  .rec-list { flex-direction: column; gap: 16px; }
  .rec-item { width: 100%; }
  .rec-item img { height: 180px; }
}
</style>
