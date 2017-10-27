package cn.skyeye.aptrules.ioc2rules.iocs.stores;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ARContext;
import cn.skyeye.aptrules.ioc2rules.extracters.IoCAsRuleExtracter;
import cn.skyeye.common.databases.DataBases;
import com.google.common.collect.Maps;
import redis.clients.jedis.Jedis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Description:
 *   sqlite
 * @author LiXiaoCong
 * @version 2017/10/27 9:48
 */
public class IoCSQLiteStore extends IoCStore {

    private final String last_time_key = "ioc2rule-last-timestamp";
    private final String last_ioccount_key = "ioc-count-last-time";

    private String table = "iocs";
    private String activeTimeField = "active_change_time";
    private List<String> columns;

    private long stime = 0L;
    private long etime;

    public IoCSQLiteStore(ARConf arConf) {
        super(arConf);
    }

    private int iocCount()  {

        Statement statement = null;
        ResultSet resultSet = null;
        int count = 0;
        try {
            statement = arConf.getConn().createStatement();
            resultSet = statement.executeQuery(String.format("select count(*) count from %s", table));
            count = resultSet.getInt("count");
        } catch (Exception e) {
           logger.error(String.format("获取表%s中的ioc总数失败。", table), e);
        } finally {
            DataBases.close(resultSet);
            DataBases.close(statement);
        }
        return count;
    }

    /**
     *  统计 active_change_time 在[lower, upper]区间的数据量
     * @param lower
     * @param upper
     * @return
     * @throws IoCSQLiteStore
     */
    private int iocActiveBetweenCount(long lower, long upper)  {

        String sql = String.format("select count(*) count from %s where %s >= ? and %s <= ?",
                table, activeTimeField, activeTimeField);

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int count = 0;
        try {
            preparedStatement = arConf.getConn().prepareStatement(sql);
            preparedStatement.setObject(1, lower);
            preparedStatement.setObject(2, upper);

            resultSet = preparedStatement.executeQuery();
            count = resultSet.getInt("count");
        } catch (Exception e) {
            logger.error(String.format("获取表%s中的字段%s在区间[%s, %s]的ioc总数失败。", table, activeTimeField, lower, upper), e);
        } finally {
            DataBases.close(resultSet);
            DataBases.close(preparedStatement);
        }

        return count;
    }

    /**
     * 获取所有的ioc
     */
    @Override
    public void extractIoCAsRules(IoCAsRuleExtracter ioCAsRuleExtracter) throws Exception {
        Statement statement = null;
        ResultSet resultSet = null;
        Jedis jedis = null;
        try {
            jedis = ARContext.get().getJedis();
            if(!isModified(jedis)){
                logger.warn("sqlite has no new ioc, just exit!");
                return;
            }

            String sql = String.format("select * from %s", table);

            statement = arConf.getConn().createStatement();
            resultSet = statement.executeQuery(sql);

            if(columns == null || columns.isEmpty()){
                columns = DataBases.getColumns(resultSet);
            }

            Map<String, Object> ioc;
            Object value;
            while (resultSet.next()){
                ioc = Maps.newHashMap();
                for(String column : columns){
                    value = resultSet.getObject(column);
                    value = changeValueByColumn(value, column);
                    ioc.put(column, value);
                }
                ioCAsRuleExtracter.extract(ioc);
            }
        } finally {
            DataBases.close(resultSet);
            DataBases.close(statement);
            if(jedis != null) jedis.close();
        }
    }

    @Override
    public void updateStatus(boolean syncSuccess) {
        Jedis jedis = null;
        try {
            if(syncSuccess) {
                jedis = ARContext.get().getJedis();
                jedis.set(last_time_key, String.valueOf(etime));
            }else {
                jedis.set(last_ioccount_key, "0");
            }
        }catch (Exception e){
            logger.error(String.format("更新redis中%s失败。", last_time_key));
        }finally {
            if(jedis != null)jedis.close();
        }
    }

    private Object changeValueByColumn(Object value, String column) {
        switch (column.toLowerCase()){
            case "active":
            case "export":
                value = changeToBoolean(value, true);
                break;
            case "confidence":
                if(value == null) value = 80;
                break;
            case "ip_or_domain":
                if(value != null) value = trim(String.valueOf(value));
                break;
            case "desc":
                if(value != null) value = trim(String.valueOf(value));
                break;
        }
        return value;
    }

    private Object changeToBoolean(Object value, boolean defaultValue) {
        if(value == null){
            value = defaultValue;
        }else{
            String tmp = String.valueOf(value);
            if("1".equals(tmp)){
                value = true;
            }else {
                value = false;
            }
        }
        return value;
    }

    private String trim(String str){
        str =  str.replaceAll("\r", "")
                .replaceAll("\n", "")
                .replaceAll("\t", "")
                .trim();
        return str;
    }

    private void initActiveTimeRange(Jedis jedis) {
        String lastActiveTime = jedis.get(last_time_key);
        if (lastActiveTime != null) {
            stime = Long.parseLong(lastActiveTime);
        }
        etime = System.currentTimeMillis();
    }

    /**
     * 判断sqlite中的ioc表是否有变动，判断规则为:
     *    判断sqlite中ioc总量是否与redis中记录的最后一次处理的ioc总量相等
     *    若不想等，则返回true,
     *    若相等，则判断从redis中记录的最后一次处理ioc时间到当前时间的范围内，是否有ioc记录变更，
     *    若有 则返回true, 否则返回false
     *
     * 查询sqlite失败的时候返回false.
     */
    private boolean isModified(Jedis jedis) {

        int redisIocCount = 0;
        String iocCountStr = jedis.get(last_ioccount_key);
        if(iocCountStr != null){
            redisIocCount = Integer.parseInt(iocCountStr);
        }

        //查询sqlite 获取当前ioc的总量 进行比较
        int dbIocCount = -1;
        try {
            dbIocCount = iocCount();
        } catch (Exception e) {
            logger.error("获取ioc的总数失败。", e);
        }

        if(dbIocCount > -1){
            try {
                initActiveTimeRange(jedis);
                boolean modified = (dbIocCount != redisIocCount || iocActiveBetweenCount(stime, etime) > 0);
                if(modified){
                    jedis.set(last_ioccount_key, String.valueOf(dbIocCount));
                    logger.info("检测到ioc存在更改。");
                }
                return modified;
            } catch (Exception e) {
                logger.error(String.format("获取时间范围[%s, %s]内的有效ioc失败。", stime, etime), e);
            }
        }

        return false;
    }
}
