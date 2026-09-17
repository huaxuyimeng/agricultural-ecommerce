package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.entity.Address;
import com.example.vuehouduan.mapper.AddressMapper;
import com.example.vuehouduan.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ============================================================
 * 收货地址服务实现 - 地址CRUD、默认地址管理
 * ============================================================
 *
 * 【前后端地址管理流程】
 *
 * 1. 获取地址列表：
 * 前端 GET /api/addresses
 * 后端 → 根据userId查询 → 默认地址排最前 → 按创建时间降序
 * 返回 → 地址列表
 *
 * 2. 新增地址：
 * 前端 POST /api/addresses
 * Body: { name, phone, province, city, district, detail, isDefault }
 * 后端 → 如果设为默认，先取消其他默认地址 → 插入新地址
 *
 * 3. 修改地址：
 * 前端 PUT /api/addresses/{id}
 * 后端 → 验证权限 → 如果设为默认，先取消其他默认地址 → 更新
 *
 * 4. 删除地址：
 * 前端 DELETE /api/addresses/{id}
 * 后端 → 验证权限 → 删除
 *
 * 5. 设置默认地址：
 * 前端 PUT /api/addresses/{id}/default
 * 后端 → 取消所有默认 → 设置当前为默认
 *
 * 【默认地址逻辑】
 * 每个用户只能有一个默认地址，设置新默认时会自动取消旧默认
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    /**
     * 获取用户地址列表
     * 默认地址排最前，其余按创建时间降序
     */
    @Override
    public List<Address> getAddressList(Long userId) {
        QueryWrapper<Address> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("is_default").orderByDesc("create_time");
        return list(wrapper);
    }

    /**
     * 新增地址
     * 如果设为默认，先取消其他默认地址
     */
    @Override
    @Transactional
    public Address addAddress(Long userId, Address address) {
        address.setUserId(userId);

        // 如果设置为默认地址，先取消其他默认地址
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            cancelDefaultAddress(userId);
        }

        save(address);
        return address;
    }

    /**
     * 更新地址
     * 验证权限 → 如果设为默认，先取消其他默认地址 → 更新
     */
    @Override
    @Transactional
    public Address updateAddress(Long addressId, Address address, Long userId) {
        Address existAddress = getById(addressId);
        if (existAddress == null) {
            throw new RuntimeException("地址不存在");
        }
        if (!existAddress.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        // 如果设置为默认地址，先取消其他默认地址
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            cancelDefaultAddress(userId);
        }

        address.setId(addressId);
        address.setUserId(userId);
        updateById(address);
        return getById(addressId);
    }

    /**
     * 删除地址
     */
    @Override
    @Transactional
    public void deleteAddress(Long addressId, Long userId) {
        Address address = getById(addressId);
        if (address == null) {
            throw new RuntimeException("地址不存在");
        }
        if (!address.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        removeById(addressId);
    }

    /**
     * 设置默认地址
     * 先取消所有默认 → 设置当前为默认
     */
    @Override
    @Transactional
    public void setDefaultAddress(Long addressId, Long userId) {
        Address address = getById(addressId);
        if (address == null) {
            throw new RuntimeException("地址不存在");
        }
        if (!address.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        // 取消所有默认地址
        cancelDefaultAddress(userId);

        // 设置新的默认地址
        address.setIsDefault(true);
        updateById(address);
    }

    /**
     * 取消用户所有默认地址
     */
    private void cancelDefaultAddress(Long userId) {
        QueryWrapper<Address> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("is_default", true);
        Address defaultAddress = getOne(wrapper);
        if (defaultAddress != null) {
            defaultAddress.setIsDefault(false);
            updateById(defaultAddress);
        }
    }
}
