package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.entity.SystemSettings;
import com.example.vuehouduan.mapper.SystemSettingsMapper;
import com.example.vuehouduan.service.SystemSettingsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemSettingsServiceImpl extends ServiceImpl<SystemSettingsMapper, SystemSettings> implements SystemSettingsService {

    @Override
    public String getSettingsByType(String type) {
        QueryWrapper<SystemSettings> wrapper = new QueryWrapper<>();
        wrapper.eq("setting_key", type);
        SystemSettings settings = baseMapper.selectOne(wrapper);
        if (settings != null) {
            return settings.getSettingValue();
        }
        return null;
    }

    @Override
    public void saveSettingsByType(String type, String value) {
        QueryWrapper<SystemSettings> wrapper = new QueryWrapper<>();
        wrapper.eq("setting_key", type);
        SystemSettings settings = baseMapper.selectOne(wrapper);
        if (settings != null) {
            settings.setSettingValue(value);
            baseMapper.updateById(settings);
        } else {
            settings = new SystemSettings();
            settings.setSettingKey(type);
            settings.setSettingValue(value);
            settings.setSettingType(type);
            baseMapper.insert(settings);
        }
    }

    @Override
    public Map<String, String> getAllSettings() {
        List<SystemSettings> settingsList = baseMapper.selectList(null);
        Map<String, String> result = new HashMap<>();
        for (SystemSettings settings : settingsList) {
            result.put(settings.getSettingKey(), settings.getSettingValue());
        }
        return result;
    }
}
