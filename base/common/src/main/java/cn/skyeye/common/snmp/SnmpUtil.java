package cn.skyeye.common.snmp;

import java.io.IOException;
import java.text.DecimalFormat;  
import java.util.ArrayList;  
import java.util.HashMap;  
import java.util.List;  
import java.util.Map;  
import java.util.Vector;  
  
import org.apache.commons.lang.StringUtils;  
import org.apache.commons.logging.Log;  
import org.apache.commons.logging.LogFactory;  
import org.snmp4j.CommunityTarget;  
import org.snmp4j.PDU;  
import org.snmp4j.Snmp;  
import org.snmp4j.TransportMapping;  
import org.snmp4j.event.ResponseEvent;  
import org.snmp4j.mp.SnmpConstants;  
import org.snmp4j.smi.Address;  
import org.snmp4j.smi.GenericAddress;  
import org.snmp4j.smi.OID;  
import org.snmp4j.smi.OctetString;  
import org.snmp4j.smi.VariableBinding;  
import org.snmp4j.transport.DefaultUdpTransportMapping;  
  
/** 
 * 使用snmp4j协议取得系统相关信息 注:这么多迭代方法是因为：监控snmp的时候异步迭代会出现问题 另外监控设置为synchronized同步又不合理 
 *  
 * @author linwang 
 * @createTime 2013-12-26 
 */  
public class SnmpUtil {

    Log log = LogFactory.getLog(SnmpUtil.class);

    private static Snmp snmp = null;

    private static TransportMapping transport = null;

    private static Address targetAddress = null;

    private static PDU responsepdu = new PDU();

    private static CommunityTarget target = null;

    private static String ipStr = "";

    /**
     * 设置监控服务器地址
     *
     * @param ipStr
     * @author linwang
     * @createTime 2014-6-30
     */
    public static void setIpStr(String ipStr) {

        SnmpUtil.ipStr = "udp:" + ipStr + "/161";

        try {
            initComm();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前服务器监控的IP地址
     *
     * @return
     * @author linwang
     * @createTime 2014-7-8
     */
    public static String getIpStr() {

        if (StringUtils.isNotEmpty(ipStr) && ipStr.indexOf(":") != -1) {
            ipStr = ipStr.split(":")[1].split("/161")[0];
        }

        return ipStr;
    }

    /**
     * 初始化snmp监控信息
     *
     * @throws IOException
     * @author linwang
     * @createTime 2014-6-30
     */
    public static synchronized void initComm() throws IOException {

        // 设置Agent方传输协议并打开监听  
        if (null != transport)
            transport.close();
        if (null != snmp)
            snmp.close();
        transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        snmp.listen();
        if (!transport.isListening())
            transport.listen();

        // 设置target  
        target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        targetAddress = GenericAddress.parse(ipStr);
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version1);
    }

    /**
     * 获取CPU利用率 百分比
     *
     * @return
     * @throws IOException
     * @author linwang
     * @createTime 2014-6-30
     */
    public static Map<String, Object> getCpuUse() throws Exception {
        Map<String, Object> temMap = new HashMap<String, Object>();
        try {
            PDU responsepdu = new PDU();
            responsepdu.add(new VariableBinding(new OID(
                    "1.3.6.1.4.1.9600.1.1.5.1.5.6.95.84.111.116.97.108")));
            responsepdu.setType(PDU.GET);
            ResponseEvent response = snmp.send(responsepdu, target);

            if (response.getResponse() == null) {
            } else {
                responsepdu = response.getResponse();  
                /* 
                 * //System.out.println("Received Value is: "+ 
                 * responsepdu.getVariableBindings()); 
                 */
            }

            String tem = responsepdu.getVariableBindings().toString();
            int temInt = 0;
            if (tem.indexOf("Null") != -1) {
                temInt = -1;
            } else {
                tem = tem.split("=")[1].split("]")[0].trim();
                if (isNum(tem)) {
                    temInt = Integer.parseInt(tem);
                }
            }

            temMap.put("data", "");
            temMap.put("cpuLoad", temInt);

            System.out.println("CPU利用率：" + temInt);
        } catch (Exception e) {
            throw new IllegalArgumentException("获取" + ipStr + "服务器的CPU失败");
        }
        return temMap;
    }

    /**
     * 获取内存利用率
     *
     * @return
     * @throws IOException
     * @author linwang
     * @createTime 2014-6-30
     */
    public static Map<String, Object> getMemoryUse() throws Exception {
        Map<String, Object> temMap = new HashMap<String, Object>();
        try {

            int total = 0;
            int used = 0;
            int temInt = 0;

            PDU responsepdu = new PDU();
            responsepdu
                    .add(new VariableBinding(new OID("1.3.6.1.2.1.25.2.2.0")));
            responsepdu.setType(PDU.GET);
            ResponseEvent response = snmp.send(responsepdu, target);
            if (response.getResponse() == null) {
            } else {
                responsepdu = response.getResponse();
                // //System.out.println("全部的: "+  
                // responsepdu.getVariableBindings());  
                String tem = responsepdu.getVariableBindings().toString();
                total = Integer
                        .parseInt(tem.split("=")[1].split("]")[0].trim());
            }
            responsepdu.clear();
            responsepdu.add(new VariableBinding(new OID(
                    "1.3.6.1.4.1.9600.1.1.2.2.0")));
            responsepdu.setType(PDU.GET);
            response = snmp.send(responsepdu, target);
            if (response.getResponse() == null) {
            } else {
                responsepdu = response.getResponse();
                // //System.out.println("已用的: " +  
                // responsepdu.getVariableBindings());  
                String tem = responsepdu.getVariableBindings().toString();
                used = Integer.parseInt(tem.split("=")[1].split("]")[0].trim());
            }
            if (total == 0) {
                temInt = 0;
            }
            if (total != 0) {
                temInt = (total - used) * 100 / total;
                temInt = Math.abs(temInt);
            }

            // System.out.println("内存利用率：" + temInt);

            temMap.put("data", "");
            temMap.put("memoryLoad", temInt);
        } catch (Exception e) {
            throw new IllegalArgumentException("获取" + ipStr + "服务器的内存失败");
        }
        return temMap;
    }

    /**
     * 取得硬盘容量
     *
     * @return
     * @throws IOException
     * @author linwang
     * @createTime 2014-6-30
     */
    public static List<Map<String, Object>> getHardpan() throws IOException {

        // 1.3.6.1.2.1.25.2.3.1.3 储存类型（硬盘（c、d..),内存、虚拟内存）  
        List<String> labelss = new ArrayList<String>();
        // 1.3.6.1.2.1.25.2.3.1.4分配单元  
        List<Integer> units = new ArrayList<Integer>();
        // 1.3.6.1.2.1.25.2.3.1.5 存储容量  
        List<Integer> size = new ArrayList<Integer>();
        // 1.3.6.1.2.1.25.2.3.1.6已使用容量  
        List<Integer> used = new ArrayList<Integer>();

        responsepdu = new PDU();
        responsepdu.add(new VariableBinding(new OID("1.3.6.1.2.1.25.2.3.1.3")));
        responsepdu.setType(PDU.GETNEXT);
        getHardpanIteration(labelss, "1.3.6.1.2.1.25.2.3.1.4.1", 1);

        responsepdu.clear();
        responsepdu.add(new VariableBinding(new OID("1.3.6.1.2.1.25.2.3.1.4")));
        getHardpanIteration(units, "1.3.6.1.2.1.25.2.3.1.5.1", 2);

        responsepdu.clear();
        responsepdu.add(new VariableBinding(new OID("1.3.6.1.2.1.25.2.3.1.5")));
        getHardpanIteration(size, "1.3.6.1.2.1.25.2.3.1.6.1", 2);

        responsepdu.clear();
        responsepdu.add(new VariableBinding(new OID("1.3.6.1.2.1.25.2.3.1.6")));
        getHardpanIteration(used, "1.3.6.1.2.1.25.2.3.1.7.1", 2);

        List<Map<String, Object>> temList = new ArrayList<Map<String, Object>>();

        int i = 0;
        for (String label : labelss) {
            Map<String, Object> temMap = new HashMap<String, Object>();
            if ("Virtual Memory".equals(label)
                    || "Physical Memory".equals(label) || size.get(i) == 0) {
                i++;
                continue;
            }
            String usedPercent = "";
            String total = "";
            String usedStr = "";
            String freeStr = "";
            int a = used.get(i);// 已使用容量  
            int b = size.get(i);// 存储容量  
            int u = units.get(i);// 分配单元  

            // 输出盘符名称  
            temMap.put("name", label.split(":")[0]);

            // 输出盘符总容量  
            float c = (float) b * (float) u / 1024 / 1024 / 1024;
            DecimalFormat df = new DecimalFormat("0");// 格式化小数，不足的补0  
            total = df.format(c);// 返回的是String类型的  
            temMap.put("total", total);

            // 输出盘符已使用百分笔  
            if (size.get(i) != 0) {
                // java中两个整数相除得到小数点并保留两位小数的方法--是否选择四舍五入  
                c = (float) a / (float) b * 100;
                df = new DecimalFormat("0.00");// 格式化小数，不足的补0  
                usedPercent = df.format(c);// 返回的是String类型的  
            }
            temMap.put("usedPercent", usedPercent);

            // 输出盘符已使用  
            c = (float) a * (float) u / 1024 / 1024 / 1024;
            df = new DecimalFormat("0.00");// 格式化小数，不足的补0  
            usedStr = df.format(c);// 返回的是String类型的  
            temMap.put("used", usedStr);

            // 输出盘符未使用容量  
            c = ((float) ((b - a)) / 1024 / 1024 / 1024 * u);
            freeStr = df.format(c);// 返回的是String类型的  
            temMap.put("free", freeStr);
            i++;
            temList.add(temMap);
        }

        // System.out.println("获取的硬盘信息：" + json);  

        return temList;
    }

    /**
     * 迭代获取各盘信息
     *
     * @param listType 硬盘相关信息List
     * @param endOid   终止OID字符串
     * @param flag     flag 1:取字符串list；2：取整数list
     * @throws IOException
     * @author linwang
     * @createTime 2014-7-12
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void getHardpanIteration(List listType, String endOid,
                                           int flag) throws IOException {

        ResponseEvent response = snmp.getBulk(responsepdu, target);

        if (response.getResponse() == null) {
            return;
        } else {
            Vector<? extends VariableBinding> recVBs = response.getResponse().getVariableBindings();
            String oid = recVBs.elementAt(0).getOid().toString();
            if (endOid.equals(oid)) {
                return;
            }
            if (!oid.contains(endOid) && (flag == 3 || flag == 4)) {
                return;
            }
            responsepdu = response.getResponse();
            // System.out.println("【获取硬盘信息】Received Value is: " +  
            // responsepdu.getVariableBindings());  
            String tem = responsepdu.getVariableBindings().toString();
            tem = tem.split("=")[1].split("]")[0].trim();
            if (flag == 2 || flag == 3) {
                if (isNum(tem)) {
                    listType.add(Integer.parseInt(tem));
                }
            } else {
                listType.add(tem);
            }

            getHardpanIteration(listType, endOid, flag);
        }
    }

    /**
     * 获取进程数量
     *
     * @return
     * @throws IOException
     * @author linwang
     * @createTime 2014-1-2
     */
    public static Map<String, Object> GetProcessesCount() throws IOException {

        PDU responsepdu = new PDU();
        Map<String, Object> temMap = new HashMap<String, Object>();
        try {
            responsepdu
                    .add(new VariableBinding(new OID("1.3.6.1.2.1.25.1.6.0")));
            responsepdu.setType(PDU.GET);
            ResponseEvent response = snmp.send(responsepdu, target);
            if (response.getResponse() == null) {
                temMap.put("success", "false");
                temMap.put("msg", "0");
            } else {
                responsepdu = response.getResponse();
            }
            String tem = responsepdu.getVariableBindings().toString();
            tem = tem.split("=")[1].split("]")[0].trim();
            temMap.put("success", "true");
            temMap.put("ProcessesCount", tem);
            // System.out.println("进程数量" + tem);  
        } catch (Exception e) {
            e.printStackTrace();
            temMap.put("success", "false");
            temMap.put("msg", e.getMessage());
            // System.out.println("获取进程失败");  
        }

        return temMap;
    }

    /**
     * 获取进程列表
     *
     * @return
     * @throws IOException
     * @author linwang
     * @createTime 2014-1-2
     */
    public static List<Map<String, Object>> GetProcessesRunInfo()
            throws IOException {
        List<Map<String, Object>> temMapList = new ArrayList<Map<String, Object>>();
        Map<String, Object> temMap = new HashMap<String, Object>();
        List<String> runNameList = new ArrayList<String>();
        List<String> runPathList = new ArrayList<String>();
        List<String> runTypeList = new ArrayList<String>();
        List<String> perfMemList = new ArrayList<String>();

        // 创建 PDU  
        PDU pdu = new PDU();
        pdu.setType(PDU.GETBULK);
        responsepdu = pdu;
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.25.4.2.1.2")));
        getProcessesListIteration(runNameList, "1.3.6.1.2.1.25.4.2.1.2");
        responsepdu.clear();
        responsepdu.add(new VariableBinding(new OID("1.3.6.1.2.1.25.4.2.1.4")));
        getProcessesListIteration(runPathList, "1.3.6.1.2.1.25.4.2.1.4");
        responsepdu.clear();
        responsepdu.add(new VariableBinding(new OID("1.3.6.1.2.1.25.4.2.1.6")));
        getProcessesListIteration(runTypeList, "1.3.6.1.2.1.25.4.2.1.6");
        responsepdu.clear();
        responsepdu.add(new VariableBinding(new OID("1.3.6.1.2.1.25.5.1.1.2")));
        getProcessesListIteration(perfMemList, "1.3.6.1.2.1.25.5.1.1.2");

        int i = 0;
        if (runNameList.size() > 0 && runPathList.size() > 0
                && perfMemList.size() > 0 && runTypeList.size() > 0) {
            for (String kvp : runNameList) {
                temMap = new HashMap<String, Object>();
                temMap.put("runName", kvp);
                temMap.put("runPath", runPathList.get(i));
                temMap.put("runType", runTypeList.get(i));
                temMap.put("perfMem", perfMemList.get(i));
                temMapList.add(temMap);
                i++;
            }
        }
        return temMapList;
    }

    /**
     * 迭代获取进程列表
     *
     * @param listType 硬盘相关信息List
     * @param endOid   终止OID字符串
     * @throws IOException
     * @author linwang
     * @createTime 2014-7-12
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void getProcessesListIteration(List listType, String endOid)
            throws IOException {
        ResponseEvent response = snmp.getBulk(responsepdu, target);

        if (response.getResponse() == null) {
            return;
        } else {
            Vector<? extends VariableBinding> recVBs = response.getResponse().getVariableBindings();
            String oid = recVBs.elementAt(0).getOid().toString();

            // 传入终止OID是父节点，如果当前迭代的oid不包含父节点前缀则退出迭代  
            if (!oid.contains(endOid)) {
                return;
            }
            responsepdu = response.getResponse();
            // System.out.println("【获取进程列表】Received Value is: " +  
            // responsepdu.getVariableBindings());  
            String tem = responsepdu.getVariableBindings().toString();
            tem = tem.split("=")[1].split("]")[0].trim();
            listType.add(tem);

            getProcessesListIteration(listType, endOid);
        }
    }

    /**
     * 查看TCP端口
     *
     * @return
     * @throws IOException
     * @author linwang
     * @createTime 2014-1-2
     */
    public static List<Map<String, Object>> GetTcpInfo() throws IOException {

        List<String> connStateList = new ArrayList<String>();
        List<String> localAddressList = new ArrayList<String>();
        List<String> localPortList = new ArrayList<String>();
        List<String> remoteAddressList = new ArrayList<String>();
        List<String> remotePortList = new ArrayList<String>();

        // 创建 PDU  
        PDU pdu = new PDU();
        pdu.setType(PDU.GETBULK);
        responsepdu = pdu;
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.6.13.1.1")));
        getTCPIteration(connStateList, "1.3.6.1.2.1.6.13.1.1");

        responsepdu.clear();
        responsepdu.add(new VariableBinding(new OID("1.3.6.1.2.1.6.13.1.2")));
        getTCPIteration(localAddressList, "1.3.6.1.2.1.6.13.1.2");

        responsepdu.clear();
        responsepdu.add(new VariableBinding(new OID("1.3.6.1.2.1.6.13.1.3")));
        getTCPIteration(localPortList, "1.3.6.1.2.1.6.13.1.3");

        responsepdu.clear();
        responsepdu.add(new VariableBinding(new OID("1.3.6.1.2.1.6.13.1.4")));
        getTCPIteration(remoteAddressList, "1.3.6.1.2.1.6.13.1.4");

        responsepdu.clear();
        responsepdu.add(new VariableBinding(new OID("1.3.6.1.2.1.6.13.1.5")));
        getTCPIteration(remotePortList, "1.3.6.1.2.1.6.13.1.5");

        int i = 0;
        Map<String, Object> temMap = new HashMap<String, Object>();
        List<Map<String, Object>> temMapList = new ArrayList<Map<String, Object>>();
        String[] state = {"", "closed", "listen", "synSent", "synReceived",
                "established", " finWait1", "finWait2", "closeWait", "lastAck",
                "closing", "timeWait", ""};
        for (String kvp : connStateList) {
            temMap = new HashMap<String, Object>();
            temMap.put("connState", state[toInteger(kvp)]);
            temMap.put("localAddress", localAddressList.get(i));
            temMap.put("localPort", localPortList.get(i));
            temMap.put("remoteAddress", remoteAddressList.get(i));
            temMap.put("remotePort", remotePortList.get(i));
            temMapList.add(temMap);
            i++;
        }

        return temMapList;
    }

    /**
     * 迭代获取TCP端口
     *
     * @param listType
     * @param endOid
     * @throws IOException
     * @author linwang
     * @createTime 2013-12-26
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void getTCPIteration(List listType, String endOid)
            throws IOException {

        ResponseEvent response = snmp.getBulk(responsepdu, target);

        if (response.getResponse() == null) {
            return;
        } else {
            Vector<? extends VariableBinding> recVBs = response.getResponse().getVariableBindings();
            String oid = recVBs.elementAt(0).getOid().toString();

            // 传入终止OID是父节点，如果当前迭代的oid不包含父节点前缀则退出迭代  
            if (!oid.contains(endOid)) {
                return;
            }
            responsepdu = response.getResponse();
            // System.out.println("【获取TCP端口】Received Value is: " +  
            // responsepdu.getVariableBindings());  
            String tem = responsepdu.getVariableBindings().toString();
            tem = tem.split("=")[1].split("]")[0].trim();
            listType.add(tem);

            getTCPIteration(listType, endOid);
        }
    }

    /**
     * 获取UDP端口
     *
     * @return
     * @throws IOException
     * @author linwang
     * @createTime 2014-1-2
     */
    public static List<Map<String, Object>> GetUdpInfo() throws IOException {

        List<String> localAddressList = new ArrayList<String>();
        List<String> localPortList = new ArrayList<String>();

        // 创建 PDU  
        PDU pdu = new PDU();
        pdu.setType(PDU.GETBULK);
        responsepdu = pdu;
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.7.5.1.1")));
        getUDPIteration(localAddressList, "1.3.6.1.2.1.7.5.1.1");
        responsepdu.clear();
        responsepdu.add(new VariableBinding(new OID("1.3.6.1.2.1.7.5.1.2")));
        getUDPIteration(localPortList, "1.3.6.1.2.1.7.5.1.2");

        int i = 0;
        Map<String, Object> temMap = new HashMap<String, Object>();
        List<Map<String, Object>> temMapList = new ArrayList<Map<String, Object>>();
        for (String kvp : localAddressList) {
            temMap = new HashMap<String, Object>();
            temMap.put("localAddress", kvp);
            temMap.put("localPort", localPortList.get(i));
            temMapList.add(temMap);
            i++;
        }

        return temMapList;
    }

    /**
     * 迭代获取UDP端口
     *
     * @param listType
     * @param endOid
     * @throws IOException
     * @author linwang
     * @createTime 2013-12-26
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void getUDPIteration(List listType, String endOid)
            throws IOException {

        ResponseEvent response = snmp.getBulk(responsepdu, target);

        if (response.getResponse() == null) {
            return;
        } else {
            Vector<? extends VariableBinding> recVBs = response.getResponse().getVariableBindings();
            String oid = recVBs.elementAt(0).getOid().toString();

            // 传入终止OID是父节点，如果当前迭代的oid不包含父节点前缀则退出迭代  
            if (!oid.contains(endOid)) {
                return;
            }
            responsepdu = response.getResponse();
            // System.out.println("【获取UDP端口】Received Value is: " +  
            // responsepdu.getVariableBindings());  
            String tem = responsepdu.getVariableBindings().toString();
            tem = tem.split("=")[1].split("]")[0].trim();
            listType.add(tem);

            getUDPIteration(listType, endOid);
        }
    }

    /**
     * 获取流量统计
     *
     * @return
     * @throws IOException
     * @author linwang
     * @createTime 2013-12-26
     */
    public static Map<String, Object> getNetWorkFlowCount() throws IOException {

        // 1.3.6.1.4.1.9600.1.1.3.1.2 流量接收  
        List<Integer> receivedList = new ArrayList<Integer>();
        // 1.3.6.1.4.1.9600.1.1.3.1.3 流量发送  
        List<Integer> sendedList = new ArrayList<Integer>();

        // 创建 PDU  
        PDU pdu = new PDU();
        VariableBinding vb = new VariableBinding();
        vb.setOid(new OID("1.3.6.1.4.1.9600.1.1.3.1.2"));
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.9600.1.1.3.1.2")));
        pdu.setType(PDU.GETBULK);
        responsepdu = pdu;
        getNetWorkFlowIteration(receivedList, "1.3.6.1.4.1.9600.1.1.3.1.2");
        responsepdu.clear();
        responsepdu.add(new VariableBinding(new OID(
                "1.3.6.1.4.1.9600.1.1.3.1.3")));
        getNetWorkFlowIteration(sendedList, "1.3.6.1.4.1.9600.1.1.3.1.3");

        Map<String, Object> temMap = new HashMap<String, Object>();
        int receive = 0;
        String receiveStr = "";
        String sendStr = "";
        int send = 0;
        for (Integer kvp : receivedList) {
            receive += kvp;
        }

        float c = (float) ((float) receive / 1024.0);
        DecimalFormat df = new DecimalFormat("0.00");// 格式化小数，不足的补0  
        receiveStr = df.format(c);// 返回的是String类型的  
        for (Integer kvp : sendedList) {
            send += kvp;
        }

        c = (float) ((float) send / 1024.0);
        sendStr = df.format(c);// 返回的是String类型的  

        temMap.put("receive", receiveStr);
        temMap.put("send", sendStr);

        // System.out.println("获取流量信息：" + temMap);  

        return temMap;
    }

    /**
     * 迭代获取流量信息
     *
     * @param target
     * @param listType
     * @param endOid
     * @throws IOException
     * @author linwang
     * @createTime 2013-12-26
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void getNetWorkFlowIteration(List listType, String endOid)
            throws IOException {

        ResponseEvent response = snmp.getBulk(responsepdu, target);

        if (response.getResponse() == null) {
            return;
        } else {
            Vector<? extends VariableBinding> recVBs = response.getResponse().getVariableBindings();
            String oid = recVBs.elementAt(0).getOid().toString();
            if (!oid.contains(endOid)) {
                return;
            }
            responsepdu = response.getResponse();
            // System.out.println("【获取流量信】Received Value is: " +  
            // responsepdu.getVariableBindings());  
            String tem = responsepdu.getVariableBindings().toString();
            tem = tem.split("=")[1].split("]")[0].trim();
            if (isNum(tem)) {
                listType.add(Integer.parseInt(tem));
            }

            getNetWorkFlowIteration(listType, endOid);
        }
    }

    /**
     * 获取主机名
     *
     * @param ipStr
     * @author JianZC
     * @createTime 2014-7-4
     */
    public static Map<String, Object> getMaster() throws IOException {

        PDU responsepdu = new PDU();
        responsepdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0")));
        responsepdu.setType(PDU.GET);
        ResponseEvent response = snmp.send(responsepdu, target);

        if (response.getResponse() == null) {
        } else {
            responsepdu = response.getResponse();
        }

        String tem = responsepdu.getVariableBindings().toString();
        tem = tem.split("=")[1].split("]")[0].trim();

        Map<String, Object> temMap = new HashMap<String, Object>();
        temMap.put("master", tem);
        return temMap;
    }

    /**
     * 服务器列表根据ip获取对应列表信息
     *
     * @param ipStr
     * @param
     * @author JianZC
     * @createTime 2014-7-4
     */
    public static Map<String, Object> getMasterDetail() {

        Map<String, Object> temMap = new HashMap<String, Object>();

        try {
            Map<String, Object> map = SnmpUtil.getCpuUse();
            int cpu = (Integer) map.get("cpuLoad");
            map = SnmpUtil.getMemoryUse();
            int memory = (Integer) map.get("memoryLoad");
            map = SnmpUtil.getMaster();
            String master = (String) map.get("master");
            int auditFlag = 1;

            if (cpu == -1 || memory == 0) {
                auditFlag = 0;
            } else {
                auditFlag = 1;
            }

            map.put("cpuLoad", cpu);
            map.put("memoryLoad", memory);
            map.put("ip", ipStr);
            map.put("operateIp", ipStr);
            map.put("masterName", master);
            map.put("auditFlag", auditFlag);
            temMap.putAll(map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return temMap;
    }

    /**
     * 判断字符串是否是数字
     *
     * @param str
     * @return
     * @author linwang
     * @createTime 2013-12-26
     */
    public static boolean isNum(String str) {
        return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
    }

    /**
     * Integer.parseInt加上非空判断-为null的设置为0
     *
     * @param str
     * @return
     * @author linwang
     * @createTime 2014-7-12
     */
    public static Integer toInteger(String str) {
        Integer integer = 0;
        if (str == null || !isNum(str)) {
            integer = 0;
        } else {
            integer = new Integer(str.trim());
        }
        return integer;
    }

    /**
     * main测试方法
     *
     * @param args
     * @author linwang
     * @createTime 2014-7-12
     */
    public static void main(String[] args) throws Exception {
        try {
            SnmpUtil.setIpStr("192.168.66.66");
            initComm();
            System.out.println(SnmpUtil.getCpuUse().get("cpuLoad").toString().equals("-1"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}