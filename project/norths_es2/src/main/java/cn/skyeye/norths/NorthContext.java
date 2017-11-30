package cn.skyeye.norths;

import cn.skyeye.norths.events.DataEventDisruptor;
import cn.skyeye.norths.events.DataEventHandler;
import cn.skyeye.norths.services.syslog.Sysloger;
import cn.skyeye.norths.sources.DataSource;
import cn.skyeye.norths.sources.es.EsDataSource;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private DataEventDisruptor dataEventDisruptor;
    private Map<String, DataSource> dataSourceMap;
    private Map<String, DataEventHandler> dataHandlerMap;

    private Map<String, Object> status;
    private long dataFetchInterval;
    private ExecutorService threadPool;

    private AtomicBoolean started = new AtomicBoolean(false);
    private Timer fetchDataTimer;

    private NorthContext(){
        this.northsConf = new NorthsConf();
        this.dataSourceMap = Maps.newHashMap();
        this.dataHandlerMap = Maps.newHashMap();
        this.status = Maps.newConcurrentMap();

        int poolSize = northsConf.getConfigItemInteger("norths.datasources.threadpool.size",
                16);
        this.threadPool = Executors.newFixedThreadPool(poolSize);

        this.dataFetchInterval = northsConf.getConfigItemLong("norths.datasources.data.fetch.intervalms",
                5 * 60 * 1000L);
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

            //注册系统关闭的钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> close()));

            startFetchData();
            started.set(true);
            logger.info("NorthContext启动成功。");

        }else {
            logger.warn("NorthContext已启动，毋须重复启动。");
        }
    }

    private void startFetchData() {
        //循环抓取数据
        fetchDataTimer = new Timer("DataFetcherTimer");
        fetchDataTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Collection<DataSource> values = dataSourceMap.values();
                CountDownLatch countDownLatch = new CountDownLatch(values.size());
                values.forEach(dataSource -> threadPool.submit(() -> {
                    try {
                        dataSource.readData();
                    } finally {
                        countDownLatch.countDown();
                    }
                }));
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                }
            }
        }, 0, dataFetchInterval);
        logger.info(String.format("启动增量读取告警日志数据的调度器成功，读取周期为：%sms", dataFetchInterval));
    }

    private void initAndStart(){
        initDataHandler();
        Preconditions.checkArgument(!dataHandlerMap.isEmpty(), "没有配置可用的数据处理器。");
        Collection<DataEventHandler> handlers = dataHandlerMap.values();
        this.dataEventDisruptor = new DataEventDisruptor(handlers.toArray(new DataEventHandler[handlers.size()]));

        initDataSources();
        Preconditions.checkArgument(!dataSourceMap.isEmpty(), "没有配置可用的数据源。");

        this.dataEventDisruptor.start();
    }

    public void close(){
        if(fetchDataTimer != null)fetchDataTimer.cancel();
        if(dataEventDisruptor != null)dataEventDisruptor.shutDown();
        started.set(false);
    }

    private void initDataSources() {
        Set<String> sources = northsConf.getConfigItemSet("norths.datasources");
        DataSource dataSource;
        for(String source : sources){
            if(source.startsWith("es")){
                  dataSource = new EsDataSource("es", threadPool, dataEventDisruptor);
                  dataSourceMap.put("es", dataSource);
            }else {
                new IllegalArgumentException(String.format("不识别的数据源类型%s， 目前仅支持es", source));
            }
        }
    }

    private void initDataHandler(){
        Set<String> handlers = northsConf.getConfigItemSet("norths.handlers");
        DataEventHandler dataEventHandler;
        for(String hangdler : handlers){
            if(hangdler.startsWith("syslog")){
                dataEventHandler = new Sysloger("syslog");
                dataHandlerMap.put("syslog", dataEventHandler);
            }else {
                new IllegalArgumentException(String.format("不识别的处理器类型%s， 目前仅支持syslog", hangdler));
            }
        }
    }

    public void setStatus(String key, Object status){
        this.status.put(key, status);
    }

    public Object getStatus(String key){
        return this.status.get(key);
    }

    public DataEventHandler getHandler(String key){
        return dataHandlerMap.get(key);
    }

    public NorthsConf getNorthsConf() {
        return northsConf;
    }
}
