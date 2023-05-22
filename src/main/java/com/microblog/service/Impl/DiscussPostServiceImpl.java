package com.microblog.service.Impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.microblog.entity.DiscussPost;
import com.microblog.mapper.DiscussPostMapper;
import com.microblog.service.DiscussPostService;
import com.microblog.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION:  帖子表
 * @DATE: 2023/4/18 15:33
 */
@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    //最大个数限制
    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    //读完最后一条后开始计时过期时间
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //热贴列表的本地缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    //帖子总数的本地缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     *  初始化本地缓存 Caffeine
     */
    @PostConstruct  //@PostConstruct是java5的时候引入的注解，指的是在项目启动的时候执行这个方法
    public void init() {
        //初始化热帖列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
            //同步加载数据
            @Override
            public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                if (key == null || key.length() == 0) {
                    throw new IllegalArgumentException("参数错误");
                }

                //key offset(每页的起始索引):limit(每页显示多少条数据)
                String[] params = key.split(":");
                if (params == null || params.length != 2) {
                    throw new IllegalArgumentException("参数错误");
                }

                //每页的起始索引
                int offset = Integer.valueOf(params[0]);
                //每页显示多少条数据
                int limit = Integer.valueOf(params[1]);

                //还可以访问二级缓存Redis

                //如果二级缓存没有，查询数据库
                return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
            }
        });

        //初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder().maximumSize(maxSize).expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build(new CacheLoader<Integer, Integer>() {
            @Override
            public @Nullable Integer load(@NonNull Integer key) throws Exception {
                return discussPostMapper.selectDiscussPostRows(key);
            }
        });
    }

    /**
     * 查询帖子总条数
     * @param userId    当传入的 userId = 0 时查找所有用户的帖子
     *                  传入的 userId != 0 时，查找该指定用户的帖子
     * @return 返回总条数
     */
    @Override
    public Integer findDiscussPostRows(int userId) {
        //当查询的是所有用户的帖子总数时，走本地缓存
        if (userId == 0) {
            return postRowsCache.get(userId);
        }
        //查询数据库
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 分页查询帖子信息 过滤拉黑的帖子
     * @param userId    当传入的 userId = 0 时查找所有用户的帖子
     *                  当传入的 userId != 0 时，查找该指定用户的帖子
     * @param offset    每页的起始索引
     * @param limit     每页显示多少条数据
     * @param orderMode 排行模式(若传入 1, 则按照热度来排序, 若传入 0, 则按照最新发布来排序)
     * @return  返回帖子数据
     */
    @Override
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    /**
     * 根据id查询帖子
     * @param id 帖子ID
     * @return 帖子详情数据
     */
    @Override
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    /**
     * 修改帖子的评论数量
     * @param id 帖子 id
     * @param commentCount 评论数量
     * @return
     */
    @Override
    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    /**
     * 添加帖子
     * @param discussPost 帖子信息
     * @return
     */
    @Override
    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 转义 HTML 标记，防止在 HTML 标签中注入攻击语句
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    /**
     * 修改帖子类型：0-普通; 1-置顶;
     * @param id 帖子ID
     * @param type 类型
     * @return
     */
    @Override
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    /**
     * 修改帖子状态：0-正常; 1-精华; 2-拉黑;
     * @param id 帖子ID
     * @param status 状态信息
     * @return
     */
    @Override
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    /**
     * 修改帖子分数
     * @param id 帖子id
     * @param score 分数
     * @return
     */
    @Override
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
