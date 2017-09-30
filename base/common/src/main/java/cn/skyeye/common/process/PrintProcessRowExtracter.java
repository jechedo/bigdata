package cn.skyeye.common.process;

import org.apache.log4j.Logger;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 1.0
 * @date 2016/10/24 14:24
 */
public class PrintProcessRowExtracter implements ProcessRowExtracter{

    private Logger logger;

    public enum PrintLevel{SYS_OUT, LOG_DEBUG, LOG_INFO, LOG_WARN, LOG_ERROR}

    private PrintLevel level;

    public PrintProcessRowExtracter(PrintLevel level){
        this(level,
                Logger.getLogger(PrintProcessRowExtracter.class));
    }

    public PrintProcessRowExtracter(PrintLevel level, Logger logger){
        this.level = level;
        this.logger = logger  ==  null ?
                Logger.getLogger(PrintProcessRowExtracter.class) : logger;
    }

    @Override
    public void extractRowData(String rowData) {

        switch (level){
            case SYS_OUT: System.out.println(rowData);break;
            case LOG_DEBUG:
                logger.debug(rowData); break;
            case LOG_INFO:
                logger.info(rowData);  break;
            case LOG_WARN:
                logger.warn(rowData);  break;
            case LOG_ERROR:
                logger.error(rowData); break;
        }
    }
}
