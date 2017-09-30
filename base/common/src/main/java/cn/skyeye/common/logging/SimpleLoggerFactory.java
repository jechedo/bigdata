package cn.skyeye.common.logging;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class SimpleLoggerFactory {

    private static String DEFAULT_LOG_PATH = "/var/log/bigdata";
    private static String LOG_NAME = "default";

    private String logPath;

    public SimpleLoggerFactory(String logPath){
        this.logPath = (logPath == null) ? DEFAULT_LOG_PATH : logPath;
    }

    public Logger newLogger(String name){
        return DynamicLogFactory.getLogger(name, logPath);
    }

    public Logger newSimpleLogger(String name){
        return DynamicLogFactory.getSimpleLogger(name, logPath);
    }

    public String getLogPath() {
        return logPath;
    }

    public static Logger getLogger(){
        return getLogger(DEFAULT_LOG_PATH);
    }

    public static Logger getLogger(String logPath){
        return getLogger(logPath, LOG_NAME);
    }

    public static Logger getLogger(String logPath, String name){
        boolean useDefault = StringUtils.isBlank(logPath);
        Logger logger;
        if(useDefault){
            logger = DynamicLogFactory.getLogger(name, DEFAULT_LOG_PATH);
        }else {
            logger = DynamicLogFactory.getLogger(name, logPath);
        }
        return logger;
    }
}