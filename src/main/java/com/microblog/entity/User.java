package com.microblog.entity;

import lombok.Data;

import java.util.Date;

/**
 * 用户表
 */
@Data
public class User {
    //用户的唯一标识
    private int id;
    //用户名
    private String username;
    //存储加盐加密后的密码
    private String password;
    //随机生成的盐，用于密码的加盐加密
    private String salt;
    //邮箱
    private String email;
    //用户类型
    private Integer type;
    //用户状态
    private Integer status;
    //激活码
    private String activationCode;
    //用户头像
    private String headerUrl;
    //创建时间
    private Date createTime;
}
