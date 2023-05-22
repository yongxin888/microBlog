package com.microblog.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 生成随机数工具类
 * @DATE: 2023/4/20 19:54
 */
public class CommunityUtil {
    /**
     * 生成随机字符串
     * @return
     */
    public static String generateUUID() {
        // 去除生成的随机字符串中的 ”-“
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * md5 加密
     * @param key 要加密的字符串
     * @return
     */
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * 将服务端返回的消息封装成 JSON 格式的字符串
     * @param code 状态码
     * @param msg 提示消息
     * @param map 业务数据
     * @return 返回 JSON 格式字符串
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    /**
     * 重载 getJSONString 方法，服务端方法可能不返回业务数据
     * @param code 状态码
     * @param msg 提示消息
     * @return
     */
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    /**
     * 重载 getJSONString 方法，服务端方法可能不返回业务数据和提示消息
     * @param code 状态码
     * @return
     */
    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        System.out.println(generateUUID());
    }
}
