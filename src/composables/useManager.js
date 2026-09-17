/**
 * 管理页面通用 Composable
 * 提供分页加载、搜索、筛选、CRUD 等通用功能
 */
import { ref, computed, watch } from '@vue/composition-api'
import { ElMessage } from 'element-ui'

/**
 * 分页列表 Composable
 */
export function usePageList(apiFunc, options = {}) {
  const {
    immediate = true,
    defaultPageSize = 10,
    transformData = (data) => data
  } = options

  // 状态
  const list = ref([])
  const total = ref(0)
  const page = ref(1)
  const pageSize = ref(defaultPageSize)
  const loading = ref(false)
  const searchParams = ref({})

  // 计算
  const hasMore = computed(() => list.value.length < total.value)

  // 加载数据
  const loadData = async () => {
    loading.value = true
    try {
      const params = {
        page: page.value,
        pageSize: pageSize.value,
        ...searchParams.value
      }
      const res = await apiFunc(params)

      let data = []
      if (res?.data?.list) data = res.data.list
      else if (res?.data?.records) data = res.data.records
      else if (Array.isArray(res?.data)) data = res.data
      else if (Array.isArray(res)) data = res

      list.value = transformData(data)
      total.value = res?.data?.total || data.length
    } catch (error) {
      console.error('加载数据失败:', error)
      ElMessage.error('加载数据失败')
      list.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  // 刷新
  const refresh = () => {
    page.value = 1
    return loadData()
  }

  // 分页变化
  const handlePageChange = (p) => {
    page.value = p
    loadData()
  }

  const handleSizeChange = (size) => {
    pageSize.value = size
    page.value = 1
    loadData()
  }

  // 搜索
  const search = (params) => {
    searchParams.value = params || {}
    page.value = 1
    return loadData()
  }

  // 立即加载
  if (immediate) {
    loadData()
  }

  return {
    list,
    total,
    page,
    pageSize,
    loading,
    searchParams,
    hasMore,
    loadData,
    refresh,
    handlePageChange,
    handleSizeChange,
    search
  }
}

/**
 * 表格选择 Composable
 */
export function useTableSelection() {
  const selectedRows = ref([])
  const selectedIds = computed(() => selectedRows.value.map(r => r.id))

  const handleSelectionChange = (rows) => {
    selectedRows.value = rows
  }

  const clearSelection = () => {
    selectedRows.value = []
  }

  const isSelected = (id) => selectedIds.value.includes(id)

  return {
    selectedRows,
    selectedIds,
    handleSelectionChange,
    clearSelection,
    isSelected
  }
}

/**
 * 对话框 Composable
 */
export function useDialog(options = {}) {
  const {
    defaultTitle = '',
    onSave = null,
    onClose = null
  } = options

  const visible = ref(false)
  const title = ref(defaultTitle)
  const isAdd = ref(true)
  const saving = ref(false)
  const formData = ref({})

  // 打开（新增）
  const openAdd = (defaultData = {}) => {
    isAdd.value = true
    title.value = options.addTitle || '新增'
    formData.value = { ...defaultData }
    visible.value = true
  }

  // 打开（编辑）
  const openEdit = (row, editTitle = '编辑') => {
    isAdd.value = false
    title.value = editTitle || options.editTitle || '编辑'
    formData.value = { ...row }
    visible.value = true
  }

  // 关闭
  const close = () => {
    visible.value = false
    onClose?.()
  }

  // 保存
  const save = async () => {
    if (!onSave) return
    saving.value = true
    try {
      await onSave(formData.value, isAdd.value)
      close()
    } finally {
      saving.value = false
    }
  }

  return {
    visible,
    title,
    isAdd,
    saving,
    formData,
    openAdd,
    openEdit,
    close,
    save
  }
}

/**
 * 删除确认 Composable
 */
export function useDelete() {
  const deleting = ref(false)

  const confirmDelete = async (id, name, deleteFunc) => {
    try {
      await ElMessageBox.confirm(
        `确定要删除「${name}」吗？此操作不可恢复。`,
        '删除确认',
        { type: 'warning' }
      )
      deleting.value = true
      await deleteFunc(id)
      ElMessage.success('删除成功')
      return true
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
      return false
    } finally {
      deleting.value = false
    }
  }

  const batchDelete = async (ids, deleteFunc) => {
    if (!ids.length) return false
    try {
      await ElMessageBox.confirm(
        `确定要删除选中的 ${ids.length} 项吗？此操作不可恢复。`,
        '批量删除确认',
        { type: 'warning' }
      )
      deleting.value = true
      await deleteFunc(ids)
      ElMessage.success('删除成功')
      return true
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
      return false
    } finally {
      deleting.value = false
    }
  }

  return {
    deleting,
    confirmDelete,
    batchDelete
  }
}

/**
 * 状态切换 Composable
 */
export function useStatusToggle(updateFunc) {
  const toggling = ref(false)

  const toggle = async (item, statusField = 'status', onToggle = null) => {
    const newStatus = item[statusField] === 1 ? 0 : 1
    const action = newStatus === 1 ? '启用' : '禁用'
    const name = item.name || item.title || item.username || '该项'

    try {
      await ElMessageBox.confirm(
        `确定要${action}「${name}」吗？`,
        `${action}确认`,
        { type: 'warning' }
      )
      toggling.value = true
      await updateFunc(item.id, { [statusField]: newStatus })
      item[statusField] = newStatus
      onToggle?.(item, newStatus)
      ElMessage.success(`${action}成功`)
      return true
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error(`${action}失败`)
      }
      return false
    } finally {
      toggling.value = false
    }
  }

  return { toggling, toggle }
}

/**
 * 格式化工具
 */
export const formatDateTime = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

export const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit'
  })
}

export const formatPrice = (price) => {
  return (parseFloat(price) || 0).toFixed(2)
}
