/**
 * 我的优惠券页面
 * 文件路径: src/views/front/MyCoupons.vue
 * 功能描述: 管理用户已持有的优惠券，展示未使用/已使用/已过期三种状态统计，Tab切换对应状态券列表，
 *           支持按状态筛选（未使用/已使用/已过期），去领券快捷入口
 * 关联文件:
 * - src/api/index.js: 提供我的优惠券数据接口
 * - src/views/front/Coupons.vue: 优惠券领取中心
 */
<template>
  <div class="my-coupons-page">
    <div class="page-header">
      <h1 class="page-title">
        <i class="el-icon-ticket"></i>
        我的优惠券
      </h1>
      <div class="header-actions">
        <el-button type="primary" plain @click="$router.push('/front/coupons')">
          <i class="el-icon-present"></i>
          去领券
        </el-button>
      </div>
    </div>

    <div class="stats-bar">
      <div class="stat-item">
        <div class="stat-value">{{ stats.unused || 0 }}</div>
        <div class="stat-label">未使用</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.used || 0 }}</div>
        <div class="stat-label">已使用</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.expired || 0 }}</div>
        <div class="stat-label">已过期</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.total || 0 }}</div>
        <div class="stat-label">总计</div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="coupon-tabs" @tab-click="handleTabChange">
      <el-tab-pane label="未使用" name="1">
        <div v-loading="loading" class="coupon-list">
          <div v-if="coupons.length === 0 && !loading" class="empty-state">
            <i class="el-icon-ticket"></i>
            <p>暂无未使用的优惠券</p>
            <el-button type="primary" @click="$router.push('/front/coupons')">去领券</el-button>
          </div>
          <div v-for="coupon in coupons" :key="coupon.id" class="coupon-card">
            <div class="coupon-left">
              <div class="coupon-amount">
                <span class="currency">¥</span>
                <span class="value">{{ coupon.coupon_amount }}</span>
              </div>
              <div class="coupon-condition" v-if="coupon.coupon_min_amount > 0">
                满{{ coupon.coupon_min_amount }}元可用
              </div>
              <div class="coupon-condition" v-else>
                无门槛
              </div>
            </div>
            <div class="coupon-right">
              <div class="coupon-info">
                <h3 class="coupon-name">{{ coupon.coupon_name }}</h3>
                <p class="coupon-desc">{{ coupon.coupon_description }}</p>
                <div class="coupon-meta">
                  <span class="meta-item">
                    <i class="el-icon-time"></i>
                    有效期至：{{ formatDateTime(coupon.valid_end) }}
                  </span>
                  <span class="meta-item">
                    <i class="el-icon-download"></i>
                    领取时间：{{ formatDateTime(coupon.receive_time) }}
                  </span>
                </div>
              </div>
              <div class="coupon-action">
                <el-tag type="success" size="small">未使用</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="已使用" name="2">
        <div v-loading="loading" class="coupon-list">
          <div v-if="coupons.length === 0 && !loading" class="empty-state">
            <i class="el-icon-ticket"></i>
            <p>暂无已使用的优惠券</p>
          </div>
          <div v-for="coupon in coupons" :key="coupon.id" class="coupon-card used">
            <div class="coupon-left">
              <div class="coupon-amount">
                <span class="currency">¥</span>
                <span class="value">{{ coupon.coupon_amount }}</span>
              </div>
              <div class="coupon-condition" v-if="coupon.coupon_min_amount > 0">
                满{{ coupon.coupon_min_amount }}元可用
              </div>
              <div class="coupon-condition" v-else>
                无门槛
              </div>
            </div>
            <div class="coupon-right">
              <div class="coupon-info">
                <h3 class="coupon-name">{{ coupon.coupon_name }}</h3>
                <p class="coupon-desc">{{ coupon.coupon_description }}</p>
                <div class="coupon-meta">
                  <span class="meta-item">
                    <i class="el-icon-time"></i>
                    使用时间：{{ formatDateTime(coupon.use_time) }}
                  </span>
                  <span class="meta-item" v-if="coupon.order_no">
                    <i class="el-icon-s-order"></i>
                    订单号：{{ coupon.order_no }}
                  </span>
                </div>
              </div>
              <div class="coupon-action">
                <el-tag type="info" size="small">已使用</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="已过期" name="3">
        <div v-loading="loading" class="coupon-list">
          <div v-if="coupons.length === 0 && !loading" class="empty-state">
            <i class="el-icon-ticket"></i>
            <p>暂无已过期的优惠券</p>
          </div>
          <div v-for="coupon in coupons" :key="coupon.id" class="coupon-card expired">
            <div class="coupon-left">
              <div class="coupon-amount">
                <span class="currency">¥</span>
                <span class="value">{{ coupon.coupon_amount }}</span>
              </div>
              <div class="coupon-condition" v-if="coupon.coupon_min_amount > 0">
                满{{ coupon.coupon_min_amount }}元可用
              </div>
              <div class="coupon-condition" v-else>
                无门槛
              </div>
            </div>
            <div class="coupon-right">
              <div class="coupon-info">
                <h3 class="coupon-name">{{ coupon.coupon_name }}</h3>
                <p class="coupon-desc">{{ coupon.coupon_description }}</p>
                <div class="coupon-meta">
                  <span class="meta-item">
                    <i class="el-icon-time"></i>
                    过期时间：{{ formatDateTime(coupon.valid_end) }}
                  </span>
                </div>
              </div>
              <div class="coupon-action">
                <el-tag type="danger" size="small">已过期</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'MyCoupons',
  data() {
    return {
      activeTab: '1',
      loading: false,
      coupons: [],
      stats: {
        total: 0,
        unused: 0,
        used: 0,
        expired: 0
      },
      user: JSON.parse(localStorage.getItem('xm-user') || '{}')
    }
  },
  created() {
    this.loadCoupons()
    this.loadStats()
  },
  methods: {
    async loadCoupons() {
      if (!this.user || !this.user.id) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }

      this.loading = true
      try {
        const res = await request({
          url: '/user-coupon/my',
          method: 'get'
        })

        if (res.code === 200 && res.data) {
          this.coupons = res.data
        }
      } catch (err) {
        console.error('获取优惠券列表失败:', err)
        this.$message.error('获取优惠券列表失败')
      } finally {
        this.loading = false
      }
    },

    async loadStats() {
      if (!this.user || !this.user.id) return

      try {
        const res = await request({
          url: '/user-coupon/stats',
          method: 'get'
        })

        if (res.code === 200 && res.data) {
          this.stats = res.data
        }
      } catch (err) {
        console.error('获取优惠券统计失败:', err)
      }
    },

    async handleTabChange(tab) {
      if (!this.user || !this.user.id) return

      this.loading = true
      try {
        const res = await request({
          url: `/user-coupon/my/status?status=${tab.name}`,
          method: 'get'
        })

        if (res.code === 200 && res.data) {
          this.coupons = res.data
        }
      } catch (err) {
        console.error('获取优惠券列表失败:', err)
      } finally {
        this.loading = false
      }
    },

    formatDateTime(time) {
      if (!time) return '-'
      const date = new Date(time)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    }
  }
}
</script>

<style scoped>
.my-coupons-page {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title i {
  color: #e6a23c;
}

.stats-bar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  color: #fff;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  opacity: 0.9;
  margin-top: 5px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
}

.coupon-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.coupon-list {
  min-height: 200px;
}

.coupon-card {
  display: flex;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  margin-bottom: 15px;
  overflow: hidden;
  transition: all 0.3s;
}

.coupon-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.coupon-card.used {
  opacity: 0.7;
}

.coupon-card.expired {
  opacity: 0.5;
  filter: grayscale(0.5);
}

.coupon-left {
  width: 180px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.coupon-card.used .coupon-left,
.coupon-card.expired .coupon-left {
  background: linear-gradient(135deg, #999 0%, #666 100%);
}

.coupon-amount {
  display: flex;
  align-items: baseline;
}

.coupon-amount .currency {
  font-size: 20px;
  font-weight: 600;
}

.coupon-amount .value {
  font-size: 42px;
  font-weight: 700;
  line-height: 1;
}

.coupon-condition {
  margin-top: 10px;
  font-size: 13px;
  opacity: 0.9;
}

.coupon-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
}

.coupon-info {
  flex: 1;
}

.coupon-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.coupon-desc {
  font-size: 13px;
  color: #666;
  margin: 0 0 12px 0;
}

.coupon-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.meta-item {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

.coupon-action {
  padding-left: 20px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state i {
  font-size: 64px;
  color: #ddd;
  margin-bottom: 15px;
}

.empty-state p {
  margin-bottom: 20px;
}
</style>
