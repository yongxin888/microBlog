package com.microblog.service;

/**
 * 关注相关
 */
public interface FollowService {
    //查询某个用户关注的实体的数量
    long findFolloweeCount(int userId, int entityType);

    //查询某个实体的粉丝数量
    long findFollowerCount(int entityType, int entityId);

    //判断当前用户是否已关注该实体
    boolean hasFollowed(int userId, int entityType, int entityId);
}
