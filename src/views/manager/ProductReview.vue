/**
 * 商品审核详情页面
 * 文件路径: src/views/manager/ProductReview.vue
 * 功能描述: 审核单个商品详细信息，顶部操作栏显示商品名称、审核状态标签、分类标签、提交时间，
 *           审核操作组（审核通过/退回修改/拒绝），退回/拒绝时需填写原因并提供常用理由快捷选择，
 *           下方Tab展示商品详情Tab（基本信息、图片、描述、价格库存）和商家信息Tab，支持返回列表和刷新
 * 关联文件:
 * - src/api/index.js: 提供商品审核接口
 * - src/views/manager/PendingProducts.vue: 商品审核列表页面
 */
<template>
  <div class="product-review-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <i class="el-icon-s-data"></i>
            商品审核详情
          </h1>
          <p class="page-subtitle">审核商品详细信息，确保商品质量和合规性</p>
        </div>
        
        <div class="header-right">
          <div class="header-actions">
            <el-button 
              @click="$router.back()" 
              class="back-btn"
              plain
              size="medium"
            >
              <i class="el-icon-arrow-left"></i>
              返回列表
            </el-button>
            
            <el-tooltip content="刷新页面" placement="top">
              <el-button 
                type="text" 
                @click="refreshPage"
                class="refresh-btn"
              >
                <i class="el-icon-refresh"></i>
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </div>
    </div>

    <!-- 审核操作栏 -->
    <div class="review-actions-bar">
      <el-card shadow="never" class="actions-card">
        <div class="actions-content">
          <!-- 商品基本信息 -->
          <div class="product-basic-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <div class="product-meta">
              <el-tag 
                :type="getStatusTagType(product.status)"
                class="status-tag"
                effect="dark"
              >
                {{ getStatusText(product.status) }}
              </el-tag>
              
              <el-tag 
                :type="getCategoryTagType(product.category)"
                class="category-tag"
              >
                {{ getCategoryText(product.category) }}
              </el-tag>
              
              <span class="submit-time">
                <i class="el-icon-time"></i>
                提交时间：{{ formatFullTime(product.submitTime || product.createTime) }}
              </span>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button-group>
              <el-button 
                type="success" 
                size="medium" 
                @click="handleApprove"
                :loading="reviewing"
                class="approve-btn"
              >
                <i class="el-icon-check"></i>
                审核通过
              </el-button>
              
              <el-button 
                type="warning" 
                size="medium" 
                @click="handleReturn"
                :loading="reviewing"
                class="return-btn"
              >
                <i class="el-icon-refresh"></i>
                退回修改
              </el-button>
              
              <el-button 
                type="danger" 
                size="medium" 
                @click="handleReject"
                :loading="reviewing"
                class="reject-btn"
              >
                <i class="el-icon-close"></i>
                拒绝通过
              </el-button>
            </el-button-group>
            
            <div class="quick-actions">
              <el-button 
                type="text" 
                @click="handlePreview"
                class="preview-btn"
              >
                <i class="el-icon-view"></i>
                预览效果
              </el-button>
              
              <el-button 
                type="text" 
                @click="handleCopyLink"
                class="copy-link-btn"
              >
                <i class="el-icon-link"></i>
                复制链接
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 商品详情内容 -->
    <div class="product-detail-content">
      <el-row :gutter="20">
        <!-- 左侧：商品详细信息 -->
        <el-col :span="16">
          <!-- 基础信息卡片 -->
          <el-card shadow="never" class="detail-card">
            <template #header>
              <h3 class="section-title">
                <i class="el-icon-s-goods"></i>
                商品基本信息
              </h3>
            </template>
            
            <div class="basic-info-grid">
              <div class="info-row">
                <div class="info-item">
                  <span class="info-label">商品名称：</span>
                  <span class="info-value">{{ product.name || '未填写' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">商品分类：</span>
                  <el-tag 
                    :type="getCategoryTagType(product.category)"
                    class="category-tag"
                  >
                    {{ getCategoryText(product.category) }}
                  </el-tag>
                </div>
              </div>
              
              <div class="info-row">
                <div class="info-item">
                  <span class="info-label">商品价格：</span>
                  <span class="info-value price-value">¥{{ (product.price || 0).toFixed(2) }}</span>
                  <span v-if="product.originalPrice" class="original-price">
                    ¥{{ product.originalPrice.toFixed(2) }}
                  </span>
                </div>
                <div class="info-item">
                  <span class="info-label">商品库存：</span>
                  <span class="info-value stock-value" :class="getStockClass(product.stock)">
                    {{ product.stock || 0 }}
                  </span>
                  <el-tag 
                    :type="getStockStatus(product.stock).type"
                    size="mini"
                    class="stock-status-tag"
                  >
                    {{ getStockStatus(product.stock).text }}
                  </el-tag>
                </div>
              </div>
              
              <div class="info-row">
                <div class="info-item">
                  <span class="info-label">商品单位：</span>
                  <span class="info-value">{{ product.unit || '件' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">商品重量：</span>
                  <span class="info-value">{{ product.weight || '0' }} {{ product.unit || 'kg' }}</span>
                </div>
              </div>
              
              <div class="info-row">
                <div class="info-item">
                  <span class="info-label">商品品牌：</span>
                  <span class="info-value">{{ product.brand || '无品牌' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">商品产地：</span>
                  <span class="info-value">{{ product.origin || '未填写' }}</span>
                </div>
              </div>
              
              <div class="info-row">
                <div class="info-item full-width">
                  <span class="info-label">商品来源：</span>
                  <el-tag 
                    :type="getSourceTagType(product.source)"
                    v-if="product.source"
                  >
                    {{ getSourceText(product.source) }}
                  </el-tag>
                  <span v-else class="info-value">未指定</span>
                </div>
              </div>
              
              <div class="info-row" v-if="product.quality">
                <div class="info-item full-width">
                  <span class="info-label">质量评级：</span>
                  <el-rate
                    v-model="product.quality"
                    disabled
                    show-score
                    text-color="#ff9900"
                    score-template="{value} 分"
                    class="quality-rate"
                  />
                </div>
              </div>
            </div>
          </el-card>
          
          <!-- 商品描述卡片 -->
          <el-card shadow="never" class="detail-card">
            <template #header>
              <h3 class="section-title">
                <i class="el-icon-document"></i>
                商品详细描述
              </h3>
            </template>
            
            <div class="description-content">
              <div v-if="product.description || product.desc" class="description-text">
                <h4 class="description-title">商品介绍：</h4>
                <p class="description-paragraph">{{ product.description || product.desc }}</p>
              </div>
              <div v-else class="no-description">
                <i class="el-icon-document" style="font-size: 24px; color: #c0c4cc;"></i>
                <p>暂无商品描述</p>
              </div>
              
              <div v-if="product.specifications" class="specifications-section">
                <h4 class="description-title">商品规格：</h4>
                <div class="specifications-list">
                  <div 
                    v-for="(spec, index) in product.specifications" 
                    :key="index"
                    class="spec-item"
                  >
                    <span class="spec-label">{{ spec.name }}：</span>
                    <span class="spec-value">{{ spec.value }}</span>
                  </div>
                </div>
              </div>
              
              <div v-if="product.notes" class="notes-section">
                <h4 class="description-title">注意事项：</h4>
                <p class="notes-text">{{ product.notes }}</p>
              </div>
            </div>
          </el-card>
          
          <!-- 审核历史卡片 -->
          <el-card shadow="never" class="detail-card" v-if="reviewHistory.length > 0">
            <template #header>
              <h3 class="section-title">
                <i class="el-icon-time"></i>
                审核历史记录
              </h3>
            </template>
            
            <div class="review-history">
              <el-timeline>
                <el-timeline-item
                  v-for="(record, index) in reviewHistory"
                  :key="index"
                  :timestamp="formatFullTime(record.time)"
                  placement="top"
                >
                  <el-card shadow="hover" class="history-card">
                    <div class="history-header">
                      <div class="history-action">
                        <el-tag 
                          :type="getStatusTagType(record.status)"
                          size="small"
                        >
                          {{ getStatusText(record.status) }}
                        </el-tag>
                        <span class="history-reviewer">{{ record.reviewer }}</span>
                      </div>
                      <div class="history-time">{{ formatTime(record.time) }}</div>
                    </div>
                    
                    <div v-if="record.reason" class="history-content">
                      <p class="history-reason">{{ record.reason }}</p>
                    </div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-card>
        </el-col>
        
        <!-- 右侧：商品图片和发布者信息 -->
        <el-col :span="8">
          <!-- 商品图片卡片 -->
          <el-card shadow="never" class="image-card">
            <template #header>
              <h3 class="section-title">
                <i class="el-icon-picture"></i>
                商品图片展示
              </h3>
            </template>
            
            <div class="image-content">
              <!-- 主图 -->
              <div class="main-image-section">
                <el-image
                  :src="getProductImage(product)"
                  fit="cover"
                  class="main-image"
                  :preview-src-list="previewImages"
                >
                  <template #error>
                    <div class="image-error">
                      <i class="el-icon-picture"></i>
                      <span>图片加载失败</span>
                    </div>
                  </template>
                  
                  <div v-if="product.isNew" class="image-badge new-badge">NEW</div>
                  <div v-if="product.isHot" class="image-badge hot-badge">HOT</div>
                </el-image>
                
                <div class="image-actions">
                  <el-button 
                    type="text" 
                    @click="handleZoomImage"
                    class="zoom-btn"
                  >
                    <i class="el-icon-view"></i>
                    放大查看
                  </el-button>
                  
                  <el-button 
                    type="text" 
                    @click="handleViewGallery"
                    v-if="product.images && product.images.length > 1"
                    class="gallery-btn"
                  >
                    <i class="el-icon-picture"></i>
                    查看相册
                  </el-button>
                </div>
              </div>
              
              <!-- 缩略图列表 -->
              <div v-if="product.images && product.images.length > 1" class="thumbnail-list">
                <div 
                  v-for="(img, index) in product.images" 
                  :key="index"
                  class="thumbnail-item"
                  :class="{ active: currentImageIndex === index }"
                  @click="changeImage(index)"
                >
                  <el-image
                    :src="getImagePath(img)"
                    fit="cover"
                    class="thumbnail-image"
                  >
                    <template #error>
                      <div class="thumbnail-error">
                        <i class="el-icon-picture"></i>
                      </div>
                    </template>
                  </el-image>
                </div>
              </div>
              
              <!-- 图片信息 -->
              <div class="image-info">
                <div class="info-item">
                  <i class="el-icon-picture"></i>
                  <span>{{ product.images ? product.images.length : 1 }} 张图片</span>
                </div>
                <div class="info-item">
                  <i class="el-icon-s-data"></i>
                  <span>支持预览放大</span>
                </div>
              </div>
            </div>
          </el-card>
          
          <!-- 发布者信息卡片 -->
          <el-card shadow="never" class="merchant-card">
            <template #header>
              <h3 class="section-title">
                <i class="el-icon-user"></i>
                发布者信息
              </h3>
            </template>
            
            <div class="merchant-content">
              <!-- 商家基本信息 -->
              <div class="merchant-info" @click="viewMerchantDetail">
                <div class="merchant-avatar">
                  <el-avatar :size="60" :src="getMerchantAvatar(product)">
                    {{ getMerchantInitial(product) }}
                  </el-avatar>
                </div>
                <div class="merchant-details">
                  <h4 class="merchant-name">{{ getMerchantName(product) }}</h4>
                  <p class="merchant-id">ID: {{ product.merchantId || '未知' }}</p>
                  
                  <div class="merchant-rating" v-if="product.merchantRating">
                    <el-rate
                      v-model="product.merchantRating"
                      disabled
                      :max="5"
                      class="rating-stars"
                    />
                    <span class="rating-score">{{ product.merchantRating.toFixed(1) }}</span>
                  </div>
                  
                  <div class="merchant-extra">
                    <div class="extra-item" v-if="product.merchantLevel">
                      <i class="el-icon-s-custom"></i>
                      <span>{{ product.merchantLevel }}</span>
                    </div>
                    <div class="extra-item" v-if="product.merchantLocation">
                      <i class="el-icon-location"></i>
                      <span>{{ product.merchantLocation }}</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 联系方式 -->
              <div class="contact-info" v-if="showContactInfo">
                <h4 class="contact-title">联系方式</h4>
                <div class="contact-list">
                  <div class="contact-item" v-if="product.merchantPhone">
                    <i class="el-icon-phone"></i>
                    <span>{{ product.merchantPhone }}</span>
                    <el-button 
                      type="text" 
                      size="mini"
                      @click.stop="handleCallPhone(product.merchantPhone)"
                      class="call-btn"
                    >
                      拨打
                    </el-button>
                  </div>
                  <div class="contact-item" v-if="product.merchantEmail">
                    <i class="el-icon-message"></i>
                    <span>{{ product.merchantEmail }}</span>
                    <el-button 
                      type="text" 
                      size="mini"
                      @click.stop="handleSendEmail(product.merchantEmail)"
                      class="email-btn"
                    >
                      发送邮件
                    </el-button>
                  </div>
                </div>
              </div>
              
              <!-- 商家统计 -->
              <div class="merchant-stats">
                <div class="stats-grid">
                  <div class="stat-item">
                    <div class="stat-number">{{ merchantStats.products || 0 }}</div>
                    <div class="stat-label">上架商品</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-number">{{ merchantStats.approved || 0 }}</div>
                    <div class="stat-label">已通过</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-number">{{ merchantStats.rejected || 0 }}</div>
                    <div class="stat-label">被拒绝</div>
                  </div>
                </div>
              </div>
              
              <!-- 查看商家按钮 -->
              <div class="merchant-actions">
                <el-button 
                  type="primary" 
                  @click="viewMerchantDetail"
                  class="view-merchant-btn"
                >
                  <i class="el-icon-user"></i>
                  查看商家详情
                </el-button>
              </div>
            </div>
          </el-card>
          
          <!-- 审核建议卡片 -->
          <el-card shadow="never" class="suggestion-card">
            <template #header>
              <h3 class="section-title">
                <i class="el-icon-edit"></i>
                审核建议
              </h3>
            </template>
            
            <div class="suggestion-content">
              <div class="suggestion-form">
                <el-input
                  v-model="suggestionText"
                  type="textarea"
                  :rows="4"
                  placeholder="您可以在这里记录审核过程中的注意事项或建议..."
                  maxlength="500"
                  show-word-limit
                  class="suggestion-input"
                />
                
                <div class="suggestion-actions">
                  <el-button 
                    type="primary" 
                    @click="saveSuggestion"
                    size="small"
                  >
                    保存建议
                  </el-button>
                  
                  <el-button 
                    @click="clearSuggestion"
                    size="small"
                  >
                    清空
                  </el-button>
                </div>
              </div>
              
              <div v-if="savedSuggestions.length > 0" class="suggestion-history">
                <h4 class="history-title">历史建议：</h4>
                <div class="suggestion-list">
                  <div 
                    v-for="(suggestion, index) in savedSuggestions" 
                    :key="index"
                    class="suggestion-item"
                  >
                    <div class="suggestion-text">{{ suggestion.text }}</div>
                    <div class="suggestion-time">{{ formatTime(suggestion.time) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 预览对话框 -->
    <el-dialog
      title="商品预览效果"
      :visible.sync="previewDialog.visible"
      width="800px"
      center
      class="preview-dialog"
    >
      <div v-if="product" class="preview-content">
        <div class="preview-header">
          <h2>{{ product.name }}</h2>
          <div class="preview-badges">
            <el-tag 
              :type="getCategoryTagType(product.category)"
              class="preview-category-badge"
            >
              {{ getCategoryText(product.category) }}
            </el-tag>
            <el-tag 
              v-if="product.isNew" 
              type="danger" 
              class="preview-new-badge"
            >
              新品
            </el-tag>
            <el-tag 
              v-if="product.isHot" 
              type="warning" 
              class="preview-hot-badge"
            >
              热销
            </el-tag>
          </div>
        </div>
        
        <div class="preview-body">
          <div class="preview-image">
            <el-image
              :src="getProductImage(product)"
              fit="cover"
              class="preview-main-image"
            />
          </div>
          
          <div class="preview-info">
            <div class="info-section">
              <h3>商品信息</h3>
              <p><strong>价格：</strong>¥{{ (product.price || 0).toFixed(2) }}</p>
              <p><strong>库存：</strong>{{ product.stock || 0 }} {{ product.unit || '件' }}</p>
              <p v-if="product.origin"><strong>产地：</strong>{{ product.origin }}</p>
              <p v-if="product.brand"><strong>品牌：</strong>{{ product.brand }}</p>
            </div>
            
            <div class="info-section">
              <h3>商品描述</h3>
              <p class="preview-description">{{ product.description || product.desc || '暂无描述' }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="previewDialog.visible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 图片相册对话框 -->
    <el-dialog
      title="商品相册"
      :visible.sync="galleryDialog.visible"
      width="90%"
      top="5vh"
      class="gallery-dialog"
    >
      <div v-if="product.images && product.images.length > 0" class="gallery-content">
        <el-carousel 
          :initial-index="currentImageIndex" 
          height="600px"
          arrow="always"
          class="gallery-carousel"
        >
          <el-carousel-item v-for="(img, index) in product.images" :key="index">
            <el-image
              :src="getImagePath(img)"
              fit="contain"
              class="gallery-image"
            >
              <template #error>
                <div class="gallery-error">
                  <i class="el-icon-picture"></i>
                  <span>图片加载失败</span>
                </div>
              </template>
            </el-image>
          </el-carousel-item>
        </el-carousel>
        
        <div class="gallery-info">
          <div class="image-counter">
            图片 {{ currentImageIndex + 1 }} / {{ product.images.length }}
          </div>
          <div class="image-name">{{ getImageName(product.images[currentImageIndex]) }}</div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="galleryDialog.visible = false">关闭</el-button>
          <el-button 
            type="primary" 
            @click="downloadCurrentImage"
            v-if="product.images && product.images.length > 0"
          >
            下载当前图片
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog
      title="填写拒绝原因"
      :visible.sync="rejectDialog.visible"
      width="500px"
      center
      class="reject-dialog"
    >
      <div class="reject-dialog-content">
        <p class="reject-hint">请详细填写拒绝原因，帮助商家了解问题并进行改进：</p>
        
        <el-form :model="rejectDialog.form" :rules="rejectDialog.rules" ref="rejectFormRef">
          <el-form-item prop="reason">
            <el-input
              v-model="rejectDialog.form.reason"
              type="textarea"
              :rows="4"
              placeholder="请输入详细的拒绝原因，例如：商品图片不清晰、价格不合理、描述不准确等..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="拒绝类型" prop="type">
            <el-select
              v-model="rejectDialog.form.type"
              placeholder="请选择拒绝类型"
              class="reject-type-select"
            >
              <el-option label="图片问题" value="image"></el-option>
              <el-option label="描述问题" value="description"></el-option>
              <el-option label="价格问题" value="price"></el-option>
              <el-option label="库存问题" value="stock"></el-option>
              <el-option label="资质问题" value="qualification"></el-option>
              <el-option label="其他问题" value="other"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="建议修改" v-if="rejectDialog.form.type">
            <el-input
              v-model="rejectDialog.form.suggestion"
              type="textarea"
              :rows="2"
              placeholder="请提供具体的修改建议..."
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rejectDialog.visible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmReject"
            :loading="rejectDialog.loading"
          >
            确认拒绝
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 退回修改对话框 -->
    <el-dialog
      title="退回修改"
      :visible.sync="returnDialog.visible"
      width="500px"
      center
    >
      <div class="return-dialog-content">
        <p class="return-hint">请详细说明需要修改的内容，帮助商家完善商品信息：</p>
        
        <el-form :model="returnDialog.form" :rules="returnDialog.rules" ref="returnFormRef">
          <el-form-item prop="reason">
            <el-input
              v-model="returnDialog.form.reason"
              type="textarea"
              :rows="4"
              placeholder="请输入需要修改的具体内容，例如：需要补充商品图片、修改价格描述、完善商品规格等..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="修改类型" prop="type">
            <el-select
              v-model="returnDialog.form.type"
              placeholder="请选择修改类型"
              class="return-type-select"
            >
              <el-option label="基本信息" value="basic"></el-option>
              <el-option label="图片信息" value="image"></el-option>
              <el-option label="描述信息" value="description"></el-option>
              <el-option label="价格信息" value="price"></el-option>
              <el-option label="规格信息" value="specification"></el-option>
              <el-option label="其他信息" value="other"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="修改要求" v-if="returnDialog.form.type">
            <el-input
              v-model="returnDialog.form.requirement"
              type="textarea"
              :rows="2"
              placeholder="请提供具体的修改要求..."
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="returnDialog.visible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmReturn"
            :loading="returnDialog.loading"
          >
            确认退回
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from '@vue/composition-api'
import { Message, MessageBox } from 'element-ui'
import { 
  approveProduct, 
  rejectProduct, 
  returnProduct,
  getProductById,
  getMerchantInfo,
  getProductReviewHistory
} from '@/data/productStore'

export default {
  name: 'ProductReview',
  setup(_, { root }) {
    const product = ref({})
    const loading = ref(false)
    const reviewing = ref(false)
    const currentImageIndex = ref(0)
    const suggestionText = ref('')
    const savedSuggestions = ref([])
    const showContactInfo = ref(false)
    const reviewHistory = ref([])
    const merchantStats = reactive({ products: 0, approved: 0, rejected: 0 })

    const previewDialog = reactive({ visible: false })
    const galleryDialog = reactive({ visible: false })
    const rejectDialog = reactive({
      visible: false,
      loading: false,
      form: { reason: '', type: '', suggestion: '' },
      rules: {
        reason: [
          { required: true, message: '请填写拒绝原因', trigger: 'blur' },
          { min: 5, max: 500, message: '长度在 5 到 500 个字符', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '请选择拒绝类型', trigger: 'change' }
        ]
      }
    })
    const returnDialog = reactive({
      visible: false,
      loading: false,
      form: { reason: '', type: '', requirement: '' },
      rules: {
        reason: [
          { required: true, message: '请填写退回原因', trigger: 'blur' },
          { min: 5, max: 500, message: '长度在 5 到 500 个字符', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '请选择修改类型', trigger: 'change' }
        ]
      }
    })

    const previewImages = computed(() => {
      if (product.value.images && product.value.images.length > 0) {
        return product.value.images.map(img => getImagePath(img))
      }
      return [getProductImage(product.value)]
    })

    const loadProduct = async () => {
      loading.value = true
      const productId = parseInt(root.$route.params.id)
      
      if (!productId) {
        Message.error('商品ID无效')
        root.$router.back()
        return
      }

      try {
        const productData = getProductById(productId)
        
        if (productData) {
          product.value = productData
          await loadMerchantInfo()
        } else {
          Message.error('商品不存在')
          root.$router.back()
        }
      } catch (error) {
        console.error('加载商品失败:', error)
        Message.error('加载商品失败')
        root.$router.back()
      } finally {
        loading.value = false
      }
    }

    const loadMerchantInfo = async () => {
      try {
        if (product.value.merchantId) {
          const merchant = getMerchantInfo(product.value.merchantId)
          if (merchant) {
            product.value.merchantName = merchant.name || merchant.username
            product.value.merchantAvatar = merchant.avatar
            product.value.merchantRating = merchant.rating
            product.value.merchantLevel = merchant.level
            product.value.merchantLocation = merchant.location
            product.value.merchantPhone = merchant.phone
            product.value.merchantEmail = merchant.email
            loadMerchantStats(merchant)
          }
        }
      } catch (error) {
        console.error('加载商家信息失败:', error)
      }
    }

    const loadMerchantStats = (merchant) => {
      merchantStats.products = merchant.productsCount || 0
      merchantStats.approved = merchant.approvedCount || 0
      merchantStats.rejected = merchant.rejectedCount || 0
    }

    const loadReviewHistory = async () => {
      try {
        const productId = parseInt(root.$route.params.id)
        if (productId) {
          reviewHistory.value = getProductReviewHistory(productId)
        }
      } catch (error) {
        console.error('加载审核历史失败:', error)
        reviewHistory.value = []
      }
    }

    const getProductImage = (prod) => {
      if (prod.image) return getImagePath(prod.image)
      if (prod.images && prod.images.length > 0) return getImagePath(prod.images[0])
      if (prod.category === 'processed') return '/imgs/foods/3.png'
      if (prod.category === 'specialty') return '/imgs/foods/2.png'
      return '/imgs/foods/1.png'
    }

    const getImagePath = (img) => {
      if (typeof img === 'string' && (img.startsWith('data:image') || img.startsWith('http'))) return img
      if (typeof img === 'string') return '/' + img
      return '/imgs/foods/1.png'
    }

    const getImageName = (img) => {
      if (typeof img === 'string') return img.includes('/') ? img.split('/').pop() : img
      return '商品图片'
    }

    const getMerchantAvatar = (prod) => prod.merchantAvatar || ''
    const getMerchantInitial = (prod) => getMerchantName(prod).charAt(0)
    const getMerchantName = (prod) => prod.merchantName || prod.user || '未知商家'

    const getCategoryText = (category) => {
      const map = { vegetable: '蔬菜类', fruit: '水果类', meat: '肉类', grain: '谷物类', processed: '加工类', other: '其他' }
      return map[category] || category
    }

    const getCategoryTagType = (category) => {
      const types = { vegetable: 'success', fruit: 'primary', meat: 'danger', grain: 'warning', processed: 'info', other: 'default' }
      return types[category] || 'info'
    }

    const getSourceText = (source) => {
      const map = { farmer: '农户自产', cooperative: '合作社', base: '基地直供' }
      return map[source] || source
    }

    const getSourceTagType = (source) => {
      const types = { farmer: 'info', cooperative: 'primary', base: 'success' }
      return types[source] || 'info'
    }

    const getStatusText = (status) => {
      const map = { pending: '待审核', approved: '已通过', rejected: '已拒绝', returned: '已退回' }
      return map[status] || status
    }

    const getStatusTagType = (status) => {
      const types = { pending: 'warning', approved: 'success', rejected: 'danger', returned: 'info' }
      return types[status] || 'info'
    }

    const getStockStatus = (stock) => {
      if (stock === undefined || stock === null) return { type: 'info', text: '未知' }
      if (stock === 0) return { type: 'danger', text: '缺货' }
      if (stock <= 10) return { type: 'warning', text: '紧张' }
      return { type: 'success', text: '充足' }
    }

    const getStockClass = (stock) => {
      if (stock === undefined || stock === null) return ''
      if (stock === 0) return 'stock-zero'
      if (stock <= 10) return 'stock-low'
      return 'stock-normal'
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

    const refreshPage = () => {
      loadProduct()
      loadReviewHistory()
      Message.success('页面已刷新')
    }

    const changeImage = (index) => { currentImageIndex.value = index }
    const handleZoomImage = () => { window.open(getProductImage(product.value), '_blank') }
    const handleViewGallery = () => { currentImageIndex.value = 0; galleryDialog.visible = true }

    const downloadCurrentImage = () => {
      const imgUrl = getProductImage(product.value)
      const link = document.createElement('a')
      link.href = imgUrl
      link.download = `商品图片_${product.value.name}_${currentImageIndex.value + 1}.jpg`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }

    const viewMerchantDetail = () => {
      if (product.value.merchantId) {
        root.$router.push(`/merchant/${product.value.merchantId}`)
      } else {
        Message.warning('商家ID不存在')
      }
    }

    const handleCallPhone = (phone) => {
      MessageBox.confirm(`确定要拨打 ${phone} 吗？`, '确认拨号', {
        confirmButtonText: '拨打',
        cancelButtonText: '取消',
        type: 'info',
        center: true
      }).then(() => { window.location.href = `tel:${phone}` }).catch(() => {})
    }

    const handleSendEmail = (email) => { window.location.href = `mailto:${email}` }
    const handlePreview = () => { previewDialog.visible = true }

    const handleCopyLink = () => {
      const url = `${window.location.origin}/product/${product.value.id}`
      if (navigator.clipboard) {
        navigator.clipboard.writeText(url)
          .then(() => Message.success('商品链接已复制到剪贴板'))
          .catch(() => fallbackCopyText(url))
      } else {
        fallbackCopyText(url)
      }
    }

    const fallbackCopyText = (text) => {
      const textArea = document.createElement("textarea")
      textArea.value = text
      textArea.style.position = "fixed"
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      try {
        const successful = document.execCommand('copy')
        Message.success(successful ? '商品链接已复制到剪贴板' : '复制失败，请手动复制链接')
      } catch (err) {
        ElMessage.error('复制失败，请手动复制链接')
      }
      document.body.removeChild(textArea)
    }

    const saveSuggestion = () => {
      if (!suggestionText.value.trim()) {
        ElMessage.warning('请输入审核建议')
        return
      }
      const suggestion = { text: suggestionText.value, time: new Date().toISOString(), productId: product.value.id }
      savedSuggestions.value.unshift(suggestion)
      localStorage.setItem(`product_${product.value.id}_suggestions`, JSON.stringify(savedSuggestions.value))
      ElMessage.success('审核建议已保存')
      suggestionText.value = ''
    }

    const clearSuggestion = () => { suggestionText.value = '' }

    const loadSavedSuggestions = () => {
      try {
        const saved = localStorage.getItem(`product_${product.value.id}_suggestions`)
        if (saved) savedSuggestions.value = JSON.parse(saved)
      } catch (error) {
        console.error('加载建议失败:', error)
      }
    }

    const handleApprove = () => {
      ElMessageBox.confirm(`确定要通过商品 "${product.value.name}" 的审核吗？`, '确认通过', {
        type: 'warning',
        confirmButtonText: '通过',
        cancelButtonText: '取消',
        center: true
      }).then(async () => { await performReview('approve') }).catch(() => {})
    }

    const handleReject = () => {
      rejectDialog.form = { reason: '', type: '', suggestion: '' }
      rejectDialog.visible = true
    }

    const confirmReject = async () => {
      const valid = await root.$refs.rejectFormRef.validate().catch(() => false)
      if (!valid) return
      rejectDialog.loading = true
      await performReview('reject')
      rejectDialog.loading = false
      rejectDialog.visible = false
    }

    const handleReturn = () => {
      returnDialog.form = { reason: '', type: '', requirement: '' }
      returnDialog.visible = true
    }

    const confirmReturn = async () => {
      const valid = await root.$refs.returnFormRef.validate().catch(() => false)
      if (!valid) return
      returnDialog.loading = true
      await performReview('return')
      returnDialog.loading = false
      returnDialog.visible = false
    }

    const performReview = async (action) => {
      reviewing.value = true
      try {
        const currentUser = JSON.parse(localStorage.getItem('xm-user') || '{}')
        let success = false
        let message = ''
        let reason = ''
        
        switch (action) {
          case 'approve':
            success = approveProduct(product.value.id, currentUser.name || currentUser.username || '管理员')
            message = '审核通过成功'
            break
          case 'reject':
            reason = rejectDialog.form.reason
            if (rejectDialog.form.type) reason = `[${rejectDialog.form.type}] ${reason}`
            if (rejectDialog.form.suggestion) reason += `\n\n修改建议：${rejectDialog.form.suggestion}`
            success = rejectProduct(product.value.id, reason, currentUser.name || currentUser.username || '管理员')
            message = '拒绝成功'
            break
          case 'return':
            reason = returnDialog.form.reason
            if (returnDialog.form.type) reason = `[${returnDialog.form.type}] ${reason}`
            if (returnDialog.form.requirement) reason += `\n\n修改要求：${returnDialog.form.requirement}`
            success = returnProduct(product.value.id, reason, currentUser.name || currentUser.username || '管理员')
            message = '退回修改成功'
            break
        }
        
        if (success) {
          ElMessage.success(message)
          await loadProduct()
          await loadReviewHistory()
          if (action !== 'return') {
            setTimeout(() => { root.$router.push('/pending-products') }, 1500)
          }
        } else {
          ElMessage.error('操作失败')
        }
      } catch (error) {
        console.error('审核操作失败:', error)
        ElMessage.error('操作失败')
      } finally {
        reviewing.value = false
      }
    }

    onMounted(() => { loadSavedSuggestions() })

    return {
      product, loading, reviewing, currentImageIndex, suggestionText, savedSuggestions, showContactInfo,
      reviewHistory, merchantStats, previewDialog, galleryDialog, rejectDialog, returnDialog,
      previewImages, loadProduct, loadReviewHistory, getProductImage, getImagePath, getImageName,
      getMerchantAvatar, getMerchantInitial, getMerchantName, getCategoryText, getCategoryTagType,
      getSourceText, getSourceTagType, getStatusText, getStatusTagType, getStockStatus, getStockClass,
      formatTime, formatFullTime, refreshPage, changeImage, handleZoomImage, handleViewGallery,
      downloadCurrentImage, viewMerchantDetail, handleCallPhone, handleSendEmail, handlePreview,
      handleCopyLink, saveSuggestion, clearSuggestion, handleApprove, handleReject, confirmReject,
      handleReturn, confirmReturn
    }
  }
}
</script>

<style scoped>
.product-review-page {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
}

/* 页面头部 */
.page-header {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  animation: slide-down 0.3s ease;
}

@keyframes slide-down {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-left {
  flex: 1;
  min-width: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title::before {
  content: '';
  display: block;
  width: 4px;
  height: 20px;
  background: linear-gradient(135deg, #e6a23c, #f2c641);
  border-radius: 2px;
}

.page-subtitle {
  font-size: 14px;
  color: #606266;
  margin: 0;
  font-weight: 500;
  opacity: 0.8;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.back-btn {
  border-radius: 8px;
  border-color: #e0e0e0;
  color: #666;
  font-weight: 500;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  background: white;
}

.back-btn:hover {
  border-color: #409EFF;
  color: #409EFF;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.refresh-btn {
  color: #909399;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  color: #409EFF;
  background: rgba(64, 158, 255, 0.1);
  transform: rotate(180deg);
}

/* 审核操作栏 */
.review-actions-bar {
  margin-bottom: 20px;
}

.actions-card {
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  background: linear-gradient(135deg, #fdf6ec, #faecd8);
  border-left: 4px solid #e6a23c;
  animation: fade-in 0.5s ease;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.actions-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.product-basic-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 12px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.status-tag,
.category-tag {
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 12px;
  border: none;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.submit-time {
  font-size: 13px;
  color: #666;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  background: white;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-end;
  flex-shrink: 0;
}

.approve-btn {
  background: #4caf50;
  border-color: #4caf50;
  color: white;
  padding: 10px 20px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 8px 0 0 8px;
}

.approve-btn:hover {
  background: #2e7d32;
  border-color: #2e7d32;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.return-btn {
  background: #2196f3;
  border-color: #2196f3;
  color: white;
  padding: 10px 20px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 0;
  border-left: none;
  border-right: none;
}

.return-btn:hover {
  background: #0d47a1;
  border-color: #0d47a1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(33, 150, 243, 0.3);
}

.reject-btn {
  background: #f44336;
  border-color: #f44336;
  color: white;
  padding: 10px 20px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  border-radius: 0 8px 8px 0;
}

.reject-btn:hover {
  background: #d32f2f;
  border-color: #d32f2f;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(244, 67, 54, 0.3);
}

.quick-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.preview-btn,
.copy-link-btn {
  font-size: 12px;
  font-weight: 500;
  color: #2196f3;
  padding: 4px 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.preview-btn:hover,
.copy-link-btn:hover {
  background: rgba(33, 150, 243, 0.1);
  transform: translateY(-1px);
}

/* 商品详情内容 */
.product-detail-content {
  margin-bottom: 20px;
}

.detail-card,
.image-card,
.merchant-card,
.suggestion-card {
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
  animation: card-slide 0.5s ease;
}

@keyframes card-slide {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.detail-card:hover,
.image-card:hover,
.merchant-card:hover,
.suggestion-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border-color: #e4e7ed;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-card:deep(.el-card__header) {
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  padding: 16px 20px;
  border-radius: 12px 12px 0 0;
}

/* 基础信息网格 */
.basic-info-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 0;
}

.info-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  flex: 1;
  min-width: 200px;
}

.info-item.full-width {
  min-width: 100%;
}

.info-label {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  min-width: 80px;
  flex-shrink: 0;
}

.info-value {
  font-size: 15px;
  color: #666;
  flex: 1;
  min-width: 0;
}

.price-value {
  font-size: 18px;
  font-weight: 700;
  color: #4caf50;
}

.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
}

.stock-value {
  font-size: 18px;
  font-weight: 600;
}

.stock-zero {
  color: #f44336;
}

.stock-low {
  color: #e6a23c;
}

.stock-normal {
  color: #4caf50;
}

.stock-status-tag {
  font-size: 10px;
  padding: 4px 8px;
  height: 20px;
  line-height: 12px;
  border-radius: 10px;
  border: none;
  font-weight: 600;
  margin-left: 8px;
}

.category-tag {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  border: none;
  font-weight: 600;
}

.quality-rate:deep(.el-rate__icon) {
  font-size: 16px;
  margin-right: 2px;
}

.quality-rate:deep(.el-rate__text) {
  font-size: 14px;
  margin-left: 8px;
}

/* 商品描述内容 */
.description-content {
  padding: 20px 0;
}

.description-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.description-paragraph {
  font-size: 15px;
  color: #333;
  line-height: 1.8;
  white-space: pre-line;
  margin: 0 0 20px 0;
}

.no-description {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
  color: #999;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.specifications-section,
.notes-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.specifications-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.spec-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.spec-label {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  min-width: 60px;
  flex-shrink: 0;
}

.spec-value {
  font-size: 13px;
  color: #666;
  flex: 1;
  min-width: 0;
}

.notes-text {
  font-size: 13px;
  color: #f44336;
  line-height: 1.6;
  padding: 12px;
  background: rgba(244, 67, 54, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(244, 67, 54, 0.2);
  margin: 0;
}

/* 审核历史 */
.review-history {
  padding: 20px 0;
}

.history-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: default;
}

.history-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateX(4px);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 8px;
}

.history-action {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.history-reviewer {
  font-size: 13px;
  color: #333;
  font-weight: 600;
}

.history-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.history-content {
  margin-top: 8px;
}

.history-reason {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  margin: 0;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e9ecef;
  white-space: pre-line;
}

/* 图片卡片 */
.image-content {
  padding: 20px 0;
}

.main-image-section {
  margin-bottom: 20px;
  text-align: center;
}

.main-image {
  width: 100%;
  max-width: 300px;
  height: 300px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
  margin: 0 auto 12px auto;
  position: relative;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-image:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 16px;
  gap: 8px;
}

.image-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 10px;
  font-weight: 700;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  line-height: 1;
  z-index: 1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.new-badge {
  background: linear-gradient(135deg, #f44336, #d32f2f);
}

.hot-badge {
  background: linear-gradient(135deg, #e6a23c, #f2c641);
}

.image-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 12px;
}

.zoom-btn,
.gallery-btn {
  font-size: 12px;
  font-weight: 500;
  color: #409EFF;
  padding: 6px 12px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.zoom-btn:hover,
.gallery-btn:hover {
  background: rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

.thumbnail-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
  margin-bottom: 20px;
  padding: 0 20px;
}

.thumbnail-item {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumbnail-item:hover {
  border-color: #409EFF;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.thumbnail-item.active {
  border-color: #4caf50;
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 20px;
}

.image-info {
  display: flex;
  justify-content: center;
  gap: 20px;
  font-size: 12px;
  color: #909399;
  background: #f8f9fa;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 商家卡片 */
.merchant-content {
  padding: 20px 0;
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.merchant-info:hover {
  transform: translateX(4px);
  background: rgba(64, 158, 255, 0.05);
  border-radius: 8px;
  padding: 20px 15px;
  margin: 0 -15px;
}

.merchant-avatar {
  flex-shrink: 0;
}

.merchant-details {
  flex: 1;
  min-width: 0;
}

.merchant-name {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 4px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.merchant-id {
  font-size: 12px;
  color: #666;
  margin: 0 0 8px 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.merchant-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.rating-stars:deep(.el-rate__icon) {
  font-size: 12px;
  margin-right: 1px;
}

.rating-score {
  font-size: 12px;
  color: #e6a23c;
  font-weight: 600;
}

.merchant-extra {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.extra-item {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #e9ecef;
}

.contact-info {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.contact-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.contact-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.contact-item:hover {
  background: #f0f9ff;
  border-color: #409EFF;
  transform: translateX(4px);
}

.contact-item i {
  font-size: 16px;
  color: #409EFF;
  flex-shrink: 0;
}

.contact-item span {
  font-size: 13px;
  color: #333;
  flex: 1;
  min-width: 0;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.call-btn,
.email-btn {
  font-size: 12px;
  font-weight: 500;
  color: #409EFF;
  padding: 2px 8px;
  border-radius: 4px;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.call-btn:hover,
.email-btn:hover {
  background: rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

.merchant-stats {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
  cursor: default;
}

.stat-item:hover {
  background: #f0f9ff;
  border-color: #409EFF;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.stat-number {
  font-size: 20px;
  font-weight: 700;
  color: #409EFF;
  margin-bottom: 4px;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.merchant-actions {
  padding-top: 20px;
  text-align: center;
}

.view-merchant-btn {
  width: 100%;
  border-radius: 8px;
  background: linear-gradient(135deg, #409EFF, #66B1FF);
  border: none;
  color: white;
  font-weight: 600;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.view-merchant-btn:hover {
  background: linear-gradient(135deg, #66B1FF, #409EFF);
}
</style>