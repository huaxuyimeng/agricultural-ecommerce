/**
 * 前台首页组件
 * 文件路径: src/views/front/Home.vue
 * 功能描述: 前台首页，包含轮播图、商品分类、订单信息和最新咨询等模块
 */
<template>
  <div class="home-page">
    <!-- 轮播图区域 -->
    <section id="home" class="carousel-section">
      <el-carousel 
        :interval="4000" 
        arrow="always" 
        height="400px" 
        indicator-position="outside"
        class="home-carousel"
      >
        <el-carousel-item v-for="(img, idx) in carouselImages" :key="idx">
          <div class="carousel-item">
            <img :src="img.url" :alt="img.alt" class="carousel-image" />
            <div class="carousel-overlay">
              <h3 class="carousel-title">{{ img.title }}</h3>
              <p class="carousel-desc">{{ img.desc }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>

      <!-- 宣传语区域 -->
      <div class="slogan-section">
        <div class="slogan-content">
          <h2 class="main-title">源自田间，鲜达餐桌</h2>
          <p class="subtitle">绿色健康 · 品质生活 · 助力乡村振兴</p>
          <div class="slogan-features">
            <div class="feature-item">
              <i class="el-icon-check feature-icon"></i>
              <span>产地直供</span>
            </div>
            <div class="feature-item">
              <i class="el-icon-check feature-icon"></i>
              <span>品质保证</span>
            </div>
            <div class="feature-item">
              <i class="el-icon-check feature-icon"></i>
              <span>全程冷链</span>
            </div>
            <div class="feature-item">
              <i class="el-icon-check feature-icon"></i>
              <span>售后无忧</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 商品分类区域 -->
    <div class="category-sections">
      <!-- 全部商品 -->
      <section id="all-products" class="product-section">
        <div class="section-header">
          <div class="section-title-wrapper">
            <i class="el-icon-s-goods title-icon"></i>
            <h2 class="section-title">全部商品</h2>
          </div>
          <el-button 
            type="primary" 
            @click="loadMoreProducts" 
            :loading="loadingProducts"
            class="view-more-btn"
          >
            加载更多
            <i class="el-icon-arrow-right"></i>
          </el-button>
        </div>
        <ProductList 
          :products="displayedProducts" 
          @add-order="addOrder" 
          @view-detail="viewDetail" 
          :loading="loadingProducts"
          class="product-list"
        />
        <div v-if="!loadingProducts && displayedProducts.length === 0" class="empty-hint">
          <i class="el-icon-shopping-cart-2"></i>
          <p>暂无商品，正在努力上架中...</p>
        </div>
      </section>

      <!-- 蔬菜类 -->
      <section id="vegetable" class="product-section">
        <div class="section-header">
          <div class="section-title-wrapper">
            <i class="el-icon-s-marketing title-icon"></i>
            <h2 class="section-title">蔬菜类</h2>
            <span class="section-count">({{ vegetableProducts.length }})</span>
          </div>
          <el-button 
            type="success" 
            @click="viewCategory('vegetable')"
            class="view-more-btn"
          >
            查看更多
            <i class="el-icon-arrow-right"></i>
          </el-button>
        </div>
        <ProductList 
          :products="vegetableProducts" 
          @add-order="addOrder" 
          @view-detail="viewDetail"
          class="product-list"
        />
      </section>

      <!-- 水果类 -->
      <section id="fruit" class="product-section">
        <div class="section-header">
          <div class="section-title-wrapper">
            <i class="el-icon-s-goods title-icon"></i>
            <h2 class="section-title">水果类</h2>
            <span class="section-count">({{ fruitProducts.length }})</span>
          </div>
          <el-button 
            type="primary" 
            @click="viewCategory('fruit')"
            class="view-more-btn"
          >
            查看更多
            <i class="el-icon-arrow-right"></i>
          </el-button>
        </div>
        <ProductList 
          :products="fruitProducts" 
          @add-order="addOrder" 
          @view-detail="viewDetail"
          class="product-list"
        />
      </section>

      <!-- 肉类 -->
      <section id="meat" class="product-section">
        <div class="section-header">
          <div class="section-title-wrapper">
            <i class="el-icon-s-custom title-icon"></i>
            <h2 class="section-title">肉类</h2>
            <span class="section-count">({{ meatProducts.length }})</span>
          </div>
          <el-button 
            type="danger" 
            @click="viewCategory('meat')"
            class="view-more-btn"
          >
            查看更多
            <i class="el-icon-arrow-right"></i>
          </el-button>
        </div>
        <ProductList 
          :products="meatProducts" 
          @add-order="addOrder" 
          @view-detail="viewDetail"
          class="product-list"
        />
      </section>

      <!-- 谷物类 -->
      <section id="grain" class="product-section">
        <div class="section-header">
          <div class="section-title-wrapper">
            <i class="el-icon-s-order title-icon"></i>
            <h2 class="section-title">谷物类</h2>
            <span class="section-count">({{ grainProducts.length }})</span>
          </div>
          <el-button 
            type="warning" 
            @click="viewCategory('grain')"
            class="view-more-btn"
          >
            查看更多
            <i class="el-icon-arrow-right"></i>
          </el-button>
        </div>
        <ProductList 
          :products="grainProducts" 
          @add-order="addOrder" 
          @view-detail="viewDetail"
          class="product-list"
        />
      </section>

      <!-- 加工类 -->
      <section id="processed" class="product-section">
        <div class="section-header">
          <div class="section-title-wrapper">
            <i class="el-icon-s-goods title-icon"></i>
            <h2 class="section-title">加工类</h2>
            <span class="section-count">({{ processedProducts.length }})</span>
          </div>
          <el-button 
            type="info" 
            @click="viewCategory('processed')"
            class="view-more-btn"
          >
            查看更多
            <i class="el-icon-arrow-right"></i>
          </el-button>
        </div>
        <ProductList 
          :products="processedProducts" 
          @add-order="addOrder" 
          @view-detail="viewDetail"
          class="product-list"
        />
      </section>
    </div>

    <!-- 优惠券中心 -->
    <section class="coupon-center-section">
      <div class="coupon-center-header">
        <div class="section-title-wrapper">
          <i class="el-icon-present title-icon"></i>
          <h2 class="section-title">优惠券中心</h2>
        </div>
        <el-button 
          type="primary" 
          @click="$router.push('/front/coupons')"
          class="view-more-btn"
        >
          去领券
          <i class="el-icon-arrow-right"></i>
        </el-button>
      </div>
      <div class="coupon-center-content" v-loading="couponLoading">
        <div v-if="couponList.length === 0 && !couponLoading" class="coupon-empty">
          <i class="el-icon-present"></i>
          <p>暂无可领取的优惠券</p>
        </div>
        <div v-else class="coupon-grid">
          <div 
            v-for="coupon in couponList" 
            :key="coupon.id" 
            class="coupon-card"
            :class="{ 'coupon-card-expired': isCouponExpired(coupon) }"
          >
            <div class="coupon-card-left">
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
            <div class="coupon-card-right">
              <h3 class="coupon-name">{{ coupon.name }}</h3>
              <p class="coupon-desc">{{ coupon.description }}</p>
              <div class="coupon-meta">
                <span class="meta-item">
                  <i class="el-icon-time"></i>
                  有效期至：{{ formatDateTime(coupon.endTime) }}
                </span>
                <span class="meta-item">
                  <i class="el-icon-download"></i>
                  已领{{ coupon.receivedNum }}/{{ coupon.totalNum === -1 ? '不限' : coupon.totalNum }}
                </span>
              </div>
              <div class="coupon-action">
                <el-button 
                  :type="coupon.alreadyReceived ? 'info' : 'danger'" 
                  size="small" 
                  :disabled="isCouponExpired(coupon) || (coupon.totalNum !== -1 && coupon.receivedNum >= coupon.totalNum) || coupon.alreadyReceived"
                  @click="handleReceiveCoupon(coupon.id)"
                >
                  {{ getReceiveButtonText(coupon) }}
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 信息区域 -->
    <div class="info-sections">
      <!-- 我的订单 -->
      <section id="my-orders" class="info-section order-section">
        <div class="section-header">
          <div class="section-title-wrapper">
            <i class="el-icon-s-order title-icon"></i>
            <h2 class="section-title">我的订单</h2>
          </div>
          <el-button 
            type="info" 
            @click="viewAllOrders"
            class="view-all-btn"
          >
            查看全部
            <i class="el-icon-arrow-right"></i>
          </el-button>
        </div>
        
        <div class="order-content">
          <div v-if="recentOrders.length > 0">
            <OrderList 
              :orders="recentOrders" 
              @cancel-order="cancelOrder" 
              @view-order="viewOrder"
              :compact="true"
              class="order-list"
            />
          </div>
          
          <div v-else class="empty-state">
            <div class="empty-illustration">
              <svg width="120" height="120" viewBox="0 0 120 120" fill="none">
                <path d="M60 20C38.1 20 20 38.1 20 60C20 81.9 38.1 100 60 100C81.9 100 100 81.9 100 60C100 38.1 81.9 20 60 20ZM60 90C45.6 90 30 85.5 30 70C30 54.5 45.6 50 60 50C74.4 50 90 54.5 90 70C90 85.5 74.4 90 60 90Z" 
                  fill="#F5F5F5"/>
                <circle cx="60" cy="60" r="6" fill="#FF9800"/>
                <path d="M80 40L100 20" stroke="#E0E0E0" stroke-width="2" stroke-linecap="round"/>
                <path d="M20 20L40 40" stroke="#E0E0E0" stroke-width="2" stroke-linecap="round"/>
                <path d="M20 100L40 80" stroke="#E0E0E0" stroke-width="2" stroke-linecap="round"/>
                <path d="M100 100L80 80" stroke="#E0E0E0" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <p class="empty-text">暂无订单，快去选购商品吧！</p>
            <el-button 
              type="warning" 
              size="small" 
              @click="$router.push('/front/products')"
              class="go-shopping-btn"
            >
              <i class="el-icon-shopping-cart-2"></i>
              去购物
            </el-button>
          </div>
          
          <!-- 订单统计 -->
          <div v-if="recentOrders.length > 0" class="order-stats">
            <div class="stat-item">
              <div class="stat-value">{{ orders.length }}</div>
              <div class="stat-label">全部订单</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-value">{{ pendingOrders }}</div>
              <div class="stat-label">待处理</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-value">{{ completedOrders }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 最新咨询 -->
      <section id="latest-news" class="info-section news-section">
        <div class="section-header">
          <div class="section-title-wrapper">
            <i class="el-icon-s-promotion title-icon news-title-icon"></i>
            <h2 class="section-title news-title">最新咨询</h2>
          </div>
          <el-button 
            type="text" 
            @click="viewAllNews"
            class="view-more-link"
          >
            <span>查看更多</span>
            <i class="el-icon-arrow-right"></i>
          </el-button>
        </div>
        
        <div class="news-content" v-loading="loadingNews">
          <div class="news-list" v-if="newsList.length > 0">
            <div v-for="(n, idx) in newsList" :key="n.id || idx" class="news-item">
              <div class="news-header">
                <div class="news-index">{{ formatIndex(idx + 1) }}</div>
                <h3 class="news-title-text">{{ n.title }}</h3>
              </div>
              <div class="news-meta">
                <div class="news-time">{{ formatDate(n.createTime) }}</div>
                <div class="news-source">{{ getCategoryText(n.category) }}</div>
              </div>
              <div class="news-actions">
                <el-button 
                  type="text" 
                  @click="goDetail(n.id)"
                  class="detail-btn"
                >
                  <i class="el-icon-document"></i>
                  查看详情
                </el-button>
              </div>
            </div>
          </div>
          
          <div v-else-if="!loadingNews" class="empty-state">
            <i class="el-icon-document" style="font-size: 48px; color: #ccc;"></i>
            <p class="empty-text">暂无资讯</p>
          </div>
          
          <!-- 资讯统计 -->
          <div class="news-stats" v-if="newsList.length > 0">
            <div class="stat-item">
              <i class="el-icon-document stat-icon"></i>
              <div class="stat-content">
                <div class="stat-value">{{ newsList.length }}</div>
                <div class="stat-label">资讯数量</div>
              </div>
            </div>
            <div class="stat-item">
              <i class="el-icon-time stat-icon"></i>
              <div class="stat-content">
                <div class="stat-value">{{ getLatestNewsTime() }}</div>
                <div class="stat-label">最新发布</div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <Footer />
  </div>
</template>

<script>
import Footer from "@/components/Footer"
import ProductList from "@/components/ProductList"
import OrderList from "@/components/OrderList"
import { getProductPageData } from '@/data/products'
import { getNewsPage } from '@/api'
import { getUserOrders } from '@/data/orders'
import { addToCart as addToCartApi } from '@/data/cart'
import { getCurrentUser } from '@/data/user'

export default {
  name: 'HomePage',
  components: { Footer, ProductList, OrderList },
  data() {
    return {
      // 轮播图数据
      carouselImages: [
        {
          url: '/imgs/foods/1.png',
          alt: '新鲜蔬菜',
          title: '新鲜农产品直供',
          desc: '每日新鲜采摘，从田间直达餐桌'
        },
        {
          url: '/imgs/foods/2.png',
          alt: '优质水果',
          title: '有机健康食品',
          desc: '无农药、无添加，享受自然美味'
        },
        {
          url: '/imgs/foods/3.png',
          alt: '农家产品',
          title: '乡村振兴助农',
          desc: '支持农业发展，助力乡村振兴'
        }
      ],
      // 商品数据
      allProducts: [],
      displayedProducts: [],
      loadingProducts: false,
      // 新闻数据
      newsList: [],
      loadingNews: false,
      // 订单数据
      orders: [],
      // 优惠券数据
      couponList: [],
      couponLoading: false
    }
  },
  computed: {
    // 蔬菜类分类（匹配 agricultural 和 vegetable）
    vegetableProducts() {
      return this.allProducts.filter(p => ['vegetable', 'agricultural'].includes(p.category)).slice(0, 4)
    },
    // 水果类分类（匹配 fruit）
    fruitProducts() {
      return this.allProducts.filter(p => p.category === 'fruit').slice(0, 4)
    },
    // 肉类分类（匹配 meat 和 livestock）
    meatProducts() {
      return this.allProducts.filter(p => ['meat', 'livestock'].includes(p.category)).slice(0, 4)
    },
    // 谷物类分类（匹配 grain）
    grainProducts() {
      return this.allProducts.filter(p => p.category === 'grain').slice(0, 4)
    },
    // 加工类分类（匹配 processed）
    processedProducts() {
      return this.allProducts.filter(p => p.category === 'processed').slice(0, 4)
    },
    // 最近的订单
    recentOrders() {
      return this.orders.slice(0, 3)
    },
    // 待处理订单
    pendingOrders() {
      return this.orders.filter(order =>
        ['PENDING', 'PAID', 'processing', 'shipping'].includes(order.status)
      ).length
    },
    // 已完成订单
    completedOrders() {
      return this.orders.filter(order => order.status === 'DELIVERED' || order.status === 'completed').length
    }
  },
  created() {
    this.initData()
  },
  methods: {
    async initData() {
      await Promise.all([
        this.loadProducts(),
        this.loadNews(),
        this.loadOrders(),
        this.loadCoupons()
      ])
    },

    // 模拟商品数据（与后端分类值一致）
    getMockProducts() {
      return [
        // 农产品类 (agricultural)
        { id: 1, name: '有机黄瓜 2斤', price: 12, originalPrice: 15, category: 'agricultural', description: '新鲜采摘，绿色无公害', image: 'https://images.unsplash.com/photo-14455251128-7c7208b6f1e6?w=400', isHot: true, stock: 100 },
        { id: 2, name: '有机西红柿 3斤', price: 18, originalPrice: 22, category: 'agricultural', description: '自然成熟，酸甜可口', image: 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=400', isNew: true, stock: 80 },
        { id: 3, name: '绿色菠菜 1斤', price: 8, originalPrice: 10, category: 'agricultural', description: '新鲜采摘，叶嫩茎脆', image: 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400', stock: 120 },
        { id: 4, name: '新鲜土豆 5斤', price: 16, originalPrice: 19, category: 'agricultural', description: '农家种植，口感绵软', image: 'https://images.unsplash.com/photo-1518977676601-35145318228c?w=400', stock: 90 },
        { id: 5, name: '优质大米 5kg', price: 68, originalPrice: 78, category: 'agricultural', description: '稻花香米，颗粒饱满', image: 'https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?w=400', isHot: true, stock: 30 },
        // 水果类
        { id: 6, name: '红富士苹果 3斤', price: 25, originalPrice: 30, category: 'fruit', description: '脆甜多汁，口感细腻', image: 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400', isHot: true, isNew: true, stock: 200 },
        { id: 7, name: '新鲜草莓 1盒', price: 38, originalPrice: 45, category: 'fruit', description: '红颜草莓，甜蜜多汁', image: 'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400', isHot: true, isNew: true, stock: 40 },
        { id: 8, name: '阳光玫瑰葡萄 2斤', price: 45, originalPrice: 55, category: 'fruit', description: '甜度高，口感好', image: 'https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=400', isHot: true, isNew: true, stock: 60 },
        { id: 9, name: '麒麟西瓜 1个', price: 35, originalPrice: 45, category: 'fruit', description: '皮薄瓤红，甜度高', image: 'https://images.unsplash.com/photo-1589984662646-e7b2e4962f18?w=400', isHot: true, stock: 150 },
        { id: 10, name: '赣南脐橙 5斤', price: 40, originalPrice: 48, category: 'fruit', description: '汁多味甜，维C丰富', image: 'https://images.unsplash.com/photo-1547514701-42782101795e?w=400', isHot: true, isNew: true, stock: 120 },
        // 畜禽产品类 (livestock)
        { id: 11, name: '生态土鸡蛋 30枚', price: 45, originalPrice: 55, category: 'livestock', description: '农家散养，营养丰富', image: 'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400', isHot: true, isNew: true, stock: 50 },
        { id: 12, name: '农家土鸡 1只', price: 88, originalPrice: 98, category: 'livestock', description: '散养土鸡，肉质紧实', image: 'https://images.unsplash.com/photo-1548550023-2bdb3c5beed7?w=400', isHot: true, stock: 15 },
        { id: 13, name: '土猪肉 2斤', price: 96, originalPrice: 116, category: 'livestock', description: '散养土猪，肉质鲜美', image: 'https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=400', isHot: true, stock: 25 },
        { id: 14, name: '散养羊肉 2斤', price: 156, originalPrice: 176, category: 'livestock', description: '草原散养，无膻味', image: 'https://images.unsplash.com/photo-1603048297172-c92544798d5a?w=400', isHot: true, stock: 18 },
        { id: 15, name: '散养牛肉 2斤', price: 176, originalPrice: 196, category: 'livestock', description: '草原放养，肉质鲜嫩', image: 'https://images.unsplash.com/photo-1603048719539-9ecb4aa395e3?w=400', isHot: true, stock: 15 },
        // 粮油类
        { id: 16, name: '有机小米 2kg', price: 38, originalPrice: 45, category: 'grain', description: '农家小米，营养丰富', image: 'https://images.unsplash.com/photo-1574634534894-89d7576c8259?w=400', isNew: true, stock: 80 },
        { id: 17, name: '新鲜玉米 5个', price: 24, originalPrice: 28, category: 'grain', description: '甜玉米，颗粒饱满', image: 'https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=400', stock: 60 },
        { id: 18, name: '手工挂面 1kg', price: 24, originalPrice: 30, category: 'grain', description: '传统手工拉面，筋道爽滑', image: 'https://images.unsplash.com/photo-1552611052-33e04de1b100?w=400', isNew: true, stock: 80 },
        { id: 19, name: '农家花生酱 500g', price: 30, originalPrice: 36, category: 'grain', description: '纯花生制作，香浓醇厚', image: 'https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=400', stock: 60 },
        { id: 20, name: '有机大豆油 5L', price: 88, originalPrice: 98, category: 'grain', description: '非转基因大豆，物理压榨', image: 'https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=400', isHot: true, stock: 40 },
        // 加工产品类
        { id: 21, name: '纯天然蜂蜜 500g', price: 88, originalPrice: 98, category: 'processed', description: '深山纯蜂蜜，无添加', image: 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=400', isHot: true, stock: 35 },
        { id: 22, name: '红薯粉条 500g', price: 25, originalPrice: 30, category: 'processed', description: '纯红薯制作，口感Q弹', image: 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=400', stock: 50 },
        { id: 23, name: '纯正香油 500ml', price: 35, originalPrice: 42, category: 'processed', description: '传统小磨香油，香气浓郁', image: 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400', isHot: true, stock: 40 },
        { id: 24, name: '农家辣椒酱 200g', price: 24, originalPrice: 30, category: 'processed', description: '新鲜辣椒制作，香辣开胃', image: 'https://images.unsplash.com/photo-1588195538326-c5b1e9f80a1b?w=400', stock: 90 },
        { id: 25, name: '农家豆瓣酱 500g', price: 40, originalPrice: 50, category: 'processed', description: '传统发酵，酱香浓郁', image: 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400', isHot: true, stock: 55 },
      ]
    },

    // 加载商品数据
    async loadProducts() {
      this.loadingProducts = true
      try {
        const result = await getProductPageData({ page: 1, pageSize: 100 })
        let products = result.list || []

        // 如果没有数据或数据量太少（少于各类别商品总和），使用模拟数据
        if (products.length === 0 || products.length < 20) {
          console.warn('后端数据不足，使用模拟数据')
          products = this.getMockProducts()
        }

        // 打印调试信息
        console.log('加载的商品数据:', products)
        console.log('商品分类统计:', this.getCategoryStats(products))

        this.allProducts = products
        this.displayedProducts = products.slice(0, 8)
      } catch (error) {
        console.error('加载商品数据失败:', error)
        this.allProducts = this.getMockProducts()
        this.displayedProducts = this.getMockProducts().slice(0, 8)
      } finally {
        this.loadingProducts = false
      }
    },

    // 获取分类统计
    getCategoryStats(products) {
      const stats = { vegetable: 0, fruit: 0, meat: 0, grain: 0, processed: 0, other: 0 }
      products.forEach(p => {
        const cat = p.category || 'other'
        if (stats[cat] !== undefined) {
          stats[cat]++
        }
      })
      return stats
    },

    // 加载新闻数据
    async loadNews() {
      this.loadingNews = true
      try {
        const res = await getNewsPage({
          page: 1,
          pageSize: 5
        })
        
        // 处理响应格式：拦截器返回 {code, data: {list, total}} 或 {list, total}
        let result = res
        if (res && res.data) {
          result = res.data
        }
        this.newsList = result.list || result.records || []
      } catch {
        this.newsList = []
      } finally {
        this.loadingNews = false
      }
    },

    // 加载订单数据
    async loadOrders() {
      try {
        const user = getCurrentUser()
        if (user && user.id) {
          const result = await getUserOrders()
          this.orders = result || []
        }
      } catch {
        this.orders = []
      }
    },

    // 添加到购物车
    async addOrder(product) {
      if (product.stock <= 0) {
        this.$message.error('该商品暂时缺货')
        return
      }
      try {
        await addToCartApi({ productId: product.id, quantity: 1 })
        this.$message.success(`已添加 ${product.name} 到购物车`)
      } catch (error) {
        this.$message.warning(error.message || '添加失败，请先登录')
      }
    },

    // 取消订单
    async cancelOrder(order) {
      try {
        await this.$confirm('确定要取消这个订单吗？', '取消订单', {
          confirmButtonText: '确定取消',
          cancelButtonText: '再想想',
          type: 'warning',
          center: true
        })
        
        // 调用后端API取消订单
        const { cancelOrder } = await import('@/data/orders')
        await cancelOrder(order.id)
        
        this.$message.success('订单已取消')
        this.loadOrders()
      } catch {
        // user cancelled
      }
    },

    goDetail(id) {
      if (id) {
        this.$router.push(`/front/news/${id}`)
      }
    },
    
    loadMoreProducts() {
      this.loadingProducts = true
      
      setTimeout(() => {
        const loadedCount = this.displayedProducts.length
        const nextBatch = this.allProducts.slice(loadedCount, loadedCount + 4)
        
        if (nextBatch.length > 0) {
          this.displayedProducts = [...this.displayedProducts, ...nextBatch]
          this.$message.success(`加载了 ${nextBatch.length} 个商品`)
        } else {
          this.$message.info('已加载全部商品')
        }
        
        this.loadingProducts = false
      }, 500)
    },
    
    viewCategory(category) {
      this.$router.push(`/front/products?category=${category}`)
    },

    viewDetail(id) {
      this.$router.push(`/front/product/${id}`)
    },

    viewOrder() {
      this.$router.push('/front/my-orders')
    },

    viewAllOrders() {
      this.$router.push('/front/my-orders')
    },
    
    viewAllNews() {
      this.$router.push('/front/news')
    },
    
    formatIndex(index) {
      return index < 10 ? '0' + index : index.toString()
    },
    
    formatDate(date) {
      if (!date) return '暂无'
      const d = new Date(date)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    
    getCategoryText(category) {
      const categories = [
        { value: 'policy', label: '政策解读' },
        { value: 'technology', label: '技术培训' },
        { value: 'farming', label: '农事指南' },
        { value: 'ecommerce', label: '电商发展' },
        { value: 'finance', label: '农村金融' },
        { value: 'safety', label: '质量安全' },
        { value: 'tourism', label: '乡村旅游' },
        { value: 'marketing', label: '品牌建设' }
      ]
      const found = categories.find(c => c.value === category)
      return found ? found.label : category || '其他'
    },
    
    getLatestNewsTime() {
      if (!this.newsList || this.newsList.length === 0) return '暂无'
      const latest = this.newsList[0]
      return this.formatDate(latest.createTime)
    },

    async loadCoupons() {
      this.couponLoading = true
      try {
        const request = await import('@/utils/request')
        const res = await request.default({
          url: '/coupon/list',
          method: 'get'
        })
        if (res.code === 200 && res.data) {
          this.couponList = res.data.slice(0, 6)
        }
      } catch (err) {
        console.error('获取优惠券列表失败:', err)
        this.couponList = []
      } finally {
        this.couponLoading = false
      }
    },

    async handleReceiveCoupon(couponId) {
      const user = getCurrentUser()
      if (!user || !user.id) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }
      try {
        const request = await import('@/utils/request')
        const res = await request.default({
          url: `/coupon/receive/${couponId}`,
          method: 'post'
        })
        if (res.code === 200) {
          this.$message.success('领取成功')
          this.loadCoupons()
        } else {
          this.$message.warning(res.message || '领取失败')
        }
      } catch (err) {
        this.$message.error('领取失败')
      }
    },

    isCouponExpired(coupon) {
      if (!coupon.endTime) return false
      return new Date(coupon.endTime) < new Date()
    },

    getReceiveButtonText(coupon) {
      if (this.isCouponExpired(coupon)) return '已过期'
      if (coupon.alreadyReceived) return '已领取'
      if (coupon.totalNum !== -1 && coupon.receivedNum >= coupon.totalNum) return '已领完'
      return '立即领取'
    },

    formatDateTime(time) {
      if (!time) return '-'
      const date = new Date(time)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    }
  }
}
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  animation: fade-in 0.6s ease;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 轮播图区域 */
.carousel-section {
  margin-bottom: 30px;
}

.home-carousel {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.carousel-item {
  position: relative;
  width: 100%;
  height: 400px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-error,
.image-loading {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;
  font-size: 16px;
  gap: 8px;
}

.carousel-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  padding: 30px 40px;
  color: white;
}

.carousel-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.carousel-desc {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

/* 宣传语区域 */
.slogan-section {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  padding: 30px 40px;
  text-align: center;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.slogan-content {
  max-width: 800px;
  margin: 0 auto;
}

.main-title {
  color: #27ae60;
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px 0;
  position: relative;
  display: inline-block;
}

.main-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(to right, #27ae60, #2ecc71);
  border-radius: 2px;
}

.subtitle {
  color: #666;
  font-size: 16px;
  margin: 0 0 20px 0;
  font-weight: 500;
}

.slogan-features {
  display: flex;
  justify-content: center;
  gap: 30px;
  flex-wrap: wrap;
  margin-top: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #333;
  font-weight: 500;
  padding: 8px 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
}

.feature-item:hover {
  border-color: #27ae60;
  color: #27ae60;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(39, 174, 96, 0.1);
}

.feature-icon {
  color: #27ae60;
  font-size: 16px;
  font-weight: bold;
}

/* 商品分类区域 */
.category-sections {
  display: flex;
  flex-direction: column;
  gap: 30px;
  margin-bottom: 30px;
}

.product-section {
  background: white;
  border-radius: 12px;
  padding: 25px 30px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

/* 区块头部通用样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 24px;
  color: #ff9800;
  background: rgba(255, 152, 0, 0.1);
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-count {
  font-size: 14px;
  color: #999;
  font-weight: 500;
  margin-left: 4px;
}

/* 查看按钮样式 */
.view-more-btn,
.view-all-btn {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  height: auto;
}

.view-more-btn:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 产品列表 */
.product-list {
  margin-bottom: 0;
}

.empty-hint {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 15px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-hint i {
  font-size: 32px;
  opacity: 0.6;
}

/* 优惠券中心 */
.coupon-center-section {
  background: #fafafa;
  border-radius: 16px;
  padding: 28px 32px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 32px;
  border-top: 3px solid #52c41a;
}

.coupon-center-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.coupon-center-header .title-icon {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.08);
}

.coupon-center-content {
  min-height: 100px;
}

.coupon-empty {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.coupon-empty i {
  font-size: 48px;
  color: #ddd;
  margin-bottom: 10px;
}

.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 16px;
}

.coupon-card {
  display: flex;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  overflow: hidden;
  transition: all 0.3s;
  background: #fff;
  position: relative;
}

.coupon-card:hover {
  box-shadow: 0 4px 16px rgba(82, 196, 26, 0.12);
  transform: translateY(-2px);
  border-color: #52c41a;
}

.coupon-card-expired {
  opacity: 0.5;
  filter: grayscale(0.8);
}

.coupon-card-left {
  width: 120px;
  background: linear-gradient(135deg, #f6ffed 0%, #e6f7ff 100%);
  border-right: 1px dashed #d9d9d9;
  color: #52c41a;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px;
  flex-shrink: 0;
  position: relative;
}

.coupon-card-left::after {
  content: '';
  position: absolute;
  right: -6px;
  top: 50%;
  transform: translateY(-50%);
  width: 12px;
  height: 12px;
  background: #fafafa;
  border-radius: 50%;
  border: 1px solid #e8e8e8;
}

.coupon-card-expired .coupon-card-left {
  background: linear-gradient(135deg, #f5f5f5 0%, #fafafa 100%);
  color: #999;
  border-right-color: #d9d9d9;
}

.coupon-amount {
  display: flex;
  align-items: baseline;
}

.coupon-amount .currency {
  font-size: 14px;
  font-weight: 600;
}

.coupon-amount .value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.coupon-condition {
  margin-top: 6px;
  font-size: 11px;
  opacity: 0.8;
  color: #666;
}

.coupon-card-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 16px;
}

.coupon-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 6px 0;
}

.coupon-desc {
  font-size: 12px;
  color: #666;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.coupon-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.meta-item {
  font-size: 11px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 3px;
}

.coupon-action {
  text-align: right;
}

@media (max-width: 768px) {
  .coupon-grid {
    grid-template-columns: 1fr;
  }
  
  .coupon-card-left {
    width: 100px;
  }
  
  .coupon-amount .value {
    font-size: 24px;
  }
}

/* 信息区域 */
.info-sections {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

@media (max-width: 992px) {
  .info-sections {
    grid-template-columns: 1fr;
  }
}

.info-section {
  background: #fafafa;
  border-radius: 16px;
  padding: 28px 32px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.info-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

/* 订单区域 */
.order-section {
  border-top: 3px solid #52c41a;
}

.order-content {
  min-height: 200px;
}

.order-list {
  margin-bottom: 20px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  text-align: center;
  gap: 15px;
}

.empty-illustration {
  opacity: 0.8;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.empty-text {
  font-size: 15px;
  color: #666;
  margin: 0;
}

.go-shopping-btn {
  border-radius: 8px;
  background: linear-gradient(135deg, #ff9800, #ff5722);
  border: none;
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
}

.go-shopping-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.4);
  background: linear-gradient(135deg, #ff5722, #ff9800);
}

/* 订单统计 */
.order-stats {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px 20px;
  border: 1px solid #e9ecef;
  gap: 20px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #52c41a;
  margin-bottom: 4px;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #dee2e6;
}

/* 最新咨询区域 */
.news-section {
  border-top: 3px solid #52c41a;
}

.news-title-icon {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.08);
}

.news-title {
  color: #52c41a;
  background: linear-gradient(135deg, #52c41a, #389e0d);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.view-more-link {
  color: #52c41a;
  font-weight: 600;
  font-size: 14px;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.view-more-link:hover {
  color: #389e0d;
  transform: translateX(4px);
}

.view-more-link i {
  font-size: 12px;
  transition: transform 0.3s ease;
}

.view-more-link:hover i {
  transform: translateX(4px);
}

.news-content {
  min-height: 200px;
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.news-item {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.news-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 3px;
  height: 100%;
  background: linear-gradient(to bottom, #52c41a, #73d13d);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.news-item:hover {
  border-color: #52c41a;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.1);
  transform: translateY(-1px);
}

.news-item:hover::before {
  opacity: 1;
}

.news-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.news-index {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #52c41a, #73d13d);
  color: white;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(82, 196, 26, 0.2);
}

.news-title-text {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex: 1;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.news-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 12px;
  color: #999;
  padding-left: 34px;
}

.news-time {
  background: #f5f5f5;
  padding: 3px 8px;
  border-radius: 4px;
  color: #999;
  font-weight: 500;
}

.news-source {
  background: #f6ffed;
  color: #52c41a;
  padding: 3px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.news-actions {
  padding-left: 34px;
}

.detail-btn {
  color: #999;
  font-size: 12px;
  padding: 4px 0;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.detail-btn:hover {
  color: #52c41a;
  transform: translateX(4px);
}

.detail-btn i {
  font-size: 12px;
}

.news-stats {
  display: flex;
  gap: 16px;
  background: #f5f5f5;
  border-radius: 8px;
  padding: 12px 16px;
  border: 1px solid #e8e8e8;
}

.news-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  text-align: left;
  flex: 1;
}

.news-stats .stat-icon {
  width: 36px;
  height: 36px;
  background: #52c41a;
  color: white;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.news-stats .stat-content {
  flex: 1;
}

.news-stats .stat-value {
  font-size: 16px;
  color: #333;
  margin-bottom: 2px;
}

.news-stats .stat-label {
  font-size: 11px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-page {
    padding: 12px;
  }
  
  .carousel-overlay {
    padding: 20px;
  }
  
  .carousel-title {
    font-size: 20px;
  }
  
  .carousel-desc {
    font-size: 14px;
  }
  
  .slogan-section {
    padding: 20px;
  }
  
  .main-title {
    font-size: 24px;
  }
  
  .slogan-features {
    gap: 10px;
  }
  
  .feature-item {
    padding: 6px 12px;
    font-size: 13px;
  }
  
  .product-section,
  .info-section {
    padding: 20px;
  }
  
  .section-title {
    font-size: 18px;
  }
  
  .title-icon {
    width: 36px;
    height: 36px;
    font-size: 20px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .view-more-btn,
  .view-all-btn,
  .view-more-link {
    align-self: flex-end;
  }
  
  .news-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .news-source {
    align-self: flex-start;
  }
  
  .news-stats {
    flex-direction: column;
    gap: 15px;
  }
  
  .news-stats .stat-item {
    justify-content: center;
  }
  
  .order-stats {
    flex-direction: column;
    gap: 15px;
  }
  
  .stat-divider {
    width: 100%;
    height: 1px;
  }
}
</style>

<style>
/* 轮播图全局样式 */
.home-carousel .el-carousel__arrow {
  background: rgba(255, 255, 255, 0.8);
  color: #333;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.home-carousel .el-carousel__arrow:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.home-carousel .el-carousel__indicators {
  bottom: -30px;
}

.home-carousel .el-carousel__indicator {
  padding: 8px 4px;
}

.home-carousel .el-carousel__indicator .el-carousel__button {
  width: 6px;
  height: 6px;
  border-radius: 3px;
  background: #ccc;
  transition: all 0.3s ease;
}

.home-carousel .el-carousel__indicator.is-active .el-carousel__button {
  width: 20px;
  background: #27ae60;
}
</style>
