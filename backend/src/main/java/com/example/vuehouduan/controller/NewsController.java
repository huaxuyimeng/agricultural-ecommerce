package com.example.vuehouduan.controller;

import com.example.vuehouduan.common.Result;
import com.example.vuehouduan.entity.News;
import com.example.vuehouduan.service.NewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Api(tags = "新闻管理")
@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @ApiOperation("获取新闻列表")
    @GetMapping
    public Result<?> getNewsList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category) {
        Map<String, Object> result = newsService.getNewsList(page, pageSize, category);
        return Result.success(result);
    }

    @ApiOperation("获取新闻详情")
    @GetMapping("/{id}")
    public Result<?> getNewsById(@PathVariable Long id) {
        News news = newsService.getNewsById(id);
        return Result.success(news);
    }

    @ApiOperation("创建新闻")
    @PostMapping
    public Result<?> createNews(@RequestBody News news) {
        News created = newsService.createNews(news);
        return Result.success(created);
    }

    @ApiOperation("更新新闻")
    @PutMapping("/{id}")
    public Result<?> updateNews(@PathVariable Long id, @RequestBody News news) {
        try {
            News updated = newsService.updateNews(id, news);
            return Result.success(updated);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @ApiOperation("删除新闻")
    @DeleteMapping("/{id}")
    public Result<?> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return Result.success("删除成功");
    }

    @ApiOperation("点赞新闻")
    @PostMapping("/{id}/like")
    public Result<?> likeNews(@PathVariable Long id) {
        newsService.likeNews(id);
        return Result.success("点赞成功");
    }

    @ApiOperation("分享新闻")
    @PostMapping("/{id}/share")
    public Result<?> shareNews(@PathVariable Long id) {
        newsService.shareNews(id);
        return Result.success("分享成功");
    }
}
