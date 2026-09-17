/**
 * 优惠券中心页面
 * 文件路径: src/views/front/Coupons.vue
 * 功能描述: 优惠券领取中心，Tab切换展示可领取/已领取/已过期优惠券列表，每张券显示面额、使用条件、有效期，
 *           支持一键领取、查看我的优惠券跳转，空状态提示、加载状态展示
 * 关联文件:
 * - src/api/index.js: 提供优惠券数据接口
 * - src/views/front/MyCoupons.vue: 我的优惠券页面
 */
<template>
  <div class="coupons-page">
    <div class="page-header">
      <h1 class="page-title">
        <i class="el-icon-present"></i>
        优惠券中心
      </h1>
      <el-button type="primary" plain @click="$router.push('/front/my-coupons')">
        <i class="el-icon-ticket"></i>
        我的优惠券
      </el-button>
    </div>

    <el-tabs v-model="activeTab" class="coupon-tabs">
      <el-tab-pane label="可领取优惠券" name="available">
        <div v-loading="loading" class="coupon-list">
          <div v-if="availableCoupons.length === 0 && !loading" class="empty-state">
            <i class="el-icon-present"></i>
            <p>暂无可领取的优惠券</p>
          </div>
          <div v-for="coupon in availableCoupons" :key="coupon.id" class="coupon-card">
            <div class="coupon-left">
              <div class="coupon-amount">
                <span class="currency">¥</span>
                <span class="value">{{ coupon.amount }}</span>
              </div>
              <div class="coupon-condition" v-if="coupon.minAmount > 0">
                满{{ coupon.minAmount }}元可用
              </div>
              <div class="coupon-condition" v-else>
                无门槛
              </div>
            </div>
            <div class="coupon-right">
              <div class="coupon-info">
                <h3 class="coupon-name">{{ coupon.name }}</h3>
                <p class="coupon-desc">{{ coupon.description }}</p>
                <div class="coupon-meta">
                  <span class="meta-item">
                    <i class="el-icon-time"></i>
                    {{ formatTime(coupon.startTime) }} - {{ formatTime(coupon.endTime) }}
                  </span>
                  <span class="meta-item" v-if="coupon.remaining === -1">
                    <i class="el-icon-s-goods"></i>
                    不限量
                  </span>
                  <span class="meta-item" v-else>
                    <i class="el-icon-s-goods"></i>
                    剩余{{ coupon.remaining }}张
                  </span>
                </div>
              </div>
              <div class="coupon-action">
                <el-button
                  size="small"
                  :class="coupon.alreadyReceived ? 'btn-received' : 'btn-receive'"
                  :disabled="coupon.alreadyReceived"
                  @click="receiveCoupon(coupon)"
                >
                  {{ coupon.alreadyReceived ? '已领取' : '立即领取' }}
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="充值赠券" name="recharge">
        <div v-loading="loading" class="recharge-section">
          <div class="section-header">
            <div class="section-title">
              <i class="el-icon-wallet"></i>
              充值送优惠券
            </div>
            <p class="section-desc">充值对应金额即可领取优惠券，多充多送，上不封顶！</p>
          </div>
          <div v-if="rechargeCoupons.length === 0 && !loading" class="empty-state">
            <i class="el-icon-wallet"></i>
            <p>暂无充值赠券活动</p>
          </div>
          <div class="recharge-coupons">
            <div v-for="item in rechargeCoupons" :key="item.id" class="recharge-card" :class="{ 'card-received': item.alreadyReceived }">
              <div class="card-badge" v-if="item.alreadyReceived">已领取</div>
              <div class="recharge-icon">
                <i class="el-icon-wallet"></i>
              </div>
              <div class="recharge-amount">
                <span class="label">充值满</span>
                <span class="value">¥{{ item.minAmount }}</span>
              </div>
              <div class="recharge-arrow">
                <i class="el-icon-right"></i>
              </div>
              <div class="recharge-gift">
                <span class="label">赠送</span>
                <span class="value">¥{{ item.amount }}优惠券</span>
              </div>
              <div class="recharge-desc">{{ item.description }}</div>
              <div class="recharge-meta">
                <span class="meta-item" v-if="item.remaining !== -1">
                  <i class="el-icon-s-goods"></i>
                  剩余{{ item.remaining }}份
                </span>
                <span class="meta-item" v-else>
                  <i class="el-icon-s-goods"></i>
                  不限量
                </span>
              </div>
              <el-button
                size="small"
                :class="item.alreadyReceived ? 'btn-received' : 'btn-recharge'"
                :disabled="item.alreadyReceived"
                @click="receiveCoupon(item)"
              >
                {{ item.alreadyReceived ? '已领取' : '立即充值领取' }}
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="消费赠券" name="consumption">
        <div v-loading="loading" class="consumption-section">
          <div class="section-header">
            <div class="section-title">
              <i class="el-icon-shopping-cart-2"></i>
              月度消费赠券
            </div>
            <p class="section-desc">当月消费满指定金额即可获得优惠券奖励，买得越多送得越多！</p>
          </div>
          <div v-if="consumptionCoupons.length === 0 && !loading" class="empty-state">
            <i class="el-icon-shopping-cart-2"></i>
            <p>暂无消费赠券活动</p>
          </div>
          <div class="consumption-coupons">
            <div v-for="item in consumptionCoupons" :key="item.id" class="consumption-card" :class="{ 'card-received': item.alreadyReceived }">
              <div class="card-badge" v-if="item.alreadyReceived">已领取</div>
              <div class="consumption-icon">
                <i class="el-icon-shopping-cart-2"></i>
              </div>
              <div class="consumption-info">
                <div class="consumption-amount">
                  <span class="label">月消费满</span>
                  <span class="value">¥{{ item.minAmount }}</span>
                </div>
                <div class="consumption-arrow">
                  <i class="el-icon-right"></i>
                </div>
                <div class="consumption-gift">
                  <span class="label">赠送</span>
                  <span class="value">¥{{ item.amount }}优惠券</span>
                </div>
              </div>
              <div class="consumption-desc">{{ item.description }}</div>
              <div class="consumption-meta">
                <span class="meta-item" v-if="item.remaining !== -1">
                  <i class="el-icon-s-goods"></i>
                  剩余{{ item.remaining }}份
                </span>
                <span class="meta-item" v-else>
                  <i class="el-icon-s-goods"></i>
                  不限量
                </span>
              </div>
              <el-button
                size="small"
                :class="item.alreadyReceived ? 'btn-received' : 'btn-consumption'"
                :disabled="item.alreadyReceived"
                @click="receiveCoupon(item)"
              >
                {{ item.alreadyReceived ? '已领取' : '立即领取' }}
              </el-button>
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
  name: 'Coupons',
  data() {
    return {
      activeTab: 'available',
      loading: false,
      availableCoupons: [],
      rechargeCoupons: [],
      consumptionCoupons: [],
      user: JSON.parse(localStorage.getItem('xm-user') || '{}')
    }
  },
  created() {
    this.loadAvailableCoupons()
    this.loadRechargeCoupons()
    this.loadConsumptionCoupons()
  },
  methods: {
    async loadAvailableCoupons() {
      this.loading = true
      try {
        const res = await request({
          url: '/coupon/list',
          method: 'get'
        })
        if (res.code === 200 && res.data) {
          this.availableCoupons = res.data
            .filter(c => c.source === 1 || c.source === 2)
            .map(coupon => ({
              ...coupon,
              remaining: coupon.totalNum === -1 ? -1 : (coupon.totalNum - coupon.receivedNum)
            }))
        }
      } catch (err) {
        console.error('获取优惠券列表失败:', err)
        this.$message.error('获取优惠券列表失败')
      } finally {
        this.loading = false
      }
    },

    async loadRechargeCoupons() {
      this.loading = true
      try {
        const res = await request({
          url: '/coupon/list',
          method: 'get'
        })
        if (res.code === 200 && res.data) {
          this.rechargeCoupons = res.data
            .filter(c => c.source === 3)
            .map(coupon => ({
              ...coupon,
              remaining: coupon.totalNum === -1 ? -1 : (coupon.totalNum - coupon.receivedNum)
            }))
        }
      } catch (err) {
        console.error('获取充值赠券失败:', err)
      } finally {
        this.loading = false
      }
    },

    async loadConsumptionCoupons() {
      this.loading = true
      try {
        const res = await request({
          url: '/coupon/list',
          method: 'get'
        })
        if (res.code === 200 && res.data) {
          this.consumptionCoupons = res.data
            .filter(c => c.source === 4)
            .map(coupon => ({
              ...coupon,
              remaining: coupon.totalNum === -1 ? -1 : (coupon.totalNum - coupon.receivedNum)
            }))
        }
      } catch (err) {
        console.error('获取消费赠券失败:', err)
      } finally {
        this.loading = false
      }
    },

    async receiveCoupon(coupon) {
      if (!this.user || !this.user.id) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }

      try {
        const res = await request({
          url: `/coupon/receive/${coupon.id}`,
          method: 'post'
        })

        if (res.code === 200 && res.data && res.data.success) {
          this.$message.success('领取成功！')
          coupon.alreadyReceived = true
        } else {
          this.$message.warning(res.data?.message || '领取失败')
        }
      } catch (err) {
        console.error('领取优惠券失败:', err)
        this.$message.error('领取优惠券失败')
      }
    },

    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${month}-${day}`
    }
  }
}
</script>

<style scoped>
.coupons-page {
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

/* 按钮样式 */
.btn-receive {
  background: #f56c6c;
  color: #fff;
  border-color: #f56c6c;
}

.btn-receive:hover {
  background: #f78989;
  border-color: #f78989;
}

.btn-received {
  background: #909399 !important;
  color: #fff !important;
  border-color: #909399 !important;
  cursor: not-allowed;
  opacity: 0.7;
}

.btn-recharge {
  background: #e6a23c;
  color: #fff;
  border-color: #e6a23c;
}

.btn-recharge:hover {
  background: #ebb563;
  border-color: #ebb563;
}

.btn-consumption {
  background: #67c23a;
  color: #fff;
  border-color: #67c23a;
}

.btn-consumption:hover {
  background: #85ce61;
  border-color: #85ce61;
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

.recharge-section,
.consumption-section {
  padding: 20px 0;
  min-height: 200px;
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title i {
  color: #e6a23c;
}

.section-desc {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.recharge-coupons,
.consumption-coupons {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 15px;
}

.recharge-card,
.consumption-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.recharge-card:hover,
.consumption-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 已领取卡片样式 */
.card-received {
  background: #f5f5f5;
  border-color: #e0e0e0;
}

.card-received:hover {
  box-shadow: none;
  transform: none;
}

.card-badge {
  position: absolute;
  top: 12px;
  right: -28px;
  background: #909399;
  color: #fff;
  font-size: 12px;
  padding: 2px 30px;
  transform: rotate(45deg);
  z-index: 1;
}

.recharge-icon,
.consumption-icon {
  font-size: 32px;
  color: #e6a23c;
  margin-bottom: 12px;
}

.card-received .recharge-icon,
.card-received .consumption-icon {
  color: #c0c4cc;
}

.recharge-arrow,
.consumption-arrow {
  font-size: 20px;
  color: #e6a23c;
  margin: 8px 0;
}

.card-received .recharge-arrow,
.card-received .consumption-arrow {
  color: #c0c4cc;
}

.recharge-amount,
.consumption-amount {
  margin-bottom: 10px;
}

.recharge-gift,
.consumption-gift {
  margin-bottom: 15px;
}

.recharge-desc,
.consumption-desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
  line-height: 1.5;
}

.recharge-meta,
.consumption-meta {
  font-size: 12px;
  color: #bbb;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.label {
  font-size: 13px;
  color: #666;
}

.value {
  font-size: 20px;
  font-weight: 600;
  color: #e6a23c;
  margin-left: 5px;
}

.card-received .value {
  color: #c0c4cc;
}

.consumption-info {
  margin-bottom: 15px;
}

</style>
