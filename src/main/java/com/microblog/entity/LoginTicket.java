package com.microblog.entity;

import lombok.Data;

import java.util.Date;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 登录凭证
 * @DATE: 2023/4/21 15:54
 */
@Data
public class LoginTicket {
    private int id;
    private int userId; //用户ID
    private String ticket; // 凭证
    private int status; // 状态（是否有效）
    private Date expired; // 过期时间
}
