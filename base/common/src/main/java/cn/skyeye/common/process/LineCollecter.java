package cn.skyeye.common.process;

import java.util.Collection;

/**
 * Description:
 *
 *      进程打印数据 按行收集的抽象类
 *
 * @author LiXiaoCong
 * @version 1.0
 * @date 2016/10/24 14:47
 */
public abstract class LineCollecter<T> implements LineExtracter {

    private Collection<T> collection;

    public LineCollecter(Collection<T> collection){
        this.collection = collection;
    }

    public void extract(boolean isError, String line) {
        T t = extractRowStr(isError, line);
        if(t != null) collection.add(t);
    }

    protected abstract T extractRowStr(boolean isError, String line);
}
