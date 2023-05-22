package com.microblog.service.Impl;

import com.microblog.entity.Comment;
import com.microblog.mapper.CommentMapper;
import com.microblog.service.CommentService;
import com.microblog.service.DiscussPostService;
import com.microblog.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

import static com.microblog.util.CommunityConstant.ENTITY_TYPE_POST;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 评论相关
 * @DATE: 2023/4/22 13:51
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 根据评论目标（类别、id）对评论进行分页查询
     * @param entityType 实体类型
     * @param entityId 实体类型ID
     * @param offset 每页的起始索引
     * @param limit 每页显示条数
     * @return 评论集合
     */
    @Override
    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
    }

    /**
     * 查询评论的数量
     * @param entityType 实体类型
     * @param entityId 实体类型ID
     * @return 评论数量
     */
    @Override
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * 添加评论（需要事务管理）
     * @param comment 评论信息
     * @return
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // Html 标签转义
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        // 敏感词过滤
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        //添加评论
        int rows = commentMapper.insertComment(comment);

        //更新帖子的评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }

    /**
     * 根据 id 查询评论
     * @param id
     * @return
     */
    @Override
    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

    /**
     * 查询某个用户的评论/回复数量
     * @param userId 用户ID
     * @return
     */
    @Override
    public int findCommentCountByUserId(int userId) {
        return commentMapper.selectCommentCountByUserId(userId);
    }

    /**
     * 分页查询某个用户的评论/回复列表
     * @param userId 用户id
     * @param offset 分页起始索引
     * @param limit 每页显示条数
     * @return
     */
    @Override
    public List<Comment> findCommentByUserId(int userId, int offset, int limit) {
        return commentMapper.selectCommentByUserId(userId, offset, limit);
    }
}
