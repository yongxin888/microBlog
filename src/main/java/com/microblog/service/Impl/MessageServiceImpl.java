package com.microblog.service.Impl;

import com.microblog.entity.Message;
import com.microblog.mapper.MessageMapper;
import com.microblog.service.MessageService;
import com.microblog.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 私信、系统通知相关
 * @DATE: 2023/4/23 13:44
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 查询当前用户的会话数量
     * @param userId 用户id
     * @return 会话数量
     */
    @Override
    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    /**
     * 查询当前用户的会话列表，针对每个会话只返回一条最新的私信
     * @param userId 用户 id
     * @param offset 每页的起始索引
     * @param limit 每页显示多少条数据
     * @return 会话列表
     */
    @Override
    public List<Message> selectConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    /**
     * 查询某个会话所包含的私信数量
     * @param conversationId 标识两个用户之间的对话
     * @return
     */
    @Override
    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    /**
     * 查询未读私信的数量
     * @param userId 用户ID
     * @param conversationId conversationId = null, 则查询该用户所有会话的未读私信数量
     *                        conversationId != null, 则查询该用户某个会话的未读私信数量
     * @return 未读私信数量
     */
    @Override
    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    /**
     * 查询未读的系统通知数量
     * @param userId 用户id
     * @param topic 系统通知类型
     * @return 通知数量
     */
    @Override
    public int findNoticeUnReadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnReadCount(userId, topic);
    }

    /**
     * 查询某个主题下最新的系统通知
     * @param userId 用户ID
     * @param topic 系统通知类型
     * @return 最新的系统通知
     */
    @Override
    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    /**
     * 查询某个主题下包含的系统通知数量
     * @param userId 用户ID
     * @param topic 系统通知类型
     * @return 通知数量
     */
    @Override
    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    /**
     * 查询某个主题所包含的通知列表
     * @param userId 用户ID
     * @param topic 系统通知类型
     * @param offset 分页起始索引
     * @param limit 每页显示条数
     * @return 通知列表
     */
    @Override
    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }

    /**
     * 读取私信(将私信状态设置为已读)
     * @param ids 私信ID
     * @return
     */
    @Override
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }

    /**
     * 添加一条私信
     * @param message 私信内容
     * @return
     */
    @Override
    public int addMessage(Message message) {
        //转义 HTML 标签
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        //过滤敏感词
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    /**
     * 查询某个会话所包含的私信列表
     * @param conversationId 用户之间的会话标识
     * @param offset 起始索引
     * @param limit 每页显示条数
     * @return 私信列表信息
     */
    @Override
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }
}
