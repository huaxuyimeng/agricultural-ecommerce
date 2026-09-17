package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.entity.Favorite;
import com.example.vuehouduan.entity.Product;
import com.example.vuehouduan.mapper.FavoriteMapper;
import com.example.vuehouduan.mapper.ProductMapper;
import com.example.vuehouduan.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ============================================================
 * 收藏服务实现 - 商品收藏管理
 * ============================================================
 *
 * 【前后端收藏流程】
 *
 * 1. 添加收藏：
 * 前端商品详情页 → 点击"收藏"按钮
 * 前端 POST /api/favorites
 * Body: { productId: 1 }
 * 后端 → 验证商品存在 → 检查是否已收藏 → 插入收藏记录 → 商品收藏数+1
 * 返回 → 成功消息
 *
 * 2. 查看收藏列表：
 * 前端 GET /api/favorites
 * 后端 → 根据userId查询 → 关联商品信息 → 按时间降序
 * 返回 → 收藏列表（含商品详情）
 *
 * 3. 取消收藏：
 * 前端 DELETE /api/favorites/{id}
 * 后端 → 验证权限 → 删除收藏记录 → 商品收藏数-1
 *
 * 【数据表关系】
 * favorite 表：收藏记录（user_id, target_id, type）
 * product 表：商品表（收藏数同步更新）
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 获取用户收藏列表
     * 查询用户所有收藏 → 关联商品信息 → 返回收藏列表
     */
    @Override
    public List<Map<String, Object>> getFavorites(Long userId) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("type", "product").orderByDesc("create_time");
        List<Favorite> list = baseMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Favorite f : list) {
            Product product = productMapper.selectById(f.getTargetId());
            if (product != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", f.getId());
                map.put("targetId", f.getTargetId());
                map.put("targetName", f.getTargetName());
                map.put("product", product);
                map.put("createTime", f.getCreateTime());
                result.add(map);
            }
        }
        return result;
    }

    /**
     * 添加收藏
     * 验证商品存在 → 检查是否已收藏 → 插入收藏记录 → 商品收藏数+1
     */
    @Override
    public void addFavorite(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("type", "product").eq("target_id", productId);
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("已收藏");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setType("product");
        favorite.setTargetId(productId);
        favorite.setTargetName(product.getName());
        favorite.setCreateTime(LocalDateTime.now());
        baseMapper.insert(favorite);

        product.setFavorites(product.getFavorites() == null ? 1 : product.getFavorites() + 1);
        productMapper.updateById(product);
    }

    /**
     * 取消收藏（通过收藏ID）
     * 删除收藏记录 → 商品收藏数-1
     */
    @Override
    public void removeFavorite(Long id, Long userId) {
        Favorite favorite = baseMapper.selectById(id);
        if (favorite == null) {
            throw new RuntimeException("收藏不存在");
        }
        if (!favorite.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        baseMapper.deleteById(id);

        if ("product".equals(favorite.getType())) {
            Product product = productMapper.selectById(favorite.getTargetId());
            if (product != null && product.getFavorites() != null && product.getFavorites() > 0) {
                product.setFavorites(product.getFavorites() - 1);
                productMapper.updateById(product);
            }
        }
    }

    /**
     * 取消收藏（通过商品ID）
     * 删除收藏记录 → 商品收藏数-1
     */
    @Override
    public void removeFavoriteByProductId(Long userId, Long productId) {
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("type", "product").eq("target_id", productId);
        Favorite favorite = baseMapper.selectOne(wrapper);
        if (favorite == null) {
            throw new RuntimeException("收藏不存在");
        }
        baseMapper.deleteById(favorite.getId());

        Product product = productMapper.selectById(productId);
        if (product != null && product.getFavorites() != null && product.getFavorites() > 0) {
            product.setFavorites(product.getFavorites() - 1);
            productMapper.updateById(product);
        }
    }
}
