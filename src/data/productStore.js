import {
  getProductPage,
  getProductById as apiGetProductById,
  deleteProduct,
  getPendingProducts,
  adminApproveProduct,
  adminRejectProduct,
  getMerchantProductPage
} from '@/api'

async function getApprovedProducts() {
  try {
    const res = await getProductPage({ page: 1, pageSize: 100, status: 'approved' })
    return res.data?.list || res.data?.records || []
  } catch {
    return []
  }
}

async function getMerchantProducts() {
  try {
    const res = await getMerchantProductPage({ page: 1, pageSize: 100 })
    return res.data?.list || res.data?.records || []
  } catch {
    return []
  }
}

async function getProductStats() {
  try {
    const [approvedRes, pendingRes] = await Promise.all([
      getProductPage({ page: 1, pageSize: 1 }),
      getPendingProducts({ page: 1, pageSize: 1 })
    ])
    const total = approvedRes.data?.total || 0
    const pending = pendingRes.data?.total || 0
    return { total, pending, approved: total, rejected: 0 }
  } catch {
    return { total: 0, pending: 0, approved: 0, rejected: 0 }
  }
}

async function approveProduct(productId) {
  try {
    await adminApproveProduct(productId)
    return true
  } catch {
    return false
  }
}

async function rejectProduct(productId, reason = '') {
  try {
    await adminRejectProduct(productId)
    return true
  } catch {
    return false
  }
}

async function returnProduct(productId, reason = '', operator = '管理员') {
  try {
    await adminRejectProduct(productId, reason || '需要修改')
    return true
  } catch {
    return false
  }
}

async function removeProductEverywhere(productId) {
  try {
    await deleteProduct(productId)
    return true
  } catch {
    return false
  }
}

async function getProductById(productId) {
  try {
    const res = await apiGetProductById(productId)
    return res.data
  } catch {
    return null
  }
}

async function getMerchantInfo(merchantId) {
  try {
    const { getUserById } = await import('@/api')
    const res = await getUserById(merchantId)
    return res.data
  } catch {
    return null
  }
}

async function getProductReviewHistory(productId) {
  try {
    const { getProductReviews } = await import('@/api')
    const res = await getProductReviews(productId, { pageNum: 1, pageSize: 100 })
    return res.data?.records || []
  } catch {
    return []
  }
}

export {
  getApprovedProducts,
  getMerchantProducts,
  getProductStats,
  approveProduct,
  rejectProduct,
  returnProduct,
  removeProductEverywhere,
  getProductById,
  getMerchantInfo,
  getProductReviewHistory
}
