package com.example.vuehouduan.service;

import java.util.Map;

public interface SystemService {
    Map<String, Object> getSystemStatus();
    Map<String, Object> getPerformanceMetrics();
    Map<String, Object> getLogs(Integer page, Integer pageSize, String level, String keyword);
    Map<String, Object> backupData();
    Map<String, Object> getBackupList();
}
