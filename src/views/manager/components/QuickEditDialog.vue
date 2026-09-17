/**
 * 快速编辑弹窗组件
 * 文件路径: src/views/manager/components/QuickEditDialog.vue
 * 功能描述: 商品快速编辑弹窗，分基本信息区（名称、分类）和价格库存区（单价、库存、单位），
 *           表单验证（名称必填、价格/库存正数），编辑后提交更新单个商品信息
 * 关联文件:
 * - src/api/index.js: 提供商品更新接口
 * - src/views/manager/Products.vue: 商品管理页面（调用编辑弹窗）
 */
<template>
  <el-dialog
    :visible="visible"
    title="快速编辑商品"
    width="600px"
    :close-on-click-modal="false"
    class="edit-dialog"
    @update:visible="$emit('update:visible', $event)"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="edit-form"
    >
      <div class="form-section">
        <h4 class="form-section-title">
          <i class="el-icon-edit"></i> 基本信息
        </h4>
        
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" clearable />
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
        <el-button @click="$emit('update:visible', false)">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ saving ? '保存中...' : '保存修改' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: 'QuickEditDialog',
  props: {
    visible: { type: Boolean, default: false },
    product: { type: Object, default: null },
    saving: { type: Boolean, default: false }
  },
  emits: ['update:visible', 'save'],
  data() {
    return {
      form: { id: '', name: '', category: '', price: 0, stock: 0, unit: '件', status: 'approved', isRecommend: false },
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
    product: {
      handler(val) {
        if (val) this.form = { ...val }
      },
      immediate: true
    }
  },
  methods: {
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
</style>
