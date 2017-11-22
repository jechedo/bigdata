package cn.skyeye.norths;

import cn.skyeye.norths.events.DataEventDisruptor;
import cn.skyeye.norths.services.syslog.Sysloger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/20 21:01
 */
public class NorthContext {

    protected final Log logger = LogFactory.getLog(NorthContext.class);

    private static volatile NorthContext northContext;

    private NorthsConf northsConf;
    private Sysloger sysloger;
    private DataEventDisruptor dataEventDisruptor;

    private AtomicBoolean started = new AtomicBoolean(false);

    private NorthContext(){
        this.sysloger = new Sysloger();
        this.dataEventDisruptor = new DataEventDisruptor(sysloger);
    }

    public static NorthContext get(){
        if(northContext == null){
            synchronized (NorthContext.class){
                if(northContext == null){
                    northContext = new NorthContext();
                }
            }
        }
        return northContext;
    }

    public void start(){
        if(!started.get()){
            initAndStart();
            started.set(true);
            logger.info("NorthContext启动成功。");
        }else {
            logger.warn("NorthContext已启动，毋须重复启动。");
        }
    }

    private void initAndStart(){
        this.northsConf = new NorthsConf();
    }

    public NorthsConf getNorthsConf() {
        return northsConf;
    }

    public Sysloger getSysloger() {
        return sysloger;
    }
}
