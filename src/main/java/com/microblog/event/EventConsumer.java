package com.microblog.event;

import com.alibaba.fastjson.JSONObject;
import com.microblog.entity.Event;
import com.microblog.entity.Message;
import com.microblog.service.MessageService;
import com.microblog.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 事件的消费者
 * @DATE: 2023/4/30 13:32
 */
@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    /**
     * 消费评论、点赞、关注事件
     * @KafkaListener 注解内指定topic名，对应topic有新消息来时，handleMessage方法被调用，参数就是topic内新消息。过程异步
     * ConsumerRecord 从kafka接收的键/值对。
     * @param record
     */
    @KafkaListener(topics = {TOPIC_COMMNET, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空");
            return ;
        }

        //JSON.parseObject（String str）是将str转化为相应的JSONObject对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {
            logger.error("消息格式错误");
            return ;
        }

        //发送系统通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        //封装消息内容
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        //经常data是否有数据，如果有，则遍历
        if (!event.getData().isEmpty()) { // 存储 Event 中的 Data
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);
    }
}
