package com.microblog.service;

import com.microblog.entity.Comment;

import java.util.List;

public interface CommentService {
    //根据评论类别、ID对评论进行分页查询
    List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit);

    //查询评论的数量
    int findCommentCount(int entityType, int entityId);

    //添加评论（需要事务管理）
    int addComment(Comment comment);

    //根据ID查询评论
    Comment findCommentById(int id);

    //查询某个用户的评论/回复数量
    int findCommentCountByUserId(int userId);

    //分页查询某个用户的评论/回复列表
    List<Comment> findCommentByUserId(int userId, int offset, int limit);
}
