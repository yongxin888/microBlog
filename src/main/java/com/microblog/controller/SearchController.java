package com.microblog.controller;

import com.microblog.entity.DiscussPost;
import com.microblog.entity.Page;
import com.microblog.service.LikeService;
import com.microblog.service.SearchService;
import com.microblog.service.UserService;
import com.microblog.util.CommunityConstant;
import com.microblog.util.MarkdownUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 搜索帖子
 * @DATE: 2023/4/21 17:25
 */
@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private SearchService searchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * 搜索
     * search?keword=xxx
     * @param keyword 关键词
     * @param page
     * @param model
     * @return
     */
    @GetMapping("/search")
    public String search(String keyword, Page page, Model model) {
        List<DiscussPost> list = searchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost discussPost : list) {
                Map<String, Object> map = new HashMap<>();
                String content = HtmlUtils.htmlUnescape(discussPost.getContent());  //内容反转义，不然 markDown 格式无法显示
                //html转markDown
                String toHtml = MarkdownUtils.markdownToHtml(content);
                discussPost.setContent(toHtml.substring(0,50) + "...");
                // 帖子
                map.put("post", discussPost);
                // 作者
                map.put("user", userService.selectById(discussPost.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        //总条数
        int count = searchService.searchDiscussPostCount(keyword);

        // 设置分页
        page.setPath("/search?keyword="+ keyword);
        page.setRows(count);

        return "site/search";
    }
}
