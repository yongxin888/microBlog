package com.microblog.service.Impl;

import com.microblog.service.FollowService;
import com.microblog.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 关注相关
 * @DATE: 2023/4/30 16:52
 */
@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询某个用户关注的实体的数量
     * @param userId 用户 id
     * @param entityType 实体类型
     * @return
     */
    @Override
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        //返回集合大小
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 查询某个实体的粉丝数量
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @return
     */
    @Override
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        //返回集合大小
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 判断当前用户是否已关注该实体
     * @param userId 用户ID
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @return
     */
    @Override
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        //获取key对应集合中entityId元素的值 检查是否存在
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null ;
    }
}
