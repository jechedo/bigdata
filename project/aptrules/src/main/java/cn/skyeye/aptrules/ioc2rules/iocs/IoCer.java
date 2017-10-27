package cn.skyeye.aptrules.ioc2rules.iocs;

import cn.skyeye.aptrules.ARConf;
import cn.skyeye.aptrules.ARContext;
import cn.skyeye.aptrules.ioc2rules.extracters.IoCAsRuleCloudExtracter;
import cn.skyeye.aptrules.ioc2rules.extracters.IoCAsRuleDBExtracter;
import cn.skyeye.aptrules.ioc2rules.extracters.IoCAsRuleExtracter;
import cn.skyeye.aptrules.ioc2rules.iocs.stores.IoCCloudStore;
import cn.skyeye.aptrules.ioc2rules.iocs.stores.IoCSQLiteStore;
import cn.skyeye.aptrules.ioc2rules.iocs.stores.IoCStore;
import cn.skyeye.aptrules.ioc2rules.rules.VagueRule;
import cn.skyeye.common.hash.Md5;
import com.google.common.hash.Hashing;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Description:
 *     iocs表的相关操作
 * @author LiXiaoCong
 * @version 2017/10/11 19:03
 */
public class IoCer {
    private final Logger logger = Logger.getLogger(IoCer.class);

    private ARConf arConf;
    private IoCStore iocDBStore;
    private IoCStore iocNetStore;

    public IoCer() {
        this.arConf = ARContext.get().getArConf();
        this.iocDBStore = new IoCSQLiteStore(arConf);
        this.iocNetStore = new IoCCloudStore(arConf);
    }



    public List<VagueRule> listIoCAsRuleInDB(){
        IoCAsRuleExtracter extracter = new IoCAsRuleDBExtracter();
        iocDBStore.extractIoCAsRules(extracter);
        logger.info(String.format("查询数据库中的ioc并转换成rule成功，iocCount = %s, ruleCount = %s",
                extracter.getIocCount(), extracter.getEffectIocCount()));
        return extracter.getRules();
    }

    public List<VagueRule> listIoCAsRuleInNet(){
        IoCAsRuleExtracter extracter = new IoCAsRuleCloudExtracter();
        iocDBStore.extractIoCAsRules(extracter);
        logger.info(String.format("查询网络中的ioc并转换成rule成功，iocCount = %s, ruleCount = %s",
                extracter.getIocCount(), extracter.getEffectIocCount()));
        return extracter.getRules();
    }

    public void updateDBStoreStatus(boolean syncSuccess){
        iocDBStore.updateStatus(syncSuccess);
    }

    public void updateNetStoreStatus(boolean syncSuccess){
        iocNetStore.updateStatus(syncSuccess);
    }

    public static void main(String[] args) throws Exception {
        //IoCer ioCer = new IoCer("D:/demo/skyeye.db");
        //ioCer.extractIoCAsRules();

        String localhost = Hashing.md5().hashString("localhost").toString();
        //System.out.println(Md5.Md5_16("localhost"));
        System.out.println(Md5.Md5_32("www.vswlczrnm.com"));

        //System.out.println(localhost);
    }

}
