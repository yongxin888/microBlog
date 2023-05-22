package com.microblog.controller;

import com.microblog.entity.Event;
import com.microblog.entity.User;
import com.microblog.event.EventProducer;
import com.microblog.service.LikeService;
import com.microblog.util.CommunityConstant;
import com.microblog.util.CommunityUtil;
import com.microblog.util.HostHolder;
import com.microblog.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 点赞相关
 * @DATE: 2023/4/22 15:39
 */
@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param entityUserId 赞的帖子/评论的作者id
     * @param postId 帖子的id (点赞了哪个帖子，点赞的评论属于哪个帖子，点赞的回复属于哪个帖子)
     * @return
     */
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();
        //点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        //触发点赞事件（系统通知） - 取消点赞不通知
        if (likeStatus == 1) {
            Event event = new Event();
            event.setTopic(TOPIC_LIKE); //事件类型,点赞
            event.setUserId(hostHolder.getUser().getId()); //事件由谁触发
            event.setEntityUserId(entityUserId); //实体的作者(该通知发送给他）
            event.setEntityId(entityId); //实体 id
            event.setEntityType(entityType); //实体类型
            event.setData("postId", postId); //帖子的id
            //处理事件（kafka）
            eventProducer.fireEvent(event);
        }

        if (entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}
