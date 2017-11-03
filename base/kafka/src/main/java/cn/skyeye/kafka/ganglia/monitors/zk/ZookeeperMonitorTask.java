package cn.skyeye.kafka.ganglia.monitors.zk;


import cn.skyeye.kafka.ganglia.configs.Constants;
import cn.skyeye.kafka.ganglia.configs.ZkMonitorConf;
import cn.skyeye.kafka.ganglia.metrics.ZookeeperMetrics;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * @author pcaparroy
 *
 */
public class ZookeeperMonitorTask implements Callable<ZookeeperMetrics>{

    public static final Logger logger = LoggerFactory.getLogger(ZookeeperMonitor.class);
    public static final String COLON = ":";
    public static final boolean AUTO_FLUSH = true;
    public static final int SOCK_TIMEOUT_IN_MS = 120000;

    private ZkMonitorConf.Server server;
    //hostname
    private String host;
    //port
    private int port;
 
    //commands to be executed
    private ZkMonitorConf.Command[] commands;

    public ZookeeperMonitorTask(ZkMonitorConf.Server server, ZkMonitorConf.Command[] commands){
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
                                //for any exception making sure that ruok is NOT_OK for that server
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
        Socket socket = null;
        OutputStream outputStream = null;
        PrintWriter out = null;
        BufferedReader in = null;
        InputStream inputStream = null;
        try{
            socket = createSocket();
            outputStream = socket.getOutputStream();
            out = new PrintWriter(outputStream, AUTO_FLUSH);

            inputStream = socket.getInputStream();
            in =  new BufferedReader(new InputStreamReader(inputStream));

            out.println(command.getCommand());
            String line = in.readLine();
            while(line != null){
                extractTheMetric(command, zMetrics, line);
                line = in.readLine();
            }
        } finally{
            Closeables.close(out, true);
            Closeables.close(in, true);
            Closeables.close(inputStream, true);
            Closeables.close(outputStream, true);
            Closeables.close(socket, true);
        }
    }

    private Socket createSocket() throws IOException {
        Socket socket;
        socket = new Socket(host, port);
        socket.setSoTimeout(SOCK_TIMEOUT_IN_MS);  //timeout on the socket
        return socket;
    }


    private void extractTheMetric(ZkMonitorConf.Command command, ZookeeperMetrics zMetrics, String line) {
        String metricName = null;
        String metricStr = null;
        double metricValue;
        //separate handling for ruok
        if(isCommandRUOK(command)){
            metricValue = Constants.NOT_OK;
            if(line.trim().equalsIgnoreCase(Constants.IAMOK)){
                metricName = Constants.RUOK;
                metricValue = Constants.OK;
            }
            zMetrics.setMetrics(metricName, metricValue);
        } else{
            //metrics "key SEPARATOR value" format
            String[] splits = line.split(command.getSeparator());
            if(splits != null && splits.length > 1) {
                metricName = splits[0].trim();
                metricStr = splits[1].trim();
            }
            if(command.getFields().contains(metricName) && metricStr != null){
                //separately handling Latecny min/avg/max metric
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
