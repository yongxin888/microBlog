package com.microblog.mapper;

import com.microblog.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SearchMapper {
    //分页搜索
    List<DiscussPost> queryDiscussPost(String keyword, int current, int limit);

    //根据条件查询的帖子总数
    int queryDiscussPostCount(String keyword);
}
