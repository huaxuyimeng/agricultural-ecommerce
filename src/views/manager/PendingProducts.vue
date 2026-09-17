/**
 * 商品审核页面
 * 文件路径: src/views/manager/PendingProducts.vue
 * 功能描述: 审核商家提交的商品信息，展示待审核/已通过/已拒绝三类统计卡片并支持点击筛选，
 *           支持按商品名称、商家名称搜索，按审核状态和商品分类筛选，单个审核（通过/拒绝）、批量审核操作，
 *           拒绝时需填写拒绝原因并提供常用理由快捷选择，支持商品详情查看
 * 关联文件:
 * - src/api/index.js: 提供商品审核、拒绝接口
 * - src/views/manager/ProductReview.vue: 商品审核详情页面
 * - src/views/manager/Products.vue: 商品管理页面
 */
<template>
  <div class="pending-products-page">
    <div class="page-header">
      <div class="header-left">
        <el-button type="text" @click="$router.back()" class="back-btn">
          <i class="el-icon-arrow-left"></i> 返回
        </el-button>
        <h2 class="page-title">
          <i class="el-icon-document-checked"></i>
          商品审核
        </h2>
        <p class="page-subtitle">审核商家提交的商品信息</p>
      </div>
      <div class="header-right">
        <el-button @click="$router.push('/manager/products')">
          <i class="el-icon-s-goods"></i> 商品管理
        </el-button>
      </div>
    </div>

    <div class="stats-row">
      <el-card class="stat-card stat-pending" shadow="hover" @click="statusFilter = 'pending'; currentPage = 1">
        <div class="stat-content">
          <div class="stat-icon">
            <i class="el-icon-time"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ (stats && stats.pending) || 0 }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card stat-approved" shadow="hover" @click="statusFilter = 'approved'; currentPage = 1">
        <div class="stat-content">
          <div class="stat-icon">
            <i class="el-icon-circle-check"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ (stats && stats.approved) || 0 }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card stat-rejected" shadow="hover" @click="statusFilter = 'rejected'; currentPage = 1">
        <div class="stat-content">
          <div class="stat-icon">
            <i class="el-icon-circle-close"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ (stats && stats.rejected) || 0 }}</div>
            <div class="stat-label">已拒绝</div>
          </div>
        </div>
      </el-card>
    </div>

    <el-card class="review-card" shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchQuery"
            placeholder="搜索商品名称或商家名称"
            clearable
            class="search-input"
            @keyup.enter.native="handleSearch"
            @clear="handleSearch"
            prefix-icon="el-icon-search"
          />
          <el-select v-model="statusFilter" placeholder="审核状态" clearable class="filter-select" @change="handleFilter">
            <el-option label="全部状态" value="" />
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
          <el-select v-model="categoryFilter" placeholder="商品分类" clearable class="filter-select" @change="handleFilter">
            <el-option label="全部分类" value="" />
            <el-option label="蔬菜" value="vegetable" />
            <el-option label="水果" value="fruit" />
            <el-option label="肉类" value="meat" />
            <el-option label="粮食" value="grain" />
            <el-option label="加工产品" value="processed" />
            <el-option label="其他" value="other" />
          </el-select>
        </div>
        <div class="toolbar-right">
          <el-button v-if="selectedProducts.length > 0" type="success" size="medium" @click="handleBatchApprove">
            <i class="el-icon-check"></i> 批量通过 ({{ selectedProducts.length }})
          </el-button>
          <el-button v-if="selectedProducts.length > 0" type="danger" size="medium" plain @click="handleBatchReject">
            <i class="el-icon-close"></i> 批量拒绝
          </el-button>
          <el-button icon="el-icon-refresh" @click="loadData" :loading="loading" circle title="刷新数据"></el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="pagedList"
        stripe
        class="review-table"
        :header-cell-style="{ background: '#fafafa', color: '#606266', fontWeight: '600' }"
        empty-text="暂无待审核商品"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div class="product-cell">
              <div class="product-image-wrapper">
                <img :src="getImageUrl(row.image)" :alt="row.name" class="product-image" @error="handleImageError" />
              </div>
              <div class="product-info">
                <div class="product-name">{{ row.name }}</div>
                <div class="product-category">
                  <el-tag :type="getCategoryTagType(row.category)" size="mini" effect="light">
                    {{ getCategoryText(row.category) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100" align="center">
          <template #default="{ row }">
            <span class="price-text">¥{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStockTagType(row.stock)" size="small">{{ row.stock || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="merchantName" label="商家" min-width="120" align="center">
          <template #default="{ row }">{{ row.merchantName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="160" align="center">
          <template #default="{ row }">{{ formatDateTime(row.submitTime || row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="审核状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small" effect="light">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons" v-if="row.status === 'pending'">
              <el-button type="success" size="mini" @click="handleApprove(row)">
                <i class="el-icon-check"></i> 通过
              </el-button>
              <el-button type="danger" size="mini" plain @click="openRejectDialog(row)">
                <i class="el-icon-close"></i> 拒绝
              </el-button>
              <el-button type="text" size="mini" @click="handleViewDetail(row)">
                <i class="el-icon-view"></i> 详情
              </el-button>
            </div>
            <div class="action-buttons" v-else>
              <el-button type="text" size="mini" @click="handleViewDetail(row)">
                <i class="el-icon-view"></i> 查看
              </el-button>
              <el-tag size="mini" type="info">{{ getStatusText(row.status) }}</el-tag>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          :current-page.sync="currentPage"
          :page-size.sync="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="filteredList.length"
          @size-change="currentPage = 1"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog :visible.sync="rejectVisible" title="拒绝商品" width="500px" append-to-body class="reject-dialog">
      <el-alert v-if="rejectProduct" :title="`商品：${rejectProduct.name}`" type="warning" :closable="false" show-icon style="margin-bottom: 20px;"></el-alert>
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="90px">
        <el-form-item label="拒绝原因" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请详细说明拒绝原因，以便商家修改" />
        </el-form-item>
      </el-form>
      <div class="common-reasons">
        <span>常用理由：</span>
        <el-tag v-for="r in commonReasons" :key="r" size="small" class="reason-tag" @click="rejectForm.reason = r">{{ r }}</el-tag>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rejectVisible = false" size="medium">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="rejecting" size="medium">确认拒绝</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="detailVisible" title="商品详情" width="600px" append-to-body>
      <el-descriptions :column="2" border v-if="detailProduct">
        <el-descriptions-item label="商品名称">{{ detailProduct.name }}</el-descriptions-item>
        <el-descriptions-item label="商品分类">{{ getCategoryText(detailProduct.category) }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ formatPrice(detailProduct.price) }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ detailProduct.stock || 0 }}</el-descriptions-item>
        <el-descriptions-item label="商家">{{ detailProduct.merchantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getStatusTagType(detailProduct.status)" size="small">{{ getStatusText(detailProduct.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间" :span="2">{{ formatDateTime(detailProduct.submitTime || detailProduct.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="商品描述" :span="2">{{ detailProduct.description || '暂无描述' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer">
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="detailProduct && detailProduct.status === 'pending'" type="success" @click="handleApprove(detailProduct); detailVisible = false">通过</el-button>
        <el-button v-if="detailProduct && detailProduct.status === 'pending'" type="danger" @click="openRejectDialog(detailProduct); detailVisible = false">拒绝</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onBeforeUnmount } from '@vue/composition-api'
import { Message, MessageBox } from 'element-ui'
import { usePendingProducts } from '@/composables/useProduct'
import { adminRejectProduct } from '@/api'

export default {
  name: 'PendingProducts',
  setup() {
    const {
      pendingList, loading, searchQuery, statusFilter, categoryFilter,
      currentPage, pageSize, selectedProducts, stats,
      filteredList, pagedList,
      loadData, handleSearch, handleFilter, handleSelectionChange, handleApprove, handleBatchApprove,
      getImageUrl, formatPrice, formatDateTime, getCategoryText, getCategoryTagType,
      getStockTagType, getStatusTagType, getStatusText
    } = usePendingProducts()

    const rejectVisible = ref(false)
    const rejecting = ref(false)
    const rejectProduct = ref(null)
    const rejectForm = reactive({ productId: null, reason: '' })
    const rejectFormRef = ref(null)
    const commonReasons = ['图片不清晰', '描述不完整', '价格不合理', '分类错误', '信息不实']
    const rejectRules = {
      reason: [
        { required: true, message: '请输入拒绝原因', trigger: 'blur' },
        { min: 5, message: '拒绝原因至少5个字符', trigger: 'blur' }
      ]
    }

    const detailVisible = ref(false)
    const detailProduct = ref(null)

    const handleBatchReject = () => {
      if (selectedProducts.value.length === 0) {
        Message.warning('请选择待审核的商品')
        return
      }
      rejectProduct.value = { name: `${selectedProducts.value.length} 个商品` }
      rejectForm.productId = selectedProducts.value.map(p => p.id)
      rejectForm.reason = ''
      rejectVisible.value = true
    }

    const openRejectDialog = (item) => {
      rejectProduct.value = item
      rejectForm.productId = item.id
      rejectForm.reason = ''
      rejectVisible.value = true
    }

    const confirmReject = async () => {
      try {
        const valid = await rejectFormRef.value.validate().catch(() => false)
        if (!valid) return

        rejecting.value = true
        const ids = Array.isArray(rejectForm.productId) ? rejectForm.productId : [rejectForm.productId]
        for (const id of ids) {
          await adminRejectProduct(id)
        }
        Message.success('操作成功')
        rejectVisible.value = false
        rejectProduct.value = null
        selectedProducts.value = []
        loadData()
      } catch (error) {
        Message.error('操作失败')
      } finally {
        rejecting.value = false
      }
    }

    const handleViewDetail = (row) => {
      detailProduct.value = row
      detailVisible.value = true
    }

    const handleImageError = (e) => {
      const img = e.target
      if (!img.dataset.fallback) {
        img.dataset.fallback = 'true'
        img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHZpZXdCb3g9IjAgMCA2MCA2MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIGZpbGw9IiNGMUY1RjkiLz48cGF0aCBkPSJNMjAgMjVDMjAgMjIuNzkgMjEuNzkgMjEgMjQgMjFIMzZDMzguMjEgMjEgNDAgMjIuNzkgNDAgMjVWMzVDMzggMzUgMjIgMzUgMjAgMzVWMjVaIiBmaWxsPSIjOTRBNEI4Ii8+PGNpcmNsZSBjeD0iMjgiIGN5PSIyOCIgcj0iMyIgZmlsbD0iIzk0QTNCOCIvPjxwYXRoIGQ9Ik0yMCAzNUwyNSAzMEwzMCAzNUwzNSAzMEw0MCAzNVYzN0gyMFYzNVoiIGZpbGw9IiM5NEEzQjgiLz48L3N2Zz4='
      }
    }

    const handlePageChange = () => {}

    const handleKeydown = (e) => {
      if (e.ctrlKey && e.key === 'r') { e.preventDefault(); loadData() }
      if (e.key === 'Escape' && rejectVisible.value) { rejectVisible.value = false }
    }

    onMounted(() => {
      loadData()
      document.addEventListener('keydown', handleKeydown)
    })

    onBeforeUnmount(() => {
      document.removeEventListener('keydown', handleKeydown)
    })

    return {
      pendingList, loading, searchQuery, statusFilter, categoryFilter,
      currentPage, pageSize, selectedProducts, stats,
      filteredList, pagedList,
      loadData, handleSearch, handleFilter, handleSelectionChange, handleApprove, handleBatchApprove,
      getImageUrl, formatPrice, formatDateTime, getCategoryText, getCategoryTagType,
      getStockTagType, getStatusTagType, getStatusText,
      rejectVisible, rejecting, rejectProduct, rejectForm, rejectFormRef,
      commonReasons, rejectRules,
      detailVisible, detailProduct,
      handleBatchReject, openRejectDialog, confirmReject,
      handleViewDetail, handleImageError, handlePageChange
    }
  }
}
</script>

<style scoped>
.pending-products-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  font-size: 14px;
  color: #606266;
  padding: 0;
}

.back-btn:hover { color: #409eff; }

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title i { color: #faad14; }

.page-subtitle {
  font-size: 13px;
  color: #909399;
  margin: 4px 0 0 0;
}

.header-right { display: flex; gap: 12px; }

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.stat-card.stat-pending .stat-icon { background: linear-gradient(135deg, #faad14 0%, #fa8c16 100%); }
.stat-card.stat-approved .stat-icon { background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%); }
.stat-card.stat-rejected .stat-icon { background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%); }

.stat-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
  font-size: 22px;
}

.stat-info { flex: 1; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 13px;
  color: #8c8c8c;
  font-weight: 500;
}

.review-card {
  border-radius: 12px;
  overflow: hidden;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-input { flex: 1; min-width: 220px; }
.filter-select { width: 130px; }

.review-table { min-height: 300px; }

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info { min-width: 0; flex: 1; }

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-text {
  font-size: 14px;
  font-weight: 600;
  color: #ff5722;
}

.action-buttons {
  display: flex;
  gap: 6px;
  justify-content: center;
  align-items: center;
}

.pagination-wrapper {
  padding: 20px 24px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.reject-dialog ::v-deep .el-dialog__header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(to right, #fff7e6, #ffffff);
}

.reject-dialog ::v-deep .el-dialog__title {
  font-size: 18px;
  font-weight: 600;
  color: #fa8c16;
}

.common-reasons {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.common-reasons > span {
  font-size: 13px;
  color: #606266;
}

.reason-tag {
  cursor: pointer;
  transition: all 0.3s ease;
}

.reason-tag:hover {
  transform: scale(1.05);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
