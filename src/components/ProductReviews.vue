/**
 * 商品评论组件
 * 文件路径: src/components/ProductReviews.vue
 * 功能描述: 显示商品评价并允许用户提交新评价
 * 关联文件:
 * - src/views/front/ProductDetail.vue: 商品详情页面
 */
<template>
  <div class="product-reviews">
    <div class="header-row">
      <h3>用户评价 <span class="count">({{ reviews.length }})</span></h3>
      <div class="meta">
        <span class="real-count">真实评价：{{ persistedCount }}</span>
      </div>
    </div>

    <div v-if="reviews.length" class="reviews-list">
      <div v-for="(r, idx) in reviews" :key="idx" class="review-item" :class="{ sample: r.isSample }">
        <div class="review-top">
          <div class="author-info">
            <el-avatar v-if="r.avatar" :src="getMerchantAvatar(r.avatar)" size="small" />
            <el-avatar v-else size="small">{{ (r.name && r.name.charAt(0)) || 'U' }}</el-avatar>
            <div class="author-block">
              <div class="author-row">
                <span class="author">{{ r.name }}</span>
                <span v-if="r.isSample" class="sample-badge">样例</span>
              </div>
              <el-rate :value="r.rating" disabled show-text class="rating-inline" />
            </div>
          </div>
          <div class="time">{{ r.time }}</div>
        </div>
        <div class="review-content">{{ r.content }}</div>
      </div>
    </div>
    <div v-else class="no-reviews">还没有评价，快来成为第一个评价的人吧！</div>

    <div class="add-review">
      <h4>写下你的评价</h4>
      <div class="form-layout">
        <div class="left-col">
          <div v-if="isLoggedIn" class="user-box">
            <el-avatar :src="currentUser && getMerchantAvatar(currentUser.avatar)" size="large" />
            <div class="user-name">{{ currentUser && (currentUser.name || currentUser.username) }}</div>
          </div>
          <div v-else class="user-box">
            <el-avatar size="large">我</el-avatar>
          </div>
        </div>

        <div class="right-col">
          <el-input v-if="!isLoggedIn" v-model="form.name" placeholder="昵称" class="input-name" />
          <div class="rate-row"><el-rate v-model="form.rating" /></div>
          <el-input type="textarea" :rows="4" v-model="form.content" placeholder="写一些你的看法..." />
          <div class="submit-row"><el-button type="primary" @click="submit">提交评价</el-button></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductReviews',
  props: {
    productId: { type: Number, required: true }
  },
  data() {
    return {
      reviews: [],
      form: { name: '', rating: 5, content: '' },
      defaultSamples: [
        { name: '小李', rating: 5, content: '非常新鲜，物流很快，下次还会再来！' },
        { name: '王女士', rating: 4, content: '质量不错，价格合理。' },
        { name: '阿强', rating: 5, content: '味道很好，推荐购买。' },
        { name: '小王', rating: 4, content: '总体满意，包装可以改进。' }
      ],
      currentUser: null,
      isLoggedIn: false,
      sampleCount: 2,
      persistedCount: 0
    }
  },
  created() {
    try {
      this.currentUser = JSON.parse(localStorage.getItem('xm-user') || 'null')
    } catch (e) {
      this.currentUser = null
    }
    this.isLoggedIn = !!(this.currentUser && (this.currentUser.id || this.currentUser.username))
    if (this.isLoggedIn) {
      this.form.name = this.currentUser.name || this.currentUser.username || ''
    }
    this.loadReviews()
  },
  watch: {
    productId() { this.loadReviews() }
  },
  methods: {
    getMerchantAvatar(url) {
      if (!url) return ''
      if (url.startsWith('data:')) return url
      if (url.startsWith('http')) return url
      if (url.startsWith('upload') || url.startsWith('upload/')) {
        const baseUrl = process.env.VUE_APP_BASEURL?.replace('/api', '') || 'http://localhost:9090'
        return `${baseUrl}/upload/${url.replace('upload/', '')}`
      }
      if (url.startsWith('imgs') || url.startsWith('/imgs')) {
        return url.startsWith('/') ? url : '/' + url
      }
      return `http://localhost:9090/${url}`
    },

    loadReviews() {
      const all = JSON.parse(localStorage.getItem('xm-reviews') || '[]')
      const persisted = all.filter(r => r.productId === this.productId)
      this.persistedCount = persisted.length

      // randomly pick some sample reviews to show alongside persisted ones
      const samples = this.defaultSamples.slice()
      for (let i = samples.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1))
        const tmp = samples[i]; samples[i] = samples[j]; samples[j] = tmp
      }
      const selected = samples.slice(0, this.sampleCount).map((s) => ({
        productId: this.productId,
        name: s.name,
        rating: s.rating,
        content: s.content,
        time: new Date(Date.now() - Math.floor(Math.random() * 7 + 1) * 86400000).toLocaleString(),
        isSample: true
      }))

      // avoid duplicating content already in persisted reviews
      const filteredSelected = selected.filter(s => !persisted.some(p => p.name === s.name && p.content === s.content))

      this.reviews = persisted.concat(filteredSelected)
    },
    submit() {
      if (this.isLoggedIn) {
        this.form.name = this.currentUser.name || this.currentUser.username || this.form.name
      } else {
        if (!this.form.name.trim()) { this.$message.error('请输入昵称'); return }
      }
      if (!this.form.content.trim()) { this.$message.error('请输入评价内容'); return }

      const newReview = {
        productId: this.productId,
        name: this.form.name.trim(),
        rating: this.form.rating || 5,
        content: this.form.content.trim(),
        time: new Date().toLocaleString()
      }
      if (this.isLoggedIn) {
        newReview.userId = this.currentUser.id
        newReview.avatar = this.currentUser.avatar || null
      }

      const all = JSON.parse(localStorage.getItem('xm-reviews') || '[]')

      if (this.isLoggedIn) {
        // replace existing review by this user for this product, if any
        const idx = all.findIndex(r => r.productId === this.productId && r.userId === this.currentUser.id)
        if (idx > -1) {
          all[idx] = Object.assign({}, all[idx], newReview)
        } else {
          all.unshift(newReview)
        }
      } else {
        all.unshift(newReview)
      }

      localStorage.setItem('xm-reviews', JSON.stringify(all))
      this.loadReviews()
      this.$message.success('感谢你的评价')

      // reset form
      this.form.content = ''
      if (!this.isLoggedIn) this.form.name = ''
    }
  }
}
</script>

<style scoped>
.product-reviews {
  margin-top: 24px;
  padding: 18px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 6px 18px rgba(13, 38, 59, 0.04);
}
.header-row { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px }
.header-row h3 { margin:0; font-size:18px }
.header-row .meta { color:#888; font-size:13px }
.reviews-list { display:flex; flex-direction:column; gap:12px }
.review-item { padding:14px; background:#fafafa; border-radius:8px; border:1px solid #f0f0f0 }
.review-item.sample { opacity:0.95 }
.review-top { display:flex; justify-content:space-between; align-items:flex-start; gap:12px }
.author-info { display:flex; align-items:center; gap:10px }
.author-block { display:flex; flex-direction:column }
.author-row { display:flex; align-items:center; gap:8px }
.author { font-weight:600; color:#333 }
.sample-badge { font-size:12px; color:#999; background:#fff; border:1px solid #eee; padding:2px 6px; border-radius:12px }
.rating-inline { margin-top:6px }
.time { color:#999; font-size:12px; white-space:nowrap }
.review-content { color:#444; margin-top:8px; line-height:1.6 }
.add-review { margin-top:18px }
.form-layout { display:flex; gap:16px; align-items:flex-start }
.left-col { width:88px; display:flex; flex-direction:column; align-items:center }
.user-box { text-align:center }
.user-name { margin-top:8px; font-weight:600 }
.right-col { flex:1 }
.input-name { width:260px; margin-bottom:8px }
.rate-row { margin:8px 0 }
.submit-row { margin-top:10px }
.no-reviews { color:#999; padding:12px 0 }

@media (max-width: 768px) {
  .form-layout { flex-direction:column }
  .left-col { width:100%; display:flex; justify-content:flex-start; gap:12px }
  .user-name { margin-top:0 }
}
</style>
