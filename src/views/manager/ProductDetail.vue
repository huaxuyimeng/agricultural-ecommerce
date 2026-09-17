/**
 * 商品详情页面
 * 文件路径: src/views/manager/ProductDetail.vue
 * 功能描述: 展示商品完整信息，支持弹窗模式和独立页面模式两种展示方式（通过isDialog属性切换），
 *           包含商品基本信息Tab（名称、价格、库存、分类、描述等）和商家信息Tab，
 *           支持编辑商品、上架/下架切换、复制商品链接、删除商品，以及图片上传功能
 * 关联文件:
 * - src/api/index.js: 提供商品详情查询、更新、删除、文件上传接口
 * - src/views/manager/Products.vue: 商品管理列表页面
 */
<template>
  <el-dialog
    v-if="isDialog"
    :visible.sync="dialogVisible"
    title="商品详情"
    width="900px"
    top="5vh"
    :close-on-click-modal="false"
    class="product-detail-dialog"
    @closed="handleDialogClosed"
  >
    <div class="product-detail-page dialog-mode">
      <div v-if="!productData" class="empty-container">
        <i class="el-icon-warning"></i>
        <p>商品数据加载失败</p>
      </div>
      <div v-else class="detail-content">
        <div class="detail-header">
          <div class="product-basic-info">
            <div class="product-image-section">
              <img
                :src="getImageUrl(productData.image) || getDefaultImage(productData.id)"
                :alt="productData.name"
                class="detail-image"
                @error="handleImageError"
              />
              <div class="image-actions">
                <el-tag v-if="productData.isNew" type="success" class="status-badge">新品</el-tag>
                <el-tag v-else-if="productData.isHot" type="danger" class="status-badge">热销</el-tag>
                <el-tag v-if="productData.isRecommend" type="warning" class="status-badge">推荐</el-tag>
              </div>
            </div>
            <div class="product-base-info">
              <h2 class="product-title">{{ productData.name || '未命名商品' }}</h2>
              <div class="product-meta">
                <div class="meta-item">
                  <span class="meta-label">商品ID：</span>
                  <span class="meta-value">{{ productData.id }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">商品分类：</span>
                  <span class="meta-value">
                    <el-tag :type="getCategoryTagType(productData.category)" size="small">
                      {{ getCategoryLabel(productData.category) }}
                    </el-tag>
                  </span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">商品状态：</span>
                  <span class="meta-value">
                    <el-tag :type="getStatusTagType(productData.status)" effect="dark" size="small">
                      {{ getStatusText(productData.status) }}
                    </el-tag>
                  </span>
                </div>
              </div>
              <div class="product-actions">
                <el-button type="primary" icon="el-icon-edit" size="small" @click="handleEdit">编辑商品</el-button>
                <el-button
                  :type="productData.status === 'approved' ? 'warning' : 'success'"
                  :icon="productData.status === 'approved' ? 'el-icon-bottom' : 'el-icon-top'"
                  size="small"
                  @click="handleToggleStatus"
                >
                  {{ productData.status === 'approved' ? '下架' : '上架' }}
                </el-button>
                <el-button type="info" icon="el-icon-link" size="small" @click="handleCopyLink">复制链接</el-button>
              </div>
            </div>
          </div>
        </div>
        <el-tabs v-model="activeTab" class="detail-tabs">
          <el-tab-pane label="基本信息" name="basic">
            <div class="tab-content">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="商品名称">{{ productData.name || '--' }}</el-descriptions-item>
                <el-descriptions-item label="商品描述">{{ productData.description || '--' }}</el-descriptions-item>
                <el-descriptions-item label="商品价格">
                  <span class="price-info">
                    <span class="current-price">¥{{ productData.price ? productData.price.toFixed(2) : '0.00' }}</span>
                    <span v-if="productData.originalPrice && productData.originalPrice > productData.price" class="original-price">
                      ¥{{ productData.originalPrice.toFixed(2) }}
                    </span>
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="库存数量">
                  <span :class="['stock-info', getStockClass(productData.stock)]">
                    {{ productData.stock || 0 }} {{ productData.unit || '件' }}
                  </span>
                  <el-tag v-if="productData.stock === 0" type="danger" size="mini">缺货</el-tag>
                  <el-tag v-else-if="productData.stock > 0 && productData.stock <= 10" type="warning" size="mini">紧张</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="计量单位">{{ productData.unit || '--' }}</el-descriptions-item>
                <el-descriptions-item label="商品重量">{{ productData.weight || '--' }}</el-descriptions-item>
                <el-descriptions-item label="商品产地">{{ productData.origin || '--' }}</el-descriptions-item>
                <el-descriptions-item label="商品品牌">{{ productData.brand || '--' }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ formatFullTime(productData.createTime) }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{ formatFullTime(productData.updateTime) }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>
          <el-tab-pane label="商家信息" name="merchant">
            <div v-if="productData.merchantName" class="tab-content">
              <div class="merchant-card">
                <div class="merchant-header">
                  <el-avatar :src="productData.merchantAvatar" :size="50">
                    {{ productData.merchantName.charAt(0) }}
                  </el-avatar>
                  <div class="merchant-title">
                    <h3>{{ productData.merchantName }}</h3>
                    <p>商家ID: {{ productData.merchantId }}</p>
                  </div>
                </div>
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="商家评分">4.8</el-descriptions-item>
                  <el-descriptions-item label="合作时间">{{ formatTime(productData.merchantTime) }}</el-descriptions-item>
                  <el-descriptions-item label="在售商品">25 个</el-descriptions-item>
                  <el-descriptions-item label="累计销量">1,258 件</el-descriptions-item>
                </el-descriptions>
              </div>
            </div>
            <div v-else class="no-merchant-info">
              <i class="el-icon-info"></i>
              <p>暂无商家信息</p>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </el-dialog>

  <div v-else class="product-detail-page">
    <div v-if="loading" class="loading-container">
      <i class="el-icon-loading"></i>
      <p>加载中...</p>
    </div>

    <div v-else-if="!productData" class="empty-container">
      <i class="el-icon-warning"></i>
      <p>商品不存在</p>
      <el-button type="primary" @click="$router.push('/products')">返回商品列表</el-button>
    </div>

    <div v-else class="detail-content">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/products' }">商品管理</el-breadcrumb-item>
        <el-breadcrumb-item>商品详情</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail-header">
        <div class="product-basic-info">
          <div class="product-image-section">
            <img
              :src="getImageUrl(productData.image) || getDefaultImage(productData.id)"
              :alt="productData.name"
              class="detail-image"
              @error="handleImageError"
            />
            <div class="image-actions">
              <el-tag v-if="productData.isNew" type="success" class="status-badge">新品</el-tag>
              <el-tag v-else-if="productData.isHot" type="danger" class="status-badge">热销</el-tag>
              <el-tag v-if="productData.isRecommend" type="warning" class="status-badge">推荐</el-tag>
            </div>
          </div>
          <div class="product-base-info">
            <h2 class="product-title">{{ productData.name || '未命名商品' }}</h2>
            <div class="product-meta">
              <div class="meta-item">
                <span class="meta-label">商品ID：</span>
                <span class="meta-value">{{ productData.id }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">商品分类：</span>
                <span class="meta-value">
                  <el-tag :type="getCategoryTagType(productData.category)" size="small">
                    {{ getCategoryLabel(productData.category) }}
                  </el-tag>
                </span>
              </div>
              <div class="meta-item">
                <span class="meta-label">商品状态：</span>
                <span class="meta-value">
                  <el-tag :type="getStatusTagType(productData.status)" effect="dark" size="small">
                    {{ getStatusText(productData.status) }}
                  </el-tag>
                </span>
              </div>
            </div>
            <div class="product-actions">
              <el-button type="primary" icon="el-icon-edit" @click="handleEdit">编辑商品</el-button>
              <el-button
                :type="productData.status === 'approved' ? 'warning' : 'success'"
                :icon="productData.status === 'approved' ? 'el-icon-bottom' : 'el-icon-top'"
                @click="handleToggleStatus"
              >
                {{ productData.status === 'approved' ? '下架' : '上架' }}
              </el-button>
              <el-button type="info" icon="el-icon-link" @click="handleCopyLink">复制链接</el-button>
              <el-dropdown trigger="click" class="more-actions-dropdown" @command="handleDropdownCommand">
                <el-button type="text" icon="el-icon-more"></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ action: 'view-logs' }">
                      <i class="el-icon-notebook-2"></i> 查看操作日志
                    </el-dropdown-item>
                    <el-divider style="margin: 4px 0;" />
                    <el-dropdown-item :command="{ action: 'delete' }" style="color: #F56C6C;">
                      <i class="el-icon-delete"></i> 删除商品
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="detail-tabs">
        <el-tab-pane label="基本信息" name="basic">
          <div class="tab-content">
            <el-descriptions :column="2" border class="product-descriptions">
              <el-descriptions-item label="商品名称">{{ productData.name || '--' }}</el-descriptions-item>
              <el-descriptions-item label="商品描述">{{ productData.description || '--' }}</el-descriptions-item>
              <el-descriptions-item label="商品价格">
                <span class="price-info">
                  <span class="current-price">¥{{ productData.price ? productData.price.toFixed(2) : '0.00' }}</span>
                  <span v-if="productData.originalPrice && productData.originalPrice > productData.price" class="original-price">
                    ¥{{ productData.originalPrice.toFixed(2) }}
                  </span>
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="库存数量">
                <span :class="['stock-info', getStockClass(productData.stock)]">
                  {{ productData.stock || 0 }} {{ productData.unit || '件' }}
                </span>
                <el-tag v-if="productData.stock === 0" type="danger" size="mini" class="stock-status-tag">缺货</el-tag>
                <el-tag v-else-if="productData.stock > 0 && productData.stock <= 10" type="warning" size="mini" class="stock-status-tag">紧张</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="计量单位">{{ productData.unit || '--' }}</el-descriptions-item>
              <el-descriptions-item label="商品重量">{{ productData.weight || '--' }}</el-descriptions-item>
              <el-descriptions-item label="商品产地">{{ productData.origin || '--' }}</el-descriptions-item>
              <el-descriptions-item label="商品品牌">{{ productData.brand || '--' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatFullTime(productData.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ formatFullTime(productData.updateTime) }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>

        <el-tab-pane label="商家信息" name="merchant">
          <div v-if="productData.merchantName" class="tab-content">
            <div class="merchant-card">
              <div class="merchant-header">
                <el-avatar :src="productData.merchantAvatar" :size="60" class="merchant-avatar-large">
                  {{ productData.merchantName.charAt(0) }}
                </el-avatar>
                <div class="merchant-title">
                  <h3>{{ productData.merchantName }}</h3>
                  <p>商家ID: {{ productData.merchantId }}</p>
                </div>
                <div class="merchant-rating">
                  <el-rate v-model="merchantRating" disabled show-score text-color="#ff9900" score-template="{value} 分" />
                </div>
              </div>
              <div class="merchant-details">
                <el-descriptions :column="2" border class="merchant-descriptions">
                  <el-descriptions-item label="商家评分"><span class="rating-score">4.8</span></el-descriptions-item>
                  <el-descriptions-item label="合作时间">{{ formatTime(productData.merchantTime) }}</el-descriptions-item>
                  <el-descriptions-item label="在售商品"><span class="product-count">25 个</span></el-descriptions-item>
                  <el-descriptions-item label="累计销量"><span class="sales-count">1,258 件</span></el-descriptions-item>
                  <el-descriptions-item label="商家等级"><el-tag type="success" size="small">金牌商家</el-tag></el-descriptions-item>
                  <el-descriptions-item label="联系方式"><span class="contact-info">138****5678</span></el-descriptions-item>
                  <el-descriptions-item label="发货地址" :span="2"><span class="address-info">北京市朝阳区望京街10号</span></el-descriptions-item>
                  <el-descriptions-item label="商家描述" :span="2">
                    <span class="merchant-description">这是一家优质的商家，提供高质量的商品和良好的服务。</span>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
              <div class="merchant-actions">
                <el-button type="primary" icon="el-icon-chat-line-square" @click="handleContactMerchant">联系商家</el-button>
                <el-button type="info" icon="el-icon-view" @click="handleViewMerchant">查看商家详情</el-button>
              </div>
            </div>
          </div>
          <div v-else class="no-merchant-info">
            <i class="el-icon-info"></i>
            <p>暂无商家信息</p>
          </div>
        </el-tab-pane>

        <el-tab-pane label="数据统计" name="statistics">
          <div class="tab-content">
            <div class="stats-grid">
              <div v-for="stat in statisticsData" :key="stat.label" class="stat-card" :style="{ background: getStatCardBackground(stat.type) }">
                <div class="stat-icon"><i :class="stat.icon"></i></div>
                <div class="stat-content">
                  <div class="stat-title">{{ stat.label }}</div>
                  <div class="stat-value">{{ stat.value }}</div>
                  <div :class="['stat-trend', stat.trend.type === 'success' ? 'trend-up' : 'trend-down']">
                    <i :class="stat.trend.icon"></i> {{ stat.trend.value }}
                  </div>
                </div>
              </div>
            </div>
            <div class="stats-details">
              <h4>详细数据</h4>
              <el-table :data="statsDetails" stripe class="stats-table">
                <el-table-column prop="label" label="统计项" width="120" />
                <el-table-column prop="value" label="数值" width="120" />
                <el-table-column label="趋势" width="100">
                  <template slot-scope="{ row }">
                    <span :class="['trend-indicator', row.trend.type === 'success' ? 'trend-up' : 'trend-down']">
                      <i :class="row.trend.icon"></i> {{ row.trend.value }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="对比" width="100">
                  <template slot-scope="{ row }">
                    <span :class="['compare-indicator', row.compare > 0 ? 'compare-up' : row.compare < 0 ? 'compare-down' : '']">
                      {{ row.compare > 0 ? '+' : '' }}{{ row.compare }}%
                    </span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="操作记录" name="logs">
          <div class="tab-content">
            <div v-if="operationLogs.length > 0" class="operation-logs">
              <div v-for="(log, index) in operationLogs" :key="index" class="log-item">
                <div class="log-time">
                  <span class="time-date">{{ formatTime(log.time) }}</span>
                  <span class="time-full">{{ formatFullTime(log.time) }}</span>
                </div>
                <div class="log-content">
                  <div class="log-header">
                    <el-tag :type="getLogTagType(log.action)" size="small" class="log-action-tag">
                      {{ getLogActionText(log.action) }}
                    </el-tag>
                    <span class="log-operator">{{ log.operator }}</span>
                  </div>
                  <div v-if="log.details" class="log-details">
                    <p class="log-detail-text">{{ log.details }}</p>
                  </div>
                  <div v-if="log.changes && Object.keys(log.changes).length > 0" class="log-changes">
                    <div v-for="(change, field) in log.changes" :key="field" class="log-change">
                      <span class="change-field">{{ getFieldText(field) }}：</span>
                      <span class="change-from">{{ change.from }}</span>
                      <span class="change-arrow">→</span>
                      <span class="change-to">{{ change.to }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="no-logs">
              <i class="el-icon-notebook-2"></i>
              <p>暂无操作记录</p>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <el-dialog
        :visible.sync="editVisible"
        title="编辑商品"
        width="700px"
        :close-on-click-modal="false"
        append-to-body
        @closed="handleEditClosed"
      >
        <el-form
          ref="editFormRef"
          :model="editForm"
          :rules="editRules"
          label-width="100px"
        >
          <div class="form-section">
            <h4 class="form-section-title">
              <i class="el-icon-edit"></i> 基本信息
            </h4>
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="editForm.name" placeholder="请输入商品名称" clearable />
            </el-form-item>
            <el-form-item label="商品描述" prop="description">
              <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
            </el-form-item>
            <el-form-item label="商品分类" prop="category">
              <el-select v-model="editForm.category" placeholder="请选择商品分类" clearable style="width: 100%;">
                <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="计量单位" prop="unit">
              <el-input v-model="editForm.unit" placeholder="如：件、千克、个" />
            </el-form-item>
            <el-form-item label="商品图片">
              <div class="image-upload-area">
                <div class="image-preview" v-if="editForm.image">
                  <img :src="editForm.image" class="preview-img" />
                  <div class="image-mask">
                    <i class="el-icon-delete" @click="editForm.image = ''"></i>
                  </div>
                </div>
                <el-upload
                  v-else
                  action="#"
                  :auto-upload="false"
                  :show-file-list="false"
                  :on-change="handleImageChange"
                  accept="image/*"
                  class="image-uploader"
                >
                  <i class="el-icon-plus"></i>
                  <span>上传图片</span>
                </el-upload>
              </div>
            </el-form-item>
          </div>

          <div class="form-section">
            <h4 class="form-section-title">
              <i class="el-icon-price-tag"></i> 价格与库存
            </h4>
            <el-form-item label="商品价格" prop="price">
              <el-input-number v-model="editForm.price" :min="0" :max="999999" :precision="2" :step="0.1" :controls="false" placeholder="请输入价格" style="width: 100%;" />
            </el-form-item>
            <el-form-item label="原价">
              <el-input-number v-model="editForm.originalPrice" :min="0" :max="999999" :precision="2" :step="0.1" :controls="false" placeholder="请输入原价" style="width: 100%;" />
            </el-form-item>
            <el-form-item label="库存数量" prop="stock">
              <el-input-number v-model="editForm.stock" :min="0" :max="999999" :controls="false" placeholder="请输入库存数量" style="width: 100%;" />
            </el-form-item>
          </div>

          <div class="form-section">
            <h4 class="form-section-title">
              <i class="el-icon-info"></i> 其他信息
            </h4>
            <el-form-item label="商品重量">
              <el-input v-model="editForm.weight" placeholder="如：500g" />
            </el-form-item>
            <el-form-item label="商品产地">
              <el-input v-model="editForm.origin" placeholder="请输入产地" />
            </el-form-item>
            <el-form-item label="商品品牌">
              <el-input v-model="editForm.brand" placeholder="请输入品牌" />
            </el-form-item>
          </div>

          <div class="form-section">
            <h4 class="form-section-title">
              <i class="el-icon-setting"></i> 状态设置
            </h4>
            <el-form-item label="商品状态" prop="status">
              <el-select v-model="editForm.status" placeholder="请选择商品状态" style="width: 100%;">
                <el-option label="上架" value="approved" />
                <el-option label="下架" value="off" />
              </el-select>
            </el-form-item>
            <el-form-item label="推荐商品">
              <el-switch v-model="editForm.isRecommend" :active-value="true" :inactive-value="false" active-text="是" inactive-text="否" />
            </el-form-item>
          </div>
        </el-form>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="editVisible = false">取消</el-button>
            <el-button type="primary" :loading="editSaving" @click="handleEditSave">
              {{ editSaving ? '保存中...' : '保存修改' }}
            </el-button>
          </div>
        </template>
      </el-dialog>

      <el-dialog
        :visible.sync="merchantDetailVisible"
        title="商家详情"
        width="650px"
        :close-on-click-modal="false"
        append-to-body
      >
        <div v-if="merchantInfo" class="merchant-detail-dialog">
          <div class="merchant-detail-header">
            <el-avatar :src="merchantInfo.avatar" :size="70">
              {{ (merchantInfo.name || '商').charAt(0) }}
            </el-avatar>
            <div class="merchant-detail-title">
              <h3>{{ merchantInfo.name }}</h3>
              <el-tag type="success" size="small">金牌商家</el-tag>
            </div>
          </div>
          <el-descriptions :column="2" border class="merchant-detail-descriptions">
            <el-descriptions-item label="商家ID">{{ merchantInfo.id }}</el-descriptions-item>
            <el-descriptions-item label="商家评分">
              <el-rate v-model="merchantInfo.rating" disabled show-score text-color="#ff9900" />
            </el-descriptions-item>
            <el-descriptions-item label="合作时间">{{ merchantInfo.cooperateTime }}</el-descriptions-item>
            <el-descriptions-item label="在售商品">{{ merchantInfo.productCount }} 个</el-descriptions-item>
            <el-descriptions-item label="累计销量">{{ merchantInfo.totalSales }} 件</el-descriptions-item>
            <el-descriptions-item label="好评率">{{ merchantInfo.goodRate }}%</el-descriptions-item>
            <el-descriptions-item label="联系方式">{{ merchantInfo.phone }}</el-descriptions-item>
            <el-descriptions-item label="发货地址" :span="2">{{ merchantInfo.address }}</el-descriptions-item>
            <el-descriptions-item label="商家描述" :span="2">{{ merchantInfo.description }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-else class="no-merchant-info">
          <i class="el-icon-info"></i>
          <p>暂无商家详情信息</p>
        </div>
        <template #footer>
          <el-button @click="merchantDetailVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleContactMerchantFromDetail">联系商家</el-button>
        </template>
      </el-dialog>

      <el-dialog
        :visible.sync="contactVisible"
        title="联系商家"
        width="500px"
        :close-on-click-modal="false"
        append-to-body
      >
        <el-form :model="contactForm" label-width="80px">
          <el-form-item label="商家名称">
            <el-input :value="merchantInfo ? merchantInfo.name : ''" disabled />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input :value="merchantInfo ? merchantInfo.phone : ''" disabled />
          </el-form-item>
          <el-form-item label="留言主题" prop="subject">
            <el-input v-model="contactForm.subject" placeholder="请输入留言主题" />
          </el-form-item>
          <el-form-item label="留言内容" prop="content">
            <el-input v-model="contactForm.content" type="textarea" :rows="4" placeholder="请输入您想对商家说的话" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="contactVisible = false">取消</el-button>
          <el-button type="primary" :loading="contactSending" @click="handleSendMessage">
            {{ contactSending ? '发送中...' : '发送留言' }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from '@vue/composition-api'
import { Message, MessageBox } from 'element-ui'
import { getProductById, updateProduct, deleteProduct, uploadFile } from '@/api'

export default {
  name: 'ProductDetail',
  props: {
    visible: { type: Boolean, default: false },
    product: { type: Object, default: null }
  },
  setup(props, { root, emit }) {
    const activeTab = ref('basic')
    const merchantRating = ref(4.8)
    const productData = ref(null)
    const loading = ref(false)
    const operationLogs = ref([])

    const editVisible = ref(false)
    const editSaving = ref(false)
    const editFormRef = ref(null)
    const editForm = reactive({
      id: '', name: '', description: '', category: '', price: 0, originalPrice: 0,
      stock: 0, unit: '件', weight: '', origin: '', brand: '', status: 'approved', isRecommend: false,
      image: ''
    })
    const editRules = {
      name: [
        { required: true, message: '请输入商品名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      category: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
      price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
      stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
    }
    const categoryOptions = [
      { label: '蔬菜', value: 'vegetable' },
      { label: '水果', value: 'fruit' },
      { label: '肉类', value: 'meat' },
      { label: '粮食', value: 'grain' },
      { label: '加工产品', value: 'processed' },
      { label: '其他', value: 'other' }
    ]

    const merchantDetailVisible = ref(false)
    const contactVisible = ref(false)
    const contactSending = ref(false)
    const merchantInfo = ref(null)
    const contactForm = reactive({
      subject: '',
      content: ''
    })

    const isDialog = computed(() => props.product !== null)
    const dialogVisible = computed({
      get: () => props.visible,
      set: (val) => emit('update:visible', val)
    })

    const productId = computed(() => {
      if (props.product && props.product.id) return props.product.id
      return root.$route.params.id
    })

    const statisticsData = computed(() => {
      if (!productData.value) return []
      return [
        { label: '浏览量', value: productData.value.views || 0, type: 'views', icon: 'el-icon-view', trend: { type: 'success', icon: 'el-icon-top', value: '+0%' } },
        { label: '收藏数', value: productData.value.favorites || 0, type: 'favorites', icon: 'el-icon-star-off', trend: { type: 'success', icon: 'el-icon-top', value: '+0%' } },
        { label: '评价数', value: productData.value.reviews || 0, type: 'reviews', icon: 'el-icon-chat-line-square', trend: { type: 'success', icon: 'el-icon-top', value: '+0%' } },
        { label: '总销量', value: productData.value.sales || 0, type: 'sales', icon: 'el-icon-s-data', trend: { type: 'success', icon: 'el-icon-top', value: '+12%' } }
      ]
    })

    const statsDetails = computed(() => {
      if (!productData.value) return []
      return [
        { label: '今日销量', value: Math.floor(productData.value.sales * 0.1) || 0, trend: { type: 'success', icon: 'el-icon-top', value: '+5%' }, compare: 5 },
        { label: '本周销量', value: Math.floor(productData.value.sales * 0.3) || 0, trend: { type: 'success', icon: 'el-icon-top', value: '+8%' }, compare: 8 },
        { label: '本月销量', value: productData.value.sales || 0, trend: { type: 'success', icon: 'el-icon-top', value: '+12%' }, compare: 12 },
        { label: '转化率', value: '2.8%', trend: { type: 'success', icon: 'el-icon-top', value: '+0.5%' }, compare: 0.5 }
      ]
    })

    const loadProduct = async () => {
      const id = productId.value
      if (!id || isNaN(Number(id))) {
        console.warn('无效的商品ID:', id)
        productData.value = null
        loading.value = false
        return
      }
      loading.value = true
      try {
        const result = await getProductById(id)
        const data = result.data || result
        if (data && data.id) {
          productData.value = data
          loadOperationLogs()
        } else {
          productData.value = null
        }
      } catch (e) {
        console.error('加载商品详情失败:', e)
        productData.value = null
      } finally {
        loading.value = false
      }
    }

    const loadOperationLogs = () => {
      if (!productData.value) return
      operationLogs.value = [
        { action: 'update', operator: '管理员', details: '更新了商品信息', changes: { price: { from: '¥100.00', to: '¥120.00' }, stock: { from: 50, to: 100 } }, time: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString() },
        { action: 'shelf', operator: '系统', details: '商品上架', changes: { status: { from: '下架', to: '在售' } }, time: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString() },
        { action: 'create', operator: '商家', details: '创建了商品', changes: {}, time: new Date(Date.now() - 48 * 60 * 60 * 1000).toISOString() }
      ]
    }

    const handleEdit = () => {
      if (!productData.value) return
      Object.assign(editForm, {
        id: productData.value.id,
        name: productData.value.name || '',
        description: productData.value.description || '',
        category: productData.value.category || '',
        price: productData.value.price || 0,
        originalPrice: productData.value.originalPrice || 0,
        stock: productData.value.stock || 0,
        unit: productData.value.unit || '件',
        weight: productData.value.weight || '',
        origin: productData.value.origin || '',
        brand: productData.value.brand || '',
        status: productData.value.status || 'approved',
        isRecommend: productData.value.isRecommend || false,
        image: productData.value.image || ''
      })
      editVisible.value = true
    }

    const handleEditSave = async () => {
      try {
        await editFormRef.value.validate()
      } catch {
        return
      }
      editSaving.value = true
      try {
        const updateData = { ...editForm }
        if (editForm.image && editForm.image.startsWith('data:')) {
          const blob = await fetch(editForm.image).then(r => r.blob())
          const formData = new FormData()
          formData.append('file', blob, 'product_image.png')
          const uploadRes = await uploadFile(formData)
          updateData.image = uploadRes.data || uploadRes.url || editForm.image
        }
        await updateProduct(editForm.id, updateData)
        Object.assign(productData.value, updateData)
        productData.value.updateTime = new Date().toISOString()
        emit('update:product', { ...productData.value })
        editVisible.value = false
        Message.success('商品信息更新成功')
      } catch {
        Message.error('更新失败，请重试')
      } finally {
        editSaving.value = false
      }
    }

    const handleEditClosed = () => {
      if (editFormRef.value) {
        editFormRef.value.resetFields()
      }
    }

    const handleImageChange = (file) => {
      const reader = new FileReader()
      reader.onload = (e) => {
        editForm.image = e.target.result
      }
      reader.readAsDataURL(file.raw)
    }

    const handleToggleStatus = () => {
      const newStatus = productData.value.status === 'approved' ? 'off' : 'approved'
      const action = newStatus === 'approved' ? '上架' : '下架'
      MessageBox.confirm(`确定要${action}商品 "${productData.value.name}" 吗？`, '确认操作', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await updateProduct(productData.value.id, { status: newStatus })
        productData.value.status = newStatus
        productData.value.updateTime = new Date().toISOString()
        emit('update:product', { ...productData.value })
        Message.success(`商品已${action}`)
      }).catch(() => {})
    }

    const handleCopyLink = () => {
      const baseUrl = window.location.origin
      const link = `${baseUrl}/product/${productData.value.id}`
      const textarea = document.createElement('textarea')
      textarea.value = link
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        Message.success('商品链接已复制到剪贴板')
      } catch {
        Message.error('复制失败，请手动复制')
      }
      document.body.removeChild(textarea)
    }

    const handleDropdownCommand = (cmd) => {
      if (cmd.action === 'view-logs') {
        activeTab.value = 'logs'
      } else if (cmd.action === 'delete') {
        handleDelete()
      }
    }

    const handleDelete = () => {
      MessageBox.confirm(`确定要删除商品 "${productData.value.name}" 吗？删除后无法恢复！`, '确认删除', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }).then(async () => {
        await deleteProduct(productData.value.id)
        Message.success('商品删除成功')
        if (isDialog.value) {
          emit('update:visible', false)
        } else {
          root.$router.push('/products')
        }
      }).catch(() => {})
    }

    const handleContactMerchant = () => {
      if (!productData.value) return
      merchantInfo.value = {
        id: productData.value.merchantId || 'M001',
        name: productData.value.merchantName || '优质商家',
        avatar: productData.value.merchantAvatar || '',
        rating: 4.8,
        cooperateTime: formatTime(productData.value.merchantTime) || '2024-01-15',
        productCount: 25,
        totalSales: '1,258',
        goodRate: 98,
        phone: '138****5678',
        address: '北京市朝阳区望京街10号',
        description: '这是一家优质的商家，提供高质量的商品和良好的服务。'
      }
      contactForm.subject = ''
      contactForm.content = ''
      contactVisible.value = true
    }

    const handleViewMerchant = () => {
      if (!productData.value) return
      merchantInfo.value = {
        id: productData.value.merchantId || 'M001',
        name: productData.value.merchantName || '优质商家',
        avatar: productData.value.merchantAvatar || '',
        rating: 4.8,
        cooperateTime: formatTime(productData.value.merchantTime) || '2024-01-15',
        productCount: 25,
        totalSales: '1,258',
        goodRate: 98,
        phone: '138****5678',
        address: '北京市朝阳区望京街10号',
        description: '这是一家优质的商家，提供高质量的商品和良好的服务。'
      }
      merchantDetailVisible.value = true
    }

    const handleContactMerchantFromDetail = () => {
      merchantDetailVisible.value = false
      contactForm.subject = ''
      contactForm.content = ''
      contactVisible.value = true
    }

    const handleSendMessage = () => {
      if (!contactForm.subject.trim()) {
        Message.warning('请输入留言主题')
        return
      }
      if (!contactForm.content.trim()) {
        Message.warning('请输入留言内容')
        return
      }
      contactSending.value = true
      setTimeout(() => {
        contactSending.value = false
        contactVisible.value = false
        Message.success('留言已发送，商家会尽快回复您')
      }, 1000)
    }

    const handleDialogClosed = () => {
      productData.value = null
    }

    const getCategoryLabel = (category) => {
      const categoryMap = {
        'vegetable': '蔬菜', 'fruit': '水果', 'meat': '肉类',
        'grain': '粮食', 'processed': '加工产品', 'other': '其他'
      }
      return categoryMap[category] || category
    }

    const getStatusText = (status) => {
      const statusMap = { 'approved': '在售', 'off': '下架', 'sold_out': '缺货' }
      return statusMap[status] || '未知'
    }

    const getStatusTagType = (status) => {
      const types = { 'approved': 'success', 'off': 'warning', 'sold_out': 'danger' }
      return types[status] || ''
    }

    const getCategoryTagType = (category) => {
      const types = {
        'vegetable_fruit': 'success', 'meat_egg_milk': 'warning', 'grain_oil': '',
        'seafood': 'info', 'dry_goods': 'warning', 'organic': 'success',
        'fast_food': 'info', 'beverage': ''
      }
      return types[category] || ''
    }

    const getStockClass = (stock) => {
      if (stock === undefined || stock === null) return ''
      if (stock === 0) return 'stock-zero'
      if (stock <= 10) return 'stock-low'
      return 'stock-normal'
    }

    const getImageUrl = (image) => {
      if (!image) return ''
      if (image.startsWith('data:image') || image.startsWith('http')) return image
      if (image.startsWith('/imgs/') || image.startsWith('imgs/')) return image.startsWith('/') ? image : `/${image}`
      if (image.startsWith('/')) return `/imgs${image}`
      return `/imgs/products/${image}`
    }

    const getDefaultImage = (id) => {
      const index = id ? (Number(id) % 11) + 1 : Math.floor(Math.random() * 11) + 1
      return `/imgs/foods/${index}.png`
    }

    const handleImageError = (e) => {
      e.target.src = getDefaultImage(productData.value ? productData.value.id : 1)
    }

    const getStatCardBackground = (type) => {
      const backgrounds = {
        'views': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'favorites': 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'reviews': 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)',
        'sales': 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
      }
      return backgrounds[type] || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }

    const getLogTagType = (action) => {
      const types = { 'create': 'success', 'update': 'primary', 'delete': 'danger', 'shelf': 'warning', 'stock': 'info', 'price': 'success' }
      return types[action] || 'info'
    }

    const getLogActionText = (action) => {
      const texts = { 'create': '创建', 'update': '更新', 'delete': '删除', 'shelf': '上架/下架', 'stock': '库存调整', 'price': '价格调整' }
      return texts[action] || action
    }

    const getFieldText = (field) => {
      const fields = { 'name': '商品名称', 'price': '商品价格', 'stock': '商品库存', 'category': '商品分类', 'status': '商品状态', 'description': '商品描述', 'isRecommend': '推荐状态', 'unit': '计量单位' }
      return fields[field] || field
    }

    const formatTime = (time) => {
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
    }

    const formatFullTime = (time) => {
      if (!time) return '从未'
      return new Date(time).toLocaleString()
    }

    watch(() => props.product, (val) => {
      if (val) {
        productData.value = { ...val }
        loadOperationLogs()
      }
    }, { immediate: true })

    onMounted(() => {
      if (!isDialog.value) {
        loadProduct()
      }
    })

    return {
      activeTab, merchantRating, productData, loading, operationLogs, productId,
      isDialog, dialogVisible,
      editVisible, editSaving, editForm, editRules, editFormRef, categoryOptions,
      statisticsData, statsDetails,
      handleEdit, handleEditSave, handleEditClosed, handleImageChange,
      handleToggleStatus, handleCopyLink, handleDropdownCommand, handleDelete,
      handleContactMerchant, handleViewMerchant, handleDialogClosed,
      handleContactMerchantFromDetail, handleSendMessage,
      merchantDetailVisible, contactVisible, contactSending, merchantInfo, contactForm,
      getCategoryLabel, getStatusText, getStatusTagType, getCategoryTagType,
      getStockClass, getImageUrl, getDefaultImage, handleImageError, getStatCardBackground, getLogTagType, getLogActionText,
      getFieldText, formatTime, formatFullTime
    }
  }
}
</script>

<style scoped>
.product-detail-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.form-section {
  margin-bottom: 20px;
}

.form-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.image-upload-area {
  display: flex;
  align-items: center;
}

.image-preview {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
}

.image-preview:hover .image-mask {
  opacity: 1;
}

.image-mask i {
  color: #fff;
  font-size: 24px;
}

.image-uploader {
  width: 120px;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.image-uploader:hover {
  border-color: #409eff;
}

.image-uploader i {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 4px;
}

.image-uploader span {
  font-size: 12px;
  color: #8c939d;
}

.merchant-detail-dialog {
  padding: 10px 0;
}

.merchant-detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.merchant-detail-title h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
}

.merchant-detail-descriptions {
  margin-top: 10px;
}

.product-detail-page.dialog-mode {
  padding: 0;
  background: transparent;
  min-height: auto;
}

.product-detail-dialog >>> .el-dialog__body {
  padding: 16px 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  color: #909399;
}

.loading-container i,
.empty-container i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.loading-container p,
.empty-container p {
  font-size: 16px;
  margin-bottom: 24px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.detail-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.detail-header {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.product-basic-info {
  display: flex;
  gap: 20px;
}

.product-image-section {
  flex: 0 0 300px;
  position: relative;
}

.detail-image {
  width: 300px;
  height: 300px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.detail-image:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.detail-image-placeholder {
  width: 300px;
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  border: 2px dashed #dcdfe6;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 14px;
}

.detail-image-placeholder i {
  font-size: 40px;
  margin-bottom: 8px;
  color: #c0c4cc;
}

.image-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-badge {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.product-base-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  line-height: 1.4;
}

.product-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.meta-label {
  color: #909399;
  font-weight: 500;
}

.meta-value {
  color: #303133;
  font-weight: 500;
}

.product-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding-top: 12px;
}

.more-actions-dropdown {
  margin-left: auto;
}

.detail-tabs {
  margin-top: 20px;
}

:deep(.el-tabs__header) {
  margin: 0;
  padding: 0 20px;
  background: #f5f7fa;
  border-radius: 8px 8px 0 0;
}

:deep(.el-tabs__item) {
  padding: 0 20px;
  height: 48px;
  line-height: 48px;
  font-size: 15px;
  font-weight: 500;
  color: #606266;
  transition: all 0.3s ease;
}

:deep(.el-tabs__item:hover) {
  color: #409eff;
}

:deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 600;
}

:deep(.el-tabs__active-bar) {
  background-color: #409eff;
  height: 3px;
  border-radius: 3px 3px 0 0;
}

.tab-content {
  padding: 20px;
  background: #ffffff;
  border-radius: 0 0 8px 8px;
  min-height: 300px;
}

.product-descriptions {
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

:deep(.el-descriptions__label) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 500;
  padding: 12px 16px;
  width: 120px;
  text-align: right;
  border-right: 1px solid #e4e7ed;
}

:deep(.el-descriptions__content) {
  background-color: #ffffff;
  color: #303133;
  padding: 12px 16px;
}

.price-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-price {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.original-price {
  font-size: 14px;
  color: #909399;
  text-decoration: line-through;
}

.stock-info {
  font-weight: 600;
  font-size: 16px;
  margin-right: 8px;
}

.stock-info.stock-normal {
  color: #67c23a;
}

.stock-info.stock-low {
  color: #e6a23c;
}

.stock-info.stock-zero {
  color: #f56c6c;
}

.stock-status-tag {
  font-size: 12px;
  height: 20px;
  line-height: 18px;
  padding: 0 6px;
}

.merchant-card {
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.merchant-header {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  gap: 20px;
  color: white;
}

.merchant-avatar-large {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.2);
  font-size: 24px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.merchant-title {
  flex: 1;
}

.merchant-title h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: white;
}

.merchant-title p {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.merchant-rating {
  flex-shrink: 0;
}

:deep(.el-rate__text) {
  font-size: 16px;
  font-weight: 600;
  color: white;
  margin-left: 8px;
}

.merchant-details {
  padding: 20px;
}

.merchant-descriptions {
  background: #f9f9f9;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.rating-score {
  font-size: 20px;
  font-weight: 700;
  color: #e6a23c;
}

.product-count,
.sales-count {
  font-weight: 600;
  color: #409eff;
}

.contact-info {
  color: #303133;
  font-weight: 500;
}

.address-info {
  color: #606266;
  line-height: 1.5;
}

.merchant-description {
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
}

.merchant-actions {
  padding: 20px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  gap: 12px;
  justify-content: center;
}

.no-merchant-info {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.no-merchant-info i {
  font-size: 60px;
  color: #dcdfe6;
  margin-bottom: 20px;
  display: block;
}

.no-merchant-info p {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 30px;
}

.stat-card {
  border-radius: 8px;
  padding: 20px;
  color: white;
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 40px;
  opacity: 0.9;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
  line-height: 1.2;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.stats-details {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e4e7ed;
}

.stats-details h4 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.stats-table {
  width: 100%;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.trend-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
  padding: 2px 6px;
  border-radius: 4px;
}

.trend-indicator.trend-up {
  color: #67c23a;
  background-color: #f0f9eb;
}

.trend-indicator.trend-down {
  color: #f56c6c;
  background-color: #fef0f0;
}

.compare-indicator {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
}

.compare-indicator.compare-up {
  color: #67c23a;
  background-color: #f0f9eb;
}

.compare-indicator.compare-down {
  color: #f56c6c;
  background-color: #fef0f0;
}

.operation-logs {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 8px;
}

.log-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  gap: 20px;
  transition: all 0.3s ease;
}

.log-item:hover {
  background-color: #f9f9f9;
  padding-left: 8px;
  padding-right: 8px;
  margin-left: -8px;
  margin-right: -8px;
  border-radius: 6px;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  flex: 0 0 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 4px;
}

.time-date {
  font-size: 13px;
  color: #409eff;
  font-weight: 600;
  margin-bottom: 2px;
}

.time-full {
  font-size: 12px;
  color: #909399;
  text-align: center;
}

.log-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.log-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.log-action-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

.log-operator {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.log-details {
  margin-top: 4px;
}

.log-detail-text {
  margin: 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

.log-changes {
  margin-top: 8px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

.log-change {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 13px;
  line-height: 1.4;
}

.log-change:last-child {
  margin-bottom: 0;
}

.change-field {
  font-weight: 600;
  color: #303133;
  min-width: 80px;
  text-align: right;
}

.change-from {
  color: #f56c6c;
  text-decoration: line-through;
  font-weight: 500;
}

.change-arrow {
  color: #409eff;
  font-weight: bold;
  margin: 0 4px;
}

.change-to {
  color: #67c23a;
  font-weight: 600;
}

.no-logs {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.no-logs i {
  font-size: 60px;
  color: #dcdfe6;
  margin-bottom: 20px;
  display: block;
}

.no-logs p {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

@media (max-width: 768px) {
  .product-basic-info {
    flex-direction: column;
  }

  .product-image-section {
    text-align: center;
  }

  .detail-image {
    width: 150px;
    height: 150px;
  }

  .detail-image-placeholder {
    width: 150px;
    height: 150px;
    margin: 0 auto;
  }

  .product-actions {
    flex-direction: column;
  }

  .product-actions .el-button {
    width: 100%;
  }

  .more-actions-dropdown {
    margin-left: 0;
  }

  .merchant-header {
    flex-direction: column;
    text-align: center;
  }

  .merchant-actions {
    flex-direction: column;
  }

  .merchant-actions .el-button {
    width: 100%;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .log-item {
    flex-direction: column;
    gap: 12px;
  }

  .log-time {
    flex: none;
    width: 100%;
    align-items: flex-start;
  }

  .log-time .time-full {
    text-align: left;
  }
}

@media (max-width: 480px) {
  .product-meta {
    flex-direction: column;
    gap: 8px;
  }
}

  .meta-item {
    width: 100%;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    font-size: 32px;
  }

  .stat-value {
    font-size: 20px;
  }

  .product-descriptions {
    padding: 12px;
  }

  :deep(.el-descriptions__label) {
    width: 100px;
  }
</style>
