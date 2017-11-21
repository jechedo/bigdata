package cn.skyeye.norths;

import cn.skyeye.norths.events.DataEventDisruptor;
import cn.skyeye.norths.syslog.Sysloger;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/11/20 21:01
 */
public class NorthContext {

    private Sysloger sysloger;
    private DataEventDisruptor dataEventDisruptor;

    private NorthContext(){
        this.sysloger = new Sysloger();
        this.dataEventDisruptor = new DataEventDisruptor(sysloger);
    }

    private void start(){

    }

    public static void main(String[] args) {
        NorthContext northContext = new NorthContext();
        northContext.start();
    }

}
