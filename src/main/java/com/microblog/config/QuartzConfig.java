package com.microblog.config;

import com.microblog.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * Spring Quartz 配置类，用于将数据存入数据库，以后直接从数据库中调用数据
 */
@Configuration
public class QuartzConfig {

    /**
     * 刷新帖子分数任务
     * @return
     */
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true); //设置任务长久保存
        factoryBean.setRequestsRecovery(true);  //设置任务可恢复
        return factoryBean;
    }

    /**
     * 刷新帖子分数触发器
     * @return
     */
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 60 * 24 * 7); // 每周刷新一次
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
