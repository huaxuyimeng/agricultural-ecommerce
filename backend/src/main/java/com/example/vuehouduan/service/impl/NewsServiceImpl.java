package com.example.vuehouduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuehouduan.entity.News;
import com.example.vuehouduan.mapper.NewsMapper;
import com.example.vuehouduan.service.NewsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * 新闻服务实现 - 新闻/公告管理
 * ============================================================
 *
 * 【前后端新闻流程】
 *
 * 1. 新闻列表：
 * 前端 GET /api/news?page=1&pageSize=10&category=公告
 * 后端 → 按分类筛选 → 按时间降序 → 分页返回
 * 返回 → { list: [...], total: 100 }
 *
 * 2. 新闻详情：
 * 前端 GET /api/news/{id}
 * 后端 → 查询新闻 → 浏览量+1 → 返回新闻对象
 *
 * 3. 发布新闻（管理员）：
 * 前端 POST /api/news
 * Body: { title, content, image, category, tags, author, source }
 * 后端 → 初始化统计字段 → 插入数据库
 *
 * 4. 点赞/分享：
 * 前端 PUT /api/news/{id}/like 或 /share
 * 后端 → 对应计数+1
 */
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {

    /**
     * 获取新闻列表（分页）
     * 支持按分类筛选，按时间降序
     */
    @Override
    public Map<String, Object> getNewsList(Integer page, Integer pageSize, String category) {
        Page<News> pageObj = new Page<>(page, pageSize);
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq("category", category);
        }
        wrapper.orderByDesc("create_time");
        Page<News> result = baseMapper.selectPage(pageObj, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return map;
    }

    /**
     * 获取新闻详情（浏览量+1）
     */
    @Override
    public News getNewsById(Long id) {
        News news = baseMapper.selectById(id);
        if (news != null) {
            news.setViews(news.getViews() == null ? 1 : news.getViews() + 1);
            baseMapper.updateById(news);
        }
        return news;
    }

    /**
     * 创建新闻（管理员操作）
     * 初始化浏览量、点赞数、分享数为0
     */
    @Override
    public News createNews(News news) {
        if (news.getDescription() == null || news.getDescription().isEmpty()) {
            news.setDescription(news.getTitle() != null ? news.getTitle() : "");
        }
        if (news.getExcerpt() == null) {
            news.setExcerpt("");
        }
        news.setViews(0);
        news.setLikes(0);
        news.setShares(0);
        news.setCreateTime(LocalDateTime.now());
        news.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(news);
        return news;
    }

    /**
     * 更新新闻（管理员操作）
     * 采用"非null即更新"策略
     */
    @Override
    public News updateNews(Long id, News news) {
        News exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new RuntimeException("新闻不存在");
        }
        if (news.getTitle() != null)
            exist.setTitle(news.getTitle());
        if (news.getDescription() != null)
            exist.setDescription(news.getDescription());
        if (news.getExcerpt() != null)
            exist.setExcerpt(news.getExcerpt());
        if (news.getContent() != null)
            exist.setContent(news.getContent());
        if (news.getImage() != null)
            exist.setImage(news.getImage());
        if (news.getCategory() != null)
            exist.setCategory(news.getCategory());
        if (news.getTags() != null)
            exist.setTags(news.getTags());
        if (news.getAuthor() != null)
            exist.setAuthor(news.getAuthor());
        if (news.getSource() != null)
            exist.setSource(news.getSource());
        exist.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(exist);
        return exist;
    }

    /**
     * 删除新闻
     */
    @Override
    public void deleteNews(Long id) {
        baseMapper.deleteById(id);
    }

    @Override
    public void likeNews(Long id) {
        News news = baseMapper.selectById(id);
        if (news != null) {
            news.setLikes(news.getLikes() == null ? 1 : news.getLikes() + 1);
            baseMapper.updateById(news);
        }
    }

    @Override
    public void shareNews(Long id) {
        News news = baseMapper.selectById(id);
        if (news != null) {
            news.setShares(news.getShares() == null ? 1 : news.getShares() + 1);
            baseMapper.updateById(news);
        }
    }
}
