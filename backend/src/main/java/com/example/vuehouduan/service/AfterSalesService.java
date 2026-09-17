package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.entity.AfterSales;
import java.util.Map;

public interface AfterSalesService extends IService<AfterSales> {
    AfterSales createAfterSales(Long userId, Map<String, Object> data);
    Map<String, Object> getUserAfterSales(Long userId, Integer page, Integer pageSize, String status);
    AfterSales getAfterSalesById(Long id, Long userId);
    void approveAfterSales(Long id, Long merchantId, String remark);
    void rejectAfterSales(Long id, Long merchantId, String remark);
    void completeAfterSales(Long id, Long merchantId);
    void cancelAfterSales(Long id, Long userId);
    void updateExpress(Long id, Long userId, String expressCompany, String expressNo);
    Map<String, Object> getMerchantAfterSales(Long merchantId, Integer page, Integer pageSize, String status);
}
