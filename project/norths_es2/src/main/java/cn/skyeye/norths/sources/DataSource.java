package cn.skyeye.norths.sources;

import cn.skyeye.norths.NorthContext;
import cn.skyeye.resources.ConfigDetail;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/20 16:28
 */
public abstract class DataSource {
    protected final Log logger = LogFactory.getLog(DataSource.class);

    protected String name;
    protected String conf_preffix;

    protected NorthContext northContext;
    protected ConfigDetail configDetail;

    public DataSource(String name){
        this.name = name;
        this.conf_preffix = String.format("norths.datasources.%s.", name);

        this.northContext = NorthContext.get();
        Map<String, String> config = northContext.getNorthsConf().getConfigMapWithPrefix(conf_preffix);
        this.configDetail = new ConfigDetail(config);
    }

    public abstract List<Map<String, Object>> readData();
}
