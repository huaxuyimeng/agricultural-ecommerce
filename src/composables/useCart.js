/**
 * 购物车状态管理 Hook
 * 文件路径: src/composables/useCart.js
 * 功能描述: 提供购物车相关的状态管理和操作方法
 */
import { ref, computed } from '@vue/composition-api'
import { getUserCart, addToCart as apiAddToCart, updateCartItem as apiUpdateCartItem, removeFromCart as apiRemoveFromCart, clearUserCart as apiClearCart } from '@/api'
import { ElMessage } from 'element-ui'
import { notifyCartChanged } from '@/utils/common'

export function useCart() {
  const cartItems = ref([])
  const loading = ref(false)

  // 购物车统计
  const cartStats = computed(() => {
    const count = cartItems.value.reduce((sum, item) => sum + (item.quantity || item.count || 0), 0)
    const total = cartItems.value.reduce((sum, item) => sum + ((item.price || 0) * (item.quantity || item.count || 0)), 0)
    return { count, total }
  })

  // 加载购物车
  const loadCart = async () => {
    loading.value = true
    try {
      const res = await getUserCart()
      cartItems.value = res.data || []
    } catch (e) {
      cartItems.value = []
    } finally {
      loading.value = false
    }
  }

  // 添加到购物车
  const addToCart = async (productId, quantity = 1) => {
    try {
      await apiAddToCart({ productId, count: quantity })
      ElMessage.success('已加入购物车')
      notifyCartChanged()
      await loadCart()
      return true
    } catch (e) {
      ElMessage.error(e.message || '添加失败')
      return false
    }
  }

  // 更新数量
  const updateQuantity = async (itemId, quantity) => {
    try {
      await apiUpdateCartItem(itemId, quantity)
      notifyCartChanged()
      await loadCart()
      return true
    } catch (e) {
      ElMessage.error('更新数量失败')
      return false
    }
  }

  // 移出商品
  const removeItem = async (itemId) => {
    try {
      await apiRemoveFromCart(itemId)
      ElMessage.success('已移除')
      notifyCartChanged()
      await loadCart()
      return true
    } catch (e) {
      ElMessage.error('移除失败')
      return false
    }
  }

  // 清空购物车
  const clearCart = async () => {
    try {
      await apiClearCart()
      ElMessage.success('购物车已清空')
      notifyCartChanged()
      await loadCart()
      return true
    } catch (e) {
      ElMessage.error('清空失败')
      return false
    }
  }

  // 选中/取消选中商品
  const toggleSelect = (item) => {
    item._selected = !item._selected
  }

  // 全选/取消全选
  const toggleSelectAll = () => {
    const allSelected = cartItems.value.every(item => item._selected)
    cartItems.value.forEach(item => { item._selected = !allSelected })
  }

  // 获取选中的商品
  const getSelectedItems = () => {
    return cartItems.value.filter(item => item._selected)
  }

  // 计算选中商品的总价
  const getSelectedTotal = () => {
    return getSelectedItems().reduce((sum, item) => sum + ((item.price || 0) * (item.quantity || 1)), 0)
  }

  return {
    cartItems,
    loading,
    cartStats,
    loadCart,
    addToCart,
    updateQuantity,
    removeItem,
    clearCart,
    toggleSelect,
    toggleSelectAll,
    getSelectedItems,
    getSelectedTotal
  }
}
