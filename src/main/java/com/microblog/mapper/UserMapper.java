package com.microblog.mapper;

import com.microblog.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    //根据ID查询用户
    User selectById (int id);

    //插入用户
    int insertUser(User user);

    //根据用户名查询用户
    User selectByName(String username);

    //根据邮箱查询用户
    User selectByEmail(String email);

    //修改密码
    int updatePassword(int id, String password);

}
