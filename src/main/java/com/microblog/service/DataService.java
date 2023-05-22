package com.microblog.service;

import java.util.Date;

/**
 * 网站数据统计（UV / DAU）
 */
public interface DataService {
    //将指定的 IP 计入当天的 UV
    void recordUV(String ip);

    //统计指定日期范围内的 UV
    long calculateUV(Date start, Date end);

    //将指定的 IP 计入当天的 DAU
    void recordDAU(int userId);

    //统计指定日期范围内的 DAU
    long calculateDAU(Date start, Date end);
}
