package cn.skyeye.cascade.quartz;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/3/14 0014.
 */
public class MyJob implements Job {
    @Override
    //把要执行的操作，写在execute方法中
    public void execute(JobExecutionContext context) throws JobExecutionException {
        DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String format = df.format(Calendar.getInstance().getTime());
        System.out.println("测试Quartz"+ format);
        try {

            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            Set<Map.Entry<String, Object>> entries = jobDataMap.entrySet();
            entries.forEach(e -> System.err.println(entries.size() + " : " + e.getKey() + " -- " + e.getValue()));

            Thread.sleep(2000);
            System.out.println("测试Quartz"+ format + "--" + this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}