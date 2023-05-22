package com.microblog.service;

import com.microblog.entity.LoginTicket;
import com.microblog.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

public interface UserService {
    //根据ID查询用户信息
    User selectById(int id);

    //插入用户
    Map<String, Object> insertUser(User user);

    //查询用户是否存在
    User findUserByName(String username);

    //用户登录
    Map<String, Object> login(String username, String password, Integer expiredSeconds);

    //根据 ticket 查询 LoginTicket 信息
    LoginTicket findLoginTicket(String ticket);

    //修改用户密码（对新密码加盐加密存入数据库）
    int updatePassword(int userId, String newPassword);

    //获取某个用户的权限
    Collection<? extends GrantedAuthority> getAuthorities(int userId);
}
