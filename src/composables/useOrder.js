/**
 * 订单状态管理 Hook
 * 文件路径: src/composables/useOrder.js
 * 功能描述: 提供订单相关的状态管理和操作方法
 */
import { ref, computed } from '@vue/composition-api'
import { getOrderPage, payOrder, cancelOrder, confirmReceive, updateOrderAddress } from '@/api'
import { ElMessage } from 'element-ui'

export function useOrder() {
  const orders = ref([])
  const loading = ref(false)
  const currentOrder = ref(null)

  // 订单统计
  const orderStats = computed(() => {
    return {
      total: orders.value.length,
      pending: orders.value.filter(o => o.status === 'pending').length,
      processing: orders.value.filter(o => o.status === 'processing').length,
      shipping: orders.value.filter(o => o.status === 'shipping').length,
      completed: orders.value.filter(o => o.status === 'completed').length,
      cancelled: orders.value.filter(o => o.status === 'cancelled').length,
      totalAmount: orders.value.reduce((sum, o) => sum + calculateOrderTotal(o), 0)
    }
  })

  // 加载订单列表
  const loadOrders = async () => {
    loading.value = true
    try {
      const res = await getOrderPage({ pageNum: 1, pageSize: 100 })
      orders.value = res.data?.records || res.data?.list || []
      // 按时间排序
      orders.value.sort((a, b) => new Date(b.createTime || b.createdAt) - new Date(a.createTime || a.createdAt))
    } catch (e) {
      orders.value = []
    } finally {
      loading.value = false
    }
  }

  // 支付订单
  const handlePay = async (order) => {
    try {
      await payOrder(order.id || order.orderId, 'alipay')
      ElMessage.success('支付成功')
      await loadOrders()
      return true
    } catch (e) {
      ElMessage.error('支付失败')
      return false
    }
  }

  // 取消订单
  const handleCancel = async (order, reason = '用户取消') => {
    try {
      await cancelOrder(order.id || order.orderId, reason)
      ElMessage.success('订单已取消')
      await loadOrders()
      return true
    } catch (e) {
      ElMessage.error('取消失败')
      return false
    }
  }

  // 确认收货
  const handleConfirm = async (order) => {
    try {
      await confirmReceive(order.id || order.orderId)
      ElMessage.success('已确认收货')
      await loadOrders()
      return true
    } catch (e) {
      ElMessage.error('确认收货失败')
      return false
    }
  }

  // 更新收货地址
  const handleUpdateAddress = async (order, address) => {
    try {
      await updateOrderAddress(order.id || order.orderId, address)
      ElMessage.success('地址已更新')
      await loadOrders()
      return true
    } catch (e) {
      ElMessage.error('地址更新失败')
      return false
    }
  }

  // 计算订单总金额
  const calculateOrderTotal = (order) => {
    const subtotal = (order.products || []).reduce((sum, p) => sum + (p.price || 0) * (p.count || 0), 0)
    return subtotal + (order.shippingFee || 0) - (order.discount || 0)
  }

  // 获取订单状态文本
  const getStatusText = (status) => {
    const map = { pending: '待支付', processing: '待发货', shipping: '配送中', completed: '已完成', cancelled: '已取消' }
    return map[status] || '未知'
  }

  return {
    orders,
    loading,
    currentOrder,
    orderStats,
    loadOrders,
    handlePay,
    handleCancel,
    handleConfirm,
    handleUpdateAddress,
    calculateOrderTotal,
    getStatusText
  }
}
