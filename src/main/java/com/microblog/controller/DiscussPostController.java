package com.microblog.controller;

import com.microblog.entity.*;
import com.microblog.event.EventProducer;
import com.microblog.service.CommentService;
import com.microblog.service.DiscussPostService;
import com.microblog.service.LikeService;
import com.microblog.service.UserService;
import com.microblog.util.CommunityConstant;
import com.microblog.util.CommunityUtil;
import com.microblog.util.HostHolder;
import com.microblog.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.validation.constraints.NotEmpty;
import java.util.*;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 帖子详情
 * @DATE: 2023/4/21 18:43
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 进入帖子发布页
     * @return
     */
    @GetMapping("/publish")
    public String getPublishPage () {
        return "site/discuss-publish";
    }

    /**
     * 进入帖子详情页
     * @param discussPostId 帖子ID
     * @param model
     * @return 详情页
     */
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        //查询帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        String content = HtmlUtils.htmlUnescape(discussPost.getContent());  //内容反转义，不然 markDown 格式无法显示
        discussPost.setContent(content);
        model.addAttribute("post", discussPost);

        //查询帖子的作者
        User user = userService.selectById(discussPost.getUserId());
        model.addAttribute("user", user);

        //查询帖子的点赞数量
        Long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);

        //当前登录用户的点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        //设置评论分页信息
        page.setRows(discussPost.getCommentCount());
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);

        //帖子评论列表
        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_TYPE_POST,
                discussPost.getId(), page.getOffset(), page.getLimit());

        //封装评论及其相关信息
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            //遍历帖子的所有评论信息
            for (Comment comment : commentList) {
                //存储对帖子的评论
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment", comment);  //评论
                commentVo.put("user", userService.selectById(comment.getUserId())); //发布评论的作者
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());   //该评论点赞数量
                commentVo.put("likeCount", likeCount);  //评论点赞数量
                likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);    //当前登录用户对该评论的点赞状态

                //每个评论对应的回复
                List<Comment> replyList = commentService.findCommentByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //封装对评论的评论和评论的作者信息
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    //遍历评论对应的回复内容
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply); //回复
                        User u = userService.selectById(reply.getUserId()); //发布该回复的作者
                        replyVo.put("user", u); //回复内容的作者
                        User target = reply.getTargetId() == 0 ? null : userService.selectById(reply.getTargetId());
                        replyVo.put("target", target); //该回复的目标用户
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount); //该回复的点赞数量
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus); // 当前登录用户的点赞状态
                        replyVoList.add(replyVo);
                    }
                }
                //该评论的回复评论
                commentVo.put("replys", replyVoList);

                //每个评论对应的回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                //添加帖子的评论相关信息
                commentVoList.add(commentVo);
            }
        }
        //将评论及其相关信息返回给前端
        model.addAttribute("comments", commentVoList);

        return "site/discuss-detail";
    }

    /**
     * 添加帖子（发帖）
     * @param title 文章标题
     * @param content 文章内容
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(@NotEmpty(message = "文章标题不能为空") String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还未登录");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(0);
        discussPost.setType(0);
        discussPost.setStatus(0);

        discussPostService.addDiscussPost(discussPost);

        // 触发发帖事件，通过消息队列将其存入 Elasticsearch 服务器
        Event event = new Event();
        event.setEntityType(ENTITY_TYPE_POST); //帖子
        event.setTopic(TOPIC_PUBLISH);  //发帖
        event.setUserId(user.getId());
        event.setEntityId(discussPost.getId());

        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());

        return CommunityUtil.getJSONString(0, "发布成功");
    }

    /**
     * 置顶帖子
     * @param id
     * @return
     */
    @PostMapping("/top")
    @ResponseBody
    public String top(int id, int type) {
        discussPostService.updateType(id, type);

        //触发发帖事件，通过消息队列将其存入 Elasticsearch 服务器
//        Event event = new Event();
//        event.setUserId(hostHolder.getUser().getId());
//        event.setEntityId(id);
//        event.setEntityType(ENTITY_TYPE_POST);
//        event.setTopic(TOPIC_PUBLISH);
//        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 加精帖子
     * @param id 帖子ID
     * @return
     */
    @PostMapping("/wonderful")
    @ResponseBody
    public String wonderful(int id) {
        discussPostService.updateStatus(id, 1);

        //触发发帖事件，通过消息队列将其存入 Elasticsearch 服务器
//        Event event = new Event();
//        event.setUserId(hostHolder.getUser().getId());
//        event.setEntityId(id);
//        event.setEntityType(ENTITY_TYPE_POST);
//        event.setTopic(TOPIC_PUBLISH);
//        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 删除帖子
     * @param id 帖子ID
     * @return
     */
    @PostMapping("/delete")
    @ResponseBody
    public String delete(int id) {
        discussPostService.updateStatus(id, 2);

        // 触发删帖事件，通过消息队列更新 Elasticsearch 服务器
//        Event event = new Event();
//        event.setUserId(hostHolder.getUser().getId());
//        event.setEntityId(id);
//        event.setEntityType(ENTITY_TYPE_POST);
//        event.setTopic(TOPIC_DELETE);
//        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }
}
