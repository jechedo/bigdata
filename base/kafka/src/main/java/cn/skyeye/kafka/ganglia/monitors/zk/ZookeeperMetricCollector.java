package cn.skyeye.kafka.ganglia.monitors.zk;


import cn.skyeye.kafka.ganglia.Utils;
import cn.skyeye.kafka.ganglia.configs.Constants;
import cn.skyeye.kafka.ganglia.configs.ZkMonitorConf;
import cn.skyeye.kafka.ganglia.metrics.ZookeeperMetrics;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;


public class ZookeeperMetricCollector implements Callable<ZookeeperMetrics>{

    private static final Logger logger = Logger.getLogger(ZookeeperMonitor.class);
    public static final String COLON = ":";
    public static final int SOCK_TIMEOUT_IN_MS = 120000;

    private ZkMonitorConf.Server server;
    //hostname
    private String host;
    //port
    private int port;
 
    //commands to be executed
    private ZkMonitorConf.Command[] commands;

    public ZookeeperMetricCollector(ZkMonitorConf.Server server, ZkMonitorConf.Command[] commands){
        this.server = server;
        this.commands = commands;
        parse(server.getServer());
    }


    public ZookeeperMetrics call() throws Exception {
        ZookeeperMetrics zMetrics = new ZookeeperMetrics(server.getDisplayName());
        zMetrics.setServerId(server.getServerId());
        if (commands != null) {
            int maxtry;
            for (ZkMonitorConf.Command command : commands) {
                maxtry = 5;
                if (isCommandValid(command)) {
                    while (maxtry > 0) {
                        try {
                            executeCommandAndCollectMetrics(command, zMetrics);
                            break;
                        } catch (Exception e) {
                            maxtry -= 1;
                            if(maxtry == 0) {
                                logger.error("Error telnetting into server ::" + zMetrics.getDisplayName(), e);
                                zMetrics.setMetrics(Constants.RUOK, Constants.NOT_OK);
                            }else {
                                Thread.sleep(500);
                            }
                        }
                    }
                }
            }
        }
        return zMetrics;
    }

    private void executeCommandAndCollectMetrics(ZkMonitorConf.Command command, ZookeeperMetrics zMetrics) throws IOException {
        List<String> res = Utils.executeCommand(host, port, command.getCommand(), SOCK_TIMEOUT_IN_MS);
        res.forEach(line -> extractTheMetric(command, zMetrics, line));
    }

    private void extractTheMetric(ZkMonitorConf.Command command, ZookeeperMetrics zMetrics, String line) {
        String metricName = null;
        String metricStr = null;
        double metricValue;
        if(isCommandRUOK(command)){
            metricValue = Constants.NOT_OK;
            if(line.trim().equalsIgnoreCase(Constants.IAMOK)){
                metricName = Constants.RUOK;
                metricValue = Constants.OK;
            }
            zMetrics.setMetrics(metricName, metricValue);
        } else{
            String[] splits = line.split(command.getSeparator());
            if(splits != null && splits.length > 1) {
                metricName = splits[0].trim();
                metricStr = splits[1].trim();
            }
            if(command.getFields().contains(metricName) && metricStr != null){
                if(metricName.equalsIgnoreCase(Constants.LATENCY_MIN_AVG_MAX)){
                    handleLatencyMetrics(zMetrics, metricStr);
                }else {
                    metricValue = Double.parseDouble(metricStr);
                    zMetrics.setMetrics(metricName, metricValue);
                }
            }
        }


    }

    private void handleLatencyMetrics(ZookeeperMetrics zMetrics, String metricValue) {
        String[] latencySplits = metricValue.split("/");
        if(latencySplits.length > 2){
            zMetrics.setMetrics(Constants.MIN_LATENCY, Double.parseDouble(latencySplits[0]));
            zMetrics.setMetrics(Constants.AVG_LATENCY, Double.parseDouble(latencySplits[1]));
            zMetrics.setMetrics(Constants.MAX_LATENCY, Double.parseDouble(latencySplits[2]));
        }
    }

    private boolean isCommandRUOK(ZkMonitorConf.Command command) {
        return command.getCommand().equalsIgnoreCase(Constants.RUOK);
    }


    private void parse(String server) {
        if(server != null){
            String[] splits = server.split(COLON);
            if(splits != null && splits.length > 1){
                host = splits[0].trim();
                try {
                    port = Integer.parseInt(splits[1].trim());
                }
                catch(NumberFormatException nfe){
                    logger.error("Error parsing the port for " + server);
                }
            }
        }
    }


    private boolean isCommandValid(ZkMonitorConf.Command command) {
        if(isCommandRUOK(command)){
            return true;
        }
        return command.getFields() != null && command.getFields().size() > 0;
    }
}
