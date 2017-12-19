package cn.skyeye.cascade.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Main {
    public static void main(String[] args) {

        DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        System.out.println("开始时间："+ df.format(Calendar.getInstance().getTime()));

        //通过schedulerFactory获取一个调度器
        SchedulerFactory schedulerfactory = new StdSchedulerFactory();
        Scheduler scheduler = null;
        try {
//      通过schedulerFactory获取一个调度器
            scheduler = schedulerfactory.getScheduler();
            //       启动调度
            scheduler.start();

//       创建jobDetail实例，绑定Job实现类
//       指明job的名称，所在组的名称，以及绑定job类
            JobDetail job = JobBuilder.newJob(MyJob.class)
                    .withIdentity("job", "jgroup1")
                    .usingJobData("test", "demo")
                    .usingJobData("id", "id-demo")
                    .build();

            JobDetail job1 = JobBuilder.newJob(MyJob.class)
                    .withIdentity("job1", "jgroup1")
                    .usingJobData("test", "demo2")
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("simpleTrigger", "triggerGroup")
                    .usingJobData("demo", "test")
                    .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(1))
                    .startNow().build();

            Trigger trigger1 = TriggerBuilder.newTrigger()
                    .withIdentity("simpleTrigger1", "triggerGroup")
                    .usingJobData("demo", "test1")
                    .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(1))
                    .startNow().build();

            //       定义调度触发规则

//      使用simpleTrigger规则
//        Trigger trigger=TriggerBuilder.newTrigger().withIdentity("simpleTrigger", "triggerGroup")
//                        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(1).withRepeatCount(8))
//                        .startNow().build();
//      使用cornTrigger规则  每天10点42分
          /*  Trigger trigger = TriggerBuilder.newTrigger().withIdentity("simpleTrigger", "triggerGroup")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0/15 * * * * ? *"))
                    //.forJob(job)
                    .startNow().build();
*/
            //把作业和触发器注册到任务调度中
            scheduler.scheduleJob(job, trigger);
            scheduler.scheduleJob(job1, trigger1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}