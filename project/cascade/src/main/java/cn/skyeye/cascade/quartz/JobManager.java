package cn.skyeye.cascade.quartz;

import cn.skyeye.cascade.CascadeContext;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Map;

/**
 * Description:
 *      启动  停止 任务
 * @author LiXiaoCong
 * @version 2017/12/14 16:56
 */
public class JobManager {
    private final Logger logger = Logger.getLogger(JobManager.class);

    private Map<JobKey, JobInfo> jobs;

    private CascadeContext cascadeContext;
    private  Scheduler scheduler;

    public JobManager(CascadeContext cascadeContext) throws SchedulerException {
        this.cascadeContext = cascadeContext;
        this.jobs = Maps.newConcurrentMap();

        StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();
        this.scheduler = stdSchedulerFactory.getScheduler();
        this.scheduler.start();
    }

    public void startJob(JobDetail job, Trigger trigger) throws SchedulerException {
        JobKey jobKey = job.getKey();
        if(!jobs.containsKey(jobKey) && job != null && trigger != null) {
            scheduler.scheduleJob(job, trigger);
            JobInfo jobInfo = new JobInfo(job, trigger);
            jobs.put(jobKey, jobInfo);
            logger.info(String.format("启动key为：%s的任务成功。", jobKey));
        }else {
            logger.warn(String.format("已经存在一个key为：%s的任务。", jobKey));
        }
    }

    /**
     * 暂停任务
     */
    public void pause(JobKey jobKey)  {
        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            logger.error(String.format("暂停任务 %s 失败 ", jobKey.toString()), e);
        }
    }

    /**
     * 恢复任务
     */
    public void recovery(JobKey jobKey) {
        try {
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            logger.error(String.format("恢复任务 %s 失败 ", jobKey.toString()), e);
        }
    }

    /**
     * 恢复所有任务
     */
    public void recoveryAll() {
        try {
            scheduler.resumeAll();
        } catch (SchedulerException e) {
            logger.error("恢复所有任务失败。");
        }
    }

    public void remove(JobKey jobKey) {
        try {
            scheduler.deleteJob(jobKey);
            jobs.remove(jobKey);
            logger.info(String.format("删除任务 %s 成功。 ", jobKey.toString()));
        } catch (SchedulerException e) {
            logger.error(String.format("删除任务 %s 失败 ", jobKey.toString()), e);
        }
    }

    public void shutdown(boolean waitForJobsToComplete) {
        try {
            scheduler.shutdown(waitForJobsToComplete);
        } catch (SchedulerException e) {}
    }

    public void shutdown()  {
        shutdown(true);
    }
}
