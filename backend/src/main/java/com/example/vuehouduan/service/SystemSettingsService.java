package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.entity.SystemSettings;

public interface SystemSettingsService extends IService<SystemSettings> {

    /**
     * 获取指定类型的设置
     */
    String getSettingsByType(String type);

    /**
     * 保存指定类型的设置
     */
    void saveSettingsByType(String type, String value);

    /**
     * 获取所有设置
     */
    java.util.Map<String, String> getAllSettings();
}
