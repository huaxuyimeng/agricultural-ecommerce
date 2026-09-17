/**
 * 我的商品页面
 * 文件路径: src/views/front/merchant/MyProducts.vue
 * 功能描述: 商家商品管理页面，展示商品列表、审核状态、库存和销售情况，支持编辑和删除操作
 * 关联文件:
 * - src/views/front/merchant/PublishProduct.vue: 发布商品页面
 * - src/views/front/merchant/OrderManage.vue: 订单管理页面
 * - src/views/front/merchant/Dashboard.vue: 商家中心页面
 */
<template>
  <div class="merchant-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <h1 class="page-title">我的商品</h1>
          <p class="page-subtitle">管理您发布的商品，查看审核状态，实时掌握库存和销售情况</p>
        </div>
        
        <!-- 操作按钮 -->
        <div class="header-actions">
          <el-button 
            type="primary" 
            @click="$router.push('/front/merchant/publish')"
            class="publish-btn"
          >
            <i class="el-icon-plus"></i>
            发布新商品
          </el-button>
        </div>
      </div>
      
      <!-- 统计信息 -->
      <div class="stats-cards">
        <div class="stat-card total">
          <div class="stat-icon">
            <i class="el-icon-s-goods"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ productStats.total }}</div>
            <div class="stat-label">商品总数</div>
          </div>
        </div>
        
        <div class="stat-card pending">
          <div class="stat-icon">
            <i class="el-icon-time"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ productStats.pending }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </div>
        
        <div class="stat-card approved">
          <div class="stat-icon">
            <i class="el-icon-check"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ productStats.approved }}</div>
            <div class="stat-label">已上架</div>
          </div>
        </div>
        
        <div class="stat-card rejected">
          <div class="stat-icon">
            <i class="el-icon-close"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ productStats.rejected }}</div>
            <div class="stat-label">已拒绝</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <el-card shadow="never" class="table-card">
      <!-- 搜索和筛选 -->
      <div class="table-tools">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品名称或描述..."
            clearable
            @keyup.enter="handleSearch"
            @clear="clearSearch"
            class="search-input"
          >
            <template #prefix>
              <i class="el-icon-search"></i>
            </template>
          </el-input>
        </div>
        
        <div class="filter-box">
          <el-select
            v-model="statusFilter"
            placeholder="全部状态"
            clearable
            @change="handleFilterChange"
            class="status-filter"
          >
            <el-option label="全部" value="" />
            <el-option label="待审核" value="pending" />
            <el-option label="已上架" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </div>
      </div>

      <!-- 商品表格 -->
      <el-table 
        :data="filteredProducts" 
        v-loading="loading"
        border
        stripe
        class="product-table"
        :default-sort="{prop: 'createTime', order: 'descending'}"
      >
        <el-table-column 
          label="商品信息" 
          min-width="300"
        >
          <template #default="scope">
            <div class="product-info">
              <div class="table-image-wrapper">
                <img
                  :src="getImageUrl(scope.row.image)"
                  class="table-image"
                  :alt="scope.row.name"
                  @error="handleImageError"
                  @click="previewImage(scope.row.image)"
                />
              </div>
              <div class="product-details">
                <h4 class="product-name">{{ scope.row.name }}</h4>
                <p class="product-desc">{{ scope.row.description || scope.row.desc || '暂无描述' }}</p>
                <div class="product-tags">
                  <el-tag 
                    :type="scope.row.category === 'agricultural' ? 'success' : 'warning'"
                    size="small"
                    class="category-tag"
                  >
                    {{ getCategoryText(scope.row.category) }}
                  </el-tag>
                  <el-tag 
                    :type="getStatusTagType(scope.row.status)"
                    size="small"
                    class="status-tag"
                  >
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column 
          prop="price" 
          label="价格" 
          width="120"
          sortable
        >
          <template #default="scope">
            <span class="price-text">¥{{ formatPrice(scope.row.price) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column 
          prop="stock" 
          label="库存" 
          width="100"
          sortable
        >
          <template #default="scope">
            <el-tag
              :type="getStockTagType(scope.row.stock)"
              class="stock-tag"
            >
              {{ scope.row.stock !== undefined && scope.row.stock !== null ? scope.row.stock : 0 }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column 
          prop="viewCount" 
          label="浏览" 
          width="80"
        >
          <template #default="scope">
            <span class="view-count">{{ scope.row.viewCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column 
          prop="soldCount" 
          label="销量" 
          width="80"
        >
          <template #default="scope">
            <span class="sold-count">{{ scope.row.soldCount || 0 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column 
          label="审核反馈" 
          width="200"
        >
          <template #default="scope">
            <div v-if="scope.row.status === 'rejected'" class="reject-reason">
              <p class="reason-title">拒绝原因：</p>
              <p class="reason-text">{{ scope.row.rejectReason || '未说明原因' }}</p>
            </div>
            <div v-else-if="scope.row.status === 'approved'" class="approve-info">
              <p>审核时间：{{ formatTime(scope.row.approveTime) }}</p>
            </div>
            <span v-else class="no-feedback">暂无反馈</span>
          </template>
        </el-table-column>
        
        <el-table-column 
          label="操作" 
          width="180"
          fixed="right"
        >
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="small" 
                @click="editProduct(scope.row.id)"
                :disabled="scope.row.status === 'approved'"
                class="edit-btn"
              >
                <i class="el-icon-edit"></i>
                编辑
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="remove(scope.row.id)"
                class="delete-btn"
              >
                <i class="el-icon-delete"></i>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="!filteredProducts.length && !loading" class="empty-state">
        <div class="empty-illustration">
          <i class="el-icon-s-goods" style="font-size: 60px; color: #c0c4cc;"></i>
        </div>
        <h3 class="empty-title">暂无商品</h3>
        <p class="empty-description">您还没有发布任何商品，点击下方按钮开始发布</p>
        <el-button 
          type="primary" 
          @click="$router.push('/front/merchant/publish')"
          class="publish-first-btn"
        >
          <i class="el-icon-plus"></i>
          发布第一个商品
        </el-button>
      </div>
    </el-card>

    <!-- 确认删除对话框 -->
    <el-dialog
      title="确认删除"
      :visible.sync="deleteDialogVisible"
      width="400px"
      center
      :append-to-body="true"
    >
      <div class="delete-dialog-content">
        <i class="el-icon-warning" style="font-size: 40px; color: #f56c6c;"></i>
        <p style="margin: 15px 0; text-align: center;">
          确定要删除商品"{{ selectedProductName }}"吗？
        </p>
        <p style="color: #999; font-size: 12px; text-align: center;">
          删除后无法恢复，请谨慎操作
        </p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button 
            type="danger" 
            @click="confirmDelete"
            :loading="deleting"
          >
            确认删除
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getMerchantProductPage, deleteProduct } from '@/api'

export default {
  name: 'MyProducts',
  data() {
    return {
      products: [],
      loading: false,
      searchKeyword: '',
      statusFilter: '',
      deleteDialogVisible: false,
      deleting: false,
      selectedProductId: null,
      selectedProductName: ''
    }
  },
  computed: {
    currentUser() {
      return JSON.parse(localStorage.getItem('xm-user') || '{}')
    },
    
    productStats() {
      const total = this.products.length
      const pending = this.products.filter(p => p.status === 'pending').length
      const approved = this.products.filter(p => p.status === 'approved').length
      const rejected = this.products.filter(p => p.status === 'rejected').length
      
      return { total, pending, approved, rejected }
    },
    
    filteredProducts() {
      let filtered = [...this.products]
      
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(item => 
          (item.name && item.name.toLowerCase().includes(keyword)) ||
          (item.description && item.description.toLowerCase().includes(keyword))
        )
      }
      
      if (this.statusFilter) {
        filtered = filtered.filter(item => item.status === this.statusFilter)
      }
      
      return filtered
    }
  },
  mounted() {
    this.load()
  },
  methods: {
    async load() {
      if (!this.currentUser.id) {
        this.products = []
        return
      }

      this.loading = true
      try {
        const res = await getMerchantProductPage({ page: 1, pageSize: 100, status: this.statusFilter || undefined })
        const resData = (res && res.data) ? res.data : res
        this.products = (resData && resData.list) ? resData.list : (resData && resData.records) ? resData.records : []
      } catch {
        this.products = []
      } finally {
        this.loading = false
      }
    },
    
    formatPrice(price) {
      const num = Number(price)
      return isNaN(num) ? '0.00' : num.toFixed(2)
    },
    
    getStockTagType(stock) {
      const num = Number(stock)
      if (num > 10) return 'success'
      if (num > 0) return 'warning'
      return 'danger'
    },
    
    getStatusTagType(status) {
      const types = {
        'pending': 'warning',
        'approved': 'success',
        'rejected': 'danger'
      }
      return types[status] || 'info'
    },
    
    getStatusText(status) {
      const texts = {
        'pending': '待审核',
        'approved': '已上架',
        'rejected': '已拒绝'
      }
      return texts[status] || '未知'
    },
    
    formatTime(time) {
      if (!time) return '未知'
      const date = new Date(time)
      return date.toLocaleDateString()
    },
    
    handleSearch() {
    },
    
    clearSearch() {
      this.searchKeyword = ''
    },
    
    handleFilterChange() {
      this.load()
    },
    
    editProduct(id) {
      this.$router.push(`/front/merchant/publish?id=${id}`)
    },
    
    remove(id) {
      const product = this.products.find(p => p.id === id)
      if (product) {
        this.selectedProductId = id
        this.selectedProductName = product.name
        this.deleteDialogVisible = true
      }
    },
    
    async confirmDelete() {
      this.deleting = true
      try {
        await deleteProduct(this.selectedProductId)
        this.$message.success('删除成功')
        await this.load()
      } catch {
        this.$message.error('删除失败')
      } finally {
        this.deleting = false
        this.deleteDialogVisible = false
        this.selectedProductId = null
        this.selectedProductName = ''
      }
    },
    
    getImageUrl(image) {
      if (!image) return ''
      if (image.startsWith('data:image') || image.startsWith('http')) return image
      if (image.startsWith('imgs/') || image.startsWith('/imgs/')) {
        const path = image.startsWith('/') ? image.substring(1) : image
        return `/${path}`
      }
      if (image.startsWith('upload/') || image.startsWith('/upload/')) {
        const path = image.startsWith('/') ? image.substring(1) : image
        return `/${path}`
      }
      return `/imgs/${image}`
    },
    
    previewImage(image) {
      const url = this.getImageUrl(image)
      if (url) {
        window.open(url, '_blank')
      }
    },
    
    getCategoryText(category) {
      const map = {
        'agricultural': '农产品',
        'livestock': '畜牧产品',
        'processed': '加工产品',
        'grain': '粮食',
        'vegetable': '蔬菜',
        'fruit': '水果',
        'meat': '肉类',
        'other': '其他'
      }
      return map[category] || category || '未知'
    },
    
    handleImageError(e) {
      const img = e.target
      if (!img.dataset.fallback) {
        img.dataset.fallback = 'true'
        img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHZpZXdCb3g9IjAgMCA2MCA2MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIGZpbGw9IiNGMUY1RjkiLz48cGF0aCBkPSJNMjAgMjVDMjAgMjIuNzkgMjEuNzkgMjEgMjQgMjFIMzZDMzguMjEgMjEgNDAgMjIuNzkgNDAgMjVWMzVDMzggMzUgMjIgMzUgMjAgMzVWMjVaIiBmaWxsPSIjOTRBNEI4Ii8+PGNpcmNsZSBjeD0iMjgiIGN5PSIyOCIgcj0iMyIgZmlsbD0iIzk0QTNCOCIvPjxwYXRoIGQ9Ik0yMCAzNUwyNSAzMEwzMCAzNUwzNSAzMEw0MCAzNVYzN0gyMFYzNVoiIGZpbGw9IiM5NEEzQjgiLz48L3N2Zz4='
      }
    }
  }
}
</script>

<style scoped>
/* 页面整体布局 */
.merchant-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* 页面头部区域 */
.page-header {
  background: white;
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-title {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2f3d;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #7f8c8d;
  margin: 0;
}

.publish-btn {
  background: linear-gradient(135deg, #10b981, #059669);
  border: none;
  padding: 10px 20px;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.3);
}

.publish-btn:hover {
  background: linear-gradient(135deg, #059669, #047857);
  transform: translateY(-1px);
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-card.total .stat-icon {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.stat-card.pending .stat-icon {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.stat-card.approved .stat-icon {
  background: linear-gradient(135deg, #10b981, #059669);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.stat-card.rejected .stat-icon {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1f2f3d;
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #7f8c8d;
}

/* 表格卡片 */
.table-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.table-tools {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.search-box {
  flex: 1;
  max-width: 320px;
}

.search-input {
  width: 100%;
}

.filter-box {
  width: 160px;
}

.status-filter {
  width: 100%;
}

/* 商品信息样式 */
.product-info {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 4px 0;
}

.table-image-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f8fafc;
  border: 1px solid #eef2f6;
}

.table-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.2s;
}

.table-image:hover {
  transform: scale(1.05);
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  color: #94a3b8;
  font-size: 20px;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 6px 0;
  line-height: 1.4;
}

.product-desc {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.category-tag,
.status-tag {
  border-radius: 12px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 500;
  border: none;
  min-width: 60px;
  text-align: center;
}

/* 价格、库存样式 */
.price-text {
  font-size: 15px;
  font-weight: 600;
  color: #f97316;
}

.stock-tag {
  border-radius: 12px;
  font-weight: 500;
  font-size: 12px;
  padding: 2px 8px;
  border: none;
}

/* 浏览量、销量样式 */
.view-count,
.sold-count {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
  text-align: center;
  display: block;
}

/* 审核反馈样式 */
.reject-reason {
  background: #fef2f2;
  border: 1px solid #fee2e2;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 12px;
}

.reason-title {
  color: #dc2626;
  font-weight: 600;
  margin: 0 0 4px 0;
  font-size: 11px;
}

.reason-text {
  color: #475569;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.approve-info {
  font-size: 12px;
  color: #10b981;
  line-height: 1.4;
}

.no-feedback {
  font-size: 12px;
  color: #94a3b8;
  font-style: italic;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 8px;
}

.edit-btn,
.delete-btn {
  padding: 5px 12px;
  font-size: 12px;
}

.edit-btn.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-illustration {
  margin-bottom: 16px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #475569;
  margin: 0 0 8px 0;
}

.empty-description {
  font-size: 14px;
  color: #94a3b8;
  margin: 0 0 24px 0;
}

.publish-first-btn {
  background: linear-gradient(135deg, #10b981, #059669);
  border: none;
  padding: 8px 20px;
}

/* 删除对话框样式 */
.delete-dialog-content {
  text-align: center;
  padding: 10px 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .merchant-page {
    padding: 12px;
  }
  
  .page-header {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    text-align: center;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .table-tools {
    flex-direction: column;
  }
  
  .search-box {
    max-width: 100%;
  }
  
  .filter-box {
    width: 100%;
  }
  
  .product-info {
    flex-direction: column;
  }
  
  .table-image {
    width: 100%;
    height: 120px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 6px;
  }
  
  .action-buttons .el-button {
    margin-left: 0 !important;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
  }
  
  .stat-number {
    font-size: 22px;
  }
}
</style>