package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.entity.News;
import java.util.Map;

public interface NewsService extends IService<News> {
    Map<String, Object> getNewsList(Integer page, Integer pageSize, String category);
    News getNewsById(Long id);
    News createNews(News news);
    News updateNews(Long id, News news);
    void deleteNews(Long id);
    void likeNews(Long id);
    void shareNews(Long id);
}
