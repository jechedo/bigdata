package cn.skyeye.common.snmp;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/6 16:56
 */
public class Snmp4js {

    public final static OIDDetail ssCpuIdle = new OIDDetail("1.3.6.1.4.1.2021.11.11", "ssCpuIdle", "空闲CPU百分比");
    public final static OIDDetail ssCpuSystem = new OIDDetail("1.3.6.1.4.1.2021.11.10", "ssCpuSystem", "内核(system)进程使用CPU百分比");
    public final static OIDDetail ssCpuUser = new OIDDetail("1.3.6.1.4.1.2021.11.9", "ssCpuUser", "用户进程使用CPU百分比");
    public final static OIDDetail laLoadInt1 = new OIDDetail("1.3.6.1.4.1.2021.10.1.5.1", "laLoadInt.1", "1分钟平均CPU负载");
    public final static OIDDetail laLoadInt2 = new OIDDetail("1.3.6.1.4.1.2021.10.1.5.2", "laLoadInt.2", "5分钟平均CPU负载");
    public final static OIDDetail laLoadInt3 = new OIDDetail("1.3.6.1.4.1.2021.10.1.5.3", "laLoadInt.3", "15分钟平均CPU负载");
    public final static OIDDetail memTotalFree = new OIDDetail("1.3.6.1.4.1.2021.4.11", "memTotalFree", "空闲内存(kB)");
    public final static OIDDetail hrSystemProcesses = new OIDDetail("1.3.6.1.2.1.25.1.6", "hrSystemProcesses", "系统启动进程个数");
    public final static OIDDetail hrSystemNumUsers = new OIDDetail("1.3.6.1.2.1.25.1.5", "hrSystemNumUsers", "系统登录用户个数");
    public final static OIDDetail dskAvail = new OIDDetail("1.3.6.1.4.1.2021.9.1.7", "dskAvail", "可用磁盘空间(kB)");
    public final static OIDDetail dskPercent = new OIDDetail("1.3.6.1.4.1.2021.9.1.9", "dskPercent", "磁盘使用百分比");
    public final static OIDDetail diskIOLA1 = new OIDDetail("1.3.6.1.4.1.2021.13.15.1.1.9", "diskIOLA1", "磁盘1分钟平均负载");
    public final static OIDDetail diskIOLA5 = new OIDDetail("1.3.6.1.4.1.2021.13.15.1.1.10", "diskIOLA5", "磁盘10分钟平均负载");
    public final static OIDDetail diskIOLA15 = new OIDDetail("1.3.6.1.4.1.2021.13.15.1.1.11", "diskIOLA15", "磁盘15分钟平均负载");

    private final Logger logger = Logger.getLogger(Snmp4js.class);

    private volatile static Snmp4js snmp4js;

    private SnmpManager snmpManager;

    private Snmp4js(SnmpManager snmpManager){
        this.snmpManager = snmpManager;
        this.snmpManager.closeAsyn();
    }

    public static Snmp4js get(SnmpManager snmpManager){
        Preconditions.checkNotNull(snmpManager, "snmpManager不能为null。");
        if(snmp4js == null){
            synchronized (Snmp4js.class){
                if (snmp4js == null)
                    snmp4js =new Snmp4js(snmpManager);
            }
        }
        return snmp4js;
    }

    public int getSsCpuIdle(){
        PDU pdu = new PDU();
        pdu.setType(PDU.GETBULK);
        pdu.add(new VariableBinding(ssCpuIdle.newOID()));
        pdu.setMaxRepetitions(1);
        AtomicInteger res = new AtomicInteger(0);
        try {
            snmpManager.sendMessage(pdu, new SnmpManager.ResponseHandler() {
                @Override
                public void extractResponse(ResponseEvent event) {
                    res.set(event.getResponse().get(0).getVariable().toInt());
                }
            });
        } catch (IOException e) {
            logger.error(String.format("获取%s失败。", ssCpuIdle), e);
            res.set(-1);
        }
        return res.get();
    }


    public static void main(String[] args) throws IOException {
        TransportMapping transportMapping = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transportMapping);
        snmp.listen();

        Address address = GenericAddress.parse("udp:192.168.66.66/161");
        CommunityTarget target = new CommunityTarget(address, new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);

       /* // walk GETNEXT or GETBULK
        TableUtils utils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETBULK));
        // only for GETBULK, set max-repetitions, default is 10
        utils.setMaxNumRowsPerPDU(5);
        OID[] columnOids = new OID[] {new OID("1.3.6.1.2.1.25.1.5")};
        // If not null, all returned rows have an index in a range (lowerBoundIndex, upperBoundIndex]
        //lowerBoundIndex,upperBoundIndex都为null时返回所有的叶子节点。
        // 必须具体到某个OID,,否则返回的结果不会在(lowerBoundIndex, upperBoundIndex)之间
        List<TableEvent> l = utils.getTable(target, columnOids, null,null);
        for (TableEvent e : l) {
            Lists.newArrayList(e.getColumns()).forEach(v -> System.out.println(v.getVariable().toString()));
        }*/

        PDU pdu = new PDU();
        pdu.setType(PDU.GETBULK);
        OID oid = new OID("1.3.6.1.4.1.2021.10.1.5.1");
        pdu.add(new VariableBinding(oid));
        pdu.setMaxRepetitions(1);

        ResponseEvent response = snmp.send(pdu, target);

        PDU pduResult = response.getResponse();
        pduResult.getVariableBindings().forEach(ver -> System.out.println(ver.getVariable().toString()));



    }

    public static class OIDDetail{
        private String oid;
        private String name;
        private String desc;

        public OIDDetail(String oid,
                         String name,
                         String desc) {
            this.oid = oid;
            this.name = name;
            this.desc = desc;
        }

        public OID newOID(){
            return new OID(oid);
        }

        public String getOid() {
            return oid;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("OID{");
            sb.append("oid='").append(oid).append('\'');
            sb.append(", name='").append(name).append('\'');
            sb.append(", desc='").append(desc).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
