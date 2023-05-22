package com.microblog.event;

import com.alibaba.fastjson.JSONObject;
import com.microblog.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 事件的生产者
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理事件
     * @param event
     */
    public void fireEvent(Event event) {
        // 将事件发布到指定的主题  JSONObject.toJSONString将一个实体对象转换成Json字符串
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
