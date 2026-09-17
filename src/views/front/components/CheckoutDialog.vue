/**
 * 结算确认弹窗组件
 * 文件路径: src/views/front/components/CheckoutDialog.vue
 * 功能描述: 订单结算确认弹窗，展示商品清单（图片、名称、单价×数量、小计），收货地址选择和新增，
 *           优惠券抵扣选择，费用明细（商品总额、运费、优惠券抵扣、实付金额），确认下单并提交订单
 * 关联文件:
 * - src/api/index.js: 提供地址、优惠券、订单创建接口
 */
<template>
  <el-dialog
    :visible.sync="dialogVisible"
    title="确认订单"
    width="950px"
    :close-on-click-modal="false"
    :append-to-body="true"
    @closed="handleClosed"
    class="checkout-dialog"
  >
    <!-- 订单商品列表 -->
    <div class="section order-items-section">
      <div class="section-header">
        <i class="el-icon-shopping-bag-2"></i>
        <span>商品清单</span>
        <span class="item-count">{{ displayItems.length }} 件商品</span>
      </div>
      <div class="order-items">
        <div v-for="item in displayItems" :key="item.key || item.id" class="order-item">
          <img :src="getImage(item)" class="item-image" @error="handleImageError" />
          <div class="item-content">
            <div class="item-name">{{ item.name || item.productName || '未知商品' }}</div>
            <div class="item-price">¥{{ formatPrice(item.price) }} × {{ item.quantity || item.count || 1 }}</div>
          </div>
          <div class="item-subtotal">¥{{ formatPrice((item.price || 0) * (item.quantity || item.count || 1)) }}</div>
        </div>
      </div>
    </div>

    <!-- 收货地址 -->
    <div class="section address-section">
      <div class="section-header">
        <i class="el-icon-location-information"></i>
        <span>收货信息</span>
      </div>

      <!-- 已保存的地址列表 -->
      <div v-if="savedAddresses.length > 0 && !showAddressForm" class="saved-addresses">
        <div
          v-for="addr in savedAddresses"
          :key="addr.id"
          :class="['saved-address-item', { active: selectedAddressId === addr.id }]"
          @click="selectAddress(addr)"
        >
          <div class="address-radio">
            <el-radio v-model="selectedAddressId" :label="addr.id">&nbsp;</el-radio>
          </div>
          <div class="address-content">
            <div class="address-header">
              <span class="receiver-name">{{ addr.receiverName || addr.name }}</span>
              <span class="receiver-phone">{{ addr.phone }}</span>
              <el-tag v-if="addr.isDefault" type="success" size="mini">默认</el-tag>
            </div>
            <div class="address-detail">
              {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress || addr.address }}
            </div>
          </div>
          <div class="address-actions">
            <el-button type="text" size="small" @click.stop="editAddress(addr)">编辑</el-button>
            <el-button type="text" size="small" @click.stop="deleteAddress(addr.id)">删除</el-button>
          </div>
        </div>
        <el-button type="primary" size="small" plain @click="showAddressForm = true" class="add-address-btn">
          <i class="el-icon-plus"></i> 使用新地址
        </el-button>
      </div>

      <!-- 地址表单 -->
      <div v-if="showAddressForm || savedAddresses.length === 0" class="address-form-container">
        <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-width="90px" size="small">
          <el-form-item label="收货人" prop="receiverName">
            <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="addressForm.phone" placeholder="请输入手机号码" maxlength="11" />
          </el-form-item>
          <el-row :gutter="10">
            <el-col :span="8">
              <el-form-item label="省份" prop="province">
                <el-input v-model="addressForm.province" placeholder="如：北京市" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="城市" prop="city">
                <el-input v-model="addressForm.city" placeholder="如：北京市" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="区县" prop="district">
                <el-input v-model="addressForm.district" placeholder="如：朝阳区" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="详细地址" prop="detailAddress">
            <el-input v-model="addressForm.detailAddress" placeholder="请输入详细地址（街道、门牌号等）" />
          </el-form-item>
        </el-form>

        <div v-if="savedAddresses.length > 0" class="form-actions">
          <el-button size="small" @click="cancelAddressForm">返回地址列表</el-button>
          <el-button type="text" size="small" @click="saveAddressForNextTime">保存到地址簿</el-button>
        </div>
      </div>
    </div>

    <!-- 费用明细 -->
    <div class="section price-section">
      <div class="section-header">
        <i class="el-icon-coin"></i>
        <span>费用明细</span>
      </div>
      <div class="price-summary">
        <div class="price-row">
          <span>商品总价</span>
          <span>¥{{ formatPrice(totalAmount) }}</span>
        </div>
        <div class="price-row">
          <span>运费</span>
          <span :class="{ free: shippingFee === 0 }">
            {{ shippingFee > 0 ? '¥' + formatPrice(shippingFee) : '免费' }}
          </span>
        </div>
        <div class="price-row coupon-row" @click="showCouponDialog = true">
          <span>
            <i class="el-icon-ticket"></i> 优惠券
            <span v-if="selectedCoupon" class="selected-coupon-info">({{ selectedCoupon.coupon_name }})</span>
          </span>
          <span class="coupon-discount">
            <span v-if="selectedCoupon" class="discount-amount">-¥{{ formatPrice(couponDiscount) }}</span>
            <span v-else class="no-coupon">暂无优惠券</span>
            <i class="el-icon-arrow-right"></i>
          </span>
        </div>
        <div class="price-row total">
          <span>实付金额</span>
          <span class="total-price">¥{{ formatPrice(finalAmount) }}</span>
        </div>
      </div>
    </div>

    <!-- 优惠券选择弹窗 -->
    <el-dialog
      :visible.sync="showCouponDialog"
      title="选择优惠券"
      width="500px"
      :append-to-body="true"
      class="coupon-select-dialog"
    >
      <div v-if="checkoutCoupons.length === 0" class="empty-coupons">
        <i class="el-icon-ticket"></i>
        <p>暂无可用优惠券</p>
      </div>
      <div v-else class="coupon-list">
        <div
          v-for="coupon in checkoutCoupons"
          :key="coupon.id"
          :class="['coupon-item', { 
            selected: selectedCoupon && selectedCoupon.id === coupon.id,
            disabled: !coupon.usable 
          }]"
          @click="coupon.usable && selectCoupon(coupon)"
        >
          <div class="coupon-amount">
            <span class="amount-value">¥{{ formatPrice(coupon.coupon_amount) }}</span>
            <span v-if="coupon.coupon_min_amount > 0" class="min-amount">满{{ coupon.coupon_min_amount }}元可用</span>
            <span v-else class="min-amount">无门槛</span>
          </div>
          <div class="coupon-info">
            <div class="coupon-name">{{ coupon.coupon_name }}</div>
            <div class="coupon-time">有效期至: {{ formatCouponTime(coupon.valid_end) }}</div>
            <div v-if="coupon.disableReason" class="disable-reason">{{ coupon.disableReason }}</div>
          </div>
          <div class="coupon-check">
            <i v-if="selectedCoupon && selectedCoupon.id === coupon.id" class="el-icon-check"></i>
            <i v-else-if="!coupon.usable" class="el-icon-close"></i>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="coupon-dialog-footer">
          <el-button @click="clearCoupon" size="small">不使用优惠券</el-button>
          <el-button type="primary" @click="confirmCoupon" size="small">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 支付方式 -->
    <div class="section payment-section">
      <div class="section-header">
        <i class="el-icon-wallet"></i>
        <span>支付方式</span>
      </div>
      <div class="payment-options">
        <div
          v-for="method in paymentMethods"
          :key="method.value"
          :class="['payment-option', { active: paymentMethod === method.value }]"
          @click="paymentMethod = method.value"
        >
          <i :class="method.icon"></i>
          <span>{{ method.label }}</span>
          <el-tag v-if="method.tag" type="success" size="mini">{{ method.tag }}</el-tag>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <div class="footer-info">
          <span class="footer-label">合计:</span>
          <span class="footer-price">¥{{ formatPrice(finalAmount) }}</span>
        </div>
        <div class="footer-actions">
          <el-button @click="handleClose" size="medium">取消</el-button>
          <el-button type="primary" :loading="submitting" size="medium" @click="handleSubmit" class="submit-btn">
            <i :class="paymentMethod === 'cash' ? 'el-icon-check' : 'el-icon-bank-card'"></i>
            {{ paymentMethod === 'cash' ? '确认下单' : '立即支付' }}
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { ref, reactive, computed, watch } from '@vue/composition-api'
import { Message } from 'element-ui'
import { createOrder, getAddressList, addAddress, updateAddress, deleteAddress, getCheckoutCoupons } from '@/api'
import { formatPrice, getFullImageUrl, validatePhone, notifyCartChanged } from '@/utils/common'

export default {
  name: 'CheckoutDialog',
  props: {
    visible: Boolean,
    selectedItems: { type: Array, default: () => [] }
  },
  setup(props, { emit }) {
    const dialogVisible = ref(false)
    const submitting = ref(false)
    const paymentMethod = ref('alipay')
    const showAddressForm = ref(false)
    const selectedAddressId = ref(null)
    const editingAddressId = ref(null)

    const paymentMethods = [
      { value: 'alipay', label: '支付宝', icon: 'el-icon-s-goods' },
      { value: 'wechat', label: '微信支付', icon: 'el-icon-chat-dot-round' },
      { value: 'card', label: '银行卡', icon: 'el-icon-s-order' },
      { value: 'cash', label: '货到付款', icon: 'el-icon-s-goods', tag: '先发货' }
    ]

    const addressForm = reactive({
      id: null,
      receiverName: '',
      phone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      isDefault: false
    })

    const addressRules = {
      receiverName: [
        { required: true, message: '请输入收货人', trigger: 'blur' },
        { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { validator: (rule, value, callback) => {
          if (!value) callback(new Error('请输入手机号'))
          else if (!/^1[3-9]\d{9}$/.test(value)) callback(new Error('手机号格式不正确'))
          else callback()
        }, trigger: 'blur' }
      ],
      province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
      city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
      district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
      detailAddress: [
        { required: true, message: '请输入详细地址', trigger: 'blur' },
        { min: 5, message: '详细地址不能少于5个字符', trigger: 'blur' }
      ]
    }

    const savedAddresses = ref([])

    const displayItems = computed(() => {
      return props.selectedItems.map((item, index) => ({
        ...item,
        key: item.id || item.productId || index
      }))
    })

    const totalAmount = computed(() => {
      return props.selectedItems.reduce((sum, item) => {
        return sum + ((item.price || 0) * (item.quantity || item.count || 1))
      }, 0)
    })

    const shippingFee = computed(() => totalAmount.value >= 99 ? 0 : 10)

    const discount = computed(() => 0)

    const showCouponDialog = ref(false)
    const selectedCoupon = ref(null)
    const checkoutCoupons = ref([])

    const couponDiscount = computed(() => {
      if (!selectedCoupon.value) return 0
      return selectedCoupon.value.coupon_amount || 0
    })

    const finalAmount = computed(() => {
      const amount = totalAmount.value + shippingFee.value - couponDiscount.value
      return amount > 0 ? amount : 0
    })

    const loadCheckoutCoupons = async () => {
      try {
        const res = await getCheckoutCoupons(totalAmount.value)
        if (res && res.data) {
          checkoutCoupons.value = Array.isArray(res.data) ? res.data : []
          // 自动选择最优惠的可用优惠券
          const usableCoupon = checkoutCoupons.value.find(c => c.usable)
          if (usableCoupon) {
            selectedCoupon.value = usableCoupon
          }
        }
      } catch (e) {
        console.error('加载优惠券失败:', e)
      }
    }

    const selectCoupon = (coupon) => {
      selectedCoupon.value = coupon
    }

    const clearCoupon = () => {
      selectedCoupon.value = null
    }

    const confirmCoupon = () => {
      showCouponDialog.value = false
    }

    const formatCouponTime = (time) => {
      if (!time) return ''
      const date = new Date(time)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    }

    const getImage = (item) => {
      const img = item.image || item.img || (item.product && item.product.image)
      return getFullImageUrl(img, null, item.id || item.productId)
    }

    const handleImageError = (e) => {
      e.target.src = '/imgs/foods/1.png'
    }

    const loadAddresses = async () => {
      try {
        const res = await getAddressList()
        if (res && res.data) {
          savedAddresses.value = Array.isArray(res.data) ? res.data : []
        } else if (res && Array.isArray(res)) {
          savedAddresses.value = res
        } else if (res && res.list) {
          savedAddresses.value = Array.isArray(res.list) ? res.list : []
        } else {
          savedAddresses.value = []
        }
        const defaultAddr = savedAddresses.value.find(addr => addr.isDefault)
        const firstAddr = savedAddresses.value[0]
        if (defaultAddr) selectAddress(defaultAddr)
        else if (firstAddr) selectAddress(firstAddr)
      } catch (e) {
        console.error('加载收货地址失败:', e)
        savedAddresses.value = []
      }
    }

    const selectAddress = (addr) => {
      selectedAddressId.value = addr.id
      Object.assign(addressForm, {
        id: addr.id,
        receiverName: addr.receiverName || addr.name || '',
        phone: addr.phone || '',
        province: addr.province || '',
        city: addr.city || '',
        district: addr.district || '',
        detailAddress: addr.detailAddress || addr.address || '',
        isDefault: addr.isDefault || false
      })
    }

    const editAddress = (addr) => {
      editingAddressId.value = addr.id
      showAddressForm.value = true
      selectAddress(addr)
    }

    const deleteAddressHandler = async (id) => {
      try {
        await deleteAddress(id)
        Message.success('地址已删除')
        await loadAddresses()
        if (selectedAddressId.value === id) selectedAddressId.value = null
      } catch (e) {
        Message.error('删除失败')
      }
    }

    const cancelAddressForm = () => {
      showAddressForm.value = false
      editingAddressId.value = null
      resetAddressForm()
      if (savedAddresses.value.length > 0) {
        const defaultAddr = savedAddresses.value.find(addr => addr.isDefault)
        if (defaultAddr) selectAddress(defaultAddr)
      }
    }

    const resetAddressForm = () => {
      Object.assign(addressForm, { id: null, receiverName: '', phone: '', province: '', city: '', district: '', detailAddress: '', isDefault: false })
    }

    const saveAddressForNextTime = async () => {
      try {
        if (editingAddressId.value) {
          await updateAddress(editingAddressId.value, { ...addressForm })
          Message.success('地址已更新')
        } else {
          await addAddress({ ...addressForm })
          Message.success('地址已保存')
        }
        await loadAddresses()
        cancelAddressForm()
      } catch (e) {
        Message.error('保存地址失败')
      }
    }

    watch(() => props.visible, (val) => {
      dialogVisible.value = val
      if (val) {
        loadAddresses().then(() => {
          if (savedAddresses.value.length === 0) showAddressForm.value = true
        })
        loadCheckoutCoupons()
        paymentMethod.value = 'alipay'
        selectedCoupon.value = null
      }
    })

    const validateAddress = () => {
      if (!addressForm.receiverName) { Message.warning('请输入收货人姓名'); return false }
      if (!addressForm.phone || !validatePhone(addressForm.phone)) { Message.warning('请输入正确的手机号码'); return false }
      if (!addressForm.province) { Message.warning('请输入省份'); return false }
      if (!addressForm.city) { Message.warning('请输入城市'); return false }
      if (!addressForm.district) { Message.warning('请输入区县'); return false }
      if (!addressForm.detailAddress || addressForm.detailAddress.length < 5) { Message.warning('请输入详细地址（至少5个字符）'); return false }
      return true
    }

    const handleSubmit = async () => {
      if (!validateAddress()) return
      if (props.selectedItems.length === 0) { Message.warning('请选择要结算的商品'); return }

      submitting.value = true
      try {
        const orderProducts = props.selectedItems.map(item => ({
          productId: item.productId || item.id,
          count: item.quantity || item.count || 1
        }))
        const shippingAddress = `${addressForm.province}${addressForm.city}${addressForm.district}${addressForm.detailAddress}`

        const orderData = {
          products: orderProducts,
          receiverName: addressForm.receiverName,
          phone: addressForm.phone,
          province: addressForm.province,
          city: addressForm.city,
          district: addressForm.district,
          detailAddress: addressForm.detailAddress,
          shippingAddress: shippingAddress,
          paymentMethod: paymentMethod.value
        }

        // 添加优惠券信息
        if (selectedCoupon.value) {
          orderData.couponId = selectedCoupon.value.id
          orderData.couponCode = selectedCoupon.value.coupon_code
          orderData.couponAmount = couponDiscount.value
        }

        const res = await createOrder(orderData)

        let orderId = res?.data?.id || res?.id || res?.data
        if (orderId) {
          Message.success('订单创建成功')
          notifyCartChanged()
          emit('success', { orderId })
          handleClose()
          setTimeout(() => { window.location.href = `/front/order/${orderId}` }, 300)
        } else {
          Message.success('订单创建成功')
          notifyCartChanged()
          emit('success', { orderId: null })
          handleClose()
          setTimeout(() => { window.location.href = '/front/my-orders' }, 300)
        }
      } catch (e) {
        console.error('创建订单失败:', e)
        Message.error(e.message || '创建订单失败，请重试')
      } finally {
        submitting.value = false
      }
    }

    const handleClose = () => emit('update:visible', false)

    const handleClosed = () => {
      resetAddressForm()
      showAddressForm.value = false
      selectedAddressId.value = null
      editingAddressId.value = null
    }

    return {
      dialogVisible, submitting, paymentMethod, paymentMethods,
      addressForm, addressRules, savedAddresses, selectedAddressId, showAddressForm,
      displayItems, totalAmount, shippingFee, discount,
      showCouponDialog, selectedCoupon, checkoutCoupons, couponDiscount, finalAmount,
      selectCoupon, clearCoupon, confirmCoupon, formatCouponTime,
      getImage, handleImageError, selectAddress, editAddress, deleteAddress: deleteAddressHandler,
      cancelAddressForm, saveAddressForNextTime, handleSubmit, handleClose, handleClosed, formatPrice
    }
  }
}
</script>

<style scoped>
/* 全局修复所有 el-dialog 黑色背景遮罩问题 */
::v-deep .el-dialog__wrapper {
  background-color: rgba(0, 0, 0, 0.5) !important;
}

::v-deep .el-dialog {
  background-color: #fff !important;
  border-radius: 12px !important;
  margin-top: 5vh !important;
  max-height: 90vh;
  overflow-y: auto;
}

::v-deep .el-dialog__header {
  background-color: #fff !important;
  border-bottom: 1px solid #eee;
  padding: 18px 20px;
}

::v-deep .el-dialog__body {
  padding: 0 !important;
  background-color: #fff !important;
}

::v-deep .el-dialog__footer {
  background-color: #fff !important;
  border-top: 1px solid #eee;
  padding: 16px 20px;
}

/* 通用 Section 样式 */
.section {
  margin-bottom: 20px;
  padding: 16px;
  background: #fafbfc;
  border-radius: 10px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.section-header i {
  color: #ff9800;
  font-size: 18px;
}

.item-count {
  margin-left: auto;
  font-size: 13px;
  font-weight: 400;
  color: #999;
}

/* 商品列表 */
.order-items {
  max-height: 180px;
  overflow-y: auto;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 8px;
  border: 1px solid #f0f0f0;
}

.order-item:last-child {
  margin-bottom: 0;
}

.item-image {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  object-fit: cover;
  background: #f5f5f5;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
}

.item-name {
  font-weight: 500;
  color: #333;
  font-size: 14px;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  color: #999;
  font-size: 13px;
}

.item-subtotal {
  font-weight: 600;
  color: #ff5722;
  font-size: 14px;
  flex-shrink: 0;
}

/* 地址列表 */
.saved-addresses {
  background: #fff;
  border-radius: 8px;
  padding: 8px;
  border: 1px solid #f0f0f0;
}

.saved-address-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border: 2px solid transparent;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.saved-address-item:hover {
  border-color: #ff9800;
  background: #fffbf5;
}

.saved-address-item.active {
  border-color: #ff9800;
  background: #fffbf5;
}

.saved-address-item:last-of-type {
  margin-bottom: 8px;
}

.address-radio {
  margin-right: 8px;
  padding-top: 2px;
}

.address-content {
  flex: 1;
}

.address-header {
  margin-bottom: 4px;
}

.receiver-name {
  font-weight: 600;
  color: #333;
  margin-right: 8px;
}

.receiver-phone {
  color: #666;
  font-size: 13px;
}

.address-detail {
  color: #666;
  font-size: 13px;
  line-height: 1.4;
}

.address-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.add-address-btn {
  width: 100%;
  margin-top: 4px;
}

/* 地址表单 */
.address-form-container {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #f0f0f0;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e8e8e8;
}

/* 费用明细 */
.price-section {
  background: #fff8f0;
  border: 1px solid #ffe4c4;
}

.price-summary {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.price-row span:last-child {
  color: #333;
  font-weight: 500;
}

.price-row span.free {
  color: #67c23a;
  font-weight: 600;
}

.price-row.total {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #ffe4c4;
  font-weight: 600;
  color: #333;
}

.price-row.coupon-row {
  cursor: pointer;
  padding: 8px 12px;
  margin: 0 -12px;
  border-radius: 6px;
  transition: background 0.2s;
}

.price-row.coupon-row:hover {
  background: #fff5e6;
}

.price-row.coupon-row i.el-icon-ticket {
  color: #ff6b35;
  margin-right: 4px;
}

.selected-coupon-info {
  color: #ff6b35;
  font-size: 12px;
  margin-left: 4px;
}

.coupon-discount {
  display: flex;
  align-items: center;
  gap: 4px;
}

.discount-amount {
  color: #ff5722 !important;
  font-weight: 600;
}

.no-coupon {
  color: #999;
  font-size: 13px;
}

.total-price {
  color: #ff5722 !important;
  font-size: 20px;
  font-weight: 700 !important;
}

/* 支付方式 */
.payment-options {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.payment-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.payment-option:hover {
  border-color: #ff9800;
  background: #fffbf5;
}

.payment-option.active {
  border-color: #ff9800;
  background: #fffbf5;
}

.payment-option i {
  font-size: 24px;
  color: #ff9800;
}

.payment-option span {
  font-size: 13px;
  color: #333;
  text-align: center;
}

/* 底部 */
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 8px 0;
}

.footer-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.footer-label {
  color: #666;
  font-size: 14px;
}

.footer-price {
  color: #ff5722;
  font-size: 26px;
  font-weight: 700;
}

.footer-actions {
  display: flex;
  gap: 12px;
}

.submit-btn {
  background: linear-gradient(135deg, #ff9800, #ff5722);
  border: none;
  min-width: 120px;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #ff5722, #ff9800);
}

/* 优惠券选择弹窗 */
::v-deep .coupon-select-dialog {
  background-color: #fff !important;
  border-radius: 12px !important;
}

::v-deep .coupon-select-dialog .el-dialog__body {
  padding: 16px !important;
  max-height: 60vh;
  overflow-y: auto;
}

.empty-coupons {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-coupons i {
  font-size: 48px;
  color: #ddd;
  margin-bottom: 12px;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coupon-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.coupon-item:hover:not(.disabled) {
  border-color: #ff9800;
  background: #fffbf5;
}

.coupon-item.selected {
  border-color: #ff6b35;
  background: #fff5e6;
}

.coupon-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f5f5f5;
}

.coupon-amount {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 100px;
  padding-right: 16px;
  border-right: 1px dashed #e8e8e8;
}

.amount-value {
  color: #ff5722;
  font-size: 24px;
  font-weight: 700;
}

.min-amount {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}

.coupon-info {
  flex: 1;
  margin-left: 16px;
}

.coupon-name {
  font-weight: 600;
  color: #333;
  font-size: 14px;
  margin-bottom: 4px;
}

.coupon-time {
  color: #999;
  font-size: 12px;
}

.disable-reason {
  color: #ff5722;
  font-size: 12px;
  margin-top: 4px;
}

.coupon-check {
  margin-left: 12px;
  font-size: 20px;
}

.coupon-check .el-icon-check {
  color: #ff6b35;
}

.coupon-check .el-icon-close {
  color: #ccc;
}

.coupon-dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 响应式 */
@media (max-width: 600px) {
  .payment-options {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
