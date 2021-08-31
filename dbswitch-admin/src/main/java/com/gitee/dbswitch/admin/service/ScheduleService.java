// Copyright tang.  All rights reserved.
// https://gitee.com/inrgihc/dbswitch
//
// Use of this source code is governed by a BSD-style license
//
// Author: tang (inrgihc@126.com)
// Date : 2020/1/2
// Location: beijing , china
/////////////////////////////////////////////////////////////
package com.gitee.dbswitch.admin.service;

import com.gitee.dbswitch.admin.dao.AssignmentJobDAO;
import com.gitee.dbswitch.admin.dao.AssignmentTaskDAO;
import com.gitee.dbswitch.admin.entity.AssignmentJobEntity;
import com.gitee.dbswitch.admin.entity.AssignmentTaskEntity;
import com.gitee.dbswitch.admin.type.ScheduleModeEnum;
import com.gitee.dbswitch.admin.util.UuidUtil;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ScheduleService {

  /**
   * @Bean是一个方法级别上的注解，Bean的ID为方法名字。
   * @Resource默认按照ByName自动注入
   * @Autowired默认按照类型byType注入
   */
  @Autowired
  private SchedulerFactoryBean schedulerFactoryBean;

  @Resource
  private AssignmentTaskDAO assignmentTaskDAO;

  @Resource
  private AssignmentJobDAO assignmentJobDAO;

  public static boolean checkCronExpressionValid(String cronExpression) {
    try {
      CronScheduleBuilder.cronSchedule(cronExpression);
      return true;
    } catch (Exception e) {
      return false;
    }

  }

//  public Trigger getQuartzJobDetail(String jobKey) {
//    Scheduler scheduler = schedulerFactoryBean.getScheduler();
//
//    String triggerName = jobKey;
//    String triggerGroup = JobExecutorService.GROUP;
//    TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
//    try {
//      Trigger trigger = scheduler.getTrigger(triggerKey);
//      return trigger;
//    } catch (SchedulerException e) {
//      log.error("Query job trigger detail failed:", e);
//    }
//
//    return null;
//  }

  public void scheduleTask(Long taskId, ScheduleModeEnum scheduleMode) {
    /** 准备JobDetail */
    String jobName = UuidUtil.generateUuid() + "@" + taskId.toString();
    String jobGroup = JobExecutorService.GROUP;
    JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

    JobBuilder jobBuilder = JobBuilder.newJob(JobExecutorService.class)
        .withIdentity(jobKey)
        .usingJobData(JobExecutorService.TASK_ID, taskId.toString())
        .usingJobData(JobExecutorService.SCHEDULE, scheduleMode.getValue().toString());

    /** 准备TriggerKey，注意这里的triggerName与jobName配置相同 */
    String triggerName = jobName;
    String triggerGroup = JobExecutorService.GROUP;
    TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);

    log.info("Create schedule task, taskId: {}", taskId);

    AssignmentTaskEntity task = assignmentTaskDAO.getById(taskId);
    if (ScheduleModeEnum.MANUAL == scheduleMode) {
      scheduleOnce(jobBuilder.storeDurably(false).build(), triggerKey);
    } else {
      scheduleCron(jobBuilder.storeDurably(true).build(), triggerKey, task.getCronExpression());
    }

  }

  public void cancelJobByTaskId(Long taskId) {
    List<AssignmentJobEntity> jobs = assignmentJobDAO.getByAssignmentId(taskId);
    if (CollectionUtils.isNotEmpty(jobs)) {
      for (AssignmentJobEntity job : jobs) {
        cancelJob(job.getId());
      }
    }
  }

  public boolean cancelJob(Long jobId) {
    AssignmentJobEntity assignmentJobEntity = assignmentJobDAO.getById(jobId);

    String jobName = assignmentJobEntity.getJobKey();
    String jobGroup = JobExecutorService.GROUP;
    JobKey jobKey = JobKey.jobKey(jobName, jobGroup);

    String triggerName = jobName;
    String triggerGroup = JobExecutorService.GROUP;
    TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);

    Scheduler scheduler = schedulerFactoryBean.getScheduler();

    try {
      scheduler.interrupt(jobKey);
      scheduler.pauseTrigger(triggerKey);
      return scheduler.unscheduleJob(triggerKey) && scheduler.deleteJob(jobKey);
    } catch (SchedulerException e) {
      log.error("Quartz stop task job failed. JobKey: {}", jobKey);
    }

    return false;
  }

  private void scheduleOnce(JobDetail jobDetail, TriggerKey triggerKey) {
    Scheduler scheduler = schedulerFactoryBean.getScheduler();
    Trigger simpleTrigger = TriggerBuilder.newTrigger()
        .startAt(new Date())
        .withIdentity(triggerKey)
        .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
        .build();

    try {
      scheduler.scheduleJob(jobDetail, simpleTrigger);
    } catch (SchedulerException e) {
      log.error("Quartz schedule task by manual failed, taskId: {}.",
          jobDetail.getJobDataMap().get(JobExecutorService.TASK_ID), e);
      throw new RuntimeException(e);
    }

  }

  private void scheduleCron(JobDetail jobDetail, TriggerKey triggerKey, String cronExpression) {
    Scheduler scheduler = schedulerFactoryBean.getScheduler();
    Trigger cronTrigger = TriggerBuilder.newTrigger()
        .withIdentity(triggerKey)
        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
        .build();

    try {
      scheduler.scheduleJob(jobDetail, cronTrigger);
    } catch (SchedulerException e) {
      log.error("Quartz schedule task by expression failed, taskId: {}.",
          jobDetail.getJobDataMap().get(JobExecutorService.TASK_ID), e);
      throw new RuntimeException(e);
    }

  }

}
