/**
 * 库存调整弹窗组件
 * 文件路径: src/views/manager/components/StockDialog.vue
 * 功能描述: 单个商品库存调整弹窗，显示当前库存值（只读），选择调整方式（设为/增加/减少），
 *           输入调整数量，实时计算调整后库存预览，填写调整原因，表单验证后提交
 * 关联文件:
 * - src/api/index.js: 提供库存调整接口
 * - src/views/manager/Products.vue: 商品管理页面（调用库存弹窗）
 */
<template>
  <el-dialog
    :visible="visible"
    :title="`库存调整 - ${productName}`"
    width="500px"
    :close-on-click-modal="false"
    @update:visible="$emit('update:visible', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="当前库存">
        <el-input :value="currentStock" disabled style="width: 200px;" />
      </el-form-item>
      
      <el-form-item label="调整方式" prop="type">
        <el-radio-group v-model="form.type">
          <el-radio label="set">设为</el-radio>
          <el-radio label="add">增加</el-radio>
          <el-radio label="reduce">减少</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="调整数量" prop="amount">
        <el-input-number v-model="form.amount" :min="0" :max="999999" :controls="false" placeholder="请输入调整数量" style="width: 200px;" />
      </el-form-item>
      
      <el-form-item v-if="form.type !== 'set'" label="调整后库存">
        <el-input :value="newStock" disabled style="width: 200px;" />
      </el-form-item>
      
      <el-form-item label="调整原因" prop="reason">
        <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入库存调整原因" maxlength="200" show-word-limit />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ saving ? '保存中...' : '确认调整' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: 'StockDialog',
  props: {
    visible: { type: Boolean, default: false },
    productId: { type: [String, Number], default: '' },
    productName: { type: String, default: '' },
    currentStock: { type: Number, default: 0 },
    saving: { type: Boolean, default: false }
  },
  emits: ['update:visible', 'save'],
  data() {
    return {
      form: { type: 'set', amount: 0, reason: '' },
      rules: {
        type: [{ required: true, message: '请选择调整方式', trigger: 'change' }],
        amount: [{ required: true, message: '请输入调整数量', trigger: 'blur' }],
        reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
      }
    }
  },
  computed: {
    newStock() {
      if (this.form.type === 'set') return this.form.amount
      if (this.form.type === 'add') return this.currentStock + this.form.amount
      if (this.form.type === 'reduce') return Math.max(0, this.currentStock - this.form.amount)
      return this.currentStock
    }
  },
  watch: {
    visible(val) {
      if (val) this.form = { type: 'set', amount: this.currentStock, reason: '' }
    }
  },
  methods: {
    async handleSave() {
      try {
        await this.$refs.formRef.validate()
        this.$emit('save', { productId: this.productId, newStock: this.newStock, reason: this.form.reason })
      } catch {}
    }
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
