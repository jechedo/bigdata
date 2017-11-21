package cn.skyeye.norths;

import cn.skyeye.norths.sources.DataSource;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/20 21:01
 */
public class NorthContext {

    private List<DataSource> dataSources;

    private NorthContext(){
        this.dataSources = Lists.newArrayList();
    }


    public static void main(String[] args) {

    }

}
