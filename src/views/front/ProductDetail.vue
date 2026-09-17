/**
 * 商品详情页面
 * 文件路径: src/views/front/ProductDetail.vue
 * 功能描述: 展示商品详细信息，支持加入购物车、立即购买、收藏等操作
 */
<template>
  <div class="product-detail-page">
    <!-- 返回导航 -->
    <div class="back-navigation">
      <el-button @click="$router.back()" class="back-btn">
        <i class="el-icon-arrow-left"></i>
        返回
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 商品信息区域 -->
    <div v-else-if="product" class="product-container">
      <div class="product-header">
        <h1 class="product-title">{{ product.name }}</h1>
        <div class="product-meta">
          <span class="product-stats">
            <i class="el-icon-view"></i> {{ product.views || 0 }}次浏览
          </span>
          <span class="product-stats">
            <i class="el-icon-shopping-cart-2"></i> {{ product.sales || 0 }}人已购买
          </span>
          <span class="product-tags">
            <el-tag v-if="product.isHot" type="danger" size="small">热销</el-tag>
            <el-tag v-if="product.isNew" type="primary" size="small">新品</el-tag>
            <el-tag v-if="product.isRecommend" type="success" size="small">推荐</el-tag>
          </span>
        </div>
      </div>

      <!-- 商品图片与信息 -->
      <div class="product-content">
        <!-- 左侧图片区域 -->
        <div class="product-images">
          <div class="main-image">
            <img :src="currentImage" :alt="product.name" class="main-img" />
          </div>
          <div class="image-thumbs" v-if="productImages.length > 1">
            <div 
              v-for="(img, index) in productImages" 
              :key="index"
              class="thumb-item"
              :class="{ active: currentImageIndex === index }"
              @click="changeImage(index)"
            >
              <img :src="img" :alt="`${product.name}-图片${index + 1}`" class="thumb-img" />
            </div>
          </div>
        </div>

        <!-- 右侧信息区域 -->
        <div class="product-info">
          <!-- 价格信息 -->
          <div class="price-section">
            <div class="current-price">
              <span class="price-label">价格：</span>
              <span class="price-value">¥{{ formatPrice(product.price) }}</span>
            </div>
            <div class="original-price" v-if="product.originalPrice">
              <span class="original-label">原价：</span>
              <span class="original-value">¥{{ formatPrice(product.originalPrice) }}</span>
            </div>
          </div>

          <!-- 库存与数量 -->
          <div class="stock-section">
            <div class="stock-info">
              <span class="stock-label">库存：</span>
              <span class="stock-value" :class="{ 'low-stock': product.stock <= 10 }">
                {{ product.stock }}件
              </span>
              <span class="stock-tip" v-if="product.stock <= 10">(库存紧张)</span>
            </div>
            
            <div class="quantity-selector">
              <span class="quantity-label">数量：</span>
              <el-input-number
                v-model="count"
                :min="1"
                :max="product.stock"
                :disabled="product.stock === 0"
                controls-position="right"
                size="medium"
                class="quantity-input"
              />
              <span class="stock-hint" v-if="product.stock > 0">
                剩余{{ product.stock }}件
              </span>
              <span class="sold-out" v-else>已售罄</span>
            </div>
          </div>

          <!-- 服务保障 -->
          <div class="service-section">
            <div class="service-title">服务保障：</div>
            <div class="service-list">
              <div class="service-item">
                <i class="el-icon-check"></i>
                <span>正品保证</span>
              </div>
              <div class="service-item">
                <i class="el-icon-check"></i>
                <span>七天退换</span>
              </div>
              <div class="service-item">
                <i class="el-icon-check"></i>
                <span>极速发货</span>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button 
              type="warning" 
              size="large" 
              @click="handleAddToCart"
              :disabled="product.stock === 0"
              :loading="addingToCart"
              class="cart-btn"
            >
              <i class="el-icon-shopping-cart-2"></i>
              {{ product.stock === 0 ? '已售罄' : '加入购物车' }}
            </el-button>
            
            <el-button 
              type="danger" 
              size="large" 
              @click="handleBuyNow"
              :disabled="product.stock === 0"
              :loading="buyingNow"
              class="buy-btn"
            >
              <i class="el-icon-shopping-cart"></i>
              立即购买
            </el-button>
            
            <el-button 
              plain 
              size="large" 
              @click="handleToggleFavorite"
              class="favorite-btn"
            >
              <i :class="isFavorite ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
              {{ isFavorite ? '已收藏' : '收藏商品' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 商品详情标签页 -->
      <div class="product-tabs">
        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="商品详情" name="detail">
            <div class="tab-content detail-content">
              <div class="description-section">
                <h3 class="section-title">商品描述</h3>
                <div class="description-text" v-html="product.description || product.desc || '暂无描述'"></div>
              </div>

              <!-- 商品参数 -->
              <div class="params-section" v-if="hasProductParams">
                <h3 class="section-title">商品参数</h3>
                <div class="params-grid">
                  <div class="param-item" v-if="product.origin">
                    <span class="param-name">产地：</span>
                    <span class="param-value">{{ product.origin }}</span>
                  </div>
                  <div class="param-item" v-if="product.brand">
                    <span class="param-name">品牌：</span>
                    <span class="param-value">{{ product.brand }}</span>
                  </div>
                  <div class="param-item" v-if="product.unit">
                    <span class="param-name">单位：</span>
                    <span class="param-value">{{ product.unit }}</span>
                  </div>
                  <div class="param-item" v-if="product.weight">
                    <span class="param-name">重量：</span>
                    <span class="param-value">{{ product.weight }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="商品评价" name="reviews">
            <div class="tab-content">
              <ProductReviews :productId="product.id" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 商品推荐 -->
      <div class="recommendations-section">
        <h3 class="section-title">猜你喜欢</h3>
        <ProductRecommendations :currentProductId="product.id" />
      </div>
    </div>

    <!-- 商品不存在 -->
    <div v-else class="not-found">
      <div class="not-found-icon">
        <i class="el-icon-goods"></i>
      </div>
      <h2 class="not-found-title">未找到商品</h2>
      <p class="not-found-description">抱歉，没有找到对应的商品信息，可能商品已下架或不存在</p>
      <div class="not-found-actions">
        <el-button type="warning" @click="$router.back()">
          <i class="el-icon-arrow-left"></i> 返回上一页
        </el-button>
        <el-button @click="$router.push('/front/products')">
          <i class="el-icon-shopping-cart-2"></i> 继续逛逛
        </el-button>
      </div>
    </div>

    <!-- 快速结算对话框 -->
    <el-dialog
      :visible.sync="quickCheckoutVisible"
      title="确认订单"
      width="600px"
      :close-on-click-modal="false"
      :append-to-body="true"
      class="quick-checkout-dialog"
    >
      <!-- 收货地址选择 -->
      <div class="checkout-section">
        <div class="section-title">
          <i class="el-icon-location-information"></i>
          收货信息
        </div>

        <!-- 已保存的地址 -->
        <div v-if="savedAddresses.length > 0 && !showAddressForm" class="saved-addresses">
          <div
            v-for="addr in savedAddresses"
            :key="addr.id"
            :class="['address-card', { active: selectedAddressId === addr.id }]"
            @click="selectAddress(addr)"
          >
            <div class="address-radio">
              <el-radio v-model="selectedAddressId" :label="addr.id">&nbsp;</el-radio>
            </div>
            <div class="address-info">
              <div class="address-header">
                <span class="receiver">{{ addr.receiverName || addr.name }}</span>
                <span class="phone">{{ addr.phone }}</span>
                <el-tag v-if="addr.isDefault" type="success" size="mini">默认</el-tag>
              </div>
              <div class="address-detail">
                {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress || addr.address }}
              </div>
            </div>
          </div>
          <el-button type="primary" size="small" plain @click="showAddressForm = true" class="add-new-btn">
            <i class="el-icon-plus"></i> 使用新地址
          </el-button>
        </div>

        <!-- 新地址表单 -->
        <div v-if="showAddressForm || savedAddresses.length === 0" class="address-form">
          <el-form :model="addressForm" :rules="addressRules" label-width="80px" size="small">
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
              <el-input v-model="addressForm.detailAddress" placeholder="请输入详细地址" />
            </el-form-item>
          </el-form>
          <div v-if="savedAddresses.length > 0" class="form-btns">
            <el-button size="small" @click="cancelAddressForm">返回</el-button>
          </div>
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="checkout-section">
        <div class="section-title">
          <i class="el-icon-shopping-bag-2"></i>
          商品清单
        </div>
        <div class="checkout-product">
          <img :src="getProductImage(product)" class="checkout-product-img" />
          <div class="checkout-product-info">
            <div class="checkout-product-name">{{ product && product.name }}</div>
            <div class="checkout-product-price">¥{{ formatPrice((product && product.price) || 0) }} × {{ count }}</div>
          </div>
          <div class="checkout-subtotal">
            <span class="subtotal-label">小计</span>
            <span class="subtotal-value">¥{{ formatPrice(((product && product.price) || 0) * count) }}</span>
          </div>
        </div>
      </div>

      <!-- 费用汇总 -->
      <div class="checkout-section fee-section">
        <div class="fee-row">
          <span>商品总价</span>
          <span>¥{{ formatPrice(((product && product.price) || 0) * count) }}</span>
        </div>
        <div class="fee-row">
          <span>运费</span>
          <span :class="{ free: shippingFee === 0 }">{{ shippingFee > 0 ? '¥' + formatPrice(shippingFee) : '免费' }}</span>
        </div>
        <div class="fee-row coupon-row" @click="showCouponDialog = true">
          <span>
            <i class="el-icon-ticket"></i> 优惠券
            <span v-if="selectedCoupon" class="selected-coupon-info">(已选)</span>
          </span>
          <span v-if="selectedCoupon" class="coupon-discount">
            <span class="discount-amount">-¥{{ formatPrice(couponDiscount) }}</span>
          </span>
          <span v-else class="no-coupon">点击选择优惠券</span>
        </div>
        <div class="fee-row total">
          <span>实付金额</span>
          <span class="total-price">¥{{ formatPrice(finalAmount) }}</span>
        </div>
      </div>

      <!-- 优惠券选择弹窗（嵌套） -->
      <el-dialog
        :visible.sync="showCouponDialog"
        title="选择优惠券"
        width="480px"
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

      <template #footer>
        <div class="dialog-footer">
          <div class="footer-amount">
            <span class="amount-label">合计:</span>
            <span class="amount-value">¥{{ formatPrice(finalAmount) }}</span>
          </div>
          <div class="footer-btns">
            <el-button @click="quickCheckoutVisible = false">取消</el-button>
            <el-button type="primary" :loading="payingNow" @click="confirmBuyNow" class="pay-btn">
              <i class="el-icon-bank-card"></i> 确认支付
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import ProductReviews from '@/components/ProductReviews'
import ProductRecommendations from '@/components/ProductRecommendations'
import { addToCart, getUserFavorites, addFavorite, removeFavorite, getProductById, createOrder, payOrder, getAddressList, addAddress, getCheckoutCoupons } from '@/api'
import { formatPrice, getFullImageUrl, validatePhone } from '@/utils/common'

export default {
  name: 'ProductDetail',
  components: { ProductReviews, ProductRecommendations },
  data() {
    return {
      product: null,
      loading: true,
      count: 1,
      addingToCart: false,
      buyingNow: false,
      payingNow: false,
      isFavorite: false,
      activeTab: 'detail',
      currentImageIndex: 0,
      quickCheckoutVisible: false,
      // 优惠券相关
      showCouponDialog: false,
      selectedCoupon: null,
      checkoutCoupons: [],
      // 地址相关
      savedAddresses: [],
      selectedAddressId: null,
      showAddressForm: false,
      addressForm: {
        receiverName: '',
        phone: '',
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: false
      },
      addressRules: {
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
    }
  },
  computed: {
    productImages() {
      if (!this.product) return []

      if (this.product.images && Array.isArray(this.product.images) && this.product.images.length > 0) {
        return this.product.images.map(img => getFullImageUrl(img))
      }

      const mainImg = this.product.image || this.product.img || ''
      const defaultImg = mainImg ? getFullImageUrl(mainImg) : `/imgs/foods/${(Number(this.product.id) % 11) + 1}.png`
      return [defaultImg]
    },

    currentImage() {
      return this.productImages[this.currentImageIndex] || '/imgs/foods/1.png'
    },

    hasProductParams() {
      return this.product && (this.product.origin || this.product.brand || this.product.unit || this.product.weight)
    },

    shippingFee() {
      const total = ((this.product && this.product.price) || 0) * this.count
      return total >= 99 ? 0 : 10
    },
    couponDiscount() {
      if (!this.selectedCoupon) return 0
      return Number(this.selectedCoupon.coupon_amount) || 0
    },
    finalAmount() {
      const total = ((this.product && this.product.price) || 0) * this.count
      const amount = total + this.shippingFee - this.couponDiscount
      return amount > 0 ? amount : 0
    }
  },
  created() {
    const id = parseInt(this.$route.params.id, 10)
    if (isNaN(id)) {
      this.$message.error('商品ID无效')
      this.$router.back()
      return
    }
    this.loadProduct(id)
  },
  watch: {
    '$route.params.id'(newId) {
      const id = parseInt(newId, 10)
      if (!isNaN(id)) {
        this.count = 1
        this.currentImageIndex = 0
        this.activeTab = 'detail'
        this.loadProduct(id)
      }
    }
  },
  mounted() {
    window.addEventListener('xm-favorites-changed', this.loadFavoriteStatus)
  },
  beforeDestroy() {
    window.removeEventListener('xm-favorites-changed', this.loadFavoriteStatus)
  },
  methods: {
    formatPrice,
    
    getProductImage(product) {
      if (!product) return '/imgs/foods/1.png'
      const img = product.image || product.img || ''
      return img ? getFullImageUrl(img) : `/imgs/foods/${(Number(product.id) % 11) + 1}.png`
    },
    
    async loadProduct(id) {
      this.loading = true
      try {
        const res = await getProductById(id)
        const foundProduct = (res && res.data) ? res.data : res

        if (foundProduct) {
          this.product = foundProduct
          this.loadFavoriteStatus()
        } else {
          this.product = null
        }
      } catch (error) {
        console.error('加载商品失败:', error)
        this.product = null
        this.$message.error('加载商品失败')
      } finally {
        this.loading = false
      }
    },
    
    async loadFavoriteStatus() {
      if (!this.product) return
      const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
      if (!user.id) return
      try {
        const res = await getUserFavorites()
        const favorites = (res && res.data) ? res.data : (Array.isArray(res) ? res : [])
        this.isFavorite = favorites.some(f => {
          const targetId = f.targetId || f.productId
          return Number(targetId) === Number(this.product.id)
        })
      } catch {
        this.isFavorite = false
      }
    },
    
    changeImage(index) {
      this.currentImageIndex = index
    },
    
    checkLogin() {
      const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
      if (!user.id) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return false
      }
      return true
    },
    
    async handleAddToCart() {
      if (!this.product || this.product.stock <= 0) {
        this.$message.warning('该商品暂无库存')
        return
      }

      if (!this.checkLogin()) return

      this.addingToCart = true
      try {
        const res = await addToCart({ 
          productId: this.product.id, 
          count: this.count 
        })
        this.$message.success('商品已成功加入购物车')
        window.dispatchEvent(new Event('xm-cart-changed'))
      } catch (err) {
        console.error('加入购物车失败:', err)
        this.$message.error(err && err.message ? err.message : '加入购物车失败，请重试')
      } finally {
        this.addingToCart = false
      }
    },

    async handleBuyNow() {
      if (!this.product || this.product.stock === 0) {
        this.$message.warning('该商品暂无库存')
        return
      }

      if (!this.checkLogin()) return

      this.quickCheckoutVisible = true
      this.loadAddresses()
      this.loadCheckoutCoupons()
    },

    // 加载结算可用优惠券
    async loadCheckoutCoupons() {
      try {
        const total = ((this.product && this.product.price) || 0) * this.count
        const res = await getCheckoutCoupons(total)
        if (res && res.data) {
          this.checkoutCoupons = Array.isArray(res.data) ? res.data : []
          // 自动选择折扣最大的可用优惠券
          const usableCoupons = this.checkoutCoupons.filter(c => c.usable)
          if (usableCoupons.length > 0) {
            usableCoupons.sort((a, b) => (Number(b.coupon_amount) || 0) - (Number(a.coupon_amount) || 0))
            this.selectedCoupon = usableCoupons[0]
          }
        }
      } catch (e) {
        console.error('加载优惠券失败:', e)
      }
    },

    selectCoupon(coupon) {
      this.selectedCoupon = coupon
    },

    clearCoupon() {
      this.selectedCoupon = null
    },

    confirmCoupon() {
      this.showCouponDialog = false
    },

    formatCouponTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    },

    // 加载地址列表
    async loadAddresses() {
      try {
        const res = await getAddressList()
        if (res && res.data) {
          this.savedAddresses = Array.isArray(res.data) ? res.data : []
        } else if (res && Array.isArray(res)) {
          this.savedAddresses = res
        } else {
          this.savedAddresses = []
        }
        // 自动选择默认地址或第一个
        const defaultAddr = this.savedAddresses.find(addr => addr.isDefault)
        const firstAddr = this.savedAddresses[0]
        if (defaultAddr) {
          this.selectAddress(defaultAddr)
        } else if (firstAddr) {
          this.selectAddress(firstAddr)
        }
      } catch (e) {
        console.error('加载地址失败:', e)
        this.savedAddresses = []
      }
    },

    // 选择地址
    selectAddress(addr) {
      this.selectedAddressId = addr.id
      this.addressForm = {
        receiverName: addr.receiverName || addr.name || '',
        phone: addr.phone || '',
        province: addr.province || '',
        city: addr.city || '',
        district: addr.district || '',
        detailAddress: addr.detailAddress || addr.address || '',
        isDefault: addr.isDefault || false
      }
    },

    // 取消地址表单
    cancelAddressForm() {
      this.showAddressForm = false
      this.resetAddressForm()
      if (this.savedAddresses.length > 0) {
        const defaultAddr = this.savedAddresses.find(addr => addr.isDefault)
        if (defaultAddr) this.selectAddress(defaultAddr)
      }
    },

    // 重置地址表单
    resetAddressForm() {
      this.addressForm = {
        receiverName: '',
        phone: '',
        province: '',
        city: '',
        district: '',
        detailAddress: '',
        isDefault: false
      }
      this.selectedAddressId = null
    },

    // 验证地址
    validateAddress() {
      if (!this.addressForm.receiverName) {
        this.$message.warning('请输入收货人姓名')
        return false
      }
      if (!this.addressForm.phone || !validatePhone(this.addressForm.phone)) {
        this.$message.warning('请输入正确的手机号码')
        return false
      }
      if (!this.addressForm.province) {
        this.$message.warning('请输入省份')
        return false
      }
      if (!this.addressForm.city) {
        this.$message.warning('请输入城市')
        return false
      }
      if (!this.addressForm.district) {
        this.$message.warning('请输入区县')
        return false
      }
      if (!this.addressForm.detailAddress || this.addressForm.detailAddress.length < 5) {
        this.$message.warning('请输入详细地址（至少5个字符）')
        return false
      }
      return true
    },

    async confirmBuyNow() {
      if (!this.validateAddress()) return

      this.payingNow = true
      try {
        const shippingAddress = `${this.addressForm.province}${this.addressForm.city}${this.addressForm.district}${this.addressForm.detailAddress}`

        const orderData = {
          products: [{
            productId: this.product.id,
            count: this.count
          }],
          receiverName: this.addressForm.receiverName,
          phone: this.addressForm.phone,
          province: this.addressForm.province,
          city: this.addressForm.city,
          district: this.addressForm.district,
          detailAddress: this.addressForm.detailAddress,
          shippingAddress: shippingAddress
        }

        if (this.selectedCoupon) {
          orderData.couponId = this.selectedCoupon.id
          orderData.couponCode = this.selectedCoupon.coupon_code
          orderData.couponAmount = this.couponDiscount
        }

        // 创建订单
        const orderRes = await createOrder(orderData)

        // 获取订单ID
        const resData = orderRes && orderRes.data ? orderRes.data : orderRes
        const orderId = resData && resData.id

        if (!orderId) {
          throw new Error('创建订单失败')
        }

        // 支付订单（从余额扣款）
        await payOrder(orderId, 'balance')

        this.$message.success('购买成功！')
        this.quickCheckoutVisible = false

        // 跳转到订单详情
        setTimeout(() => {
          this.$router.push(`/front/order/${orderId}`)
        }, 500)
      } catch (err) {
        console.error('购买失败:', err)
        this.$message.error(err && err.message ? err.message : '购买失败，余额可能不足')
      } finally {
        this.payingNow = false
      }
    },
    
    async handleToggleFavorite() {
      if (!this.product) return
      
      if (!this.checkLogin()) return
      
      try {
        if (this.isFavorite) {
          await removeFavorite(this.product.id, true)
          this.isFavorite = false
          this.$message.success('已取消收藏')
        } else {
          await addFavorite({ productId: this.product.id })
          this.isFavorite = true
          this.$message.success('收藏成功')
        }
        window.dispatchEvent(new Event('xm-favorites-changed'))
      } catch (err) {
        console.error('操作失败:', err)
        this.$message.error(err && err.message ? err.message : '操作失败')
      }
    }
  }
}
</script>

<style scoped>
/* 修复对话框黑色背景遮罩 */
::v-deep .el-dialog__wrapper {
  background-color: rgba(0, 0, 0, 0.5) !important;
}

::v-deep .quick-checkout-dialog .el-dialog {
  background-color: #fff !important;
  border-radius: 12px !important;
  margin-top: 5vh !important;
}

::v-deep .quick-checkout-dialog .el-dialog__header {
  background-color: #fff !important;
  border-bottom: 1px solid #eee;
  padding: 18px 20px;
}

::v-deep .quick-checkout-dialog .el-dialog__body {
  padding: 0 !important;
  background-color: #fff !important;
}

::v-deep .quick-checkout-dialog .el-dialog__footer {
  background-color: #fff !important;
  border-top: 1px solid #eee;
  padding: 16px 20px;
}

.product-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
  padding: 20px;
  position: relative;
}

/* 返回导航 */
.back-navigation {
  margin-bottom: 20px;
}

.back-btn {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  color: #666;
  font-weight: 500;
  padding: 10px 16px;
  transition: all 0.3s ease;
}

.back-btn:hover {
  border-color: #ff9800;
  color: #ff9800;
  transform: translateX(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.back-btn i {
  margin-right: 8px;
  font-weight: bold;
}

/* 商品详情卡片 */
.product-container {
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 30px;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 商品头部 */
.product-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.product-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.product-stats {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #666;
  font-size: 14px;
}

.product-tags {
  display: flex;
  gap: 8px;
}

/* 商品内容区域 */
.product-content {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

/* 图片区域 */
.product-images {
  flex: 1;
  min-width: 400px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.main-image {
  position: relative;
  background: #f8f8f8;
  border-radius: 8px;
  overflow: hidden;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.main-img {
  max-width: 100%;
  max-height: 400px;
  border-radius: 4px;
}

.image-thumbs {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.thumb-item {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.thumb-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.thumb-item.active {
  border-color: #ff9800;
  box-shadow: 0 0 0 2px rgba(255, 152, 0, 0.2);
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 信息区域 */
.product-info {
  flex: 1;
  min-width: 320px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 价格区域 */
.price-section {
  background: #f8f8f8;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 10px;
}

.current-price {
  margin-bottom: 8px;
}

.price-label {
  font-size: 16px;
  color: #666;
  margin-right: 8px;
}

.price-value {
  font-size: 32px;
  font-weight: 700;
  color: #ff5722;
}

.original-price {
  margin-bottom: 8px;
}

.original-label {
  font-size: 14px;
  color: #999;
  margin-right: 8px;
}

.original-value {
  font-size: 16px;
  color: #999;
  text-decoration: line-through;
}

.discount-badge {
  display: inline-block;
  background: #ff5722;
  color: white;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* 规格区域 */
.spec-section {
  margin-bottom: 10px;
}

.spec-item {
  margin-bottom: 12px;
}

.spec-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.spec-options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.spec-option {
  margin-right: 8px;
  margin-bottom: 8px;
}

/* 库存区域 */
.stock-section {
  margin-bottom: 10px;
}

.stock-info {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stock-label {
  font-size: 16px;
  color: #333;
}

.stock-value {
  font-size: 16px;
  font-weight: 500;
  color: #4caf50;
}

.stock-value.low-stock {
  color: #ff9800;
}

.stock-tip {
  font-size: 14px;
  color: #ff9800;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.quantity-label {
  font-size: 16px;
  color: #333;
  white-space: nowrap;
}

.quantity-input {
  width: 120px;
}

.stock-hint {
  font-size: 14px;
  color: #666;
}

.sold-out {
  font-size: 14px;
  color: #f44336;
  font-weight: 500;
}

/* 服务保障 */
.service-section {
  margin-bottom: 10px;
}

.service-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
}

.service-list {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
}

.service-item i {
  color: #4caf50;
  font-size: 16px;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.cart-btn,
.buy-btn,
.favorite-btn {
  flex: 1;
  min-width: 120px;
}

.cart-btn {
  background: #ff9800;
  border-color: #ff9800;
}

.cart-btn:hover {
  background: #f57c00;
  border-color: #f57c00;
}

.buy-btn {
  background: #ff5722;
  border-color: #ff5722;
}

.buy-btn:hover {
  background: #e64a19;
  border-color: #e64a19;
}

.favorite-btn {
  border-color: #ff9800;
  color: #ff9800;
}

.favorite-btn:hover {
  background: rgba(255, 152, 0, 0.1);
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  gap: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

.share-btn,
.compare-btn,
.service-btn {
  color: #666;
  font-size: 14px;
}

.share-btn:hover,
.compare-btn:hover,
.service-btn:hover {
  color: #ff9800;
}

/* 标签页 */
.product-tabs {
  margin-bottom: 40px;
  border-top: 1px solid #f0f0f0;
  padding-top: 30px;
}

.detail-tabs .el-tabs__header {
  margin-bottom: 30px;
}

.detail-tabs .el-tabs__item {
  font-size: 16px;
  font-weight: 500;
  padding: 0 24px;
}

.detail-tabs .el-tabs__active-bar {
  height: 3px;
  background: #ff9800;
}

.tab-content {
  padding: 0 20px;
}

/* 详情内容 */
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.description-text {
  line-height: 1.8;
  color: #666;
  font-size: 16px;
  white-space: pre-wrap;
}

.params-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.param-item {
  display: flex;
  padding: 12px;
  background: #f8f8f8;
  border-radius: 6px;
}

.param-name {
  font-weight: 500;
  color: #333;
  margin-right: 12px;
  min-width: 80px;
}

.param-value {
  color: #666;
  flex: 1;
}

.detail-images {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 30px;
}

.detail-image-item {
  text-align: center;
}

.detail-img {
  max-width: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 服务内容 */
.service-content {
  line-height: 1.8;
}

.service-policy {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.policy-item h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.policy-item p {
  color: #666;
  margin: 0;
}

/* 推荐区域 */
.recommendations-section {
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid #f0f0f0;
}

/* 未找到商品 */
.not-found {
  max-width: 600px;
  margin: 100px auto;
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.not-found-icon {
  margin-bottom: 24px;
}

.not-found-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

.not-found-description {
  color: #666;
  margin: 0 0 32px 0;
  font-size: 16px;
}

.not-found-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-detail-page {
    padding: 10px;
  }
  
  .product-container {
    padding: 20px;
  }
  
  .product-content {
    flex-direction: column;
    gap: 30px;
  }
  
  .product-images {
    min-width: 100%;
  }
  
  .product-info {
    min-width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .cart-btn,
  .buy-btn,
  .favorite-btn {
    width: 100%;
  }
  
  .quick-actions {
    justify-content: center;
  }
  
  .params-grid {
    grid-template-columns: 1fr;
  }
}

/* 加载状态 */
.loading-state {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px;
  background: white;
  border-radius: 12px;
}

/* 快速结算对话框 */
.quick-checkout-content {
  padding: 10px 0;
}

.checkout-section {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.checkout-section:last-child {
  border-bottom: none;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.section-title i {
  color: #ff9800;
}

/* 已保存的地址 */
.saved-addresses {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-card {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.address-card:hover {
  border-color: #ff9800;
  background: #fffbf5;
}

.address-card.active {
  border-color: #ff9800;
  background: #fff8f0;
}

.address-radio {
  margin-right: 8px;
  padding-top: 2px;
}

.address-info {
  flex: 1;
}

.address-header {
  margin-bottom: 4px;
}

.address-header .receiver {
  font-weight: 600;
  color: #333;
  margin-right: 8px;
}

.address-header .phone {
  color: #666;
  font-size: 13px;
}

.address-detail {
  color: #666;
  font-size: 13px;
  line-height: 1.4;
}

.add-new-btn {
  margin-top: 8px;
  width: 100%;
}

/* 地址表单 */
.address-form {
  background: #fafbfc;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #f0f0f0;
}

.form-btns {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e8e8e8;
}

/* 商品信息 */
.checkout-product {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
}

.checkout-product-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f5f5;
}

.checkout-product-info {
  flex: 1;
  min-width: 0;
}

.checkout-product-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.checkout-product-price {
  color: #999;
  font-size: 13px;
}

.checkout-subtotal {
  text-align: right;
}

.subtotal-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.subtotal-value {
  font-weight: 600;
  color: #ff5722;
  font-size: 16px;
}

/* 费用汇总 */
.fee-section {
  background: #fff8f0;
}

.fee-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: #666;
}

.fee-row span:last-child {
  color: #333;
  font-weight: 500;
}

.fee-row span.free {
  color: #67c23a;
  font-weight: 600;
}

.fee-row.total {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #ffe4c4;
  font-weight: 600;
  color: #333;
}

.fee-row.total span:last-child {
  font-size: 20px;
  color: #ff5722;
}

.fee-row.coupon-row {
  cursor: pointer;
  padding: 8px 12px;
  margin: 0 -12px;
  border-radius: 6px;
  transition: background 0.2s;
}

.fee-row.coupon-row:hover {
  background: #fff5e6;
}

.fee-row.coupon-row i.el-icon-ticket {
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

/* 优惠券选择弹窗 */
::v-deep .coupon-select-dialog .el-dialog {
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

/* 底部 */
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.footer-amount {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.amount-label {
  color: #666;
  font-size: 14px;
}

.amount-value {
  color: #ff5722;
  font-size: 26px;
  font-weight: 700;
}

.footer-btns {
  display: flex;
  gap: 12px;
}

.pay-btn {
  background: linear-gradient(135deg, #ff9800, #ff5722) !important;
  border: none !important;
  min-width: 120px;
}

.pay-btn:hover {
  background: linear-gradient(135deg, #ff5722, #ff9800) !important;
}

.checkout-product-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.checkout-product-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f5f5;
}

.checkout-product-info {
  flex: 1;
}

.checkout-product-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.checkout-product-price {
  color: #ff5722;
  font-weight: 600;
}

.checkout-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  padding: 10px 0;
}

.checkout-total .total-price {
  font-size: 24px;
  font-weight: bold;
  color: #ff5722;
}

/* 未找到商品图标 */
.not-found-icon {
  font-size: 80px;
  color: #e0e0e0;
}
</style>