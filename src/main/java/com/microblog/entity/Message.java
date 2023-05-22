package com.microblog.entity;

import lombok.Data;

import java.util.Date;

/**
 * 私信表
 */
@Data
public class Message {
    //私信/系统通知的唯一标识
    private int id;
    //私信/系统通知的发送方 id
    private int fromId;
    //私信/系统通知的接收方 id
    private int toId;
    //标识两个用户之间的对话。
    private String conversationId;
    //私信/系统通知的内容
    private String content;
    //私信/系统通知的状态
    private int status;
    //私信/系统通知的发送时间
    private Date createTime;
}
