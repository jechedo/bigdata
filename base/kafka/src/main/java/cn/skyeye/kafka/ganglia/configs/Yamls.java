package cn.skyeye.kafka.ganglia.configs;

import cn.skyeye.kafka.ganglia.monitors.zk.ZookeeperMonitor;
import com.google.common.base.Preconditions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Description:
 *    yaml配置文件读取
 * @author LiXiaoCong
 * @version 2017/11/3 10:49
 */
public class Yamls {

    private Yamls(){}

    public static <T> T load(String name, Class<T> clazz)  {
        InputStream resourceAsStream = ZookeeperMonitor.class.getResourceAsStream(name);
        if(resourceAsStream == null){
            try {
                resourceAsStream = new FileInputStream(new File("/usr/conf", name));
            } catch (FileNotFoundException e) {}
        }
        Preconditions.checkNotNull(resourceAsStream, "配置不存在。");

        Yaml yaml = new Yaml();
        return yaml.loadAs(resourceAsStream, clazz);
    }

}
