package com.microblog.service;

/**
 * 点赞相关
 */
public interface LikeService {
    //查询某实体被点赞的数量
    Long findEntityLikeCount(int entityType, int entityId);

    //查询某个用户对某个实体的点赞状态（是否已赞）
    int findEntityLikeStatus(int userId, int entityType, int entityId);

    //点赞
    void like(int userId, int entityType, int entityId, int entityUserId);

    //查询某个用户获得赞数量
    int findUserLikeCount(int userId);
}
