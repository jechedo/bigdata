package cn.skyeye.common.process;

import org.apache.log4j.Logger;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 1.0
 * @date 2016/10/24 14:24
 */
public class PrintLineExtracter implements LineExtracter {

    private Logger logger;

    public enum PrintLevel{SYS_OUT, LOG_DEBUG, LOG_INFO, LOG_WARN, LOG_ERROR}

    private PrintLevel level;

    public PrintLineExtracter(PrintLevel level){
        this(level,
                Logger.getLogger(PrintLineExtracter.class));
    }

    public PrintLineExtracter(PrintLevel level, Logger logger){
        this.level = level;
        this.logger = logger  ==  null ?
                Logger.getLogger(PrintLineExtracter.class) : logger;
    }

    @Override
    public void extract(boolean isError, String line) {

        switch (level){
            case SYS_OUT:
                if(isError){
                    System.err.println(line);
                }else{
                    System.out.println(line);}
                break;
            case LOG_DEBUG:
                logger.debug(line); break;
            case LOG_INFO:
                logger.info(line);  break;
            case LOG_WARN:
                logger.warn(line);  break;
            case LOG_ERROR:
                logger.error(line); break;
        }
    }
}
