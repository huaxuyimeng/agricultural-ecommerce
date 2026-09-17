package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.entity.Product;
import java.util.List;
import java.util.Map;

public interface ProductService extends IService<Product> {
    Map<String, Object> getProductList(Integer page, Integer pageSize, String category, String keyword,
                                        Boolean isHot, Boolean isNew, Boolean isRecommend,
                                        Long merchantId, String sortBy, String sortOrder);
    Product getProductById(Long id);
    List<Product> getHotProducts(Integer limit);
    List<Product> getNewProducts(Integer limit);
    List<Product> getRecommendedProducts(Integer limit, String category);
    List<Product> searchProducts(String keyword, String category);
    Product createProduct(Product product, Long merchantId);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
    void approveProduct(Long id);
    void rejectProduct(Long id);
    Map<String, Object> getMerchantProducts(Long merchantId, Integer page, Integer pageSize, String status);
    Map<String, Object> getPendingProducts(Integer page, Integer pageSize);
    Map<String, Object> getProductStats();
}
