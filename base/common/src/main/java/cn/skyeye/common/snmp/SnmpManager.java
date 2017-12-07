package cn.skyeye.common.snmp;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description:
 *   默认采用UDP传输
 * @author LiXiaoCong
 * @version 2017/11/8 18:51
 */
public class SnmpManager {

    private final Logger logger = Logger.getLogger(SnmpManager.class);

    public enum Protocol{tcp {
        @Override
        public Address targetAddress(String host, int port) {
            return new TcpAddress(String.format("%s/%s", host, port));
        }
    }, udp {
        @Override
        public Address targetAddress(String host, int port) {
            return new UdpAddress(String.format("%s/%s", host, port));
        }
    };
        public abstract Address targetAddress(String host, int port);
        /**
         * 默认端口 161
         * @param host
         * @return
         */
        public Address targetAddress(String host){
            return targetAddress(host, 161);
        }
    }
    public enum Version{
        version1 {
            @Override
            public int getVersion() {
                return 0;
            }
        }, version2c {
            @Override
            public int getVersion() {
                return 1;
            }
        }, version3 {
            @Override
            public int getVersion() {
                return 3;
            }
        };
        public abstract int getVersion();
    }

    public enum SecurityType{
        AUTH_PRIV {
            @Override
            public int getSecurityLevel() {
                return 3;
            }
        }, AUTH_NOPRIV {
            @Override
            public int getSecurityLevel() {
                return 2;
            }
        }, NOAUTH_NOPRIV {
            @Override
            public int getSecurityLevel() {
                return 1;
            }
        };

        public abstract int getSecurityLevel();
    }
    public enum AuthenticationType{SHA, MD5}
    public enum PrivacyType{DES, AES128}

    private Snmp snmp;
    private Protocol protocol;
    private Version version;
    private String community = "public";

    private String targetHost;
    private int targetPort;
    private Address targetAddress;

    private OctetString username;
    private SecurityType securityType = SecurityType.AUTH_NOPRIV;
    private OID authenticationProtocol;
    private OctetString authenticationPassphrase;
    private OID privacyProtocol;
    private OctetString privacyPassphras;

    private AtomicBoolean useAsyn = new AtomicBoolean(false);

    private int retries = -1;
    private long timeout = -1;
    private int maxSizeRequestPDU = -1;
    private Target target;

    private AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * 默认 Protocol.udp Version.version2c
     * @throws IOException
     */
    public SnmpManager() throws IOException {
        this(Protocol.udp, Version.version2c);
    }

    /**
     * 默认Protocol.udp
     * @param version
     * @throws IOException
     */
    public SnmpManager(Version version) throws IOException {
        this(Protocol.udp, version);
    }

    /**  默认 Version.version2c
     * @param protocol
     * @throws IOException
     */
    public SnmpManager(Protocol protocol) throws IOException {
        this(protocol, Version.version2c);
    }

    public SnmpManager(Protocol protocol, Version version) throws IOException {
        this.version = version;
        this.protocol = protocol;
        TransportMapping transportMapping = getTransportMapping(protocol);
        this.snmp = new Snmp(transportMapping);
        if(version ==  Version.version3){
            //设置安全模式
            USM usm = new USM(SecurityProtocols.getInstance(),new OctetString(MPv3.createLocalEngineID()), 0);
            SecurityModels.getInstance().addSecurityModel(usm);
        }
        transportMapping.listen();
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        if(community != null)
            this.community = community;
    }

    public String getUsername() {
        return username == null ? null : username.toString();
    }

    public void setUsername(String username) {
        if(username != null) {
            this.username = new OctetString(username);
        }
    }

    public void setAuthentication(AuthenticationType type, String pwd){
        if(type != null && pwd != null) {
            this.authenticationPassphrase = new OctetString(pwd);
            switch (type) {
                case SHA:
                    this.authenticationProtocol = AuthSHA.ID;
                    break;
                case MD5:
                    this.authenticationProtocol = AuthMD5.ID;
                    break;
            }
        }
    }

    public void setPrivacy(PrivacyType type, String pwd){
        if(type != null && pwd != null) {
            this.privacyPassphras = new OctetString(pwd);
            switch (type) {
                case DES:
                    this.privacyProtocol = PrivDES.ID;
                    break;
                case AES128:
                    this.privacyProtocol = PrivAES128.ID;
                    break;
            }
        }
    }

    public void setSecurityType(SecurityType securityType) {
        if(securityType != null)
            this.securityType = securityType;
    }

    public void setTargetAddress(String host, int port){
        if(host != null && port > -1 && port < 65536){
            this.targetHost = host;
            this.targetPort = port;
            this.targetAddress = protocol.targetAddress(host, port);
        }
    }

    public boolean useAsyn() {
        return useAsyn.get();
    }

    public void openAsyn() {
        this.useAsyn.set(true);
    }

    public void closeAsyn() {
        this.useAsyn.set(false);
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setMaxSizeRequestPDU(int maxSizeRequestPDU) {
        this.maxSizeRequestPDU = maxSizeRequestPDU;
    }

    private TransportMapping getTransportMapping(Protocol protocol) throws IOException {
        TransportMapping transportMapping;
        switch (protocol){
            case tcp:
                transportMapping = new DefaultTcpTransportMapping();
                break;
            case udp:
                transportMapping = new DefaultUdpTransportMapping();
                break;
            default:
                transportMapping = new DefaultUdpTransportMapping();
                break;
        }
        return transportMapping;
    }

    private void initialize(){
        if(!initialized.get()){
            if (version == Version.version3) {
                // 创建用户
                UsmUser user = new UsmUser(username,
                        authenticationProtocol,
                        authenticationPassphrase,
                        privacyProtocol,
                        privacyPassphras);
                //添加 用户
                snmp.getUSM().addUser(username, user);

                target = new UserTarget();
                // 设置安全级别
                target.setSecurityLevel(securityType.getSecurityLevel());
                target.setSecurityName(username);
            } else {
                target = new CommunityTarget();
                ((CommunityTarget) target).setCommunity(new OctetString(community));
            }
            // 目标对象相关设置
            target.setVersion(version.getVersion());
            target.setAddress(targetAddress);
            if(retries > 0)target.setRetries(retries);
            if(timeout > 0)target.setTimeout(timeout);
            if(maxSizeRequestPDU > 0)target.setMaxSizeRequestPDU(maxSizeRequestPDU);

            logger.info(String.format("snmp连接参数如下：\n\t %s", toString()));

            initialized.set(true);
        }
    }

    public void sendMessage(PDU pdu,
                            ResponseHandler handler) throws IOException {
        sendMessage(pdu, handler, null);
    }
    public void sendMessage(PDU pdu,
                            ResponseHandler handler,
                            Object userHandle) throws IOException {
        Preconditions.checkNotNull(pdu, "pdu不能为null。");
        Preconditions.checkNotNull(handler, "handler不能为null。");
        if(!initialized.get())initialize();

        if (!useAsyn.get()) {
            // 发送报文 并且接受响应
            handler.extractResponse(snmp.send(pdu, target));
        } else {
            snmp.send(pdu, target, userHandle, handler);
        }
    }

    public static abstract class ResponseHandler implements ResponseListener{
        @Override
        public void onResponse(ResponseEvent event) {
            extractResponse(event);
        }

        public abstract void extractResponse(ResponseEvent event);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("protocol=").append(protocol);
        sb.append(", version=").append(version);
        sb.append(", community='").append(community).append('\'');
        sb.append(", targetHost='").append(targetHost).append('\'');
        sb.append(", targetPort=").append(targetPort);
        sb.append(", targetAddress=").append(targetAddress);
        sb.append(", username=").append(username);
        sb.append(", securityType=").append(securityType);
        sb.append(", authenticationProtocol=").append(authenticationProtocol);
        sb.append(", authenticationPassphrase=").append(authenticationPassphrase);
        sb.append(", privacyProtocol=").append(privacyProtocol);
        sb.append(", privacyPassphras=").append(privacyPassphras);
        sb.append(", useAsyn=").append(useAsyn);
        sb.append(", retries=").append(retries);
        sb.append(", timeout=").append(timeout);
        sb.append(", maxSizeRequestPDU=").append(maxSizeRequestPDU);
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {

        SnmpManager snmpManager = new SnmpManager(Protocol.udp, Version.version2c);
        snmpManager.setTargetAddress("192.168.66.66", 161);
        snmpManager.setCommunity("public");

        PDU pdu = new PDU();
        pdu.setType(PDU.GETBULK);
        OID oid = new OID("1.3.6.1.4.1.2021.9.1.7");
        pdu.add(new VariableBinding(oid));
        pdu.setMaxRepetitions(1);

        snmpManager.sendMessage(pdu, new ResponseHandler() {
            @Override
            public void extractResponse(ResponseEvent event) {
                PDU response = event.getResponse();
                response.getVariableBindings().forEach(ver -> System.out.println(ver.getVariable().toString()));
            }
        });

    }
}
