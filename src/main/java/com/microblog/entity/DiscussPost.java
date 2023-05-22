package com.microblog.entity;

import lombok.Data;

import java.util.Date;

/**
 * 帖子表
 */
@Data
public class DiscussPost {
    //帖子的唯一标识
    private Integer id;
    //发表该帖子的用户的 id
    private Integer userId;
    //帖子标题
    private String title;
    //帖子内容
    private String content;
    //帖子类型
    private Integer type;
    //帖子状态
    private Integer status;
    //帖子发表时间
    private Date createTime;
    //帖子的评论数量
    private Integer commentCount;
    //热度 / 分数
    private Double score;
}
