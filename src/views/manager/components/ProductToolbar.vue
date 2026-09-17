/**
 * 商品搜索工具栏组件
 * 文件路径: src/views/manager/components/ProductToolbar.vue
 * 功能描述: 商品管理搜索和筛选工具栏，主搜索区（关键词搜索+搜索/重置按钮），快速筛选区（分类下拉、状态筛选、审核状态筛选），
 *           排序选择（价格/库存/销量/更新时间升降序），支持展开更多筛选选项
 * 关联文件:
 * - src/api/index.js: 提供商品分类数据接口
 * - src/views/manager/Products.vue: 商品管理页面（父组件）
 */
<template>
  <div class="product-toolbar">
    <!-- 主搜索栏 -->
    <div class="toolbar-main">
      <div class="toolbar-left">
        <!-- 搜索框 -->
        <div class="search-box">
          <el-input
            v-model="search.keyword"
            placeholder="搜索商品名称、描述、商家..."
            clearable
            class="search-input"
            @keyup.enter="$emit('search')"
            @clear="$emit('search')"
          >
            <template #prefix>
              <i class="el-icon-search"></i>
            </template>
          </el-input>
          <el-button type="primary" icon="el-icon-search" @click="$emit('search')">搜索</el-button>
          <el-button type="default" icon="el-icon-refresh" @click="$emit('reset')">重置</el-button>
        </div>
      </div>

      <div class="toolbar-right">
        <!-- 快速筛选 -->
        <el-select
          v-model="search.category"
          placeholder="全部分类"
          clearable
          class="filter-select"
          @change="$emit('filter-change')"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        
        <el-select
          v-model="search.status"
          placeholder="全部状态"
          clearable
          class="filter-select"
          @change="$emit('filter-change')"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        
        <el-date-picker
          v-model="search.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="date-picker"
          value-format="yyyy-MM-dd"
          @change="$emit('filter-change')"
        />
        
        <el-button
          type="text"
          :class="['filter-toggle', { active: showAdvancedFilter }]"
          @click="$emit('toggle-advanced')"
        >
          <i :class="showAdvancedFilter ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
          {{ showAdvancedFilter ? '收起' : '高级筛选' }}
        </el-button>
      </div>
    </div>

    <!-- 高级筛选 -->
    <transition name="filter-slide">
      <div v-if="showAdvancedFilter" class="advanced-filter">
        <div class="advanced-form">
          <div class="form-row">
            <div class="form-item">
              <label class="form-label">价格范围</label>
              <div class="price-range">
                <el-input
                  v-model="advancedFilter.minPrice"
                  placeholder="最低价"
                  class="price-input"
                  @input="$emit('price-filter')"
                />
                <span class="range-separator">-</span>
                <el-input
                  v-model="advancedFilter.maxPrice"
                  placeholder="最高价"
                  class="price-input"
                  @input="$emit('price-filter')"
                />
              </div>
            </div>
            
            <div class="form-item">
              <label class="form-label">库存状态</label>
              <el-select
                v-model="advancedFilter.stock"
                placeholder="全部库存"
                clearable
                class="filter-select"
                @change="$emit('filter-change')"
              >
                <el-option
                  v-for="item in stockOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </div>
            
            <div class="form-item">
              <label class="form-label">商家名称</label>
              <el-input
                v-model="advancedFilter.merchant"
                placeholder="输入商家名称"
                clearable
                class="merchant-input"
                @keyup.enter="$emit('search')"
                @clear="$emit('search')"
              />
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'ProductToolbar',
  props: {
    search: { type: Object, required: true },
    advancedFilter: { type: Object, required: true },
    showAdvancedFilter: { type: Boolean, default: false }
  },
  emits: ['search', 'reset', 'filter-change', 'price-filter', 'toggle-advanced'],
  data() {
    return {
      categoryOptions: [
        { label: '蔬菜水果', value: 'vegetable_fruit' },
        { label: '肉禽蛋奶', value: 'meat_egg_milk' },
        { label: '粮油调味', value: 'grain_oil' },
        { label: '海鲜水产', value: 'seafood' },
        { label: '干货特产', value: 'dry_goods' },
        { label: '有机食品', value: 'organic' },
        { label: '速食方便', value: 'fast_food' },
        { label: '饮品酒水', value: 'beverage' }
      ],
      statusOptions: [
        { label: '已上架', value: 'approved' },
        { label: '已下架', value: 'off' },
        { label: '已售罄', value: 'sold_out' }
      ],
      stockOptions: [
        { label: '充足', value: 'normal' },
        { label: '紧张', value: 'low' },
        { label: '缺货', value: 'zero' },
        { label: '低库存', value: 'low_stock' }
      ]
    }
  }
}
</script>

<style scoped>
/* ========== 工具栏容器 ========== */
.product-toolbar {
  padding: 16px 24px;
  border-bottom: 1px solid #ebeef5;
  background: #fafbfc;
}

.toolbar-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

/* ========== 搜索框 ========== */
.search-box {
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-input {
  width: 320px;
}

.search-input :deep(.el-input__inner) {
  border-radius: 6px;
}

/* ========== 筛选器 ========== */
.filter-select {
  width: 130px;
}

.date-picker {
  width: 240px;
}

.filter-toggle {
  color: #606266;
  font-size: 14px;
  padding: 0 4px;
  transition: color 0.2s;
}

.filter-toggle:hover {
  color: #409eff;
}

.filter-toggle.active {
  color: #409eff;
}

/* ========== 高级筛选 ========== */
.advanced-filter {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #dcdfe6;
}

.advanced-form {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.form-row {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 200px;
}

.form-label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-input {
  width: 110px;
}

.range-separator {
  color: #909399;
  font-size: 14px;
}

.merchant-input {
  width: 180px;
}

/* ========== 动画 ========== */
.filter-slide-enter-active,
.filter-slide-leave-active {
  transition: all 0.3s ease;
}

.filter-slide-enter,
.filter-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ========== 响应式 ========== */
@media (max-width: 1200px) {
  .toolbar-main {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-left,
  .toolbar-right {
    justify-content: flex-start;
  }

  .search-input {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .product-toolbar {
    padding: 12px 16px;
  }

  .search-box {
    flex-direction: column;
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .search-box .el-button {
    width: 100%;
  }

  .filter-select {
    width: 100%;
  }

  .date-picker {
    width: 100%;
  }

  .form-row {
    flex-direction: column;
    gap: 16px;
  }

  .form-item {
    min-width: 100%;
  }

  .price-input {
    width: 100%;
  }

  .merchant-input {
    width: 100%;
  }
}
</style>