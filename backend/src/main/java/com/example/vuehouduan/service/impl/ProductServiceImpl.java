package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.entity.Merchant;
import com.example.vuehouduan.entity.Product;
import com.example.vuehouduan.entity.ProductImage;
import com.example.vuehouduan.mapper.MerchantMapper;
import com.example.vuehouduan.mapper.ProductImageMapper;
import com.example.vuehouduan.mapper.ProductMapper;
import com.example.vuehouduan.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================
 * 商品服务实现 - 商品CRUD、搜索、分类、审核
 * ============================================================
 *
 * 【前后端商品浏览流程】
 *
 * 1. 商品列表：
 * 前端 GET
 * /api/products?page=1&pageSize=12&category=蔬菜&keyword=黄瓜&sortBy=price&sortOrder=asc
 * 后端 → 构建QueryWrapper条件 → 分页查询 → 填充商家信息 → 填充商品图片
 * 返回 → { list: [...], total: 100, page: 1, pageSize: 12 }
 *
 * 2. 商品详情：
 * 前端 GET /api/products/{id}
 * 后端 → 查询商品 → 浏览量+1 → 填充商家信息 → 填充商品图片列表
 * 返回 → 商品对象（含images数组、merchantName等）
 *
 * 3. 商家发布商品：
 * 前端 POST /api/products
 * Body: { name, description, price, category, stock, images:["url1","url2"],
 * ... }
 * 后端 → 设置merchantId → 状态设为pending(待审核) → 插入product表
 * → 保存商品图片到product_image表
 * 返回 → 商品对象
 *
 * 4. 管理员审核商品：
 * 前端 PUT /api/admin/products/{id}/approve 或 /reject
 * 后端 → 更新商品状态为approved/rejected
 *
 * 【数据表关系】
 * product 表：商品主表（名称、价格、库存、分类、状态等）
 * product_image 表：商品图片表（product_id关联，支持多图）
 * merchant 表：商家表（填充商家名称和头像到商品信息中）
 *
 * 【商品状态流转】
 * pending(待审核) → approved(已通过) / rejected(已拒绝)
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ProductImageMapper productImageMapper;

    /**
     * 获取商品列表（分页、筛选、排序）
     *
     * 【前后端数据流】
     * 前端 GET
     * /api/products?page=1&pageSize=12&category=蔬菜&keyword=黄瓜&isHot=true&sortBy=price&sortOrder=asc
     * 后端 → 构建多条件QueryWrapper → 只查询status=approved的商品 → 分页查询
     * → 填充商家信息 → 填充商品图片
     * 返回 → { list, total, page, pageSize }
     *
     * 【支持的筛选条件】
     * - category: 商品分类
     * - keyword: 名称/描述模糊搜索
     * - isHot/isNew/isRecommend: 布尔筛选
     * - merchantId: 按商家筛选
     * - sortBy: price/sales/views
     * - sortOrder: asc/desc
     */
    @Override
    public Map<String, Object> getProductList(Integer page, Integer pageSize, String category, String keyword,
            Boolean isHot, Boolean isNew, Boolean isRecommend,
            Long merchantId, String sortBy, String sortOrder) {
        Page<Product> pageObj = new Page<>(page, pageSize);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "approved");

        if (StringUtils.hasText(category)) {
            wrapper.eq("category", category);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("name", keyword).or().like("description", keyword));
        }
        if (isHot != null && isHot) {
            wrapper.eq("is_hot", 1);
        }
        if (isNew != null && isNew) {
            wrapper.eq("is_new", 1);
        }
        if (isRecommend != null && isRecommend) {
            wrapper.eq("is_recommend", 1);
        }
        if (merchantId != null) {
            wrapper.eq("merchant_id", merchantId);
        }

        if ("price".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(sortOrder), "price");
        } else if ("sales".equals(sortBy)) {
            wrapper.orderByDesc("sales");
        } else if ("views".equals(sortBy)) {
            wrapper.orderByDesc("views");
        } else {
            wrapper.orderByDesc("create_time");
        }

        Page<Product> result = baseMapper.selectPage(pageObj, wrapper);
        fillMerchantInfo(result.getRecords());
        for (Product p : result.getRecords()) {
            fillProductImages(p);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", page);
        map.put("pageSize", pageSize);
        return map;
    }

    /**
     * 获取商品详情（浏览量+1）
     *
     * 【前后端数据流】
     * 前端 GET /api/products/{id}
     * 后端 → 查询商品 → 浏览量自增 → 填充商家信息 → 填充图片列表
     * 返回 → 商品对象
     */
    @Override
    public Product getProductById(Long id) {
        Product product = baseMapper.selectById(id);
        if (product != null) {
            if (product.getViews() == null) {
                product.setViews(1);
            } else {
                product.setViews(product.getViews() + 1);
            }
            baseMapper.updateById(product);
            fillMerchantInfo(Collections.singletonList(product));
            fillProductImages(product);
        }
        return product;
    }

    /**
     * 获取热门商品（首页展示）
     * 按销量降序，限制数量
     */
    @Override
    public List<Product> getHotProducts(Integer limit) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "approved").eq("is_hot", 1).orderByDesc("sales")
                .last("LIMIT " + (limit != null ? limit : 10));
        List<Product> list = baseMapper.selectList(wrapper);
        fillMerchantInfo(list);
        for (Product p : list) {
            fillProductImages(p);
        }
        return list;
    }

    /**
     * 获取新品推荐
     * 按创建时间降序，限制数量
     */
    @Override
    public List<Product> getNewProducts(Integer limit) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "approved").eq("is_new", 1).orderByDesc("create_time")
                .last("LIMIT " + (limit != null ? limit : 10));
        List<Product> list = baseMapper.selectList(wrapper);
        fillMerchantInfo(list);
        for (Product p : list) {
            fillProductImages(p);
        }
        return list;
    }

    /**
     * 获取推荐商品
     * 按销量降序，支持按分类筛选
     */
    @Override
    public List<Product> getRecommendedProducts(Integer limit, String category) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "approved");
        if (StringUtils.hasText(category)) {
            wrapper.eq("category", category);
        }
        wrapper.eq("is_recommend", 1).orderByDesc("sales").last("LIMIT " + (limit != null ? limit : 10));
        List<Product> list = baseMapper.selectList(wrapper);
        fillMerchantInfo(list);
        for (Product p : list) {
            fillProductImages(p);
        }
        return list;
    }

    /**
     * 搜索商品
     * 支持关键词模糊搜索（名称+描述）和分类筛选
     */
    @Override
    public List<Product> searchProducts(String keyword, String category) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "approved");
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("name", keyword).or().like("description", keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq("category", category);
        }
        wrapper.orderByDesc("sales");
        List<Product> list = baseMapper.selectList(wrapper);
        fillMerchantInfo(list);
        for (Product p : list) {
            fillProductImages(p);
        }
        return list;
    }

    /**
     * 商家发布商品
     *
     * 【前后端数据流】
     * 前端 POST /api/products
     * Body: { name, description, price, originalPrice, category, stock, unit,
     * weight, origin, brand, images:["url1","url2"] }
     * 后端 → 设置merchantId → 状态设为pending(待管理员审核) → 初始化统计字段
     * → 插入product表 → 保存商品图片到product_image表
     * 返回 → 商品对象
     */
    @Override
    public Product createProduct(Product product, Long merchantId) {
        product.setMerchantId(merchantId);
        product.setStatus("pending");
        product.setViews(0);
        product.setSales(0);
        product.setFavorites(0);
        product.setReviews(0);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        product.setSubmitTime(LocalDateTime.now());

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String firstImage = product.getImages().get(0);
            if (firstImage != null && !firstImage.isEmpty()) {
                product.setImage(firstImage);
            }
        }

        baseMapper.insert(product);
        saveProductImages(product);
        return product;
    }

    /**
     * 更新商品信息
     * 采用"非null即更新"策略，前端只需传需要修改的字段
     * 如果传了images，会重新保存商品图片
     */
    @Override
    public Product updateProduct(Long id, Product product) {
        Product exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getName() != null)
            exist.setName(product.getName());
        if (product.getDescription() != null)
            exist.setDescription(product.getDescription());
        if (product.getPrice() != null)
            exist.setPrice(product.getPrice());
        if (product.getOriginalPrice() != null)
            exist.setOriginalPrice(product.getOriginalPrice());
        if (product.getImage() != null)
            exist.setImage(product.getImage());
        if (product.getCategory() != null)
            exist.setCategory(product.getCategory());
        if (product.getStock() != null)
            exist.setStock(product.getStock());
        if (product.getUnit() != null)
            exist.setUnit(product.getUnit());
        if (product.getWeight() != null)
            exist.setWeight(product.getWeight());
        if (product.getOrigin() != null)
            exist.setOrigin(product.getOrigin());
        if (product.getBrand() != null)
            exist.setBrand(product.getBrand());
        exist.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(exist);
        if (product.getImages() != null) {
            exist.setImages(product.getImages());
            saveProductImages(exist);
        }
        return exist;
    }

    /**
     * 删除商品
     */
    @Override
    public void deleteProduct(Long id) {
        baseMapper.deleteById(id);
    }

    /**
     * 审核通过商品（管理员操作）
     * 状态: pending → approved
     */
    @Override
    public void approveProduct(Long id) {
        Product product = baseMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        product.setStatus("approved");
        product.setApproveTime(LocalDateTime.now());
        baseMapper.updateById(product);
    }

    /**
     * 拒绝商品（管理员操作）
     * 状态: pending → rejected
     */
    @Override
    public void rejectProduct(Long id) {
        Product product = baseMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        product.setStatus("rejected");
        product.setApproveTime(LocalDateTime.now());
        baseMapper.updateById(product);
    }

    /**
     * 获取商家自己的商品列表（商家管理端）
     * 支持按审核状态筛选
     */
    @Override
    public Map<String, Object> getMerchantProducts(Long merchantId, Integer page, Integer pageSize, String status) {
        Page<Product> pageObj = new Page<>(page, pageSize);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("merchant_id", merchantId);
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        Page<Product> result = baseMapper.selectPage(pageObj, wrapper);
        fillMerchantInfo(result.getRecords());
        for (Product p : result.getRecords()) {
            fillProductImages(p);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return map;
    }

    /**
     * 获取待审核商品列表（管理员端）
     */
    @Override
    public Map<String, Object> getPendingProducts(Integer page, Integer pageSize) {
        Page<Product> pageObj = new Page<>(page, pageSize);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "pending").orderByDesc("submit_time");
        Page<Product> result = baseMapper.selectPage(pageObj, wrapper);
        fillMerchantInfo(result.getRecords());
        for (Product p : result.getRecords()) {
            fillProductImages(p);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return map;
    }

    /**
     * 获取商品统计信息
     * 返回各状态商品数量: { total, pending, approved, rejected }
     */
    @Override
    public Map<String, Object> getProductStats() {
        QueryWrapper<Product> wrapperAll = new QueryWrapper<>();
        long total = baseMapper.selectCount(wrapperAll);

        QueryWrapper<Product> wrapperPending = new QueryWrapper<>();
        wrapperPending.eq("status", "pending");
        long pending = baseMapper.selectCount(wrapperPending);

        QueryWrapper<Product> wrapperApproved = new QueryWrapper<>();
        wrapperApproved.eq("status", "approved");
        long approved = baseMapper.selectCount(wrapperApproved);

        QueryWrapper<Product> wrapperRejected = new QueryWrapper<>();
        wrapperRejected.eq("status", "rejected");
        long rejected = baseMapper.selectCount(wrapperRejected);

        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("pending", pending);
        map.put("approved", approved);
        map.put("rejected", rejected);
        return map;
    }

    /**
     * 批量填充商家信息到商品列表
     * 收集所有merchantId → 批量查询merchant表 → 设置merchantName和merchantAvatar
     */
    private void fillMerchantInfo(List<Product> products) {
        if (products == null || products.isEmpty())
            return;
        Set<Long> merchantIds = products.stream()
                .map(Product::getMerchantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (merchantIds.isEmpty())
            return;
        List<Merchant> merchants = merchantMapper.selectBatchIds(merchantIds);
        Map<Long, Merchant> merchantMap = merchants.stream()
                .collect(Collectors.toMap(Merchant::getId, m -> m));
        for (Product p : products) {
            Merchant m = merchantMap.get(p.getMerchantId());
            if (m != null) {
                p.setMerchantName(m.getShopName());
                p.setMerchantAvatar(m.getShopLogo());
            }
        }
    }

    /**
     * 保存商品图片到product_image表
     * 先删除旧图片 → 遍历images列表 → 第一张设为主图 → 同步更新product.image字段
     * 注意：如果图片是base64格式，直接存储到product.image字段，不存入product_image表
     */
    private void saveProductImages(Product product) {
        if (product.getId() == null)
            return;
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ProductImage> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("product_id", product.getId());
        productImageMapper.delete(wrapper);

        List<String> images = product.getImages();
        if (images == null || images.isEmpty()) {
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                ProductImage pi = new ProductImage();
                pi.setProductId(product.getId());
                pi.setImageUrl(product.getImage());
                pi.setIsMain(true);
                pi.setSortOrder(0);
                productImageMapper.insert(pi);
            }
            return;
        }

        String mainImage = null;
        for (int i = 0; i < images.size(); i++) {
            String url = images.get(i);
            if (url == null || url.isEmpty())
                continue;

            if (url.startsWith("data:image")) {
                if (i == 0) {
                    mainImage = url;
                }
                continue;
            }

            ProductImage pi = new ProductImage();
            pi.setProductId(product.getId());
            pi.setImageUrl(url);
            pi.setIsMain(i == 0);
            pi.setSortOrder(i);
            productImageMapper.insert(pi);
            if (i == 0) {
                mainImage = url;
            }
        }

        if (mainImage != null && !mainImage.equals(product.getImage())) {
            product.setImage(mainImage);
            baseMapper.updateById(product);
        }
    }

    /**
     * 填充商品图片列表
     * 从product_image表查询 → 提取URL列表 → 设置到product.images
     */
    private void fillProductImages(Product product) {
        if (product == null || product.getId() == null)
            return;
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ProductImage> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.eq("product_id", product.getId()).orderByAsc("sort_order");
        List<ProductImage> imageList = productImageMapper.selectList(wrapper);
        if (imageList != null && !imageList.isEmpty()) {
            List<String> urls = imageList.stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList());
            product.setImages(urls);
        }
    }
}
