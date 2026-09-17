package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.service.LoginHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Api(tags = "管理员登录历史")
@RestController
@RequestMapping("/api/admin/login-history")
public class AdminLoginHistoryController {

    @Autowired
    private LoginHistoryService loginHistoryService;

    @ApiOperation("获取所有登录历史")
    @GetMapping
    public Result<?> getAllLoginHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username) {
        Map<String, Object> result = loginHistoryService.getAllLoginHistory(page, pageSize, userId, username);
        return Result.success(result);
    }
}
