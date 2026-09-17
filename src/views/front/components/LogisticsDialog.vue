/**
 * 物流跟踪弹窗组件
 * 文件路径: src/views/front/components/LogisticsDialog.vue
 * 功能描述: 展示订单物流配送跟踪信息，显示快递公司、运单号（支持复制）、收货人及地址，
 *           物流轨迹时间线（发货/运输/派送/签收各节点时间），支持刷新物流信息
 * 关联文件:
 * - src/api/index.js: 提供物流跟踪数据接口
 * - src/views/front/OrderDetail.vue: 订单详情页面
 * - src/views/front/MyOrders.vue: 我的订单页面
 */
<template>
  <el-dialog
    :visible.sync="dialogVisible"
    title="物流跟踪"
    width="600px"
    :close-on-click-modal="true"
    append-to-body
    @close="handleClose"
  >
    <!-- 物流信息头部 -->
    <div class="logistics-header">
      <div class="package-info">
        <div class="info-row">
          <span class="label">快递公司：</span>
          <span class="value">{{ logisticsInfo.company }}</span>
        </div>
        <div class="info-row">
          <span class="label">运单号：</span>
          <span class="value tracking-no">{{ logisticsInfo.trackingNo }}</span>
          <el-button type="text" size="mini" class="copy-btn" @click="copyTrackingNo">复制</el-button>
        </div>
        <div class="info-row">
          <span class="label">收货人：</span>
          <span class="value">{{ logisticsInfo.receiver }}</span>
        </div>
        <div class="info-row">
          <span class="label">收货地址：</span>
          <span class="value">{{ logisticsInfo.receiverAddress }}</span>
        </div>
      </div>
    </div>

    <!-- 物流路线 -->
    <div class="logistics-route">
      <div class="route-point sender">
        <div class="point-marker">
          <i class="el-icon-location"></i>
        </div>
        <div class="point-info">
          <div class="point-label">发货地</div>
          <div class="point-value">{{ logisticsInfo.senderAddress }}</div>
        </div>
      </div>
      
      <div class="route-line">
        <div class="line-segment" v-for="n in 3" :key="n"></div>
        <div class="truck-icon">
          <i class="el-icon-truck"></i>
        </div>
      </div>
      
      <div class="route-point receiver">
        <div class="point-marker">
          <i class="el-icon-location"></i>
        </div>
        <div class="point-info">
          <div class="point-label">收货地</div>
          <div class="point-value">{{ logisticsInfo.receiverAddress }}</div>
        </div>
      </div>
    </div>

    <!-- 物流进度 -->
    <div class="logistics-progress">
      <div class="progress-title">
        <i class="el-icon-time"></i>
        <span>物流动态</span>
      </div>
      <div class="timeline">
        <div 
          v-for="(item, index) in logisticsInfo.tracking"
          :key="index"
          :class="['timeline-item', { first: index === 0, completed: item.completed }]"
        >
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <div class="timeline-time">{{ item.time }}</div>
            <div class="timeline-status">{{ item.status }}</div>
            <div class="timeline-location">{{ item.location }}</div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'LogisticsDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    order: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      dialogVisible: false,
      loading: false,
      logisticsInfo: {
        company: '',
        trackingNo: '',
        sender: '',
        senderAddress: '',
        receiver: '',
        receiverAddress: '',
        tracking: []
      }
    }
  },
  watch: {
    visible: {
      immediate: true,
      handler(val) {
        this.dialogVisible = val
        if (val) {
          this.loadLogistics()
        }
      }
    },
    dialogVisible: {
      handler(val) {
        this.$emit('update:visible', val)
      }
    }
  },
  methods: {
    handleClose() {
      this.dialogVisible = false
    },

    async loadLogistics() {
      this.loading = true
      // 优先使用订单号（orderId），其次使用数字ID（id）
      const orderId = this.order.orderId || this.order.order_id || this.order.id
      if (!orderId) {
        this.$message.error('订单号不存在')
        this.loading = false
        return
      }
      try {
        const res = await request({
          url: `/delivery/tracking/${orderId}`,
          method: 'get'
        })
        if (res.code === 200 && res.data && res.data.success) {
          const data = res.data
          this.logisticsInfo = {
            company: data.company || '',
            trackingNo: data.trackingNo || '',
            sender: '商家',
            senderAddress: data.currentLocation || '',
            receiver: data.receiver || '',
            receiverAddress: data.receiverAddress || '',
            tracking: (data.tracking || []).map(item => ({
              time: item.time || '',
              status: item.status || '',
              location: item.location || '',
              completed: item.completed === 'true' || item.completed === true
            }))
          }
        } else {
          this.$message.warning(res.data?.message || '暂无物流信息')
        }
      } catch (err) {
        console.error('获取物流信息失败:', err)
        this.$message.error('获取物流信息失败')
      } finally {
        this.loading = false
      }
    },

    copyTrackingNo() {
      const no = this.logisticsInfo.trackingNo
      if (!no) return
      navigator.clipboard.writeText(no).then(() => {
        this.$message.success('快递单号已复制')
      }).catch(() => {
        const textarea = document.createElement('textarea')
        textarea.value = no
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        this.$message.success('快递单号已复制')
      })
    }
  }
}
</script>

<style scoped>
.logistics-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.package-info .info-row {
  display: flex;
  padding: 6px 0;
  font-size: 14px;
}

.package-info .label {
  width: 80px;
  opacity: 0.8;
}

.package-info .value {
  flex: 1;
}

.package-info .tracking-no {
  font-family: monospace;
  letter-spacing: 1px;
}

.package-info .copy-btn {
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  margin-left: 4px;
  padding: 2px 6px;
}

.package-info .copy-btn:hover {
  color: #fff;
}

.logistics-route {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.route-point {
  display: flex;
  align-items: center;
  gap: 12px;
}

.point-marker {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.sender .point-marker {
  background: #e6f7ff;
  color: #1890ff;
}

.receiver .point-marker {
  background: #f6ffed;
  color: #52c41a;
}

.point-info {
  text-align: left;
}

.point-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.point-value {
  font-size: 13px;
  color: #333;
  max-width: 120px;
}

.route-line {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
  position: relative;
}

.line-segment {
  height: 2px;
  flex: 1;
  background: linear-gradient(90deg, #1890ff, #52c41a);
}

.truck-icon {
  position: absolute;
  background: white;
  padding: 8px;
  border-radius: 50%;
  color: #ff9800;
  font-size: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.logistics-progress {
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.progress-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.progress-title i {
  color: #ff9800;
}

.timeline {
  position: relative;
  padding-left: 20px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e8e8e8;
}

.timeline-item {
  position: relative;
  padding-bottom: 20px;
  padding-left: 20px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -14px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #d9d9d9;
  border: 2px solid #fff;
  z-index: 1;
}

.timeline-item.first .timeline-dot {
  background: #52c41a;
  box-shadow: 0 0 0 4px rgba(82, 196, 26, 0.2);
}

.timeline-item.completed .timeline-dot {
  background: #1890ff;
}

.timeline-content {
  background: #fafafa;
  padding: 12px;
  border-radius: 8px;
}

.timeline-item.first .timeline-content {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
}

.timeline-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.timeline-status {
  font-size: 14px;
  color: #333;
  margin-bottom: 2px;
}

.timeline-location {
  font-size: 12px;
  color: #666;
}
</style>
