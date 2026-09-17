/**
 * 我的订单页面
 * 文件路径: src/views/front/MyOrders.vue
 * 功能描述: 用户订单管理中心，展示全部/待支付/已支付/已发货/已完成/已取消状态统计卡片并支持点击筛选，
 *           订单列表展示（订单号、商品信息、金额、状态、下单时间），支持按状态筛选、分页查看、
 *           订单详情查看、取消订单、确认收货、查看物流、去支付等操作，刷新按钮重新加载
 * 关联文件:
 * - src/api/index.js: 提供订单数据接口
 * - src/views/front/OrderDetail.vue: 订单详情页面
 * - src/views/front/components/LogisticsDialog.vue: 物流跟踪弹窗
 */
<template>
  <div class="orders-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2><i class="el-icon-s-order"></i> 我的订单</h2>
      <el-button size="small" @click="loadOrders" :loading="loading">
        <i class="el-icon-refresh"></i> 刷新
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card" :class="{ active: filterStatus === '' }" @click="filterStatus = ''">
        <div class="stat-icon total"><i class="el-icon-s-order"></i></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">全部订单</div>
        </div>
      </div>
      <div class="stat-card" :class="{ active: filterStatus === 'pending' }" @click="filterStatus = 'pending'">
        <div class="stat-icon pending"><i class="el-icon-time"></i></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待支付</div>
        </div>
      </div>
      <div class="stat-card" :class="{ active: filterStatus === 'paid' }" @click="filterStatus = 'paid'">
        <div class="stat-icon processing"><i class="el-icon-s-goods"></i></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.paid }}</div>
          <div class="stat-label">待发货</div>
        </div>
      </div>
      <div class="stat-card" :class="{ active: filterStatus === 'shipping' }" @click="filterStatus = 'shipping'">
        <div class="stat-icon shipping"><i class="el-icon-truck"></i></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.shipping }}</div>
          <div class="stat-label">配送中</div>
        </div>
      </div>
      <div class="stat-card" :class="{ active: filterStatus === 'completed' }" @click="filterStatus = 'completed'">
        <div class="stat-icon completed"><i class="el-icon-check"></i></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 订单列表 -->
    <div v-else-if="filteredOrders.length > 0" class="orders-list">
      <div v-for="order in filteredOrders" :key="order.id || order.orderId" class="order-card">
        <!-- 订单头部 -->
        <div class="order-header">
          <div class="order-info">
            <span class="order-no">订单号：{{ order.orderId || order.id }}</span>
            <span class="order-time">{{ formatDateTime(order.createTime || order.createdAt) }}</span>
          </div>
          <el-tag :type="getStatusType(order.status)" size="small">{{ getStatusText(order.status) }}</el-tag>
        </div>

        <!-- 商品列表 -->
        <div class="order-products" @click="goOrderDetail(order)">
          <div v-for="product in (order.products || [])" :key="product.id" class="product-item">
            <img :src="getImage(product)" class="product-image" @error="handleImageError" />
            <div class="product-info">
              <div class="product-name">{{ product.productName || product.name || '商品' }}</div>
              <div class="product-price">¥{{ formatPrice(product.price) }} × {{ product.quantity || product.count || 1 }}</div>
            </div>
          </div>
        </div>

        <!-- 物流信息条 -->
        <div v-if="(order.expressCompany || order.trackingNumber || order.expressNo) && (order.status === 'SHIPPED' || order.status === 'SHIPPING' || order.status === 'DELIVERED' || order.status === 'COMPLETED')" class="order-express-bar">
          <div class="express-bar-left">
            <i class="el-icon-truck"></i>
            <span class="express-company-name">{{ order.expressCompany || order.shippingCompany || '快递' }}</span>
            <span class="express-divider">|</span>
            <span class="express-tracking-no">{{ order.trackingNumber || order.expressNo }}</span>
          </div>
          <el-button type="text" size="mini" class="copy-tracking-btn" @click.stop="copyTrackingNo(order)">复制单号</el-button>
        </div>

        <!-- 订单底部 -->
        <div class="order-footer">
          <div class="order-amount">
            <span class="amount-label">实付：</span>
            <span class="amount-value">¥{{ formatPrice(order.totalAmount || order.total || calculateTotal(order)) }}</span>
          </div>
          <div class="order-actions">
            <!-- 待支付 -->
            <template v-if="order.status === 'PENDING' || order.status === 'pending'">
              <template v-if="!isMerchant">
                <el-button type="danger" size="small" @click.stop="handlePay(order)">立即支付</el-button>
                <el-button size="small" @click.stop="handleCancel(order)">取消</el-button>
              </template>
              <el-tag v-else type="info" size="small">等待买家付款</el-tag>
            </template>
            <!-- 已支付待发货 -->
            <template v-else-if="order.status === 'PAID' || order.status === 'paid' || order.status === 'PROCESSING' || order.status === 'processing'">
              <el-button v-if="isMerchant" type="primary" size="small" @click.stop="handleShip(order)">发货</el-button>
              <el-button v-else type="danger" size="small" plain @click.stop="handleRefund(order)">申请退款</el-button>
              <el-tag v-if="!isMerchant" type="warning" size="small">等待发货</el-tag>
            </template>
            <!-- 配送中 -->
            <template v-else-if="order.status === 'SHIPPING' || order.status === 'shipping' || order.status === 'SHIPPED' || order.status === 'shipped'">
              <template v-if="!isMerchant">
                <el-button type="success" size="small" @click.stop="handleConfirm(order)">确认收货</el-button>
              </template>
              <el-tag v-if="isMerchant" type="success" size="small">已发货，等待买家确认</el-tag>
              <el-button type="primary" size="small" plain @click.stop="viewLogistics(order)">查看物流</el-button>
            </template>
            <!-- 已完成 -->
            <template v-else-if="order.status === 'DELIVERED' || order.status === 'COMPLETED' || order.status === 'delivered' || order.status === 'completed'">
              <template v-if="!isMerchant">
                <el-button type="text" size="small" @click.stop="handleReorder(order)">再来一单</el-button>
                <el-button type="warning" text size="small" @click.stop="goAfterSales(order)">申请售后</el-button>
              </template>
              <el-tag v-else type="success" size="small">交易已完成</el-tag>
              <el-button v-if="order.expressCompany || order.trackingNumber" type="primary" text size="small" @click.stop="viewLogistics(order)">查看物流</el-button>
            </template>
            <!-- 已退款 -->
            <template v-else-if="order.status === 'REFUNDED' || order.status === 'refunded'">
              <template v-if="!isMerchant">
                <el-button type="text" size="small" @click.stop="handleReorder(order)">再来一单</el-button>
              </template>
              <el-tag v-else type="info" size="small">已退款</el-tag>
              <el-tag v-if="!isMerchant" type="info" size="small">已退款</el-tag>
            </template>
            <!-- 已取消 -->
            <template v-else-if="order.status === 'CANCELLED' || order.status === 'cancelled'">
              <template v-if="!isMerchant">
                <el-button type="text" size="small" @click.stop="handleReorder(order)">再来一单</el-button>
              </template>
              <el-tag v-else type="info" size="small">已取消</el-tag>
              <el-tag v-if="!isMerchant" type="info" size="small">已取消</el-tag>
            </template>
            <el-button type="primary" text size="small" @click.stop="goOrderDetail(order)">查看详情</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-orders">
      <div class="empty-icon"><i class="el-icon-s-order"></i></div>
      <h3>暂无订单</h3>
      <p>{{ filterStatus ? '没有符合条件的订单' : '快去选购心仪的商品吧' }}</p>
      <div class="empty-actions">
        <el-button type="primary" @click="goProducts">去购物</el-button>
        <el-button v-if="filterStatus" @click="filterStatus = ''">查看全部订单</el-button>
      </div>
    </div>

    <!-- 发货对话框 -->
    <el-dialog :visible.sync="shipDialogVisible" width="520px" center :close-on-click-modal="false" custom-class="ship-dialog">
      <template slot="title">
        <div class="ship-dialog-title">
          <i class="el-icon-s-promotion"></i>
          <span>订单发货</span>
        </div>
      </template>

      <!-- 订单摘要 -->
      <div class="ship-order-summary" v-if="currentOrder">
        <div class="summary-row">
          <span class="summary-label">订单号</span>
          <span class="summary-value">{{ currentOrder.orderId || currentOrder.id }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">商品</span>
          <span class="summary-value">{{ getOrderProductNames(currentOrder) }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">收货人</span>
          <span class="summary-value">{{ currentOrder.receiver || currentOrder.contactName || currentOrder.shippingName || '-' }}</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">收货地址</span>
          <span class="summary-value">{{ currentOrder.address || currentOrder.shippingAddress || '-' }}</span>
        </div>
      </div>

      <el-divider></el-divider>

      <el-form :model="shipForm" label-width="90px" class="ship-form">
        <el-form-item label="快递公司" required>
          <el-select v-model="shipForm.expressCompany" placeholder="请选择快递公司" style="width: 100%" @change="onExpressChange">
            <el-option
              v-for="item in expressOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <span>{{ item.label }}</span>
              <span v-if="item.code" class="express-code-hint">{{ item.code }}-</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" required>
          <el-input
            v-model="shipForm.expressNo"
            placeholder="请输入快递单号（不含前缀）"
            @input="onExpressNoInput"
          >
            <template slot="prepend" v-if="currentExpressCode">{{ currentExpressCode }}-</template>
          </el-input>
          <div v-if="previewTrackingNo" class="tracking-preview">
            <i class="el-icon-view"></i>
            完整单号预览：<strong>{{ previewTrackingNo }}</strong>
          </div>
        </el-form-item>
      </el-form>

      <div class="ship-tips">
        <i class="el-icon-info"></i>
        <span>发货后订单状态将变为"配送中"，买家可查看物流信息</span>
      </div>

      <span slot="footer">
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitShip" :loading="shipping" icon="el-icon-s-promotion">确认发货</el-button>
      </span>
    </el-dialog>

    <!-- 物流跟踪对话框 -->
    <LogisticsDialog :visible.sync="logisticsDialogVisible" :order="currentLogisticsOrder" />
  </div>
</template>

<script>
import { ref, computed, onMounted, getCurrentInstance } from '@vue/composition-api'
import { ElMessageBox, ElMessage } from 'element-ui'
import { getOrderPage, getMerchantOrders, payOrder, cancelOrder, confirmReceive, merchantShipOrder } from '@/api'
import { formatPrice, getFullImageUrl, isLoggedIn } from '@/utils/common'
import LogisticsDialog from './components/LogisticsDialog.vue'

export default {
  name: 'MyOrders',
  components: {
    LogisticsDialog
  },
  setup() {
    const { proxy } = getCurrentInstance()
    const router = proxy.$router
    const orders = ref([])
    const loading = ref(false)
    const filterStatus = ref('')
    const payDialogVisible = ref(false)
    const selectedPayment = ref('alipay')
    const currentOrder = ref(null)
    const paying = ref(false)
    const isMerchant = ref(false)
    const shipDialogVisible = ref(false)
    const shipForm = ref({ expressCompany: '', expressNo: '' })
    const shipping = ref(false)
    const logisticsDialogVisible = ref(false)
    const currentLogisticsOrder = ref(null)

    // 快递公司选项
    const expressOptions = [
      { label: '顺丰快递', value: '顺丰快递', code: 'SF' },
      { label: '圆通快递', value: '圆通快递', code: 'YT' },
      { label: '中通快递', value: '中通快递', code: 'ZT' },
      { label: '韵达快递', value: '韵达快递', code: 'YD' },
      { label: '申通快递', value: '申通快递', code: 'ST' },
      { label: '邮政快递', value: '邮政快递', code: 'EMS' },
      { label: '极兔快递', value: '极兔快递', code: 'JT' },
      { label: '京东物流', value: '京东物流', code: 'JD' },
      { label: '其他', value: '其他', code: '' }
    ]

    // 当前选中的快递编码
    const currentExpressCode = ref('')
    const previewTrackingNo = ref('')

    // 跳转商品列表
    const goProducts = () => {
      window.location.href = '/front/products'
    }

    // 跳转订单详情
    const goOrderDetail = (order) => {
      const id = order.id
      if (!id) {
        return
      }
      router.push({ name: 'FrontOrderDetail', params: { orderId: String(id) } })
    }

    // 统计
    const stats = computed(() => ({
      total: orders.value.length,
      pending: orders.value.filter(o => o.status === 'PENDING' || o.status === 'pending').length,
      paid: orders.value.filter(o => o.status === 'PAID' || o.status === 'paid' || o.status === 'PROCESSING' || o.status === 'processing').length,
      shipping: orders.value.filter(o => o.status === 'SHIPPED' || o.status === 'SHIPPING' || o.status === 'shipping' || o.status === 'shipped').length,
      completed: orders.value.filter(o => o.status === 'DELIVERED' || o.status === 'delivered' || o.status === 'COMPLETED' || o.status === 'completed').length,
      refunded: orders.value.filter(o => o.status === 'REFUNDED' || o.status === 'refunded').length
    }))

    // 过滤订单
    const filteredOrders = computed(() => {
      if (!filterStatus.value) return orders.value
      const statusMap = {
        'pending': ['PENDING', 'pending'],
        'paid': ['PAID', 'paid', 'PROCESSING', 'processing'],
        'processing': ['PAID', 'paid', 'PROCESSING', 'processing'],
        'shipping': ['SHIPPED', 'SHIPPING', 'shipping', 'shipped'],
        'completed': ['DELIVERED', 'delivered', 'COMPLETED', 'completed'],
        'refunded': ['REFUNDED', 'refunded'],
        'PENDING': ['PENDING', 'pending'],
        'PAID': ['PAID', 'paid', 'PROCESSING', 'processing'],
        'SHIPPED': ['SHIPPED', 'SHIPPING', 'shipping', 'shipped'],
        'DELIVERED': ['DELIVERED', 'delivered'],
        'COMPLETED': ['COMPLETED', 'completed'],
        'REFUNDED': ['REFUNDED', 'refunded']
      }
      const targetStatuses = statusMap[filterStatus.value] || [filterStatus.value]
      return orders.value.filter(o => targetStatuses.includes(o.status))
    })

    // 加载订单
    const loadOrders = async () => {
      loading.value = true
      try {
        let res
        if (isMerchant.value) {
          res = await getMerchantOrders({ page: 1, pageSize: 100 })
        } else {
          res = await getOrderPage({ page: 1, pageSize: 100 })
        }
        let data = res
        if (res?.data) data = res.data
        
        orders.value = (data?.records) ? data.records : 
                      (data?.list) ? data.list : 
                      (Array.isArray(data) ? data : [])
        
        orders.value.sort((a, b) => new Date(b.createTime || b.createdAt || 0) - new Date(a.createTime || a.createdAt || 0))
      } catch (error) {
        console.error('加载订单失败:', error)
        orders.value = []
      } finally {
        loading.value = false
      }
    }

    // 状态文本
    const getStatusText = (status) => {
      const map = { 
        'PENDING': '待支付', 
        'PAID': '待发货', 
        'processing': '待发货',
        'SHIPPED': '配送中',
        'SHIPPING': '配送中',
        'shipping': '配送中',
        'shipped': '配送中',
        'DELIVERED': '已完成', 
        'COMPLETED': '已完成',
        'completed': '已完成',
        'cancelled': '已取消',
        'CANCELLED': '已取消',
        'REFUNDED': '已退款'
      }
      return map[status] || status || '未知'
    }

    const getStatusType = (status) => {
      const map = { 
        'PENDING': 'danger', 
        'pending': 'danger',
        'PAID': 'warning', 
        'paid': 'warning', 
        'processing': 'warning',
        'SHIPPED': 'primary',
        'SHIPPING': 'primary',
        'shipping': 'primary',
        'shipped': 'primary',
        'DELIVERED': 'success', 
        'COMPLETED': 'success',
        'completed': 'success',
        'CANCELLED': 'info',
        'cancelled': 'info',
        'REFUNDED': 'info'
      }
      return map[status] || 'info'
    }

    // 计算订单总额
    const calculateTotal = (order) => {
      const subtotal = (order.products || []).reduce((sum, p) => sum + ((p.price || 0) * (p.count || 1)), 0)
      return subtotal
    }

    // 获取图片
    const getImage = (product) => {
      const img = product.image || product.img
      return getFullImageUrl(img, null, product.id)
    }

    // 图片加载失败
    const handleImageError = (e) => {
      e.target.src = '/imgs/foods/1.png'
    }

    // 支付 - 跳转到订单详情页进行支付和选择优惠券
    const handlePay = (order) => {
      router.push(`/front/order/${order.id}`)
    }

    // 取消订单
    const handleCancel = async (order) => {
      try {
        await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
        await cancelOrder(order.id)
        ElMessage.success('订单已取消')
        loadOrders()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('取消失败')
        }
      }
    }

    // 确认收货
    const handleConfirm = async (order) => {
      try {
        await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', { type: 'info' })
        await confirmReceive(order.id)
        ElMessage.success('确认收货成功')
        loadOrders()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('操作失败')
        }
      }
    }

    // 快递公司编码映射
    const EXPRESS_CODE_MAP = {
      '顺丰快递': 'SF',
      '圆通快递': 'YT',
      '中通快递': 'ZT',
      '韵达快递': 'YD',
      '申通快递': 'ST',
      '邮政快递': 'EMS',
      '极兔快递': 'JT',
      '京东物流': 'JD',
      '其他': ''
    }

    // 获取订单商品名称列表
    const getOrderProductNames = (order) => {
      const products = order.products || []
      if (products.length === 0) return '-'
      const names = products.map(p => p.productName || p.name || '商品').slice(0, 3)
      return names.join('、') + (products.length > 3 ? ` 等${products.length}件` : '')
    }

    // 快递公司变更
    const onExpressChange = (val) => {
      const opt = expressOptions.find(o => o.value === val)
      currentExpressCode.value = opt ? opt.code : ''
      updatePreview()
    }

    // 快递单号输入
    const onExpressNoInput = () => {
      updatePreview()
    }

    // 更新预览
    const updatePreview = () => {
      if (shipForm.value.expressNo && currentExpressCode.value) {
        previewTrackingNo.value = `${currentExpressCode.value}-${shipForm.value.expressNo}`
      } else if (shipForm.value.expressNo) {
        previewTrackingNo.value = shipForm.value.expressNo
      } else {
        previewTrackingNo.value = ''
      }
    }

    // 商家发货 - 直接跳转到订单详情页进行发货操作
    const handleShip = (order) => {
      if (!order || !order.id) {
        ElMessage.error('订单信息无效')
        return
      }
      // 直接跳转到订单详情页，在详情页进行发货操作
      router.push({ name: 'FrontOrderDetail', params: { orderId: String(order.id) } })
    }

    const submitShip = async () => {
      if (!shipForm.value.expressCompany) {
        ElMessage.warning('请选择快递公司')
        return
      }
      if (!shipForm.value.expressNo) {
        ElMessage.warning('请输入快递单号')
        return
      }
      if (!currentOrder.value || !currentOrder.value.id) {
        ElMessage.error('订单信息无效')
        return
      }
      shipping.value = true
      try {
        const codePrefix = EXPRESS_CODE_MAP[shipForm.value.expressCompany] || ''
        const trackingNo = codePrefix ? `${codePrefix}-${shipForm.value.expressNo}` : shipForm.value.expressNo

        await merchantShipOrder(currentOrder.value.id, {
          expressCompany: shipForm.value.expressCompany,
          expressNo: trackingNo
        })
        ElMessage.success('发货成功！买家将收到物流通知')
        shipDialogVisible.value = false
        // 发货成功后跳转到订单详情页查看发货状态
        router.push({ name: 'FrontOrderDetail', params: { orderId: String(currentOrder.value.id) } })
      } catch (err) {
        const msg = err?.message || err?.response?.data?.message || '发货失败，请重试'
        ElMessage.error(msg)
      } finally {
        shipping.value = false
      }
    }

    // 再来一单
    const handleReorder = (order) => {
      goProducts()
    }

    // 申请售后
    const goAfterSales = (order) => {
      const id = order.id
      if (!id) return
      router.push({ name: 'FrontOrderDetail', params: { orderId: String(id) } })
    }

    // 申请退款
    const handleRefund = (order) => {
      const id = order.id
      if (!id) return
      router.push({ name: 'FrontOrderDetail', params: { orderId: String(id) }, query: { action: 'refund' } })
    }

    // 查看物流
    const viewLogistics = (order) => {
      if (!order || !order.id) {
        ElMessage.error('订单信息无效')
        return
      }
      currentLogisticsOrder.value = order
      logisticsDialogVisible.value = true
    }

    // 复制快递单号
    const copyTrackingNo = (order) => {
      const no = order.trackingNumber || order.expressNo
      if (!no) {
        ElMessage.warning('暂无快递单号')
        return
      }
      navigator.clipboard.writeText(no).then(() => {
        ElMessage.success('快递单号已复制')
      }).catch(() => {
        const textarea = document.createElement('textarea')
        textarea.value = no
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        ElMessage.success('快递单号已复制')
      })
    }

    // 格式化时间
    const formatDateTime = (date) => {
      if (!date) return '-'
      const d = new Date(date)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    }

    onMounted(() => {
      if (!isLoggedIn()) {
        router.push('/login')
        return
      }
      const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
      isMerchant.value = user.role === 'MERCHANT'
      loadOrders()
    })

    return {
      orders,
      loading,
      filterStatus,
      stats,
      filteredOrders,
      isMerchant,
      shipDialogVisible,
      shipForm,
      shipping,
      logisticsDialogVisible,
      currentLogisticsOrder,
      expressOptions,
      currentExpressCode,
      previewTrackingNo,
      currentOrder,
      goProducts,
      goOrderDetail,
      loadOrders,
      getStatusText,
      getStatusType,
      calculateTotal,
      getImage,
      handleImageError,
      handlePay,
      handleCancel,
      handleConfirm,
      handleShip,
      submitShip,
      viewLogistics,
       getOrderProductNames,
       onExpressChange,
       onExpressNoInput,
       copyTrackingNo,
       handleReorder,
      goAfterSales,
      handleRefund,
      formatPrice,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.orders-page {
  max-width: 1000px;
  margin: 20px auto;
  padding: 20px;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-header h2 i {
  color: #4caf50;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 2px solid transparent;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-card.active {
  border-color: #4caf50;
  background: #f0fff0;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-icon.total { background: linear-gradient(135deg, #4caf50, #8bc34a); }
.stat-icon.pending { background: linear-gradient(135deg, #f44336, #e57373); }
.stat-icon.processing { background: linear-gradient(135deg, #2196f3, #64b5f6); }
.stat-icon.shipping { background: linear-gradient(135deg, #9c27b0, #ba68c8); }
.stat-icon.completed { background: linear-gradient(135deg, #4caf50, #81c784); }

.stat-info { flex: 1; }

.stat-value {
  font-size: 22px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.loading-state {
  background: white;
  border-radius: 12px;
  padding: 20px;
}

.order-card {
  background: white;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: #fafafa;
  border-bottom: 1px solid #eee;
}

.order-info {
  display: flex;
  gap: 20px;
  align-items: center;
}

.order-no {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.order-time {
  color: #999;
  font-size: 12px;
}

.order-products {
  display: flex;
  gap: 12px;
  padding: 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.order-products:hover {
  background: #fafafa;
}

.product-item {
  display: flex;
  gap: 12px;
  flex: 1;
}

.product-image {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  background: #f5f5f5;
  flex-shrink: 0;
  object-fit: cover;
}

.product-info { flex: 1; }

.product-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  color: #ff5722;
  font-weight: 600;
  font-size: 14px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-top: 1px solid #eee;
  background: #fafafa;
}

.order-amount {
  font-size: 14px;
}

.amount-label {
  color: #666;
}

.amount-value {
  color: #ff5722;
  font-weight: bold;
  font-size: 20px;
}

.order-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.express-info {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: #e6f7ff;
  border-radius: 4px;
  font-size: 12px;
  color: #1890ff;
}

.express-info i {
  font-size: 14px;
}

.express-info .tracking-no {
  font-family: monospace;
  font-weight: 600;
  letter-spacing: 0.5px;
}

/* ========== 物流信息条 ========== */
.order-express-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f5ff 100%);
  border-top: 1px dashed #b3d8ff;
  border-bottom: 1px dashed #b3d8ff;
}

.express-bar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.express-bar-left i {
  color: #409eff;
  font-size: 16px;
}

.express-company-name {
  color: #409eff;
  font-weight: 600;
}

.express-divider {
  color: #c0c4cc;
}

.express-tracking-no {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #303133;
  letter-spacing: 0.5px;
  background: rgba(255, 255, 255, 0.7);
  padding: 2px 8px;
  border-radius: 3px;
}

.copy-tracking-btn {
  color: #409eff;
  font-size: 12px;
  padding: 4px 8px;
}

.copy-tracking-btn:hover {
  color: #337ecc;
}

/* ========== 发货对话框样式 ========== */
.ship-dialog-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  color: #303133;
}

.ship-dialog-title i {
  color: #4caf50;
  font-size: 22px;
}

.ship-order-summary {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.ship-order-summary .summary-row {
  display: flex;
  padding: 5px 0;
  font-size: 13px;
}

.ship-order-summary .summary-label {
  width: 70px;
  color: #909399;
  flex-shrink: 0;
}

.ship-order-summary .summary-value {
  color: #303133;
  flex: 1;
  word-break: break-all;
}

.ship-form {
  margin-top: 4px;
}

.express-code-hint {
  float: right;
  color: #909399;
  font-size: 12px;
  font-family: monospace;
}

.tracking-preview {
  margin-top: 8px;
  padding: 8px 12px;
  background: #ecf5ff;
  border-radius: 4px;
  font-size: 13px;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 6px;
}

.tracking-preview i {
  font-size: 14px;
}

.tracking-preview strong {
  font-family: monospace;
  letter-spacing: 0.5px;
}

.ship-tips {
  margin-top: 12px;
  padding: 10px 14px;
  background: #fdf6ec;
  border-radius: 6px;
  font-size: 13px;
  color: #e6a23c;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ship-tips i {
  font-size: 16px;
  flex-shrink: 0;
}

.empty-orders {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 12px;
}

.empty-icon {
  font-size: 80px;
  color: #ddd;
  margin-bottom: 20px;
}

.empty-orders h3 {
  margin: 0 0 8px;
  color: #333;
}

.empty-orders p {
  margin: 0 0 20px;
  color: #999;
}

.empty-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .order-products {
    flex-direction: column;
  }
  
  .order-footer {
    flex-direction: column;
    gap: 12px;
  }
  
  .order-amount {
    width: 100%;
    text-align: center;
  }
  
  .order-actions {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
