package com.microblog.entity;

import lombok.Data;

import java.util.Date;

/**
 * 评论表
 */
@Data
public class Comment {
    //主键ID
    private int id;
    // 发布评论的作者
    private int userId;
    // 评论目标的类型（帖子、评论）
    private int entityType;
    // 评论目标的 id
    private int entityId;
    // 指明对哪个用户进行评论(用户 id)
    private int targetId;
    // 内容
    private String content;
    // 状态：0 正常，1 禁用
    private int status;
    // 发布时间
    private Date createTime;
}
