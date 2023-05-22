package com.microblog.service.Impl;

import com.microblog.service.DataService;
import com.microblog.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @PROJECT_NAME: microBlog
 * @DESCRIPTION: 网站数据统计（UV 独立访客 / DAU 日活跃用户数量）
 * @DATE: 2023/5/1 14:20
 */
@Service
public class DataServiceImpl implements DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 一个与语言环境相关的格式化日期和分析日期的工具类。利用该类可以将日期转换成文本，或者将文本转换成日期
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 将指定的 IP 计入当天的 UV
     * @param ip
     */
    @Override
    public void recordUV(String ip) {
        //format()方法的作用是将日期(Date)转换为文本
        String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }

    /**
     * 统计指定日期范围内的 UV
     * @param start 开始时间
     * @param end 结束时间
     * @return
     */
    @Override
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 整理该日期范围内的 key
        List<String> keyList = new ArrayList<>();

        //Calendar类提供了获取或者设置各种日历的字段的方法。
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

        //判断开始时间是否大于结束时间
        while (!calendar.getTime().after(end)) {
            //将每天的IP相加
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE, 1); // 加1天
        }

        // 合并这些天的 UV
        String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());

        // 返回统计结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    /**
     * 将指定的 ID 计入当天的 DAU
     * @param userId 用户ID
     */
    @Override
    public void recordDAU(int userId) {
        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    /**
     * 统计指定日期范围内的 DAU
     * @param start 开始时间
     * @param end 结束时间
     * @return
     */
    @Override
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 整理该日期范围内的 key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

        //判断开始时间是否大于结束时间
        while (!calendar.getTime().after(end)) {
            //将每天的IP相加
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1); // 加1天
        }

        // 进行 or 运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
    }
}
