/**
 * 收藏状态管理 Hook
 * 文件路径: src/composables/useFavorite.js
 * 功能描述: 提供收藏相关的状态管理和操作方法
 */
import { ref, computed } from '@vue/composition-api'
import { getUserFavorites, addFavorite as apiAddFavorite, removeFavorite as apiRemoveFavorite } from '@/api'
import { ElMessage } from 'element-ui'
import { notifyFavoritesChanged } from '@/utils/common'

export function useFavorite() {
  const favorites = ref([])
  const loading = ref(false)

  // 按类型分类收藏
  const favoriteProducts = computed(() => favorites.value.filter(f => f.type === 'product'))
  const favoriteNews = computed(() => favorites.value.filter(f => f.type === 'news'))

  // 加载收藏列表
  const loadFavorites = async () => {
    loading.value = true
    try {
      const res = await getUserFavorites()
      favorites.value = res.data || []
    } catch (e) {
      favorites.value = []
    } finally {
      loading.value = false
    }
  }

  // 添加收藏
  const addFavorite = async (targetId, type = 'product') => {
    try {
      await apiAddFavorite({ productId: targetId })
      ElMessage.success('收藏成功')
      notifyFavoritesChanged()
      await loadFavorites()
      return true
    } catch (e) {
      ElMessage.error(e.message || '收藏失败')
      return false
    }
  }

  // 取消收藏
  const removeFavorite = async (targetId, type = 'product') => {
    try {
      await apiRemoveFavorite(targetId)
      ElMessage.success('已取消收藏')
      notifyFavoritesChanged()
      await loadFavorites()
      return true
    } catch (e) {
      ElMessage.error('取消收藏失败')
      return false
    }
  }

  // 切换收藏状态
  const toggleFavorite = async (targetId, type = 'product') => {
    const isFavorited = favorites.value.some(f => 
      f.type === type && Number(f.targetId || f.target_id) === Number(targetId)
    )
    if (isFavorited) {
      return await removeFavorite(targetId, type)
    } else {
      return await addFavorite(targetId, type)
    }
  }

  // 检查是否已收藏
  const isFavorited = (targetId, type = 'product') => {
    return favorites.value.some(f => 
      f.type === type && Number(f.targetId || f.target_id) === Number(targetId)
    )
  }

  return {
    favorites,
    loading,
    favoriteProducts,
    favoriteNews,
    loadFavorites,
    addFavorite,
    removeFavorite,
    toggleFavorite,
    isFavorited
  }
}
