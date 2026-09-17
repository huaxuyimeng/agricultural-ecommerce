/**
 * 商品数据分析弹窗组件
 * 文件路径: src/views/manager/components/AnalysisDialog.vue
 * 功能描述: 单个商品数据分析展示弹窗，核心指标卡片（浏览量、销量、收藏数、评分、转化率），
 *           扩展统计（评论数、复购率、库存周转等），各指标趋势对比（环比/同比箭头），
 *           支持导出分析报告（CSV）
 * 关联文件:
 * - src/api/index.js: 提供商品统计数据接口
 * - src/views/manager/Products.vue: 商品管理页面（调用分析弹窗）
 */
<template>
  <el-dialog
    :visible="dialogVisible"
    title="商品数据分析"
    width="900px"
    :close-on-click-modal="false"
    class="analysis-dialog"
    @close="handleClose"
  >
    <div v-if="data" class="analysis-content">
      <!-- 核心数据统计 -->
      <div class="stats-grid">
        <div v-for="stat in data.stats" :key="stat.label" class="stat-card" :style="{ background: statBackground(stat.type) }">
          <div class="stat-icon"><i :class="stat.icon"></i></div>
          <div class="stat-info">
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-trend"><i :class="stat.trend.icon"></i> {{ stat.trend.value }}</div>
          </div>
        </div>
      </div>

      <!-- 扩展统计 -->
      <div v-if="data.extraStats" class="extra-stats-grid">
        <div v-for="item in data.extraStats" :key="item.label" class="extra-stat-card">
          <div class="extra-stat-icon"><i :class="item.icon"></i></div>
          <div class="extra-stat-info">
            <div class="extra-stat-label">{{ item.label }}</div>
            <div class="extra-stat-value">{{ item.value }}</div>
          </div>
        </div>
      </div>

      <div class="charts-row">
        <!-- 分类统计 -->
        <div class="chart-section chart-half">
          <h4><i class="el-icon-folder"></i> 商品分类统计</h4>
          <div v-if="data.categoryData.length === 0" class="empty">暂无分类数据</div>
          <div v-else class="category-list">
            <div v-for="item in data.categoryData" :key="item.name" class="category-item">
              <div class="category-info">
                <span class="category-name"><i class="el-icon-collection-tag"></i> {{ categoryLabel(item.name) }}</span>
                <span class="category-count">{{ item.value }} 个</span>
              </div>
              <el-progress :percentage="Math.round((item.value / totalCount) * 100)" :stroke-width="8" :show-text="false" />
            </div>
          </div>
        </div>

        <!-- 商家统计 -->
        <div class="chart-section chart-half">
          <h4><i class="el-icon-s-shop"></i> 商家商品分布</h4>
          <div v-if="!data.merchantData || data.merchantData.length === 0" class="empty">暂无商家数据</div>
          <div v-else class="merchant-list">
            <div v-for="(item, index) in data.merchantData" :key="item.name" class="merchant-item">
              <div class="merchant-rank">{{ index + 1 }}</div>
              <div class="merchant-info">
                <span class="merchant-name">{{ item.name }}</span>
                <span class="merchant-count">{{ item.value }} 个商品</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 状态统计 -->
      <div class="chart-section">
        <h4><i class="el-icon-s-opportunity"></i> 商品状态统计</h4>
        <div v-if="data.statusData.length === 0" class="empty">暂无状态数据</div>
        <div v-else class="status-list">
          <div v-for="item in data.statusData" :key="item.name" class="status-item">
            <el-tag :type="statusType(item.name)" size="small">{{ statusText(item.name) }}</el-tag>
            <span class="status-count">{{ item.value }} 个</span>
            <el-progress
              :percentage="Math.round((item.value / totalCount) * 100)"
              :stroke-width="6"
              :show-text="false"
              :color="statusColor(item.name)"
            />
          </div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" icon="el-icon-download" @click="$emit('export')">导出报表</el-button>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: 'AnalysisDialog',
  props: {
    visible: { type: Boolean, default: false },
    data: { type: Object, default: null },
    totalCount: { type: Number, default: 0 }
  },
  data() {
    return {
      dialogVisible: false
    }
  },
  watch: {
    visible(val) {
      this.dialogVisible = val
    }
  },
  methods: {
    handleClose() {
      this.dialogVisible = false
      this.$emit('update:visible', false)
    },
    statBackground(type) {
      const bgs = {
        total: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        onSale: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        lowStock: 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)',
        sales: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
      }
      return bgs[type] || bgs.total
    },
    categoryLabel(name) {
      const map = {
        vegetable: '蔬菜',
        fruit: '水果',
        meat: '肉类',
        grain: '粮食',
        processed: '加工产品',
        other: '其他'
      }
      return map[name] || name
    },
    statusText(status) {
      const map = {
        pending: '待审核',
        approved: '在售',
        rejected: '已拒绝',
        sold_out: '缺货',
        off_shelf: '下架'
      }
      return map[status] || status
    },
    statusType(status) {
      const map = {
        pending: 'warning',
        approved: 'success',
        rejected: 'danger',
        sold_out: 'danger',
        off_shelf: 'info'
      }
      return map[status] || ''
    },
    statusColor(status) {
      const map = {
        pending: '#e6a23c',
        approved: '#67c23a',
        rejected: '#f56c6c',
        sold_out: '#f56c6c',
        off_shelf: '#909399'
      }
      return map[status] || '#409eff'
    }
  }
}
</script>

<style scoped>
.analysis-content { padding: 10px 0; }

.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card { border-radius: 12px; padding: 20px; color: white; display: flex; align-items: center; gap: 16px; transition: transform 0.2s; }
.stat-card:hover { transform: translateY(-2px); }
.stat-icon { font-size: 36px; opacity: 0.9; }
.stat-label { font-size: 13px; opacity: 0.9; margin-bottom: 4px; }
.stat-value { font-size: 24px; font-weight: 700; }
.stat-trend { font-size: 12px; opacity: 0.8; margin-top: 4px; }

.extra-stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 24px; }
.extra-stat-card { background: #f5f7fa; border-radius: 8px; padding: 14px 16px; display: flex; align-items: center; gap: 12px; }
.extra-stat-icon { font-size: 20px; color: #409eff; width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; background: #ecf5ff; border-radius: 8px; }
.extra-stat-label { font-size: 12px; color: #909399; }
.extra-stat-value { font-size: 18px; font-weight: 600; color: #303133; }

.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }
.chart-section { margin-bottom: 20px; }
.chart-half { margin-bottom: 0; }
.chart-section h4 { font-size: 14px; color: #606266; margin: 0 0 12px 0; display: flex; align-items: center; gap: 6px; }
.empty { text-align: center; padding: 30px; color: #909399; }

.category-list { display: flex; flex-direction: column; gap: 10px; }
.category-item { display: flex; flex-direction: column; gap: 6px; }
.category-info { display: flex; justify-content: space-between; align-items: center; }
.category-name { color: #303133; font-size: 13px; }
.category-count { color: #909399; font-size: 13px; }

.merchant-list { display: flex; flex-direction: column; gap: 8px; }
.merchant-item { display: flex; align-items: center; gap: 12px; padding: 8px 12px; background: #f5f7fa; border-radius: 6px; }
.merchant-rank { width: 24px; height: 24px; display: flex; align-items: center; justify-content: center; background: #409eff; color: white; border-radius: 50%; font-size: 12px; font-weight: 600; }
.merchant-item:nth-child(1) .merchant-rank { background: #f56c6c; }
.merchant-item:nth-child(2) .merchant-rank { background: #e6a23c; }
.merchant-item:nth-child(3) .merchant-rank { background: #67c23a; }
.merchant-info { flex: 1; display: flex; justify-content: space-between; align-items: center; }
.merchant-name { color: #303133; font-size: 13px; }
.merchant-count { color: #909399; font-size: 12px; }

.status-list { display: flex; flex-direction: column; gap: 10px; }
.status-item { display: flex; align-items: center; gap: 12px; }
.status-count { color: #909399; font-size: 13px; min-width: 50px; }
.status-item .el-progress { flex: 1; }
</style>
