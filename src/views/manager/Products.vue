/**
 * 商品管理页面
 * 文件路径: src/views/manager/Products.vue
 * 功能描述: 管理平台所有商品信息，展示商品统计概览（总数、上架数、下架数、库存预警），支持商品列表展示与搜索筛选、
 *           添加/编辑/删除商品、批量操作（上下架、改库存、调价格、改分类、批量删除）、库存预警提示与低库存管理、
 *           商品详情查看、快速编辑、库存调整、操作日志查看、数据分析、数据导出，支持自定义表格列显示
 * 关联文件:
 * - src/api/index.js: 提供商品增删改查接口
 * - src/views/manager/ProductDetail.vue: 商品详情弹窗/页面组件
 * - src/components/ProductToolbar.vue: 商品搜索工具栏组件
 * - src/components/ProductTable.vue: 商品表格组件
 */
<template>
  <div class="product-management">
    <!-- 统计概览卡片 -->
    <div class="stats-overview">
      <div
        v-for="stat in quickStats"
        :key="stat.label"
        class="stat-card"
        :class="`stat-card--${stat.type}`"
      >
        <div class="stat-card__icon">
          <i :class="stat.icon"></i>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__value">{{ stat.value }}</span>
          <span class="stat-card__label">{{ stat.label }}</span>
        </div>
        <div class="stat-card__bg"></div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content-card">
      <!-- 页面头部 -->
      <div class="content-header">
        <div class="content-header__left">
          <el-button type="text" @click="$router.back()" class="nav-back">
            <i class="el-icon-arrow-left"></i> 返回
          </el-button>
          <div class="title-group">
            <h1 class="content-title">
              <i class="el-icon-s-goods"></i>
              商品管理
            </h1>
            <span class="content-subtitle">共 {{ pagination.total }} 件商品</span>
          </div>
        </div>
        <div class="content-header__right">
          <el-button plain @click="$router.push('/pending-products')">
            <i class="el-icon-document-checked"></i> 商品审核
          </el-button>
          <el-button type="primary" icon="el-icon-plus" @click="handleAddProduct">
            添加商品
          </el-button>
          <el-dropdown trigger="click" @command="handleExportAction">
            <el-button plain icon="el-icon-download">导出</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="selected" :disabled="selectedProducts.length === 0">
                  <i class="el-icon-document-copy"></i> 导出选中 ({{ selectedProducts.length }})
                </el-dropdown-item>
                <el-dropdown-item command="all">
                  <i class="el-icon-files"></i> 导出全部
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button plain icon="el-icon-s-data" @click="showAnalysis">数据分析</el-button>
        </div>
      </div>

      <!-- 工具栏 -->
      <ProductToolbar
        :search="search"
        :advanced-filter="advancedFilter"
        :show-advanced-filter="showAdvancedFilter"
        @search="handleSearch"
        @reset="resetFilters"
        @filter-change="handleFilterChange"
        @price-filter="handlePriceFilter"
        @toggle-advanced="toggleAdvancedFilter"
      />

      <!-- 库存预警 -->
      <div v-if="viewSettings.showStockAlert && lowStockProducts.length > 0" class="inventory-alert">
        <div class="inventory-alert__header">
          <i class="el-icon-warning-outline"></i>
          <span>库存预警</span>
          <el-tag type="danger" size="mini" effect="dark">{{ lowStockProducts.length }} 件商品库存不足</el-tag>
        </div>
        <div class="inventory-alert__body">
          <div class="inventory-alert__list">
            <div v-for="product in lowStockProducts" :key="product.id" class="inventory-alert__item">
              <span class="inventory-alert__name" @click="handleViewDetail(product)">
                {{ product.name }}
              </span>
              <el-tag type="danger" size="mini" effect="plain">
                仅剩 {{ product.stock || 0 }} {{ product.unit || '件' }}
              </el-tag>
              <el-button type="text" size="mini" @click="closeLowStockAlert(product.id)">忽略</el-button>
            </div>
          </div>
          <el-button type="warning" size="small" plain @click="handleLowStockManagement">
            <i class="el-icon-refresh"></i> 批量管理低库存
          </el-button>
        </div>
      </div>

      <!-- 批量操作栏 -->
      <transition name="batch-slide">
        <div v-if="selectedProducts.length > 0" class="batch-bar">
          <div class="batch-bar__info">
            <el-checkbox v-model="isSelectAll" :indeterminate="isIndeterminate" @change="handleSelectAll">
              已选择 <strong>{{ selectedProducts.length }}</strong> 个商品
            </el-checkbox>
            <el-button type="text" @click="clearSelection">清空</el-button>
          </div>
          <div class="batch-bar__actions">
            <el-button size="small" @click="batchOperation('shelf', 'approved')">
              <i class="el-icon-top"></i> 上架
            </el-button>
            <el-button size="small" @click="batchOperation('shelf', 'off')">
              <i class="el-icon-bottom"></i> 下架
            </el-button>
            <el-button size="small" @click="batchOperation('update_stock')">
              <i class="el-icon-sold-out"></i> 改库存
            </el-button>
            <el-button size="small" @click="batchOperation('update_price')">
              <i class="el-icon-coin"></i> 调价格
            </el-button>
            <el-button size="small" @click="batchOperation('update_category')">
              <i class="el-icon-folder"></i> 改分类
            </el-button>
            <el-button size="small" type="danger" plain @click="batchOperation('delete')">
              <i class="el-icon-delete"></i> 删除
            </el-button>
          </div>
          <div class="batch-bar__tools">
            <el-tooltip content="刷新" placement="top">
              <el-button type="text" icon="el-icon-refresh" @click="loadProducts" />
            </el-tooltip>
            <el-tooltip content="显示设置" placement="top">
              <el-button type="text" icon="el-icon-setting" @click="showViewSettings = true" />
            </el-tooltip>
          </div>
        </div>
      </transition>

      <!-- 商品表格 -->
      <ProductTable
        ref="tableRef"
        :paginated-products="paginatedProducts"
        :selected-products="selectedProducts"
        :view-settings="viewSettings"
        :pagination="pagination"
        :quick-stats="quickStats"
        @view-detail="handleViewDetail"
        @quick-edit="handleQuickEdit"
        @quick-stock="handleQuickStock"
        @toggle-shelf="handleToggleShelf"
        @toggle-recommend="handleToggleRecommend"
        @copy-link="handleCopyLink"
        @view-logs="handleViewLogs"
        @delete="handleDelete"
        @view-merchant="handleViewMerchant"
        @selection-change="handleSelectionChange"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      />
    </div>

    <!-- 添加商品对话框 -->
    <AddProductDialog :visible.sync="addDialog.visible" :saving="addDialog.saving" @save="handleAddSave" />

    <!-- 商品详情对话框 -->
    <ProductDetail :visible.sync="detailDialog.visible" :product="detailDialog.data" @update:product="handleProductUpdate" />

    <!-- 快速编辑对话框 -->
    <QuickEditDialog :visible.sync="quickEditDialog.visible" :product="quickEditDialog.form" :saving="quickEditDialog.saving" @save="handleQuickSave" />

    <!-- 库存调整对话框 -->
    <StockDialog
      :visible.sync="stockDialog.visible"
      :product-id="stockDialog.productId"
      :product-name="stockDialog.productName"
      :current-stock="stockDialog.currentStock"
      :saving="stockDialog.saving"
      @save="handleSaveStock"
    />

    <!-- 批量操作对话框 -->
    <BatchDialog
      :visible.sync="batchDialog.visible"
      :type="batchDialog.type"
      :selected-count="selectedProducts.length"
      :loading="batchDialog.loading"
      @confirm="confirmBatchOperation"
    />

    <!-- 数据分析对话框 -->
    <AnalysisDialog :visible.sync="analysisDialog.visible" :data="analysisDialog.data" :total-count="pagination.total" @export="exportAnalysis" />

    <!-- 操作日志对话框 -->
    <el-dialog v-if="logDialog" :visible.sync="logDialog.visible" :title="`操作日志 - ${logDialog.productName || ''}`" width="600px">
      <div v-if="logDialog.loading" class="log-loading">
        <i class="el-icon-loading"></i>
        <span>加载中...</span>
      </div>
      <div v-else-if="logDialog.logs && logDialog.logs.length > 0" class="log-timeline">
        <div v-for="(log, index) in logDialog.logs" :key="index" class="log-card">
          <div class="log-action">
            <el-tag :type="logActionType(log.action)" size="small">{{ logActionText(log.action) }}</el-tag>
            <span class="log-operator">{{ log.operator }}</span>
            <span class="log-time">{{ formatTime(log.time) }}</span>
          </div>
          <div v-if="log.details" class="log-details">{{ log.details }}</div>
        </div>
      </div>
      <div v-else class="empty-logs">暂无操作记录</div>
    </el-dialog>

    <!-- 显示设置抽屉 -->
    <ViewSettingsDrawer :visible.sync="showViewSettings" :settings="viewSettings" @change="saveViewSettings" @reset="resetViewSettings" />
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from '@vue/composition-api'
import { useProductList, useProductActions, useTableSelection, useKeyboardShortcuts } from '@/composables/useProduct'
import AddProductDialog from './components/AddProductDialog.vue'
import ProductDetail from './ProductDetail.vue'
import ProductTable from './components/ProductTable.vue'
import ProductToolbar from './components/ProductToolbar.vue'
import QuickEditDialog from './components/QuickEditDialog.vue'
import StockDialog from './components/StockDialog.vue'
import BatchDialog from './components/BatchDialog.vue'
import AnalysisDialog from './components/AnalysisDialog.vue'
import ViewSettingsDrawer from './components/ViewSettingsDrawer.vue'

export default {
  name: 'Products',
  components: { AddProductDialog, ProductDetail, ProductTable, ProductToolbar, QuickEditDialog, StockDialog, BatchDialog, AnalysisDialog, ViewSettingsDrawer },
  setup() {
    const productList = useProductList()
    const tableSelection = useTableSelection()
    const actions = useProductActions(productList.products, productList.applyFilters, tableSelection.selectedProducts, productList.dismissedAlerts, productList.stats, productList.loadProducts)
    const tableRef = ref()
    const showViewSettings = ref(false)

    const { addKeyboardShortcuts, removeKeyboardShortcuts } = useKeyboardShortcuts({
      onSearchFocus: () => document.querySelector('.search-input input')?.focus(),
      onSelectAll: () => tableSelection.handleSelectAll(true),
      onEscape: () => tableSelection.clearSelection(),
      onDelete: () => actions.batchOperation('delete'),
      onRefresh: () => productList.loadProducts()
    })

    const handleExportAction = (command) => {
      if (command === 'selected') actions.batchExport()
      else actions.handleExport()
    }

    onMounted(() => {
      productList.loadProducts()
      productList.loadViewSettings()
      addKeyboardShortcuts()
    })
    onUnmounted(() => removeKeyboardShortcuts())

    return {
      ...productList,
      ...actions,
      ...tableSelection,
      tableRef,
      showViewSettings,
      handleExportAction
    }
  }
}
</script>

<style scoped>
/* ========== 基础变量 ========== */
.product-management {
  --primary: #409eff;
  --primary-light: #ecf5ff;
  --success: #67c23a;
  --success-light: #f0f9eb;
  --warning: #e6a23c;
  --warning-light: #fdf6ec;
  --danger: #f56c6c;
  --danger-light: #fef0f0;
  --info: #909399;
  --bg: #f0f2f5;
  --card-bg: #ffffff;
  --border: #ebeef5;
  --text: #303133;
  --text-secondary: #606266;
  --text-muted: #909399;
  --radius: 12px;
  --radius-sm: 8px;
  --shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  --shadow-hover: 0 4px 20px rgba(0, 0, 0, 0.1);
  --transition: 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  padding: 24px;
  background: var(--bg);
  min-height: 100%;
}

/* ========== 统计概览卡片 ========== */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: var(--card-bg);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
  cursor: default;
  transition: transform var(--transition), box-shadow var(--transition);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.stat-card__bg {
  position: absolute;
  right: -20px;
  top: -20px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  opacity: 0.08;
  pointer-events: none;
}

.stat-card--success .stat-card__bg { background: var(--success); }
.stat-card--danger .stat-card__bg { background: var(--danger); }
.stat-card--warning .stat-card__bg { background: var(--warning); }
.stat-card--info .stat-card__bg { background: var(--primary); }

.stat-card__icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-card--success .stat-card__icon {
  background: var(--success-light);
  color: var(--success);
}

.stat-card--danger .stat-card__icon {
  background: var(--danger-light);
  color: var(--danger);
}

.stat-card--warning .stat-card__icon {
  background: var(--warning-light);
  color: var(--warning);
}

.stat-card--info .stat-card__icon {
  background: var(--primary-light);
  color: var(--primary);
}

.stat-card__body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-card__value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text);
  line-height: 1;
}

.stat-card__label {
  font-size: 13px;
  color: var(--text-muted);
}

/* ========== 主内容卡片 ========== */
.main-content-card {
  background: var(--card-bg);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
}

/* ========== 内容头部 ========== */
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
}

.content-header__left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-back {
  font-size: 14px;
  color: var(--text-secondary);
  padding: 0;
  transition: color var(--transition);
}

.nav-back:hover {
  color: var(--primary);
}

.title-group {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.content-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text);
  display: flex;
  align-items: center;
  gap: 8px;
}

.content-title i {
  color: var(--primary);
  font-size: 20px;
}

.content-subtitle {
  font-size: 13px;
  color: var(--text-muted);
}

.content-header__right {
  display: flex;
  gap: 8px;
}

/* ========== 库存预警 ========== */
.inventory-alert {
  margin: 0 24px 16px;
  background: linear-gradient(135deg, #fef0f0 0%, #fdf6ec 100%);
  border: 1px solid #fde2e2;
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.inventory-alert__header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(245, 108, 108, 0.06);
  font-size: 14px;
  font-weight: 600;
  color: var(--danger);
}

.inventory-alert__header i {
  font-size: 16px;
}

.inventory-alert__body {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.inventory-alert__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.inventory-alert__item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  border: 1px solid #fde2e2;
  transition: all var(--transition);
}

.inventory-alert__item:hover {
  border-color: var(--danger);
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.12);
}

.inventory-alert__name {
  font-size: 13px;
  color: var(--text);
  cursor: pointer;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.inventory-alert__name:hover {
  color: var(--primary);
}

/* ========== 批量操作栏 ========== */
.batch-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 24px 16px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f4f9ff 100%);
  border: 1px solid #d9ecff;
  border-radius: var(--radius-sm);
}

.batch-bar__info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-bar__info strong {
  color: var(--primary);
}

.batch-bar__actions {
  display: flex;
  gap: 6px;
}

.batch-bar__tools {
  display: flex;
  gap: 4px;
}

.batch-slide-enter-active,
.batch-slide-leave-active {
  transition: all 0.3s ease;
}

.batch-slide-enter,
.batch-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ========== 日志弹窗 ========== */
.log-timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-card {
  padding: 12px 16px;
  background: #fafafa;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border);
}

.log-action {
  display: flex;
  align-items: center;
  gap: 10px;
}

.log-operator {
  font-size: 13px;
  color: var(--text-secondary);
}

.log-time {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-muted);
}

.log-details {
  margin-top: 8px;
  font-size: 13px;
  color: var(--text-muted);
  padding-left: 4px;
}

.empty-logs {
  text-align: center;
  padding: 48px 0;
  color: var(--text-muted);
  font-size: 14px;
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .product-management {
    padding: 12px;
  }

  .stats-overview {
    grid-template-columns: 1fr;
  }

  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .content-header__right {
    width: 100%;
    flex-wrap: wrap;
  }

  .batch-bar {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .batch-bar__actions {
    flex-wrap: wrap;
  }

  .inventory-alert__body {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>