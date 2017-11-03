package cn.skyeye.kafka.ganglia.monitors.zk;


import cn.skyeye.kafka.ganglia.configs.Constants;
import cn.skyeye.kafka.ganglia.configs.Yamls;
import cn.skyeye.kafka.ganglia.configs.ZkMonitorConf;
import cn.skyeye.kafka.ganglia.metrics.ZookeeperMetrics;
import cn.skyeye.kafka.ganglia.reporters.MetricReporter;
import cn.skyeye.kafka.ganglia.reporters.ZookeeperMetricReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * @author pcaparroy
 *  Telnet into each zookeeper node and extract out the metrics.
 */
public class ZookeeperMonitor implements Runnable{
    public static final Logger logger = LoggerFactory.getLogger(ZookeeperMonitor.class);
    private static final String _CONFIG= "/kafka/ganglia-zk-config.yml";

    private static final int DEFAULT_NUMBER_OF_THREADS = 10;
    public static final int DEFAULT_THREAD_TIMEOUT = 10;
    public static String LOG_PREFIX = "ZOOKEPER_GANGLIA_MONITOR";

    private ScheduledExecutorService threadPool;

    private ZkMonitorConf config;

    //To load the config files
    private MetricReporter metricReporter;


    public ZookeeperMonitor() throws FileNotFoundException {
        this.config =  Yamls.load(_CONFIG, ZkMonitorConf.class);
        init();
    }
    
    public void init()  {
        try {
             metricReporter = new ZookeeperMetricReporter(config.getGmondHost(), config.getGmondPort(), config.getMetricPrefix());
             int poolSize = config.getNumberOfThreads() == 0 ? DEFAULT_NUMBER_OF_THREADS : config.getNumberOfThreads();
             threadPool = Executors.newScheduledThreadPool(poolSize);
             ScheduledFuture<?> future = threadPool.scheduleWithFixedDelay(this, 0, config.getPollingIntervalMillis(), TimeUnit.MILLISECONDS);
            while(!future.isDone()){ }
            logger.info("Zookeeper ganglia-monitor stopping!!");
        }  catch (Exception e) {
            logger.error(getLogPrefix() + "Metrics collection failed", e);
            throw new RuntimeException("Failed to start Zookeeper monitor !!",e);
        } finally{
            if(!threadPool.isShutdown()){
                threadPool.shutdown();
            }
        }
    }
    
    public void run(){
    	//create parallel tasks to telnet into each server
        List<Future<ZookeeperMetrics>> parallelTasks = createParallelTasks(config);
        //collect the metrics
        int timeout = config.getThreadTimeout() == 0 ? DEFAULT_THREAD_TIMEOUT : config.getThreadTimeout();
        collectMetrics(parallelTasks, timeout);
        //collect the metrics
        try {
             metricReporter.report();
		} catch (Throwable e) {
			logger.error("Failed to collect and/or report metric", e);
			throw new RuntimeException(e);
		}
        logger.info(getLogPrefix() + "Zookeeper monitoring task completed successfully.");
    }

    private List<Future<ZookeeperMetrics>> createParallelTasks(ZkMonitorConf config) {
        List<Future<ZookeeperMetrics>> parallelTasks = new ArrayList<>();
        if (config != null && config.getServers() != null) {
            ZookeeperMonitorTask zookeeperTask;
            for (ZkMonitorConf.Server server : config.getServers()) {
                zookeeperTask = new ZookeeperMonitorTask(server, config.getCommands());
                parallelTasks.add(getThreadPool().submit(zookeeperTask));
            }
        }
        return parallelTasks;
    }

    private void collectMetrics(List<Future<ZookeeperMetrics>> parallelTasks, int timeout) {

        ZookeeperMetrics total = new ZookeeperMetrics("total");

        ZookeeperMetrics zMetrics;
        for (Future<ZookeeperMetrics> aParallelTask : parallelTasks) {
            try {
                double zk_followers;
                zMetrics = aParallelTask.get(timeout, TimeUnit.SECONDS);
                //metricReporter.collect(zMetrics);
                zk_followers = zMetrics.getMetric("zk_followers", -1);
                if(zk_followers > -1){
                    total.setMetrics("Leader Node", zMetrics.getServerId());
                }

                total.addOrSetMetrics("All Nodes", 1);

                //有效的节点加1
                if(zMetrics.getMetric(Constants.RUOK, -1) == Constants.OK){
                    total.addOrSetMetrics("Available Nodes", 1);
                }else{
                    total.addOrSetMetrics("Invalid Nodes", 1);
                }

            } catch (InterruptedException e) {
                logger.error(getLogPrefix() + "Task interrupted." + e);
            } catch (ExecutionException e) {
                logger.error(getLogPrefix() + "Task execution failed." + e);
            } catch (TimeoutException e) {
                logger.error(getLogPrefix() + "Task timed out." + e);
            }
        }

        metricReporter.collect(total);
    }


    private String getConfigFilename(String filename) {
    	String configFileName = "";
       
        if(new File(filename).exists()){
            configFileName = filename;
        }
        
        return configFileName;
    }

    public String getLogPrefix() {
        return LOG_PREFIX;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

}
