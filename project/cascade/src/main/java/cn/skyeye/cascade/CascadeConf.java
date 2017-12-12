package cn.skyeye.cascade;

import cn.skyeye.resources.ConfigDetail;
import org.apache.log4j.Logger;

import java.sql.Connection;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/12 17:40
 */
public class CascadeConf extends ConfigDetail {
    private final static String _CONFIG = "/cascade_config/cascade";
    protected final Logger logger = Logger.getLogger(CascadeConf.class);

    private Connection conn;

}
