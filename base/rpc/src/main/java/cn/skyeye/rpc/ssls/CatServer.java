package cn.skyeye.rpc.ssls;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
  
public class CatServer implements Runnable, HandshakeCompletedListener {  
  
    public static final int SERVER_PORT = 11123;  
  
    private final Socket _s;  
    private String peerCerName;  
  
    public CatServer(Socket s) {  
        _s = s;  
    }  
  
    public static void main(String[] args) throws Exception {

        String serverKeyStoreFile = "/ssl/sslserver.keystore";
        String serverKeyStorePwd = "ssllss456.#";
        String catServerKeyPwd = "ssllss123.!@#";

        String serverTrustKeyStoreFile = "/ssl/sslservertrust.keystore";
        String serverTrustKeyStorePwd = "sslttlss456.#";
  
        KeyStore serverKeyStore = KeyStore.getInstance("JKS");  
        serverKeyStore.load(CatServer.class.getResourceAsStream(serverKeyStoreFile), serverKeyStorePwd.toCharArray());
  
        KeyStore serverTrustKeyStore = KeyStore.getInstance("JKS");  
        serverTrustKeyStore.load(CatServer.class.getResourceAsStream(serverTrustKeyStoreFile), serverTrustKeyStorePwd.toCharArray());
  
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());  
        kmf.init(serverKeyStore, catServerKeyPwd.toCharArray());  
  
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());  
        tmf.init(serverTrustKeyStore);  
  
        SSLContext sslContext = SSLContext.getInstance("TLSv1");  
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);  
  
        SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();  
        SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(SERVER_PORT);  
        sslServerSocket.setNeedClientAuth(true);  
  
        while (true) {  
            SSLSocket s = (SSLSocket)sslServerSocket.accept();  
            CatServer cs = new CatServer(s);  
            s.addHandshakeCompletedListener(cs);  
            new Thread(cs).start();  
        }  
    }  
  
    @Override  
    public void run() {  
        try {  
            BufferedReader reader = new BufferedReader(new InputStreamReader(_s.getInputStream()));  
            PrintWriter writer = new PrintWriter(_s.getOutputStream(), true);  
  
            writer.println("Welcome~, enter exit to leave.");  
            String s;  
            while ((s = reader.readLine()) != null && !s.trim().equalsIgnoreCase("exit")) {  
                writer.println("Echo: " + s);  
            }  
            writer.println("Bye~, " + peerCerName);  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            try {  
                _s.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }  
  
    @Override  
    public void handshakeCompleted(HandshakeCompletedEvent event) {  
        try {  
            X509Certificate cert = (X509Certificate) event.getPeerCertificates()[0];  
            peerCerName = cert.getSubjectX500Principal().getName();  
        } catch (SSLPeerUnverifiedException ex) {  
            ex.printStackTrace();  
        }  
    }  
  
}