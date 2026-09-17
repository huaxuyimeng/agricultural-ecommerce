/**
 * 发布商品页面（商家端）
 * 文件路径: src/views/front/merchant/PublishProduct.vue
 * 功能描述: 商家发布新商品或编辑已有商品，支持基本信息（名称、分类、描述）、价格库存（单价、库存、单位）、
 *           商品图片上传（多图上传/预览/删除、支持拖拽排序）、详情描述（富文本编辑器），
 *           编辑模式回显现有商品数据，审核流程提示说明，提交后进入审核队列，
 *           保存草稿和立即提交，表单验证，步骤引导式填写
 * 关联文件:
 * - src/api/index.js: 提供商品创建/更新、文件上传接口
 * - src/views/front/merchant/MyProducts.vue: 我的商品列表页面
 */
<template>
  <div class="publish-page">
    <div class="page-container">
      <div class="page-header">
        <div class="header-left">
          <el-button @click="$router.back()" class="back-btn" circle>
            <i class="el-icon-arrow-left"></i>
          </el-button>
          <div class="header-info">
            <h1 class="page-title">{{ isEditMode ? '编辑商品' : '发布新商品' }}</h1>
            <p class="page-subtitle">{{ pageSubtitle }}</p>
          </div>
        </div>
        <div class="header-right">
          <div class="status-badge" :class="currentStatusClass">
            <i :class="currentStatusIcon"></i>
            <span>{{ currentStatusText }}</span>
          </div>
        </div>
      </div>

      <div class="main-content">
        <div class="form-column">
          <div class="review-tip" v-if="!isEditMode && showTip">
            <div class="tip-icon">
              <i class="el-icon-info"></i>
            </div>
            <div class="tip-content">
              <h4>审核流程说明</h4>
              <p>提交后商品将进入审核队列，管理员将在 <strong>1-3 个工作日</strong>内完成审核。审核通过后商品将自动上架销售。</p>
            </div>
            <el-button type="text" @click="showTip = false" class="tip-close">
              <i class="el-icon-close"></i>
            </el-button>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" class="product-form">
            <div class="form-section">
              <div class="section-header">
                <div class="section-icon basic-icon">
                  <i class="el-icon-document"></i>
                </div>
                <div class="section-info">
                  <h3 class="section-title">基本信息</h3>
                  <p class="section-desc">填写商品的核心信息</p>
                </div>
              </div>
              
              <div class="section-body">
                <el-form-item label="商品名称" prop="name">
                  <el-input 
                    v-model="form.name" 
                    placeholder="例如：有机黄瓜、生态土鸡蛋" 
                    maxlength="50"
                    show-word-limit
                  />
                </el-form-item>

                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-form-item label="商品分类" prop="category">
                      <el-select v-model="form.category" placeholder="选择分类" style="width: 100%">
                        <el-option label="蔬菜类" value="vegetable" />
                        <el-option label="水果类" value="fruit" />
                        <el-option label="肉类" value="meat" />
                        <el-option label="谷物类" value="grain" />
                        <el-option label="加工类" value="processed" />
                        <el-option label="其他" value="other" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="品牌">
                      <el-input v-model="form.brand" placeholder="可选" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="16">
                  <el-col :span="8">
                    <el-form-item label="售价" prop="price">
                      <el-input-number 
                        v-model="form.price" 
                        :min="0.01" 
                        :precision="2" 
                        controls-position="right"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="原价">
                      <el-input-number 
                        v-model="form.originalPrice" 
                        :min="0.01" 
                        :precision="2" 
                        controls-position="right"
                        style="width: 100%"
                        placeholder="可选"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="单位" prop="unit">
                      <el-select v-model="form.unit" style="width: 100%">
                        <el-option label="斤" value="斤" />
                        <el-option label="公斤" value="公斤" />
                        <el-option label="个" value="个" />
                        <el-option label="份" value="份" />
                        <el-option label="袋" value="袋" />
                        <el-option label="箱" value="箱" />
                        <el-option label="盒" value="盒" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="16">
                  <el-col :span="8">
                    <el-form-item label="库存" prop="stock">
                      <el-input-number 
                        v-model="form.stock" 
                        :min="0" 
                        controls-position="right"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="重量/规格">
                      <el-input v-model="form.weight" placeholder="例如：500g" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="产地">
                      <el-input v-model="form.origin" placeholder="例如：北京大兴" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </div>

            <div class="form-section">
              <div class="section-header">
                <div class="section-icon tag-icon">
                  <i class="el-icon-star-off"></i>
                </div>
                <div class="section-info">
                  <h3 class="section-title">商品标签</h3>
                  <p class="section-desc">设置商品的特殊属性</p>
                </div>
              </div>
              
              <div class="section-body">
                <div class="tag-group">
                  <div class="tag-item" :class="{ active: form.isHot }" @click="form.isHot = !form.isHot">
                    <div class="tag-icon-wrapper hot-tag">
                      <i class="el-icon-fire"></i>
                    </div>
                    <span class="tag-label">热销</span>
                  </div>
                  <div class="tag-item" :class="{ active: form.isNew }" @click="form.isNew = !form.isNew">
                    <div class="tag-icon-wrapper new-tag">
                      <i class="el-icon-bell"></i>
                    </div>
                    <span class="tag-label">新品</span>
                  </div>
                  <div class="tag-item" :class="{ active: form.isRecommend }" @click="form.isRecommend = !form.isRecommend">
                    <div class="tag-icon-wrapper recommend-tag">
                      <i class="el-icon-thumb"></i>
                    </div>
                    <span class="tag-label">推荐</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="form-section">
              <div class="section-header">
                <div class="section-icon image-icon">
                  <i class="el-icon-picture"></i>
                </div>
                <div class="section-info">
                  <h3 class="section-title">商品图片</h3>
                  <p class="section-desc">上传清晰的商品实拍图</p>
                </div>
              </div>
              
              <div class="section-body">
                <div class="image-upload-area">
                  <div v-if="form.image" class="image-preview-wrapper">
                    <img :src="form.image" class="preview-image" />
                    <div class="image-overlay">
                      <el-button @click="removeImage" type="danger" size="small" circle>
                        <i class="el-icon-delete"></i>
                      </el-button>
                    </div>
                  </div>
                  <el-upload
                    v-else
                    class="upload-trigger"
                    action="#"
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="handleImageChange"
                    accept="image/*"
                  >
                    <div class="upload-placeholder">
                      <i class="el-icon-plus"></i>
                      <span>点击上传</span>
                    </div>
                  </el-upload>
                </div>
                <p class="upload-tip">支持 JPG/PNG 格式，建议尺寸 800×600，不超过 2MB</p>
              </div>
            </div>

            <div class="form-section">
              <div class="section-header">
                <div class="section-icon desc-icon">
                  <i class="el-icon-edit"></i>
                </div>
                <div class="section-info">
                  <h3 class="section-title">商品描述</h3>
                  <p class="section-desc">详细介绍商品特色、品质等</p>
                </div>
              </div>
              
              <div class="section-body">
                <el-form-item prop="description">
                  <el-input 
                    type="textarea" 
                    v-model="form.description" 
                    :rows="6"
                    placeholder="描述商品的特色、产地、品质、保存方法等..."
                    maxlength="1000"
                    show-word-limit
                  />
                </el-form-item>
              </div>
            </div>
          </el-form>
        </div>

        <div class="preview-column">
          <div class="preview-card">
            <div class="preview-header">
              <i class="el-icon-view"></i>
              <span>实时预览</span>
            </div>
            <div class="preview-body">
              <div class="preview-image-section">
                <div v-if="form.image" class="preview-img-wrapper">
                  <img :src="form.image" />
                </div>
                <div v-else class="preview-img-placeholder">
                  <i class="el-icon-picture-outline"></i>
                  <span>商品图片</span>
                </div>
              </div>
              <div class="preview-info">
                <h4 class="preview-name">{{ form.name || '商品名称' }}</h4>
                <div class="preview-tags" v-if="hasActiveTags">
                  <span class="preview-tag hot" v-if="form.isHot">热销</span>
                  <span class="preview-tag new" v-if="form.isNew">新品</span>
                  <span class="preview-tag recommend" v-if="form.isRecommend">推荐</span>
                </div>
                <div class="preview-price">
                  <span class="price-current">¥{{ form.price || '0.00' }}</span>
                  <span class="price-original" v-if="form.originalPrice">¥{{ form.originalPrice }}</span>
                </div>
                <div class="preview-meta">
                  <span class="meta-item" v-if="form.origin">
                    <i class="el-icon-location"></i>
                    {{ form.origin }}
                  </span>
                  <span class="meta-item" v-if="form.stock !== null">
                    <i class="el-icon-box"></i>
                    库存 {{ form.stock }}{{ form.unit }}
                  </span>
                </div>
                <p class="preview-desc">{{ form.description || '商品描述...' }}</p>
              </div>
            </div>
          </div>

          <div class="review-status-card" v-if="isEditMode && currentProduct">
            <div class="status-card-header">
              <i class="el-icon-time"></i>
              <span>审核状态</span>
            </div>
            <div class="status-card-body">
              <div class="status-timeline">
                <div class="timeline-item completed">
                  <div class="timeline-dot"></div>
                  <div class="timeline-content">
                    <span class="timeline-title">提交商品</span>
                    <span class="timeline-time">{{ formatTime(currentProduct.submitTime) }}</span>
                  </div>
                </div>
                <div class="timeline-item" :class="{ active: currentProduct.status === 'pending' }">
                  <div class="timeline-dot"></div>
                  <div class="timeline-content">
                    <span class="timeline-title">审核中</span>
                    <span class="timeline-time" v-if="currentProduct.status === 'pending'">等待管理员审核</span>
                    <span class="timeline-time" v-else-if="currentProduct.status === 'approved'">已通过</span>
                    <span class="timeline-time" v-else-if="currentProduct.status === 'rejected'">未通过</span>
                  </div>
                </div>
                <div class="timeline-item" :class="{ completed: currentProduct.status === 'approved' }">
                  <div class="timeline-dot"></div>
                  <div class="timeline-content">
                    <span class="timeline-title">上架销售</span>
                    <span class="timeline-time" v-if="currentProduct.status === 'approved'">审核通过，已上架</span>
                    <span class="timeline-time" v-else>审核通过后自动上架</span>
                  </div>
                </div>
              </div>
              <div class="status-note" v-if="currentProduct.status === 'rejected'">
                <i class="el-icon-warning"></i>
                <span>商品未通过审核，请修改后重新提交</span>
              </div>
            </div>
          </div>

          <div class="action-card">
            <el-button 
              type="primary" 
              @click="submit"
              :loading="submitting"
              class="submit-btn"
              size="large"
            >
              <i class="el-icon-upload2"></i>
              {{ submitButtonText }}
            </el-button>
            <el-button @click="reset" class="reset-btn" size="large">
              <i class="el-icon-refresh"></i>
              重置
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getProductById, createProduct, updateProduct } from '@/api'

export default {
  name: 'PublishProduct',
  data() {
    const validatePrice = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入价格'))
      } else if (value <= 0) {
        callback(new Error('价格必须大于0'))
      } else {
        callback()
      }
    }
    
    return {
      form: {
        name: '',
        category: '',
        price: null,
        originalPrice: null,
        stock: null,
        unit: '斤',
        weight: '',
        origin: '',
        brand: '',
        isHot: false,
        isNew: false,
        isRecommend: false,
        image: '',
        description: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入商品名称', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ],
        category: [
          { required: true, message: '请选择商品分类', trigger: 'change' }
        ],
        price: [
          { required: true, validator: validatePrice, trigger: 'blur' }
        ],
        stock: [
          { required: true, message: '请输入库存', trigger: 'blur' }
        ],
        description: [
          { required: true, message: '请输入商品描述', trigger: 'change' },
          { min: 10, max: 1000, message: '描述长度在 10 到 1000 个字符', trigger: 'change' }
        ]
      },
      submitting: false,
      isEditMode: false,
      productId: null,
      currentProduct: null,
      showTip: true
    }
  },
  computed: {
    pageSubtitle() {
      return this.isEditMode 
        ? '修改商品信息，重新提交审核' 
        : '填写商品信息，提交给管理员审核'
    },
    submitButtonText() {
      return this.isEditMode ? '重新提交审核' : '提交审核'
    },
    currentStatusText() {
      if (!this.currentProduct) return '草稿'
      const statusMap = {
        pending: '待审核',
        approved: '已通过',
        rejected: '被拒绝'
      }
      return statusMap[this.currentProduct.status] || '草稿'
    },
    currentStatusIcon() {
      if (!this.currentProduct) return 'el-icon-edit'
      const iconMap = {
        pending: 'el-icon-time',
        approved: 'el-icon-check',
        rejected: 'el-icon-close'
      }
      return iconMap[this.currentProduct.status] || 'el-icon-edit'
    },
    currentStatusClass() {
      if (!this.currentProduct) return 'status-draft'
      return `status-${this.currentProduct.status}`
    },
    hasActiveTags() {
      return this.form.isHot || this.form.isNew || this.form.isRecommend
    }
  },
  created() {
    this.checkEditMode()
  },
  methods: {
    checkEditMode() {
      const id = this.$route.query.id
      if (id) {
        this.isEditMode = true
        this.productId = parseInt(id)
        this.loadProduct()
      }
    },
    
    async loadProduct() {
      try {
        const res = await getProductById(this.productId)
        const product = res.data
        if (product) {
          this.currentProduct = { ...product }
          this.form = {
            name: product.name || '',
            category: product.category || '',
            price: product.price,
            originalPrice: product.originalPrice,
            stock: product.stock,
            unit: product.unit || '斤',
            weight: product.weight || '',
            origin: product.origin || '',
            brand: product.brand || '',
            isHot: product.isHot || false,
            isNew: product.isNew || false,
            isRecommend: product.isRecommend || false,
            image: product.image || '',
            description: product.description || ''
          }
        } else {
          this.$message.warning('商品不存在')
          this.$router.push('/front/merchant/products')
        }
      } catch {
        this.$message.error('加载商品信息失败')
      }
    },
    
    removeImage() {
      this.form.image = ''
    },
    
    handleImageChange(file) {
      const isImage = file.raw.type.startsWith('image/')
      if (!isImage) {
        this.$message.error('只能上传图片文件')
        return
      }
      
      const isLt5M = file.raw.size / 1024 / 1024 < 5
      if (!isLt5M) {
        this.$message.error('图片大小不能超过 5MB')
        return
      }
      
      this.compressImage(file.raw, (compressedBase64) => {
        this.form.image = compressedBase64
      })
    },
    
    compressImage(file, callback) {
      const reader = new FileReader()
      reader.onload = (e) => {
        const img = new Image()
        img.onload = () => {
          const canvas = document.createElement('canvas')
          let width = img.width
          let height = img.height
          const maxSize = 800
          
          if (width > maxSize || height > maxSize) {
            if (width > height) {
              height = Math.round((height * maxSize) / width)
              width = maxSize
            } else {
              width = Math.round((width * maxSize) / height)
              height = maxSize
            }
          }
          
          canvas.width = width
          canvas.height = height
          const ctx = canvas.getContext('2d')
          ctx.drawImage(img, 0, 0, width, height)
          
          const compressedBase64 = canvas.toDataURL('image/jpeg', 0.7)
          callback(compressedBase64)
        }
        img.src = e.target.result
      }
      reader.readAsDataURL(file)
    },
    
    submit() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return
        
        this.submitting = true
        try {
          const productData = {
            name: this.form.name,
            category: this.form.category,
            price: this.form.price,
            originalPrice: this.form.originalPrice,
            stock: this.form.stock,
            unit: this.form.unit,
            weight: this.form.weight,
            origin: this.form.origin,
            brand: this.form.brand,
            isHot: this.form.isHot,
            isNew: this.form.isNew,
            isRecommend: this.form.isRecommend,
            description: this.form.description,
            image: this.form.image,
            images: this.form.image ? [this.form.image] : []
          }
          
          if (this.isEditMode) {
            await updateProduct(this.productId, productData)
            this.$message({
              message: '商品已更新，已重新提交审核',
              type: 'success',
              duration: 3000
            })
          } else {
            await createProduct(productData)
            this.$message({
              message: '商品已提交审核，请等待管理员审核',
              type: 'success',
              duration: 3000
            })
          }
          
          setTimeout(() => {
            this.$router.push('/front/merchant/products')
          }, 1500)
        } catch (error) {
          console.error('提交失败:', error)
          const errorMsg = error.response?.data?.msg || error.message || '未知错误'
          this.$message.error(`${this.isEditMode ? '更新商品失败' : '发布商品失败'}: ${errorMsg}`)
        } finally {
          this.submitting = false
        }
      })
    },
    
    reset() {
      if (this.isEditMode && this.currentProduct) {
        this.form = {
          name: this.currentProduct.name || '',
          category: this.currentProduct.category || '',
          price: this.currentProduct.price,
          originalPrice: this.currentProduct.originalPrice,
          stock: this.currentProduct.stock,
          unit: this.currentProduct.unit || '斤',
          weight: this.currentProduct.weight || '',
          origin: this.currentProduct.origin || '',
          brand: this.currentProduct.brand || '',
          isHot: this.currentProduct.isHot || false,
          isNew: this.currentProduct.isNew || false,
          isRecommend: this.currentProduct.isRecommend || false,
          image: this.currentProduct.image || '',
          description: this.currentProduct.description || ''
        }
      } else {
        this.form = {
          name: '',
          category: '',
          price: null,
          originalPrice: null,
          stock: null,
          unit: '斤',
          weight: '',
          origin: '',
          brand: '',
          isHot: false,
          isNew: false,
          isRecommend: false,
          image: '',
          description: ''
        }
      }
      this.$nextTick(() => {
        this.$refs.formRef.clearValidate()
      })
    },

    formatTime(time) {
      if (!time) return '未知时间'
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
.publish-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  padding: 24px;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  background: white;
  border-radius: 16px;
  padding: 24px 32px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  width: 40px;
  height: 40px;
  border: 1px solid #e0e0e0;
  transition: all 0.2s;
}

.back-btn:hover {
  border-color: #52c41a;
  color: #52c41a;
  transform: translateX(-2px);
}

.header-info {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 13px;
  color: #8c8c8c;
  margin: 0;
}

.review-tip {
  background: linear-gradient(135deg, #f6ffed 0%, #e6f7ff 100%);
  border: 1px solid #b7eb8f;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tip-icon {
  width: 36px;
  height: 36px;
  background: #52c41a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  flex-shrink: 0;
}

.tip-content {
  flex: 1;
}

.tip-content h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.tip-content p {
  font-size: 13px;
  color: #595959;
  margin: 0;
  line-height: 1.5;
}

.tip-content strong {
  color: #52c41a;
  font-weight: 600;
}

.tip-close {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px;
  color: #8c8c8c;
  font-size: 16px;
}

.tip-close:hover {
  color: #595959;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.status-draft {
  background: #f5f5f5;
  color: #8c8c8c;
}

.status-pending {
  background: #fff7e6;
  color: #faad14;
}

.status-approved {
  background: #f6ffed;
  color: #52c41a;
}

.status-rejected {
  background: #fff1f0;
  color: #ff4d4f;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
}

.form-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-form {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.form-section {
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 1px solid #f0f0f0;
}

.form-section:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.section-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
}

.basic-icon { background: linear-gradient(135deg, #52c41a, #73d13d); }
.tag-icon { background: linear-gradient(135deg, #faad14, #ffc53d); }
.image-icon { background: linear-gradient(135deg, #1890ff, #40a9ff); }
.desc-icon { background: linear-gradient(135deg, #722ed1, #9254de); }

.section-info {
  flex: 1;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.section-desc {
  font-size: 12px;
  color: #8c8c8c;
  margin: 0;
}

.section-body {
  padding-left: 52px;
}

.product-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #595959;
  margin-bottom: 8px;
}

.product-form :deep(.el-input__inner),
.product-form :deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  transition: all 0.2s;
}

.product-form :deep(.el-input__inner:focus),
.product-form :deep(.el-textarea__inner:focus) {
  border-color: #52c41a;
  box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.1);
}

.tag-group {
  display: flex;
  gap: 16px;
}

.tag-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-radius: 12px;
  border: 2px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s;
  background: #fafafa;
}

.tag-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.tag-item.active {
  border-color: #52c41a;
  background: #f6ffed;
}

.tag-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  transition: all 0.2s;
}

.hot-tag { background: linear-gradient(135deg, #ff7a45, #ff9c6e); }
.new-tag { background: linear-gradient(135deg, #1890ff, #40a9ff); }
.recommend-tag { background: linear-gradient(135deg, #722ed1, #9254de); }

.tag-item.active .tag-icon-wrapper {
  transform: scale(1.1);
}

.tag-label {
  font-size: 14px;
  font-weight: 600;
  color: #595959;
}

.tag-item.active .tag-label {
  color: #52c41a;
}

.image-upload-area {
  display: flex;
  gap: 16px;
}

.upload-trigger {
  width: 150px;
  height: 150px;
}

.upload-trigger :deep(.el-upload) {
  width: 100%;
  height: 100%;
  border: 2px dashed #e0e0e0;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-trigger :deep(.el-upload:hover) {
  border-color: #52c41a;
  background: #f6ffed;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #8c8c8c;
}

.upload-placeholder i {
  font-size: 32px;
}

.upload-placeholder span {
  font-size: 13px;
}

.image-preview-wrapper {
  width: 150px;
  height: 150px;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  border: 2px solid #f0f0f0;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-preview-wrapper:hover .image-overlay {
  opacity: 1;
}

.upload-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin: 12px 0 0 0;
}

.preview-column {
  position: sticky;
  top: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.preview-header {
  padding: 16px 20px;
  background: linear-gradient(135deg, #52c41a, #73d13d);
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
}

.preview-body {
  padding: 20px;
}

.preview-image-section {
  width: 100%;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
  background: #f5f5f5;
}

.preview-img-wrapper {
  width: 100%;
  height: 100%;
}

.preview-img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #bfbfbf;
}

.preview-img-placeholder i {
  font-size: 40px;
}

.preview-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  line-height: 1.4;
  min-height: 44px;
}

.preview-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.preview-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.preview-tag.hot {
  background: #fff2e8;
  color: #ff7a45;
}

.preview-tag.new {
  background: #e6f7ff;
  color: #1890ff;
}

.preview-tag.recommend {
  background: #f9f0ff;
  color: #722ed1;
}

.preview-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;
}

.price-current {
  font-size: 24px;
  font-weight: 700;
  color: #ff4d4f;
}

.price-original {
  font-size: 14px;
  color: #bfbfbf;
  text-decoration: line-through;
}

.preview-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #8c8c8c;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.preview-desc {
  font-size: 13px;
  color: #8c8c8c;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.action-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #52c41a, #73d13d);
  border: none;
  transition: all 0.2s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(82, 196, 26, 0.3);
}

.reset-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  border: 1px solid #e0e0e0;
  color: #595959;
  transition: all 0.2s;
}

.reset-btn:hover {
  border-color: #52c41a;
  color: #52c41a;
}

.review-status-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.status-card-header {
  padding: 16px 20px;
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
}

.status-card-body {
  padding: 20px;
}

.status-timeline {
  position: relative;
  padding-left: 24px;
}

.status-timeline::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: #e8e8e8;
}

.timeline-item {
  position: relative;
  padding-bottom: 24px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-dot {
  position: absolute;
  left: -24px;
  top: 4px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #e8e8e8;
  border: 2px solid white;
  box-shadow: 0 0 0 2px #e8e8e8;
  z-index: 1;
}

.timeline-item.completed .timeline-dot {
  background: #52c41a;
  box-shadow: 0 0 0 2px #52c41a;
}

.timeline-item.active .timeline-dot {
  background: #faad14;
  box-shadow: 0 0 0 2px #faad14;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 2px #faad14;
  }
  50% {
    box-shadow: 0 0 0 6px rgba(250, 173, 20, 0.2);
  }
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.timeline-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

.timeline-time {
  font-size: 12px;
  color: #8c8c8c;
}

.timeline-item.completed .timeline-title {
  color: #52c41a;
}

.timeline-item.active .timeline-title {
  color: #faad14;
}

.status-note {
  margin-top: 16px;
  padding: 12px;
  background: #fff1f0;
  border: 1px solid #ffccc7;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #ff4d4f;
}

.status-note i {
  font-size: 16px;
}

@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }
  
  .preview-column {
    position: static;
  }
}

@media (max-width: 768px) {
  .publish-page {
    padding: 16px;
  }
  
  .page-header {
    padding: 20px;
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .product-form {
    padding: 20px;
  }
  
  .section-body {
    padding-left: 0;
  }
  
  .tag-group {
    flex-direction: column;
  }
}
</style>
