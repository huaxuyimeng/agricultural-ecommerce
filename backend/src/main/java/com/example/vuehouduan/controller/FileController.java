package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * ============================================================
 * 文件上传控制器 - 处理图片/文件上传
 * ============================================================
 *
 * 【前后端文件上传流程】
 *
 * 1. 前端使用 <input type="file"> 或 Element UI 的 el-upload 组件选择文件
 * 2. 前端通过 FormData 封装文件数据：
 *    const formData = new FormData();
 *    formData.append('file', fileObject);  // 'file' 必须与后端 @RequestParam("file") 一致
 *    axios.post('/api/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
 * 3. 后端 FileController.upload() 接收 MultipartFile
 *    → 生成UUID文件名（防止重名）
 *    → 按日期分目录存储（防止单目录文件过多）
 *    → 保存到磁盘
 *    → 返回访问路径
 * 4. 前端拿到路径 → 拼接完整URL显示图片
 *    例如：返回 "20240501/abc123.jpg"
 *    前端拼接：`http://localhost:9090/upload/20240501/abc123.jpg`
 *
 * 【文件存储结构】
 * 项目目录/
 *   upload/
 *     20240501/
 *       abc123def456.jpg
 *       ...
 *     20240502/
 *       ...
 *
 * 【MultipartFile 原理】
 * Spring 自动解析 multipart/form-data 格式的请求，
 * 将文件部分封装为 MultipartFile 对象，包含：
 * - getOriginalFilename() → 原始文件名
 * - getSize() → 文件大小
 * - transferTo() → 保存到磁盘
 */
@Api(tags = "文件上传")
@RestController
@RequestMapping("/api")
public class FileController {

    /** 上传目录：项目根目录下的 upload 文件夹 */
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "upload";

    /**
     * 通用文件上传
     *
     * 【前端请求示例】
     * POST /api/upload
     * Content-Type: multipart/form-data
     * FormData: { file: <文件二进制数据> }
     *
     * 【后端响应示例】
     * { code:200, data:"20240501/abc123def456.jpg" }
     *
     * 【文件名处理】
     * UUID去横线 + 原后缀 → 保证唯一性，防止覆盖
     * 例如：原文件 "头像.png" → 新文件名 "a1b2c3d4e5f6.png"
     */
    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error(400, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error(400, "文件名无效");
        }

        // 提取文件后缀名（如 .jpg, .png）
        String suffix = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            suffix = originalFilename.substring(dotIndex);
        }

        // 生成UUID文件名，防止重名覆盖
        String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 按日期分目录存储，防止单目录文件过多
        String datePath = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String dirPath = UPLOAD_DIR + File.separator + datePath;

        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件到磁盘
        String filePath = dirPath + File.separator + newFileName;
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "文件保存失败: " + e.getMessage());
        }

        // 返回相对路径，前端拼接域名后访问
        String accessPath = datePath + "/" + newFileName;

        return Result.success(accessPath);
    }

    /**
     * 头像上传（与通用上传逻辑相同，独立接口方便前端区分）
     */
    @ApiOperation("头像上传")
    @PostMapping("/upload/avatar")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.error(400, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error(400, "文件名无效");
        }

        String suffix = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            suffix = originalFilename.substring(dotIndex);
        }

        String newFileName = "avatar_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16) + suffix;
        String datePath = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String dirPath = UPLOAD_DIR + File.separator + datePath;

        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = dirPath + File.separator + newFileName;
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(500, "头像保存失败: " + e.getMessage());
        }

        String accessPath = datePath + "/" + newFileName;
        return Result.success(accessPath);
    }
}