package com.microblog.service.Impl;

import com.microblog.service.LikeService;
import com.microblog.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 用户点赞相关
 * @DATE: 2023/4/20 17:17
 */
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询某实体被点赞的数量
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @return 指定key对应值的长度
     */
    @Override
    public Long findEntityLikeCount(int entityType, int entityId) {
        //获取Key值
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //返回指定key对应值的长度
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询某个用户对某个实体的点赞状态（是否已赞）
     * @param userId 用户ID
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @return 1:已赞，0:未赞
     */
    @Override
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //判断key中是否存在value值 如果存在，则返回1，否则返回0
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    /**
     * 点赞
     * @param userId 点赞的用户 id
     * @param entityType 点赞的实体类型
     * @param entityId 被点赞的实体id
     * @param entityUserId 被赞的帖子/评论的作者 id
     */
    @Override
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        //执行 Redis 会话
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //某个实体（帖子、评论/回复）的获赞
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                //某个用户的获赞数量
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //判断用户是否点赞过
                boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey, userId);
                //开启事务
                redisOperations.multi();

                if (isMember) {
                    //如果用户已经点过赞，点第二次则取消赞
                    redisOperations.opsForSet().remove(entityLikeKey, userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                }else {
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }

                //提交事务
                return redisOperations.exec();
            }
        });
    }

    /**
     * 查询某个用户获得赞数量
     * @param userId 用户ID
     * @return
     */
    @Override
    public int findUserLikeCount(int userId) {
        //获取用户的获赞数量的键
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }
}
