/**
 * 订单详情页面
 * 文件路径: src/views/front/OrderDetail.vue
 * 功能描述: 展示单个订单完整信息，订单状态栏（状态图标、状态描述、下单时间），收货信息（收货人、电话、地址），
 *           商品清单（图片、名称、单价×数量、小计），订单金额明细（商品总额、运费、优惠券抵扣、实付金额），
 *           订单信息（订单号、支付方式、下单/支付/发货/完成时间），支持取消订单、确认收货、查看物流、去支付操作
 * 关联文件:
 * - src/api/index.js: 提供订单详情和操作接口
 * - src/views/front/MyOrders.vue: 我的订单列表页面
 * - src/views/front/components/LogisticsDialog.vue: 物流跟踪弹窗
 */
<template>
  <div class="order-detail-page">
    <div v-if="order" class="order-content">
      <div class="order-status-bar">
        <div class="status-info">
          <div class="status-icon" :class="order.status">
            <i :class="getStatusIcon(order.status)"></i>
          </div>
          <div class="status-text">
            <h2>{{ getStatusText(order.status) }}</h2>
            <p>{{ getStatusDesc(order.status) }}</p>
          </div>
        </div>
        <div class="order-time">
          <span>下单时间：{{ formatDateTime(order.createTime) }}</span>
        </div>
      </div>

      <div class="info-card">
        <div class="card-header">
          <i class="el-icon-location"></i>
          <h3>收货信息</h3>
        </div>
        <div class="card-body">
          <div class="info-row">
            <span class="label">收货人：</span>
            <span class="value">{{ order.receiver || order.receiverName || '未填写' }}</span>
          </div>
          <div class="info-row">
            <span class="label">联系电话：</span>
            <span class="value">{{ order.phone || '未填写' }}</span>
          </div>
          <div class="info-row">
            <span class="label">收货地址：</span>
            <span class="value">{{ order.address || order.shippingAddress || '未填写' }}</span>
          </div>
        </div>
      </div>

      <div v-if="order.trackingNumber" class="info-card logistics-card">
        <div class="card-header">
          <i class="el-icon-truck"></i>
          <h3>物流信息</h3>
          <el-button type="primary" size="mini" plain @click="viewLogistics">查看物流详情</el-button>
        </div>
        <div class="card-body">
          <div class="info-row">
            <span class="label">快递公司：</span>
            <span class="value express-company">{{ order.shippingCompany || order.expressCompany || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="label">运单号：</span>
            <span class="value tracking-row">
              <code class="tracking-code">{{ order.trackingNumber }}</code>
              <el-button type="text" size="small" @click="copyTracking">复制</el-button>
            </span>
          </div>
        </div>
      </div>

      <div class="info-card">
        <div class="card-header">
          <i class="el-icon-goods"></i>
          <h3>商品信息</h3>
        </div>
        <div class="card-body">
          <div v-if="orderProducts.length > 0">
            <div v-for="product in orderProducts" :key="product.id" class="product-item">
              <img :src="getImage(product)" class="product-image" @error="handleImageError" />
              <div class="product-info">
                <h4 class="product-name" @click="goProduct(product.productId || product.id)">{{ product.name || product.productName || '未知商品' }}</h4>
                <p class="product-desc">{{ product.description || product.spec || '' }}</p>
              </div>
              <div class="product-price">
                <span class="unit-price">¥{{ formatPrice(product.price || product.unitPrice || 0) }}</span>
                <span class="quantity">×{{ product.quantity || product.count || 1 }}</span>
                <span class="subtotal">¥{{ formatPrice((product.price || product.unitPrice || 0) * (product.quantity || product.count || 1)) }}</span>
              </div>
            </div>
          </div>
          <div v-else class="no-products">
            <el-empty description="暂无商品信息" :image-size="60"></el-empty>
          </div>
        </div>
      </div>

      <div class="info-card">
        <div class="card-header">
          <i class="el-icon-wallet"></i>
          <h3>支付信息</h3>
        </div>
        <div class="card-body">
          <div class="info-row">
            <span class="label">支付方式：</span>
            <span class="value">{{ getPaymentText(order.paymentMethod) }}</span>
          </div>
          <div v-if="isProcessing && shippingDeadlineText" class="info-row shipping-deadline">
            <span class="label">预计发货：</span>
            <span class="value deadline-text">
              <i class="el-icon-time"></i>
              {{ shippingDeadlineText }}
            </span>
          </div>
          <div class="info-row">
            <span class="label">商品总额：</span>
            <span class="value">¥{{ formatPrice(orderTotal) }}</span>
          </div>
          <div class="info-row">
            <span class="label">运费：</span>
            <span class="value">¥{{ formatPrice(order.shippingFee || 0) }}</span>
          </div>
          <div v-if="order && order.couponAmount && order.couponAmount > 0" class="info-row">
            <span class="label">优惠券：</span>
            <span class="value discount">-¥{{ formatPrice(order.couponAmount) }}</span>
          </div>
          <div class="info-row total">
            <span class="label">实付金额：</span>
            <span class="value final-price">¥{{ formatPrice(actualPaidAmount) }}</span>
          </div>
        </div>
      </div>

      <div class="action-bar">
        <el-button @click="$router.back()">返回</el-button>
        <template v-if="isPending">
          <template v-if="!isMerchant">
            <el-button type="primary" @click="handlePay">立即支付</el-button>
            <el-button type="danger" plain @click="handleCancel">取消订单</el-button>
          </template>
          <el-tag v-else type="info">等待买家付款</el-tag>
        </template>
        <template v-else-if="isProcessing">
          <template v-if="isMerchant">
            <el-button type="primary" @click="handleShip">发货</el-button>
          </template>
          <template v-else>
            <el-button type="danger" plain @click="showRefundDialog">申请退款</el-button>
            <el-button type="info" plain disabled>等待发货</el-button>
          </template>
        </template>
        <template v-else-if="isShipping">
          <template v-if="!isMerchant">
            <el-button type="success" @click="handleConfirm">确认收货</el-button>
          </template>
          <el-tag v-else type="success">已发货，等待买家确认收货</el-tag>
          <el-button @click="viewLogistics">查看物流</el-button>
        </template>
        <template v-else-if="isCompleted">
          <template v-if="!isMerchant">
            <el-button @click="handleReorder">再来一单</el-button>
            <el-button type="primary" plain @click="handleReview">评价商品</el-button>
            <el-button type="warning" plain @click="showAfterSalesDialog">申请售后</el-button>
          </template>
          <el-tag v-else type="success">交易已完成</el-tag>
          <el-button v-if="order.trackingNumber" @click="viewLogistics">查看物流</el-button>
        </template>
        <template v-else-if="isCancelled">
          <template v-if="!isMerchant">
            <el-button @click="handleReorder">重新购买</el-button>
          </template>
          <el-tag v-else type="info">订单已取消</el-tag>
        </template>
        <template v-else-if="isRefunded">
          <template v-if="!isMerchant">
            <el-button @click="handleReorder">再来一单</el-button>
          </template>
          <el-tag v-else type="info">订单已退款</el-tag>
        </template>
      </div>
    </div>

    <div v-else-if="loading" class="loading">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else class="error-state">
      <div class="error-icon"><i class="el-icon-warning-outline"></i></div>
      <h3>加载失败</h3>
      <p>{{ errorMessage }}</p>
      <el-button type="primary" @click="loadOrder">重新加载</el-button>
    </div>

    <el-dialog :visible.sync="payDialogVisible" title="订单支付" width="480px" :append-to-body="true" class="pay-dialog">
      <div v-if="order" class="pay-dialog-body">
        <!-- 优惠券信息 -->
        <div class="pay-section coupon-section">
          <div class="section-title">
            <i class="el-icon-ticket"></i>
            <span>优惠券</span>
            <el-button type="primary" size="mini" plain @click="showChangeCouponDialog" class="change-btn">
              {{ order.couponAmount && order.couponAmount > 0 ? '更换' : '选择' }}
            </el-button>
          </div>
          <div v-if="order.couponAmount && order.couponAmount > 0" class="coupon-used">
            <div class="coupon-tag">
              <span class="tag-icon">券</span>
              <span class="tag-text">{{ order.couponCode || '优惠券' }}</span>
            </div>
            <span class="coupon-save">已省 ¥{{ formatPrice(order.couponAmount) }}</span>
          </div>
          <div v-else class="coupon-empty">
            <span>暂无优惠券</span>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="pay-section payment-section">
          <div class="section-title">
            <i class="el-icon-wallet"></i>
            <span>支付方式</span>
          </div>
          <div class="payment-methods">
            <div v-for="method in paymentMethods" :key="method.value"
                 :class="['payment-method', { active: selectedPayment === method.value }]"
                 @click="selectedPayment = method.value">
              <div class="method-icon">
                <i :class="method.icon"></i>
              </div>
              <span class="method-name">{{ method.label }}</span>
              <i v-if="selectedPayment === method.value" class="el-icon-check method-check"></i>
            </div>
          </div>
        </div>

        <!-- 支付金额 -->
        <div class="pay-section amount-section">
          <div class="amount-row">
            <span class="amount-label">实付金额</span>
            <div class="amount-value">
              <span class="currency">¥</span>
              <span class="price">{{ formatPrice(actualPaidAmount) }}</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="pay-dialog-footer">
          <el-button @click="payDialogVisible = false" size="medium">取消</el-button>
          <el-button type="primary" :loading="paying" @click="confirmPay" size="medium" class="confirm-btn">
            确认支付
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 更换优惠券弹窗 -->
    <el-dialog :visible.sync="changeCouponDialogVisible" title="更换优惠券" width="500px" :append-to-body="true" class="change-coupon-dialog">
      <div v-if="payCoupons.length === 0" class="empty-coupons">
        <i class="el-icon-ticket"></i>
        <p>暂无可用优惠券</p>
      </div>
      <div v-else class="coupon-list">
        <div
          :class="['coupon-item', { selected: !tempSelectedCouponId }]"
          @click="tempSelectedCouponId = null"
        >
          <div class="coupon-amount">
            <span class="min-amount">不使用优惠券</span>
          </div>
          <div class="coupon-info">
            <div class="coupon-name">原价支付</div>
          </div>
          <div class="coupon-check">
            <i v-if="!tempSelectedCouponId" class="el-icon-check"></i>
          </div>
        </div>
        <div
          v-for="coupon in payCoupons"
          :key="coupon.id"
          :class="['coupon-item', { 
            selected: tempSelectedCouponId === coupon.id,
            disabled: !coupon.usable 
          }]"
          @click="coupon.usable && (tempSelectedCouponId = coupon.id)"
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
            <i v-if="tempSelectedCouponId === coupon.id" class="el-icon-check"></i>
            <i v-else-if="!coupon.usable" class="el-icon-close"></i>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="changeCouponDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="changingCoupon" @click="confirmChangeCoupon">确定</el-button>
      </template>
    </el-dialog>

    <LogisticsDialog :visible.sync="logisticsDialogVisible" :order="order" />

    <el-dialog :visible.sync="afterSalesDialogVisible" title="申请售后" width="500px" :append-to-body="true">
      <el-form :model="afterSalesForm" label-width="100px">
        <el-form-item label="售后类型">
          <el-radio-group v-model="afterSalesForm.type">
            <el-radio label="return_refund">退货退款</el-radio>
            <el-radio label="refund_only">仅退款</el-radio>
            <el-radio label="exchange">换货</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择商品">
          <el-select v-model="afterSalesForm.productId" placeholder="请选择商品" style="width: 100%">
            <el-option v-for="item in orderProducts" :key="item.productId || item.id" :label="item.name || item.productName" :value="item.productId || item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="售后数量">
          <el-input-number v-model="afterSalesForm.quantity" :min="1" :max="maxQuantity" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="退款金额">
          <span style="color: #ff9800; font-size: 18px; font-weight: bold">¥{{ refundAmount }}</span>
        </el-form-item>
        <el-form-item label="售后原因">
          <el-select v-model="afterSalesForm.reason" placeholder="请选择原因" style="width: 100%">
            <el-option label="商品质量问题" value="商品质量问题"></el-option>
            <el-option label="商品与描述不符" value="商品与描述不符"></el-option>
            <el-option label="商品破损/漏发" value="商品破损/漏发"></el-option>
            <el-option label="发错商品" value="发错商品"></el-option>
            <el-option label="不想要了" value="不想要了"></el-option>
            <el-option label="其他原因" value="其他原因"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="afterSalesForm.description" type="textarea" :rows="3" placeholder="请详细描述您的问题"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="afterSalesDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingAfterSales" @click="submitAfterSales">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog :visible.sync="refundDialogVisible" title="申请退款" width="500px" :append-to-body="true">
      <div class="refund-dialog-content">
        <div class="refund-notice">
          <i class="el-icon-info"></i>
          <span v-if="isPayAfterDelivery">先用后付订单，退款将恢复商品库存</span>
          <span v-else-if="isBalancePayment">余额支付订单，退款将退回您的余额</span>
          <span v-else>退款将在审核通过后原路返回您的支付账户</span>
        </div>
        <el-form :model="refundForm" label-width="100px" class="refund-form">
          <el-form-item label="退款类型">
            <el-radio-group v-model="refundForm.type">
              <el-radio label="full_refund">全额退款</el-radio>
              <el-radio label="partial_refund">部分退款</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="选择商品" v-if="refundForm.type === 'partial_refund'">
            <el-select v-model="refundForm.productId" placeholder="请选择要退款的商品" style="width: 100%">
              <el-option v-for="item in orderProducts" :key="item.productId || item.id" :label="item.name || item.productName" :value="item.productId || item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="退款数量" v-if="refundForm.type === 'partial_refund'">
            <el-input-number v-model="refundForm.quantity" :min="1" :max="refundMaxQuantity" style="width: 100%"></el-input-number>
          </el-form-item>
          <el-form-item label="退款金额">
            <div class="refund-amount-display">
              <span class="amount-label">预计退款</span>
              <span v-if="isPayAfterDelivery" class="amount-value" style="color: #67c23a">无需退款</span>
              <span v-else class="amount-value">¥{{ formatPrice(refundTotalAmount) }}</span>
            </div>
          </el-form-item>
          <el-form-item label="退款原因">
            <el-select v-model="refundForm.reason" placeholder="请选择退款原因" style="width: 100%">
              <el-option label="不想要了" value="不想要了"></el-option>
              <el-option label="拍错/多拍" value="拍错/多拍"></el-option>
              <el-option label="商品质量问题" value="商品质量问题"></el-option>
              <el-option label="商品与描述不符" value="商品与描述不符"></el-option>
              <el-option label="其他原因" value="其他原因"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="补充说明">
            <el-input v-model="refundForm.description" type="textarea" :rows="3" placeholder="请输入补充说明（选填）"></el-input>
          </el-form-item>
        </el-form>
        <div class="refund-summary">
          <div class="summary-item">
            <span>订单金额</span>
            <span>¥{{ formatPrice(finalTotal) }}</span>
          </div>
          <div class="summary-item" v-if="order && order.couponAmount && order.couponAmount > 0">
            <span>优惠券抵扣</span>
            <span style="color: #67c23a">-¥{{ formatPrice(order.couponAmount) }}</span>
          </div>
          <div class="summary-item">
            <span>实际支付</span>
            <span style="color: #ff9800; font-weight: 600">¥{{ formatPrice(actualPaidAmount) }}</span>
          </div>
          <div class="summary-divider"></div>
          <div class="summary-item" v-if="!isPayAfterDelivery">
            <span>退款金额</span>
            <span style="color: #f56c6c; font-weight: 600">-¥{{ formatPrice(refundTotalAmount) }}</span>
          </div>
          <div class="summary-item" v-if="isPayAfterDelivery">
            <span>退款金额</span>
            <span style="color: #67c23a">无需退款（先用后付）</span>
          </div>
          <div class="summary-divider"></div>
          <div class="summary-item total">
            <span>退款后实付</span>
            <span v-if="isPayAfterDelivery" style="color: #67c23a">¥0.00（先用后付）</span>
            <span v-else>¥{{ formatPrice(Math.max(0, actualPaidAmount - refundTotalAmount)) }}</span>
          </div>
        </div>
        <div class="refund-tips">
          <p><i class="el-icon-warning"></i> 温馨提示：</p>
          <ul>
            <li>退款申请提交后，商家将在24小时内审核</li>
            <li v-if="isPayAfterDelivery">先用后付订单：退款将恢复商品库存，无需退款</li>
            <li v-else-if="isBalancePayment">余额支付订单：退款将退回您的账户余额</li>
            <li v-else>审核通过后，退款将原路返回您的支付账户</li>
          </ul>
        </div>
      </div>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingRefund" @click="submitRefund">确认退款</el-button>
      </template>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog v-if="order" :visible.sync="shipDialogVisible" title="订单发货" width="520px" :append-to-body="true">
      <div class="ship-dialog-content">
        <div class="ship-order-summary">
          <div class="summary-row">
            <span class="summary-label">订单号：</span>
            <span class="summary-value">{{ order.orderId || order.id }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">商品：</span>
            <span class="summary-value">{{ getOrderProductNames(order) }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">收货人：</span>
            <span class="summary-value">{{ order.contactName || '-' }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">地址：</span>
            <span class="summary-value">{{ order.shippingAddress || '-' }}</span>
          </div>
        </div>
        <el-divider />
        <el-form :model="shipForm" label-width="100px">
          <el-form-item label="快递公司">
            <el-select v-model="shipForm.expressCompany" placeholder="请选择快递公司" style="width: 100%" @change="onExpressChange">
              <el-option
                v-for="item in expressOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value">
                <span>{{ item.label }}</span>
                <span v-if="item.code" style="color: #999; margin-left: 8px; font-size: 12px;">({{ item.code }}-)</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="快递单号">
            <el-input v-model="shipForm.expressNo" placeholder="请输入快递单号" @input="onExpressNoInput">
              <template slot="prepend" v-if="currentExpressCode">{{ currentExpressCode }}-</template>
            </el-input>
            <div v-if="previewTrackingNo" class="tracking-preview">
              <i class="el-icon-document"></i>
              完整单号预览：<strong>{{ previewTrackingNo }}</strong>
            </div>
          </el-form-item>
        </el-form>
        <div class="ship-tips">
          <i class="el-icon-warning"></i>
          <span>发货后订单状态将变为配送中，买家可查看物流信息</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipping" @click="submitShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { MessageBox } from 'element-ui'
import { getOrderById, payOrder, cancelOrder, confirmReceive, createAfterSales, merchantShipOrder, getCheckoutCoupons, changeOrderCoupon } from '@/api'
import { addToCart as addToCartApi } from '@/data/cart'
import { formatPrice, formatDateTime, getFullImageUrl, copyToClipboard } from '@/utils/common'
import LogisticsDialog from './components/LogisticsDialog.vue'

export default {
  name: 'OrderDetail',
  components: {
    LogisticsDialog
  },
  data() {
    return {
      order: null,
      loading: true,
      errorMessage: '',
      payDialogVisible: false,
      logisticsDialogVisible: false,
      selectedPayment: 'alipay',
      paying: false,
      afterSalesDialogVisible: false,
      submittingAfterSales: false,
      afterSalesForm: {
        type: 'return_refund',
        productId: null,
        quantity: 1,
        reason: '',
        description: ''
      },
      refundDialogVisible: false,
      submittingRefund: false,
      refundForm: {
        type: 'full_refund',
        productId: null,
        quantity: 1,
        reason: '',
        description: ''
      },
      shippingTimer: null,
      shippingNow: Date.now(),
      paymentMethods: [
        { value: 'alipay', label: '支付宝', icon: 'el-icon-s-goods' },
        { value: 'wechat', label: '微信支付', icon: 'el-icon-s-goods' },
        { value: 'card', label: '银行卡', icon: 'el-icon-s-order' }
      ],
      shipDialogVisible: false,
      shipping: false,
      shipForm: { expressCompany: '', expressNo: '' },
      expressOptions: [
        { label: '顺丰快递', value: '顺丰快递', code: 'SF' },
        { label: '圆通快递', value: '圆通快递', code: 'YT' },
        { label: '中通快递', value: '中通快递', code: 'ZT' },
        { label: '韵达快递', value: '韵达快递', code: 'YD' },
        { label: '申通快递', value: '申通快递', code: 'ST' },
        { label: '邮政快递', value: '邮政快递', code: 'EMS' },
        { label: '极兔快递', value: '极兔快递', code: 'JT' },
        { label: '京东物流', value: '京东物流', code: 'JD' },
        { label: '其他', value: '其他', code: '' }
      ],
      currentExpressCode: '',
      previewTrackingNo: '',
      changeCouponDialogVisible: false,
      payCoupons: [],
      tempSelectedCouponId: null,
      changingCoupon: false
    }
  },
  computed: {
    orderId() {
      return this.$route.params.orderId
    },
    orderProducts() {
      if (!this.order) return []
      if (this.order.products && this.order.products.length > 0) {
        return this.order.products
      }
      return this.order.items || this.order.orderProducts || []
    },
    orderTotal() {
      return this.orderProducts.reduce((sum, p) => sum + ((p.price || p.unitPrice || 0) * (p.quantity || p.count || 1)), 0)
    },
    finalTotal() {
      return this.orderTotal + ((this.order && this.order.shippingFee) || 0) - ((this.order && this.order.discount) || 0)
    },
    actualPaidAmount() {
      if (this.order && this.order.actualAmount != null) {
        return Number(this.order.actualAmount) || 0
      }
      return this.finalTotal
    },
    payAmount() {
      return this.actualPaidAmount
    },
    actualOrderId() {
      return (this.order && this.order.id) || (this.order && this.order.orderId)
    },
    isPending() {
      const s = this.order && this.order.status
      return s === 'PENDING' || s === 'pending'
    },
    isProcessing() {
      const s = this.order && this.order.status
      return s === 'PAID' || s === 'paid' || s === 'PROCESSING' || s === 'processing'
    },
    isShipping() {
      const s = this.order && this.order.status
      return s === 'SHIPPED' || s === 'SHIPPING' || s === 'shipping' || s === 'shipped'
    },
    isCompleted() {
      const s = this.order && this.order.status
      return s === 'COMPLETED' || s === 'completed' || s === 'DELIVERED' || s === 'delivered'
    },
    isCancelled() {
      const s = this.order && this.order.status
      return s === 'CANCELLED' || s === 'cancelled'
    },
    isRefunded() {
      const s = this.order && this.order.status
      return s === 'REFUNDED' || s === 'refunded'
    },
    maxQuantity() {
      const selectedProduct = this.orderProducts.find(p => (p.productId || p.id) === this.afterSalesForm.productId)
      return selectedProduct ? (selectedProduct.quantity || selectedProduct.count || 1) : 1
    },
    refundAmount() {
      const selectedProduct = this.orderProducts.find(p => (p.productId || p.id) === this.afterSalesForm.productId)
      if (!selectedProduct) return 0
      const price = selectedProduct.price || selectedProduct.unitPrice || 0
      return price * this.afterSalesForm.quantity
    },
    refundMaxQuantity() {
      if (this.refundForm.type === 'full_refund') {
        return this.orderProducts.reduce((sum, p) => sum + (p.quantity || p.count || 1), 0)
      }
      const selectedProduct = this.orderProducts.find(p => (p.productId || p.id) === this.refundForm.productId)
      return selectedProduct ? (selectedProduct.quantity || selectedProduct.count || 1) : 1
    },
    refundTotalAmount() {
      if (this.isPayAfterDelivery) return 0
      if (this.refundForm.type === 'full_refund') {
        return this.actualPaidAmount
      }
      const selectedProduct = this.orderProducts.find(p => (p.productId || p.id) === this.refundForm.productId)
      if (!selectedProduct) return 0
      const price = selectedProduct.price || selectedProduct.unitPrice || 0
      return price * this.refundForm.quantity
    },
    shippingDeadline() {
      if (!this.order) return null
      const createTime = this.order.createTime || this.order.createdAt
      if (!createTime) return null
      const deadline = new Date(new Date(createTime).getTime() + 5 * 60 * 60 * 1000)
      return deadline
    },
    shippingDeadlineText() {
      if (!this.shippingDeadline) return ''
      const now = this.shippingNow
      const deadline = this.shippingDeadline
      if (now >= deadline) return '已超时'
      const diff = deadline - now
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      return `${hours}小时${minutes}分钟内发货`
    },
    isBalancePayment() {
      return this.order && (this.order.paymentMethod === 'balance' || this.order.paymentMethod === 'wallet')
    },
    isPayAfterDelivery() {
      return this.order && (this.order.paymentMethod === 'pay_after_delivery' || this.order.paymentMethod === 'credit')
    },
    isMerchant() {
      try {
        const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
        return user.role === 'MERCHANT'
      } catch (e) {
        return false
      }
    }
  },
  watch: {
    '$route.params.orderId': {
      handler() {
        this.loadOrder()
      }
    },
    'refundForm.type'() {
      this.refundForm.productId = null
      this.refundForm.quantity = 1
    },
    'refundForm.productId'() {
      this.refundForm.quantity = 1
    }
  },
  mounted() {
    this.loadOrder()
    this.startShippingDeadlineTimer()
    if (this.$route.query.action === 'refund') {
      this.$nextTick(() => {
        this.showRefundDialog()
      })
    }
  },
  beforeDestroy() {
    this.stopShippingDeadlineTimer()
  },
  methods: {
    formatPrice,
    formatDateTime,

    async loadOrder() {
      if (!this.orderId) {
        this.errorMessage = '订单ID不存在'
        this.loading = false
        return
      }
      this.loading = true
      this.errorMessage = ''
      try {
        const res = await getOrderById(this.orderId)
        this.order = res.data || res
        if (!this.order) {
          this.errorMessage = '订单不存在'
        }
      } catch (err) {
        console.error('加载订单失败:', err)
        this.errorMessage = err.message || '加载订单失败，请检查订单号是否正确'
        this.order = null
      } finally {
        this.loading = false
      }
    },

    getStatusIcon(status) {
      const icons = {
        'PENDING': 'el-icon-wallet', 'PAID': 'el-icon-s-goods', 'paid': 'el-icon-s-goods',
        'PROCESSING': 'el-icon-s-goods', 'processing': 'el-icon-s-goods',
        'SHIPPED': 'el-icon-truck', 'SHIPPING': 'el-icon-truck', 'shipping': 'el-icon-truck', 'shipped': 'el-icon-truck',
        'DELIVERED': 'el-icon-check',
        'COMPLETED': 'el-icon-check', 'CANCELLED': 'el-icon-close',
        'REFUNDED': 'el-icon-circle-close', 'refunded': 'el-icon-circle-close',
        'pending': 'el-icon-wallet',
        'completed': 'el-icon-check', 'cancelled': 'el-icon-close'
      }
      return icons[status] || 'el-icon-s-data'
    },

    getStatusText(status) {
      const texts = {
        'PENDING': '待支付', 'PAID': '已支付', 'paid': '已支付',
        'PROCESSING': '待发货', 'processing': '待发货',
        'SHIPPED': '配送中', 'SHIPPING': '配送中', 'shipping': '配送中', 'shipped': '配送中',
        'DELIVERED': '已签收', 'COMPLETED': '已完成', 'CANCELLED': '已取消',
        'REFUNDED': '订单已结束', 'refunded': '订单已结束',
        'pending': '待支付',
        'completed': '已完成', 'cancelled': '已取消'
      }
      return texts[status] || status
    },

    getStatusDesc(status) {
      const descs = {
        'PENDING': '请尽快完成支付，订单将为您保留24小时',
        'PAID': '商家正在准备商品，请耐心等待',
        'paid': '商家正在准备商品，请耐心等待',
        'PROCESSING': '商家正在准备商品，请耐心等待',
        'processing': '商家正在准备商品，请耐心等待',
        'SHIPPED': '商品已发出，请注意查收',
        'SHIPPING': '商品已发出，请注意查收',
        'shipping': '商品已发出，请注意查收',
        'shipped': '商品已发出，请注意查收',
        'DELIVERED': '商品已签收，感谢您的购买',
        'COMPLETED': '感谢您的购买，欢迎再次光临',
        'CANCELLED': '订单已取消，如有需要可重新下单',
        'REFUNDED': '退款已处理，金额已退回您的账户',
        'refunded': '退款已处理，金额已退回您的账户',
        'pending': '请尽快完成支付，订单将为您保留24小时',
        'completed': '感谢您的购买，欢迎再次光临',
        'cancelled': '订单已取消，如有需要可重新下单'
      }
      return descs[status] || ''
    },

    getPaymentText(method) {
      const texts = {
        alipay: '支付宝', wechat: '微信支付', card: '银行卡',
        balance: '余额支付', wallet: '余额支付',
        pay_after_delivery: '先用后付', credit: '先用后付',
        cash: '货到付款'
      }
      return texts[method] || method || '未支付'
    },

    getImage(product) {
      const image = product.image || product.img || product.productImage ||
        (product.product && product.product.image) || null
      return getFullImageUrl(image, null, product.id || product.productId || (product.product && product.product.id))
    },

    handleImageError(e) {
      e.target.style.display = 'none'
    },

    goProduct(id) {
      if (id) window.location.href = `/front/product/${id}`
    },

    async copyTracking() {
      if (this.order && this.order.trackingNumber) {
        await copyToClipboard(this.order.trackingNumber)
      }
    },

    handlePay() {
      this.payDialogVisible = true
      this.loadPayCoupons()
    },

    async loadPayCoupons() {
      try {
        const orderAmount = this.order ? (this.order.totalAmount || this.orderTotal) : 0
        const res = await getCheckoutCoupons(orderAmount)
        if (res && res.data) {
          this.payCoupons = Array.isArray(res.data) ? res.data : []
          this.tempSelectedCouponId = this.order && this.order.couponId ? this.order.couponId : null
        }
      } catch (e) {
        console.error('加载优惠券失败:', e)
        this.payCoupons = []
      }
    },

    showChangeCouponDialog() {
      this.loadPayCoupons()
      this.changeCouponDialogVisible = true
    },

    async confirmChangeCoupon() {
      if (!this.actualOrderId) return
      this.changingCoupon = true
      try {
        await changeOrderCoupon(this.actualOrderId, this.tempSelectedCouponId)
        this.changeCouponDialogVisible = false
        await this.loadOrder()
        this.$message.success('优惠券更换成功')
      } catch (err) {
        console.error('更换优惠券失败:', err)
        this.$message.error(err.message || '更换优惠券失败')
      } finally {
        this.changingCoupon = false
      }
    },

    formatCouponTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    },

    async confirmPay() {
      if (!this.actualOrderId) return
      this.paying = true
      try {
        const payData = { paymentMethod: this.selectedPayment }
        await payOrder(this.actualOrderId, payData)
        this.payDialogVisible = false
        this.$message.success('支付成功')
        this.loadOrder()
      } catch (err) {
        console.error('支付失败:', err)
        this.$message.error(err.message || '支付失败，请重试')
      } finally {
        this.paying = false
      }
    },

    async handleCancel() {
      if (!this.actualOrderId) return
      try {
        await MessageBox.confirm('确定要取消该订单吗？取消后优惠券将自动退回', '提示', { type: 'warning' })
        await cancelOrder(this.actualOrderId, '用户取消')
        this.$message.success('订单已取消')
        this.loadOrder()
      } catch (err) {
        if (err !== 'cancel') {
          console.error('取消订单失败:', err)
          this.$message.error(err.message || '取消订单失败，请重试')
        }
      }
    },

    async handleConfirm() {
      if (!this.actualOrderId) {
        this.$message.error('订单信息不完整')
        return
      }
      try {
        await MessageBox.confirm('确认已收到商品吗？', '提示', { type: 'info' })
        await confirmReceive(this.actualOrderId)
        this.$message.success('确认收货成功')
        this.loadOrder()
      } catch (err) {
        if (err !== 'cancel') {
          console.error('确认收货失败:', err)
          this.$message.error('确认收货失败，请重试')
        }
      }
    },

    viewLogistics() {
      this.logisticsDialogVisible = true
    },

    handleShip() {
      this.shipDialogVisible = true
      this.shipForm = { expressCompany: '', expressNo: '' }
      this.currentExpressCode = ''
      this.previewTrackingNo = ''
    },

    onExpressChange(val) {
      const opt = this.expressOptions.find(o => o.value === val)
      this.currentExpressCode = opt ? opt.code : ''
      this.updatePreview()
    },

    onExpressNoInput() {
      this.updatePreview()
    },

    updatePreview() {
      if (this.shipForm.expressNo && this.currentExpressCode) {
        this.previewTrackingNo = `${this.currentExpressCode}-${this.shipForm.expressNo}`
      } else if (this.shipForm.expressNo) {
        this.previewTrackingNo = this.shipForm.expressNo
      } else {
        this.previewTrackingNo = ''
      }
    },

    getOrderProductNames(order) {
      if (!order) return '-'
      const products = order.products || []
      if (products.length === 0) return '-'
      const names = products.map(p => p.productName || p.name || '商品').slice(0, 3)
      return names.join('、') + (products.length > 3 ? ` 等${products.length}件` : '')
    },

    async submitShip() {
      if (!this.shipForm.expressCompany) {
        this.$message.warning('请选择快递公司')
        return
      }
      if (!this.shipForm.expressNo) {
        this.$message.warning('请输入快递单号')
        return
      }
      if (!this.order || !this.order.id) {
        this.$message.error('订单信息无效')
        return
      }
      this.shipping = true
      try {
        const codePrefix = this.currentExpressCode || ''
        const trackingNo = codePrefix ? `${codePrefix}-${this.shipForm.expressNo}` : this.shipForm.expressNo
        const res = await merchantShipOrder(this.order.id, {
          expressCompany: this.shipForm.expressCompany,
          expressNo: trackingNo
        })
        if (res.code === 200 || res.success) {
          this.$message.success('发货成功')
          this.shipDialogVisible = false
          this.loadOrder()
        } else {
          this.$message.error(res.message || '发货失败')
        }
      } catch (err) {
        console.error('发货失败:', err)
        this.$message.error(err.message || '发货失败，请重试')
      } finally {
        this.shipping = false
      }
    },

    async handleReorder() {
      if (!this.order) return
      const products = this.orderProducts
      if (!products || products.length === 0) return
      try {
        for (const item of products) {
          const productId = item.productId || item.id
          const quantity = item.quantity || item.count || 1
          try {
            await addToCartApi({ productId, quantity })
          } catch {}
        }
        window.dispatchEvent(new Event('xm-cart-changed'))
        setTimeout(() => { window.location.href = '/front/cart' }, 500)
      } catch {}
    },

    handleReview() {
      if (!this.order) return
      const products = this.orderProducts
      if (products && products.length > 0) {
        const productId = products[0].productId || products[0].id
        window.location.href = `/front/product/${productId}#reviews`
      }
    },

    showAfterSalesDialog() {
      if (!this.orderProducts || this.orderProducts.length === 0) {
        this.$message.warning('暂无可申请售后的商品')
        return
      }
      this.afterSalesForm = {
        type: 'return_refund',
        productId: this.orderProducts[0].productId || this.orderProducts[0].id,
        quantity: 1,
        reason: '',
        description: ''
      }
      this.afterSalesDialogVisible = true
    },

    async submitAfterSales() {
      if (!this.afterSalesForm.productId) {
        this.$message.warning('请选择商品')
        return
      }
      if (!this.afterSalesForm.reason) {
        this.$message.warning('请选择售后原因')
        return
      }
      if (!this.afterSalesForm.description) {
        this.$message.warning('请填写详细描述')
        return
      }
      this.submittingAfterSales = true
      try {
        await createAfterSales({
          orderId: this.order.id,
          productId: this.afterSalesForm.productId,
          type: this.afterSalesForm.type,
          quantity: this.afterSalesForm.quantity,
          reason: this.afterSalesForm.reason,
          description: this.afterSalesForm.description
        })
        this.$message.success('售后申请已提交，请等待商家审核')
        this.afterSalesDialogVisible = false
      } catch (err) {
        console.error('提交售后申请失败:', err)
        this.$message.error(err.message || '提交失败，请重试')
      } finally {
        this.submittingAfterSales = false
      }
    },

    showRefundDialog() {
      this.refundForm = {
        type: 'full_refund',
        productId: null,
        quantity: 1,
        reason: '',
        description: ''
      }
      this.refundDialogVisible = true
    },

    async submitRefund() {
      if (!this.refundForm.reason) {
        this.$message.warning('请选择退款原因')
        return
      }
      this.submittingRefund = true
      try {
        if (this.isPayAfterDelivery) {
          const afterSalesData = {
            orderId: this.order.id,
            type: 'refund_only',
            reason: this.refundForm.reason,
            description: this.refundForm.description
          }
          if (this.refundForm.type === 'partial_refund') {
            if (!this.refundForm.productId) {
              this.$message.warning('请选择要退款的商品')
              this.submittingRefund = false
              return
            }
            afterSalesData.productId = this.refundForm.productId
            afterSalesData.quantity = this.refundForm.quantity
          } else {
            afterSalesData.productId = this.orderProducts[0].productId || this.orderProducts[0].id
            afterSalesData.quantity = this.orderProducts.reduce((sum, p) => sum + (p.quantity || p.count || 1), 0)
          }
          await createAfterSales(afterSalesData)
          this.$message.success('退款申请已提交，商品库存已恢复')
        } else {
          const afterSalesData = {
            orderId: this.order.id,
            type: 'refund_only',
            reason: this.refundForm.reason,
            description: this.refundForm.description
          }
          if (this.refundForm.type === 'partial_refund') {
            if (!this.refundForm.productId) {
              this.$message.warning('请选择要退款的商品')
              this.submittingRefund = false
              return
            }
            afterSalesData.productId = this.refundForm.productId
            afterSalesData.quantity = this.refundForm.quantity
          } else {
            afterSalesData.productId = this.orderProducts[0].productId || this.orderProducts[0].id
            afterSalesData.quantity = this.orderProducts.reduce((sum, p) => sum + (p.quantity || p.count || 1), 0)
          }
          await createAfterSales(afterSalesData)
          if (this.isBalancePayment) {
            this.$message.success('退款申请已提交，¥' + formatPrice(this.refundTotalAmount) + '将退回您的余额')
          } else {
            this.$message.success('退款申请已提交，¥' + formatPrice(this.refundTotalAmount) + '将退回原支付账户')
          }
        }
        this.refundDialogVisible = false
        await this.loadOrder()
      } catch (err) {
        console.error('提交退款申请失败:', err)
        this.$message.error(err.message || '提交失败，请重试')
      } finally {
        this.submittingRefund = false
      }
    },

    startShippingDeadlineTimer() {
      this.stopShippingDeadlineTimer()
      this.shippingTimer = setInterval(() => {
        this.shippingNow = Date.now()
      }, 60000)
    },

    stopShippingDeadlineTimer() {
      if (this.shippingTimer) {
        clearInterval(this.shippingTimer)
        this.shippingTimer = null
      }
    }
  }
}
</script>

<style scoped>
.order-detail-page { max-width: 800px; margin: 20px auto; padding: 20px; }

.order-status-bar { background: linear-gradient(135deg, #667eea, #764ba2); color: white; padding: 30px; border-radius: 12px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
.status-info { display: flex; align-items: center; gap: 16px; }
.status-icon { width: 64px; height: 64px; background: rgba(255,255,255,0.2); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28px; }
.status-text h2 { margin: 0 0 4px; font-size: 24px; }
.status-text p { margin: 0; opacity: 0.9; font-size: 14px; }
.order-time { font-size: 14px; opacity: 0.9; }

.info-card { background: white; border-radius: 8px; margin-bottom: 16px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.card-header { display: flex; align-items: center; gap: 8px; padding: 16px; background: #fafafa; border-bottom: 1px solid #eee; }
.card-header i { color: #ff9800; font-size: 18px; }
.card-header h3 { margin: 0; font-size: 16px; color: #333; }
.card-body { padding: 16px; }

.info-row { display: flex; padding: 8px 0; border-bottom: 1px solid #f5f5f5; }
.info-row:last-child { border-bottom: none; }
.info-row .label { color: #999; width: 100px; flex-shrink: 0; }
.info-row .value { color: #333; flex: 1; }
.info-row.total .label, .info-row.total .value { font-weight: bold; font-size: 16px; }
.info-row .discount { color: #67c23a; }
.info-row .final-price { color: #ff9800; font-size: 20px; }
.tracking { display: flex; align-items: center; gap: 8px; }
.tracking-row { display: flex; align-items: center; gap: 8px; }
.tracking-code {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  background: #f5f7fa;
  padding: 4px 10px;
  border-radius: 4px;
  letter-spacing: 0.5px;
}
.express-company { font-weight: 600; color: #409eff; }
.logistics-card .card-header { display: flex; align-items: center; justify-content: space-between; }
.logistics-card .card-header h3 { margin: 0; }
.shipping-deadline .deadline-text { color: #ff9800; font-weight: 600; display: flex; align-items: center; gap: 6px; }
.shipping-deadline .deadline-text i { font-size: 16px; }

.product-item { display: flex; gap: 12px; padding: 12px 0; border-bottom: 1px solid #f5f5f5; }
.product-item:last-child { border-bottom: none; }
.product-image { width: 80px; height: 80px; border-radius: 8px; background: #f5f5f5; flex-shrink: 0; object-fit: cover; }
.product-info { flex: 1; min-width: 0; }
.product-name { margin: 0 0 4px; font-size: 14px; color: #333; cursor: pointer; }
.product-name:hover { color: #ff9800; }
.product-desc { margin: 0; font-size: 12px; color: #999; }
.product-price { text-align: right; flex-shrink: 0; }
.unit-price { display: block; color: #ff9800; font-weight: 600; }
.quantity { display: block; color: #999; font-size: 12px; margin: 4px 0; }
.subtotal { display: block; font-weight: bold; color: #333; }
.no-products { padding: 20px 0; }

.action-bar { background: white; padding: 20px; border-radius: 8px; display: flex; justify-content: flex-end; gap: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }

.payment-methods { display: flex; flex-direction: column; gap: 10px; }
.payment-method { display: flex; align-items: center; gap: 10px; padding: 12px; border: 1px solid #ddd; border-radius: 8px; cursor: pointer; transition: all 0.2s; }
.payment-method:hover { border-color: #ff9800; }
.payment-method.active { border-color: #ff9800; background: #fff9e6; }
.payment-method i { font-size: 20px; color: #ff9800; }

.loading { padding: 40px; background: white; border-radius: 8px; }

.error-state { text-align: center; padding: 60px 20px; background: white; border-radius: 8px; }
.error-icon { font-size: 64px; color: #f56c6c; margin-bottom: 16px; }
.error-state h3 { margin: 0 0 8px; color: #666; }
.error-state p { color: #999; margin: 0 0 20px; }

::v-deep .el-dialog__wrapper { background-color: rgba(0, 0, 0, 0.5) !important; }
::v-deep .el-dialog { background-color: #fff !important; }
::v-deep .el-dialog__body { background-color: #fff !important; }

.refund-dialog-content { padding: 0 10px; }
.refund-notice { display: flex; align-items: center; gap: 8px; padding: 12px 16px; background: #e6f7ff; border: 1px solid #91d5ff; border-radius: 4px; margin-bottom: 20px; color: #1890ff; font-size: 14px; }
.refund-notice i { font-size: 16px; }
.refund-form { margin-bottom: 20px; }
.refund-amount-display { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #fff9e6; border-radius: 4px; width: 100%; }
.amount-label { color: #666; font-size: 14px; }
.amount-value { color: #ff9800; font-size: 20px; font-weight: bold; }
.refund-summary { background: #fafafa; border-radius: 8px; padding: 16px; margin-bottom: 20px; }
.summary-item { display: flex; justify-content: space-between; padding: 8px 0; color: #666; font-size: 14px; }
.summary-item.total { color: #333; font-weight: bold; font-size: 16px; padding-top: 12px; }
.summary-divider { height: 1px; background: #e8e8e8; margin: 8px 0; }
.refund-tips { background: #fff7e6; border: 1px solid #ffd591; border-radius: 4px; padding: 12px 16px; color: #d46b08; font-size: 13px; }
.refund-tips p { margin: 0 0 8px; font-weight: bold; display: flex; align-items: center; gap: 6px; }
.refund-tips ul { margin: 0; padding-left: 20px; }
.refund-tips li { margin-bottom: 4px; line-height: 1.5; }

/* 发货对话框样式 */
.ship-dialog-content { padding: 0 10px; }
.ship-order-summary { background: #f8f9fa; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
.summary-row { display: flex; padding: 6px 0; font-size: 14px; }
.summary-label { color: #999; width: 80px; flex-shrink: 0; }
.summary-value { color: #333; flex: 1; }
.tracking-preview { margin-top: 8px; padding: 8px 12px; background: #ecf5ff; border-radius: 4px; font-size: 13px; color: #409eff; display: flex; align-items: center; gap: 6px; }
.tracking-preview i { font-size: 14px; }
.ship-tips { margin-top: 12px; padding: 10px 14px; background: #fdf6ec; border-radius: 6px; font-size: 13px; color: #e6a23c; display: flex; align-items: center; gap: 8px; }

/* 支付弹窗样式 */
::v-deep .pay-dialog {
  border-radius: 16px !important;
  overflow: hidden;
}

::v-deep .pay-dialog .el-dialog__header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
}

::v-deep .pay-dialog .el-dialog__title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

::v-deep .pay-dialog .el-dialog__body {
  padding: 0 !important;
}

::v-deep .pay-dialog .el-dialog__footer {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.pay-dialog-body {
  padding: 20px 24px;
}

.pay-section {
  margin-bottom: 20px;
}

.pay-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.section-title i {
  color: #ff9800;
  font-size: 16px;
}

.section-title span {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.change-btn {
  margin-left: auto;
  font-size: 12px;
}

/* 优惠券区域 */
.coupon-used {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: linear-gradient(135deg, #fff8f0, #fff3e6);
  border: 1px solid #ffe0b2;
  border-radius: 10px;
}

.coupon-tag {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #ff6b35, #ff9800);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  border-radius: 4px;
}

.tag-text {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  letter-spacing: 0.5px;
}

.coupon-save {
  font-size: 14px;
  font-weight: 600;
  color: #ff5722;
}

.coupon-empty {
  padding: 12px 16px;
  background: #fafafa;
  border: 1px dashed #e0e0e0;
  border-radius: 10px;
  text-align: center;
}

.coupon-empty span {
  font-size: 13px;
  color: #999;
}

/* 支付方式 */
.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1.5px solid #e8e8e8;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.payment-method:hover {
  border-color: #ff9800;
  background: #fffbf5;
}

.payment-method.active {
  border-color: #ff9800;
  background: linear-gradient(135deg, #fffbf5, #fff8f0);
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.15);
}

.method-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #fff3e6, #ffe0b2);
  border-radius: 8px;
}

.method-icon i {
  font-size: 18px;
  color: #ff9800;
}

.method-name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.method-check {
  font-size: 18px;
  color: #ff9800;
}

/* 支付金额 */
.amount-section {
  padding: 16px;
  background: linear-gradient(135deg, #fff8f0, #fff3e6);
  border-radius: 12px;
}

.amount-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.amount-label {
  font-size: 14px;
  color: #666;
}

.amount-value {
  display: flex;
  align-items: baseline;
}

.amount-value .currency {
  font-size: 16px;
  font-weight: 600;
  color: #ff5722;
  margin-right: 2px;
}

.amount-value .price {
  font-size: 28px;
  font-weight: 700;
  color: #ff5722;
  line-height: 1;
}

/* 底部按钮 */
.pay-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.pay-dialog-footer .confirm-btn {
  padding: 10px 32px;
  font-size: 15px;
  font-weight: 600;
}

/* 优惠券选择弹窗 */
::v-deep .coupon-select-dialog { background-color: #fff !important; border-radius: 12px !important; }
::v-deep .coupon-select-dialog .el-dialog__body { padding: 16px !important; max-height: 60vh; overflow-y: auto; }

.empty-coupons { text-align: center; padding: 40px 20px; color: #999; }
.empty-coupons i { font-size: 48px; color: #ddd; margin-bottom: 12px; }

.coupon-list { display: flex; flex-direction: column; gap: 12px; }

.coupon-item { display: flex; align-items: center; padding: 16px; border: 2px solid #e8e8e8; border-radius: 10px; cursor: pointer; transition: all 0.2s; background: #fff; }
.coupon-item:hover:not(.disabled) { border-color: #ff9800; background: #fffbf5; }
.coupon-item.selected { border-color: #ff6b35; background: #fff5e6; }
.coupon-item.disabled { opacity: 0.5; cursor: not-allowed; background: #f5f5f5; }

.coupon-amount { display: flex; flex-direction: column; align-items: center; min-width: 100px; padding-right: 16px; border-right: 1px dashed #e8e8e8; }
.amount-value { color: #ff5722; font-size: 24px; font-weight: 700; }
.min-amount { color: #999; font-size: 12px; margin-top: 4px; }

.coupon-info { flex: 1; margin-left: 16px; }
.coupon-name { font-weight: 600; color: #333; font-size: 14px; margin-bottom: 4px; }
.coupon-time { color: #999; font-size: 12px; }
.disable-reason { color: #ff5722; font-size: 12px; margin-top: 4px; }

.coupon-check { margin-left: 12px; font-size: 20px; }
.coupon-check .el-icon-check { color: #ff6b35; }
.coupon-check .el-icon-close { color: #ccc; }

.coupon-dialog-footer { display: flex; justify-content: space-between; align-items: center; }
.ship-tips i { font-size: 16px; }

/* 更换优惠券弹窗样式 */
::v-deep .change-coupon-dialog .el-dialog__body { padding: 16px !important; max-height: 60vh; overflow-y: auto; }
</style>
