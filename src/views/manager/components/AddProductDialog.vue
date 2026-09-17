/**
 * 添加商品弹窗组件
 * 文件路径: src/views/manager/components/AddProductDialog.vue
 * 功能描述: 管理后台添加商品弹窗，表单分区域填写：基本信息（名称、描述、分类），价格库存（单价、库存、单位），
 *           图片上传（多图上传预览），商品状态（审核后直接上架/待审核可选），表单验证后提交
 * 关联文件:
 * - src/api/index.js: 提供商品创建接口
 * - src/views/manager/Products.vue: 商品管理页面（调用添加弹窗）
 */
<template>
  <el-dialog
    :visible="dialogVisible"
    title="添加商品"
    width="650px"
    :close-on-click-modal="false"
    class="add-dialog"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="add-form"
    >
      <div class="form-section">
        <h4 class="form-section-title">
          <i class="el-icon-edit"></i> 基本信息
        </h4>
        
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" clearable />
        </el-form-item>
        
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述" />
        </el-form-item>
        
        <el-form-item label="商品分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择商品分类" clearable style="width: 100%;">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="计量单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入计量单位，如：件、千克、个等" />
        </el-form-item>
      </div>
      
      <div class="form-section">
        <h4 class="form-section-title">
          <i class="el-icon-price-tag"></i> 价格与库存
        </h4>
        
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :max="999999" :precision="2" :step="0.1" :controls="false" placeholder="请输入价格" style="width: 100%;" />
        </el-form-item>
        
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :max="999999" :controls="false" placeholder="请输入库存数量" style="width: 100%;" />
        </el-form-item>
      </div>
      
      <div class="form-section">
        <h4 class="form-section-title">
          <i class="el-icon-picture"></i> 商品图片
        </h4>
        
        <el-form-item label="商品图片">
          <div class="image-upload-area">
            <div class="image-preview" v-if="form.image">
              <img :src="form.image" class="preview-img" />
              <div class="image-mask">
                <i class="el-icon-delete" @click="form.image = ''"></i>
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
          <i class="el-icon-setting"></i> 状态设置
        </h4>
        
        <el-form-item label="商品状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择商品状态" style="width: 100%;">
            <el-option label="上架" value="approved" />
            <el-option label="下架" value="off" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="推荐商品" prop="isRecommend">
          <el-switch v-model="form.isRecommend" :active-value="true" :inactive-value="false" active-text="是" inactive-text="否" />
        </el-form-item>
      </div>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ saving ? '添加中...' : '确认添加' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: 'AddProductDialog',
  props: {
    visible: { type: Boolean, default: false },
    saving: { type: Boolean, default: false }
  },
  data() {
    return {
      dialogVisible: false,
      form: { name: '', description: '', category: '', price: 0, stock: 0, unit: '件', image: '', status: 'approved', isRecommend: false },
      rules: {
        name: [
          { required: true, message: '请输入商品名称', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ],
        category: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
        price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
        stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
      },
      categoryOptions: [
        { label: '蔬菜', value: 'vegetable' },
        { label: '水果', value: 'fruit' },
        { label: '肉类', value: 'meat' },
        { label: '粮食', value: 'grain' },
        { label: '加工产品', value: 'processed' },
        { label: '其他', value: 'other' }
      ]
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (val) {
        this.form = { name: '', description: '', category: '', price: 0, stock: 0, unit: '件', image: '', status: 'approved', isRecommend: false }
        this.$nextTick(() => {
          if (this.$refs.formRef) this.$refs.formRef.clearValidate()
        })
      }
    }
  },
  methods: {
    handleClose() {
      this.dialogVisible = false
      this.$emit('update:visible', false)
    },
    handleImageChange(file) {
      const reader = new FileReader()
      reader.onload = (e) => {
        this.form.image = e.target.result
      }
      reader.readAsDataURL(file.raw)
    },
    async handleSave() {
      try {
        await this.$refs.formRef.validate()
        this.$emit('save', { ...this.form })
      } catch {}
    }
  }
}
</script>

<style scoped>
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
  background: rgba(0, 0, 0, 0.5);
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
  font-size: 24px;
  color: white;
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
  color: #c0c4cc;
  margin-bottom: 4px;
}

.image-uploader span {
  font-size: 12px;
  color: #909399;
}
</style>
