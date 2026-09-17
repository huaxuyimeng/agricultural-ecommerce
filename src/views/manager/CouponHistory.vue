/**
 * 优惠券使用历史页面
 * 文件路径: src/views/manager/CouponHistory.vue
 * 功能描述: 查看所有优惠券的使用历史记录，展示用户使用优惠券的详细流水（用户名、优惠券名称、面额、订单金额、
 *           使用时间、订单编号），支持按用户名和优惠券名称搜索筛选、日期范围筛选，数据导出，分页展示
 * 关联文件:
 * - src/api/index.js: 提供优惠券使用历史接口
 * - src/views/manager/CouponManage.vue: 优惠券管理页面
 * - src/views/manager/Manager.vue: 后台管理框架布局
 */
<template>
  <div class="coupon-history-page">
    <div class="page-header">
      <div class="header-left">
        <el-button type="text" icon="el-icon-arrow-left" @click="$router.push('/coupon-manage')">
          返回优惠券管理
        </el-button>
        <h2 class="page-title">
          <i class="el-icon-time"></i>
          优惠券使用历史
        </h2>
        <p class="page-subtitle">查看所有用户使用优惠券的详细记录</p>
      </div>
      <div class="header-right">
        <el-button @click="exportData">
          <i class="el-icon-download"></i>
          导出数据
        </el-button>
      </div>
    </div>

    <el-card class="filter-card" shadow="never">
      <div class="filter-bar">
        <div class="filter-left">
          <el-input
            v-model="searchUsername"
            placeholder="搜索用户名"
            clearable
            prefix-icon="el-icon-search"
            class="search-input"
            @keyup.enter.native="loadData"
            @clear="loadData"
          />
          <el-input
            v-model="searchCouponName"
            placeholder="搜索优惠券名称"
            clearable
            prefix-icon="el-icon-present"
            class="search-input"
            @keyup.enter.native="loadData"
            @clear="loadData"
          />
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            class="date-picker"
            @change="loadData"
          />
          <el-button type="primary" @click="loadData">
            <i class="el-icon-search"></i>
            查询
          </el-button>
        </div>
        <div class="filter-right">
          <el-button icon="el-icon-refresh" @click="loadData" circle title="刷新"></el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        class="history-table"
        empty-text="暂无使用记录"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="用户名" width="120" align="center">
          <template #default="{ row }">
            <span class="username-text">{{ row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="couponName" label="优惠券名称" min-width="150">
          <template #default="{ row }">
            <span class="coupon-name-text">{{ row.couponName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="优惠面额" width="100" align="center">
          <template #default="{ row }">
            <span class="amount-text">¥{{ Number(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" width="120" align="center">
          <template #default="{ row }">
            <span class="order-amount">¥{{ row.orderAmount ? Number(row.orderAmount) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单编号" width="180" align="center">
          <template #default="{ row }">
            <span class="order-no">{{ row.orderNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="使用时间" width="170" align="center">
          <template #default="{ row }">
            <span>{{ formatTime(row.useTime || row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          :current-page.sync="pageNum"
          :page-size.sync="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { getCouponUsageHistory } from '@/api/index'

export default {
  name: 'CouponHistory',
  data() {
    return {
      loading: false,
      searchUsername: '',
      searchCouponName: '',
      dateRange: [],
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      total: 0
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
        if (this.searchUsername) params.username = this.searchUsername
        if (this.searchCouponName) params.couponName = this.searchCouponName
        if (this.dateRange && this.dateRange.length === 2) {
          params.startTime = this.dateRange[0] + ' 00:00:00'
          params.endTime = this.dateRange[1] + ' 23:59:59'
        }
        const res = await getCouponUsageHistory(params)
        if (res.code === 200 && res.data) {
          this.tableData = res.data.records || res.data.list || []
          this.total = res.data.total || 0
        } else {
          this.tableData = []
          this.total = 0
        }
      } catch (err) {
        console.error('获取使用历史失败:', err)
        this.$message.error('获取使用历史失败')
        this.tableData = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    exportData() {
      if (this.tableData.length === 0) {
        this.$message.warning('暂无数据可导出')
        return
      }
      const headers = ['ID', '用户名', '优惠券名称', '优惠面额', '订单金额', '订单编号', '使用时间', '状态']
      const keys = ['id', 'username', 'couponName', 'amount', 'orderAmount', 'orderNo', 'useTime', 'status']
      let csv = '\uFEFF' + headers.join(',') + '\n'
      this.tableData.forEach(row => {
        const rowData = keys.map(key => {
          let val = row[key]
          if (key === 'useTime' || key === 'createTime') val = this.formatTime(val)
          if (key === 'status') val = this.getStatusLabel(val)
          if (val === null || val === undefined) val = ''
          val = String(val).replace(/"/g, '""')
          return `"${val}"`
        })
        csv += rowData.join(',') + '\n'
      })
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `优惠券使用历史_${new Date().toISOString().slice(0, 10)}.csv`
      link.click()
      URL.revokeObjectURL(url)
      this.$message.success('导出成功')
    },

    formatTime(time) {
      if (!time) return '-'
      const d = new Date(time)
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const h = String(d.getHours()).padStart(2, '0')
      const min = String(d.getMinutes()).padStart(2, '0')
      const s = String(d.getSeconds()).padStart(2, '0')
      return `${y}-${m}-${day} ${h}:${min}:${s}`
    },

    getStatusLabel(status) {
      const map = { 0: '已使用', 1: '已过期', 2: '已退回' }
      return map[status] !== undefined ? map[status] : '未知'
    },

    getStatusTag(status) {
      const map = { 0: 'success', 1: 'info', 2: 'warning' }
      return map[status] || 'info'
    }
  }
}
</script>

<style scoped>
.coupon-history-page {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.header-left .page-title {
  margin: 8px 0 6px 0;
  font-size: 22px;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.filter-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: none;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 200px;
}

.date-picker {
  width: 280px;
}

.table-card {
  border-radius: 12px;
  border: none;
}

.amount-text {
  color: #f56c6c;
  font-weight: 600;
  font-size: 15px;
}

.order-amount {
  color: #409eff;
  font-weight: 500;
}

.order-no {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

.username-text {
  font-weight: 500;
  color: #303133;
}

.coupon-name-text {
  font-weight: 500;
  color: #303133;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>