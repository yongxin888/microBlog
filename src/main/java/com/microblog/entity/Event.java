package com.microblog.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 封装事件（用于系统通知）
 * @DATE: 2023/4/22 16:22
 */
@Data
public class Event {
    private String topic; // 事件类型
    private int userId; // 事件由谁触发
    private int entityType; // 实体类型
    private int entityId; // 实体 id
    private int entityUserId; // 实体的作者(该通知发送给他）
    private Map<String, Object> data = new HashMap<>(); // 存储未来可能需要用到的数据

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
