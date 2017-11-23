package cn.skyeye.norths;

import cn.skyeye.common.json.Jsons;
import cn.skyeye.norths.events.DataEventDisruptor;
import cn.skyeye.norths.events.DataEventHandler;
import cn.skyeye.norths.services.syslog.Sysloger;
import cn.skyeye.norths.sources.DataSource;
import cn.skyeye.norths.sources.es.EsDataSource;
import cn.skyeye.resources.ConfigDetail;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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

    private File tmpfile;
    private Map<String, Object> status;
    private Timer tmpfileTimer;
    private String lastStatus;
    private long deltaDataFlushInterval;
    private long dataFetchInterval;
    private ExecutorService threadPool;

    private AtomicBoolean started = new AtomicBoolean(false);
    private Timer fetchDataTimer;

    private NorthContext(){
        this.northsConf = new NorthsConf();
        this.dataSourceMap = Maps.newHashMap();
        this.dataHandlerMap = Maps.newHashMap();

        int poolSize = northsConf.getConfigItemInteger("norths.datasources.threadpool.size",
                16);
        this.threadPool = Executors.newFixedThreadPool(poolSize);

        this.deltaDataFlushInterval = northsConf.getConfigItemLong("norths.datasources.status.flush.intervalms",
                10 * 60 * 1000L);

        this.dataFetchInterval = northsConf.getConfigItemLong("norths.datasources.data.fetch.intervalms",
                5 * 60 * 1000L);

        getTmpFile(northsConf);
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
            //startAutoFlushStatus();

            //注册钩子
            Runtime.getRuntime()
                    .addShutdownHook(
                            new Thread(() -> close()));

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
                        flushStatus();
                    } catch (InterruptedException e) { }
                }
            }, 0, dataFetchInterval);

            started.set(true);
            logger.info("NorthContext启动成功。");
        }else {
            logger.warn("NorthContext已启动，毋须重复启动。");
        }
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

        if(tmpfileTimer != null){
           flushStatus();
           tmpfileTimer.cancel();
        }

        if(dataEventDisruptor != null)dataEventDisruptor.shutDown();
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

    private void getTmpFile(ConfigDetail configDetail) {
        this.status = Maps.newConcurrentMap();
        String tmpFileDir = configDetail.getConfigItemValue("north.datasources.tmpfile.dir",
                "/opt/work/web/xenwebsite/data/");
        File file = new File(tmpFileDir);
        if(!file.exists()){
            file.mkdirs();
        }

        this.tmpfile = new File(file, "norths_delta.txt");
        if(tmpfile.exists()){
            try {
                String startTimeStr = FileUtils.readFileToString(tmpfile);
                if(StringUtils.isNotBlank(startTimeStr)) {
                    this.status.putAll(Jsons.toMap(startTimeStr));
                    this.lastStatus = startTimeStr;
                }
                logger.info(String.format("文件%s存在，内容为：\n\t %s", tmpfile, startTimeStr));
            } catch (Exception e) {
                logger.error(String.format("读取%s失败。", tmpfile), e);
            }
        }else{
            try {
                tmpfile.createNewFile();
                logger.info(String.format("新建文件%s", tmpfile));
            } catch (IOException e) {
                logger.error(String.format("新建%s失败。", tmpfile), e);
            }
        }
    }

    private void startAutoFlushStatus(){
        tmpfileTimer = new Timer("DataSourceStatusFlusher");
        tmpfileTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                flushStatus();
            }
        }, deltaDataFlushInterval, deltaDataFlushInterval);

        logger.info(String.format("增量状态定期刷新启动成功，周期为：%ss", deltaDataFlushInterval/1000));
    }

    private void flushStatus(){
        HashMap<String, Object> stringLongHashMap = Maps.newHashMap(status);
        String str = Jsons.obj2JsonString(stringLongHashMap);
        if(!str.equals(lastStatus)) {
            try {
                FileUtils.write(tmpfile, str, Charset.forName("UTF-8"), false);
                lastStatus = str;
                logger.info(String.format("更新%s成功，更新内容为：\n\t %s", tmpfile, str));
            } catch (IOException e) {
                logger.error(String.format("更新%s失败。", tmpfile), e);
            }
        }else {
            logger.info(String.format("%s内容无更新。", tmpfile));
        }
    }

    public DataEventHandler getHandler(String key){
        return dataHandlerMap.get(key);
    }

    public NorthsConf getNorthsConf() {
        return northsConf;
    }
}
