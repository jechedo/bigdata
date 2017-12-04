package cn.skyeye.norths.services.syslog;

import org.apache.log4j.Logger;
import org.productivity.java.syslog4j.SyslogIF;
import org.productivity.java.syslog4j.SyslogRuntimeException;
import org.productivity.java.syslog4j.impl.backlog.log4j.Log4jSyslogBackLogHandler;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/4 23:11
 */
public class Log4jBackLogHandler extends Log4jSyslogBackLogHandler {

    public Log4jBackLogHandler(Logger logger) throws SyslogRuntimeException {
        super(logger);
    }

    @Override
    protected String combine(SyslogIF var1, int var2, String var3, String var4) {
        String var5 = var3 != null ? var3 : "UNKNOWN";
        String var6 = var4 != null ? var4 : "UNKNOWN";
        if (this.appendReason) {
            var5 = String.format("%s \n %s[host = %s, port = %s, protocol = %s]",
                    var4, var6, var1.getConfig().getHost(), var1.getConfig().getPort(), var1.getProtocol());
        }

        return var5;
    }

    @Override
    public void log(SyslogIF var1, int var2, String var3, String var4) throws SyslogRuntimeException {
        String var6 = this.combine(var1, var2, var3, var4);
        this.logger.error(var6);
    }
}
