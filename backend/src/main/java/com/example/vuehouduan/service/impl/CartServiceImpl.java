package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.entity.Cart;
import com.example.vuehouduan.entity.Product;
import com.example.vuehouduan.mapper.CartMapper;
import com.example.vuehouduan.mapper.ProductMapper;
import com.example.vuehouduan.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * 购物车服务实现 - 购物车CRUD
 * ============================================================
 *
 * 【前后端购物车流程】
 *
 * 1. 加入购物车：
 * 前端商品详情页 → 点击"加入购物车"
 * 前端 POST /api/cart
 * Body: { productId: 1, count: 2 }
 * 后端 → 查询商品信息 → 检查购物车是否已有该商品
 * → 有则累加数量 → 无则新增记录
 * 返回 → 成功消息
 *
 * 2. 查看购物车：
 * 前端 GET /api/cart
 * 后端 → 根据userId查询 → 填充商品名称和图片
 * 返回 → 购物车列表（含商品信息）
 *
 * 3. 修改数量：
 * 前端 PUT /api/cart/{id}
 * Body: { quantity: 3 }
 * 后端 → 验证权限 → 数量<=0则删除 → 否则更新数量和总价
 *
 * 4. 删除商品：
 * 前端 DELETE /api/cart/{id}
 * 后端 → 验证权限 → 删除记录
 *
 * 5. 清空购物车：
 * 前端 DELETE /api/cart/clear
 * 后端 → 删除该用户所有购物车记录
 *
 * 【与订单的关系】
 * 下单成功后，OrderServiceImpl.createOrder() 会自动清除购物车中已购买的商品
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 获取购物车列表
     * 查询用户所有购物车项 → 填充商品名称和图片
     */
    @Override
    public List<Cart> getCartList(Long userId) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        List<Cart> list = baseMapper.selectList(wrapper);
        for (Cart c : list) {
            Product p = productMapper.selectById(c.getProductId());
            if (p != null) {
                c.setProductName(p.getName());
                c.setProductImage(p.getImage());
            }
        }
        return list;
    }

    /**
     * 加入购物车
     * 如果购物车已有该商品则累加数量，否则新增记录
     */
    @Override
    public void addToCart(Long userId, Long productId, Integer count) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("product_id", productId);
        Cart exist = baseMapper.selectOne(wrapper);

        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + count);
            exist.setTotalPrice(product.getPrice().multiply(new BigDecimal(exist.getQuantity())));
            exist.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(exist);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(count);
            cart.setPrice(product.getPrice());
            cart.setTotalPrice(product.getPrice().multiply(new BigDecimal(count)));
            cart.setIsSelected(true);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            baseMapper.insert(cart);
        }
    }

    /**
     * 更新购物车商品数量
     * 数量<=0时自动删除该商品
     */
    @Override
    public void updateCartItem(Long id, Long userId, Integer quantity) {
        Cart cart = baseMapper.selectById(id);
        if (cart == null) {
            throw new RuntimeException("购物车项不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        if (quantity <= 0) {
            baseMapper.deleteById(id);
        } else {
            cart.setQuantity(quantity);
            cart.setTotalPrice(cart.getPrice().multiply(new BigDecimal(quantity)));
            cart.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(cart);
        }
    }

    /**
     * 删除购物车中的商品
     */
    @Override
    public void removeCartItem(Long id, Long userId) {
        Cart cart = baseMapper.selectById(id);
        if (cart == null) {
            throw new RuntimeException("购物车项不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        baseMapper.deleteById(id);
    }

    /**
     * 清空购物车
     * 删除该用户所有购物车记录
     */
    @Override
    public void clearCart(Long userId) {
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        baseMapper.delete(wrapper);
    }
}
