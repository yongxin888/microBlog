package com.microblog.util;

/**
 * 常量
 */
public interface CommunityConstant {
    // 实体类型：帖子
    int ENTITY_TYPE_POST = 1;

    // 默认的登录凭证超时时间 (12小时)
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    // 记住我状态下的凭证超时时间 (30天)
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 30;

    // 实体类型：评论
    int ENTITY_TYPE_COMMENT = 2;

    // Kafka 主题：点赞
    String TOPIC_LIKE = "like";

    // Kafka 主题：评论
    String TOPIC_COMMNET = "comment";

    // Kafka 主题：关注
    String TOPIC_FOLLOW = "follow";

    // Kafka 主题：发帖
    String TOPIC_PUBLISH = "publish";

    // Kafka 主题：删帖
    String TOPIC_DELETE = "delete";

    // 系统用户的 id
    int SYSTEM_USER_ID = 1;

    // 实体类型：人
    int ENTITY_TYPE_USER = 3;

    // 权限：普通用户
    String AUTHORITY_USER = "user";

    // 权限：管理员
    String AUTHORITY_ADMIN = "admin";

    // 权限：版主
    String AUTHORITY_MODERATOR = "moderator";
}
