package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.entity.Cart;
import java.util.List;

public interface CartService extends IService<Cart> {
    List<Cart> getCartList(Long userId);
    void addToCart(Long userId, Long productId, Integer count);
    void updateCartItem(Long id, Long userId, Integer quantity);
    void removeCartItem(Long id, Long userId);
    void clearCart(Long userId);
}
