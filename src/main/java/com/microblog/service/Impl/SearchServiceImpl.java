package com.microblog.service.Impl;

import com.microblog.entity.DiscussPost;
import com.microblog.mapper.SearchMapper;
import com.microblog.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 搜索相关
 * @DATE: 2023/5/2 14:27
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SearchMapper searchMapper;

    /**
     * 分页搜索
     * @param keyword 搜索的关键词
     * @param current 当前页码
     * @param limit 每页显示多少条数据
     * @return
     */
    @Override
    public List<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        return searchMapper.queryDiscussPost(keyword, current, limit);
    }

    /**
     * 根据关键字搜索总条数
     * @param keyword 关键字
     * @return
     */
    @Override
    public int searchDiscussPostCount(String keyword) {
        return searchMapper.queryDiscussPostCount(keyword);
    }
}
