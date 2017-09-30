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
public abstract class ProcessRowCollecter<T> implements ProcessRowExtracter {

    private Collection<T> collection;

    public ProcessRowCollecter(Collection<T> collection){
        this.collection = collection;
    }

    public void extractRowData(String rowData) {
        T t = extractRowStr(rowData);
        if(t != null) collection.add(t);
    }

    protected abstract T extractRowStr(String rowData);
}
