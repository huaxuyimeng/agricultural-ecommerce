/**
 * 购物车页面
 * 文件路径: src/views/front/Cart.vue
 * 功能描述: 用户购物车管理，展示商品列表含数量调整（增减/输入）、单价小计显示，
 *           全选/单选切换、已选商品总计计算、收藏商品快捷操作、空购物车引导去逛逛，
 *           支持清空购物车、去结算（跳转CheckoutDialog）、加载状态提示
 * 关联文件:
 * - src/api/index.js: 提供购物车数据接口
 * - src/utils/cart.js: 购物车数据管理工具
 * - src/views/front/components/CheckoutDialog.vue: 结算弹窗组件
 */
<template>
  <div class="cart-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-shopping-cart-2"></i> 我的购物车</h2>
        <span class="cart-count">共 {{ cartItems.length }} 件商品</span>
      </div>
      <el-button v-if="cartItems.length > 0" type="text" @click="clearCart" class="clear-btn">
        <i class="el-icon-delete"></i> 清空
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <i class="el-icon-loading"></i>
      <span>加载中...</span>
    </div>

    <!-- 购物车主内容 -->
    <div v-else-if="cartItems.length > 0" class="cart-container">
      <!-- 左侧：商品列表 -->
      <div class="cart-main">
        <!-- 全选栏 -->
        <div class="select-bar">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">
            全选
          </el-checkbox>
          <span class="selected-info">已选 {{ selectedItems.length }} 件商品</span>
        </div>

        <!-- 商品列表 -->
        <div class="product-list">
          <div
            v-for="item in cartItems"
            :key="item.id"
            :class="['product-card', { selected: isSelected(item) }]"
          >
            <div class="product-checkbox">
              <el-checkbox
                :value="isSelected(item)"
                @change="toggleSelect(item)"
              />
            </div>

            <div class="product-image-wrapper" @click="goProduct(item)">
              <img :src="getImage(item)" class="product-image" @error="handleImageError($event)" />
              <div v-if="item.stock === 0" class="stock-overlay">缺货</div>
            </div>

            <div class="product-content">
              <div class="product-name" @click="goProduct(item)">
                {{ item.name || item.productName || '未知商品' }}
              </div>
              <div class="product-meta">
                <span class="stock-status" :class="getStockClass(item)">
                  <i :class="getStockIcon(item)"></i>
                  {{ getStockText(item) }}
                </span>
              </div>
              <div class="product-price-row">
                <span class="product-price">¥{{ formatPrice(item.price) }}</span>
              </div>
            </div>

            <div class="product-actions">
              <div class="quantity-control">
                <el-button
                  size="mini"
                  circle
                  :disabled="item.count <= 1"
                  @click="decreaseQuantity(item)"
                >
                  <i class="el-icon-minus"></i>
                </el-button>
                <span class="quantity-value">{{ item.count }}</span>
                <el-button
                  size="mini"
                  circle
                  :disabled="item.count >= 99"
                  @click="increaseQuantity(item)"
                >
                  <i class="el-icon-plus"></i>
                </el-button>
              </div>
              <div class="subtotal">¥{{ formatPrice((item.price || 0) * item.count) }}</div>
              <el-button type="text" class="remove-btn" @click="handleRemove(item)">
                <i class="el-icon-delete"></i>
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：结算面板 -->
      <div class="cart-sidebar">
        <div class="summary-card">
          <h3 class="summary-title">订单摘要</h3>

          <div class="summary-row">
            <span class="summary-label">商品总价</span>
            <span class="summary-value">¥{{ formatPrice(selectedTotal) }}</span>
          </div>

          <div class="summary-row">
            <span class="summary-label">运费</span>
            <span class="summary-value" :class="{ free: shippingFee === 0 }">
              {{ shippingFee > 0 ? '¥' + formatPrice(shippingFee) : '免费' }}
            </span>
          </div>

          <div v-if="selectedTotal >= 99" class="shipping-tip">
            <i class="el-icon-check"></i> 满99元免运费
          </div>

          <div class="summary-divider"></div>

          <div class="summary-row total">
            <span class="summary-label">实付金额</span>
            <span class="summary-value price">¥{{ formatPrice(selectedTotal + shippingFee) }}</span>
          </div>

          <el-button
            type="primary"
            size="large"
            :disabled="selectedItems.length === 0"
            @click="handleCheckout"
            class="checkout-btn"
          >
            去结算 ({{ selectedItems.length }})
          </el-button>

          <div v-if="selectedItems.length > 0" class="checkout-tip">
            已选 {{ selectedItems.length }} 件商品
          </div>
        </div>

        <!-- 推荐商品（可选） -->
        <div class="recommend-section">
          <div class="recommend-header">
            <i class="el-icon-shopping-bag-2"></i>
            <span>猜你喜欢</span>
          </div>
          <el-button type="text" @click="goProducts" class="browse-btn">
            继续逛逛 <i class="el-icon-arrow-right"></i>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空购物车 -->
    <div v-else class="empty-cart">
      <div class="empty-icon-wrapper">
        <i class="el-icon-shopping-cart-2 empty-icon"></i>
        <div class="empty-shadow"></div>
      </div>
      <h3>购物车是空的</h3>
      <p>快去挑选心仪的商品吧</p>
      <el-button type="primary" size="large" @click="goProducts" class="go-shopping-btn">
        <i class="el-icon-shopping-bag-2"></i> 去购物
      </el-button>
    </div>

    <!-- 结算对话框 -->
    <CheckoutDialog
      :visible.sync="checkoutVisible"
      :selected-items="selectedItems"
      @success="handleCheckoutSuccess"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from '@vue/composition-api'
import { MessageBox, Message } from 'element-ui'
import { getUserCart, updateCartItem as apiUpdateCartItem, removeFromCart as apiRemoveFromCart, clearUserCart as apiClearCart } from '@/api'
import { formatPrice, getFullImageUrl, notifyCartChanged, isLoggedIn } from '@/utils/common'
import CheckoutDialog from './components/CheckoutDialog.vue'

export default {
  name: 'CartPage',
  components: { CheckoutDialog },
  setup() {
    const cartItems = ref([])
    const loading = ref(false)
    const selectedItems = ref([])
    const selectAll = ref(false)
    const checkoutVisible = ref(false)

    // 计算属性
    const selectedTotal = computed(() => {
      return selectedItems.value.reduce((sum, item) => sum + ((item.price || 0) * (item.count || 1)), 0)
    })

    const shippingFee = computed(() => selectedTotal.value >= 99 ? 0 : 10)

    // 跳转商品详情
    const goProduct = (item) => {
      const id = item.productId || item.id || (item.product && item.product.id)
      if (id) {
        window.location.href = `/front/product/${id}`
      }
    }

    // 跳转商品列表
    const goProducts = () => {
      window.location.href = '/front/products'
    }

    // 判断商品是否选中
    const isSelected = (item) => {
      return selectedItems.value.some(i => i.id === item.id)
    }

    // 切换选中状态
    const toggleSelect = (item) => {
      const index = selectedItems.value.findIndex(i => i.id === item.id)
      if (index > -1) {
        selectedItems.value.splice(index, 1)
      } else {
        selectedItems.value.push(item)
      }
      selectAll.value = selectedItems.value.length === cartItems.value.length
    }

    // 全选/取消全选
    const handleSelectAll = (val) => {
      if (val) {
        selectedItems.value = [...cartItems.value]
      } else {
        selectedItems.value = []
      }
    }

    // 获取图片
    const getImage = (item) => {
      const img = item.image || (item.product && item.product.image) || item.img
      return getFullImageUrl(img, null, item.id || item.productId)
    }

    // 图片加载失败
    const handleImageError = (event) => {
      event.target.src = '/imgs/foods/1.png'
    }

    // 获取库存状态文本
    const getStockText = (item) => {
      if (item.stock === 0) return '缺货'
      if (item.stock && item.stock <= 10) return '库存紧张'
      return '有货'
    }

    // 获取库存状态样式
    const getStockClass = (item) => {
      if (item.stock === 0) return 'out-of-stock'
      if (item.stock && item.stock <= 10) return 'low-stock'
      return 'in-stock'
    }

    // 获取库存图标
    const getStockIcon = (item) => {
      if (item.stock === 0) return 'el-icon-close'
      if (item.stock && item.stock <= 10) return 'el-icon-warning'
      return 'el-icon-check'
    }

    // 减少数量
    const decreaseQuantity = (item) => {
      if (item.count > 1) {
        item.count--
        handleQuantityChange(item)
      }
    }

    // 增加数量
    const increaseQuantity = (item) => {
      if (item.count < 99) {
        item.count++
        handleQuantityChange(item)
      }
    }

    // 修改数量
    const handleQuantityChange = async (item) => {
      const newCount = item.count
      if (newCount < 1 || newCount > 99) {
        item.count = Math.max(1, Math.min(99, newCount))
        return
      }
      try {
        await apiUpdateCartItem(item.id, newCount)
        notifyCartChanged()
      } catch (error) {
        console.error('更新数量失败:', error)
        Message.error('更新数量失败，请稍后重试')
        loadCart()
      }
    }

    // 移除商品
    const handleRemove = async (item) => {
      try {
        await MessageBox.confirm('确定要从购物车移除该商品吗？', '提示', {
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        })
        await apiRemoveFromCart(item.id)
        notifyCartChanged()
        Message.success('已移除商品')
        // 从本地列表中移除
        const index = cartItems.value.findIndex(i => i.id === item.id)
        if (index > -1) cartItems.value.splice(index, 1)
        const selIndex = selectedItems.value.findIndex(i => i.id === item.id)
        if (selIndex > -1) selectedItems.value.splice(selIndex, 1)
        selectAll.value = selectedItems.value.length === cartItems.value.length
      } catch (error) {
        if (error !== 'cancel') {
          console.error('移除商品失败:', error)
          Message.error('移除失败，请稍后重试')
        }
      }
    }

    // 清空购物车
    const clearCart = async () => {
      try {
        await MessageBox.confirm('确定要清空购物车吗？', '提示', {
          type: 'warning',
          confirmButtonText: '确定清空',
          cancelButtonText: '取消'
        })
        await apiClearCart()
        notifyCartChanged()
        Message.success('购物车已清空')
        cartItems.value = []
        selectedItems.value = []
        selectAll.value = false
      } catch (error) {
        if (error !== 'cancel') {
          console.error('清空购物车失败:', error)
          Message.error('清空失败，请稍后重试')
        }
      }
    }

    // 加载购物车
    const loadCart = async () => {
      loading.value = true
      try {
        const res = await getUserCart()
        let items = []
        if (Array.isArray(res)) {
          items = res
        } else if (res && res.data) {
          items = Array.isArray(res.data) ? res.data : (res.data.list || [])
        } else if (res && res.list) {
          items = Array.isArray(res.list) ? res.list : []
        }

        cartItems.value = items.map((item, index) => {
          const product = item.product || {}
          return {
            ...item,
            count: item.quantity || item.count || 1,
            name: product.name || item.productName || item.name || '未知商品',
            image: product.image || item.img || item.image,
            price: item.price || product.price || 0,
            stock: item.stock ?? product.stock ?? item.stockNum ?? 999,
            id: item.id || item.cartId || `temp-${index}`,
            productId: item.productId || product.id || item.id || item.cartId
          }
        })
        selectedItems.value = []
        selectAll.value = false
      } catch (error) {
        console.error('加载购物车失败:', error)
        cartItems.value = []
        Message.error('加载购物车失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    // 去结算
    const handleCheckout = () => {
      if (!isLoggedIn()) {
        Message.warning('请先登录')
        setTimeout(() => { window.location.href = '/login' }, 500)
        return
      }
      if (selectedItems.value.length === 0) {
        Message.warning('请选择要结算的商品')
        return
      }
      const outOfStock = selectedItems.value.some(item => {
        return item.stock !== undefined && item.stock !== null && item.stock === 0
      })
      if (outOfStock) {
        Message.warning('所选商品中包含缺货商品，请取消选择后再结算')
        return
      }
      checkoutVisible.value = true
    }

    // 结算成功
    const handleCheckoutSuccess = () => {
      loadCart()
    }

    // 监听购物车变化
    onMounted(() => {
      if (!isLoggedIn()) {
        Message.warning('请先登录')
        setTimeout(() => { window.location.href = '/login' }, 500)
        return
      }
      loadCart()
      window.addEventListener('xm-cart-changed', loadCart)
    })

    onUnmounted(() => {
      window.removeEventListener('xm-cart-changed', loadCart)
    })

    return {
      cartItems,
      loading,
      selectedItems,
      selectAll,
      checkoutVisible,
      selectedTotal,
      shippingFee,
      loadCart,
      getImage,
      handleImageError,
      goProduct,
      goProducts,
      isSelected,
      toggleSelect,
      handleSelectAll,
      getStockText,
      getStockClass,
      getStockIcon,
      decreaseQuantity,
      increaseQuantity,
      handleQuantityChange,
      handleRemove,
      clearCart,
      handleCheckout,
      handleCheckoutSuccess,
      formatPrice
    }
  }
}
</script>

<style scoped>
.cart-page {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px 40px;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 2px solid #ff9800;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-header h2 i {
  color: #ff9800;
  font-size: 28px;
}

.cart-count {
  color: #999;
  font-size: 14px;
}

.clear-btn {
  color: #999;
}

.clear-btn:hover {
  color: #f56c6c;
}

/* 加载状态 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 80px;
  color: #999;
  gap: 10px;
  font-size: 16px;
}

.loading-container i {
  font-size: 28px;
  color: #ff9800;
}

/* 主容器 */
.cart-container {
  display: flex;
  gap: 24px;
}

/* 左侧商品列表 */
.cart-main {
  flex: 1;
  min-width: 0;
}

/* 全选栏 */
.select-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.selected-info {
  color: #666;
  font-size: 14px;
}

/* 商品列表 */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-card {
  display: grid;
  grid-template-columns: 40px 100px 1fr auto;
  grid-template-rows: auto auto;
  align-items: center;
  gap: 12px 16px;
  padding: 16px 20px;
  background: #fff;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  transition: all 0.2s;
}

.product-card:hover {
  border-color: #ddd;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.product-card.selected {
  border-color: #ff9800;
  background: #fffbf5;
}

.product-checkbox {
  grid-row: 1 / 3;
  display: flex;
  align-items: center;
}

.product-image-wrapper {
  grid-row: 1 / 3;
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  cursor: pointer;
  background: #f5f5f5;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.2s;
}

.product-image-wrapper:hover .product-image {
  transform: scale(1.05);
}

.stock-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.product-content {
  grid-column: 3;
  grid-row: 1;
  min-width: 0;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-name:hover {
  color: #ff9800;
}

.product-meta {
  margin-bottom: 6px;
}

.stock-status {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  padding: 2px 8px;
  border-radius: 4px;
}

.stock-status.in-stock {
  color: #67c23a;
  background: #f0f9eb;
}

.stock-status.low-stock {
  color: #e6a23c;
  background: #fdf6ec;
}

.stock-status.out-of-stock {
  color: #f56c6c;
  background: #fef0f0;
}

.product-price-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-price {
  font-size: 18px;
  font-weight: 600;
  color: #ff9800;
}

/* 商品操作区 */
.product-actions {
  grid-column: 4;
  grid-row: 1 / 3;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-shrink: 0;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quantity-value {
  min-width: 32px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.subtotal {
  font-size: 16px;
  font-weight: 600;
  color: #ff5722;
}

.remove-btn {
  color: #c0c4cc;
}

.remove-btn:hover {
  color: #f56c6c;
}

/* 右侧结算面板 */
.cart-sidebar {
  width: 320px;
  flex-shrink: 0;
}

.summary-card {
  position: sticky;
  top: 20px;
  background: #fff;
  border: 2px solid #ff9800;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(255, 152, 0, 0.15);
}

.summary-title {
  margin: 0 0 16px;
  font-size: 18px;
  color: #333;
  padding-bottom: 12px;
  border-bottom: 1px dashed #e8e8e8;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.summary-label {
  color: #666;
  font-size: 14px;
}

.summary-value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.summary-value.free {
  color: #67c23a;
  font-weight: 600;
}

.summary-value.price {
  font-size: 22px;
  font-weight: 700;
  color: #ff5722;
}

.shipping-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #67c23a;
  background: #f0f9eb;
  padding: 6px 10px;
  border-radius: 4px;
  margin-bottom: 12px;
}

.summary-divider {
  height: 1px;
  background: #e8e8e8;
  margin: 16px 0;
}

.summary-row.total {
  margin-bottom: 20px;
}

.checkout-btn {
  width: 100%;
  height: 48px;
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff9800, #ff5722);
  border: none;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
}

.checkout-btn:hover {
  background: linear-gradient(135deg, #ff5722, #ff9800);
  box-shadow: 0 6px 16px rgba(255, 152, 0, 0.4);
}

.checkout-btn:disabled {
  background: #ccc;
  box-shadow: none;
}

.checkout-tip {
  text-align: center;
  margin-top: 12px;
  font-size: 13px;
  color: #999;
}

/* 推荐区域 */
.recommend-section {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.recommend-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.recommend-header i {
  color: #ff9800;
  font-size: 18px;
}

.browse-btn {
  color: #ff9800;
  font-size: 14px;
}

.browse-btn:hover {
  color: #ff5722;
}

/* 空购物车 */
.empty-cart {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 12px;
}

.empty-icon-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 24px;
}

.empty-icon {
  font-size: 120px;
  color: #e8e8e8;
}

.empty-shadow {
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 20px;
  background: radial-gradient(ellipse, rgba(0,0,0,0.1) 0%, transparent 70%);
}

.empty-cart h3 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #666;
}

.empty-cart p {
  color: #999;
  margin: 0 0 24px;
  font-size: 14px;
}

.go-shopping-btn {
  background: linear-gradient(135deg, #ff9800, #ff5722);
  border: none;
  padding: 14px 40px;
  font-size: 16px;
  border-radius: 24px;
}

.go-shopping-btn:hover {
  background: linear-gradient(135deg, #ff5722, #ff9800);
}

/* 响应式 */
@media (max-width: 900px) {
  .cart-container {
    flex-direction: column;
  }

  .cart-sidebar {
    width: 100%;
    order: -1;
  }

  .summary-card {
    position: static;
  }
}

@media (max-width: 600px) {
  .product-card {
    flex-wrap: wrap;
  }

  .product-content {
    flex-basis: calc(100% - 140px);
  }

  .product-actions {
    flex-direction: row;
    width: 100%;
    justify-content: space-between;
    padding-top: 12px;
    border-top: 1px dashed #e8e8e8;
  }
}
</style>
