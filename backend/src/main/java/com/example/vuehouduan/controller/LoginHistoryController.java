package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.service.LoginHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "登录历史")
@RestController
@RequestMapping("/api/login-history")
public class LoginHistoryController {

    @Autowired
    private LoginHistoryService loginHistoryService;

    @ApiOperation("获取当前用户登录历史")
    @GetMapping
    public Result<?> getLoginHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = loginHistoryService.getLoginHistory(userId, page, pageSize);
        return Result.success(result);
    }

    @ApiOperation("记录登录")
    @PostMapping
    public Result<?> recordLogin(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String ip = params.get("ip");
        String userAgent = params.get("userAgent");
        loginHistoryService.recordLogin(userId, username, ip, userAgent, "SUCCESS", null);
        return Result.success();
    }

    @ApiOperation("记录登出")
    @PutMapping("/{id}/logout")
    public Result<?> recordLogout(@PathVariable Long id) {
        loginHistoryService.recordLogout(id);
        return Result.success();
    }
}
