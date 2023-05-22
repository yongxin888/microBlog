package com.microblog.mapper;

import com.microblog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    //根据评论类别、id对评论进行分页查询
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    //查询评论的数量
    int selectCountByEntity(int entityType, int entityId);

    //添加评论
    int insertComment(Comment comment);

    //根据ID查询评论
    Comment selectCommentById(int id);

    //查询某个用户的评论/回复数量
    int selectCommentCountByUserId(int userId);

    //分页查询某个用户的评论/回复列表
    List<Comment> selectCommentByUserId(int userId, int offset, int limit);
}
