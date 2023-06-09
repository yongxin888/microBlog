package com.microblog.service;

import com.microblog.entity.Message;

import java.util.List;

public interface MessageService {
    //查询当前用户的会话数量
    int findConversationCount(int userId);

    //查询当前用户的会话列表，针对每个会话只返回一条最新的私信
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询某个会话所包含的私信数量
    int findLetterCount(String conversationId);

    //查询未读私信的数量
    int findLetterUnreadCount(int userId, String conversationId);

    //查询未读的系统通知数量
    int findNoticeUnReadCount(int userId, String topic);

    //查询某个主题下最新的系统通知
    Message findLatestNotice(int userId, String topic);

    //查询某个主题下包含的系统通知数量
    int findNoticeCount(int userId, String topic);

    //查询某个主题所包含的通知列表
    List<Message> findNotices(int userId, String topic, int offset, int limit);

    //读取私信(将私信状态设置为已读)
    int readMessage(List<Integer> ids);

    //添加一条私信
    int addMessage(Message message);

    //查询某个会话所包含的私信列表
    List<Message> findLetters(String conversationId, int offset, int limit);
}
