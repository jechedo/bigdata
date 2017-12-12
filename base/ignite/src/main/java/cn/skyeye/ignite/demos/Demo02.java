package cn.skyeye.ignite.demos;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCluster;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterNode;

import java.util.Collection;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/8 17:08
 */
public class Demo02 {

    public static void main(String[] args) {
        System.out.println("***55");
        //demo002();
        demo001();
    }

    private static void demo001() {
        try {
            Ignite ignite = Ignition.start("config/example-ignite.xml");

            IgniteCluster cluster = ignite.cluster();
            Collection<ClusterNode> nodes = cluster.nodes();
            nodes.forEach(node ->{
                System.out.println(node.id());
                System.out.println(node.hostNames());
                System.out.println(node.metrics());
                System.out.println(node.isClient());
                System.out.println(node.version());
                System.out.println("**********************");
            });


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void demo002() {
        try {
            Ignite ignite = Ignition.start("config/example-ignite.xml");
            IgniteCache<Integer, String> cache = ignite.getOrCreateCache("myCacheName");
            // Store keys in cache (values will end up on different cache nodes).
            for (int i = 0; i < 10; i++)
                cache.put(i, Integer.toString(i));

            for (int i = 0; i < 10; i++)
                System.out.println("Got [key=" + i + ", val=" + cache.get(i) + ']');
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
