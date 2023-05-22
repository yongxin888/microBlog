package com.microblog.controller;

import com.microblog.entity.Comment;
import com.microblog.entity.DiscussPost;
import com.microblog.entity.Page;
import com.microblog.entity.User;
import com.microblog.service.*;
import com.microblog.util.CommunityConstant;
import com.microblog.util.CommunityUtil;
import com.microblog.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 用户
 * @DATE: 2023/4/30 15:05
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 进入个人主页
     * @param userId 用户ID
     * @param model
     * @return
     */
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.selectById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        //用户
        model.addAttribute("user", user);
        //查询用户获赞数量
        int userLikeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("userLikeCount", userLikeCount);
        //查询用户关注的数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 当前登录用户是否已关注该用户
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        model.addAttribute("tab", "profile"); // 该字段用于指示标签栏高亮

        return "site/profile";
    }
    /**
     * 进入我的帖子（查询某个用户的帖子列表）
     * @param userId 用户ID
     * @param page 分页信息
     * @param model
     * @return
     */
    @GetMapping("/discuss/{userId}")
    public String getMyDiscussPosts(@PathVariable("userId") int userId, Page page, Model model) {
        //判断用户是否存在
        User user = userService.selectById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        //该用户的帖子总数
        int rows = discussPostService.findDiscussPostRows(userId);
        model.addAttribute("rows", rows);

        //设置分页信息
        page.setLimit(5);
        page.setPath("/user/discuss/" + userId);
        page.setRows(rows);

        // 分页查询(按照最新查询)
        List<DiscussPost> list = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), 0);

        //封装帖子和该帖子对应的用户信息
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                //查询该帖子被点赞的数量
                Long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("tab", "mypost"); // 该字段用于指示标签栏高亮

        return "site/my-post";
    }

    /**
     * 进入我的评论/回复（查询某个用户的评论/回复列表）
     * @param userId 用户ID
     * @param page 分页信息
     * @param model
     * @return
     */
    @GetMapping("/comment/{userId}")
    public String getMyComments(@PathVariable("userId") int userId, Page page, Model model) {
        //查询用户是否存在
        User user = userService.selectById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        // 该用户的评论/回复总数
        int commentCounts = commentService.findCommentCountByUserId(userId);
        model.addAttribute("commentCounts", commentCounts);

        //设置分页信息
        page.setLimit(5);
        page.setPath("/user/comment/" + userId);
        page.setRows(commentCounts);

        //分页查询评论/回复列表
        List<Comment> list = commentService.findCommentByUserId(userId, page.getOffset(), page.getLimit());

        //封装评论和该评论对应的帖子信息
        List<Map<String, Object>> comments = new ArrayList<>();
        if (list != null) {
            for (Comment comment : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                //显示评论/回复对应的文章信息
                if (comment.getEntityType() == ENTITY_TYPE_POST) {
                    //如果是对帖子的评论，直接查询target_id
                    DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
                    map.put("post", post);
                }else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
                    //如过是对评论的回复，则先根据该回复的 target_id 查询评论的 id, 再根据该评论的 target_id 查询帖子的 id
                    Comment targetComment = commentService.findCommentById(comment.getEntityId());
                    DiscussPost post = discussPostService.findDiscussPostById(targetComment.getEntityId());
                    map.put("post", post);
                }
                comments.add(map);
            }
        }
        model.addAttribute("comments", comments);
        model.addAttribute("tab", "myreply"); // 该字段用于指示标签栏高亮

        return "site/my-reply";
    }

    /**
     * 跳转至账号设置界面
     * @return
     */
    @GetMapping("/setting")
    public String getSettingPage(Model model) {
        // 生成上传文件的名称
        String fileName = CommunityUtil.generateUUID();
        model.addAttribute("fileName", fileName);

        return "site/setting";
    }

    /**
     * 修改用户密码
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @param model
     * @return
     */
    @PostMapping("/password")
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        //验证原密码是否正确
        User user = hostHolder.getUser();
        String md5OldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(md5OldPassword)) {
            model.addAttribute("oldPasswordError", "原密码错误");
            return "site/setting";
        }

        //判断新密码是否合法
        String md5NewPassword = CommunityUtil.md5(newPassword + user.getSalt());
        if (user.getPassword().equals(md5NewPassword)) {
            model.addAttribute("newPasswordError", "新密码和原密码相同");
            return "site/setting";
        }
        // 修改用户密码
        userService.updatePassword(user.getId(), newPassword);

        return "redirect:/index";
    }
}
