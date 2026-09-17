package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.service.SystemService;
import com.example.vuehouduan.service.SystemSettingsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "系统管理")
@RestController
@RequestMapping("/api/admin")
public class SystemController {

    @Autowired
    private SystemService systemService;

    @Autowired
    private SystemSettingsService systemSettingsService;

    @ApiOperation("获取服务器状态")
    @GetMapping("/system/status")
    public Result<?> getSystemStatus() {
        return Result.success(systemService.getSystemStatus());
    }

    @ApiOperation("获取性能指标")
    @GetMapping("/system/performance")
    public Result<?> getPerformanceMetrics() {
        return Result.success(systemService.getPerformanceMetrics());
    }

    @ApiOperation("获取日志列表")
    @GetMapping("/logs")
    public Result<?> getLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword) {
        return Result.success(systemService.getLogs(page, pageSize, level, keyword));
    }

    @ApiOperation("执行数据备份")
    @PostMapping("/backup")
    public Result<?> backupData() {
        return Result.success(systemService.backupData());
    }

    @ApiOperation("获取备份列表")
    @GetMapping("/backup")
    public Result<?> getBackupList() {
        return Result.success(systemService.getBackupList());
    }

    @ApiOperation("获取系统设置")
    @GetMapping("/settings")
    public Result<?> getSettings(@RequestParam(required = false) String type) {
        if (type != null && !type.isEmpty()) {
            String value = systemSettingsService.getSettingsByType(type);
            return Result.success(value);
        }
        return Result.success(systemSettingsService.getAllSettings());
    }

    @ApiOperation("保存系统设置")
    @PostMapping("/settings")
    public Result<?> saveSettings(@RequestBody Map<String, String> params) {
        String type = params.get("type");
        String value = params.get("value");
        if (type == null || value == null) {
            return Result.error("参数不完整");
        }
        systemSettingsService.saveSettingsByType(type, value);
        return Result.success("保存成功");
    }

    @ApiOperation("批量保存系统设置")
    @PostMapping("/settings/batch")
    public Result<?> saveSettingsBatch(@RequestBody Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            systemSettingsService.saveSettingsByType(entry.getKey(), entry.getValue());
        }
        return Result.success("批量保存成功");
    }
}
