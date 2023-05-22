package com.microblog.controller;

import com.microblog.entity.Comment;
import com.microblog.entity.DiscussPost;
import com.microblog.entity.Event;
import com.microblog.event.EventProducer;
import com.microblog.service.CommentService;
import com.microblog.service.DiscussPostService;
import com.microblog.util.CommunityConstant;
import com.microblog.util.HostHolder;
import com.microblog.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 评论、回复
 * @DATE: 2023/4/28 15:44
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加评论
     * @param discussPostId 被评论的id
     * @param comment 评论内容
     * @return
     */
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        //回复状态
        comment.setStatus(0);
        //发表评论的用户
        comment.setUserId(hostHolder.getUser().getId());
        //设置时间
        comment.setCreateTime(new Date());
        //添加评论
        commentService.addComment(comment);

        //触发评论事件，发送系统通知
        Event event = new Event();
        event.setTopic(TOPIC_COMMNET);
        event.setEntityType(comment.getEntityType());
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityId(comment.getEntityId());
        event.setData("postId", discussPostId);

        //设置实体的作者
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost discussPostById = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(discussPostById.getUserId());
        }else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        //评论的帖子接收方用户
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            //触发发帖事件，通过消息队列将其存入 Elasticsearch 服务器
            event = new Event();
            event.setEntityId(discussPostId);
            event.setTopic(TOPIC_PUBLISH);
            event.setUserId(comment.getUserId());
            event.setEntityType(ENTITY_TYPE_POST);
            eventProducer.fireEvent(event);

            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, discussPostId);
        }

        return "redirect:/discuss/detail/" + discussPostId;
    }

}
