/**
 * 订单列表组件
 * 文件路径: src/components/OrderList.vue
 * 功能描述: 显示订单列表，支持查看和取消订单操作
 * 关联文件:
 * - src/views/front/MyOrders.vue: 我的订单页面
 * - src/views/front/Home.vue: 首页组件
 */
<template>
  <div class="order-list">
    <h2 v-if="!compact">我的订单</h2>
    <div v-for="order in orders" :key="order.id || order.orderId" class="order-item">
      <div class="order-header">
        <span class="order-id">订单号: {{ order.orderId }}</span>
        <span class="order-status" :class="getStatusClass(order.status)">{{ getStatusText(order.status) }}</span>
      </div>
      <el-table :data="order.products || []" style="width: 100%" :show-header="!compact">
        <el-table-column label="商品名称" min-width="180">
          <template slot-scope="scope">
            <div class="product-cell">
              <img v-if="scope.row.productImage || scope.row.img" :src="scope.row.productImage || scope.row.img" class="product-thumb" @error="$event.target.style.display='none'" />
              <span>{{ scope.row.productName || scope.row.name || '未知商品' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100">
          <template slot-scope="scope">
            ¥{{ formatPrice(scope.row.price) }}
          </template>
        </el-table-column>
        <el-table-column label="数量" width="80">
          <template slot-scope="scope">
            {{ scope.row.quantity || scope.row.count || 1 }}
          </template>
        </el-table-column>
        <el-table-column label="总价" width="100">
          <template slot-scope="scope">
            ¥{{ formatPrice((scope.row.price || 0) * (scope.row.quantity || scope.row.count || 1)) }}
          </template>
        </el-table-column>
      </el-table>
      <div class="order-footer" v-if="!compact">
        <div class="order-total">
          订单总额: <span class="total-price">¥{{ formatPrice(order.totalAmount || order.total) }}</span>
        </div>
        <div class="order-actions">
          <el-button size="mini" type="primary" @click="$emit('view-order', order)">查看详情</el-button>
          <el-button v-if="canCancel(order)" size="mini" type="danger" @click="$emit('cancel-order', order)">取消订单</el-button>
        </div>
      </div>
      <div class="order-actions" v-else>
        <el-button size="mini" type="text" @click="$emit('view-order', order)">查看</el-button>
        <el-button v-if="canCancel(order)" size="mini" type="text" class="cancel-btn" @click="$emit('cancel-order', order)">取消</el-button>
      </div>
    </div>
    <div v-if="orders.length === 0" class="empty-state">
      <p>暂无订单</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderList',
  props: {
    orders: {
      type: Array,
      required: true
    },
    compact: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    formatPrice(price) {
      if (price === null || price === undefined) return '0.00'
      return Number(price).toFixed(2)
    },
    getStatusText(status) {
      const map = {
        'PENDING': '待支付',
        'PAID': '已支付',
        'PROCESSING': '处理中',
        'SHIPPED': '已发货',
        'DELIVERED': '已送达',
        'CANCELLED': '已取消',
        'COMPLETED': '已完成',
        'REFUNDED': '已退款'
      }
      return map[status] || status || '未知'
    },
    getStatusClass(status) {
      const map = {
        'PENDING': 'status-pending',
        'PAID': 'status-paid',
        'PROCESSING': 'status-processing',
        'SHIPPED': 'status-shipped',
        'DELIVERED': 'status-delivered',
        'CANCELLED': 'status-cancelled',
        'COMPLETED': 'status-completed'
      }
      return map[status] || ''
    },
    canCancel(order) {
      return ['PENDING', 'PAID'].includes(order.status)
    }
  }
}
</script>

<style scoped>
.order-list {
  margin: 20px 0;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px #f0f1f2;
}

.order-item {
  margin-bottom: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

.order-item:last-child {
  margin-bottom: 0;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #eee;
}

.order-id {
  font-size: 13px;
  color: #666;
}

.order-status {
  font-size: 13px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-pending { color: #e6a23c; background: #fdf6ec; }
.status-paid { color: #409eff; background: #ecf5ff; }
.status-processing { color: #409eff; background: #ecf5ff; }
.status-shipped { color: #67c23a; background: #f0f9eb; }
.status-delivered { color: #67c23a; background: #f0f9eb; }
.status-cancelled { color: #f56c6c; background: #fef0f0; }
.status-completed { color: #67c23a; background: #f0f9eb; }

.product-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-thumb {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  object-fit: cover;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-top: 1px solid #eee;
}

.order-total {
  font-size: 14px;
  color: #666;
}

.total-price {
  font-size: 18px;
  font-weight: 600;
  color: #ff9800;
}

.order-actions {
  display: flex;
  gap: 8px;
}

.cancel-btn {
  color: #f56c6c;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
