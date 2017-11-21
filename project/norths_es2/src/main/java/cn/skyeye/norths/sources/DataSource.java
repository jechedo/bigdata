package cn.skyeye.norths.sources;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/20 16:28
 */
public abstract class DataSource {

    protected final Logger logger = Logger.getLogger(DataSource.class);

    public abstract List<Map<String, Object>> readData();
}
