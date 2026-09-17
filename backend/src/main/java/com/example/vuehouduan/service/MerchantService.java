package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.entity.Merchant;
import java.util.Map;

public interface MerchantService extends IService<Merchant> {
    Map<String, Object> getMerchantList(Integer page, Integer pageSize, String status, String keyword);
    Map<String, Object> getMerchantById(Long id);
    void approveMerchant(Long id);
    void disableMerchant(Long id);
}
