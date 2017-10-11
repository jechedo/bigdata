package cn.skyeye.resources;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Description:
 *
 *   配置文件读取器
 *
 *  读取的环境分为： PRO:生产环境 ，DEV 研发环境 ， TEST：测试环境
 *  读取的级别分为： EXTERNAL：只读外部配置 ， INTERNAL：只读内部配置 ， EXTERNAL_INTERNAL：先读外部 ，外部不存在的情况下读内部 ， INTERNAL_EXTERNAL：和EXTERNAL_INTERNAL相反
 *  外部配置的存放父目录为：/usr/conf
 *  传入的配置参数格式为: [/project_dir]/[propertiesName]  propertiesName不需要后缀
 *  例如：
 *
 *    List<String> confs =   Lists.newArrayList();
 *    confs.add(/test/test);
 *    new Resources(confs, Env.DEV)
 *
 *    #CompositeConfiguration 类读取配置文件，相同的字段名称的值会累加
 *
 *   在接收到参数后 读取内部配置 使用成 /test/test-dev.properties， 外部则使用 /usr/conf/test/test-dev.properties
 *
 *   注意:
 *        1. 传入多个配置文件 要确保没有字段重复
 *        2. 默认优先从外部目录 /usr/conf/ 中读取
 *        3. /usr/conf/  不存在指定的配置为文件 则从项目的 resources 目录中读取
 *
 * @author LiXiaoCong
 * @version 2017/4/6 15:19
 */
public class Resources extends ConfigDetail{

    public enum Env{
        PRO {
            @Override
            public String path(String conf) {
                return String.format("%s-pro.properties", conf);
            }
        },
        DEV {
            @Override
            public String path(String conf) {
                return String.format("%s-dev.properties", conf);
            }
        },
        TEST {
            @Override
            public String path(String conf) {
                return String.format("%s-test.properties", conf);
            }
        },
        NONE {
            @Override
            public String path(String conf) {
                return String.format("%s.properties", conf);
            }
        };

        public abstract String path(String conf);

        public static Env newInstance(String envName){

            switch (envName.toUpperCase()){
                case "PRO": return PRO;
                case "DEV": return DEV;
                case "TEST": return TEST;
                case "NONE": return NONE;
            }
            return null;
        }

    }
    public enum ReadLevel{EXTERNAL, INTERNAL, EXTERNAL_INTERNAL, INTERNAL_EXTERNAL}

    private final String EXTERNAL_RESOURCE_DIR = "/opt/work/configs";

    private Env env;
    private ReadLevel level;
    private List<String> losts ;
    private Map<String, PropEntry> props;
    private boolean igoreNotExists;
    private Map<String, Exception> exceptionMap;

    public Resources(Env env, String ... confs) throws Exception{
        this(Lists.newArrayList(confs), env, false);
    }

    public Resources(List<String> confs, Env env) throws Exception{
        this(confs, env, false);
    }

    public Resources(List<String> confs, Env env, boolean igoreNotExists) throws Exception{
        this(confs, env, igoreNotExists, ReadLevel.EXTERNAL_INTERNAL);
    }

    public Resources(List<String> confs, Env env, boolean igoreNotExists, ReadLevel level) throws Exception{
        super();

        if(confs == null || confs.isEmpty()){
            throw new IllegalArgumentException("confs不能为空。");
        }

        if(env == null){
            throw new IllegalArgumentException("env不能为空。");
        }

        this.env = env;
        this.level = level == null ? ReadLevel.EXTERNAL_INTERNAL : level;
        this.igoreNotExists = igoreNotExists;
        if(igoreNotExists){
            this.losts = Lists.newArrayList();
        }

        this.props = Maps.newHashMap();
        this.exceptionMap = Maps.newHashMap();

        for (String conf : confs) read(env.path(conf));
    }

    public PropEntry getPropeties(String conf){
        return props.get(env.path(conf));
    }

    private void read(String conf) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(conf), "配置文件路径不能为空。");
        PropEntry prop;

        boolean isNetConfig = false;
        try {
            URL urlStr = new URL(conf);
            HttpURLConnection connection = (HttpURLConnection) urlStr.openConnection();
            if (connection.getResponseCode() == 200) isNetConfig = true;
        }catch (Exception e){}


        String externalConf = isNetConfig ? conf : getExternalConf(conf);

        switch (level) {
            case EXTERNAL_INTERNAL:
                prop = readProp(externalConf, isNetConfig);
                if (prop.value == null && !isNetConfig) prop = readProp(conf, isNetConfig);
                checkProp(conf, externalConf, prop);
                break;
            case INTERNAL_EXTERNAL:
                prop = readProp(conf, isNetConfig);
                if (prop.value == null && !isNetConfig) prop = readProp(externalConf, isNetConfig);
                checkProp(conf, externalConf, prop);
                break;
            case EXTERNAL:
                prop = readProp(externalConf, isNetConfig);
                checkProp(conf, externalConf, prop);
                break;
            case INTERNAL:
                prop = readProp(conf, isNetConfig);
                checkProp(conf, externalConf, prop);
                break;

        }
    }

    public List<String> getLostConf(){
        return losts;
    }

    public Env getEnv() {
        return env;
    }

    public ReadLevel getLevel() {
        return level;
    }

    public boolean isIgoreNotExists() {
        return igoreNotExists;
    }

    public Map<String, Exception> getExceptionMap() {
        return exceptionMap;
    }

    private void checkProp(String conf, String externalConf, PropEntry entry) throws Exception {

        Properties prop = entry.getValue();
        if(prop == null){
            if(!igoreNotExists){
                Exception exception = new Exception(String.format("读取配置文件 %s 失败。", conf));
                Exception e = exceptionMap.get(externalConf);
                if(e != null) exception.addSuppressed(e);
                e = exceptionMap.get(conf);
                if(e != null) exception.addSuppressed(e);
                throw exception;
            }else {
                losts.add(conf);
            }
        }else {
            props.put(conf, entry);
            readKV(prop);
        }
    }

    private void readKV(Properties prop){
        for(Map.Entry<Object,Object> entry : prop.entrySet()){
            configMap.put((String)entry.getKey(), (String)entry.getValue());
        }
    }

    private String getExternalConf(String conf){
        File file = new File(EXTERNAL_RESOURCE_DIR, conf);
        return file.getAbsolutePath();
    }

    private PropEntry readProp(String conf, boolean isNetConfig){

        Properties prop = null;
        InputStream is = null;
        InputStreamReader reader = null;
        try {

            URL url;
            if(isNetConfig) {
                url = new URL(conf);
            }else {
                url = Resources.class.getResource(conf);
                if (url == null) url = new File(conf).toURI().toURL();
            }

            if(url != null){
                is = url.openStream();
                if(is == null){
                    throw new IllegalArgumentException(
                            String.format("读取取配置文件 %s 失败，请检查地址是否正确。", conf));
                }else{
                    prop = new Properties();
                    reader = new InputStreamReader(is, "UTF-8");
                    prop.load(reader);
                }
            }
        }catch (Exception e){
            exceptionMap.put(conf, e);
        }finally {
            if(reader != null) Closeables.closeQuietly(reader);
            if(is != null) Closeables.closeQuietly(is);
        }

        return new PropEntry(conf, prop);
    }

    public class PropEntry implements Serializable{

        private String key;
        private Properties value;

        private PropEntry(String conf, Properties prop){
            this.key = conf;
            this.value = prop;
        }

        public String getKey() {
            return key;
        }

        public Properties getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("PropEntry[key = %s, value = %s]", key, value);
        }
    }

    public static void main(String[] args) throws Exception {
        String config = "http://172.16.1.112/conf/hbase/hbase-server-config";
        Resources resources = new Resources(Lists.newArrayList(config), Env.DEV);

        System.out.println(resources.getPropeties(config));
    }
}
