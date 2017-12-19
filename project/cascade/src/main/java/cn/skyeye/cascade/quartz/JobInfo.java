package cn.skyeye.cascade.quartz;

import org.apache.log4j.Logger;
import org.quartz.*;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/14 18:00
 */
public class JobInfo {
    private final Logger logger = Logger.getLogger(JobInfo.class);
    private static final String GROUP = "skyeye";

    private JobKey jobKey;
    private TriggerKey triggerKey;

    private JobDetail job;
    private Trigger trigger;

    public JobInfo(JobDetail job, Trigger trigger) {
        this.jobKey = job.getKey();
        this.job = job;
        this.triggerKey = trigger.getKey();
        this.trigger = trigger;
    }

    public JobKey getJobKey() {
        return jobKey;
    }

    public TriggerKey getTriggerKey() {
        return triggerKey;
    }

    public JobDetail getJob() {
        return job;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public static JobKey getJobKey(String name){
        return new JobKey(name, GROUP);
    }
}
