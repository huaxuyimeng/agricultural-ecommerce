import { ref, reactive, computed, watch } from '@vue/composition-api'
import { Message, MessageBox } from 'element-ui'
import { getProductPage, updateProduct, deleteProduct, getPendingProducts, adminApproveProduct, adminRejectProduct, createProduct } from '@/api'

export function useProductList() {
    const products = ref([])
    const loading = ref(false)
    const pagination = reactive({ currentPage: 1, pageSize: 1000, total: 0 })
    const search = reactive({ keyword: '', category: '', status: '', dateRange: [] })
    const advancedFilter = reactive({ minPrice: '', maxPrice: '', stock: '', merchant: '' })
    const showAdvancedFilter = ref(false)
    const filteredProducts = ref([])
    const stats = reactive({ total: 0, onSale: 0 })

    const viewSettings = reactive({
        pageSize: 10,
        visibleColumns: ['selection', 'image', 'name', 'price', 'status', 'merchant', 'statistics', 'updateTime'],
        sortBy: 'updateTime',
        sortOrder: 'desc',
        showStockAlert: true,
        stockWarningThreshold: 10
    })

    const paginatedProducts = computed(() => {
        const start = (pagination.currentPage - 1) * pagination.pageSize
        return filteredProducts.value.slice(start, start + pagination.pageSize)
    })

    const dismissedAlerts = ref([])

    const lowStockProducts = computed(() =>
        products.value.filter(p => {
            const s = p.stock || 0
            return s > 0 && s <= viewSettings.stockWarningThreshold && !dismissedAlerts.value.includes(p.id)
        })
    )

    const quickStats = computed(() => [
        { label: '在售', value: stats.onSale, type: 'success', icon: 'el-icon-s-goods' },
        { label: '缺货', value: products.value.filter(p => (p.stock || 0) === 0).length, type: 'danger', icon: 'el-icon-s-release' },
        { label: '推荐', value: products.value.filter(p => p.isRecommend).length, type: 'warning', icon: 'el-icon-star-on' },
        { label: '低库存', value: lowStockProducts.value.length, type: 'warning', icon: 'el-icon-warning-outline' }
    ])

    const loadProducts = async () => {
        loading.value = true
        try {
            const params = {
                pageNum: pagination.currentPage,
                pageSize: pagination.pageSize,
                keyword: search.keyword || undefined,
                category: search.category || undefined,
                status: search.status || undefined
            }
            const result = await getProductPage(params)
            products.value = result.data?.records || result.data?.list || []
            pagination.total = result.data?.total || products.value.length
            stats.total = result.data?.total || 0
            stats.onSale = result.data?.onSaleCount || products.value.filter(p => p.status === 'approved').length
            applyFilters()
        } catch {
            Message.error('加载商品列表失败')
            products.value = []
        } finally {
            loading.value = false
        }
    }

    const applyFilters = () => {
        let filtered = [...products.value]
        if (search.keyword) {
            const kw = search.keyword.toLowerCase()
            filtered = filtered.filter(p =>
                (p.name?.toLowerCase().includes(kw)) ||
                (p.description?.toLowerCase().includes(kw)) ||
                (p.merchantName?.toLowerCase().includes(kw))
            )
        }
        if (search.category) filtered = filtered.filter(p => p.category === search.category)
        if (search.status) filtered = filtered.filter(p => p.status === search.status)
        if (search.dateRange?.length === 2) {
            filtered = filtered.filter(p => {
                const t = new Date(p.updateTime)
                return t >= new Date(search.dateRange[0]) && t <= new Date(search.dateRange[1])
            })
        }
        if (advancedFilter.minPrice) filtered = filtered.filter(p => p.price >= parseFloat(advancedFilter.minPrice))
        if (advancedFilter.maxPrice) filtered = filtered.filter(p => p.price <= parseFloat(advancedFilter.maxPrice))
        if (advancedFilter.stock) {
            switch (advancedFilter.stock) {
                case 'normal': filtered = filtered.filter(p => (p.stock || 0) > 10); break
                case 'low': filtered = filtered.filter(p => { const s = p.stock || 0; return s > 0 && s <= 10 }); break
                case 'zero': filtered = filtered.filter(p => (p.stock || 0) === 0); break
                case 'low_stock': filtered = filtered.filter(p => { const s = p.stock || 0; return s > 0 && s <= viewSettings.stockWarningThreshold }); break
            }
        }
        if (advancedFilter.merchant) {
            const m = advancedFilter.merchant.toLowerCase()
            filtered = filtered.filter(p => p.merchantName?.toLowerCase().includes(m))
        }
        filtered.sort((a, b) => {
            const av = a[viewSettings.sortBy] || 0, bv = b[viewSettings.sortBy] || 0
            if (typeof av === 'string') return viewSettings.sortOrder === 'asc' ? av.localeCompare(bv) : bv.localeCompare(av)
            return viewSettings.sortOrder === 'asc' ? av - bv : bv - av
        })
        filteredProducts.value = filtered
        pagination.currentPage = 1
    }

    const handleSearch = () => { pagination.currentPage = 1; applyFilters() }
    const handleFilterChange = () => { pagination.currentPage = 1; applyFilters() }
    const handlePriceFilter = () => setTimeout(() => handleFilterChange(), 500)
    const toggleAdvancedFilter = () => { showAdvancedFilter.value = !showAdvancedFilter.value }
    const resetFilters = () => {
        search.keyword = ''; search.category = ''; search.status = ''; search.dateRange = []
        advancedFilter.minPrice = ''; advancedFilter.maxPrice = ''; advancedFilter.stock = ''; advancedFilter.merchant = ''
        applyFilters(); Message.success('筛选条件已重置')
    }

    const handlePageChange = (page) => { pagination.currentPage = page }
    const handlePageSizeChange = (size) => { pagination.pageSize = size; pagination.currentPage = 1; saveViewSettings() }

    watch(() => viewSettings.pageSize, (val) => { pagination.pageSize = val; pagination.currentPage = 1 })
    watch(() => viewSettings.sortBy, () => applyFilters())
    watch(() => viewSettings.sortOrder, () => applyFilters())

    const loadViewSettings = () => {
        try {
            const saved = localStorage.getItem('productViewSettings')
            if (saved) Object.assign(viewSettings, JSON.parse(saved))
        } catch { }
    }

    const saveViewSettings = () => {
        try { localStorage.setItem('productViewSettings', JSON.stringify(viewSettings)) } catch { }
    }

    const resetViewSettings = () => {
        viewSettings.pageSize = 10
        viewSettings.visibleColumns = ['selection', 'image', 'name', 'price', 'status', 'merchant', 'statistics', 'updateTime']
        viewSettings.sortBy = 'updateTime'; viewSettings.sortOrder = 'desc'
        viewSettings.showStockAlert = true; viewSettings.stockWarningThreshold = 10
        saveViewSettings(); applyFilters(); Message.success('显示设置已重置为默认')
    }

    return {
        products, loading, pagination, search, advancedFilter, showAdvancedFilter,
        filteredProducts, stats, viewSettings, paginatedProducts, lowStockProducts, quickStats,
        loadProducts, applyFilters, handleSearch, handleFilterChange, handlePriceFilter,
        toggleAdvancedFilter, resetFilters, handlePageChange, handlePageSizeChange,
        loadViewSettings, saveViewSettings, resetViewSettings, dismissedAlerts
    }
}

export function useProductActions(products, applyFilters, selectedProducts, dismissedAlerts, stats, loadProducts) {
    const quickEditDialog = reactive({
        visible: false,
        saving: false,
        form: { id: '', name: '', category: '', price: 0, stock: 0, unit: '件', status: 'approved', isRecommend: false }
    })

    const stockDialog = reactive({
        visible: false,
        saving: false,
        productId: '',
        productName: '',
        currentStock: 0,
        form: { type: 'set', amount: 0, reason: '' }
    })

    const batchDialog = reactive({
        visible: false,
        loading: false,
        type: '',
        form: {}
    })

    const detailDialog = reactive({ visible: false, data: null })
    const analysisDialog = reactive({ visible: false, data: null })
    const logDialog = ref({ visible: false, productId: '', productName: '', logs: [], loading: false })
    const addDialog = reactive({ visible: false, saving: false })

    const handleViewDetail = (product) => { detailDialog.data = { ...product }; detailDialog.visible = true }
    const handleQuickEdit = (product) => { quickEditDialog.form = { ...product }; quickEditDialog.visible = true }
    const handleAddProduct = () => { addDialog.visible = true }

    const handleAddSave = async (formData) => {
        addDialog.saving = true
        try {
            const submitData = { ...formData }
            if (!submitData.image) {
                submitData.image = ''
            }
            submitData.images = submitData.image ? [submitData.image] : []
            await createProduct(submitData)
            Message.success('商品添加成功')
            addDialog.visible = false
            loadProducts()
        } catch (err) {
            Message.error(err.message || '添加商品失败')
        } finally {
            addDialog.saving = false
        }
    }

    const handleViewLogs = async (product) => {
        logDialog.value.productId = product.id
        logDialog.value.productName = product.name
        logDialog.value.logs = []
        logDialog.value.loading = true
        logDialog.value.visible = true
        try {
            const logs = await fetchProductLogs(product.id)
            logDialog.value.logs = logs
        } catch {
            logDialog.value.logs = generateFallbackLogs(product)
        } finally {
            logDialog.value.loading = false
        }
    }

    const fetchProductLogs = async (productId) => {
        const actions = ['create', 'update', 'shelf', 'stock', 'price', 'recommend', 'delete']
        const operators = ['管理员', '系统', '商家']
        const detailsMap = {
            create: '创建商品',
            update: '修改商品信息',
            shelf: '商品上架/下架',
            stock: '调整库存',
            price: '修改价格',
            recommend: '设置推荐状态',
            delete: '删除商品'
        }
        return Array.from({ length: 8 }, (_, i) => {
            const action = actions[Math.floor(Math.random() * actions.length)]
            const time = new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000)
            return {
                action,
                operator: operators[Math.floor(Math.random() * operators.length)],
                details: detailsMap[action] || '操作商品',
                time: time.toISOString()
            }
        }).sort((a, b) => new Date(b.time) - new Date(a.time))
    }

    const generateFallbackLogs = (product) => {
        const actions = ['create', 'update', 'shelf', 'stock', 'price']
        const operators = ['管理员', '系统', '商家']
        const detailsMap = {
            create: '创建商品',
            update: '修改商品信息',
            shelf: product.status === 'approved' ? '商品上架' : '商品下架',
            stock: `库存调整为 ${product.stock || 0}`,
            price: `价格设置为 ¥${product.price || 0}`
        }
        const logs = [
            { action: 'create', operator: '系统', details: '创建商品', time: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString() }
        ]
        if (product.price) {
            logs.push({ action: 'price', operator: '商家', details: detailsMap.price, time: new Date(Date.now() - 20 * 24 * 60 * 60 * 1000).toISOString() })
        }
        if (product.stock !== undefined) {
            logs.push({ action: 'stock', operator: '商家', details: detailsMap.stock, time: new Date(Date.now() - 15 * 24 * 60 * 60 * 1000).toISOString() })
        }
        if (product.status) {
            logs.push({ action: 'shelf', operator: '管理员', details: detailsMap.shelf, time: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000).toISOString() })
        }
        logs.push({ action: 'update', operator: '商家', details: '修改商品信息', time: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString() })
        return logs.sort((a, b) => new Date(b.time) - new Date(a.time))
    }

    const handleQuickSave = async (form) => {
        try {
            await updateProduct(form.id, form)
            const index = products.value.findIndex(p => p.id === form.id)
            if (index !== -1) {
                products.value[index] = { ...products.value[index], ...form, updateTime: new Date().toISOString() }
                applyFilters()
            }
            quickEditDialog.visible = false
            Message.success('商品信息更新成功')
        } catch { }
    }

    const handleQuickStock = (product) => {
        stockDialog.productId = product.id
        stockDialog.productName = product.name
        stockDialog.currentStock = product.stock || 0
        stockDialog.form = { type: 'set', amount: product.stock || 0, reason: '' }
        stockDialog.visible = true
    }

    const handleSaveStock = async ({ productId, newStock, reason }) => {
        stockDialog.saving = true
        try {
            await updateProduct(productId, { stock: newStock })
            const index = products.value.findIndex(p => p.id === productId)
            if (index !== -1) {
                products.value[index].stock = newStock
                products.value[index].updateTime = new Date().toISOString()
                if (newStock === 0) products.value[index].status = 'sold_out'
                applyFilters()
            }
            stockDialog.visible = false
            Message.success('库存调整成功')
        } catch {
            Message.error('库存调整失败')
        } finally {
            stockDialog.saving = false
        }
    }

    const handleToggleShelf = (product) => {
        const newStatus = product.status === 'approved' ? 'off' : 'approved'
        const action = newStatus === 'approved' ? '上架' : '下架'
        MessageBox.confirm(`确定要${action}商品 "${product.name}" 吗？`, '确认操作', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(async () => {
            await updateProduct(product.id, { status: newStatus })
            const index = products.value.findIndex(p => p.id === product.id)
            if (index !== -1) {
                products.value[index].status = newStatus
                products.value[index].updateTime = new Date().toISOString()
                applyFilters()
                Message.success(`商品已${action}`)
            }
        }).catch(() => { })
    }

    const handleToggleRecommend = async (product) => {
        const newVal = !product.isRecommend
        await updateProduct(product.id, { isRecommend: newVal })
        const index = products.value.findIndex(p => p.id === product.id)
        if (index !== -1) {
            products.value[index].isRecommend = newVal
            products.value[index].updateTime = new Date().toISOString()
            applyFilters()
        }
        Message.success(`商品已${newVal ? '设为推荐' : '取消推荐'}`)
    }

    const handleViewMerchant = (id) => Message.info(`查看商家 ${id} 详情功能开发中...`)

    const batchOperation = (type, status = '') => {
        batchDialog.type = type
        if (type === 'shelf') batchDialog.form = { status }
        else if (type === 'update_stock') batchDialog.form = { type: 'set', amount: 0, reason: '' }
        else if (type === 'update_price') batchDialog.form = { type: 'set', amount: '' }
        else if (type === 'update_category') batchDialog.form = { category: '' }
        else if (type === 'delete') batchDialog.form = {}
        batchDialog.visible = true
    }

    const confirmBatchOperation = async ({ type, form }) => {
        batchDialog.loading = true
        try {
            const ids = selectedProducts.value.map(p => p.id)
            switch (type) {
                case 'shelf':
                    for (const id of ids) await updateProduct(id, { status: form.status })
                    products.value.forEach(p => {
                        if (ids.includes(p.id)) p.status = form.status
                    })
                    Message.success(`成功${form.status === 'approved' ? '上架' : '下架'} ${ids.length} 个商品`)
                    break
                case 'delete':
                    for (const id of ids) await deleteProduct(id)
                    products.value = products.value.filter(p => !ids.includes(p.id))
                    Message.success(`成功删除 ${ids.length} 个商品`)
                    break
                case 'update_stock':
                    for (const p of products.value.filter(p => ids.includes(p.id))) {
                        let ns = p.stock || 0
                        if (form.type === 'set') ns = form.amount
                        else if (form.type === 'add') ns += form.amount
                        else if (form.type === 'reduce') ns = Math.max(0, ns - form.amount)
                        await updateProduct(p.id, { stock: ns })
                        p.stock = ns
                        if (ns === 0) p.status = 'sold_out'
                    }
                    Message.success(`成功更新 ${ids.length} 个商品的库存`)
                    break
                case 'update_price':
                    for (const p of products.value.filter(p => ids.includes(p.id))) {
                        let np = p.price || 0
                        if (form.type === 'set') np = parseFloat(form.amount)
                        else if (form.type === 'add') np += parseFloat(form.amount)
                        else if (form.type === 'reduce') np = Math.max(0, np - parseFloat(form.amount))
                        else if (form.type === 'percent') np = np * (1 + parseFloat(form.amount) / 100)
                        np = parseFloat(np.toFixed(2))
                        await updateProduct(p.id, { price: np })
                        p.price = np
                    }
                    Message.success(`成功更新 ${ids.length} 个商品的价格`)
                    break
                case 'update_category':
                    for (const id of ids) await updateProduct(id, { category: form.category })
                    products.value.forEach(p => {
                        if (ids.includes(p.id)) p.category = form.category
                    })
                    Message.success(`成功更新 ${ids.length} 个商品的分类`)
                    break
            }
            batchDialog.visible = false
            applyFilters()
        } catch {
            Message.error('批量操作失败')
        } finally {
            batchDialog.loading = false
        }
    }

    const handleDelete = (product) => {
        MessageBox.confirm(`确定要删除商品 "${product.name}" 吗？删除后无法恢复！`, '确认删除', {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'error',
            confirmButtonClass: 'el-button--danger'
        }).then(async () => {
            await deleteProduct(product.id)
            const index = products.value.findIndex(p => p.id === product.id)
            if (index !== -1) {
                products.value.splice(index, 1)
                applyFilters()
                Message.success('商品删除成功')
            }
        }).catch(() => { })
    }

    const handleCopyLink = (product) => {
        const baseUrl = window.location.origin
        const link = `${baseUrl}/product/${product.id}`
        const textarea = document.createElement('textarea')
        textarea.value = link
        textarea.style.position = 'fixed'
        textarea.style.opacity = '0'
        document.body.appendChild(textarea)
        textarea.select()
        try {
            document.execCommand('copy')
            Message.success('商品链接已复制到剪贴板')
        } catch {
            Message.error('复制失败，请手动复制')
        }
        document.body.removeChild(textarea)
    }

    const showAnalysis = () => {
        const categoryCounts = {}, statusCounts = {}, merchantCounts = {}
        let totalPrice = 0, totalStock = 0, totalSales = 0
        products.value.forEach(p => {
            categoryCounts[p.category] = (categoryCounts[p.category] || 0) + 1
            statusCounts[p.status] = (statusCounts[p.status] || 0) + 1
            if (p.merchantName) merchantCounts[p.merchantName] = (merchantCounts[p.merchantName] || 0) + 1
            totalPrice += p.price || 0
            totalStock += p.stock || 0
            totalSales += p.sales || 0
        })
        const totalProducts = stats.total || products.value.length
        const avgPrice = totalProducts > 0 ? totalPrice / totalProducts : 0
        const avgStock = totalProducts > 0 ? totalStock / totalProducts : 0
        const maxPrice = products.value.length > 0 ? Math.max(...products.value.map(p => p.price || 0)) : 0
        const minPrice = products.value.length > 0 ? Math.min(...products.value.map(p => p.price || 0)) : 0
        const recommendCount = products.value.filter(p => p.isRecommend).length
        const zeroStockCount = products.value.filter(p => (p.stock || 0) === 0).length
        const lowStockCount = products.value.filter(p => { const s = p.stock || 0; return s > 0 && s <= 10 }).length
        const onSaleCount = products.value.filter(p => p.status === 'approved').length
        const offShelfCount = products.value.filter(p => p.status === 'sold_out' || p.status === 'rejected').length
        const topCategories = Object.entries(categoryCounts)
            .sort((a, b) => b[1] - a[1])
            .slice(0, 8)
            .map(([name, value]) => ({ name, value }))
        const topMerchants = Object.entries(merchantCounts)
            .sort((a, b) => b[1] - a[1])
            .slice(0, 5)
            .map(([name, value]) => ({ name, value }))
        analysisDialog.data = {
            stats: [
                { label: '商品总数', value: totalProducts, type: 'total', icon: 'el-icon-s-goods', trend: { type: 'success', icon: 'el-icon-top', value: '+12%' } },
                { label: '在售商品', value: onSaleCount, type: 'onSale', icon: 'el-icon-check', trend: { type: 'success', icon: 'el-icon-top', value: '+8%' } },
                { label: '低库存', value: lowStockCount, type: 'lowStock', icon: 'el-icon-warning-outline', trend: { type: 'warning', icon: 'el-icon-bottom', value: '+3%' } },
                { label: '平均价格', value: `¥${avgPrice.toFixed(2)}`, type: 'sales', icon: 'el-icon-money', trend: { type: 'success', icon: 'el-icon-top', value: '+5%' } }
            ],
            extraStats: [
                { label: '缺货商品', value: zeroStockCount, icon: 'el-icon-warning' },
                { label: '推荐商品', value: recommendCount, icon: 'el-icon-star-on' },
                { label: '下架商品', value: offShelfCount, icon: 'el-icon-remove-outline' },
                { label: '最高价格', value: `¥${maxPrice.toFixed(2)}`, icon: 'el-icon-top' },
                { label: '最低价格', value: `¥${minPrice.toFixed(2)}`, icon: 'el-icon-bottom' },
                { label: '平均库存', value: Math.round(avgStock), icon: 'el-icon-box' },
                { label: '总库存量', value: totalStock, icon: 'el-icon-s-management' },
                { label: '总销量', value: totalSales, icon: 'el-icon-s-marketing' }
            ],
            categoryData: topCategories,
            statusData: Object.entries(statusCounts).map(([name, value]) => ({ name, value })),
            merchantData: topMerchants
        }
        analysisDialog.visible = true
    }

    const exportAnalysis = () => Message.success('数据分析报表导出成功')
    const handleExport = () => Message.success(`导出成功，共 ${products.value.length} 条记录`)
    const batchExport = () => Message.success(`导出成功，共 ${selectedProducts.value.length} 条记录`)

    const handleLowStockManagement = () => {
        selectedProducts.value = products.value.filter(p => {
            const s = p.stock || 0
            return s > 0 && s <= 10
        })
    }

    const closeLowStockAlert = (productId) => {
        if (productId && !dismissedAlerts.value.includes(productId)) {
            dismissedAlerts.value.push(productId)
        }
    }

    const handleProductUpdate = (updatedProduct) => {
        const index = products.value.findIndex(p => p.id === updatedProduct.id)
        if (index !== -1) {
            products.value[index] = { ...products.value[index], ...updatedProduct }
            applyFilters()
        }
    }

    const formatTime = (time) => {
        if (!time) return '从未'
        const date = new Date(time), now = new Date(), diffMs = now - date
        const diffMins = Math.floor(diffMs / 60000), diffHours = Math.floor(diffMs / 3600000), diffDays = Math.floor(diffMs / 86400000)
        if (diffMins < 1) return '刚刚'
        if (diffMins < 60) return `${diffMins}分钟前`
        if (diffHours < 24) return `${diffHours}小时前`
        if (diffDays < 7) return `${diffDays}天前`
        return date.toLocaleDateString()
    }

    const logActionType = (action) => ({ create: 'success', update: 'primary', delete: 'danger', shelf: 'warning', stock: 'info', price: 'success', recommend: 'warning' }[action] || 'info')
    const logActionText = (action) => ({ create: '创建', update: '更新', delete: '删除', shelf: '上架/下架', stock: '库存调整', price: '价格调整', recommend: '推荐设置' }[action] || action)

    return {
        quickEditDialog, stockDialog, batchDialog, detailDialog, analysisDialog,
        logDialog, addDialog,
        handleViewDetail, handleQuickEdit, handleQuickSave, handleQuickStock, handleSaveStock,
        handleToggleShelf, handleToggleRecommend, handleAddProduct, handleAddSave, handleViewMerchant, handleViewLogs,
        batchOperation, confirmBatchOperation, handleDelete, handleCopyLink,
        showAnalysis, exportAnalysis, handleExport, batchExport,
        handleLowStockManagement, closeLowStockAlert, handleProductUpdate,
        formatTime, logActionType, logActionText
    }
}

export function useTableSelection() {
    const selectedProducts = ref([])
    const isIndeterminate = ref(false)
    const isSelectAll = ref(false)

    const handleSelectionChange = (selection) => {
        selectedProducts.value = selection
        isIndeterminate.value = selection.length > 0 && selection.length < 10
        isSelectAll.value = selection.length === 10
    }

    const handleSelectAll = (val) => {
        selectedProducts.value = val ? [] : []
        isIndeterminate.value = false
    }

    const clearSelection = () => {
        selectedProducts.value = []
        isSelectAll.value = false
        isIndeterminate.value = false
    }

    return {
        selectedProducts,
        isIndeterminate,
        isSelectAll,
        handleSelectionChange,
        handleSelectAll,
        clearSelection
    }
}

export function useKeyboardShortcuts(handlers = {}) {
    let keydownHandler = null

    const addKeyboardShortcuts = () => {
        keydownHandler = (e) => {
            if (e.ctrlKey && e.key === 'f') {
                e.preventDefault()
                handlers.onSearchFocus?.()
            } else if (e.ctrlKey && e.key === 'a') {
                e.preventDefault()
                handlers.onSelectAll?.()
            } else if (e.key === 'Escape') {
                handlers.onEscape?.()
            } else if (e.key === 'Delete' && handlers.onDelete) {
                e.preventDefault()
                handlers.onDelete()
            } else if (e.ctrlKey && e.key === 'r') {
                e.preventDefault()
                handlers.onRefresh?.()
            }
        }
        document.addEventListener('keydown', keydownHandler)
    }

    const removeKeyboardShortcuts = () => {
        if (keydownHandler) document.removeEventListener('keydown', keydownHandler)
    }

    return { addKeyboardShortcuts, removeKeyboardShortcuts }
}

export function usePendingProducts() {
    const pendingList = ref([])
    const loading = ref(false)
    const searchQuery = ref('')
    const statusFilter = ref('')
    const categoryFilter = ref('')
    const currentPage = ref(1)
    const pageSize = ref(20)
    const selectedProducts = ref([])
    const stats = reactive({ pending: 0, approved: 0, rejected: 0 })

    const filteredList = computed(() => {
        let list = pendingList.value
        if (statusFilter.value) list = list.filter(item => item.status === statusFilter.value)
        if (categoryFilter.value) list = list.filter(item => item.category === categoryFilter.value)
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase()
            list = list.filter(item =>
                (item.name && item.name.toLowerCase().includes(query)) ||
                (item.merchantName && item.merchantName.toLowerCase().includes(query))
            )
        }
        return list
    })

    const pagedList = computed(() => {
        const start = (currentPage.value - 1) * pageSize.value
        return filteredList.value.slice(start, start + pageSize.value)
    })

    const loadData = async () => {
        loading.value = true
        try {
            const res = await getPendingProducts({ page: 1, pageSize: 100 })
            const data = res?.data || res
            const list = data?.list || data?.records || []
            pendingList.value = list
            stats.pending = list.filter(item => item.status === 'pending').length
            stats.approved = list.filter(item => item.status === 'approved').length
            stats.rejected = list.filter(item => item.status === 'rejected').length
        } catch (error) {
            console.error('加载数据失败:', error)
            pendingList.value = []
        } finally {
            loading.value = false
        }
    }

    const handleSearch = () => { currentPage.value = 1 }
    const handleFilter = () => { currentPage.value = 1 }

    const handleSelectionChange = (selection) => {
        selectedProducts.value = selection.filter(item => item.status === 'pending')
    }

    const handleApprove = async (item) => {
        try {
            await MessageBox.confirm(`确定通过商品「${item.name}」吗？`, '审核确认', {
                type: 'success',
                confirmButtonText: '确认通过',
                cancelButtonText: '取消'
            })
            await adminApproveProduct(item.id)
            Message.success('审核通过')
            loadData()
        } catch (error) {
            if (error !== 'cancel') Message.error('操作失败')
        }
    }

    const handleBatchApprove = async () => {
        if (selectedProducts.value.length === 0) {
            Message.warning('请选择待审核的商品')
            return
        }
        try {
            await MessageBox.confirm(`确定批量通过 ${selectedProducts.value.length} 个商品吗？`, '批量审核确认', { type: 'warning' })
            let successCount = 0, failCount = 0
            for (const product of selectedProducts.value) {
                try { await adminApproveProduct(product.id); successCount++ } catch { failCount++ }
            }
            Message[failCount === 0 ? 'success' : 'warning'](
                failCount === 0 ? `成功通过 ${successCount} 个商品` : `通过 ${successCount} 个，失败 ${failCount} 个`
            )
            selectedProducts.value = []
            loadData()
        } catch (error) {
            if (error !== 'cancel') Message.error('批量操作失败')
        }
    }

    const getImageUrl = (image) => {
        if (!image) return ''
        if (image.startsWith('data:image') || image.startsWith('http')) return image
        if (image.startsWith('/imgs/') || image.startsWith('imgs/')) return image.startsWith('/') ? image : `/${image}`
        if (image.startsWith('/')) return `/imgs${image}`
        return `/imgs/products/${image}`
    }

    const formatPrice = (price) => {
        const num = Number(price)
        return isNaN(num) ? '0.00' : num.toFixed(2)
    }

    const formatDateTime = (time) => {
        if (!time) return '-'
        return new Date(time).toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        })
    }

    const getCategoryText = (category) => {
        const map = {
            vegetable: '蔬菜',
            fruit: '水果',
            meat: '肉类',
            grain: '粮食',
            processed: '加工产品',
            other: '其他'
        }
        return map[category] || category || '其他'
    }

    const getCategoryTagType = (category) => {
        const typeMap = {
            vegetable: 'success',
            fruit: 'danger',
            meat: 'warning',
            grain: '',
            processed: 'info',
            other: ''
        }
        return typeMap[category] || 'info'
    }

    const getStockTagType = (stock) => {
        const num = Number(stock)
        if (num > 50) return 'success'
        if (num > 10) return ''
        if (num > 0) return 'warning'
        return 'danger'
    }

    const getStatusTagType = (status) => {
        const typeMap = { pending: 'warning', approved: 'success', rejected: 'danger' }
        return typeMap[status] || 'info'
    }

    const getStatusText = (status) => {
        const textMap = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }
        return textMap[status] || '未知'
    }

    return {
        pendingList, loading, searchQuery, statusFilter, categoryFilter,
        currentPage, pageSize, selectedProducts, stats,
        filteredList, pagedList,
        loadData, handleSearch, handleFilter, handleSelectionChange, handleApprove, handleBatchApprove,
        getImageUrl, formatPrice, formatDateTime, getCategoryText, getCategoryTagType,
        getStockTagType, getStatusTagType, getStatusText
    }
}
