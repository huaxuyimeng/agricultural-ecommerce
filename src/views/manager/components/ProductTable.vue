/**
 * 商品表格组件
 * 文件路径: src/views/manager/components/ProductTable.vue
 * 功能描述: 管理后台商品列表表格，支持多选列、商品信息列（图片+名称点击查看详情）、价格库存列、
 *           商品状态列（上架/下架/待审核含彩色标签）、商家信息列、数据统计列（销量/浏览量）、
 *           更新时间列、操作列（查看/编辑/上下架/库存/分析/删除），行内库存预警高亮样式，
 *           支持自定义列显示（通过ViewSettingsDrawer），分页数据展示
 * 关联文件:
 * - src/api/index.js: 提供商品操作接口
 * - src/views/manager/Products.vue: 商品管理页面（父组件）
 * - src/views/manager/components/ViewSettingsDrawer.vue: 列显示设置抽屉
 */
<template>
  <div class="product-table-wrapper">
    <!-- 商品表格 -->
    <el-table
      ref="tableRef"
      :data="paginatedProducts"
      style="width: 100%"
      stripe
      class="products-table"
      :row-class-name="tableRowClassName"
      @selection-change="$emit('selection-change', $event)"
    >
      <!-- 选择列 -->
      <el-table-column
        v-if="viewSettings.visibleColumns.includes('selection')"
        type="selection"
        width="55"
        align="center"
      />

      <!-- 商品图片和名称 -->
      <el-table-column
        v-if="viewSettings.visibleColumns.includes('image') || viewSettings.visibleColumns.includes('name')"
        prop="image"
        label="商品信息"
        min-width="300"
      >
        <template #default="{ row }">
          <div class="product-info" @click="$emit('view-detail', row)">
            <div class="product-image">
              <img
                :src="getImageUrl(row.image) || getDefaultImage(row.id)"
                :alt="row.name"
                class="table-image"
                @error="handleImageError(row)"
              />
              <!-- 状态标记 -->
              <div v-if="row.isNew" class="new-badge">新品</div>
              <div v-else-if="row.isHot" class="hot-badge">热销</div>
            </div>
            
            <div class="product-details">
              <div class="product-header">
                <h4 class="product-name" :title="row.name">
                  {{ row.name || '未命名商品' }}
                </h4>
                
                <div class="product-badges">
                  <el-tag
                    v-if="row.category"
                    :type="getCategoryTagType(row.category)"
                    size="mini"
                    class="category-tag"
                  >
                    {{ getCategoryLabel(row.category) }}
                  </el-tag>
                  
                  <el-tag
                    v-if="row.isRecommend"
                    type="success"
                    size="mini"
                    class="recommend-tag"
                  >
                    <i class="el-icon-star-on"></i> 推荐
                  </el-tag>
                </div>
              </div>
              
              <p
                v-if="row.description"
                class="product-desc"
                :title="row.description"
              >
                {{ row.description }}
              </p>
              
              <div class="product-specs">
                <span v-if="row.origin" class="spec-item">
                  <i class="el-icon-location-information"></i>
                  {{ row.origin }}
                </span>
                <span v-if="row.weight" class="spec-item">
                  <i class="el-icon-scales"></i>
                  {{ row.weight }}
                </span>
                <span v-if="row.brand" class="spec-item">
                  <i class="el-icon-s-flag"></i>
                  {{ row.brand }}
                </span>
              </div>
            </div>
          </div>
        </template>
      </el-table-column>

      <!-- 价格和库存 -->
      <el-table-column
        v-if="viewSettings.visibleColumns.includes('price')"
        prop="price"
        label="价格"
        width="140"
        align="center"
      >
        <template #default="{ row }">
          <div class="price-stock-info">
            <div class="price-section">
              <span class="price-value">¥{{ row.price && row.price.toFixed(2) || '0.00' }}</span>
              <span
                v-if="row.originalPrice && row.originalPrice > row.price"
                class="price-original"
              >
                ¥{{ row.originalPrice.toFixed(2) }}
              </span>
            </div>
            
            <div class="stock-section">
              <span class="stock-label">库存：</span>
              <span
                :class="['stock-value', getStockClass(row.stock)]"
              >
                {{ row.stock || 0 }}
              </span>
              <span v-if="row.unit" class="stock-unit">{{ row.unit }}</span>
              <el-tag v-if="row.stock === 0" type="danger" size="mini" class="stock-status">缺货</el-tag>
              <el-tag v-else-if="row.stock > 0 && row.stock <= 10" type="warning" size="mini" class="stock-status">紧张</el-tag>
            </div>
          </div>
        </template>
      </el-table-column>

      <!-- 商品状态 -->
      <el-table-column
        v-if="viewSettings.visibleColumns.includes('status')"
        prop="status"
        label="状态"
        width="100"
        align="center"
      >
        <template #default="{ row }">
          <el-tag
            :type="getStatusTagType(row.status)"
            effect="dark"
            class="status-tag"
          >
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <!-- 商家信息 -->
      <el-table-column
        v-if="viewSettings.visibleColumns.includes('merchant')"
        prop="merchant"
        label="商家"
        width="140"
      >
        <template #default="{ row }">
          <div
            v-if="row.merchant"
            class="merchant-info"
            @click="$emit('view-merchant', row.merchantId)"
          >
            <el-avatar
              v-if="row.merchantAvatar"
              :src="row.merchantAvatar"
              :size="32"
              class="merchant-avatar"
            >
              {{ row.merchantName && row.merchantName.charAt(0) || '商' }}
            </el-avatar>
            <div class="merchant-details">
              <div class="merchant-name" :title="row.merchantName">
                {{ row.merchantName || '未知商家' }}
              </div>
            </div>
          </div>
          <span class="merchant-system">系统商品</span>
        </template>
      </el-table-column>

      <!-- 更新时间 -->
      <el-table-column
        v-if="viewSettings.visibleColumns.includes('updateTime')"
        prop="updateTime"
        label="更新时间"
        width="120"
        align="center"
      >
        <template #default="{ row }">
          <span class="update-time">{{ formatTime(row.updateTime) }}</span>
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column
        label="操作"
        width="160"
        align="center"
        fixed="right"
      >
        <template #default="{ row }">
          <div class="action-buttons">
            <!-- 查看详情 -->
            <el-tooltip content="查看详情" placement="top">
              <el-button type="primary" icon="el-icon-view" size="mini" class="operation-button" @click="$emit('view-detail', row)" />
            </el-tooltip>
            
            <!-- 快速编辑 -->
            <el-tooltip content="快速编辑" placement="top">
              <el-button type="warning" icon="el-icon-edit" size="mini" class="operation-button" @click="$emit('quick-edit', row)" />
            </el-tooltip>
            
            <!-- 库存调整 -->
            <el-tooltip content="库存调整" placement="top">
              <el-button type="success" icon="el-icon-sold-out" size="mini" class="operation-button" @click="$emit('quick-stock', row)" />
            </el-tooltip>
            
            <!-- 更多操作 -->
            <el-dropdown trigger="click" placement="bottom-end" @command="handleDropdownCommand">
              <el-button type="text" icon="el-icon-more" size="mini" class="operation-button" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{ action: 'copy-link', row }">
                    <i class="el-icon-link"></i> 复制链接
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'view-logs', row }">
                    <i class="el-icon-notebook-2"></i> 操作日志
                  </el-dropdown-item>
                  <el-divider />
                  <el-dropdown-item :command="{ action: 'toggle-shelf', row }">
                    <i :class="row.status === 'approved' ? 'el-icon-bottom' : 'el-icon-top'"></i>
                    {{ row.status === 'approved' ? '下架' : '上架' }}
                  </el-dropdown-item>
                  <el-dropdown-item :command="{ action: 'toggle-recommend', row }">
                    <i :class="row.isRecommend ? 'el-icon-star-off' : 'el-icon-star-on'"></i>
                    {{ row.isRecommend ? '取消推荐' : '设为推荐' }}
                  </el-dropdown-item>
                  <el-divider />
                  <el-dropdown-item :command="{ action: 'delete', row }" style="color: #F56C6C;">
                    <i class="el-icon-delete"></i> 删除商品
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <div class="pagination-left">
        <span>共 {{ pagination.total }} 条记录</span>
      </div>
      
      <div class="pagination-right">
        <el-pagination
          :current-page.sync="pagination.currentPage"
          :page-size.sync="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="prev, pager, next, jumper"
          background
          @current-change="$emit('page-change', $event)"
          @size-change="$emit('page-size-change', $event)"
        />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductTable',
  props: {
    paginatedProducts: { type: Array, default: () => [] },
    selectedProducts: { type: Array, default: () => [] },
    viewSettings: { type: Object, required: true },
    pagination: { type: Object, required: true },
    quickStats: { type: Array, default: () => [] },
    tableRef: { type: Object, default: null }
  },
  emits: [
    'view-detail', 'quick-edit', 'quick-stock', 'toggle-shelf', 'toggle-recommend',
    'copy-link', 'view-logs', 'delete', 'view-merchant',
    'selection-change', 'page-change', 'page-size-change'
  ],
  methods: {
    getCategoryLabel(category) {
      const categoryMap = {
        'vegetable': '蔬菜',
        'fruit': '水果',
        'meat': '肉类',
        'grain': '粮食',
        'processed': '加工产品',
        'other': '其他'
      }
      return categoryMap[category] || category
    },
    getStatusText(status) {
      const statusMap = { 'approved': '在售', 'off': '下架', 'sold_out': '缺货' }
      return statusMap[status] || '未知'
    },
    getStatusTagType(status) {
      const types = { 'approved': 'success', 'off': 'warning', 'sold_out': 'danger' }
      return types[status] || ''
    },
    getCategoryTagType(category) {
      const types = {
        'vegetable': 'success', 'fruit': 'warning', 'meat': 'danger',
        'grain': '', 'processed': 'info', 'other': ''
      }
      return types[category] || ''
    },
    getStockClass(stock) {
      if (stock === undefined || stock === null) return ''
      if (stock === 0) return 'stock-zero'
      if (stock <= 10) return 'stock-low'
      return 'stock-normal'
    },
    formatTime(time) {
      if (!time) return '从未'
      const date = new Date(time)
      const now = new Date()
      const diffMs = now - date
      const diffMins = Math.floor(diffMs / 60000)
      const diffHours = Math.floor(diffMs / 3600000)
      const diffDays = Math.floor(diffMs / 86400000)
      if (diffMins < 1) return '刚刚'
      if (diffMins < 60) return `${diffMins}分钟前`
      if (diffHours < 24) return `${diffHours}小时前`
      if (diffDays < 7) return `${diffDays}天前`
      return date.toLocaleDateString()
    },
    formatFullTime(time) {
      if (!time) return '从未'
      return new Date(time).toLocaleString()
    },
    getDefaultImage(id) {
      const index = id ? (Number(id) % 11) + 1 : Math.floor(Math.random() * 11) + 1
      return `/imgs/foods/${index}.png`
    },
    getImageUrl(image) {
      if (!image) return ''
      if (image.startsWith('data:image') || image.startsWith('http')) return image
      if (image.startsWith('/imgs/') || image.startsWith('imgs/')) return image.startsWith('/') ? image : `/${image}`
      if (image.startsWith('/')) return `/imgs${image}`
      return `/imgs/products/${image}`
    },
    handleImageError(row) {
      row.image = null
    },
    tableRowClassName({ row }) {
      if (row.status === 'off') return 'off-row'
      if (row.status === 'sold_out') return 'sold-out-row'
      if (row.isRecommend) return 'recommend-row'
      return ''
    },
    handleDropdownCommand(cmd) {
      this.$emit(cmd.action, cmd.row)
    }
  }
}
</script>

<style scoped>
.product-table-wrapper {
  background: white;
  overflow: hidden;
}

/* 商品图片样式 */
.product-image {
  width: 72px;
  height: 72px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  transition: transform 0.2s ease;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.new-badge, .hot-badge {
  position: absolute;
  top: 0;
  left: 0;
  padding: 2px 6px;
  font-size: 10px;
  color: white;
  border-radius: 0 0 4px 0;
}

.new-badge { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.hot-badge { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }

.product-info {
  display: flex;
  gap: 12px;
  cursor: pointer;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-header {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 4px;
}

.product-name {
  font-weight: 500;
  color: #303133;
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-badges {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.product-desc {
  font-size: 12px;
  color: #909399;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.spec-item {
  font-size: 11px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 2px;
}

/* 价格和库存 */
.price-stock-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.price-section {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
}

.price-value {
  font-size: 15px;
  font-weight: 600;
  color: #f56c6c;
}

.price-original {
  font-size: 11px;
  color: #909399;
  text-decoration: line-through;
}

.stock-section {
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: center;
}

.stock-label {
  font-size: 12px;
  color: #909399;
}

.stock-value {
  font-weight: 500;
  font-size: 13px;
}

.stock-normal { color: #67c23a; }
.stock-low { color: #e6a23c; }
.stock-zero { color: #f56c6c; }

/* 状态 */
.status-tag {
  font-size: 12px;
}

/* 商家 */
.merchant-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.merchant-info:hover .merchant-name {
  color: #409eff;
}

.merchant-details {
  flex: 1;
  min-width: 0;
}

.merchant-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.merchant-system {
  font-size: 13px;
  color: #909399;
}

/* 更新时间 */
.update-time {
  font-size: 13px;
  color: #606266;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: center;
}

.operation-button {
  padding: 4px !important;
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}

.pagination-left {
  font-size: 13px;
  color: #606266;
}

/* 表格样式 */
:deep(.el-table) {
  --el-table-border-color: #ebeef5;
}

:deep(.el-table th) {
  background-color: #fafbfc;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
}

:deep(.el-table .off-row) { background-color: #fafafa; }
:deep(.el-table .sold-out-row) { background-color: #fef0f0; }
:deep(.el-table .recommend-row) { background-color: #f0f9ff; }
</style>