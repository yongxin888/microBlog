package com.microblog.controller;

import com.microblog.entity.DiscussPost;
import com.microblog.entity.Page;
import com.microblog.entity.User;
import com.microblog.service.DiscussPostService;
import com.microblog.service.LikeService;
import com.microblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microblog.util.CommunityConstant.ENTITY_TYPE_POST;

/**
 * 首页
 */
@Controller
public class IndexController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping("/")
    public String root() {
        return "forward:/index";
    }

    /**
     *  进入首页
     * @param model
     * @param page  封装的分页相关信息
     * @param orderMode 筛选条件 0 最新 1 最热 默认是0
     * @return
     */
    @GetMapping("/index")
    public String getIndexPage(Model model, Page page, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        //获取总页数并设置查询路径
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode?" + orderMode);

        //分页查询帖子
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);

        //封装帖子以及帖子的用户信息
        List<Map<String, Object>> discussPosts = new ArrayList<>();

        if (list != null) {
            //遍历帖子集合
            for (DiscussPost post: list) {
                Map<String, Object> map = new HashMap<>();
                //帖子数据
                map.put("post", post);
                //用户数据
                User user = userService.selectById(post.getUserId());
                map.put("user", user);
                //点赞数据
                Long entityLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", entityLikeCount);
                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);
        return "index";
    }

    /**
     * 进入 500 错误界面
     * @return
     */
    @GetMapping("/error")
    public String getErrorPage() {
        return "error/500";
    }

    /**
     * 没有权限访问时的错误界面（也是 404）
     * @return
     */
    @GetMapping("/denied")
    public String getDeniedPage() {
        return "error/404";
    }
}
