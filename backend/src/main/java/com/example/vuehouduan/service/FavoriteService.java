package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.entity.Favorite;
import java.util.List;
import java.util.Map;

public interface FavoriteService extends IService<Favorite> {
    List<Map<String, Object>> getFavorites(Long userId);
    void addFavorite(Long userId, Long productId);
    void removeFavorite(Long id, Long userId);
    void removeFavoriteByProductId(Long userId, Long productId);
}
