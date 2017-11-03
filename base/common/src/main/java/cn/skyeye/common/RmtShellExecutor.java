package cn.skyeye.common;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import cn.skyeye.common.process.LineExtracter;
import cn.skyeye.common.process.Processs;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class RmtShellExecutor {

    private static final Logger LOG = Logger.getLogger(RmtShellExecutor.class);

    private Connection conn;
    private String ip;
    private String usr;
    private String psword;
    private String charset = Charset.defaultCharset().toString();

    private static final int TIME_OUT = 1000 * 5 * 60;

    public RmtShellExecutor(String ip, String usr, String pwd) {
        this.ip = ip;
        this.usr = usr;
        this.psword = pwd;
    }

    private boolean login() throws IOException {
        conn = new Connection(ip);
        conn.connect();
        return conn.authenticateWithPassword(usr, psword);
    }

    public void exec(String command, LineExtracter lineExtracter) throws IOException {

        try {
            if (login()) {
                Session session = conn.openSession();
                session.execCommand(command, charset);
                Processs.extractOutMsg(session, TIME_OUT, lineExtracter);
            } else {
                throw new IOException(String.format("login faild。 host = %s, user = %s, pwd = %s.", ip, usr, psword));
            }
        } finally {
            if (conn != null) conn.close();
        }

    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public static void main(String[] args) throws IOException {

        RmtShellExecutor rmtShellExecutor = new RmtShellExecutor("test", "root", "1234567");
        rmtShellExecutor.exec("echo mntr | nc localhost 2185", new LineExtracter() {
            @Override
            public void extract(boolean isError, String line) {
                System.out.println(line);
                ArrayList<String> strings = Lists.newArrayList(line.split("\t"));
                strings.forEach(v -> System.out.println(v));

            }
        });

    }
}