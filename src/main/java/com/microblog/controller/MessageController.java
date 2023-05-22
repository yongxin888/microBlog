package com.microblog.controller;

import com.alibaba.fastjson.JSONObject;
import com.microblog.entity.Message;
import com.microblog.entity.Page;
import com.microblog.entity.User;
import com.microblog.service.LikeService;
import com.microblog.service.MessageService;
import com.microblog.service.UserService;
import com.microblog.util.CommunityConstant;
import com.microblog.util.CommunityUtil;
import com.microblog.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 私信、系统通知
 * @DATE: 2023/4/23 13:42
 */
@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    /**
     * 私信列表
     * @param model
     * @param page 分页类
     * @return
     */
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page) {
        //获取当前登录的用户信息
        User user = hostHolder.getUser();

        //分页信息
        page.setPath("/letter/list");
        page.setLimit(5);
        page.setRows(messageService.findConversationCount(user.getId()));

        //私信列表
        List<Message> conversationList = messageService.selectConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();

        //封装私信状态信息
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                //私信
                map.put("conversation", message);
                //私信数量
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                //未读私信数量
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                //私信对方
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.selectById(targetId));
                //添加私信状态信息到集合
                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 查询当前用户的所有未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        //查询未读系统消息数量
        int noticeUnreadCount = messageService.findNoticeUnReadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "site/letter";
    }

    /**
     * 通知列表（只显示最新一条消息）
     * @param model
     * @return
     */
    @GetMapping("/notice/list")
    public String getNoticeList(Model model) {
        //获取当前登录用户
        User user = hostHolder.getUser();

        //查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMNET);
        
        //封装通知需要的各种数据
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            //评论通知
            messageVO.put("message", message);
            //获取评论通知内容
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //将String类型的JSON字符串转换成Map集合
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            //获取发送方用户信息
            messageVO.put("user", userService.selectById((Integer) data.get("userId")));
            //获取实体类型（这里是评论类型）
            messageVO.put("entityType", data.get("entityType"));
            //获取实体类型ID
            messageVO.put("entityId", data.get("entityId"));
            //获取评论的帖子ID
            messageVO.put("postId", data.get("postId"));
            //查询该用户评论的系统通知数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMNET);
            messageVO.put("count", count);
            //查询该用户未读的系统通知数量
            int unread = messageService.findNoticeUnReadCount(user.getId(), TOPIC_COMMNET);
            messageVO.put("unread", unread);
            //添加评论系统通知
            model.addAttribute("commentNotice", messageVO);
        }

        //查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            //点赞通知
            messageVO.put("message", message);
            //获取点赞通知内容
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //将String类型的JSON字符串转换成Map集合
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            //获取点赞方用户信息
            messageVO.put("user", userService.selectById((Integer) data.get("userId")));
            //获取实体类型（这里是点赞类型）
            messageVO.put("entityType", data.get("entityType"));
            //获取实体类型ID
            messageVO.put("entityId", data.get("entityId"));
            //获取点赞的帖子ID
            messageVO.put("postId", data.get("postId"));
            //查询该用户点赞的系统通知数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);
            //查询该用户未读的系统通知数量
            int unread = messageService.findNoticeUnReadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);
            //添加点赞系统通知
            model.addAttribute("likeNotice", messageVO);
        }

        // 查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            //关注通知
            messageVO.put("message", message);
            //获取关注通知内容
            String content = HtmlUtils.htmlUnescape(message.getContent());
            //将String类型的JSON字符串转换成Map集合
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            //获取关注方用户信息
            messageVO.put("user", userService.selectById((Integer) data.get("userId")));
            //获取实体类型（这里是关注类型）
            messageVO.put("entityType", data.get("entityType"));
            //实体类型ID
            messageVO.put("entityId", data.get("entityId"));
            //查询该用户关注的系统通知数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);
            //查询该用户未读的系统通知数量
            int unread = messageService.findNoticeUnReadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);
            //添加关注系统通知
            model.addAttribute("followNotice", messageVO);
        }

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        //查询未读系统消息数量
        int noticeUnreadCount = messageService.findNoticeUnReadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "site/notice";
    }

    /**
     * 查询某个主题所包含的通知列表
     * @param topic 系统通知类型
     * @param page 分页
     * @param model 模型
     * @return
     */
    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        //获取当前用户
        User user = hostHolder.getUser();
        //设置分页信息
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));
        //获取某个主题包含的通知列表
        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        //封装通知列表所需的信息
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            //遍历通知列表
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                //通知
                map.put("notice", notice);
                //内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                //将String类型的JSON字符串转换成Map集合
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                //获取用户信息
                map.put("user", userService.selectById((Integer) data.get("userId")));
                //获取实体类型
                map.put("entityType", data.get("entityType"));
                //获取实体ID
                map.put("entityId", data.get("entityId"));
                //获取帖子ID
                map.put("postId", data.get("postId"));
                //发送系统通知的作者
                map.put("fromUser", userService.selectById(notice.getFromId()));
                //添加通知列表信息
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);
        //设置状态为已读
        List<Integer> ids = getUnreadLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "site/notice-detail";
    }

    /**
     * 获取当前登录用户未读私信的 id
     * @param letterList
     * @return
     */
    private List<Integer> getUnreadLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                // 当前用户是私信的接收者且该私信处于未读状态
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    /**
     * 发送私信
     * @param toName 收信人 username
     * @param content 内容
     * @return
     */
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String toName, String content) {
        //查询收信人是否存在
        User target = userService.findUserByName(toName);

        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在");
        }

        //封装私信表
        Message message = new Message();
        //接收方ID
        message.setToId(target.getId());
        //发送方ID
        message.setFromId(hostHolder.getUser().getId());
        //标识两个用户之间的对话
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }
        else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        //私信内容
        message.setContent(content);
        //状态 默认就是 0 未读
        message.setStatus(0);
        //发送时间
        message.setCreateTime(new Date());

        //新增私信
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 私信详情页
     * @param conversationId 用户之间的对话标识
     * @param page 分页
     * @param model 模型
     * @return
     */
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        //分页信息
        page.setLimit(5);
        //分页链接
        page.setPath("/letter/detail/" + conversationId);
        //某个会话的私信数量
        page.setRows(messageService.findLetterCount(conversationId));
        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        //封装私信信息
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                //私信
                map.put("letter", message);
                //发送方
                map.put("fromUser", userService.selectById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        //私信对方目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 将私信列表中的未读消息改为已读
        List<Integer> ids = getUnreadLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "site/letter-detail";
    }

    /**
     * 获取私信对方对象
     * @param conversationId 用户私信标识
     * @return
     */
    private User getLetterTarget(String conversationId) {
        //将标识拆开
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.selectById(id1);
        }
        else {
            return userService.selectById(id0);
        }
    }
}
