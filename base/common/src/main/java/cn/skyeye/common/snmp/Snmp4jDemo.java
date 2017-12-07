package cn.skyeye.common.snmp;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

/**
 * Description:
 *
 * @author LiXiaoCong
 * @version 2017/12/6 16:56
 */
public class Snmp4jDemo {

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

        OIDDetail ssCpuIdle = new OIDDetail("1.3.6.1.4.1.2021.11.11", "ssCpuIdle", "空闲CPU百分比");
        OIDDetail ssCpuSystem = new OIDDetail("1.3.6.1.4.1.2021.11.10", "ssCpuSystem", "内核(system)进程使用CPU百分比");
        OIDDetail ssCpuUser = new OIDDetail("1.3.6.1.4.1.2021.11.9", "ssCpuUser", "用户进程使用CPU百分比");
        OIDDetail laLoadInt1 = new OIDDetail("1.3.6.1.4.1.2021.10.1.5.1", "laLoadInt.1", "1分钟平均CPU负载");
        OIDDetail laLoadInt2 = new OIDDetail("1.3.6.1.4.1.2021.10.1.5.2", "laLoadInt.2", "5分钟平均CPU负载");
        OIDDetail laLoadInt3 = new OIDDetail("1.3.6.1.4.1.2021.10.1.5.3", "laLoadInt.3", "15分钟平均CPU负载");

        OIDDetail memTotalFree = new OIDDetail("1.3.6.1.4.1.2021.4.11", "memTotalFree", "空闲内存(kB)");

        OIDDetail hrSystemProcesses = new OIDDetail("1.3.6.1.2.1.25.1.6", "hrSystemProcesses", "系统启动进程个数");
        OIDDetail hrSystemNumUsers = new OIDDetail("1.3.6.1.2.1.25.1.5", "hrSystemNumUsers", "系统登录用户个数");

        OIDDetail dskAvail = new OIDDetail("1.3.6.1.4.1.2021.9.1.7", "dskAvail", "可用磁盘空间(kB)");
        OIDDetail dskPercent = new OIDDetail("1.3.6.1.4.1.2021.9.1.9", "dskPercent", "磁盘使用百分比");

        OIDDetail diskIOLA1 = new OIDDetail("1.3.6.1.4.1.2021.13.15.1.1.9", "diskIOLA1", "磁盘1分钟平均负载");
        OIDDetail diskIOLA5 = new OIDDetail("1.3.6.1.4.1.2021.13.15.1.1.10", "diskIOLA5", "磁盘10分钟平均负载");
        OIDDetail diskIOLA15 = new OIDDetail("1.3.6.1.4.1.2021.13.15.1.1.11", "diskIOLA15", "磁盘15分钟平均负载");


    }

    private static class OIDDetail{
        private String oid;
        private String name;
        private String desc;

        public OIDDetail(String oid, String name, String desc) {
            this.oid = oid;
            this.name = name;
            this.desc = desc;
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
