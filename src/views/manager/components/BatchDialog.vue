/**
 * 批量操作弹窗组件
 * 文件路径: src/views/manager/components/BatchDialog.vue
 * 功能描述: 管理后台商品批量操作统一弹窗，支持操作类型：批量上下架（含状态切换确认）、批量删除（含警告提示）、
 *           批量库存调整（设为/增加/减少，输入数值验证）、批量价格调整（调价方式/比例/金额）、
 *           批量改分类（选择目标分类），显示已选中商品数量，二次确认防误操作
 * 关联文件:
 * - src/api/index.js: 提供批量操作接口
 * - src/views/manager/Products.vue: 商品管理页面（调用批量弹窗）
 */
<template>
  <el-dialog
    :model-value="visible"
    :title="dialogConfig.title"
    width="500px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
  >
    <!-- 上下架/删除确认 -->
    <div v-if="['shelf', 'delete'].includes(type)">
      <p style="margin: 0 0 20px 0; line-height: 1.5; color: #606266;">{{ dialogConfig.message }}</p>
      <el-alert v-if="type === 'shelf'" title="提示" type="warning" :closable="false" show-icon>
        将选中的 {{ selectedCount }} 个商品{{ dialogConfig.form && dialogConfig.form.status === 'approved' ? '上架' : '下架' }}
      </el-alert>
      <el-alert v-else title="警告" type="error" :closable="false" show-icon>
        删除的商品将无法恢复，请谨慎操作！
      </el-alert>
    </div>
    
    <!-- 表单 -->
    <el-form v-else ref="formRef" :model="form" :rules="formRules" label-width="100px">
      <!-- 批量库存调整 -->
      <div v-if="type === 'update_stock'">
        <p style="margin: 0 0 20px 0; line-height: 1.5; color: #606266;">将选中的 {{ selectedCount }} 个商品进行库存调整</p>
        <el-form-item label="调整方式" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="set">设为</el-radio>
            <el-radio label="add">增加</el-radio>
            <el-radio label="reduce">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整数量" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :max="999999" :controls="false" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </div>
      
      <!-- 批量价格调整 -->
      <div v-else-if="type === 'update_price'">
        <p style="margin: 0 0 20px 0; line-height: 1.5; color: #606266;">将选中的 {{ selectedCount }} 个商品进行价格调整</p>
        <el-form-item label="调整方式" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="set">设为</el-radio>
            <el-radio label="add">增加</el-radio>
            <el-radio label="reduce">减少</el-radio>
            <el-radio label="percent">调整百分比</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整数值" prop="amount">
          <el-input v-model="form.amount" style="width: 200px;">
            <template #append>{{ form.type === 'percent' ? '%' : '元' }}</template>
          </el-input>
        </el-form-item>
      </div>
      
      <!-- 批量修改分类 -->
      <div v-else-if="type === 'update_category'">
        <p style="margin: 0 0 20px 0; line-height: 1.5; color: #606266;">将选中的 {{ selectedCount }} 个商品修改分类</p>
        <el-form-item label="商品分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择商品分类" clearable style="width: 100%;">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
      </div>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="$emit('update:visible', false)">取消</el-button>
        <el-button :type="dialogConfig.buttonType" :loading="loading" @click="handleConfirm">
          {{ loading ? '处理中...' : dialogConfig.buttonText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: 'BatchDialog',
  props: {
    visible: { type: Boolean, default: false },
    type: { type: String, default: '' },
    selectedCount: { type: Number, default: 0 },
    loading: { type: Boolean, default: false }
  },
  emits: ['update:visible', 'confirm'],
  data() {
    return {
      form: {},
      formRules: {
        type: [{ required: true, message: '请选择调整方式', trigger: 'change' }],
        amount: [{ required: true, message: '请输入调整数值', trigger: 'blur' }],
        reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }],
        category: [{ required: true, message: '请选择商品分类', trigger: 'change' }]
      },
      categoryOptions: [
        { label: '蔬菜水果', value: 'vegetable_fruit' },
        { label: '肉禽蛋奶', value: 'meat_egg_milk' },
        { label: '粮油调味', value: 'grain_oil' },
        { label: '海鲜水产', value: 'seafood' },
        { label: '干货特产', value: 'dry_goods' },
        { label: '有机食品', value: 'organic' },
        { label: '速食方便', value: 'fast_food' },
        { label: '饮品酒水', value: 'beverage' }
      ]
    }
  },
  computed: {
    dialogConfig() {
      const configs = {
        shelf: { title: this.form?.status === 'approved' ? '批量上架' : '批量下架', message: `确定要将选中的 ${this.selectedCount} 个商品${this.form?.status === 'approved' ? '上架' : '下架'}吗？`, buttonType: this.form?.status === 'approved' ? 'success' : 'warning', buttonText: this.form?.status === 'approved' ? '确定上架' : '确定下架' },
        delete: { title: '批量删除', message: `确定要删除选中的 ${this.selectedCount} 个商品吗？删除后无法恢复！`, buttonType: 'danger', buttonText: '确认删除' },
        update_stock: { title: '批量修改库存', message: '', buttonType: 'primary', buttonText: '确认修改' },
        update_price: { title: '批量调整价格', message: '', buttonType: 'primary', buttonText: '确认调整' },
        update_category: { title: '批量修改分类', message: '', buttonType: 'primary', buttonText: '确认修改' }
      }
      return configs[this.type] || { title: '操作', buttonType: 'primary', buttonText: '确定' }
    }
  },
  watch: {
    visible(val) {
      if (val) {
        if (this.type === 'shelf') this.form = { status: 'approved' }
        else if (this.type === 'update_stock') this.form = { type: 'set', amount: 0, reason: '' }
        else if (this.type === 'update_price') this.form = { type: 'set', amount: '' }
        else if (this.type === 'update_category') this.form = { category: '' }
      }
    }
  },
  methods: {
    async handleConfirm() {
      if (!['shelf', 'delete'].includes(this.type)) {
        try {
          await this.$refs.formRef.validate()
        } catch { return }
      }
      this.$emit('confirm', { type: this.type, form: this.form })
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
