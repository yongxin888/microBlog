package com.microblog.service;

import com.microblog.entity.DiscussPost;

import java.util.List;

public interface DiscussPostService {
    //查询讨论贴的个数
    Integer findDiscussPostRows(int userId);

    //分页查询讨论贴信息
    List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode);

    //根据ID查询帖子
    DiscussPost findDiscussPostById(int id);

    //修改帖子的评论数量
    int updateCommentCount(int id, int commentCount);

    //添加帖子
    int addDiscussPost(DiscussPost discussPost);

    //修改帖子类型：0-普通; 1-置顶;
    int updateType(int id, int type);

    //修改帖子状态
    int updateStatus(int id, int status);

    //修改帖子分数
    int updateScore(int id, double score);
}
