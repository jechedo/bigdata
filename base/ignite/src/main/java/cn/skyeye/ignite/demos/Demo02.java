package cn.skyeye.ignite.demos;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteCallable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/8 17:08
 */
public class Demo02 {

    public static void main(String[] args) {
        System.out.println("***44");
        demo002();
    }

    private static void demo001() {
        URL resource = Demo02.class.getResource("config/example-ignite.xml");
        try {
            Ignite ignite = Ignition.start("config/example-ignite.xml");
            Collection<IgniteCallable<Integer>> calls = new ArrayList<>();
            // Iterate through all the words in the sentence and create Callable jobs.
            for (final String word : "Count characters using callable".split(" "))
                calls.add(word::length);
            // Execute collection of Callables on the grid.
            Collection<Integer> res = ignite.compute().call(calls);
            // Add up all the results.
            int sum = res.stream().mapToInt(Integer::intValue).sum();
            System.out.println("Total number of characters is '" + sum + "'.");
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
