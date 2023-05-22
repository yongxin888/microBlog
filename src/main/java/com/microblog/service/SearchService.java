package com.microblog.service;

import com.microblog.entity.DiscussPost;

import java.util.List;

public interface SearchService {
    //根据标题关键字搜索
    List<DiscussPost> searchDiscussPost(String keyword, int current, int limit);

    //根据关键字搜索总条数
    int searchDiscussPostCount(String keyword);
}
