package com.microblog.mapper;

import com.microblog.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子表
 */
@Mapper
public interface DiscussPostMapper {
    //分页查询帖子信息 过滤拉黑的帖子
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    //查询帖子总条数
    Integer selectDiscussPostRows(int userId);

    //根据ID查询帖子
    DiscussPost selectDiscussPostById(int id);

    //修改评论数量
    int updateCommentCount(int id, int commentCount);

    //插入/添加帖子
    int insertDiscussPost(DiscussPost discussPost);

    //修改帖子类型：0-普通; 1-置顶
    int updateType(int id, int type);

    //修改帖子状态 0-正常; 1-精华; 2-拉黑
    int updateStatus(int id, int status);

    //修改帖子分数
    int updateScore(int id, double score);
}
