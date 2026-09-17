/**
 * 显示设置抽屉组件
 * 文件路径: src/views/manager/components/ViewSettingsDrawer.vue
 * 功能描述: 商品管理表格显示设置抽屉，自定义表格列显示/隐藏（选择列、商品图片、商品名称、价格库存、
 *           商品状态、商家信息、数据统计、更新时间），排序设置（更新时间/价格/库存/销量），
 *           实时预览效果，应用设置后更新父组件表格显示
 * 关联文件:
 * - src/views/manager/Products.vue: 商品管理页面（父组件）
 * - src/views/manager/components/ProductTable.vue: 商品表格组件
 */
<template>
  <el-drawer
    :model-value="visible"
    title="显示设置"
    direction="rtl"
    size="300px"
    @update:model-value="$emit('update:visible', $event)"
  >
    <div class="settings-content">
      <h4 class="settings-title"><i class="el-icon-s-data"></i> 表格显示</h4>
      <div class="settings-section">
        <el-checkbox-group v-model="localSettings.visibleColumns">
          <el-checkbox label="selection">选择列</el-checkbox>
          <el-checkbox label="image">商品图片</el-checkbox>
          <el-checkbox label="name">商品名称</el-checkbox>
          <el-checkbox label="price">价格库存</el-checkbox>
          <el-checkbox label="status">商品状态</el-checkbox>
          <el-checkbox label="merchant">商家信息</el-checkbox>
          <el-checkbox label="statistics">数据统计</el-checkbox>
          <el-checkbox label="updateTime">更新时间</el-checkbox>
        </el-checkbox-group>
      </div>

      <h4 class="settings-title"><i class="el-icon-sort"></i> 排序设置</h4>
      <div class="settings-section">
        <el-radio-group v-model="localSettings.sortBy">
          <el-radio label="updateTime">更新时间</el-radio>
          <el-radio label="price">商品价格</el-radio>
          <el-radio label="stock">商品库存</el-radio>
          <el-radio label="sales">商品销量</el-radio>
          <el-radio label="name">商品名称</el-radio>
        </el-radio-group>
        <el-radio-group v-model="localSettings.sortOrder" style="margin-top: 10px;">
          <el-radio label="desc">降序</el-radio>
          <el-radio label="asc">升序</el-radio>
        </el-radio-group>
      </div>

      <h4 class="settings-title"><i class="el-icon-s-opportunity"></i> 库存预警</h4>
      <div class="settings-section">
        <el-switch v-model="localSettings.showStockAlert" active-text="显示库存预警" inactive-text="隐藏库存预警" @change="handleChange" />
        <div style="margin-top: 20px;">
          <span style="display: block; margin-bottom: 10px; font-size: 13px; color: #606266;">
            低库存预警阈值：<el-tag type="warning" size="small">{{ localSettings.stockWarningThreshold }}</el-tag>
          </span>
          <el-slider v-model="localSettings.stockWarningThreshold" :min="1" :max="50" :step="1" show-input @change="handleChange">
            <template #marks>
              <span v-if="localSettings.stockWarningThreshold <= 5" class="mark">紧急</span>
              <span v-if="localSettings.stockWarningThreshold <= 20" class="mark">警告</span>
            </template>
          </el-slider>
        </div>
      </div>

      <div class="settings-actions">
        <el-button type="default" style="width: 100%;" @click="handleReset">
          <i class="el-icon-refresh"></i> 恢复默认设置
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script>
export default {
  name: 'ViewSettingsDrawer',
  props: {
    visible: { type: Boolean, default: false },
    settings: { type: Object, required: true }
  },
  emits: ['update:visible', 'change', 'reset'],
  data() {
    return {
      localSettings: {}
    }
  },
  watch: {
    settings: {
      handler(val) { this.localSettings = { ...val } },
      immediate: true
    }
  },
  methods: {
    handleChange() { this.$emit('change', { ...this.localSettings }) },
    handleReset() {
      this.localSettings = { pageSize: 10, visibleColumns: ['selection', 'image', 'name', 'price', 'status', 'merchant', 'statistics', 'updateTime'], sortBy: 'updateTime', sortOrder: 'desc', showStockAlert: true, stockWarningThreshold: 10 }
      this.$emit('reset')
    }
  }
}
</script>

<style scoped>
.settings-content { padding: 0 16px; }
.settings-title { font-size: 14px; color: #409eff; margin: 20px 0 12px; }
.settings-section { padding: 12px; background: #f5f7fa; border-radius: 4px; }
.settings-actions { margin-top: 30px; }
</style>
