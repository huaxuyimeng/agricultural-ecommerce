package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.entity.Merchant;
import com.example.vuehouduan.entity.User;
import com.example.vuehouduan.mapper.MerchantMapper;
import com.example.vuehouduan.mapper.UserMapper;
import com.example.vuehouduan.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * 商家服务实现 - 商家信息管理、审核
 * ============================================================
 *
 * 【前后端商家管理流程】
 *
 * 1. 商家列表（管理员）：
 * 前端 GET /api/admin/merchants?page=1&pageSize=10&status=PENDING&keyword=
 * 后端 → 查询user表中role=MERCHANT的记录 → 关联merchant表 → 合并信息返回
 * 返回 → { list: [...], total: 100 }
 *
 * 2. 审核商家：
 * 前端 PUT /api/admin/merchants/{id}/approve 或 /reject
 * 后端 → 更新merchant表的approval_status字段
 * → 如果通过审核，同时更新user表的status=1（启用）
 *
 * 3. 商家详情：
 * 前端 GET /api/merchants/{id}
 * 后端 → 查询merchant表 → 关联user表 → 返回合并信息
 *
 * 【数据表关系】
 * user 表：存储商家用户基本信息（role=MERCHANT）
 * merchant 表：存储商家额外信息（店铺名、营业执照、审核状态），通过id关联user表
 */
@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取商家列表（管理员端，分页）
     * 查询user表中role=MERCHANT的记录 → 关联merchant表 → 合并信息返回
     * 支持按关键词搜索（用户名或姓名）
     */
    @Override
    public Map<String, Object> getMerchantList(Integer page, Integer pageSize, String status, String keyword) {
        Page<User> pageObj = new Page<>(page, pageSize);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", "MERCHANT");
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("username", keyword).or().like("name", keyword));
        }
        wrapper.orderByDesc("create_time");
        Page<User> result = userMapper.selectPage(pageObj, wrapper);

        for (User u : result.getRecords()) {
            Merchant m = baseMapper.selectById(u.getId());
            if (m != null) {
                // Copy merchant fields to user for frontend compatibility
                u.setShopName(m.getShopName());
                u.setShopAddress(m.getShopAddress());
                u.setShopDescription(m.getShopDescription());
                u.setShopLogo(m.getShopLogo());
                u.setBusinessLicense(m.getBusinessLicense());
                u.setApprovalStatus(m.getApprovalStatus());
                u.setAvatar(m.getShopLogo());
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return map;
    }

    /**
     * 获取商家详情
     * 查询user表和merchant表 → 合并信息返回
     */
    @Override
    public Map<String, Object> getMerchantById(Long id) {
        User user = userMapper.selectById(id);
        Merchant merchant = baseMapper.selectById(id);
        if (merchant != null && user != null) {
            // Copy merchant fields to user for frontend compatibility
            user.setShopName(merchant.getShopName());
            user.setShopAddress(merchant.getShopAddress());
            user.setShopDescription(merchant.getShopDescription());
            user.setShopLogo(merchant.getShopLogo());
            user.setBusinessLicense(merchant.getBusinessLicense());
            user.setApprovalStatus(merchant.getApprovalStatus());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("merchant", merchant);
        return result;
    }

    /**
     * 审核通过商家
     * 更新merchant表的approval_status为APPROVED → 设置审核时间
     */
    @Override
    public void approveMerchant(Long id) {
        Merchant merchant = baseMapper.selectById(id);
        if (merchant == null) {
            throw new RuntimeException("商家不存在");
        }
        merchant.setApprovalStatus("APPROVED");
        merchant.setApproveTime(LocalDateTime.now());
        baseMapper.updateById(merchant);
    }

    /**
     * 禁用商家账号
     * 更新user表的status为0（禁用状态）
     */
    @Override
    public void disableMerchant(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(0);
        userMapper.updateById(user);
    }
}
